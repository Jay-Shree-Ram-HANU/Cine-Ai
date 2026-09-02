# CineAI Studio — Full Code Review & Fix Report

**App:** CineAI Studio (AI cinematic camera, color-grader, unblur & shorts converter)
**Stack:** Kotlin · Jetpack Compose (Material 3) · CameraX · Room · Retrofit/Moshi/OkHttp · Gemini API
**Reviewed:** all 110 files in `cineai.zip` (extracted and fixed under `cineai-app/`)

> The Google Drive link did not resolve, so I worked from `cineai.zip` in the repo (as you suggested).

---

## 1. Severity legend

| Level | Meaning |
|-------|---------|
| 🔴 **Critical** | Breaks the build / crashes / security leak / data corruption |
| 🟠 **High** | Feature silently doesn't work or produces wrong output |
| 🟡 **Medium** | Visible bug, jank, misleading UX, or inconsistent behaviour |
| 🔵 **Low** | Dead code, smell, future risk |

---

## 2. Issues found (with fixes applied)

### 🔴 C1 — Test code references a composable that doesn't exist
`app/src/test/java/com/example/GreetingScreenshotTest.kt` calls `Greeting("Robolectric")`, but no `Greeting`
composable exists anywhere in the app (it was a leftover AI-Studio template). **This fails the `test` compilation.**
✅ *Fixed:* replaced with a real `Text("CineAI Studio")` inside the app theme.

### 🔴 C2 — Robolectric test asserts the wrong app name
`ExampleRobolectricTest.kt` asserted `"My Application"`, but `strings.xml` defines `CineAI Studio`, so the test
always failed. ✅ *Fixed:* assertion now matches `R.string.app_name`.

### 🔴 C3 — Gemini API key leaks into logcat in release builds
`GeminiDirectorClient` added `HttpLoggingInterceptor(Level.BASIC)` unconditionally. Because the API key is sent as a
URL query parameter, BASIC logging writes the **full request URL including the key** to logcat.
✅ *Fixed:* logging interceptor is now only added when `BuildConfig.DEBUG` is true.

### 🟠 H1 — AI Auto-Grade ignored 6 of the 8 values the model returned
`parseDirectorGuidance()` parsed only `unblur` and `contrast`; exposure, saturation, warmth, tint, vignette and grain
were hard-coded regardless of what Gemini actually recommended. The "AI Director" feature was effectively fake for
most parameters.
✅ *Fixed:* the prompt now demands a strict labeled format and a robust regex parser extracts **all** values
(plus a new `Denoise` value), with per-field clamping.

### 🟠 H2 — Live preview and exported image used different color math
`buildComposeCinematicColorMatrix` (preview) divided by `/35f` and used one set of LUT matrices, while
`BitmapProcessor.buildCinematicColorMatrix` (export) divided by `/50f` with a different LUT set. The same slider
settings produced **different images** on screen vs. exported.
✅ *Fixed:* new `data/processing/CinematicColorMatrix.kt` is now the single source of truth consumed by both paths.

### 🟠 H3 — "AI Smooth Denoise" slider did nothing
The editor's denoise slider updated `denoiseStrength`, but that value was never passed into the render pipeline,
so it had zero effect on any output.
✅ *Fixed:* a real 3×3 neighborhood-blend denoise pass was added to `processCinematicBitmap`, and `denoiseStrength`
is now threaded through export and AI auto-grade.

### 🟠 H4 — "Convert to 24fps Short" was fake
`convertShortToCinematic()` just slept 1.2s and flipped a few DB flags — it ignored the on-screen toggles
(24fps cadence / anamorphic scope / halation / motion blur) entirely.
✅ *Fixed:* removed the artificial delay; the function now honours all four toggles and writes the correct
fps/letterbox/grain/vignette/unblur values (the toggles are passed from `ShortsScreen`).

### 🟠 H5 — Sharpening produced a 1-px transparent border
`applySharpenKernel` allocated an output array of zeros and never wrote the image border pixels, so the rendered
image got a **transparent/black 1-px frame** around it.
✅ *Fixed:* output is initialized from a copy of the source pixels, so borders keep their original values.

### 🟠 H6 — Large photos decoded at full resolution on the main thread
`exportAndShareMedia`, `runAiDirectorAutoGrade`, and `BeforeAfterSlider` all called `BitmapFactory.decodeFile` on the
main thread at full resolution → ANR/jank and OutOfMemory risk on 12MP+ photos.
✅ *Fixed:* added `BitmapProcessor.decodeSampledBitmap()`; all three call sites now decode off the main thread with
downsampling.

### 🟠 H7 — Imported camera photos appeared rotated
`loadBitmapFromUri()` ignored EXIF orientation, so phone camera shots (which store rotation in metadata) displayed
sideways/upside-down.
✅ *Fixed:* EXIF orientation is read and applied (with stream-safe `use {}` handling).

### 🟡 M1 — Camera flash overlay never cleared
`CameraScreen` set `showFlashEffect = true` on capture but never reset it, so after the first photo the viewfinder
stayed covered by the white flash layer. ✅ *Fixed:* auto-hide after ~220ms via `LaunchedEffect`.

### 🟡 M2 — Snackbars invisible on tablets
The wide-screen (tablet) layout in `CineAppShell` had no `SnackbarHost`, so all feedback messages were silently
dropped there. ✅ *Fixed:* wide layout is now wrapped in a `Scaffold` with the snackbar host.

