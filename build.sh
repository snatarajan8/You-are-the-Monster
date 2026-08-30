#!/bin/bash

echo "========================================="
echo " Building You Are The Monster"
echo "========================================="

# Check if Java is available
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed."
    echo "Please install Java 17 or higher."
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "Error: Java 17 or higher is required."
    echo "Current version: $JAVA_VERSION"
    exit 1
fi

echo "Java version: $(java -version 2>&1 | head -n 1)"

# Build with Gradle
echo ""
echo "Building fat JAR..."
if [ -f "./gradlew" ]; then
    ./gradlew shadowJar
else
    echo "Error: gradlew not found."
    echo "Please install Gradle or use: gradle shadowJar"
    exit 1
fi

if [ $? -ne 0 ]; then
    echo "Error: Build failed."
    exit 1
fi

echo ""
echo "Build successful!"
echo "Fat JAR: build/libs/YouAreTheMonster-1.0.0.jar"
echo ""
echo "To run directly:"
echo "  java -jar build/libs/YouAreTheMonster-1.0.0.jar"
echo ""
echo "To create native executable (requires jpackage):"
echo "  jpackage --input build/libs --main-jar YouAreTheMonster-1.0.0.jar --name YouAreTheMonster --type app-image --dest build/executables"
