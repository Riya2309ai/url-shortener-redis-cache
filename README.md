# URL Shortener

        A URL shortener built to demonstrate backend system design: caching strategy, cache-aside vs. write-through, database-enforced uniqueness under concurrency, and async writes on a hot path.

        ## Features
        - Shorten a long URL to a short code (auto-generated via Base62, or a custom alias)
        - Redirect from short code to original URL
        - Redis caching for redirects (cache-aside pattern)
        - Async click counting (no synchronous DB write on the read path)
        - Fully containerized: app + MySQL + Redis via Docker Compose

        ## Tech stack
        Java 21, Spring Boot 4.1.1, MySQL 8.4, Redis 7, Docker Compose, JUnit 5 + Mockito

        ## Architecture

        \`\`\`
        Client → POST /api/shorten → Base62 encode / custom alias → MySQL (insert) + Redis (write-through cache)

        Client → GET /{code} → Redis (cache hit?) → [miss] MySQL → backfill Redis → 302 redirect
        ↳ Redis INCR (async click count)
        ↳ every 5s: flush to MySQL
        \`\`\`

        ## Design decisions and trade-offs

        **Base62 over hashing (MD5/SHA).** Encoding an auto-increment ID is deterministic and collision-free by construction. Hashing the URL directly needs collision-handling and retry logic for no real benefit at this scale.

        **Two ID spaces: `id_sequence` is separate from `url_mapping`.** Custom aliases don't consume a counter slot. If the counter and the data table were the same, every custom alias would create a gap in the sequence.

        **Write-through caching on create, not just on read.** A freshly created link is often clicked within seconds. Caching only on the first read guarantees a cold miss on that first click; write-through avoids it.

        **Database-enforced uniqueness (`INSERT`, not `save()`/upsert).** Spring Data's `save()` on an entity with a manually-set ID performs an upsert. Two simultaneous requests for the same custom alias could silently overwrite each other with no error. A native `INSERT` lets MySQL's primary key constraint reject the second one, which turns a silent data-loss bug into a clean `409`.

        **Async click counting, not a synchronous `UPDATE` per redirect.** Counting clicks synchronously would put a MySQL write on the highest-traffic path in the system. Clicks are instead `INCR`'d in Redis and flushed to MySQL every 5 seconds. This makes click counts eventually consistent — up to 5 seconds of lag, and up to 5 seconds of counts lost if Redis crashes before a flush. That trade-off is intentional: redirect latency matters more than real-time analytics for this use case. A durable alternative would be publishing click events to a queue (e.g., Kafka) for guaranteed delivery.

        **`302 Found`, not `301 Moved Permanently`, on redirect.** A 301 gets cached by browsers, which then skip the server on future clicks — breaking click counting entirely. 302 forces every click through the app.

        **Monolith for now, not separate read/write services.** Read and write traffic patterns genuinely differ (redirects vastly outnumber creates), which is a real reason to eventually split them into independently scalable services. For this project's scope, a monolith with a clear internal read/write separation (distinct service classes) captures the design reasoning without the operational overhead of two deployables. The split is a natural next step, not a limitation of the design.

        ## Running locally

        Requires Docker Desktop.

        \`\`\`bash
        git clone <your-repo-url>
    cd urlshortner
    docker compose up -d --build
    \`\`\`

    This starts MySQL, Redis, and the app together. The app is available at `http://localhost:8080`.

    ## API examples

    **Create a short URL (auto-generated code):**
    \`\`\`bash
    curl -X POST http://localhost:8080/api/shorten \\
    -H "Content-Type: application/json" \\
    -d '{"longUrl":"https://example.com"}'
    \`\`\`

    **Create with a custom alias:**
    \`\`\`bash
    curl -X POST http://localhost:8080/api/shorten \\
    -H "Content-Type: application/json" \\
    -d '{"longUrl":"https://github.com","customAlias":"gh"}'
    \`\`\`

    **Redirect:**
    \`\`\`bash
    curl -i http://localhost:8080/gh
    \`\`\`

    ## Running tests

    \`\`\`bash
    ./mvnw test
    \`\`\`

    ## Notes
    - Database credentials (`root`/`root`) are for local development only.
    - Config (DB host, Redis host, base URL) is externalized via environment variables — see `docker-compose.yml`.