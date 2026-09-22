package com.nextstep;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

/** On-screen box showing the pinned goal and live directions to where you're headed. */
public class GoalOverlay extends OverlayPanel
{
	private static final Color ORANGE = new Color(224, 160, 80);
	private static final Color TIP_GRAY = new Color(180, 180, 180);

	private final NextStepPlugin plugin;
	private final NextStepConfig config;

	@Inject
	GoalOverlay(NextStepPlugin plugin, NextStepConfig config)
	{
		super(plugin);
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.TOP_LEFT);
		panelComponent.setPreferredSize(new Dimension(200, 0));
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.showGoalOverlay())
		{
			return null;
		}
		EvaluatedSuggestion goal = plugin.getPinnedEval();
		GuideInfo guide = config.showDirections() ? plugin.getGuideInfo() : null;
		if (goal == null && guide == null)
		{
			return null;
		}

		if (goal != null)
		{
			renderGoal(goal);
		}

		if (guide != null)
		{
			boolean sameAsGoal = goal != null && goal.getName().equals(guide.name);
			if (!sameAsGoal)
			{
				panelComponent.getChildren().add(TitleComponent.builder()
					.text("Guiding: " + guide.name)
					.color(Celebration.BLUE)
					.build());
			}
			if (guide.locationName != null)
			{
				panelComponent.getChildren().add(LineComponent.builder()
					.left("Go to: " + guide.locationName)
					.leftColor(Color.WHITE)
					.build());
			}
			if (guide.directions != null)
			{
				panelComponent.getChildren().add(LineComponent.builder()
					.left(guide.directions)
					.leftColor(guide.directions.startsWith("You're here") ? Celebration.GREEN : Celebration.BLUE)
					.build());
			}
			if (guide.travelTip != null)
			{
				panelComponent.getChildren().add(LineComponent.builder()
					.left("Tip: " + guide.travelTip)
					.leftColor(TIP_GRAY)
					.build());
			}
		}
		return super.render(graphics);
	}

	private void renderGoal(EvaluatedSuggestion goal)
	{
		int pct = (int) Math.round(goal.getProgress() * 100);
		panelComponent.getChildren().add(TitleComponent.builder()
			.text("Goal: " + goal.getName())
			.color(Celebration.GOLD)
			.build());
		panelComponent.getChildren().add(LineComponent.builder()
			.left("Progress")
			.right(pct + "%")
			.rightColor(pct >= 100 ? Celebration.GREEN : Color.WHITE)
			.build());

		if (goal.getStatus() == EvaluatedSuggestion.Status.READY)
		{
			panelComponent.getChildren().add(LineComponent.builder()
				.left("Ready! Go do it.")
				.leftColor(Celebration.GREEN)
				.build());
			return;
		}
		int shown = 0;
		for (String m : goal.getMissing())
		{
			if (shown++ >= 3)
			{
				break;
			}
			panelComponent.getChildren().add(LineComponent.builder()
				.left(m)
				.leftColor(ORANGE)
				.build());
		}
	}
}
