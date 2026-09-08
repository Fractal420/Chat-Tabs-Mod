# Tibia-Style Chat Tabs

Client-side Fabric mod for Minecraft Java 1.21.11.

## Architecture

The implementation deliberately uses Fabric's message events rather than replacing `ChatHud`:
- `ClientReceiveMessageEvents.CHAT` captures player chat with the original `Text`, signed message, sender profile, message type parameters, and reception timestamp.
- `ClientReceiveMessageEvents.GAME` captures server/system messages without touching vanilla display.
- `ClientSendMessageEvents.COMMAND` observes outgoing commands so `/w Alice Hello` creates Alice's conversation immediately.
- A single minimal `ChatHud#setScreen` mixin swaps only the vanilla chat screen for the tabbed screen. Vanilla `ChatHud` history/rendering remains intact, so Better Chat and ChatPatches can continue to process the underlying chat stream.

Fabric documents these receive/send events as listener APIs; they do not require canceling or replacing the vanilla message pipeline.

## Private-message detection

There is no universal server-side whisper format. The classifier uses sender profiles plus several common whisper layouts and supports extra regexes in `config/tibia_chat_tabs.json`.

Configured regexes use:
- capture group 1 = player name
- capture group 2 = message body

For servers with an unusual format, add a regex rather than changing rendering code.

## Configuration

`config/tibia_chat_tabs.json`:

```json
{
  "whisperCommand": "/w",
  "whisperAliases": ["w", "msg", "tell", "whisper"],
  "incomingWhisperRegexes": []
}
```

`whisperCommand` controls what the private-tab input sends. It should normally be `/w`.

## Build

Use JDK 21:

```bash
./gradlew build
```

The jar is written to `build/libs/`.

## Compatibility notes

The mod never cancels `CHAT` or `GAME` receive events and never replaces the vanilla `ChatHud` message list. This is intentional for compatibility with client chat mods.

Meteor's current Better Chat mixin targets `ChatHud` internals, including queue sizes, rendering width, anti-spam bookkeeping, and clear/refresh hooks. This mod leaves those paths alone.

ChatPatches similarly owns vanilla chat enhancements such as history, timestamps, search, name rendering and context/copy features. Because the underlying vanilla chat flow is retained, those features remain available in the normal chat HUD.

### Important limitation

A client cannot reliably infer arbitrary server-specific whisper syntax. The supplied classifier is conservative to avoid turning normal public messages into private conversations. Add the server's format to `incomingWhisperRegexes` when needed.

## Test matrix

Test with:
1. Vanilla Fabric.
2. Meteor Client + Better Chat enabled.
3. ChatPatches enabled.
4. Meteor Client + Better Chat + ChatPatches together.

Verify:
- public/system messages stay in Main;
- incoming whispers create a tab without changing the selected tab;
- selecting a whisper clears its unread count;
- outgoing `/w` creates the conversation before any server response;
- echoed outgoing messages are deduplicated;
- Minecraft chat history and the underlying chat HUD continue to receive messages normally.
