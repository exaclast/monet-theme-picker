#!/usr/bin/env bash

# Exit immediately if a command exits with a non-zero status
set -e

git fetch

# Ensure we are on the main branch
CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)
if [ "$CURRENT_BRANCH" != "main" ]; then
    echo "Error: You can only create release tags from the 'main' branch. Current branch is '$CURRENT_BRANCH'."
    exit 1
fi

# Ensure the working directory is clean
if ! git diff --quiet || ! git diff --cached --quiet; then
    echo "Error: You have uncommitted changes. Please commit your version bump before creating a tag."
    exit 1
fi

# Path to the build.gradle.kts file
BUILD_FILE="app/build.gradle.kts"

# Ensure the file exists
if [ ! -f "$BUILD_FILE" ]; then
    echo "Error: $BUILD_FILE not found."
    exit 1
fi

# Parse the versionName and versionCode using grep and sed
VERSION_NAME=$(grep -E 'versionName\s*=\s*"[^"]+"' "$BUILD_FILE" | sed -E 's/.*versionName\s*=\s*"([^"]+)".*/\1/')
VERSION_CODE=$(grep -E 'versionCode\s*=\s*[0-9]+' "$BUILD_FILE" | sed -E 's/.*versionCode\s*=\s*([0-9]+).*/\1/')

if [ -z "$VERSION_NAME" ]; then
    echo "Error: Could not parse versionName from $BUILD_FILE"
    exit 1
fi

if [ -z "$VERSION_CODE" ]; then
    echo "Error: Could not parse versionCode from $BUILD_FILE"
    exit 1
fi

# Check if versionCode has been bumped since the last tag
LAST_TAG=$(git describe --tags --abbrev=0 2>/dev/null || echo "")
if [ -n "$LAST_TAG" ]; then
    # Use git show to read the build file exactly as it existed at the previous tag
    OLD_VERSION_CODE=$(git show "${LAST_TAG}:${BUILD_FILE}" 2>/dev/null | grep -E 'versionCode\s*=\s*[0-9]+' | sed -E 's/.*versionCode\s*=\s*([0-9]+).*/\1/')
    if [ -n "$OLD_VERSION_CODE" ] && [ "$VERSION_CODE" -le "$OLD_VERSION_CODE" ]; then
        echo "Error: versionCode ($VERSION_CODE) must be strictly greater than the versionCode from the last tag $LAST_TAG ($OLD_VERSION_CODE)."
        echo "Please increment the versionCode in $BUILD_FILE before tagging."
        exit 1
    fi
fi

# Construct the tag name (prefixing with 'v' is standard)
TAG_NAME="v${VERSION_NAME}"

# Check if the tag already exists
if git rev-parse "$TAG_NAME" >/dev/null 2>&1; then
    echo "Error: Tag '$TAG_NAME' already exists. We will not overwrite it."
    exit 1
fi

# Create and push the tag
echo "Creating tag: $TAG_NAME"
git tag "$TAG_NAME" && git push origin "$TAG_NAME"

echo "Tag $TAG_NAME created and pushed successfully!"
