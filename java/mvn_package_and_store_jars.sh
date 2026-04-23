#!/bin/bash

# Exit immediately if a command exits with a non-zero status
set -e
echo "🚀 Starting Build Process..."

# 1. Build the JAR first (on your Mac)
mvn clean package

# 2. Define the destination folder
DEST_FOLDER=$DESTINATION_FOLDER

# 3. Create the folder if it doesn't exist. The -p flag prevents errors if the folder already exists
mkdir -p "$DEST_FOLDER"

# 4. Clean the folder so we don't have stale JARs from previous runs
echo "🧹 Cleaning old JARs..."
#rm -f "$DEST_FOLDER"/*.jar
find "$DEST_FOLDER" -name "*.jar" -type f -delete

# 5. Find & copy all executable JARs
# This searches all subdirectories for .jar files in 'target' folders
# and moves them into our destination
echo "📂 Collecting JARs into /$DEST_FOLDER..."
find . -name "*.jar" -path "*/target/*" ! -name "original-*.jar" ! -path "./$DEST_FOLDER/*" -exec cp {} "$DEST_FOLDER/" \;
echo "✅ Build Complete! Your JARs are in: $(pwd)/$DEST_FOLDER"
ls -lh "$DEST_FOLDER"
