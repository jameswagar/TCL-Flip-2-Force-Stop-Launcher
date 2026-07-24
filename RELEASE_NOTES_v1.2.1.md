# Force Stop v1.2.1

## Changes

- Adds a translucent-black rounded badge behind each unselected app name so labels remain readable wherever the launcher wallpaper's colored shapes appear.
- Keeps the launch-reason suffix in its own darker badge with a small visual gap between the app-name and reason badges.
- Preserves the Recent apps-style selected row: white background with black app-name and suffix text.
- Retains the launcher wallpaper, **Force stop** heading, launch-reason classifications, and native bottom controls from v1.2.0.

## Verification

Built and signed with the existing Force Stop signing identity. JVM parser, formatter, resolver, recents, and safety-policy tests pass. Verified on the rooted TCL Flip 2 (`4058L`, Android 11) for selected and unselected contrast, wallpaper visibility, title placement, and native bottom controls.
