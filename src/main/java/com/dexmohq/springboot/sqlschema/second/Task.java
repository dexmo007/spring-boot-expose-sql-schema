package com.dexmohq.springboot.sqlschema.second;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Data
public class Task {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

}
