package com.dexmohq.springboot.sqlschema.model;

import lombok.Value;

@Value
public class ColumnDef {
    String name;
    String type;
    int size;
    boolean nullable;
    boolean unique;
    boolean formula;
}
