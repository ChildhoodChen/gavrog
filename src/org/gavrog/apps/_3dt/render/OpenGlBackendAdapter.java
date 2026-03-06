/*
   Copyright 2012 Olaf Delgado-Friedrichs

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package org.gavrog.apps._3dt.render;

import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import de.jreality.scene.Viewer;

public final class OpenGlBackendAdapter {
    private final Viewer viewer;
    private final Method offscreenMethod;

    private OpenGlBackendAdapter(final Viewer viewer, final Method offscreenMethod) {
        this.viewer = viewer;
        this.offscreenMethod = offscreenMethod;
    }

    public static OpenGlBackendAdapter tryCreate(final String[] backendClassNames,
            final List<String> failures) {
        for (int i = 0; i < backendClassNames.length; ++i) {
            final String className = backendClassNames[i];
            final OpenGlBackendAdapter adapter = tryCreateSingle(className, failures);
            if (adapter != null) {
                return adapter;
            }
        }
        return null;
    }

    private static OpenGlBackendAdapter tryCreateSingle(final String className,
            final List<String> failures) {
        try {
            final Class<?> klass = Class.forName(className);
            final Object instance = klass.getDeclaredConstructor().newInstance();
            if (!(instance instanceof Viewer)) {
                failures.add(className + " is not a de.jreality.scene.Viewer");
                return null;
            }
            Method offscreen = null;
            try {
                offscreen = klass.getMethod("renderOffscreen",
                        new Class[] { Integer.TYPE, Integer.TYPE });
            } catch (NoSuchMethodException ex) {
                offscreen = null;
            }
            return new OpenGlBackendAdapter((Viewer) instance, offscreen);
        } catch (Throwable ex) {
            failures.add(className + " failed: " + ex.getClass().getSimpleName()
                    + (ex.getMessage() == null ? "" : " (" + ex.getMessage() + ")"));
            return null;
        }
    }

    public Viewer getViewer() {
        return viewer;
    }

    public String getBackendClass() {
        return viewer.getClass().getName();
    }

    public String getBackendVersion() {
        String version = detectPackageVersion(viewer.getClass());
        if ("unknown".equals(version)) {
            final String joglVersion = detectJoglVersion();
            if (joglVersion != null) {
                version = joglVersion;
            }
        }
        return version;
    }

    public BufferedImage renderOffscreen(final int width, final int height) {
        if (offscreenMethod == null) {
            return null;
        }
        try {
            return (BufferedImage) offscreenMethod.invoke(viewer,
                    new Object[] { Integer.valueOf(width), Integer.valueOf(height) });
        } catch (IllegalAccessException ex) {
            return null;
        } catch (InvocationTargetException ex) {
            return null;
        }
    }

    private static String detectPackageVersion(final Class<?> klass) {
        final Package pkg = klass.getPackage();
        if (pkg == null) {
            return "unknown";
        }
        final String impl = pkg.getImplementationVersion();
        if (impl != null && impl.length() > 0) {
            return impl;
        }
        final String spec = pkg.getSpecificationVersion();
        if (spec != null && spec.length() > 0) {
            return spec;
        }
        return "unknown";
    }

    private static String detectJoglVersion() {
        final String[] candidates = new String[] { "com.jogamp.opengl.GL",
                "javax.media.opengl.GL" };
        for (int i = 0; i < candidates.length; ++i) {
            try {
                final Class<?> glClass = Class.forName(candidates[i]);
                return candidates[i] + "@" + detectPackageVersion(glClass);
            } catch (Throwable ex) {
                // try next candidate
            }
        }
        return null;
    }
}
