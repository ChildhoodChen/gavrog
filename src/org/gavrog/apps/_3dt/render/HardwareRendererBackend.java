package org.gavrog.apps._3dt.render;

import java.util.List;

public interface HardwareRendererBackend {
    String getName();

    OpenGlBackendAdapter tryCreate(List<String> failures);
}
