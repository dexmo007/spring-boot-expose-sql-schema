package com.dexmohq.springboot.sqlschema;

import com.dexmohq.springboot.sqlschema.model.Schemas;
import lombok.Value;
import lombok.extern.apachecommons.CommonsLog;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@CommonsLog
public class SchemaController {

    @Autowired
    private Schemas schemas;

    @PersistenceContext(unitName = "second")
    private EntityManager entityManager;

    @Transactional
    @GetMapping("${schema.expose.base-path:/schema}/{unit}")
    public HashMap<String, List<ColumnDef>> getReportingSchemaFull(@PathVariable("unit") String unit) {
        final HashMap<String, List<ColumnDef>> tableDefs = new HashMap<>();
        entityManager.unwrap(Session.class).doWork(connection -> {
            final DatabaseMetaData metaData = connection.getMetaData();
            String[] types = { "TABLE" };
            final String catalogName = DatabaseMetadataIntegrator.getMetadataMap().get(unit).getDatabase().getJdbcEnvironment().getCurrentCatalog().getText();
            ResultSet resultSet = metaData.getTables(catalogName, null, "%", types);
            while (resultSet.next()) {
                String tableName = resultSet.getString(3);

                String tableCatalog = resultSet.getString(1);
                String tableSchema = resultSet.getString(2);
                log.info("Schema introspection: found " + tableCatalog + "." + tableSchema + "." + tableName);

                final ArrayList<ColumnDef> columnDefs = new ArrayList<>();
                final ResultSet columns = metaData.getColumns(tableCatalog, tableSchema, tableName, "%");
                while (columns.next()) {
                    final String name = columns.getString("COLUMN_NAME");
                    final String type = columns.getString("TYPE_NAME");
                    final int size = columns.getInt("COLUMN_SIZE");
                    columnDefs.add(new ColumnDef(name, type, size));
                }
                tableDefs.put(tableCatalog + "." + tableSchema + "." + tableName, columnDefs);
            }
        });
        return tableDefs;
    }

    @Value
    private static class ColumnDef {
        String name;
        String type;
        int size;
    }

}
