# Cine-Ai — CineAI Studio

AI-powered cinematic camera app (Kotlin · Jetpack Compose · CameraX · Room · Gemini API)
that applies Hollywood LUT color grades, "AI unblur"/denoise, a Gemini "AI Director"
auto-grade, and a 24fps shorts converter.

## Repository layout

| Path | Purpose |
|------|---------|
| `cineai-app/` | Full, **reviewed & fixed** Android project source (open this in Android Studio) |
| `cineai.zip` | Repacked downloadable archive of the fixed source |
| `CODE_REVIEW.md` | Full audit: every bug found, severity, and what was fixed |
| `REPAIR_PROMPT.md` | Copy-paste prompt to reproduce/continue the repairs in any AI agent |

## What was fixed (summary)

- Broken tests (missing `Greeting` composable, wrong app-name assertion)
- Gemini API key leaking into logcat (release builds)
- AI Auto-Grade ignoring 6 of 8 recommended grading values
- Preview vs. exported image using different color math (now one shared matrix)
- Denoise slider doing nothing (now a real pipeline stage)
- "Convert to 24fps Short" being a fake `delay(1200)` (now respects its toggles)
- Transparent 1-px border after sharpening
- Camera photos displayed rotated (EXIF orientation now honored)
- Full-resolution decodes on the main thread (now downsampled on background dispatchers)
- Camera flash overlay never clearing, tablet snackbars missing, duplicate composable

See `CODE_REVIEW.md` for the complete list and remaining recommendations.

## Run locally

1. Open `cineai-app/` in Android Studio.
2. Create `.env` in `cineai-app/` and set `GEMINI_API_KEY=<your key>` (see `.env.example`).
3. Run on an emulator/device.
