package com.nextstep;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import net.runelite.api.Quest;

/**
 * Finds quests by their in-game name instead of by enum constant.
 * A typo or a renamed quest just skips that rule (with a log warning)
 * instead of breaking the whole build.
 */
final class QuestLookup
{
	private static final Map<String, Quest> BY_NAME = new HashMap<>();

	static
	{
		for (Quest quest : Quest.values())
		{
			BY_NAME.put(normalize(quest.getName()), quest);
		}
	}

	private QuestLookup()
	{
	}

	static Quest find(String name)
	{
		return name == null ? null : BY_NAME.get(normalize(name));
	}

	private static String normalize(String s)
	{
		return s.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
	}
}
