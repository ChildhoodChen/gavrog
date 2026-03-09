package org.gavrog.apps._3dt;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

public class RendererSelfCheck {
	private static final String BACKENDS_PROPERTY =
			"org.gavrog.3dt.opengl.backends";
	private static final String DEFAULT_BACKENDS =
			"de.jreality.jogl3.Viewer";
	private static final String[] NATIVE_LIBRARIES =
			new String[] { "gluegen-rt", "jogl", "jogl_awt", "jogl_cg" };

	private static void printClassStatus(final String role, final String className) {
		try {
			final Class<?> cls = Class.forName(className);
			String version = "unknown";
			if (cls.getPackage() != null) {
				if (cls.getPackage().getImplementationVersion() != null) {
					version = cls.getPackage().getImplementationVersion();
				} else if (cls.getPackage().getSpecificationVersion() != null) {
					version = cls.getPackage().getSpecificationVersion();
				}
			}
			System.out.println("[OK] class " + role + " -> " + className
					+ " (version=" + version + ")");
		} catch (Throwable ex) {
			System.out.println("[FAIL] class " + role + " -> " + className
					+ " (" + ex.getClass().getName() + ": "
					+ safeMessage(ex) + ")");
		}
	}

	private static void printNativeLoadStatus(final String lib) {
		try {
			System.loadLibrary(lib);
			final File resolved = resolveLibrary(lib);
			if (resolved != null) {
				System.out.println("[OK] native " + lib + " -> "
						+ resolved.getPath() + " sha256=" + sha256(resolved));
			} else {
				System.out.println("[OK] native " + lib + " loaded (path unresolved)");
			}
		} catch (Throwable ex) {
			System.out.println("[FAIL] native " + lib + " ("
					+ ex.getClass().getName() + ": " + safeMessage(ex) + ")");
		}
	}

	private static File resolveLibrary(final String lib) {
		final String mapped = System.mapLibraryName(lib);
		final String[] parts = System.getProperty("java.library.path", "").split(
				File.pathSeparator);
		for (int i = 0; i < parts.length; ++i) {
			final File candidate = new File(parts[i], mapped);
			if (candidate.exists()) {
				return candidate;
			}
		}
		return null;
	}

	private static String sha256(final File file) {
		FileInputStream in = null;
		try {
			final MessageDigest digest = MessageDigest.getInstance("SHA-256");
			in = new FileInputStream(file);
			final byte[] buffer = new byte[8192];
			while (true) {
				final int count = in.read(buffer);
				if (count <= 0) {
					break;
				}
				digest.update(buffer, 0, count);
			}
			final byte[] hash = digest.digest();
			final StringBuffer sb = new StringBuffer();
			for (int i = 0; i < hash.length; ++i) {
				sb.append(String.format("%02x", Integer.valueOf(hash[i] & 0xff)));
			}
			return sb.toString();
		} catch (Exception ex) {
			return "unavailable:" + ex.getClass().getName();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception ex) {
					// ignore
				}
			}
		}
	}

	private static String safeMessage(final Throwable ex) {
		return ex.getMessage() == null ? "<no message>" : ex.getMessage();
	}

	public static void main(final String[] args) {
		System.out.println("3dt renderer self-check");
		System.out.println("java.version=" + System.getProperty("java.version"));
		System.out.println("java.vendor=" + System.getProperty("java.vendor"));
		System.out.println("os.name=" + System.getProperty("os.name"));
		System.out.println("os.arch=" + System.getProperty("os.arch"));
		System.out.println("java.library.path="
				+ System.getProperty("java.library.path"));

		printClassStatus("software", "de.jreality.softviewer.SoftViewer");

		final String backendConfig =
				System.getProperty(BACKENDS_PROPERTY, DEFAULT_BACKENDS);
		System.out.println("configured.opengl.backends=" + backendConfig);
		final String[] backendClasses = backendConfig.split(",");
		for (int i = 0; i < backendClasses.length; ++i) {
			final String className = backendClasses[i].trim();
			if (className.length() > 0) {
				printClassStatus("hardware-candidate", className);
			}
		}

		for (int i = 0; i < NATIVE_LIBRARIES.length; ++i) {
			printNativeLoadStatus(NATIVE_LIBRARIES[i]);
		}
	}
}
