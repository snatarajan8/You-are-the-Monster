#!/bin/bash

echo "========================================="
echo " Building You Are The Monster"
echo "========================================="

# Check if Java is available
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed."
    echo "Please install Java 17 or higher from https://adoptium.net/"
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

# Create build directory
mkdir -p build/classes build/libs

# Detect platform
OS=$(uname -s)
case $OS in
    Linux*)     PLATFORM=linux;;
    Darwin*)    PLATFORM=mac;;
    MINGW*|MSYS*|CYGWIN*) PLATFORM=win;;
    *)          PLATFORM=linux;;
esac
echo "Platform: $PLATFORM"

# Download JavaFX if not present
JAVAFX_DIR="lib/javafx"
if [ ! -d "$JAVAFX_DIR" ]; then
    echo ""
    echo "Downloading JavaFX..."
    mkdir -p "$JAVAFX_DIR"
    
    JAVAFX_VERSION="17.0.2"
    JAVAFX_URL="https://download2.gluonhq.com/openjfx/${JAVAFX_VERSION}/openjfx-${JAVAFX_VERSION}_${PLATFORM}-bin-sdk.zip"
    
    echo "Downloading from: $JAVAFX_URL"
    curl -L -o /tmp/javafx.zip "$JAVAFX_URL" 2>/dev/null
    
    if [ $? -eq 0 ]; then
        echo "Extracting JavaFX..."
        unzip -q -o /tmp/javafx.zip -d "$JAVAFX_DIR"
        rm /tmp/javafx.zip
    else
        echo "Error: Could not download JavaFX."
        echo "Please download JavaFX SDK manually from https://gluonhq.com/products/javafx/"
        echo "Extract it to lib/javafx/ directory."
        exit 1
    fi
fi

# Find JavaFX jars
JAVAFX_MODS="$JAVAFX_DIR/javafx-sdk-${JAVAFX_VERSION}/lib"
if [ ! -d "$JAVAFX_MODS" ]; then
    # Try alternate path
    JAVAFX_MODS=$(find "$JAVAFX_DIR" -name "lib" -type d | head -1)
fi

if [ ! -d "$JAVAFX_MODS" ]; then
    echo "Error: Could not find JavaFX lib directory."
    echo "Please check lib/javafx/ directory structure."
    exit 1
fi

echo "Using JavaFX from: $JAVAFX_MODS"

# Download JSON library if not present
JSON_JAR="lib/json.jar"
if [ ! -f "$JSON_JAR" ]; then
    echo ""
    echo "Downloading JSON library..."
    mkdir -p lib
    curl -L -o "$JSON_JAR" "https://repo1.maven.org/maven2/org/json/json/20231013/json-20231013.jar" 2>/dev/null
    if [ $? -ne 0 ]; then
        echo "Warning: Could not download JSON library."
        echo "Some features may not work without it."
    fi
fi

# Build classpath
CP="$JAVAFX_MODS/*"
if [ -f "$JSON_JAR" ]; then
    CP="$CP:$JSON_JAR"
fi

echo ""
echo "Compiling..."
find src -name "*.java" > /tmp/sources.txt
javac -cp "$CP" -d build/classes @/tmp/sources.txt 2>&1

if [ $? -ne 0 ]; then
    echo ""
    echo "Error: Compilation failed."
    exit 1
fi

echo "Copying resources..."
cp -r src/resources build/classes/
cp src/*.json build/classes/ 2>/dev/null || true
cp -r src/levels build/classes/ 2>/dev/null || true
cp src/GameBackground.fxml build/classes/ 2>/dev/null || true

echo ""
echo "Build successful!"
echo ""
echo "To run the game:"
echo "  ./run.sh"
