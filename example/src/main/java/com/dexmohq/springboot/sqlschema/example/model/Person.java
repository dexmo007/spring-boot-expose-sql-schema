package com.dexmohq.springboot.sqlschema.example.model;

import lombok.Data;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.Formula;

import javax.persistence.*;

@Data
@Entity
@Check(constraints = "LENGTH(TRIM(LAST_NAME)) = LENGTH(LAST_NAME)")
@Table(indexes = @Index(columnList = "LAST_NAME, FIRST_NAME"))
public class Person {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "LAST_NAME", nullable = false)
    private String lastName;

    @Column(name = "FIRST_NAME", nullable = false)
    private String firstName;

    @Column(unique = true)
    private String mail;

    @ManyToOne
    private Address address;

    @Formula("CONCAT(FIRST_NAME, ' ', LAST_NAME)")
    @Transient
    private String fullName;

}
