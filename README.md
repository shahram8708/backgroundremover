# Background Remover (Android)

Background Remover is an Android application that allows users to capture or select an image, automatically remove its background using the remove.bg API, apply a new background from the provided presets, and save the edited image to the device gallery.

---

## Overview

The application is structured around a simple flow:

1. Open the camera or gallery to select an image.
2. Automatically send the image to the remove.bg background removal API.
3. Display the processed transparent image in an editor.
4. Allow the user to apply one of the predefined background images.
5. Save the final image to the gallery.

The project is written in Java using Android Jetpack components and standard Android SDK tooling.

---

## Key Features

* Capture photos using the device camera.
* Select images from the device gallery.
* Automatic background removal using remove.bg API.
* Preview and edit the processed image.
* Apply selectable background images via a horizontal background picker.
* Save the final edited image to device storage.
* Clean MVVM-style separation using ViewModel and LiveData.

---

## Tech Stack

* **Language:** Java
* **Minimum SDK:** 21
* **Target SDK:** 33
* **Architecture & Libraries:**

  * AndroidX Components
  * CameraX
  * RecyclerView
  * ViewModel & LiveData
  * OkHttp3
* **External Service:**

  * remove.bg Background Removal API

---

## Project Structure

```
app/
 ├── src/main/
 │   ├── java/com/example/backgroundremover/
 │   │   ├── MainActivity.java
 │   │   ├── fragments/
 │   │   │   ├── CameraFragment.java     # Handles camera and gallery image selection
 │   │   │   ├── EditFragment.java       # Sends image to API, applies backgrounds, saves output
 │   │   ├── adapters/
 │   │   │   └── BackgroundAdapter.java  # RecyclerView adapter for background list
 │   │   └── viewmodel/
 │   │       └── MainViewModel.java      # Stores original and processed images
 │   ├── res/layout/
 │   │   ├── activity_main.xml
 │   │   ├── fragment_camera.xml
 │   │   ├── fragment_edit.xml
 │   │   └── item_background.xml
 │   └── AndroidManifest.xml
 └── build.gradle
```

---

## Permissions

Defined in `AndroidManifest.xml`:

* Camera access
* Internet access
* Read storage
* Write storage

---

## Installation

1. Open the project in **Android Studio**.
2. Ensure Android SDK 33 is installed.
3. Sync Gradle.
4. Build and run on a device or emulator.

---

## Configuration

The application communicates with the remove.bg API inside `EditFragment` using OkHttp.
The API key is currently hard-coded in the request header under `X-Api-Key` in `RemoveBgTask`.
If needed, update the key directly in that file.

---

## Usage

1. Launch the app.
2. Choose to take a photo using the camera or select an image from the gallery.
3. Wait for the background removal process to complete.
4. Select a new background from the horizontal list.
5. Save the edited image using the save button.
6. The app confirms once the image is successfully stored in the gallery.

---

## Notes

* The application relies on an active internet connection to communicate with the remove.bg API.
* If the API request fails, processing returns null, and the image will not be updated.

---

## Author / Credits

This project includes:

* Android application source code
* UI layouts
* remove.bg API integration
* Camera and gallery handling
* Image background editing workflow

If more credits or contributors appear in future commits, they should be listed here.

---

## License

No license file is included in this repository.
