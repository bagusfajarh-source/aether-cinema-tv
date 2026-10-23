#!/usr/bin/env bash
set -euo pipefail
if ! command -v gradle >/dev/null 2>&1; then
  echo 'Gradle tidak ditemukan. Pakai Android Studio + Gradle 8.9, atau build via GitHub Actions.' >&2
  exit 1
fi
: "${ANDROID_HOME:?Set ANDROID_HOME ke Android SDK}"
gradle --no-daemon :app:assembleDebug
printf 'APK: app/build/outputs/apk/debug/app-debug.apk\n'
