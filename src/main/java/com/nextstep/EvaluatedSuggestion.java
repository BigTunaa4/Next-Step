package com.nextstep;

import java.util.List;

/** A Suggestion checked against the player's current account. Immutable. */
public class EvaluatedSuggestion
{
	public enum Status
	{
		DONE,
		READY,
		ALMOST,
		LOCKED
	}

	private final Suggestion suggestion;
	private final Status status;
	private final int levelGap;
	private final List<String> missing;
	private final List<String> recWarnings;
	private final double progress;
	private final boolean inProgress;
	private int distance = -1;   // tiles from the player, -1 if unknown
	private String compass;      // short direction like "NE"

	public EvaluatedSuggestion(Suggestion suggestion, Status status, int levelGap, List<String> missing,
		List<String> recWarnings, double progress, boolean inProgress)
	{
		this.suggestion = suggestion;
		this.status = status;
		this.levelGap = levelGap;
		this.missing = missing;
		this.recWarnings = recWarnings;
		this.progress = progress;
		this.inProgress = inProgress;
	}

	public Suggestion getSuggestion()
	{
		return suggestion;
	}

	public String getName()
	{
		return suggestion.getName();
	}

	public Status getStatus()
	{
		return status;
	}

	public int getLevelGap()
	{
		return levelGap;
	}

	public List<String> getMissing()
	{
		return missing;
	}

	public List<String> getRecWarnings()
	{
		return recWarnings;
	}

	public double getProgress()
	{
		return progress;
	}

	public boolean isInProgress()
	{
		return inProgress;
	}

	public int getDistance()
	{
		return distance;
	}

	public String getCompass()
	{
		return compass;
	}

	void setDistance(int distance, String compass)
	{
		this.distance = distance;
		this.compass = compass;
	}

	/** Done quest, or a non-quest whose requirements are all met. */
	public boolean isAchieved()
	{
		return status == Status.DONE
			|| (status == Status.READY && suggestion.getType() != Suggestion.Type.QUEST);
	}
}
