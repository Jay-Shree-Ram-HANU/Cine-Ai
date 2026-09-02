# CineAI Studio — Repair & Optimization Prompt

> Paste everything below (from **BEGIN** to **END**) into your AI coding agent
> (Google AI Studio, Gemini CLI, Copilot, etc.). It is self-contained and assumes the
> agent can read/edit the `cineai-app/` Kotlin source tree in this repository.

---

**BEGIN PROMPT**

You are a senior Android engineer reviewing and repairing **CineAI Studio**, a Kotlin +
Jetpack Compose cinematic-camera app (CameraX, Room, Retrofit/Moshi/OkHttp, Gemini API).
The project source is in the `cineai-app/` directory. Do **not** change the Gradle
toolchain versions unless a file explicitly says to. Follow these instructions exactly.

## Context
The app claims to: capture/import photos, apply cinematic LUT color grades, "AI unblur"
and denoise, run a Gemini "AI Director" auto-grade, convert videos to "24fps cinematic
shorts", and export/share the result. Several functions are currently broken or fake.
Fix them.

## Required repairs (apply ALL of these)

### 1. Test suite must compile and pass
- `cineai-app/app/src/test/java/com/example/GreetingScreenshotTest.kt` references a
  non-existent `Greeting("Robolectric")`. Replace it with a real composable
  (e.g. `Text("CineAI Studio")` inside the app theme).
- `cineai-app/app/src/test/java/com/example/ExampleRobolectricTest.kt` asserts
  `"My Application"`; the real value from `res/values/strings.xml` is `"CineAI Studio"`.
  Fix the assertion.

### 2. Gemini "AI Director" must actually use ALL model-recommended values
File: `cineai-app/app/src/main/java/com/example/data/ai/GeminiDirectorService.kt`
- The prompt to the model must demand a strict, one-value-per-line labeled format
  (LUT, Unblur, Denoise, Contrast, Exposure, Saturation, Warmth, Tint, Vignette, Grain, Notes).
- `parseDirectorGuidance()` must parse **every** value robustly (tolerate decimals,
  negative signs, colons/equals, markdown bullets), clamp each to its documented range,
  and extract a clean Notes string.
- Label results truthfully: live Gemini results say "AI Director (Gemini): …";
  the offline rule-based fallback says "AI Director (Offline): …".
- Do **not** log the API key: the OkHttp `HttpLoggingInterceptor` (which logs the request
  URL, where the key travels as a query param) must only be installed in `BuildConfig.DEBUG`.
- Keep the model name in a single top-level `const` so it can be changed in one place.

### 3. The live preview and the exported image must be identical
File: `cineai-app/app/src/main/java/com/example/ui/components/CinematicCanvasEngine.kt`
and `cineai-app/app/src/main/java/com/example/data/processing/BitmapProcessor.kt`
- These two files currently compute color-grade matrices with **different** formulas
  (`/35f` vs `/50f`, different LUT tables). Extract ONE shared function
  (suggest `data/processing/CinematicColorMatrix.kt`) returning a 20-float matrix and
  use it from both the Compose preview and the bitmap exporter.

### 4. Denoise must be real, and must be wired end-to-end
- `processCinematicBitmap(...)` must accept a `denoiseStrength` parameter and apply a real
  spatial smoothing pass (a simple 3×3 neighborhood blend mixed by strength is fine).
- `StudioViewModel.exportAndShareMedia(...)` and `runAiDirectorAutoGrade(...)` must pass
  `item.denoiseStrength` / `recommendation.denoiseStrength` through.
- The `DirectorGradeRecommendation` model must carry `denoiseStrength`.

### 5. "Convert to 24fps Short" must be real, not a `delay(1200)`
File: `cineai-app/app/src/main/java/com/example/ui/viewmodel/StudioViewModel.kt`
- Remove the fake `kotlinx.coroutines.delay(1200)`.
- Change `convertShortToCinematic(...)` to accept the four toggles
  (`enable24Fps`, `enableAnamorphicScope`, `enableHalation`, `enableMotionBlur`) and
  actually set `fps`, `letterboxRatio`, `grain`, `vignette`, `unblurStrength` accordingly.
- Update `ShortsScreen.kt` to pass the toggle state into the call.

### 6. Fix bitmap-processing correctness bugs
File: `cineai-app/app/src/main/java/com/example/data/processing/BitmapProcessor.kt`
- `applySharpenKernel` leaves a transparent/black 1-px border (unwritten array defaults).
  Initialize the output from a copy of the source pixels so borders are preserved.
- `loadBitmapFromUri` must honor EXIF orientation so camera photos aren't sideways, and
  must close streams with `use {}` in all paths.
- Add a `decodeSampledBitmap(file, maxDimension)` helper (bounds + `inSampleSize`) and use
  it everywhere a local file is decoded — never `BitmapFactory.decodeFile` on the main thread.

### 7. Remove main-thread decode & memory spikes
- `exportAndShareMedia`, `runAiDirectorAutoGrade`, and `BeforeAfterSlider` (in
  `CinematicCanvasEngine.kt`) must decode bitmaps on `Dispatchers.IO` (e.g. via
  `withContext`/`produceState`) with downsampling.

### 8. UI bug fixes
- `CameraScreen.kt`: the shutter flash overlay (`showFlashEffect`) is set but never
  cleared — auto-hide it after ~220ms.
- `CineAppShell.kt`: the wide/tablet layout has no `SnackbarHost`, so snackbars are lost
  on tablets — wrap the wide layout in a `Scaffold` that shows the snackbar host.
- `CinematicCanvasEngine.kt`: remove the duplicate `CameraGridOverlay` composable that
  conflicts with the one in `CameraHUDComponents.kt`.

## Acceptance criteria
- `./gradlew test` compiles and passes.
- Importing a camera photo shows it upright.
- Moving the Denoise slider changes the exported image.
- The Before/After preview visually matches the exported JPG for identical settings.
- All 8+ grading sliders change when "AI Auto-Grade" runs with a valid API key.
- No API key appears in logcat for release builds.
- The shorts converter respects its four toggles and does not sleep for a fake 1.2s.

## Do NOT do
- Do not change package names, `applicationId`, or the navigation route structure.
- Do not add new third-party dependencies unless a repair explicitly requires one.
- Do not rewrite working UI that isn't listed above.

Report a short list of every file you changed and why.

**END PROMPT**
