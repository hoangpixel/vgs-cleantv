# Aqua v2.3b — visual review candidate

`CosmicDecor.java` is an isolated candidate, not an input to `launcher-aqua-lite.patch` or the current APKs.

- Planet radius 10 → 18 pixels (1.8× diameter), centre (413,56), halo 7 pixels.
- Twelve static star points; two are slightly brighter. No animation or extra texture.
- Compile explicitly with the existing AquaSurface and generated CosmicDecorOriginal to render A/B.
- Do not promote or change APK metadata until the visual review is complete. If promoted, update the old <1% decoration test budget to the measured larger-planet region and rerun regression/build verification.

Preview and reproduction commands: `docs/aqua-v2.3b-visual-review.md`.
