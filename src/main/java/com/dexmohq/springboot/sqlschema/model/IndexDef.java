package com.dexmohq.springboot.sqlschema.model;

import lombok.Value;

import java.util.List;

@Value
public class IndexDef {

    String name;

    List<String> columns;

    boolean unique;

    Order order;

    public enum Order {
        ASC, DESC;
    }

}
