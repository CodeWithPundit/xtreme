#!/bin/bash

# Version update script
VERSION_MAJOR=$(grep "VERSION_MAJOR" version.properties | cut -d'=' -f2)
VERSION_MINOR=$(grep "VERSION_MINOR" version.properties | cut -d'=' -f2)
VERSION_PATCH=$(grep "VERSION_PATCH" version.properties | cut -d'=' -f2)
VERSION_CODE=$(grep "VERSION_CODE" version.properties | cut -d'=' -f2)

echo "Current version: $VERSION_MAJOR.$VERSION_MINOR.$VERSION_PATCH ($VERSION_CODE)"

if [ "$1" == "major" ]; then
    VERSION_MAJOR=$((VERSION_MAJOR + 1))
    VERSION_MINOR=0
    VERSION_PATCH=0
elif [ "$1" == "minor" ]; then
    VERSION_MINOR=$((VERSION_MINOR + 1))
    VERSION_PATCH=0
elif [ "$1" == "patch" ]; then
    VERSION_PATCH=$((VERSION_PATCH + 1))
fi

VERSION_CODE=$((VERSION_CODE + 1))

# Update version.properties
sed -i "s/VERSION_MAJOR=.*/VERSION_MAJOR=$VERSION_MAJOR/" version.properties
sed -i "s/VERSION_MINOR=.*/VERSION_MINOR=$VERSION_MINOR/" version.properties
sed -i "s/VERSION_PATCH=.*/VERSION_PATCH=$VERSION_PATCH/" version.properties
sed -i "s/VERSION_CODE=.*/VERSION_CODE=$VERSION_CODE/" version.properties

echo "New version: $VERSION_MAJOR.$VERSION_MINOR.$VERSION_PATCH ($VERSION_CODE)"

# Update app build.gradle.kts
sed -i "s/versionCode = [0-9]*/versionCode = $VERSION_CODE/" app/build.gradle.kts
sed -i "s/versionName = \".*\"/versionName = \"$VERSION_MAJOR.$VERSION_MINOR.$VERSION_PATCH\"/" app/build.gradle.kts
