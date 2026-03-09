package org.gavrog.apps._3dt;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.gavrog.apps._3dt.render.OpenGlBackendAdapter;

/**
 * Non-GUI OpenGL backend probe for CI/release gating.
 */
public final class RendererProbe {
    private static final String OPENGL_BACKENDS_PROPERTY =
            "org.gavrog.3dt.opengl.backends";
    private static final String[] DEFAULT_OPENGL_BACKENDS =
            new String[] { "de.jreality.jogl3.Viewer" };

    private RendererProbe() {
    }

    private static String[] configuredOpenGlBackends() {
        final String configured = System.getProperty(OPENGL_BACKENDS_PROPERTY);
        if (configured == null || configured.trim().length() == 0) {
            return DEFAULT_OPENGL_BACKENDS;
        }
        final String[] raw = configured.split(",");
        final List<String> names = new ArrayList<String>();
        for (int i = 0; i < raw.length; ++i) {
            final String trimmed = raw[i].trim();
            if (trimmed.length() > 0) {
                names.add(trimmed);
            }
        }
        if (names.isEmpty()) {
            return DEFAULT_OPENGL_BACKENDS;
        }
        return names.toArray(new String[names.size()]);
    }

    private static String joinFailures(final List<String> failures) {
        if (failures.isEmpty()) {
            return "none";
        }
        return Arrays.toString(failures.toArray(new String[failures.size()]));
    }

    static int runProbe(final PrintStream out) {
        final String[] backends = configuredOpenGlBackends();
        out.println("3dt renderer probe");
        out.println("  java.library.path=" + System.getProperty("java.library.path"));
        out.println("  java.awt.headless=" + java.awt.GraphicsEnvironment.isHeadless());
        out.println("  opengl_backend_candidates=" + Arrays.toString(backends));
        out.println("  opengl_backend_selected_class=" + backends[0]);

        final List<String> failures = new ArrayList<String>();
        final OpenGlBackendAdapter adapter =
                OpenGlBackendAdapter.tryCreate(backends, failures);
        if (adapter == null) {
            out.println("  opengl_backend_available=false");
            out.println("  opengl_backend_failures=" + joinFailures(failures));
            return 2;
        }

        out.println("  opengl_backend_available=true");
        out.println("  opengl_backend_class=" + adapter.getBackendClass());
        out.println("  opengl_backend_version=" + adapter.getBackendVersion());
        out.println("  opengl_backend_failures=" + joinFailures(failures));
        return 0;
    }

    public static void main(final String[] args) {
        final int status = runProbe(System.out);
        if (status != 0) {
            System.exit(status);
        }
    }
}
