package com.dexmohq.springboot.sqlschema.example;

import com.dexmohq.springboot.sqlschema.config.ExposeSchemaConfigurer;
import com.dexmohq.springboot.sqlschema.example.model.Person;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExposeSchemaConfig implements ExposeSchemaConfigurer {

    @Override
    public void configure(ExposeSchemaConfig config) {
        config.exclude(Person.class);
    }
}
