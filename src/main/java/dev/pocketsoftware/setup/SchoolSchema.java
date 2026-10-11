package dev.pocketsoftware.setup;

import java.util.List;

/**
 * School tables and columns, mirroring the live 'school' database exactly
 * (no guardianPhone on students; classId 36; text/intent/details sizes as
 * deployed) so running the wizard against an existing installation is a
 * pure no-op.
 */
final class SchoolSchema {

    static List<Schema.Table> tables() {
        return List.of(
                new Schema.Table("students", "Students", List.of(
                        col("phone", "string", 20, true, null),
                        col("fullName", "string", 128, true, null),
                        col("classId", "string", 36, false, null),
                        col("guardianName", "string", 128, false, null),
                        col("admissionNo", "string", 36, false, null),
                        col("status", "string", 16, false, "active"))),
                new Schema.Table("identities", "Identities", List.of(
                        col("channel", "string", 16, true, null),
                        col("channelId", "string", 64, true, null),
                        col("studentId", "string", 36, true, null))),
                new Schema.Table("message_log", "Message Log", List.of(
                        col("phone", "string", 20, true, null),
                        col("direction", "string", 16, true, null),
                        col("channel", "string", 16, false, null),
                        col("text", "string", 1000, false, null),
                        col("intent", "string", 36, false, null),
                        col("confidence", "float", 0, false, null))),
                new Schema.Table("audit_log", "Audit Log", List.of(
                        col("actor", "string", 36, true, null),
                        col("action", "string", 64, true, null),
                        col("details", "string", 1000, false, null))),
                new Schema.Table("classes", "Classes", List.of(
                        col("name", "string", 20, true, null),
                        col("teacherPhone", "string", 20, false, null))),
                new Schema.Table("staff", "Staff", List.of(
                        col("phone", "string", 20, true, null),
                        col("name", "string", 128, true, null),
                        col("role", "string", 16, true, null))),
                new Schema.Table("grades", "Grades", List.of(
                        col("studentId", "string", 36, true, null),
                        col("subject", "string", 64, true, null),
                        col("term", "string", 20, true, null),
                        col("score", "integer", 0, true, null),
                        col("enteredBy", "string", 20, false, null))),
                new Schema.Table("attendance", "Attendance", List.of(
                        col("studentId", "string", 36, true, null),
                        col("date", "string", 10, true, null),
                        col("status", "string", 16, true, null))),
                new Schema.Table("courses", "Courses", List.of(
                        col("name", "string", 64, true, null),
                        col("code", "string", 16, false, null),
                        col("teacher", "string", 128, false, null))),
                new Schema.Table("fees", "Fees", List.of(
                        col("studentId", "string", 36, true, null),
                        col("type", "string", 16, true, null),
                        col("amount", "integer", 0, true, null),
                        col("description", "string", 128, false, null),
                        col("enteredBy", "string", 20, false, null))));
    }

    private static Schema.Column col(String key, String type, int size, boolean required, String dflt) {
        return new Schema.Column(key, type, size, required, dflt);
    }

    private SchoolSchema() {
    }
}
