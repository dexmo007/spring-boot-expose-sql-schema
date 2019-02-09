package com.dexmohq.springboot.sqlschema;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class SchemaController {

    @GetMapping("/reporting/schema")
    public Map<String, List<String>> getReportingSchema() {
        return DatabaseMetadataIntegrator.getMetadataMap();
    }

    @GetMapping("/reporting/schema/full")
    public DatabaseMetadataIntegrator.Schema getReportingSchemaFull() {
        return DatabaseMetadataIntegrator.getSchema();
    }

}
