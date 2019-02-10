package com.dexmohq.springboot.sqlschema.model;

import lombok.Value;

import java.util.List;

/**
 * @author Henrik Drefs
 */
@Value
public class Schema {
    String dialect;
    List<TableDef> tables;
}
