package com.nextstep;

import java.util.List;
import java.util.Map;
import net.runelite.api.Skill;

/** Snapshot handed from the client thread to the Swing panel. */
public class PanelData
{
	final List<EvaluatedSuggestion> ready;
	final List<EvaluatedSuggestion> almost;
	final List<EvaluatedSuggestion> later;
	final EvaluatedSuggestion pinned;
	final Rank rank;
	final int achieved;
	final int total;
	final int questsDone;
	final int questsTotal;
	final int sessionUnlocks;
	final boolean showRecommended;
	final String guideName;
	final boolean showDistance;
	final List<EvaluatedSuggestion> guides;
	final Map<Skill, Integer> levels;

	PanelData(List<EvaluatedSuggestion> ready, List<EvaluatedSuggestion> almost, List<EvaluatedSuggestion> later,
		EvaluatedSuggestion pinned, Rank rank, int achieved, int total, int questsDone, int questsTotal,
		int sessionUnlocks, boolean showRecommended, String guideName, boolean showDistance,
		List<EvaluatedSuggestion> guides, Map<Skill, Integer> levels)
	{
		this.ready = ready;
		this.almost = almost;
		this.later = later;
		this.pinned = pinned;
		this.rank = rank;
		this.achieved = achieved;
		this.total = total;
		this.questsDone = questsDone;
		this.questsTotal = questsTotal;
		this.sessionUnlocks = sessionUnlocks;
		this.showRecommended = showRecommended;
		this.guideName = guideName;
		this.showDistance = showDistance;
		this.guides = guides;
		this.levels = levels;
	}
}
