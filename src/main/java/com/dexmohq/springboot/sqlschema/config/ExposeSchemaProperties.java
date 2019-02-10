package com.dexmohq.springboot.sqlschema.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.Set;

@Data
@ConfigurationProperties(prefix = "schema.expose")
public class ExposeSchemaProperties {

    private String basePath = "/schema";

    private Set<String> persistenceUnits = Collections.emptySet();

    private Set<String> include = Collections.emptySet();

    private Set<String> exclude = Collections.emptySet();

    private boolean introspect = false;

}
