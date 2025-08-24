package com.example.bankcards.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@Entity
public class Role {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence_role")
    @SequenceGenerator(name = "sequence_role", sequenceName = "role_main_sequence", allocationSize = 1)
    private long id;

    @Column(name = "name")
    private String name;
}
