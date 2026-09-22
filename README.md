# Next Step

A RuneLite plugin that answers "what should I do next?", guides you there, and celebrates your progress.

![icon](icon.png)

## Features

### Next tab
- **Ready now / Almost there / Long-term** lists built from your real levels, quests and quest points
- **Account rank** from Fresh Spawn to Maxed Mind, with progress to the next rank
- **Pinned goal** with an on-screen tracker (right-click anything -> Pin as goal)

### Skills tab: a training guide for every skill
- 120+ methods across all skills, from level 1 to 99
- Each method shows its level, tags (Fast XP, AFK, Profit, Cheap, Costs GP),
  requirements you're missing, and where to go
- **Best for you right now**: the best Fast XP, AFK and Profit method you can do today
- "Only show what I can do" filter

### Minigames tab
- 27 minigames with requirements, rewards and locations, sorted by what you can play

### Guides you there
- Right-click -> **Show me where to go**: world map marker, live compass directions
  ("Head north-west, ~340 tiles"), the hint arrow when close, and an arrival message
- Works with the **Shortest Path** plugin (if installed) to draw a walking route

### Celebrations
- Animated popup with confetti when you finish a quest, unlock a method or minigame,
  reach your goal or rank up
- Optional chat messages, "1 level away" nudges and desktop notifications
- Press **Test** in the panel to preview

Everything is configurable in the plugin settings.

## How it follows RuneLite's plugin guidelines
- Standard structure: `NextStepPlugin` (extends `Plugin`, `@PluginDescriptor`),
  `NextStepConfig` (extends `Config`), overlays (`Overlay` / `OverlayPanel`) and a sidebar `PluginPanel`
- All `@Subscribe` event handlers live in the plugin class
- Overlays and the navigation button are registered in `startUp()` and removed in `shutDown()`
- Persistent data (the pinned goal) is stored through `ConfigManager`
- Client access happens on the client thread; the Swing panel only receives immutable snapshots
- No network access, no reflection, no automation: the plugin only reads your own
  account state and displays information
- Icon loaded as a resource with `ImageUtil.loadImageResource`

## Run it locally
1. Clone https://github.com/runelite/example-plugin (this gives you the Gradle wrapper).
2. Replace its `src/`, `build.gradle`, `settings.gradle` and `runelite-plugin.properties`
   with these files, and copy `icon.png` to the repo root.
3. Open in IntelliJ and run `NextStepPluginTest` (add `-ea` to VM options).

## Editing the guides
- Quests and PvM: `SuggestionRepository.java`
- Training methods and minigames: `GuideRepository.java`
- Quests are referenced by in-game name; unknown names are skipped with a log warning.
- Coordinates are approximate. To fix one, stand on the spot with Developer Tools ->
  Location enabled (or use mejrs's world map) and copy the x/y into `.at(...)`.
- Check levels and requirements against the OSRS Wiki before publishing.
- New content (e.g. Sailing) can be added the same way.

## Submit to the Plugin Hub
1. Change `YourName` in `runelite-plugin.properties` and `LICENSE`.
2. Push to a public GitHub repo.
3. Fork https://github.com/runelite/plugin-hub, add `plugins/next-step`:
   ```
   repository=https://github.com/YOURNAME/next-step.git
   commit=<full commit hash>
   ```
4. Open a pull request.
