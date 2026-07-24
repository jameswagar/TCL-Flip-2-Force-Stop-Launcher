#!/bin/bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
BUILD="$ROOT/build/test-classes"
if [[ -z "${JAVA_HOME:-}" ]] && command -v brew >/dev/null 2>&1; then
  JAVA_HOME="$(brew --prefix openjdk)/libexec/openjdk.jdk/Contents/Home"
  export JAVA_HOME
fi
rm -rf "$BUILD"
mkdir -p "$BUILD"
javac -d "$BUILD" \
  "$ROOT/src/com/dumbphone/forcestop/ActivePackageParser.java" \
  "$ROOT/src/com/dumbphone/forcestop/RecentTaskInfo.java" \
  "$ROOT/src/com/dumbphone/forcestop/RecentTaskParser.java" \
  "$ROOT/src/com/dumbphone/forcestop/ForceStopPolicy.java" \
  "$ROOT/tests/com/dumbphone/forcestop/ActivePackageParserTest.java" \
  "$ROOT/tests/com/dumbphone/forcestop/RecentTaskParserTest.java" \
  "$ROOT/tests/com/dumbphone/forcestop/ForceStopPolicyTest.java"
java -cp "$BUILD" com.dumbphone.forcestop.ActivePackageParserTest
java -cp "$BUILD" com.dumbphone.forcestop.RecentTaskParserTest
java -cp "$BUILD" com.dumbphone.forcestop.ForceStopPolicyTest
