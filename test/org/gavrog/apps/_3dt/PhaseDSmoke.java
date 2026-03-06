package org.gavrog.apps._3dt;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Simple phase-D smoke runner for loading 3dt fixtures without GUI startup.
 */
public class PhaseDSmoke {
    private static final String REQUESTED_FIXTURE =
            "test/TestResources/RCSR-tilings/SSZ-43-mono_3dt.cgd";

    private static List<String> defaultFixtures() {
        return Arrays.asList(
                REQUESTED_FIXTURE,
                "test/TestResources/RCSR-tilings/qtz-3dt.cgd",
                "test/TestResources/RCSR-tilings/afi-3dt.cgd",
                "test/TestResources/RCSR-tilings/srs-3dt.cgd");
    }

    public static void main(final String[] args) throws Exception {
        final List<String> fixtures = args.length > 0
                ? Arrays.asList(args)
                : defaultFixtures();

        int loaded = 0;
        int missing = 0;
        final List<String> failures = new ArrayList<String>();

        for (final String path: fixtures) {
            final File file = new File(path);
            if (!file.exists()) {
                System.err.println("[phase-d] missing fixture: " + path);
                ++missing;
                continue;
            }
            try {
                final List<Document> docs = Document.load(path);
                if (docs == null || docs.size() == 0) {
                    failures.add(path + " (loaded 0 documents)");
                } else {
                    System.err.println("[phase-d] loaded " + path + " -> "
                            + docs.size() + " document(s)");
                    ++loaded;
                }
            } catch (Throwable ex) {
                failures.add(path + " (" + ex.getClass().getName() + ": "
                        + ex.getMessage() + ")");
            }
        }

        System.err.println("[phase-d] summary: loaded=" + loaded + ", missing="
                + missing + ", failed=" + failures.size());

        if (!failures.isEmpty()) {
            for (final String x: failures) {
                System.err.println("[phase-d] failure: " + x);
            }
            System.exit(1);
        }
    }
}
