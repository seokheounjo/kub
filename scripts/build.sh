#!/bin/bash

# Build script for guarantee application
set -e

echo "🔨 Building guarantee application..."

# Navigate to guarantee directory
cd guarantee

# Build the application
echo "📦 Building Java application..."
./gradlew clean bootJar

echo "🐳 Building Docker image..."
DOCKER_TAG=${1:-latest}
docker build -t guarantee:${DOCKER_TAG} .

echo "✅ Build completed successfully!"
echo "Docker image: guarantee:${DOCKER_TAG}"