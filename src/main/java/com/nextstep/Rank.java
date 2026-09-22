package com.nextstep;

import java.awt.Color;

/** Fun account rank based on how much of the suggestion list you've achieved. */
public enum Rank
{
	FRESH_SPAWN("Fresh Spawn", 0.00, new Color(170, 170, 170)),
	ADVENTURER("Adventurer", 0.15, new Color(120, 200, 120)),
	EXPLORER("Explorer", 0.30, new Color(90, 180, 230)),
	VETERAN("Veteran", 0.45, new Color(160, 130, 255)),
	CHAMPION("Champion", 0.60, new Color(255, 150, 60)),
	HERO("Hero", 0.75, new Color(255, 90, 90)),
	LEGEND("Legend", 0.90, new Color(255, 210, 60)),
	MAXED_MIND("Maxed Mind", 1.00, new Color(255, 255, 255));

	private final String displayName;
	private final double threshold;
	private final Color color;

	Rank(String displayName, double threshold, Color color)
	{
		this.displayName = displayName;
		this.threshold = threshold;
		this.color = color;
	}

	public static Rank forFraction(double fraction)
	{
		Rank result = FRESH_SPAWN;
		for (Rank r : values())
		{
			if (fraction + 1e-9 >= r.threshold)
			{
				result = r;
			}
		}
		return result;
	}

	public Rank next()
	{
		int i = ordinal() + 1;
		return i < values().length ? values()[i] : null;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public double getThreshold()
	{
		return threshold;
	}

	public Color getColor()
	{
		return color;
	}
}
