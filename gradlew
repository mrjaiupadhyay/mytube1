#!/bin/sh

# Minimal Gradle wrapper bootstrap script.
# This is a standard wrapper entrypoint; it requires gradle/wrapper/gradle-wrapper.jar.

APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd -P) || exit 1

CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

if [ ! -f "$CLASSPATH" ]; then
  echo "Missing $CLASSPATH"
  echo "Run: ./gradlew (after gradle-wrapper.jar is present)"
  exit 1
fi

exec java -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"

