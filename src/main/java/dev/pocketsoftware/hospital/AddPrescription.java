package dev.pocketsoftware.hospital;

/** Parses "add prescription <patient> <medicine> [dosage]" and saves it. */
public final class AddPrescription {

    private AddPrescription() {
    }

    /** Handles one add-prescription command; returns the chat reply. */
    static String handle(PrescriptionStore prescriptions, String text) {
        String body = text.trim().substring("add prescription ".length()).trim();
        String[] parts = body.split("\\s+");
        if (parts.length < 2) {
            return "Format: add prescription <patient> <medicine> [dosage]\n"
                    + "Example: add prescription Fatou amoxicillin 2x daily";
        }
        try {
            String dosage = parts.length >= 3 ? parts[2] : "";
            prescriptions.add(parts[0], parts[1], dosage, "");
            return "✅ Prescription saved: " + parts[1] + " for " + parts[0]
                    + (dosage.isEmpty() ? "" : " (" + dosage + ")") + ".";
        } catch (IllegalStateException e) {
            return e.getMessage();
        }
    }
}
