package com.dexmohq.springboot.sqlschema.model;

import lombok.Value;
import org.hibernate.mapping.Table;

import java.util.List;

/**
 * @author Henrik Drefs
 */
@Value
public class Schema {
    String dialect;
    List<Table> tables;
}
