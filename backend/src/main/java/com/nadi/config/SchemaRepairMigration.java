package com.nadi.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

/**
 * Repairs columns that Hibernate {@code ddl-auto: update} cannot widen.
 *
 * Several entities declare {@code @Column(columnDefinition = "TEXT")} for
 * photo/logo fields, but the production tables were created earlier with
 * {@code varchar(255)} — and {@code update} never alters existing column
 * types. Result: every photo save failed with
 * {@code value too long for type character varying(255)} (HTTP 500).
 *
 * This runner widens those columns to TEXT on boot. Widening varchar to
 * TEXT never loses data, runs only when needed (checked via metadata),
 * and is a harmless no-op when the column is already TEXT.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SchemaRepairMigration implements ApplicationRunner {

    private final DataSource dataSource;

    private static final List<String[]> TEXT_COLUMNS = List.of(
            new String[]{"academie", "logo_url"},
            new String[]{"document", "fichier_url"},
            new String[]{"entraineur", "photo_url"},
            new String[]{"joueur", "photo_url"},
            new String[]{"joueur", "postes_secondaires"}
    );

    @Override
    public void run(ApplicationArguments args) {
        try (Connection connection = dataSource.getConnection()) {
            boolean postgres = connection.getMetaData().getDatabaseProductName()
                    .toLowerCase().contains("postgres");
            for (String[] tableColumn : TEXT_COLUMNS) {
                widenIfNeeded(connection, postgres, tableColumn[0], tableColumn[1]);
            }
        } catch (Exception e) {
            log.error("Schema repair check failed, continuing startup", e);
        }
    }

    private void widenIfNeeded(Connection connection, boolean postgres, String table, String column) {
        try {
            String typeName = currentType(connection, table, column);
            if (typeName == null) {
                log.warn("Schema repair: {}.{} not found, skipping", table, column);
                return;
            }
            String upper = typeName.toUpperCase();
            if (upper.contains("TEXT") || upper.contains("CLOB") || upper.contains("OBJECT")) {
                return;
            }
            String sql = postgres
                    ? "ALTER TABLE " + table + " ALTER COLUMN " + column + " TYPE TEXT"
                    : "ALTER TABLE " + table + " ALTER COLUMN " + column + " SET DATA TYPE TEXT";
            try (Statement statement = connection.createStatement()) {
                statement.execute(sql);
            }
            log.info("Schema repair: widened {}.{} from {} to TEXT", table, column, typeName);
        } catch (Exception e) {
            log.warn("Schema repair: could not widen {}.{}: {}", table, column, e.getMessage());
        }
    }

    private String currentType(Connection connection, String table, String column) {
        // Metadata matching can be case-sensitive depending on the database
        // (H2 stores unquoted identifiers uppercase, PostgreSQL lowercase).
        for (String[] candidate : new String[][]{{table, column}, {table.toUpperCase(), column.toUpperCase()}}) {
            try (ResultSet rs = connection.getMetaData().getColumns(null, null, candidate[0], candidate[1])) {
                if (rs.next()) {
                    return rs.getString("TYPE_NAME");
                }
            } catch (Exception e) {
                log.warn("Schema repair: metadata lookup failed for {}.{}: {}", table, column, e.getMessage());
            }
        }
        return null;
    }
}
