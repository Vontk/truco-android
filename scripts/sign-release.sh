#!/usr/bin/env bash
set -euo pipefail
project_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
android_sdk="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-/tmp/campus-sdk}}"
key_dir="${TRUCO_SIGNING_DIR:-$project_dir/.signing}"
unsigned_apk="${1:-$project_dir/app/build/outputs/apk/release/app-release-unsigned.apk}"
version="$(sed -n 's/.*versionName = "\([^"]*\)".*/\1/p' "$project_dir/app/build.gradle.kts" | head -n 1)"
build_tools="$(find "$android_sdk/build-tools" -mindepth 1 -maxdepth 1 -type d | sort -V | tail -n 1)"
mkdir -p "$project_dir/artifacts"
"$build_tools/zipalign" -f -P 16 4 "$unsigned_apk" "$project_dir/artifacts/aligned.apk"
"$build_tools/apksigner" sign --ks "$key_dir/truco-release.jks" --ks-key-alias truco --ks-pass "file:$key_dir/password" --key-pass "file:$key_dir/password" --out "$project_dir/artifacts/truco-$version.apk" "$project_dir/artifacts/aligned.apk"
"$build_tools/apksigner" verify --verbose --print-certs "$project_dir/artifacts/truco-$version.apk" > "$project_dir/artifacts/signature.txt"
"$build_tools/zipalign" -c -P 16 4 "$project_dir/artifacts/truco-$version.apk"
rm "$project_dir/artifacts/aligned.apk"
(cd "$project_dir/artifacts" && sha256sum truco-*.apk > SHA256SUMS)
