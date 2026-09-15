# Chat Tabs

A client-side Fabric mod for Minecraft that adds **Tibia-style chat tabs** to the vanilla chat screen, so private messages get their own tab instead of scrolling by in the main chat feed.

Built with **6b6t** (an anarchy server) in mind, where whisper spam can bury conversations, but the whisper detection is regex-based and fully configurable, so it can be adapted to other servers' chat formats.


https://github.com/user-attachments/assets/cd6b56dd-60c8-44ec-9002-a004ff0477c6

<img width="2448" height="1080" alt="1002465899" src="https://github.com/user-attachments/assets/34c914c4-cae8-4275-a669-42efbb5d9b68" />
<img width="2448" height="1080" alt="1002465910" src="https://github.com/user-attachments/assets/2dd60fca-6054-4752-837d-ed5dc27acc09" />
<img width="2448" height="1080" alt="1002465911" src="https://github.com/user-attachments/assets/f57e2564-4216-492c-8b67-c408e89559a9" />
<img width="2448" height="1080" alt="1002465912" src="https://github.com/user-attachments/assets/ce19a11c-d8a0-4e87-add3-283767fc7b6e" />


## Why

Old-school MMORPGs like **Tibia** kept private messages in separate tabs next to the main channel. This mod brings that idea to Minecraft: every player you whisper with (or who whispers you) gets their own tab at the bottom of the chat screen, with an unread counter, so you never lose a DM in the noise of public chat.

## Features

### Core chat tabs
- **Automatic private-message tabs** — incoming and outgoing whispers are detected from chat text and grouped into a per-player conversation tab.
- **Main tab** — all messages (public, system, and a copy of every whisper) still flow into a "Main" tab, so nothing is ever hidden, just organized.
- **Unread indicators** — each tab shows an unread count; unread tab labels are highlighted. A small badge (✉) is drawn on the HUD outside of the chat screen showing your total unread whispers.
- **Scrollable tab bar** — tabs overflow into a horizontally scrollable strip (mouse wheel over the bar) with `‹` / `›` indicators when there's more than fits on screen. The selected tab is auto-scrolled into view.
- **Click to switch, click × to close** — hover a tab to reveal its close button; closing the active tab falls back to the next open one (or Main).
- **Drag to reorder** — click and drag a conversation tab to rearrange the order of your tabs.
- **Reply from a tab** — while a whisper tab is open, pressing Enter in the chat box automatically sends your message as a whisper to that tab's player, using your configured whisper command. You don't need to type `/w <name>` yourself.
- **Slash commands still work** — if you type a command starting with `/` while a private tab is selected, it is sent as a normal command (not wrapped in a whisper), so things like `/tpa` keep working.
- **Outgoing/incoming echo de-duplication** — a fingerprinting system matches your sent whispers against the server's own echo/confirmation line so messages aren't duplicated in the tab.

### Appearance & HUD
- **Color themes** — cycle through 24 built-in themes (Ocean, Midnight, Slate, Forest, Ember, Crimson, Purple, Neon, Abyss, High Contrast, and more) from the settings screen.
- **Custom transparency** — independent opacity for the bar and tabs.
- **Scalable tab bar** — resize the whole tab strip (and its text) with a scale slider.
- **Reposition & resize HUD** — open the HUD editor to drag the tab bar, the unread notification badge, and the settings button. Drag the blue edge of the tab bar to change its width.
- **Settings button on the tab bar** — a ⚙ button sits on the right of the tab strip for quick access to options while chat is open.

### Configuration
- **In-game settings GUI** — open via the ⚙ button on the tab bar, Mod Menu, or a configurable keybind ("Open Chat Tabs Settings").
- **In-game PM detection editor** — change the whisper command, aliases, and whisper formats and detection options from the settings screens.
- **Config file** — everything is stored in `config/tibia_chat_tabs.json` and is created automatically on first run.

## Requirements

- [Fabric API](https://modrinth.com/mod/fabric-api)
- [Mod Menu](https://modrinth.com/mod/modmenu) (optional, for the config entry in the mods list)

## Downloads

- CurseForge: https://www.curseforge.com/minecraft/mc-mods/chat-tabs
- You can also grab the jar from the [Releases](https://github.com/Fractal420/Chat-Tabs-Mod/releases) page.

## Supported Minecraft versions

| Minecraft | Notes  |
|-----------|--------|
| 1.21.11   | Fabric |
| 26.1      | Fabric |
| 26.2      | Fabric |

## How to use

1. Install Fabric Loader, Fabric API, and this mod.
2. Join a server (or singleplayer) and open chat (`T` by default).
3. Whisper someone or wait for someone to whisper you — a tab for that player appears on the tab bar.
4. Click a tab to switch; the chat log is filtered to that conversation and its unread count is cleared.
5. Type a message and press Enter while a whisper tab is selected to reply as a whisper automatically.
6. Use the ⚙ button on the tab bar (or Mod Menu / keybind) to open settings: themes, sizes, HUD layout, and Message Detection settings.

Because the tab bar is rendered on top of the vanilla `ChatScreen`, tabs are only visible while the chat window is open. The unread-count badge on the main HUD lets you know a new whisper arrived even while the chat screen is closed.

## Whisper detection

Messages are classified with simple user-friendly settings (no regex knowledge needed). Defaults work with 6b6t including timestamps and heads.

You can toggle and customize:

- Timestamps (ON/OFF) and common formats: `[HH:MM:SS]`, `[HH:MM]`, `<HH:MM:SS>`, etc.
- Heads / prefixes (ON/OFF) and styles: `[anything]`, `[PLAYER head]`, `[PLAYER]`, etc.
- Player name matching pattern
- Whisper message formats using the placeholders `{player}` and `{message}`

Default formats:

- `{player} whispers: {message}`
- `{player} whispers to you: {message}`
- `[{player} -> You]: {message}`
- `{player} -> you: {message}`
- `[PM] {player}: {message}` (also MSG / WHISPER)

Outgoing echo is also detected automatically. Open **Message Detection** in the settings to customize for any server.

## Building from source

```bash
./gradlew buildAll
```

Or build a single target:

```bash
./gradlew :1.21.11:build
./gradlew :26.1:build
./gradlew :26.2:build
```

Alternatively:

```bash
./gradlew build -Pmc=1.21.11
./gradlew build -Pmc=26.1
./gradlew build -Pmc=26.2
```

The built jar (and sources jar) will be in `versions/<version>/build/libs/`. With `-Pmc=...`, copies are also placed under the root `build/libs/`.

## Limitations

- Client-side only — it reorganizes what you see locally and doesn't change what the server sends.
- Whisper detection depends on matching the server's chat phrasing; servers using different wording can be adapted via the in-game PM Detection Rules screen.

## License

MIT — see [LICENSE](LICENSE).
