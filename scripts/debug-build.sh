#!/bin/bash

# Xtreme IPTV - Local Debug Helper Script

echo "🚀 Xtreme IPTV Debug Helper"
echo "==========================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to check file existence
check_file() {
    if [ -f "$1" ]; then
        echo -e "${GREEN}✓ Found: $1${NC}"
        return 0
    else
        echo -e "${RED}✗ Missing: $1${NC}"
        return 1
    fi
}

# Function to check directory
check_dir() {
    if [ -d "$1" ]; then
        echo -e "${GREEN}✓ Directory: $1${NC}"
        return 0
    else
        echo -e "${RED}✗ Missing directory: $1${NC}"
        return 1
    fi
}

echo -e "\n${YELLOW}1. Checking Critical Files${NC}"
echo "------------------------"

# Check root files
check_file "settings.gradle.kts"
check_file "build.gradle.kts"
check_file "gradle.properties"
check_file "gradlew"
check_file "gradlew.bat"
check_file "version.properties"

# Check gradle wrapper
check_dir "gradle/wrapper"
check_file "gradle/wrapper/gradle-wrapper.jar"
check_file "gradle/wrapper/gradle-wrapper.properties"

# Check app module
check_dir "app/src/main"
check_file "app/src/main/AndroidManifest.xml"
check_dir "app/src/main/java/com/xtremeiptv"
check_file "app/src/main/java/com/xtremeiptv/XtremeIPTVApplication.kt"
check_file "app/src/main/java/com/xtremeiptv/MainActivity.kt"

# Check resources
check_dir "app/src/main/res/values"
check_file "app/src/main/res/values/strings.xml"
check_file "app/src/main/res/values/colors.xml"
check_file "app/src/main/res/values/themes.xml"

echo -e "\n${YELLOW}2. Running Gradle Checks${NC}"
echo "------------------------"

# Try to sync gradle
echo "Syncing Gradle..."
./gradlew --version > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Gradle synced successfully${NC}"
else
    echo -e "${RED}✗ Gradle sync failed${NC}"
fi

# Check for compile errors
echo -e "\n${YELLOW}3. Compilation Check${NC}"
echo "------------------------"
./gradlew compileDebugKotlin --stacktrace > compile_errors.txt 2>&1

if grep -q "error:" compile_errors.txt; then
    echo -e "${RED}✗ Compilation errors found:${NC}"
    grep -n "error:" compile_errors.txt | head -10
    echo "... (see compile_errors.txt for full list)"
else
    echo -e "${GREEN}✓ No compilation errors${NC}"
fi

# Check for common issues
echo -e "\n${YELLOW}4. Common Issue Scan${NC}"
echo "------------------------"

# Check for missing imports in Kotlin files
echo "Scanning for common Kotlin issues..."
MISSING_IMPORTS=$(grep -r "unresolved reference" --include="*.kt" . | wc -l)
if [ $MISSING_IMPORTS -gt 0 ]; then
    echo -e "${RED}✗ Found $MISSING_IMPORTS unresolved references${NC}"
else
    echo -e "${GREEN}✓ No unresolved references${NC}"
fi

# Check for null safety issues
NULL_ISSUES=$(grep -r "Only safe or non-null asserted calls are allowed" --include="*.kt" . | wc -l)
if [ $NULL_ISSUES -gt 0 ]; then
    echo -e "${RED}✗ Found $NULL_ISSUES null safety issues${NC}"
else
    echo -e "${GREEN}✓ No null safety issues${NC}"
fi

# Check for Hilt annotations
HILT_ISSUES=$(grep -r "unresolved reference .*Hilt" --include="*.kt" . | wc -l)
if [ $HILT_ISSUES -gt 0 ]; then
    echo -e "${RED}✗ Found $HILT_ISSUES Hilt-related issues${NC}"
else
    echo -e "${GREEN}✓ Hilt seems configured correctly${NC}"
fi

# Check for Room
ROOM_ISSUES=$(grep -r "unresolved reference .*Room" --include="*.kt" . | wc -l)
if [ $ROOM_ISSUES -gt 0 ]; then
    echo -e "${RED}✗ Found $ROOM_ISSUES Room-related issues${NC}"
else
    echo -e "${GREEN}✓ Room seems configured correctly${NC}"
fi

echo -e "\n${YELLOW}5. Build Attempt${NC}"
echo "------------------------"

# Try to build debug APK
./gradlew assembleDebug > build_log.txt 2>&1 &
BUILD_PID=$!
echo "Building... (PID: $BUILD_PID)"

# Wait with timeout
wait $BUILD_PID
BUILD_RESULT=$?

if [ $BUILD_RESULT -eq 0 ]; then
    echo -e "${GREEN}✓ Build successful!${NC}"
    echo "APK location: app/build/outputs/apk/debug/"
else
    echo -e "${RED}✗ Build failed (exit code: $BUILD_RESULT)${NC}"
    echo "Last 20 lines of error:"
    tail -20 build_log.txt | grep -E "error:|FAILED" --color=always
fi

echo -e "\n${YELLOW}6. Debug Summary${NC}"
echo "------------------------"

# Generate summary
echo "=== QUICK FIX GUIDE ===" > debug_summary.txt
echo "" >> debug_summary.txt

if [ $MISSING_IMPORTS -gt 0 ]; then
    echo "🔧 UNRESOLVED REFERENCES:" >> debug_summary.txt
    echo "   Run: ./gradlew kaptDebugKotlin" >> debug_summary.txt
    echo "   Check: missing imports in affected files" >> debug_summary.txt
    echo "" >> debug_summary.txt
fi

if [ $NULL_ISSUES -gt 0 ]; then
    echo "🔧 NULL SAFETY:" >> debug_summary.txt
    echo "   Add '?' for nullable types or '!!' if sure not null" >> debug_summary.txt
    echo "" >> debug_summary.txt
fi

if [ $HILT_ISSUES -gt 0 ]; then
    echo "🔧 HILT ISSUES:" >> debug_summary.txt
    echo "   Check: @AndroidEntryPoint, @Inject constructor, @Module" >> debug_summary.txt
    echo "   Run: ./gradlew kaptDebugKotlin" >> debug_summary.txt
    echo "" >> debug_summary.txt
fi

if [ $ROOM_ISSUES -gt 0 ];
