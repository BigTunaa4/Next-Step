package com.nextstep;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup("nextstep")
public interface NextStepConfig extends Config
{
	@ConfigSection(name = "Suggestions", description = "What shows up in the panel", position = 0)
	String suggestionsSection = "suggestions";

	@ConfigSection(name = "Celebrations", description = "Popups and messages when you complete things", position = 1)
	String celebrationsSection = "celebrations";

	@ConfigSection(name = "Goal overlay", description = "On-screen tracker for your pinned goal", position = 2)
	String goalSection = "goal";

	@ConfigSection(name = "Navigation", description = "Showing you where to go", position = 3)
	String navSection = "navigation";

	@ConfigSection(name = "Drops & milestones", description = "Celebrations for drops, bosses and big milestones", position = 4)
	String extraSection = "extras";

	// ---------- Suggestions ----------
	@Range(min = 1, max = 50)
	@ConfigItem(keyName = "almostThreshold", name = "'Almost there' range",
		description = "Show suggestions you're within this many total levels of",
		position = 0, section = suggestionsSection)
	default int almostThreshold()
	{
		return 5;
	}

	@Range(min = 1, max = 30)
	@ConfigItem(keyName = "maxShown", name = "Max per section",
		description = "How many suggestions to show in each section",
		position = 1, section = suggestionsSection)
	default int maxShown()
	{
		return 8;
	}

	@ConfigItem(keyName = "showQuests", name = "Show quests", description = "Include quests",
		position = 2, section = suggestionsSection)
	default boolean showQuests()
	{
		return true;
	}

	@ConfigItem(keyName = "showTraining", name = "Show methods & minigames",
		description = "Include featured training methods and minigames in Next Steps",
		position = 3, section = suggestionsSection)
	default boolean showTraining()
	{
		return true;
	}

	@ConfigItem(keyName = "showPvm", name = "Show PvM", description = "Include bosses, Slayer and combat gear",
		position = 4, section = suggestionsSection)
	default boolean showPvm()
	{
		return true;
	}

	@ConfigItem(keyName = "showRecommended", name = "Show recommended stats",
		description = "Warn when you're below recommended (not required) levels",
		position = 5, section = suggestionsSection)
	default boolean showRecommended()
	{
		return true;
	}

	// ---------- Celebrations ----------
	@ConfigItem(keyName = "popups", name = "Celebration popups",
		description = "Show an animated popup when you complete or unlock something",
		position = 0, section = celebrationsSection)
	default boolean popups()
	{
		return true;
	}

	@ConfigItem(keyName = "confetti", name = "Confetti", description = "Throw confetti with popups",
		position = 1, section = celebrationsSection)
	default boolean confetti()
	{
		return true;
	}

	@Range(min = 2, max = 10)
	@ConfigItem(keyName = "popupSeconds", name = "Popup length (seconds)", description = "How long each popup stays up",
		position = 2, section = celebrationsSection)
	default int popupSeconds()
	{
		return 4;
	}

	@ConfigItem(keyName = "chatMessages", name = "Chat messages", description = "Also post celebrations in the chatbox",
		position = 3, section = celebrationsSection)
	default boolean chatMessages()
	{
		return true;
	}

	@ConfigItem(keyName = "celebrateGuides", name = "Celebrate new methods",
		description = "Celebrate when a level-up unlocks a new training method or minigame",
		position = 4, section = celebrationsSection)
	default boolean celebrateGuides()
	{
		return true;
	}

	@ConfigItem(keyName = "nudges", name = "'1 level away' nudges",
		description = "Chat message when you're one level from an unlock",
		position = 5, section = celebrationsSection)
	default boolean nudges()
	{
		return true;
	}

	@ConfigItem(keyName = "desktopNotify", name = "Desktop notifications",
		description = "Send a RuneLite notification for celebrations",
		position = 6, section = celebrationsSection)
	default boolean desktopNotify()
	{
		return false;
	}

