package com.dexmohq.springboot.sqlschema.controller;

import com.dexmohq.springboot.sqlschema.model.Schema;
import com.dexmohq.springboot.sqlschema.model.Schemas;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

@RestController
@CommonsLog
@RequiredArgsConstructor
public class SchemaController {

    private final Schemas schemas;

    @PostConstruct
    public void init() {
        log.info("Exposing schemas at /...");
    }

    @GetMapping("${schema.expose.base-path:/schema}/{unit}")
    public ResponseEntity<Schema> getReportingSchemaFull(@PathVariable("unit") String unit) {
        final Schema schema = schemas.get(unit);
        if (schema == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(schema);
    }

}
