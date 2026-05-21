#!/bin/bash
# Packages Sa Kabukiran into a self-contained .app (macOS) or app-image (other OS).
# Requirements: Java 21+, Maven 3.8+
# Run from the animal-farm/ directory: ./package.sh

set -e
DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$DIR"

APP_NAME="SaKabukiran"
VERSION="1.0.0"
MAIN_CLASS="com.animalfarm.App"
MAIN_JAR="animal-farm.jar"
LIB_DIR="target/lib"
OUT_DIR="target/package"

echo "==> Building..."
mvn clean package -q

echo "==> Collecting dependencies in $LIB_DIR..."
# Copy app JAR alongside its deps
cp "target/$MAIN_JAR" "$LIB_DIR/"

echo "==> Running jpackage..."
rm -rf "$OUT_DIR"
mkdir -p "$OUT_DIR"

jpackage \
  --input        "$LIB_DIR" \
  --main-jar     "$MAIN_JAR" \
  --main-class   "$MAIN_CLASS" \
  --name         "$APP_NAME" \
  --app-version  "$VERSION" \
  --dest         "$OUT_DIR" \
  --type         app-image \
  --java-options "--module-path \$APPDIR/lib" \
  --java-options "--add-modules javafx.controls,javafx.graphics,javafx.media" \
  --java-options "--add-opens javafx.graphics/com.sun.glass.utils=ALL-UNNAMED" \
  --java-options "-Djava.library.path=\$APPDIR/lib"

echo ""
echo "Done! App is at: $OUT_DIR/$APP_NAME"
if [[ "$OSTYPE" == "darwin"* ]]; then
    echo "      Run it:  open \"$OUT_DIR/$APP_NAME.app\""
    echo "      Or zip:  cd $OUT_DIR && zip -r SaKabukiran.zip $APP_NAME.app"
fi
