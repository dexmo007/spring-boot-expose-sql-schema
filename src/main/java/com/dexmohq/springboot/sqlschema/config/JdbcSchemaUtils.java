package com.dexmohq.springboot.sqlschema.config;

import com.dexmohq.springboot.sqlschema.model.*;
import lombok.experimental.UtilityClass;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@UtilityClass
public class JdbcSchemaUtils {

    private Set<ColumnDef> retrieveColumnDefs(DatabaseMetaData metaData, String tableCatalog, String tableSchema, String tableName) throws SQLException {
        final Set<ColumnDef> columnDefs = new HashSet<>();
        final ResultSet columns = metaData.getColumns(tableCatalog, tableSchema, tableName, "%");
        while (columns.next()) {
            final String name = columns.getString("COLUMN_NAME");
            final String type = columns.getString("TYPE_NAME");
            final int size = columns.getInt("COLUMN_SIZE");
            columnDefs.add(new ColumnDef(name, type, size, false, false, false));
        }
        return columnDefs;
    }

    private PrimaryKeyDef retrievePrimaryKey(DatabaseMetaData metaData, String tableCatalog, String tableSchema, String tableName) throws SQLException {
        final ResultSet primaryKeys = metaData.getPrimaryKeys(tableCatalog, tableSchema, tableName);
        final ArrayList<String> primaryKeyColumns = new ArrayList<>();
        String pkName = null;
        while (primaryKeys.next()) {
            primaryKeyColumns.add(primaryKeys.getString("COLUMN_NAME"));
            if (pkName == null) {
                pkName = primaryKeys.getString("PK_NAME");
            }
        }
        return new PrimaryKeyDef(pkName, primaryKeyColumns);
    }

    private Set<ForeignKeyDef> retrieveForeignKeys(DatabaseMetaData metaData, String tableCatalog, String tableSchema, String tableName) throws SQLException {
        final ResultSet importedKeys = metaData.getImportedKeys(tableCatalog, tableSchema, tableName);
        final HashMap<String, ForeignKeyDef> foreignKeyDefs = new HashMap<>();
        while (importedKeys.next()) {
            final String name = importedKeys.getString("FK_NAME");
            final String referencedTable = importedKeys.getString("FKTABLE_NAME");
            final ForeignKeyDef def = foreignKeyDefs.computeIfAbsent(name, n -> new ForeignKeyDef(n, new ArrayList<>(), referencedTable, new ArrayList<>()));
            def.getColumns().add(importedKeys.getString("PKCOLUMN_NAME"));
            def.getReferencedColumns().add(importedKeys.getString("FKCOLUMN_NAME"));
        }
        return new HashSet<>(foreignKeyDefs.values());
    }

    private Set<IndexDef> retrieveIndexes(DatabaseMetaData metaData, String tableCatalog, String tableSchema, String tableName) throws SQLException {
        final HashMap<String, IndexDef> indexDefs = new HashMap<>();
        final ResultSet indexInfos = metaData.getIndexInfo(tableCatalog, tableSchema, tableName, false, false);
        while (indexInfos.next()) {
            final String indexName = indexInfos.getString("INDEX_NAME");
            final String columnName = indexInfos.getString("COLUMN_NAME");
            final boolean unique = !indexInfos.getBoolean("NON_UNIQUE");
            final String ascOrDesc = indexInfos.getString("ASC_OR_DESC");
            final IndexDef.Order order;
            if (ascOrDesc == null) {
                order = null;
            } else {
                switch (ascOrDesc) {
                    case "A":
                        order = IndexDef.Order.ASC;
                        break;
                    case "D":
                        order = IndexDef.Order.DESC;
                        break;
                    default:
                        order = null;
                }
            }
            final IndexDef indexDef = indexDefs.computeIfAbsent(indexName, in -> new IndexDef(in, new ArrayList<>(), unique, order));
            // todo assure correct order of columns
            indexDef.getColumns().add(columnName);
        }
        return new HashSet<>(indexDefs.values());
    }

    public Schema fromMetadata(DatabaseMetaData metaData, String catalogName) throws SQLException {
        final String dialect = metaData.getDatabaseProductName();
        final ArrayList<TableDef> tableDefs = new ArrayList<>();

        ResultSet resultSet = metaData.getTables(catalogName, null, "%", new String[]{"TABLE"});
        while (resultSet.next()) {
            String tableName = resultSet.getString(3);

            String tableCatalog = resultSet.getString(1);
            String tableSchema = resultSet.getString(2);

            // todo check constraints

            tableDefs.add(new TableDef(
                    tableCatalog + "." + tableSchema + "." + tableName,
                    retrieveColumnDefs(metaData, tableCatalog, tableSchema, tableName),
                    retrievePrimaryKey(metaData, tableCatalog, tableSchema, tableName),
                    null,
                    retrieveForeignKeys(metaData, tableCatalog, tableSchema, tableName),
                    retrieveIndexes(metaData, tableCatalog, tableSchema, tableName)
            ));
        }

        return new Schema(dialect, tableDefs);
    }

}
