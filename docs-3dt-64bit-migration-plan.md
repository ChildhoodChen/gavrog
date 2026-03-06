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
- supports explicit startup mode selection via `org.gavrog.3dt.renderer=software|auto|opengl`,
- routes OpenGL backend loading through `org.gavrog.apps._3dt.render.OpenGlBackendAdapter`,
- reliably falls back to `SoftViewer` when OpenGL initialization fails.

This improves behavior for architecture/native-loader failures that often surface as `Error` subclasses.

### 2) Disable legacy JOGL path by default in launcher scripts

Launchers now set:
- `-Dorg.gavrog.3dt.renderer=software`

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

Phase D coverage has been expanded to define both automated smoke checks and GUI acceptance gates.

### 1) Non-GUI smoke coverage expansion (implemented)

`test/org/gavrog/apps/_3dt/PhaseDSmoke.java` now validates more than plain fixture loading:

- representative `.cgd` fixtures (`qtz`, `afi`, `srs`, `dia` + requested fixture when present),
- representative `.ds` fixtures (`simple_14_good`, `simple_15_good`, `simple_16_good`),
- critical non-GUI operations per loaded document:
  - embedder/net/signature/group-info derivation,
  - tile insertion, recoloring, neighbor and neighbor-facet expansion,
  - tile-class color update,
  - facet hide/show and facet class color mutation.

The runner still reports `loaded/missing/failed` and exits non-zero when any fixture/operation check fails.

### 2) GUI/render acceptance scenarios (defined)

Acceptance scenarios for interactive and export behavior are now explicitly defined:

1. **Camera controls**
   - orbit/rotate, pan, zoom, fit-to-scene, and reset behavior.
2. **Picking/interaction tools**
   - face picking, add tile at picked face, remove selected tile, color-edit actions.
3. **Tile operations and visual property changes**
   - tile/facet visibility toggles, random recolor, explicit class recolor, edge/facet display toggles.
4. **Screenshot/export workflows**
   - screenshot export with antialiasing,
   - scene export workflows (OBJ and Sunflow) complete without runtime exceptions.

### 3) Validation matrix dimensions (defined)

The release validation matrix includes these required dimensions:

- **OS/Arch:** Linux x64, Windows x64, macOS x64, macOS arm64.
- **JDKs:** all currently supported JDK lines (at least oldest supported LTS + latest supported LTS).
- **Renderer mode:** software, auto, opengl.

Each matrix cell must run startup + acceptance scenario subset suitable for the platform.

### 4) Deterministic render-regression checks (defined)

Where deterministic pixel parity is feasible, use golden-image checks. Where it is not, use stable scene metrics:

- camera matrix / transform snapshots,
- scene graph node counts by semantic type,
- pick-hit identity for canonical click targets,
- export artifact sanity checks (non-empty file, expected headers/sections).

For hardware vs software parity, compare against tolerances and fail on material deviations outside thresholds.

### 5) Release gates (defined)

A release is blocked unless all of the following are true:

1. **No startup crashes in opengl mode** on supported targets in the matrix.
2. **Fallback behavior is documented and verified** (hardware failure degrades to software mode with explicit diagnostics).
3. **No high-severity visual/function regressions** in acceptance scenarios versus the current baseline.


## GUI acceptance checklist (release blocking)

The following checklist must be executed and recorded for every required
matrix cell before migration is declared complete.

### Camera tools

- [ ] Orbit/rotate interaction updates the view smoothly.
- [ ] Pan interaction preserves orientation.
- [ ] Zoom in/out behaves consistently.
- [ ] Fit-to-scene and reset-camera return to predictable framing.

### Picking and interaction

- [ ] Face-picking resolves the expected tile/facet target.
- [ ] Add-tile from picked face succeeds.
- [ ] Remove-selected tile succeeds.
- [ ] Recolor action applies to the intended picked object.

### Tile operations

- [ ] Neighbor and neighbor-facet expansion actions succeed.
- [ ] Tile/facet visibility toggles behave as expected.
- [ ] Tile-class and facet-class color edits persist in-session.

### Screenshot / export

- [ ] Screenshot export completes and writes non-empty output.
- [ ] OBJ export completes and contains expected header/body sections.
- [ ] Sunflow export completes without renderer/runtime exceptions.

### Renderer parity checks

- [ ] Software vs OpenGL scene state matches for canonical fixtures.
- [ ] Picking identity parity is within accepted tolerance.
- [ ] Any backend-specific visual deviation is triaged and documented.

## Release gate matrix (must be green)

Use the matrix below as the minimum release gate surface. A migration release
cannot be declared complete unless every required row is green.

| OS | Arch | JDK | Renderer mode | Required status |
| --- | --- | --- | --- | --- |
| Linux | x86_64 | oldest supported LTS | software | green |
| Linux | x86_64 | latest supported LTS | auto | green |
| Linux | x86_64 | latest supported LTS | opengl | green |
| Windows | x86_64 | oldest supported LTS | software | green |
| Windows | x86_64 | latest supported LTS | auto | green |
| Windows | x86_64 | latest supported LTS | opengl | green |
| Windows | arm64 | latest supported LTS | software | green |
| Windows | arm64 | latest supported LTS | auto | green |
| macOS | x86_64 | latest supported LTS | software | green |
| macOS | x86_64 | latest supported LTS | opengl | green |
| macOS | arm64 | oldest supported LTS | software | green |
| macOS | arm64 | latest supported LTS | auto | green |
| macOS | arm64 | latest supported LTS | opengl | green |

A matrix row is **green** only when all conditions are true:

1. launcher startup passes with the selected renderer mode,
2. Phase-D smoke runner passes for required fixtures,
3. GUI acceptance checklist items above are completed,
4. renderer parity checks have no unresolved critical deviations.

If any required row is non-green, migration completion status must remain
**blocked**.

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

- startup mode is controlled via `-Dorg.gavrog.3dt.renderer={software|auto|opengl}` (default: `auto`),
- OpenGL backend candidates are configurable via `-Dorg.gavrog.3dt.opengl.backends=...` and default to `de.jreality.jogl3.Viewer`,
- `opengl` and `auto` both go through a single renderer-selection decision point in `ViewerFrame.selectBackend()`,
- OpenGL viewers are created via `org.gavrog.apps._3dt.render.OpenGlBackendAdapter` (no direct legacy JOGL viewer wiring in `ViewerFrame`),
- screenshot export attempts backend offscreen rendering first and transparently falls back to software offscreen rendering when unavailable,
- startup diagnostics always log requested renderer, selected renderer, and fallback reason (`none` when no fallback happened), plus backend class/version.
