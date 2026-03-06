# 3dt 32-bit → 64-bit Migration Plan

## Why this migration is hard


## Current deployment matrix and launcher behavior

Deployment assembly is now split into:

- **core runtime** (always software-capable; no hard-coded legacy native path),
- **optional OpenGL package** (`jogl` jars + `hardware/<platform-arch>` natives).

Supported hardware bundle directories:

- `hardware/linux-x86_64`
- `hardware/windows-x86_64`
- `hardware/windows-arm64`
- `hardware/macos-x86_64`
- `hardware/macos-arm64`

Launcher renderer selection is `software|auto|opengl` via `GAVROG_3DT_RENDERER`
or `--renderer`. `opengl` prints an explicit warning and falls back to
software when no matching bundle is available.

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

## Phase B progress implemented

A first Phase B step is now in place in the runtime code:

- direct compile-time references to `de.jreality.jogl.Viewer` in startup paths were replaced by reflective loading/invocation in `ViewerFrame`,
- OpenGL-only diagnostic code in `Main` now also uses reflection and gracefully no-ops when the JOGL viewer class is unavailable.

This reduces hard coupling to legacy JOGL classes and makes future renderer replacement/upgrades easier while preserving existing behavior when JOGL is present.

## Phase C progress implemented

A first packaging/distribution modernization step is now implemented in launch scripts:

- renderer mode is exposed as `GAVROG_3DT_RENDERER` (`software`, `auto`, `opengl`) in both Unix and Windows launchers,
- software mode excludes JOGL jars/native path from startup classpath/options,
- OpenGL mode now checks for JOGL jar presence and falls back to software mode when unavailable,
- Unix launcher emits an architecture warning when OpenGL is requested on common 64-bit architectures with legacy natives.

This moves 3dt closer to a split-core/optional-acceleration packaging model without changing the historical installer structure yet.

## Phase D progress implemented

A non-GUI smoke runner was added for repeatable phase-D validation:

- `test/org/gavrog/apps/_3dt/PhaseDSmoke.java` loads `.cgd` fixtures through `Document.load(...)`,
- supports explicit fixture arguments and includes a default fixture set,
- reports loaded/missing/failed counts and exits non-zero on parse/load failures.

This provides a lightweight regression check for the data-loading path independent of OpenGL availability.

## Final migration outcome (implemented)

The migration was completed by **removing the legacy OpenGL backend path** from runtime startup and packaging:

- `ViewerFrame` now always initializes `SoftViewer` and no longer attempts JOGL viewer construction,
- launcher scripts no longer place JOGL jars/native paths on the runtime classpath,
- installer packaging no longer includes the optional legacy `jogl` pack.

This eliminates 32-bit vs 64-bit native-library mismatch risks in 3dt runtime.

### Validation status

- data-loading smoke validation is automated via `PhaseDSmoke`,
- rendering correctness validation is now tied to software-renderer behavior only.


## Renderer abstraction update (implemented)

A renderer abstraction was introduced in `ViewerFrame` to decouple startup from a hardwired viewer class:

- startup mode is controlled via `-Dorg.gavrog.3dt.renderer={auto|hardware|software}` (default: `auto`),
- hardware backend candidates are configurable via `-Dorg.gavrog.3dt.hardware.backends=...` and default to `de.jreality.jogl3.Viewer,de.jreality.jogl.Viewer`,
- hardware viewers are instantiated reflectively and share the same tool-system, picking path, camera path, and render-trigger wiring as software mode,
- screenshot export now attempts backend offscreen rendering first and transparently falls back to software offscreen rendering when unavailable,
- startup diagnostics now log requested/selected renderer, backend class/version, and explicit fallback reasons.
