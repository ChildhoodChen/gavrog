package org.gavrog.apps._3dt.render;

import java.util.List;

public class ReflectiveOpenGlBackend implements HardwareRendererBackend {
    private final String[] backendClassNames;

    public ReflectiveOpenGlBackend(final String[] backendClassNames) {
        this.backendClassNames = backendClassNames;
    }

    public String getName() {
        return "opengl";
    }

    public OpenGlBackendAdapter tryCreate(final List<String> failures) {
        return OpenGlBackendAdapter.tryCreate(backendClassNames, failures);
    }
}
