#!/bin/bash

echo "========================================="
echo " Running You Are The Monster"
echo "========================================="

# Check if Java is available
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed."
    echo "Please install Java 17 or higher from https://adoptium.net/"
    exit 1
fi

# Check if build exists
if [ ! -d "build/classes" ]; then
    echo "Game not built yet. Building..."
    ./build.sh
    if [ $? -ne 0 ]; then
        echo "Build failed."
        exit 1
    fi
fi

# Detect platform
OS=$(uname -s)
ARCH=$(uname -m)
case $OS in
    Linux*)     
        if [ "$ARCH" = "aarch64" ]; then
            PLATFORM="linux-aarch64"
        else
            PLATFORM="linux-x64"
        fi
        ;;
    Darwin*)    
        if [ "$ARCH" = "arm64" ]; then
            PLATFORM="macos-aarch64"
        else
            PLATFORM="macos-x64"
        fi
        ;;
    MINGW*|MSYS*|CYGWIN*) 
        PLATFORM="windows-x64"
        ;;
    *)          
        PLATFORM="linux-x64"
        ;;
esac

# Find JavaFX
JAVAFX_DIR="lib/javafx"
JAVAFX_VERSION="17.0.2"
JAVAFX_MODS="$JAVAFX_DIR/javafx-sdk-${JAVAFX_VERSION}/lib"
if [ ! -d "$JAVAFX_MODS" ]; then
    JAVAFX_MODS=$(find "$JAVAFX_DIR" -name "lib" -type d | head -1)
fi

if [ ! -d "$JAVAFX_MODS" ]; then
    echo "Error: JavaFX not found. Please run ./build.sh first."
    exit 1
fi

# Build classpath
CP="build/classes:$JAVAFX_MODS/*"
if [ -f "lib/json.jar" ]; then
    CP="$CP:lib/json.jar"
fi

echo "Starting game..."
java -cp "$CP" Game
