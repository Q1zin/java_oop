#!/bin/bash
set -e

echo "Building Java client..."
cd client-java
gradle wrapper
./gradlew build
cd ..

JAR=$(ls client-java/build/libs/client-java-*.jar | head -n 1)
mkdir -p src-tauri/java/input
cp "$JAR" src-tauri/java/input/client-java.jar

echo "Packaging into ChatClient.app..."
rm -rf src-tauri/java/ChatClient.app
jpackage \
  --name ChatClient \
  --app-version 1.0 \
  --type app-image \
  --input src-tauri/java/input \
  --main-jar client-java.jar \
  --main-class client.Client \
  --dest src-tauri/java