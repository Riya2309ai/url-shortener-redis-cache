package com.riya.urlshortner.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "id_sequence")
public class IdSequence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long  id;

    public Long getId() {
        return id;
    }

}
