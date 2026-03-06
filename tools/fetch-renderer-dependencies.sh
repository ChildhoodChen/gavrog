#!/usr/bin/env bash
set -euo pipefail

LOCKFILE="tools/renderer-dependencies.lock.tsv"
VERIFY_ONLY=0
if [[ "${1-}" == "--verify-only" ]]; then
  VERIFY_ONLY=1
fi

if [[ ! -f "$LOCKFILE" ]]; then
  echo "Missing lockfile: $LOCKFILE" >&2
  exit 1
fi

status=0
while IFS=$'\t' read -r path sha256 url license notes; do
  [[ -z "${path}" || "${path:0:1}" == "#" ]] && continue

  if [[ ! -f "$path" ]]; then
    if [[ -n "${url}" && "$VERIFY_ONLY" -eq 0 ]]; then
      echo "Downloading $path"
      mkdir -p "$(dirname "$path")"
      curl -fsSL "$url" -o "$path"
    else
      echo "MISSING: $path"
      status=1
      continue
    fi
  fi

  actual="$(sha256sum "$path" | awk '{print $1}')"
  if [[ "$actual" != "$sha256" ]]; then
    echo "HASH MISMATCH: $path"
    echo "  expected: $sha256"
    echo "  actual:   $actual"
    status=1
  else
    echo "OK: $path"
  fi
done < "$LOCKFILE"

exit "$status"
