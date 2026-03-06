# 3dt 32-bit → 64-bit Migration Plan

## Why this migration is hard

The core issue is not Java integer width in the 3dt source code; it is **packaged native rendering dependencies**:

1. 3dt uses jReality's JOGL-based OpenGL viewer (`de.jreality.jogl.Viewer`) for fast rendering.
2. The repository bundles very old JOGL 1.1.1 native libraries in `Deploy/jogl-*`.
3. Those native libraries are architecture-specific and were historically shipped as 32-bit binaries.
4. Startup scripts set `-Djava.library.path` to the bundled native directory and include JOGL jars unconditionally.
5. On modern 64-bit JVMs, loading incompatible native binaries can fail with linker/ABI errors (or worse), causing the OpenGL path to fail.

This means the hardest part is **dependency/runtime migration**, not basic Java type changes.

## What has been changed in this patch

### 1) Make OpenGL initialization failure-safe on 64-bit

`ViewerFrame` now:
- supports an explicit system property `org.gavrog.3dt.opengl` (`off|false|0` to disable OpenGL),
- catches `Throwable` (not only `Exception`) around OpenGL viewer setup/render bootstrap,
- reliably falls back to `SoftViewer`.

This improves behavior for architecture/native-loader failures that often surface as `Error` subclasses.

### 2) Disable legacy JOGL path by default in launcher scripts

Launchers now set:
- `-Dorg.gavrog.3dt.opengl=off`

This keeps 3dt usable on 64-bit Java out of the box via software rendering, while still allowing future re-enablement by editing scripts or passing overriding JVM properties.

## Full migration roadmap

### Phase A — Stabilize runtime on modern JDKs (short-term)

1. Keep software renderer as safe default.
2. Ensure all OpenGL/native failures degrade gracefully (no crash on startup).
3. Add clear startup logging: which renderer was selected and why.

### Phase B — Replace obsolete graphics stack (medium-term)

1. Upgrade jReality integration or replace renderer backend.
2. If keeping OpenGL path:
   - migrate to modern JOGL (or alternative maintained binding),
   - ship signed and tested 64-bit natives for Linux/macOS/Windows,
   - remove old `jnilib` usage and align with current platform packaging.
3. Re-test picking, camera tools, and screenshot/export workflows.

### Phase C — Packaging and distribution modernization

1. Replace old installer assumptions (IzPack-era packaging) with modern distribution format.
2. Split optional renderer modules:
   - core (pure Java software mode),
   - hardware acceleration plugin.
3. Add architecture-aware startup checks.

### Phase D — Regression and acceptance testing

1. Smoke tests:
   - open `.ds` and `.cgd`,
   - tile operations, color changes, camera controls,
   - screenshot export and Sunflow export.
2. Cross-platform matrix:
   - Linux x64, macOS arm64/x64, Windows x64.
3. Performance baseline comparison between software and accelerated modes.

## Remaining work to complete a true 64-bit OpenGL migration

- Update/remove legacy jReality/JOGL dependencies.
- Rebuild native distribution bundles for 64-bit targets.
- Validate rendering correctness and interaction tools after dependency upgrade.

In other words, this patch tackles the immediate runtime blocker and provides a practical migration path, but full acceleration-stack modernization is still a larger dependency upgrade project.