### 🟡 M3 — Duplicate `CameraGridOverlay` composable
Two conflicting `CameraGridOverlay` definitions existed in the same package
(`CameraHUDComponents.kt` and `CinematicCanvasEngine.kt`). It compiled (overload resolution picked one), but it was
ambiguous, confusing, and made the richer grid-type variant dead code.
✅ *Fixed:* removed the duplicate from `CinematicCanvasEngine.kt`.

### 🟡 M4 — Misleading "AI Director (Gemini)" labels on offline results
The rule-based fallback (no API key / no network) was labelled "AI Director (Gemini)". 
✅ *Fixed:* offline results are now labelled `AI Director (Offline)`; Gemini results keep the Gemini label.

### 🔵 L1 — Grain was one-sided
Film grain drew only white specks. ✅ *Fixed:* light + dark specks alternate for a more realistic emulsion look.

### 🔵 L2 — Unused imports in `CinematicCanvasEngine.kt`
`BlendMode`, `Compare`, `MaterialTheme` were imported but unused. ✅ *Removed.*

---

## 3. Issues documented but NOT changed (recommended next iteration)

| # | Area | Issue | Suggested fix |
|---|------|-------|---------------|
| R1 | Auth/Profile | `AuthRepository` is in-memory only — profile and the **custom Gemini API key are lost on app restart**, even though the `datastore-preferences` dependency is already declared. | Persist `UserProfile`/API key with DataStore (or Room) and load on startup. |
| R2 | Shorts | "24fps conversion" is still metadata-only — there is **no real video transcoding/motion-blur**. | Add Media3 `Transformer`/`Effect` pipeline (or clearly label it a "style preview"). |
| R3 | CameraX | Flash toggle doesn't rebuild the `ImageCapture` use case (works via capture-time `flashMode`, but the preview flash/torch state can drift). | Include `flashMode`/torch in the bind `LaunchedEffect` keys or drive both through `CameraControl`. |
| R4 | Preview parity | The Compose preview still does not apply **spatial** denoise/unblur (it's color-only); the exported bitmap does. | Apply a shader/sample-based sharpening in the preview, or note the limitation in the UI. |
| R5 | BeforeAfterSlider | `showControls` param is unused; `hold_before`/`hold_after` modes and "tap-to-compare" are dead code. | Implement tap/hold gestures or remove the dead branches. |
| R6 | Dead code | `StudioViewModel.importCapturedDevicePhoto` is never called (CameraX uses `saveCameraXCapturedPhoto`). | Remove or wire up. |
| R7 | DB | `fallbackToDestructiveMigration()` silently wipes data on any schema change. | Add explicit migrations. |
| R8 | DI/Lifecycle | `AppContainer` seeds data with an unscoped `CoroutineScope(Dispatchers.IO)` (not tied to app lifecycle). | Use an app-scoped `SupervisorJob` + `applicationScope`. |
| R9 | Build config | Release signing points at a missing `my-upload-key.jks`; README asks users to hand-edit `build.gradle.kts`. | Provide a proper `keystore.properties` flow and document it. |
| R10 | Model | `gemini-3.5-flash` is a valid current model, but **verify it is enabled for your API key/quota** (region & billing). | Keep the model in one `const` (done) and surface a clear error when `ListModels` doesn't include it. |
| R11 | Toolchain | AGP `9.1.1`, Kotlin `2.2.10`, KSP `2.3.5`, Room `2.7.0`, Compose BOM `2024.09.00`. | Let Android Studio's import resolve version compatibility (some are very new). |

---

## 4. Files changed (this pass)

| File | Change |
|------|--------|
| `data/processing/CinematicColorMatrix.kt` | **New** — single color-grade matrix source of truth |
| `data/processing/BitmapProcessor.kt` | EXIF rotation, safe streams, downsampled decode, denoise, sharpen border fix, shared matrix, 2-tone grain |
| `data/ai/GeminiDirectorService.kt` | Robust full-value parsing, stricter prompt, model constant, secure logging, honest engine labels |
| `ui/viewmodel/StudioViewModel.kt` | Background decode, denoise wiring, real shorts conversion (toggle-aware), no fake delay |
| `ui/components/CinematicCanvasEngine.kt` | Removed duplicate `CameraGridOverlay`, unified preview matrix, off-thread downsampled image load |
| `ui/components/CameraScreen.kt` | Flash overlay auto-reset |
| `ui/screens/shorts/ShortsScreen.kt` | Passes conversion toggles to the ViewModel |
| `ui/navigation/CineAppShell.kt` | Snackbar host on wide/tablet layout |
| `src/test/…/GreetingScreenshotTest.kt` | Removed reference to missing `Greeting` |
| `src/test/…/ExampleRobolectricTest.kt` | Corrected app-name assertion |

Full fixed source tree: **`cineai-app/`** · Repacked downloadable archive: **`cineai.zip`** (original preserved in git history).

---

## 5. How to verify

1. Open `cineai-app/` in Android Studio; let it sync.
2. Run the unit/Robolectric tests — they now compile and pass.
3. Import a **camera-taken** photo → confirm it is upright (EXIF fix).
4. Drag the Before/After slider in the editor, then **Export** → confirm the exported image matches the preview (shared matrix fix).
5. Move the **Denoise** slider and export → confirm smoothing is visible (denoise fix).
6. Take a photo → confirm the white flash effect clears (flash fix).
7. On a tablet/landscape → confirm snackbars appear (shell fix).
8. Profile → set a custom Gemini API key → "Auto-Grade" → confirm all sliders move to the model's values (full-parse fix) and logcat shows no key in release.
