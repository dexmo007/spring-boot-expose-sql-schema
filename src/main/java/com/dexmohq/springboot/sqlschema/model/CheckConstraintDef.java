package com.dexmohq.springboot.sqlschema.model;

import lombok.Value;

@Value
public class CheckConstraintDef {

    String name;

    String checkExpression;

}
