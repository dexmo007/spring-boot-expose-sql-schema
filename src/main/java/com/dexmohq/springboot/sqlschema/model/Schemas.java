package com.dexmohq.springboot.sqlschema.model;

import java.util.HashMap;
import java.util.Map;

public class Schemas extends HashMap<String, Schema> {

    public Schemas(Map<? extends String, ? extends Schema> m) {
        super(m);
    }
}
