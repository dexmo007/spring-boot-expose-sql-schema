package com.dexmohq.springboot.sqlschema.model;

import lombok.Value;

import java.util.Set;

@Value
public class TableDef {

    String name;

    Set<ColumnDef> columns;

    PrimaryKeyDef primaryKey;

    Set<String> checkConstraints;

    Set<ForeignKeyDef> foreignKeys;

    Set<IndexDef> indexes;

}
