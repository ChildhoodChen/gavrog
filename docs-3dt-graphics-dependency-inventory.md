# 3dt Graphics Dependency Inventory (`Deploy/` → `src/org/gavrog/apps/_3dt`)

## 1) jReality core graphics/runtime jars

| Deploy artifact | Category | Usage site(s) in `src/org/gavrog/apps/_3dt` |
|---|---|---|
| `Deploy/jReality/jReality.jar` | Scene graph, viewer API, geometry/maths, toolsystem | `Main.java` imports jReality scene/geometry/math/shader/util classes; `ViewerFrame.java` drives `Viewer`, scene graph, camera tools, render trigger, screenshots. |
| `Deploy/jReality/jtem-beans.jar` | Swing utility beans used by 3dt dialogs | `Main.java` imports and uses `de.jtem.beans.DimensionPanel` for screenshot and Sunflow resolution dialogs. |
| `Deploy/jReality/bsh.jar` | jReality transitive runtime support (BeanShell) | Not directly imported in `_3dt` sources; packaged as runtime support for jReality. |
| `Deploy/jReality/jterm.jar` | jReality transitive runtime support | Not directly imported in `_3dt` sources; packaged as runtime support for jReality. |

## 2) JOGL-related hardware acceleration artifacts

| Deploy artifact | Category | Usage site(s) in `src/org/gavrog/apps/_3dt` |
|---|---|---|
| `Deploy/jogl/jogl.jar` | OpenGL Java bindings (hardware backend dependency) | No direct compile-time imports in `_3dt`; loaded reflectively through `OpenGlBackendAdapter` when backend classes are instantiated. |
| `Deploy/jogl/gluegen-rt.jar` | JOGL runtime glue layer | No direct compile-time imports in `_3dt`; required by hardware backend at runtime only. |
| `Deploy/hardware/linux-x86_64/*` | Linux x64 native JOGL libs | Discovered by startup scripts (`Deploy/bin/3dt`) and `NativeBundleSupport` fail-safe checks. |
| `Deploy/hardware/windows-x86_64/*` | Windows x64 native JOGL libs | Discovered by startup scripts (`Deploy/bin/3dt.bat`) and `NativeBundleSupport` fail-safe checks. |
| `Deploy/hardware/macos-x86_64/*` | macOS Intel x64 native JOGL libs | Discovered by startup scripts and `NativeBundleSupport` fail-safe checks. |
| `Deploy/hardware/macos-arm64/*` | macOS Apple Silicon arm64 native JOGL libs | Discovered by startup scripts and `NativeBundleSupport` fail-safe checks. |
| `Deploy/hardware/windows-arm64/*` | Windows arm64 native JOGL libs (optional extension) | Discovered by startup scripts and `NativeBundleSupport` fail-safe checks. |

## 3) Renderer-support libraries

| Deploy artifact | Category | Usage site(s) in `src/org/gavrog/apps/_3dt` |
|---|---|---|
| `Deploy/sunflow/sunflow.jar` | Optional ray-trace export backend | `Main.java` imports and uses `de.jreality.sunflow.Sunflow` + `RenderOptions` for Sunflow export dialogs/actions. |
| `Deploy/sunflow/janino.jar` | Sunflow expression compiler/runtime dependency | No direct compile-time imports in `_3dt`; runtime support for `sunflow.jar`. |

## 4) New renderer integration layer summary

- `org.gavrog.apps._3dt.render.HardwareRendererBackend` defines the adapter-facing hardware backend contract.
- `org.gavrog.apps._3dt.render.ReflectiveOpenGlBackend` is the concrete implementation that tries configured OpenGL backend class names.
- `org.gavrog.apps._3dt.render.OpenGlBackendAdapter` performs reflective viewer construction/offscreen rendering/version detection without compile-time JOGL coupling.
- `org.gavrog.apps._3dt.render.NativeBundleSupport` evaluates platform key, JVM bitness, and native bundle presence for fail-safe hardware-renderer gating.
