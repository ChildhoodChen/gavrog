# 3dt renderer dependency inventory and reproducible acquisition

This document inventories graphics-related runtime artifacts currently under `Deploy/` and defines a reproducible acquisition process for them.

## Inventory: graphics jars, natives, source/version/license

| Artifact | Category | Source / upstream | Version status | License status | SHA-256 |
|---|---|---|---|---|---|
| `Deploy/jReality/jReality.jar` | Renderer core | jReality project (`https://www3.math.tu-berlin.de/jreality/`) | **Unknown legacy build** (no manifest version) | jReality license text in `Deploy/jReality/LICENSE` | `94397baf1302218b96c0841305555ef60bbee5b44633b0d80d0efa5c5442b32c` |
| `Deploy/jReality/bsh.jar` | Supporting lib (BeanShell) | BeanShell (`http://www.beanshell.org`) | `2.0b4` (manifest) | LGPL (per `Deploy/jReality/README`) | `91395c07885839a8c6986d5b7c577cd9bacf01bf129c89141f35e8ea858427b6` |
| `Deploy/jReality/jtem-beans.jar` | Supporting lib (jTEM) | jTEM (`http://www.jtem.de`) | **Unknown legacy build** | jReality bundle license context (`Deploy/jReality/LICENSE`) | `be48bf108058cad0d8ab0f3020d63c18284e3da8ec4ef84582af6cc2d8083474` |
| `Deploy/jReality/jterm.jar` | Supporting lib (jTerm) | jTEM jTerm (`http://www.jtem.de`) | **Unknown legacy build** | **License unclear** (explicitly noted in `Deploy/jReality/README`) | `458b4931461ed48fa2bdca48dd66c172490df52cf01ce544db26c18b63547f2f` |
| `Deploy/jogl/jogl.jar` | Hardware renderer binding | JOGL project | `1.1.1` (manifest/title + filename) | BSD-style (`Deploy/jogl/LICENSE-JOGL-1.1.1.txt`) | `653a0ac4116c42439a11dd7c77361783a19b40c81d0fc8cd1a6197d304360cdf` |
| `Deploy/jogl/gluegen-rt.jar` | JOGL support | GlueGen (JOGL ecosystem) | **Likely JOGL 1.1.1 era** (no manifest version) | JOGL bundle license (`Deploy/jogl/LICENSE-JOGL-1.1.1.txt`) | `c9f7fe49cbc50c51de4a309388c1e1523e8fcb717f72ac011b356a61ae5044f7` |
| `Deploy/jogl-win/gluegen-rt.dll` | Native (Windows) | JOGL 1.1.1-era bundle | Legacy pinned by checksum | JOGL license | `9b606794552c48ab661ee4a879b97cf85d85b577ddecb184e3463a2d29359a4d` |
| `Deploy/jogl-win/jogl.dll` | Native (Windows) | JOGL 1.1.1-era bundle | Legacy pinned by checksum | JOGL license | `1c58df63f00f4e4ef2876773b8c78832bf815d86337cd38e437a4ecd4ba3b538` |
| `Deploy/jogl-win/jogl_awt.dll` | Native (Windows) | JOGL 1.1.1-era bundle | Legacy pinned by checksum | JOGL license | `afc35c840368302e206f5751a5abca2911b5dc5ecc2547804d91612b33d5ad22` |
| `Deploy/jogl-win/jogl_cg.dll` | Native (Windows) | JOGL 1.1.1-era bundle | Legacy pinned by checksum | JOGL license | `d15496c45dd988d4acc4f8441bdc4e2beb3609421cb5c6fc36742039d29180fa` |
| `Deploy/jogl-unix/libgluegen-rt.so` | Native (Linux/Unix) | JOGL 1.1.1-era bundle | Legacy pinned by checksum | JOGL license | `b12462550ad7359c2bd5ee8b0373ff4286542fbe193d14a085dd699d3b2bbfa6` |
| `Deploy/jogl-unix/libjogl.so` | Native (Linux/Unix) | JOGL 1.1.1-era bundle | Legacy pinned by checksum | JOGL license | `5765dc54992755191602336ecf2706141cc20c35e9fa2a6fa0f466f0253e6df5` |
| `Deploy/jogl-unix/libjogl_awt.so` | Native (Linux/Unix) | JOGL 1.1.1-era bundle | Legacy pinned by checksum | JOGL license | `c27d7b258e5016854205ea6a2fb34b37ac06d94260eb0f2b69a5454f1303b96c` |
| `Deploy/jogl-unix/libjogl_cg.so` | Native (Linux/Unix) | JOGL 1.1.1-era bundle | Legacy pinned by checksum | JOGL license | `26013a5734473a753960960b8b0fc1c9e23ccd5b1cdc3d6d934e03de0a5f676a` |
| `Deploy/jogl-mac/libgluegen-rt.jnilib` | Native (macOS legacy) | JOGL 1.1.1-era bundle | Legacy pinned by checksum | JOGL license | `dc182e99f03061f6a1ee7c0701c409830ee9ec459271d3801265739d7e9df182` |
| `Deploy/jogl-mac/libjogl.jnilib` | Native (macOS legacy) | JOGL 1.1.1-era bundle | Legacy pinned by checksum | JOGL license | `d2ea2d3ba0acadd464010622f4a8fce892967cabeb692999a42ceb2621289c22` |
| `Deploy/jogl-mac/libjogl_awt.jnilib` | Native (macOS legacy) | JOGL 1.1.1-era bundle | Legacy pinned by checksum | JOGL license | `92f2c1c730d546f5cf32a58f7ac59946b82880576f6bb50d61751314fbaf0e70` |
| `Deploy/jogl-mac/libjogl_cg.jnilib` | Native (macOS legacy) | JOGL 1.1.1-era bundle | Legacy pinned by checksum | JOGL license | `59d8df4be3b46d52478c960e2a33e080290687a16c69d990df1d871a2902a6d3` |
| `Deploy/sunflow/sunflow.jar` | Supporting renderer/export lib | Sunflow (`http://sunflow.sourceforge.net`) | **Unknown legacy build** | MIT (`Deploy/sunflow/LICENSE`) | `b72a4cd19ecab47a6fc6b094c3251cf63b276996ba23b98f1fd49acf2b9ed704` |
| `Deploy/sunflow/janino.jar` | Supporting lib (Sunflow compiler dep) | Janino (`http://www.janino.net/`) | **Unknown legacy build** | BSD (per `Deploy/jReality/README`) | `bc136ff424efdbd272f613110f81c878d1e8dc9f1dfbfb7c9a639615c2bacecb` |
| `Deploy/XStream/xstream.jar` | Supporting lib (serialization) | XStream (`http://xstream.codehaus.org`) | **Unknown legacy build** | BSD (`Deploy/XStream/LICENSE`) | `84ae98d9e5ea2c596e4161b6b20d9bb3ebe5dbc17f31822835c7dc5b71ccd27f` |
| `Deploy/XStream/xpp3.jar` | Supporting lib (XML parser) | XPP3 (`http://www.extreme.indiana.edu/xgws/xsoap/xpp`) | Legacy line (license file says `Version 1.1.1`) | Indiana University Extreme! Lab license (`Deploy/XStream/xpp-license.txt`) | `ebcdef45cb16eeb113032b27c8537fd98d6f46b1071b6765febd596b8cac0f1a` |

## Reproducible acquisition process

A lockfile-driven process has been added:

- Lockfile: `tools/renderer-dependencies.lock.tsv`
- Fetch/verify script: `tools/fetch-renderer-dependencies.sh`

Usage:

```bash
# Verify all currently vendored files match recorded hashes.
tools/fetch-renderer-dependencies.sh --verify-only

# Download missing entries that have URLs, then verify all hashes.
tools/fetch-renderer-dependencies.sh
```

Notes:

- Some very old artifacts (especially native binaries and jReality-era jars) do not have reliable modern canonical download locations. Those are intentionally treated as checksum-pinned legacy blobs until replaced.
- The lockfile is the source of truth for binary integrity in CI/manual release checks.
