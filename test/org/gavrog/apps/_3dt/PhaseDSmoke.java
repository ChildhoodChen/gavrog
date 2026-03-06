package org.gavrog.apps._3dt;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.gavrog.joss.geometry.Vector;
import org.gavrog.joss.tilings.Tiling;

/**
 * Phase-D smoke runner for loading and exercising representative 3dt fixtures
 * without GUI startup.
 */
public class PhaseDSmoke {
    private static final String REQUESTED_FIXTURE =
            "test/TestResources/RCSR-tilings/SSZ-43-mono_3dt.cgd";

    private static List<String> defaultFixtures() {
        return Arrays.asList(
                // --- representative .cgd load cases
                REQUESTED_FIXTURE,
                "test/TestResources/RCSR-tilings/qtz-3dt.cgd",
                "test/TestResources/RCSR-tilings/afi-3dt.cgd",
                "test/TestResources/RCSR-tilings/srs-3dt.cgd",
                "test/TestResources/RCSR-tilings/dia-3dt.cgd",
                "test/TestResources/RCSR-tilings/ana-3dt.cgd",
                "test/TestResources/RCSR-tilings/ato-3dt.cgd",
                "test/TestResources/RCSR-tilings/asv-3dt.cgd",
                // --- representative .ds load cases
                "test/TestResources/simple_14_good.ds",
                "test/TestResources/simple_15_good.ds",
                "test/TestResources/simple_16_good.ds",
                "test/TestResources/ftmax.ds");
    }

    private static void runCriticalOperations(final Document doc) {
        doc.initializeEmbedder();
        doc.getNet();
        doc.getSignature();
        doc.getGroupName();
        doc.info();

        final List<Tiling.Tile> tiles = doc.getTiles();
        if (tiles.isEmpty()) {
            throw new IllegalStateException("document has no tiles");
        }

        final Tiling.Tile tile = tiles.get(0);
        final List<Vector> shifts = doc.centerIntoUnitCell(tile);
        final Vector shift = shifts.isEmpty() ? Vector.zero(3) : shifts.get(0);

        final DisplayList.Item item = doc.add(tile, shift);
        if (item == null) {
            throw new IllegalStateException("failed to add tile instance");
        }

        if (!doc.recolor(item, Color.RED)) {
            throw new IllegalStateException("failed to recolor tile instance");
        }

        final Color previous = doc.getTileClassColor(0);
        doc.setTileClassColor(0, Color.BLUE);
        doc.setTileClassColor(0, previous);

        final Tiling.Facet facet = tile.facet(0);
        doc.setFacetClassColor(facet, Color.GREEN);
        doc.hideFacetClass(facet);
        if (!doc.isHiddenFacetClass(facet)) {
            throw new IllegalStateException("failed to hide facet class");
        }
        doc.showFacetClass(facet);
        doc.removeFacetClassColor(facet);

        doc.addNeighbor(item, 0);
        doc.addNeighborFacet(item, 0);
        doc.remove(item);
    }

    public static void main(final String[] args) throws Exception {
        final List<String> fixtures = args.length > 0
                ? Arrays.asList(args)
                : defaultFixtures();

        int loaded = 0;
        int missing = 0;
        int docsChecked = 0;
        final List<String> failures = new ArrayList<String>();
        final List<String> missingFixtures = new ArrayList<String>();

        for (final String path: fixtures) {
            final File file = new File(path);
            if (!file.exists()) {
                System.err.println("[phase-d] missing fixture: " + path);
                ++missing;
                missingFixtures.add(path);
                continue;
            }
            try {
                final List<Document> docs = Document.load(path);
                if (docs == null || docs.size() == 0) {
                    failures.add(path + " (loaded 0 documents)");
                } else {
                    for (int i = 0; i < docs.size(); ++i) {
                        final Document doc = docs.get(i);
                        try {
                            runCriticalOperations(doc);
                            ++docsChecked;
                            System.err.println("[phase-d] checked " + path + "#" + i
                                    + " name=\"" + doc.getName() + "\"");
                        } catch (Throwable ex) {
                            failures.add(path + "#" + i + " ("
                                    + ex.getClass().getName() + ": "
                                    + String.valueOf(ex.getMessage()) + ")");
                        }
                    }
                    ++loaded;
                }
            } catch (Throwable ex) {
                failures.add(path + " (" + ex.getClass().getName() + ": "
                        + String.valueOf(ex.getMessage()) + ")");
            }
        }

        System.err.println("[phase-d] summary: loaded=" + loaded + ", docsChecked="
                + docsChecked + ", missing=" + missing + ", failed="
                + failures.size());

        Collections.sort(missingFixtures);
        Collections.sort(failures);

        for (final String path: missingFixtures) {
            System.err.println("[phase-d] missing: " + path);
        }

        if (!failures.isEmpty()) {
            for (final String x: failures) {
                System.err.println("[phase-d] failure: " + x);
            }
            System.exit(1);
        }
    }
}
