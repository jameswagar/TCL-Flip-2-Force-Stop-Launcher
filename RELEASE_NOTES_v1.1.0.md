# Force Stop v1.1.0

## Changes

- Combines recent-task history with currently running app processes, so safe background apps such as Beeper, Telegram, and Lime can be stopped even when absent from Android's Recents list.
- Hides packages that Android reports as genuinely force-stopped; stale recent-task entries no longer reappear in Force Stop.
- Adds conservative filtering for launchable apps and explicitly protects launcher, telephony, messaging infrastructure, keyboard, voice input, Magisk, LSPosed, Google/microG push infrastructure, and related TCL helper modules.
- Retains a small allowlist of vetted, nonessential system utilities such as Recorder.
- Changes the empty state to **No active apps** and clarifies that **Stop All** affects all listed safe apps.

## Verified on the target phone

Tested on the rooted TCL Flip 2 (`Gflip6_USCC`, Android 11):

- **Stop All** force-stopped Telegram, Beeper, Healthy Battery Charging, and Lime.
- Android reported `stopped=true` for each package, with no remaining process.
- Reopening Force Stop showed **No active apps** rather than rebuilding stale entries.
- Dumb Launcher, Dumb TT9, Google Play services/microG, and Voice-to-Text remained running and protected.
