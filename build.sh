#!/bin/bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
BUILD="$ROOT/build"
SDK_ROOT="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-$HOME/Library/Android/sdk}}"
BT="$SDK_ROOT/build-tools/${BUILD_TOOLS_VERSION:-35.0.0}"
ANDROID_JAR="$SDK_ROOT/platforms/android-${ANDROID_PLATFORM_VERSION:-35}/android.jar"
KEYSTORE="${FORCE_STOP_KEYSTORE:-$ROOT/force-stop-launcher.jks}"
KEY_ALIAS="${FORCE_STOP_KEY_ALIAS:-force-stop}"
: "${FORCE_STOP_STOREPASS:?Set FORCE_STOP_STOREPASS to build or sign the APK}"
FORCE_STOP_KEYPASS="${FORCE_STOP_KEYPASS:-$FORCE_STOP_STOREPASS}"

if [[ -z "${JAVA_HOME:-}" ]] && command -v brew >/dev/null 2>&1; then
  JAVA_HOME="$(brew --prefix openjdk)/libexec/openjdk.jdk/Contents/Home"
  export JAVA_HOME
fi
export PATH="${JAVA_HOME:+$JAVA_HOME/bin:}$PATH"

rm -rf "$BUILD"
mkdir -p "$BUILD/classes" "$BUILD/dex"

if [[ ! -f "$KEYSTORE" ]]; then
  keytool -genkeypair -noprompt -keystore "$KEYSTORE" \
    -storepass "$FORCE_STOP_STOREPASS" -keypass "$FORCE_STOP_KEYPASS" \
    -alias "$KEY_ALIAS" -keyalg RSA -keysize 2048 -validity 10000 \
    -dname "CN=Force Stop, OU=Dumbphone, O=Local, C=US"
fi

"$BT/aapt2" compile --dir "$ROOT/res" -o "$BUILD/resources.zip"
"$BT/aapt2" link -o "$BUILD/unsigned.apk" -I "$ANDROID_JAR" \
  --manifest "$ROOT/AndroidManifest.xml" --min-sdk-version 24 --target-sdk-version 35 \
  "$BUILD/resources.zip"

javac -source 8 -target 8 -bootclasspath "$ANDROID_JAR" \
  -d "$BUILD/classes" "$ROOT"/src/com/dumbphone/forcestop/*.java

CLASS_FILES=("$BUILD"/classes/com/dumbphone/forcestop/*.class)
"$BT/d8" --lib "$ANDROID_JAR" --min-api 24 --output "$BUILD/dex" "${CLASS_FILES[@]}"
(cd "$BUILD/dex" && zip -q -j "$BUILD/unsigned.apk" classes.dex)
"$BT/zipalign" -f 4 "$BUILD/unsigned.apk" "$BUILD/aligned.apk"
"$BT/apksigner" sign --ks "$KEYSTORE" --ks-key-alias "$KEY_ALIAS" \
  --ks-pass "pass:$FORCE_STOP_STOREPASS" --key-pass "pass:$FORCE_STOP_KEYPASS" \
  --out "$ROOT/ForceStop.apk" "$BUILD/aligned.apk"
"$BT/apksigner" verify --verbose --print-certs "$ROOT/ForceStop.apk"
