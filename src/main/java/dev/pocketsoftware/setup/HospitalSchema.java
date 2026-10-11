package dev.pocketsoftware.setup;

import java.util.List;

/** Hospital tables and columns: patients, visits, appointments, prescriptions, bills, payments. */
final class HospitalSchema {

    static List<Schema.Table> tables() {
        return List.of(
                new Schema.Table("patients", "Patients", List.of(
                        col("fullName", "string", 128, true, null),
                        col("phone", "string", 20, true, null),
                        col("gender", "string", 10, false, null),
                        col("age", "integer", 0, false, null),
                        col("address", "string", 255, false, null),
                        col("status", "string", 16, false, null))),
                new Schema.Table("visits", "Visits", List.of(
                        col("patientId", "string", 36, true, null),
                        col("visitDate", "string", 20, true, null),
                        col("complaint", "string", 512, false, null),
                        col("diagnosis", "string", 255, false, null),
                        col("treatment", "string", 512, false, null),
                        col("doctor", "string", 128, false, null),
                        col("status", "string", 16, false, null))),
                new Schema.Table("appointments", "Appointments", List.of(
                        col("patientId", "string", 36, true, null),
                        col("date", "string", 20, true, null),
                        col("time", "string", 10, false, null),
                        col("doctor", "string", 128, false, null),
                        col("status", "string", 16, true, null))),
                new Schema.Table("prescriptions", "Prescriptions", List.of(
                        col("patientId", "string", 36, true, null),
                        col("visitId", "string", 36, false, null),
                        col("medicine", "string", 255, true, null),
                        col("dosage", "string", 128, false, null),
                        col("instructions", "string", 255, false, null),
                        col("status", "string", 16, false, null))),
                new Schema.Table("bills", "Bills", List.of(
                        col("patientId", "string", 36, true, null),
                        col("description", "string", 255, true, null),
                        col("amount", "float", 0, true, null),
                        col("status", "string", 16, true, null))),
                new Schema.Table("payments", "Payments", List.of(
                        col("patientId", "string", 36, true, null),
                        col("amount", "float", 0, true, null),
                        col("method", "string", 32, false, null),
                        col("date", "string", 20, false, null),
                        col("note", "string", 255, false, null))));
    }

    private static Schema.Column col(String key, String type, int size, boolean required, String dflt) {
        return new Schema.Column(key, type, size, required, dflt);
    }

    private HospitalSchema() {
    }
}
