package com.dexmohq.springboot.sqlschema.example.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Entity
public class Address {

    @Id
    @GeneratedValue
    private Long id;

    private String street;

    @Column(length = 10)
    private String houseNumber;

    @Column(length = 10)
    private String zip;

}
