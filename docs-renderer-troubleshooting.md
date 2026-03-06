# 3dt renderer troubleshooting

This guide focuses on runtime failures around jReality/JOGL/native loading.

## Quick diagnostics first

Run:

```bash
Deploy/bin/3dt-selfcheck
```

On Windows:

```bat
Deploy\bin\3dt-selfcheck.bat
```

Attach the full output to support reports.

## Common failures and fixes

### 1) Missing symbol / unresolved native symbol

Typical symptoms:

- `UnsatisfiedLinkError: ... undefined symbol ...`
- `symbol lookup error` from `libjogl*.so` or `libgluegen-rt.so`

Likely cause:

- Native binary built against incompatible system GL/driver ABI.

Actions:

1. Keep software renderer enabled (`-Dorg.gavrog.3dt.renderer=software`).
2. Confirm `java.library.path` points to expected native directory only.
3. Check native file hashes using lockfile verification.
4. Update GPU driver stack if hardware backend is required.

### 2) Driver mismatch / incompatible OpenGL runtime

Typical symptoms:

- backend initializes then crashes,
- context creation failure,
- driver-specific errors in stderr.

Likely cause:

- Old JOGL native binaries vs modern driver stack.

Actions:

1. Use software renderer as stable fallback.
2. If testing hardware mode, explicitly set backend order via `-Dorg.gavrog.3dt.hardware.backends=...`.
3. Collect `3dt-selfcheck` output and OS/GPU/driver details.

### 3) Unsupported GPU or OpenGL version

Typical symptoms:

- no hardware backend candidate loads,
- black window or immediate fallback.

Likely cause:

- GPU/driver does not meet required OpenGL capabilities for selected backend.

Actions:

1. Run in software renderer mode.
2. Avoid forcing hardware mode in launcher scripts on unsupported hosts.
3. For long-term mitigation, prefer maintained renderer backends with current native support.

### 4) Wrong architecture native binaries (32/64-bit mismatch)

Typical symptoms:

- `wrong ELF class`, `Bad CPU type`, or architecture mismatch on DLL load.

Likely cause:

- Legacy native binaries do not match JVM/OS architecture.

Actions:

1. Verify JVM architecture (`java -version`) and OS architecture.
2. Use software renderer fallback.
3. Do not ship architecture-incompatible natives in future packaging updates.

## Fallback guidance

- **Default recommendation:** keep `-Dorg.gavrog.3dt.renderer=software` for stability.
- Use hardware mode only in explicitly tested environments.
- If hardware is required, capture self-check output plus full stderr logs before escalation.
