# v2.3c Champagne Moon — approved design snapshot

The user approved this preview. The renderer has been copied into the official Aqua patch and verified in the v2.3c APKs. This directory remains a review snapshot, not a direct Android build input. See `docs/aqua-galaxy-v2.3c.md` for the final build/test and APK verification.

- Planet body diameter 48 px, centre (413,64) on the existing 480×270 texture.
- Champagne light, smoky-violet dark face, restrained warm rim/halo; all static and baked once.
- Twelve stars copied unchanged from v2.3b. No new View, animation, shader loop or background task.
- The official `CosmicDecor` produces exactly the approved pixel hash; this Java snapshot is retained unchanged.
- Keep v2.3b intact for comparison. No animation or navigation changes accompany promotion.

See `docs/aqua-v2.3c-planet-review.md` for the comparison image and reproduction commands.
