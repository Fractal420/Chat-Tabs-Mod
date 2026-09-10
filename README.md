# Chat Tabs

A client-side Fabric mod for Minecraft that adds **Tibia-style chat tabs** to the vanilla chat screen, so private messages get their own tab instead of scrolling by in the main chat feed.

Built with **6b6t** (an anarchy server) in mind, where whisper spam can bury conversations, but the whisper detection is regex-based and configurable, so it can be adapted to other servers' chat formats.


https://github.com/user-attachments/assets/e0f0f37b-672b-439a-ab1e-5682ec3d714d





## Why

Old-school MMORPGs like **Tibia** kept private messages in separate tabs next to the main channel. This mod brings that idea to Minecraft: every player you whisper with (or who whispers you) gets their own tab at the bottom of the chat screen, with an unread counter, so you never lose a DM in the noise of public chat.

## Features

- **Automatic private-message tabs** — incoming and outgoing whispers are detected from chat text and grouped into a per-player conversation tab.
- **Unread indicators** — each tab shows an unread count, and a small badge (✉) is drawn on the HUD outside of the chat screen showing your total unread whispers.
- **Scrollable tab bar** — tabs overflow into a horizontally scrollable strip (mouse wheel or shift-scroll) with `‹` / `›` indicators when there's more than fits on screen.
- **Click to switch, click × to close** — hover a tab to reveal its close button; closing the active tab falls back to the next open one (or Main).
- **Reply from a tab** — while a whisper tab is open, pressing Enter in the chat box automatically sends your message as a whisper to that tab's player, using your configured whisper command.
- **Main tab** — all messages (public, system, and a copy of every whisper) still flow into a "Main" tab, so nothing is ever hidden, just organized.
- **Outgoing/incoming echo de-duplication** — a fingerprinting system matches your sent whispers against the server's own echo/confirmation line so messages aren't duplicated in the tab.
- **Configurable whisper detection** — the command used to whisper, its aliases, and the regex patterns used to recognize incoming whispers are all stored in a config file and can be edited to match other servers.

## Requirements

- [Fabric API](https://modrinth.com/mod/fabric-api)
- [Mod Menu](https://modrinth.com/mod/modmenu)

## Downloads

- Curseforge: https://www.curseforge.com/minecraft/mc-mods/chat-tabs
- You can also download it from github releases https://github.com/Fractal420/Chat-Tabs-Mod/releases

## Installation

1. Install Fabric Loader for Minecraft.
2. Download and place [Fabric API](https://modrinth.com/mod/fabric-api) and the `chat-tabs-*.jar` from this repo's releases (or your own build) into your `mods` folder.
3. Launch the game. This is a **client-side only** mod — no server-side installation is needed, and it can be used on servers you don't control (like 6b6t).

## How it works

Open chat as usual (default `T` key). The tab bar appears as a strip along the bottom of the chat screen:

- The leftmost tab, **Main**, always shows every message.
- A new tab appears automatically the first time you whisper someone or someone whispers you.
- Switching tabs filters the chat log to just that conversation and clears its unread count.
- Typing and pressing Enter while a whisper tab is selected sends your text as a whisper to that player — you don't need to type `/w <name>` yourself.

Because the tab bar is rendered on top of the vanilla `ChatScreen`, tabs are only visible while the chat window is open; the unread-count badge on the main HUD lets you know a new whisper arrived even while the chat screen is closed.

## Whisper detection

Messages are classified using pattern matching against the raw chat line, tuned for 6b6t's chat format by default. Recognized incoming formats include:

- `PlayerName whispers: message`
- `[PlayerName -> You]: message`
- `[PM] PlayerName: message`

Your own outgoing whispers are recognized from the server's echo (e.g. `You whisper to PlayerName: message`) so they land in the correct tab instead of Main only.

## Configuration

Settings are stored in `config/tibia_chat_tabs.json` and are created automatically on first run:

```json
{
  "whisperCommand": "/w",
  "whisperAliases": ["w", "msg", "tell", "whisper"],
  "incomingWhisperRegexes": [
    "^(.+?)\\s+whispers:\\s*:?(.*)$"
  ]
}
```

| Field | Description |
|---|---|
| `whisperCommand` | The command sent when you reply from a tab (e.g. `/w`, `/msg`, `/tell`). |
| `whisperAliases` | Additional command names that should also be treated as whisper commands when you type them yourself, so outgoing messages are captured into the right tab. |
| `incomingWhisperRegexes` | Extra regular expressions (in addition to the built-in defaults) used to detect an incoming whisper line and extract the sender's name and message body. Each pattern needs two capture groups: the sender name and the message. |

Edit this file (while the game is closed, or restart afterward) to adapt the mod to a different server's chat format.

## Building from source

```bash
./gradlew build
```

The built jar (and sources jar) will be in `build/libs/`.

## Project structure

```
src/main/java/com/example/tibiachat/
├── TibiaChatTabsClient.java     # Mod entrypoint, event registration, HUD unread badge
├── chat/
│   ├── ChatManager.java         # Core message routing, echo de-duplication, outgoing whispers
│   ├── ConversationManager.java # Tracks per-player Conversation objects
│   ├── Conversation.java        # A single tab's messages, unread count, scroll state
│   ├── ChatMessage.java         # A stored, timestamped chat line
│   ├── MessageClassifier.java   # Regex-based classification of chat lines
│   ├── Classification.java      # Result of classifying a message
│   └── MessageType.java         # PUBLIC / WHISPER_INCOMING / WHISPER_OUTGOING / SYSTEM / UNKNOWN
├── config/
│   └── TibiaChatConfig.java     # Loads/saves config/tibia_chat_tabs.json
└── mixin/
    └── ChatScreenTabBarMixin.java  # Renders the tab bar and handles clicks/scroll/Enter on ChatScreen
```

## Limitations

- Client-side only — it reorganizes what you see locally and doesn't change what the server sends.
- Whisper detection depends on matching the server's exact chat phrasing; servers using different wording will need custom `incomingWhisperRegexes`.
- Tabs and unread counts are not persisted between game sessions.

## License

MIT — see [LICENSE](LICENSE).
