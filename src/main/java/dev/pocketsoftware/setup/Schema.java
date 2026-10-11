package dev.pocketsoftware.setup;

import java.util.List;

/**
 * Per-system database ids and schema lookup. Each chosen system gets its own
 * database ('school', 'hospital', 'business'); table definitions live in
 * SchoolSchema, HospitalSchema and BusinessSchema.
 */
final class Schema {

    static final String SCHOOL = "school";
    static final String HOSPITAL = "hospital";
    static final String BUSINESS = "business";
    static final List<String> ALL = List.of(SCHOOL, HOSPITAL, BUSINESS);

    /** One column: key, type (string/integer/float), size, required, default. */
    record Column(String key, String type, int size, boolean required, String dflt) {
    }

    /** One table: id, display name, columns. */
    record Table(String id, String name, List<Column> columns) {
    }

    /** Database id for a system, e.g. school or pocket_test_school. */
    static String databaseId(String system, String prefix) {
        return prefix + system;
    }

    /** Display name for a system's database. */
    static String databaseName(String system) {
        return switch (system) {
            case SCHOOL -> "School";
            case HOSPITAL -> "Hospital";
            case BUSINESS -> "Business";
            default -> throw new ProvisioningException("Unknown system: " + system);
        };
    }

    /** Tables for a system. */
    static List<Table> tablesFor(String system) {
        return switch (system) {
            case SCHOOL -> SchoolSchema.tables();
            case HOSPITAL -> HospitalSchema.tables();
            case BUSINESS -> BusinessSchema.tables();
            default -> throw new ProvisioningException("Unknown system: " + system);
        };
    }

    private Schema() {
    }
}
