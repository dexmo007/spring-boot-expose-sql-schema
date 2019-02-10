package com.dexmohq.springboot.sqlschema.model;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Value
public class PrimaryKeyDef extends ConstraintDef {
    public PrimaryKeyDef(String name, List<String> columns) {
        super(name, columns);
    }
}
