# Renderer dependency update policy

This policy covers 3dt rendering dependencies under `Deploy/` (jReality, JOGL/native binaries, and supporting jars).

## Cadence

- **Quarterly** dependency review for all renderer and graphics-adjacent jars.
- **Monthly** security/license watch for any dependency with known CVE activity.
- **Before each release** run lockfile verification and compatibility checklist.

## Required maintenance actions

1. Re-run `tools/fetch-renderer-dependencies.sh --verify-only` and ensure all hashes match.
2. For each dependency with a pinned URL, check if a maintained compatible version exists.
3. For native binaries, validate platform/architecture relevance and retire obsolete blobs when no longer needed.
4. Update `tools/renderer-dependencies.lock.tsv` and `docs-renderer-dependencies.md` when any binary changes.
5. Re-check license terms and attribution files when dependency versions change.

## Compatibility test checklist

When updating renderer dependencies or native binaries, all checks below must pass before merge:

### Runtime bootstrap

- 3dt launches with software renderer on current LTS JDK.
- If hardware backend is enabled, startup does not crash when backend initialization fails.
- Renderer selection diagnostics include requested/selected mode and fallback reason.

### Data and interaction smoke

- Open representative `.ds` and `.cgd` inputs.
- Perform camera rotate/pan/zoom.
- Exercise tile add/remove/recolor interactions.
- Validate screenshot export.
- Validate Sunflow export path.

### Cross-platform/native checks

- Linux x64: verify no unresolved symbols in JOGL/gluegen natives.
- Windows x64: verify native loading and DLL search-path behavior.
- macOS (if supported): verify fallback behavior for unsupported legacy `.jnilib` artifacts.

### Diagnostics and supportability

- Run `Deploy/bin/3dt-selfcheck` (or `.bat` on Windows).
- Confirm output reports backend availability and native load attempts.
- Confirm generated diagnostics are attachable to support tickets.

## Exit criteria for updates

A renderer dependency update is accepted only when:

- lockfile hashes are updated and verified,
- compatibility checklist passes,
- troubleshooting documentation remains accurate,
- and fallback behavior remains functional on unsupported hardware/driver stacks.
