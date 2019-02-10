package com.dexmohq.springboot.sqlschema.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

@EqualsAndHashCode(callSuper = true)
@Value
@JsonInclude(NON_EMPTY)
public class ForeignKeyDef extends ConstraintDef {

    String referencedTable;

    List<String> referencedColumns;

    public ForeignKeyDef(String name, List<String> columns, String referencedTable, List<String> referencedColumns) {
        super(name, columns);
        this.referencedTable = referencedTable;
        this.referencedColumns = referencedColumns;
    }

    public boolean isReferenceToPrimaryKey() {
        return referencedColumns == null || referencedColumns.isEmpty();
    }

}
