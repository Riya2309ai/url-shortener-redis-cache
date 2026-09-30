package com.riya.urlshortner.repository;

import com.riya.urlshortner.entity.IdSequence;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdSequenceRepository extends JpaRepository<IdSequence, Long> {

}