	// ---------- Goal overlay ----------
	@ConfigItem(keyName = "showGoalOverlay", name = "Show goal overlay",
		description = "Show your pinned goal on screen",
		position = 0, section = goalSection)
	default boolean showGoalOverlay()
	{
		return true;
	}

	// ---------- Navigation ----------
	@ConfigItem(keyName = "autoGuidePinned", name = "Guide to pinned goal",
		description = "Automatically show the way to your pinned goal",
		position = 0, section = navSection)
	default boolean autoGuidePinned()
	{
		return true;
	}

	@ConfigItem(keyName = "mapMarker", name = "World map marker",
		description = "Put a marker on the world map (it sticks to the edge when off-screen)",
		position = 1, section = navSection)
	default boolean mapMarker()
	{
		return true;
	}

	@ConfigItem(keyName = "hintArrow", name = "Hint arrow",
		description = "Show the yellow hint arrow when you're close",
		position = 2, section = navSection)
	default boolean hintArrow()
	{
		return true;
	}

	@ConfigItem(keyName = "shortestPath", name = "Use Shortest Path plugin",
		description = "If the Shortest Path plugin is installed, draw a walking route to the destination",
		position = 3, section = navSection)
	default boolean shortestPath()
	{
		return true;
	}

	@ConfigItem(keyName = "showDirections", name = "Directions in overlay",
		description = "Show compass direction and distance in the goal overlay",
		position = 4, section = navSection)
	default boolean showDirections()
	{
		return true;
	}

	@ConfigItem(keyName = "showDistance", name = "Distance on cards",
		description = "Show how far away each suggestion is in the panel",
		position = 5, section = navSection)
	default boolean showDistance()
	{
		return true;
	}

	@ConfigItem(keyName = "sortByDistance", name = "Sort 'Ready now' by distance",
		description = "Put the closest ready suggestions first instead of the most valuable",
		position = 6, section = navSection)
	default boolean sortByDistance()
	{
		return false;
	}

	// ---------- Drops & milestones ----------
	@ConfigItem(keyName = "collectionLog", name = "Collection log",
		description = "Celebrate new collection log slots (turn on the game's collection log chat message). Rare items get bigger celebrations.",
		position = 0, section = extraSection)
	default boolean collectionLog()
	{
		return true;
	}

	@ConfigItem(keyName = "pets", name = "Pet drops", description = "The biggest celebration of all",
		position = 1, section = extraSection)
	default boolean pets()
	{
		return true;
	}

	@ConfigItem(keyName = "luck", name = "Spooned & dry streaks",
		description = "Special popups when a rare drop comes very early or ends a long dry streak",
		position = 2, section = extraSection)
	default boolean luck()
	{
		return true;
	}

	@ConfigItem(keyName = "bossKills", name = "Boss kills", description = "First kill of a boss and kill count milestones",
		position = 3, section = extraSection)
	default boolean bossKills()
	{
		return true;
	}

	@ConfigItem(keyName = "milestones", name = "Big milestones",
		description = "99s, XP milestones, total level and quest progress",
		position = 4, section = extraSection)
	default boolean milestones()
	{
		return true;
	}

	@ConfigItem(keyName = "sounds", name = "Celebration sounds", description = "Play a short jingle with each popup",
		position = 5, section = extraSection)
	default boolean sounds()
	{
		return true;
	}

	@Range(min = 0, max = 100)
	@ConfigItem(keyName = "soundVolume", name = "Sound volume", description = "Volume of celebration sounds (0-100)",
		position = 6, section = extraSection)
	default int soundVolume()
	{
		return 60;
	}

	// ---------- Hidden state ----------
	@ConfigItem(keyName = "pinnedGoal", name = "Pinned goal", description = "Set by right-clicking a suggestion",
		hidden = true)
	default String pinnedGoal()
	{
		return "";
	}
}
