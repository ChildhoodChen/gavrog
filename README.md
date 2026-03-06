# gavrog
Generation, analysis and visualization of reticular ornaments using Gavrog.

This is the Java code for the programs Systre and 3dt, and associated libraries.

The Java version of Systre will continue to be maintained indefinitely, but it is unlikely that significant new features will be added.

Unfortunately, 3dt is a casualty of 64-bit Java, among other things. It ended up being too hard to maintain, and I decided instead to focus on my new project webGavrog. It is not yet at the point where it can completely replace 3dt, but I think it's approaching usefulness. Unlike 3dt, it can display nets directly, not just in association with tilings.

See https://github.com/odf/webGavrog for the code and http://gavrog.org/webGavrog/ for an online version. A file with all applicable RCSR nets can be downloaded from http://rcsr.net/systre as "Systre input data (.cgd)" and then opened in webGavrog.

An online version of Systre as part of webGavrog is not yet available, but there is a command line script implementing most of its functionality.


## 3dt and 64-bit status

The hardest migration issue for 3dt is the historical OpenGL stack (jReality + JOGL 1.1.1 native libraries), which was packaged for older platforms and does not map cleanly to modern 64-bit JVM setups.

This repository now includes explicit renderer selection in 3dt (`software`, `auto`, `opengl`) via `org.gavrog.3dt.renderer`. Runtime uses one renderer-selection decision point: `software` forces jReality's `SoftViewer`, while `auto` and `opengl` try the OpenGL adapter and fall back to software when initialization fails.

A detailed migration plan is documented in `docs-3dt-64bit-migration-plan.md`.


## 3dt runtime packaging and renderer selection

The deployment layout under `Deploy/` is split into:

- **Core runtime**: pure Java launch/runtime artifacts (always includes software rendering).
- **Optional hardware acceleration package**: JOGL jars plus platform-specific native libraries under `hardware/<platform-arch>/`.

Launchers (`Deploy/bin/3dt`, `Deploy/bin/3dt.bat`) support explicit renderer selection:

- `software`: force software renderer.
- `auto`: try OpenGL only when matching hardware bundle is present; otherwise use software.
- `opengl`: request OpenGL; if unavailable, print a warning and fall back to software.

Renderer mode can be set either with `GAVROG_3DT_RENDERER` or CLI flags `--renderer=<mode>` / `--renderer <mode>`.

Optional override: set `GAVROG_3DT_ACCEL_ROOT` to point to an alternate
hardware bundle root (default: `<install>/hardware`).

Runtime OpenGL backend candidates are configured with `org.gavrog.3dt.opengl.backends` (default: `de.jreality.jogl3.Viewer`) and instantiated through `org.gavrog.apps._3dt.render.OpenGlBackendAdapter`.

### Supported hardware-bundle platform matrix

Hardware bundles are expected in these deployment directories:

- `hardware/linux-x86_64`
- `hardware/windows-x86_64`
- `hardware/windows-arm64`
- `hardware/macos-x86_64`
- `hardware/macos-arm64`

Only matching 64-bit bundles are considered by launchers; legacy `Deploy/jogl-{win,unix,mac}` native folders are not used by runtime assembly anymore.
