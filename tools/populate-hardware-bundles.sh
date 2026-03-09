#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

copy_bundle() {
  local target="$1"; shift
  local source_dir="$1"; shift
  mkdir -p "$ROOT/Deploy/hardware/$target"
  for file in "$@"; do
    cp "$ROOT/$source_dir/$file" "$ROOT/Deploy/hardware/$target/$file"
  done
}

copy_bundle "linux-x86_64" "Deploy/jogl-unix" \
  libgluegen-rt.so libjogl.so libjogl_awt.so libjogl_cg.so
copy_bundle "windows-x86_64" "Deploy/jogl-win" \
  gluegen-rt.dll jogl.dll jogl_awt.dll jogl_cg.dll
copy_bundle "macos-x86_64" "Deploy/jogl-mac" \
  libgluegen-rt.jnilib libjogl.jnilib libjogl_awt.jnilib libjogl_cg.jnilib
copy_bundle "macos-arm64" "Deploy/jogl-mac" \
  libgluegen-rt.jnilib libjogl.jnilib libjogl_awt.jnilib libjogl_cg.jnilib

echo "Hardware bundles populated from legacy JOGL native directories."
