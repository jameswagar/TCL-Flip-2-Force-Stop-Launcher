# Force Stop v1.2.0

## Changes

- Adds bracketed launch-reason suffixes to explain why each package is listed:
  - `[U]` user-visible launch
  - `[P]` push delivery
  - `[A]` alarm
  - `[J]` scheduled job
  - `[B]` other broadcast
  - `[S]` service
  - `[C]` content-provider access
  - `[I]` inter-app activity
  - `[R]` Android/vendor restart
  - `[T]` recent task only
  - `[?]` unknown or expired evidence
- Preserves the latest observed launch category when Android's in-memory logs roll over.
- Changes the heading to **Force stop**.
- Exposes the launcher wallpaper like TCL's **Recent apps** screen.
- Uses Recent apps-style reverse contrast for the selected row and compact translucent-black badges to keep unselected bracketed suffixes readable over the wallpaper.
- Places **Stop**, **Open**, and **Stop All** in the TCL native menu bar at the physical bottom of the screen.
- Retains a visible in-app control fallback for compatible devices without TCL's native `MenuBar` API.

## Verification

Built and signed with the existing Force Stop signing identity. JVM parser, formatter, resolver, recents, and safety-policy tests pass. Verified on the rooted TCL Flip 2 (`4058L`, Android 11) with live `[P]`, `[U]`, and `[?]` classification, wallpaper exposure, reverse selection contrast, and native bottom controls.
