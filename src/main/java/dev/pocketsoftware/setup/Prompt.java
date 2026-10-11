package dev.pocketsoftware.setup;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/** Console prompts: systems selection, Appwrite settings, database id. */
final class Prompt {

    /** Asks which systems run here; accepts 1/2/3, all, or a comma list like 1,3. */
    static List<String> askSystems(Scanner in) {
        while (true) {
            System.out.println();
            System.out.println("Which systems does this installation run?");
            System.out.println("  1) School");
            System.out.println("  2) Hospital 🏥");
            System.out.println("  3) Business");
            System.out.println("  all) School + Hospital + Business");
            System.out.print("Choose (1, 2, 3, all, or comma list e.g. 1,3): ");
            String answer = in.nextLine().trim().toLowerCase();
            List<String> systems = parseSystems(answer);
            if (systems != null) {
                return systems;
            }
            System.out.println("Please answer 1, 2, 3, all, or a comma list like 1,3.");
        }
    }

    /** Maps input to system names; null when unrecognized. */
    static List<String> parseSystems(String answer) {
        String a = answer.trim().toLowerCase();
        if (a.equals("all")) {
            return List.copyOf(Schema.ALL);
        }
        boolean single = Schema.ALL.contains(a);
        List<String> out = new ArrayList<>();
        if (single) {
            out.add(a);
        } else {
            for (String part : a.split(",")) {
                String s = part.trim();
                if (s.isEmpty()) {
                    continue;
                }
                if (s.equals("1") || s.equals("2") || s.equals("3")) {
                    out.add(Schema.ALL.get(Integer.parseInt(s) - 1));
                } else {
                    return null;
                }
            }
        }
        return out.isEmpty() ? null : out;
    }

    /** Asks for a value with a default; accepts blank for default. */
    static String askValue(Scanner in, String label, String dflt) {
        String suffix = (dflt == null || dflt.isBlank()) ? "" : " [" + dflt + "]";
        System.out.print(label + suffix + ": ");
        String answer = in.nextLine().trim();
        return answer.isEmpty() ? (dflt == null ? "" : dflt) : answer;
    }

    /** Asks for the API key; never echoes the stored key, blank keeps it. */
    static String askKey(Scanner in, String stored) {
        boolean configured = !stored.isBlank() && !stored.toUpperCase().startsWith("REPLACE");
        String suffix = configured ? " [configured - Enter to keep]" : "";
        System.out.print("Appwrite API key" + suffix + ": ");
        String answer = in.nextLine().trim();
        return answer.isEmpty() ? (configured ? stored : "") : answer;
    }
}
