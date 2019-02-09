package com.dexmohq.springboot.sqlschema;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
public class BarcodeType {
    @Id
    private Long id;
    private String name;
    private String regex;

}
