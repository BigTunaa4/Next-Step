package com.nextstep;

import java.awt.Color;
import java.util.concurrent.ThreadLocalRandom;

/** Content for one celebration popup. */
public class Celebration
{
	static final Color GOLD = new Color(255, 200, 60);
	static final Color GREEN = new Color(90, 220, 110);
	static final Color PURPLE = new Color(190, 120, 255);
	static final Color BLUE = new Color(100, 180, 255);

	private static final String[] CHEERS = {
		"Gz!",
		"Big moves.",
		"The grind pays off.",
		"Another one!",
		"Zezima would be proud.",
		"Look at you go!",
		"Absolutely cracked.",
		"Your bank thanks you.",
		"That's a clip.",
		"Main character energy."
	};

	private final String title;
	private final String subtitle;
	private final String detail;
	private final String cheer;
	private final Color color;

	private Celebration(String title, String subtitle, String detail, Color color)
	{
		this.title = title;
		this.subtitle = subtitle;
		this.detail = detail;
		this.cheer = CHEERS[ThreadLocalRandom.current().nextInt(CHEERS.length)];
		this.color = color;
	}

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
}
