package com.dexmohq.springboot.sqlschema.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashSet;
import java.util.Set;

@Data
@ConfigurationProperties(prefix = "schema.expose")
public class ExposeSchemaProperties {

    private String basePath = "/schema";

    private Set<String> persistenceUnits = new HashSet<>();

    private Set<String> include = new HashSet<>();

    private Set<String> exclude = new HashSet<>();

    private boolean introspect = false;

}
