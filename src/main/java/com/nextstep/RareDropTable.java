package com.nextstep;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Rarity data for collection log celebrations.
 *
 * Drop rates are APPROXIMATE and used only to pick how big the celebration is
 * and whether a drop was "spooned" (early) or ended a dry streak. Verify and
 * extend against the OSRS Wiki. Item names must match the collection log text.
 */
final class RareDropTable
{
	static final class Entry
	{
		final String source; // must match the boss name in "Your X kill count is" messages
		final int rate;      // 1 in N

		Entry(String source, int rate)
		{
			this.source = source;
			this.rate = rate;
		}
	}

	private static final Map<String, Entry> BY_NAME = new HashMap<>();

	/** Always MEGA regardless of rate (raid mega-rares, clue jackpots). */
	private static final Set<String> MEGA_NAMES = new HashSet<>(Arrays.asList(
		"twisted bow", "elder maul", "kodai insignia", "twisted buckler",
		"scythe of vitur (uncharged)", "tumeken's shadow (uncharged)", "bloodhound"
	));

	/** Always at least RARE. */
	private static final Set<String> RARE_NAMES = new HashSet<>(Arrays.asList(
		"dragon claws", "ancestral hat", "ancestral robe top", "ancestral robe bottom",
		"dexterous prayer scroll", "arcane prayer scroll", "ranger boots", "dragon warhammer"
	));

	static
	{
		add("Tanzanite fang", "Zulrah", 512);
		add("Magic fang", "Zulrah", 512);
		add("Serpentine visage", "Zulrah", 512);
		add("Jar of swamp", "Zulrah", 3000);
		add("Pet snakeling", "Zulrah", 4000);

		add("Dragonbone necklace", "Vorkath", 1000);
		add("Jar of decay", "Vorkath", 3000);
		add("Vorki", "Vorkath", 3000);
		add("Draconic visage", "Vorkath", 5000);
		add("Skeletal visage", "Vorkath", 5000);

		add("Prince black dragon", "King Black Dragon", 3000);
		add("Baby mole", "Giant Mole", 3000);
		add("Kalphite princess", "Kalphite Queen", 3000);
		add("Jar of sand", "Kalphite Queen", 2000);

		add("Primordial crystal", "Cerberus", 520);
		add("Pegasian crystal", "Cerberus", 520);
		add("Eternal crystal", "Cerberus", 520);
		add("Smouldering stone", "Cerberus", 520);
		add("Jar of souls", "Cerberus", 2000);
		add("Hellpuppy", "Cerberus", 3000);

		add("Spectral sigil", "Corporeal Beast", 1365);
		add("Arcane sigil", "Corporeal Beast", 1365);
		add("Elysian sigil", "Corporeal Beast", 4095);
		add("Pet dark core", "Corporeal Beast", 5000);

		add("Pet chaos elemental", "Chaos Elemental", 300);
		add("Pet general graardor", "General Graardor", 5000);
		add("Pet zilyana", "Commander Zilyana", 5000);
		add("Pet k'ril tsutsaroth", "K'ril Tsutsaroth", 5000);
		add("Pet kree'arra", "Kree'arra", 5000);
		add("Phoenix", "Wintertodt", 5000);
		add("Tiny tempor", "Tempoross", 8000);
	}

	private RareDropTable()
	{
	}

	private static void add(String item, String source, int rate)
	{
		BY_NAME.put(key(item), new Entry(source, rate));
	}

	static Entry find(String item)
	{
		return BY_NAME.get(key(item));
	}

	static Celebration.Tier tierFor(String item)
	{
		String k = key(item);
		if (k.startsWith("3rd age") || MEGA_NAMES.contains(k))
		{
			return Celebration.Tier.MEGA;
		}
		Entry e = BY_NAME.get(k);
		if (e != null && e.rate >= 5000)
		{
			return Celebration.Tier.MEGA;
		}
		if (k.startsWith("gilded ") || RARE_NAMES.contains(k) || (e != null && e.rate >= 1000))
		{
			return Celebration.Tier.RARE;
		}
		return Celebration.Tier.NORMAL;
	}

	static String key(String s)
	{
		return s == null ? "" : s.toLowerCase(Locale.ROOT).trim();
	}
}
