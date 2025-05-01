#!/bin/bash

# pdi.sh - Publish Docker Image
# This script creates a Docker image from an executable JAR for a given Amper module
# It uses the classpath information from the Amper-generated args file

# Check if a module name was provided
if [ $# -ne 1 ]; then
    echo "Usage: $0 <module-name>"
    echo "Example: $0 rest"
    exit 1
fi

MODULE_NAME=$1
JAR_DIR="build/tasks/_${MODULE_NAME}_executableJarJvm"
JAR_FILE="${MODULE_NAME}-jvm-executable.jar"
JAR_PATH="${JAR_DIR}/${JAR_FILE}"

# Check if the JAR file exists
if [ ! -f "$JAR_PATH" ]; then
    echo "Error: JAR file not found at $JAR_PATH"
    echo "Make sure you've built the module first."
    exit 1
fi

# Find all kotlin-args files in the build/temp directory
ARGS_FILES=$(find build/temp -name "kotlin-args-*.txt" -type f)

if [ -z "$ARGS_FILES" ]; then
    echo "Error: No kotlin-args files found in build/temp directory"
    echo "Make sure you've run the application at least once."
    exit 1
fi

# Initialize ARGS_FILE and CLASSPATH variables
ARGS_FILE=""
CLASSPATH=""

# Iterate through all args files to find the first one that contains our JAR file
for file in $ARGS_FILES; do
    # Extract the classpath from the args file
    cp=$(sed -n '2p' "$file")

    # Check if the classpath contains our JAR file
    if [[ "$cp" == *"$MODULE_NAME"* ]]; then
        ARGS_FILE="$file"
        CLASSPATH="$cp"
        break
    fi
done

if [ -z "$ARGS_FILE" ]; then
    echo "Error: No args file found that contains the expected JAR file ($JAR_FILE)"
    echo "Make sure you've run the correct module."
    exit 1
fi

echo "Using args file: $ARGS_FILE"

# Create a temporary directory for the Docker build
TEMP_DIR=$(mktemp -d)
trap "rm -rf $TEMP_DIR" EXIT

# Create a lib directory in the temp directory
mkdir -p "$TEMP_DIR/lib"

# Copy the JAR file to the temporary directory
cp "$JAR_PATH" "$TEMP_DIR/app.jar"

# Parse the classpath and copy all dependencies to the lib directory
echo "Copying dependencies..."
IFS=':' read -ra PATHS <<< "$CLASSPATH"
for path in "${PATHS[@]}"; do
    # Skip the main JAR file as we've already copied it
    if [[ "$path" == *"$JAR_FILE"* ]]; then
        continue
    fi

    # Only copy .jar files
    if [[ "$path" == *.jar ]]; then
        filename=$(basename "$path")
        cp "$path" "$TEMP_DIR/lib/$filename"
    fi
done

# Extract the main class from the args file
MAIN_CLASS=$(sed -n '4p' "$ARGS_FILE")

# Set PORT based on MODULE_NAME
if [ "$MODULE_NAME" == "admin" ]; then
    PORT=8081
else
    PORT=8080
fi

# Create a Dockerfile
cat > "$TEMP_DIR/Dockerfile" << EOF
FROM eclipse-temurin:23-jre

WORKDIR /app

COPY app.jar /app/app.jar
COPY lib/ /app/lib/

EXPOSE $PORT

ENTRYPOINT ["java", "-cp", "/app/app.jar:/app/lib/*", "$MAIN_CLASS"]
EOF

# Build the Docker image
IMAGE_NAME="ktor-docker-image:ktor-chat-${MODULE_NAME}"
echo "Building Docker image: $IMAGE_NAME"
docker build -t "$IMAGE_NAME" "$TEMP_DIR"

echo "Docker image built successfully: $IMAGE_NAME"
echo "You can run it with: docker run -p $PORT:$PORT $IMAGE_NAME"
echo "Or use it in docker-compose.yml"
