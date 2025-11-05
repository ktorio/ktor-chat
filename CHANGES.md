# Documentation Updates

This document summarizes the corrections made to the README.md file to ensure accuracy with the actual codebase structure.

## Changes Made

### 1. Terminology Correction (Line 15)
- **Changed:** "amper modules" → "modules"
- **Reason:** The project uses Gradle build system, not Amper. No Amper configuration files exist in the repository.

### 2. Module Table Addition (Line 26)
- **Added:** `app / wasmJs` module entry
- **Reason:** The wasmJs web application exists in the codebase but was not documented in the module table.

### 3. Mermaid Diagram Corrections (Lines 37, 40-41)
- **Removed:** Non-existent `htmx` server module
- **Added:** `wasmJs` application module (line 37)
- **Restored:** `admin` server module (line 41)
- **Reason:** The diagram should accurately reflect the actual module structure.

### 4. File Path Corrections

#### ChatViewModelProvider.kt (Line 56)
- **Changed:** `app/common/src@android/ktor/chat/vm/ChatViewModelProvider.kt`
- **To:** `app/common/src/androidMain/kotlin/vm/ChatViewModelProvider.kt`
- **Reason:** The original path used an invalid syntax and incorrect directory structure.

#### Server REST Main File (Line 63)
- **Changed:** `server/rest/src/Application.kt`
- **To:** `server/rest/src/main/kotlin/Rest.kt`
- **Reason:** The actual main file is named `Rest.kt` and located in the standard Gradle source directory structure.

#### Desktop App Main File (Line 64)
- **Changed:** `app/desktop/src/main.kt`
- **To:** `app/desktop/src/main/kotlin/main.kt`
- **Reason:** The file is located in the standard Kotlin source directory structure.

#### Android App Main File (Line 65)
- **Changed:** `app/android/src/MainActivity.kt`
- **To:** `app/android/src/androidMain/kotlin/MainActivity.kt`
- **Reason:** The file follows the Kotlin Multiplatform directory structure.

### 5. Running Commands Table Addition (Line 66)
- **Added:** wasmJs application with its main source path and gradle command
- **Command:** `./gradlew :app:wasmJs:wasmJsBrowserRun`
- **Reason:** The wasmJs application exists but was not documented in the running commands.

## Verification

All file paths have been verified to exist in the actual repository structure:
- ✅ `app/common/src/androidMain/kotlin/vm/ChatViewModelProvider.kt`
- ✅ `server/rest/src/main/kotlin/Rest.kt`
- ✅ `app/desktop/src/main/kotlin/main.kt`
- ✅ `app/android/src/androidMain/kotlin/MainActivity.kt`
- ✅ `app/wasmJs/src/wasmJsMain/kotlin/io.ktor.chat/main.kt`

All module directories have been verified:
- ✅ `server/rest` (exists)
- ✅ `server/admin` (exists)
- ✅ `server/common` (exists)
- ✅ `app/android` (exists)
- ✅ `app/desktop` (exists)
- ✅ `app/wasmJs` (exists)
- ❌ `server/htmx` (does not exist - removed from documentation)

## Impact

These changes ensure that:
1. Developers can successfully navigate to the referenced files
2. The module structure diagram accurately represents the codebase
3. All gradle commands will work as documented
4. The terminology correctly reflects the build system in use