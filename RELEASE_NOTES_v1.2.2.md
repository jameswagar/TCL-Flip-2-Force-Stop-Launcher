# Force Stop v1.2.2

## Changes

- Applies TCL Recent apps' own bottom-bar gradient colors (`#22000000` to `#DE000000`) behind **Stop**, **Open**, and **Stop All**, replacing the lighter default bar.
- Aligns the main list typography and spacing with TCL Recent apps:
  - 20sp heading and empty-state text
  - 18sp app labels
  - 36dp icons
  - 4dp vertical row padding
  - native TCL menu-bar font and sizing
- Retains the wallpaper, dark app and launch-reason badges, reverse selected-row contrast, and launch-reason classifications.

## Verification

Built and signed with the existing Force Stop signing identity. JVM parser, formatter, resolver, recents, and safety-policy tests pass. Verified on the rooted TCL Flip 2 (`4058L`, Android 11) against the installed TCL `RecentsListActivity` layout and menu-bar implementation.
