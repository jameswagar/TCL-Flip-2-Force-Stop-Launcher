# Force Stop v1.0.0

This is the first consolidated stable release of Force Stop for the rooted TCL Flip 2.

## Highlights

- Lists genuinely active, safe-to-stop, user-launchable apps instead of stale Android Recent Apps history.
- Uses a conservative safety policy that excludes phone/system infrastructure, accessibility and input services, root/LSPosed components, push infrastructure, Dumb Launcher, and Force Stop itself.
- Stops individual apps or all listed safe apps with Android's rooted `am force-stop` behavior.
- Hides confirmed-stopped packages until Android genuinely launches them again.
- Displays evidence-backed launch-reason suffixes such as `[U]`, `[P]`, `[A]`, `[J]`, `[B]`, `[S]`, `[C]`, `[I]`, `[R]`, `[T]`, and `[?]`.
- Preserves the latest meaningful launch category across Android log rollover and clears it after a successful stop.
- Uses the title-cased **Force Stop** heading.
- Matches the TCL keypad interface with launcher wallpaper, 20sp/18sp typography, 36dp icons, dark app/reason badges, reverse selected-row contrast, and a black-gradient native bottom bar.
- Native bottom controls provide **Stop**, **Open**, and **Stop All**.

## Verification

Built and signed with the established Force Stop signing identity. Parser, formatter, resolver, recents, and safety-policy JVM tests pass. Verified on the rooted TCL Flip 2 (`4058L`, Android 11) for live package membership, protected-package exclusions, stopping behavior, relaunch visibility, launch-reason display, title placement, selection contrast, and native bottom controls.

The visible release version is `1.0.0`. Android version code remains `6` so this consolidated build upgrades all development builds already installed on the phone.
