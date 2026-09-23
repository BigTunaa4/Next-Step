package com.nextstep;

import java.awt.Color;
import java.util.concurrent.ThreadLocalRandom;

/** Content for one celebration popup. Bigger tiers get bigger effects and sounds. */
public class Celebration
{
	public enum Tier
	{
		NORMAL,
		RARE,
		MEGA
	}

	static final Color GOLD = new Color(255, 200, 60);
	static final Color GREEN = new Color(90, 220, 110);
	static final Color PURPLE = new Color(190, 120, 255);
	static final Color BLUE = new Color(100, 180, 255);
	static final Color PINK = new Color(255, 110, 190);
	static final Color RED = new Color(255, 100, 90);

	private static final String[] CHEERS = {
		"Gz!", "Big moves.", "The grind pays off.", "Another one!", "Zezima would be proud.",
		"Look at you go!", "Absolutely cracked.", "Your bank thanks you.", "That's a clip.",
		"Main character energy."
	};

	private final String title;
	private final String subtitle;
	private final String detail;
	private final String cheer;
	private final Color color;
	private final Tier tier;
	private final String banner; // shown above the box for RARE/MEGA, may be null

	private Celebration(String title, String subtitle, String detail, Color color)
	{
		this(title, subtitle, detail, color, Tier.NORMAL, null);
	}

	private Celebration(String title, String subtitle, String detail, Color color, Tier tier, String banner)
	{
		this.title = title;
		this.subtitle = subtitle;
		this.detail = detail;
		this.cheer = CHEERS[ThreadLocalRandom.current().nextInt(CHEERS.length)];
		this.color = color;
		this.tier = tier;
		this.banner = banner;
	}

	private static Tier atLeastRare(Tier t)
	{
		return t == Tier.NORMAL ? Tier.RARE : t;
	}

	// ---------------- Progress ----------------

	static Celebration questDone(Suggestion s)
	{
		return new Celebration("Quest complete!", s.getName(), s.getReward(), GOLD);
	}

	static Celebration unlocked(Suggestion s)
	{
		String title;
		switch (s.getType())
		{
			case QUEST:
				title = "Quest unlocked!";
				break;
			case METHOD:
				title = "New method unlocked!";
				break;
			case MINIGAME:
				title = "Minigame unlocked!";
				break;
			default:
				title = "New unlock!";
		}
		return new Celebration(title, s.getName(), s.getReward(), GREEN);
	}

	static Celebration goal(Suggestion s)
	{
		return new Celebration("Goal reached!", s.getName(), "You set it. You did it.", PURPLE);
	}

	static Celebration batch(int count)
	{
		return new Celebration(count + " new unlocks!", "Your account just grew", "Open the Next Step panel to see them", GREEN);
	}

	static Celebration rankUp(Rank rank)
	{
		return new Celebration("Rank up!", "You are now: " + rank.getDisplayName(), "Keep climbing.", rank.getColor());
	}

	static Celebration preview()
	{
		return new Celebration("Quest complete!", "Next Step preview", "This is what finishing things feels like", GOLD);
	}

	static Celebration wheel(String pick)
	{
		return new Celebration("The wheel has spoken!", pick, "Go get it.", PURPLE);
	}

	// ---------------- Drops ----------------

	static Celebration collectionLog(String item, Tier tier)
	{
		switch (tier)
		{
			case MEGA:
				return new Celebration("MEGA RARE!", item, "New collection log slot", PINK, Tier.MEGA, "MEGA RARE DROP!");
			case RARE:
				return new Celebration("Rare drop!", item, "New collection log slot", GOLD, Tier.RARE, "RARE DROP!");
			default:
				return new Celebration("Collection log!", item, "New slot filled", BLUE);
		}
	}

	static Celebration pet()
	{
		return new Celebration("PET DROP!", "You have a funny feeling you're being followed...",
			"The rarest thrill in Gielinor", PINK, Tier.MEGA, "PET!");
	}

	static Celebration spooned(String item, int kc, int rate, Tier tier)
	{
		return new Celebration("SPOONED!", item + " at " + kc + " kc", "Expected around 1 in " + rate,
			GREEN, atLeastRare(tier), "LUCKY!");
	}

	static Celebration finallyDrop(String item, int kc, int rate, Tier tier)
	{
		String times = String.format("%.1f", kc / (double) rate);
		return new Celebration("FINALLY!", item + " at " + kc + " kc", times + "x the drop rate. You earned this.",
			GOLD, atLeastRare(tier), "DRY STREAK OVER");
	}

	// ---------------- Bosses ----------------

	static Celebration firstKill(String boss)
	{
		return new Celebration("New boss slain!", boss, "Your first kill. Many more to come.", RED, Tier.RARE, "FIRST KILL");
	}

	static Celebration killMilestone(String boss, int kc)
	{
		Tier tier = kc >= 1000 ? Tier.RARE : Tier.NORMAL;
		return new Celebration(kc + " kills!", boss, "Kill count milestone", RED, tier, tier == Tier.RARE ? "MILESTONE" : null);
	}

	// ---------------- Milestones ----------------

	static Celebration level99(String skill)
	{
		return new Celebration("99 " + skill + "!", "Skill mastered", "Time to buy that cape.", GOLD, Tier.MEGA, "MAXED SKILL");
	}

	static Celebration xpMilestone(String skill, String label, Tier tier)
	{
		return new Celebration(label + " " + skill + " XP!", "XP milestone", "The grind is real.", BLUE, tier,
			tier == Tier.NORMAL ? null : "XP MILESTONE");
	}

	static Celebration totalLevel(int total)
	{
		Tier tier = total >= 2000 ? Tier.MEGA : total >= 1500 ? Tier.RARE : Tier.NORMAL;
		return new Celebration("Total level " + total + "!", "Account milestone", "Every level counts.", PURPLE, tier,
			tier == Tier.NORMAL ? null : "TOTAL LEVEL");
	}

	static Celebration questProgress(int pct)
	{
		Tier tier = pct >= 100 ? Tier.MEGA : pct >= 75 ? Tier.RARE : Tier.NORMAL;
		String sub = pct >= 100 ? "Every quest complete" : pct + "% of quests complete";
		return new Celebration(pct >= 100 ? "Quest cape unlocked!" : "Quest milestone!", sub,
			pct >= 100 ? "Legendary." : "On the road to the quest cape.", GOLD, tier, tier == Tier.NORMAL ? null : "QUEST PROGRESS");
	}

	// ---------------- Getters ----------------

	public String getTitle()
	{
		return title;
	}

	public String getSubtitle()
	{
		return subtitle;
	}

	public String getDetail()
	{
		return detail;
	}

	public String getCheer()
	{
		return cheer;
	}

	public Color getColor()
	{
		return color;
	}

	public Tier getTier()
	{
		return tier;
	}

	public String getBanner()
	{
		return banner;
	}
}
