package com.riya.urlshortner.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class Base62EncoderTest {

    @Test
    void Base62EncoderTest(){
        assertEquals("0", Base62Encoder.encode(0));
    }

    @Test
    void encodesSmallNumbersAsSingleCharacters(){
        assertEquals("1", Base62Encoder.encode(1));
        assertEquals("9", Base62Encoder.encode(9));
        assertEquals("a", Base62Encoder.encode(10));
        assertEquals("z", Base62Encoder.encode(35));
        assertEquals("A", Base62Encoder.encode(36));
    }

    @Test
    void encodingIsDeterministic() {
        assertEquals(Base62Encoder.encode(12345), Base62Encoder.encode(12345));
    }

    @Test
    void differentInputsProduceDifferentOutputs() {
        assertNotEquals(Base62Encoder.encode(100), Base62Encoder.encode(101));
    }

    @Test
    void rollsOverToTwoCharactersPast61() {
        // 0-61 are single characters (62 of them, indices 0-61).
        // 62 is the first two-character code: "10" in base62.
        assertEquals(2, Base62Encoder.encode(62).length());
    }
}