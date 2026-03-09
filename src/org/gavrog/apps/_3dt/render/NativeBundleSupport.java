package org.gavrog.apps._3dt.render;

import java.io.File;

public final class NativeBundleSupport {
    public static final class Status {
        private final String platformKey;
        private final boolean is64BitJvm;
        private final boolean hasKnownPlatform;
        private final boolean hasNativeDirectory;
        private final String missingNativeFile;

        private Status(final String platformKey, final boolean is64BitJvm,
                final boolean hasKnownPlatform, final boolean hasNativeDirectory,
                final String missingNativeFile) {
            this.platformKey = platformKey;
            this.is64BitJvm = is64BitJvm;
            this.hasKnownPlatform = hasKnownPlatform;
            this.hasNativeDirectory = hasNativeDirectory;
            this.missingNativeFile = missingNativeFile;
        }

        public String getPlatformKey() {
            return platformKey;
        }

        public boolean is64BitJvm() {
            return is64BitJvm;
        }

        public boolean hasKnownPlatform() {
            return hasKnownPlatform;
        }

        public boolean hasNativeDirectory() {
            return hasNativeDirectory;
        }

        public String getMissingNativeFile() {
            return missingNativeFile;
        }

        public boolean hasAllRequiredNativeFiles() {
            return missingNativeFile == null;
        }

        public boolean canAttemptHardwareRenderer() {
            return is64BitJvm && hasKnownPlatform && hasNativeDirectory
                    && hasAllRequiredNativeFiles();
        }

        public String describeBlocker() {
            if (!is64BitJvm) {
                return "JVM is not 64-bit";
            }
            if (!hasKnownPlatform) {
                return "unsupported platform or architecture ('" + platformKey + "')";
            }
            if (!hasNativeDirectory) {
                return "no native bundle found at 3dt.home/hardware/" + platformKey;
            }
            if (!hasAllRequiredNativeFiles()) {
                return "required native file missing: " + missingNativeFile
                        + " in 3dt.home/hardware/" + platformKey;
            }
            return "none";
        }
    }

    private NativeBundleSupport() {
    }

    public static Status evaluate() {
        final String platformKey = detectPlatformKey();
        final boolean is64 = is64BitJvm();
        final boolean knownPlatform = !platformKey.startsWith("unknown-");
        final File nativeDir = getNativeDirectory(platformKey);
        final boolean hasNativeDir = nativeDir != null && nativeDir.isDirectory();
        final String missingNativeFile = hasNativeDir
                ? findMissingNativeFile(platformKey, nativeDir)
                : null;
        return new Status(platformKey, is64, knownPlatform, hasNativeDir,
                missingNativeFile);
    }

    public static String detectPlatformKey() {
        final String os = normalizeOs(System.getProperty("os.name"));
        final String arch = normalizeArch(System.getProperty("os.arch"));
        return os + "-" + arch;
    }

    private static String normalizeOs(final String osName) {
        final String os = osName == null ? "" : osName.toLowerCase();
        if (os.indexOf("win") >= 0) {
            return "windows";
        }
        if (os.indexOf("mac") >= 0 || os.indexOf("darwin") >= 0) {
            return "macos";
        }
        if (os.indexOf("linux") >= 0) {
            return "linux";
        }
        return "unknown";
    }

    private static String normalizeArch(final String archName) {
        final String arch = archName == null ? "" : archName.toLowerCase();
        if ("x86_64".equals(arch) || "amd64".equals(arch)) {
            return "x86_64";
        }
        if ("aarch64".equals(arch) || "arm64".equals(arch)) {
            return "arm64";
        }
        return arch.length() == 0 ? "unknown" : arch;
    }

    private static boolean is64BitJvm() {
        final String dataModel = System.getProperty("sun.arch.data.model");
        if ("64".equals(dataModel)) {
            return true;
        }
        final String arch = System.getProperty("os.arch", "").toLowerCase();
        return arch.indexOf("64") >= 0 || "aarch64".equals(arch);
    }

    private static File getNativeDirectory(final String platformKey) {
        final String base = System.getProperty("3dt.home");
        if (base == null || base.length() == 0) {
            return null;
        }
        return new File(new File(base, "hardware"), platformKey);
    }

    private static String findMissingNativeFile(final String platformKey,
            final File nativeDir) {
        final String[] required = requiredNativeFiles(platformKey);
        for (int i = 0; i < required.length; ++i) {
            if (!new File(nativeDir, required[i]).isFile()) {
                return required[i];
            }
        }
        return null;
    }

    private static String[] requiredNativeFiles(final String platformKey) {
        if (platformKey.startsWith("linux-")) {
            return new String[] { "libgluegen-rt.so", "libjogl.so",
                    "libjogl_awt.so", "libjogl_cg.so" };
        }
        if (platformKey.startsWith("windows-")) {
            return new String[] { "gluegen-rt.dll", "jogl.dll",
                    "jogl_awt.dll", "jogl_cg.dll" };
        }
        if (platformKey.startsWith("macos-")) {
            return new String[] { "libgluegen-rt.jnilib", "libjogl.jnilib",
                    "libjogl_awt.jnilib", "libjogl_cg.jnilib" };
        }
        return new String[0];
    }
}
