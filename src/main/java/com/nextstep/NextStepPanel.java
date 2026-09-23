package com.nextstep;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import java.util.concurrent.ThreadLocalRandom;
import net.runelite.api.Skill;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.LinkBrowser;

/** Sidebar panel with three tabs: Next Steps, Skills (training guides), Minigames. */
public class NextStepPanel extends PluginPanel
{
	private static final Color MISSING_COLOR = new Color(224, 160, 80);
	private static final Color HOVER_COLOR = new Color(60, 60, 60);
	private static final Color GUIDE_BG = new Color(30, 50, 70);
	private static final String WIKI_BASE = "https://oldschool.runescape.wiki/w/";
	private static final String TAB_NEXT = "next";
	private static final String TAB_SKILLS = "skills";
	private static final String TAB_MINIGAMES = "minigames";

	private final NextStepPlugin plugin;
	private final JPanel cardHolder = new JPanel(new BorderLayout());
	private JPanel nextTab;
	private JPanel skillsTab;
	private JPanel minigamesTab;

	// Next Steps tab
	private final JPanel nextList = column();
	private boolean showLater;

	// Skills tab
	private final JComboBox<String> skillBox = new JComboBox<>();
	private final JCheckBox skillsOnlyReady = new JCheckBox("Only show what I can do");
	private final JPanel skillsList = column();
	private final List<Skill> skillOrder = new ArrayList<>();
	private boolean updatingSkillBox;

	// Minigames tab
	private final JCheckBox minigamesOnlyReady = new JCheckBox("Only show what I can do");
	private final JPanel minigamesList = column();

	private PanelData lastData;

	// "Pick for me" wheel
	private final JLabel wheelLabel = new JLabel(" ", SwingConstants.CENTER);
	private final JPanel wheelActions = new JPanel(new GridLayout(1, 2, 4, 0));
	private javax.swing.Timer spinTimer;

	NextStepPanel(NextStepPlugin plugin)
	{
		super();
		this.plugin = plugin;
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel top = new JPanel();
		top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
		top.setBackground(ColorScheme.DARK_GRAY_COLOR);
		top.add(buildHeader());
		top.add(buildTabs());

		cardHolder.setBackground(ColorScheme.DARK_GRAY_COLOR);
		nextTab = nextList;
		skillsTab = buildSkillsTab();
		minigamesTab = buildMinigamesTab();
		showTab(TAB_NEXT);

		add(top, BorderLayout.NORTH);
		add(cardHolder, BorderLayout.CENTER);
		showLoggedOut();
	}

	// ======================= Construction =======================

	private JPanel buildHeader()
	{
		JPanel header = new JPanel(new BorderLayout());
		header.setBackground(ColorScheme.DARK_GRAY_COLOR);
		header.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

		JLabel title = new JLabel("Next Step");
		title.setFont(FontManager.getRunescapeBoldFont());
		title.setForeground(Color.WHITE);

		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
		buttons.setBackground(ColorScheme.DARK_GRAY_COLOR);
		JButton test = smallButton("Test", "Preview a celebration popup in game");
		test.addActionListener(e -> plugin.previewCelebration());
		JButton refresh = smallButton("Refresh", "Re-check your account now");
		refresh.addActionListener(e -> plugin.requestRefresh());
		buttons.add(test);
		buttons.add(refresh);

		header.add(title, BorderLayout.WEST);
		header.add(buttons, BorderLayout.EAST);
		return header;
	}

	private JPanel buildTabs()
	{
		JPanel tabs = new JPanel(new GridLayout(1, 3, 4, 0));
		tabs.setBackground(ColorScheme.DARK_GRAY_COLOR);
		tabs.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
		ButtonGroup group = new ButtonGroup();
		addTab(tabs, group, "Next", TAB_NEXT, true);
		addTab(tabs, group, "Skills", TAB_SKILLS, false);
		addTab(tabs, group, "Minigames", TAB_MINIGAMES, false);
		return tabs;
	}

	private void addTab(JPanel tabs, ButtonGroup group, String text, String card, boolean selected)
	{
		JToggleButton b = new JToggleButton(text, selected);
		b.setFocusable(false);
		b.setMargin(new Insets(2, 2, 2, 2));
		b.addActionListener(e -> showTab(card));
		group.add(b);
		tabs.add(b);
	}

	private JPanel buildSkillsTab()
	{
		JPanel tab = new JPanel(new BorderLayout(0, 6));
		tab.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel controls = column();
		skillBox.setFocusable(false);
		skillBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, skillBox.getPreferredSize().height));
		skillBox.setAlignmentX(LEFT_ALIGNMENT);
		skillBox.addActionListener(e ->
		{
			if (!updatingSkillBox)
			{
				rebuildSkills();
			}
		});
		styleCheckBox(skillsOnlyReady);
		skillsOnlyReady.addActionListener(e -> rebuildSkills());
		controls.add(skillBox);
		controls.add(Box.createRigidArea(new Dimension(0, 4)));
		controls.add(skillsOnlyReady);

		tab.add(controls, BorderLayout.NORTH);
		tab.add(skillsList, BorderLayout.CENTER);
		return tab;
	}

	private JPanel buildMinigamesTab()
	{
		JPanel tab = new JPanel(new BorderLayout(0, 6));
		tab.setBackground(ColorScheme.DARK_GRAY_COLOR);
		styleCheckBox(minigamesOnlyReady);
		minigamesOnlyReady.addActionListener(e -> rebuildMinigames());
		tab.add(minigamesOnlyReady, BorderLayout.NORTH);
		tab.add(minigamesList, BorderLayout.CENTER);
		return tab;
	}

	/** Only the active tab is in the layout, so each tab scrolls to its own height. */
	private void showTab(String tab)
	{
		cardHolder.removeAll();
		switch (tab)
		{
			case TAB_SKILLS:
				cardHolder.add(skillsTab, BorderLayout.NORTH);
				break;
			case TAB_MINIGAMES:
				cardHolder.add(minigamesTab, BorderLayout.NORTH);
				break;
			default:
				cardHolder.add(nextTab, BorderLayout.NORTH);
		}
		cardHolder.revalidate();
		cardHolder.repaint();
	}

	// ======================= Updates =======================

	void showLoggedOut()
	{
		lastData = null;
		for (JPanel list : new JPanel[]{nextList, skillsList, minigamesList})
		{
			list.removeAll();
			list.add(infoLabel("Log in to see what to do next."));
			list.revalidate();
			list.repaint();
		}
	}

	void update(PanelData data)
	{
		lastData = data;
		populateSkillBox(data);
		rebuildNext();
		rebuildSkills();
		rebuildMinigames();
	}

	private void populateSkillBox(PanelData d)
	{
		if (!skillOrder.isEmpty())
		{
			return;
		}
		Set<Skill> withGuides = EnumSet.noneOf(Skill.class);
		for (EvaluatedSuggestion e : d.guides)
		{
			withGuides.addAll(e.getSuggestion().getGuideSkills());
		}
		updatingSkillBox = true;
		for (Skill skill : Skill.values())
		{
			if (withGuides.contains(skill))
			{
				skillOrder.add(skill);
				skillBox.addItem(skill.getName());
			}
		}
		updatingSkillBox = false;
	}

	// ---------------- Next Steps tab ----------------

	private void rebuildNext()
	{
		PanelData d = lastData;
		nextList.removeAll();
		if (d == null)
		{
			return;
		}

		nextList.add(rankBlock(d));
		gap(nextList, 8);
		nextList.add(wheelBlock());
		gap(nextList, 10);

		if (d.guideName != null)
		{
			nextList.add(guideBlock(d.guideName));
			gap(nextList, 10);
		}
		if (d.pinned != null)
		{
			nextList.add(sectionLabel("Current goal"));
			nextList.add(card(d.pinned, null));
			gap(nextList, 10);
		}

		nextList.add(sectionLabel("Ready now (" + d.ready.size() + ")"));
		if (d.ready.isEmpty())
		{
			nextList.add(infoLabel("Nothing ready right now. Check 'Almost there' below."));
		}
		addCards(nextList, d.ready);

		gap(nextList, 8);
		nextList.add(sectionLabel("Almost there (" + d.almost.size() + ")"));
		if (d.almost.isEmpty())
		{
			nextList.add(infoLabel("Nothing within range. Raise the range in settings to see more."));
		}
		addCards(nextList, d.almost);

		gap(nextList, 8);
		JButton toggle = new JButton(showLater
			? "Hide long-term goals"
			: "Show long-term goals (" + d.later.size() + ")");
		toggle.setFocusable(false);
		toggle.setAlignmentX(LEFT_ALIGNMENT);
		toggle.setMaximumSize(new Dimension(Integer.MAX_VALUE, toggle.getPreferredSize().height));
		toggle.addActionListener(e ->
		{
			showLater = !showLater;
			rebuildNext();
		});
		nextList.add(toggle);
		if (showLater)
		{
			gap(nextList, 6);
			addCards(nextList, d.later);
		}

		gap(nextList, 8);
		nextList.add(infoLabel("Tip: right-click anything to pin it, get directions, or open the wiki."));
		nextList.revalidate();
		nextList.repaint();
	}

	// ---------------- Skills tab ----------------

	private void rebuildSkills()
	{
		PanelData d = lastData;
		skillsList.removeAll();
		int index = skillBox.getSelectedIndex();
		if (d == null || index < 0 || index >= skillOrder.size())
		{
			skillsList.revalidate();
			skillsList.repaint();
			return;
		}
		Skill skill = skillOrder.get(index);
		Integer level = d.levels.get(skill);

		List<EvaluatedSuggestion> list = d.guides.stream()
			.filter(e -> e.getSuggestion().getGuideSkills().contains(skill))
			.sorted(Comparator.comparingInt((EvaluatedSuggestion e) -> e.getSuggestion().getLevelFrom())
				.thenComparing(EvaluatedSuggestion::getName))
			.collect(Collectors.toList());

		EvaluatedSuggestion bestFast = best(list, Suggestion.Tag.FAST);
		EvaluatedSuggestion bestAfk = best(list, Suggestion.Tag.AFK);
		EvaluatedSuggestion bestProfit = best(list, Suggestion.Tag.PROFIT);

		JPanel summary = column();
		summary.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		summary.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		StringBuilder html = new StringBuilder("<html><body style='width:170px'>");
		html.append("<b>Your ").append(skill.getName()).append(" level: ")
			.append(level == null ? "?" : level).append("</b>");
		html.append("<br><font color='#FFC83C'>Best for you right now:</font>");
		appendBest(html, "Fast XP", bestFast);
		appendBest(html, "AFK", bestAfk);
		appendBest(html, "Profit", bestProfit);
		html.append("</body></html>");
		JLabel summaryLabel = new JLabel(html.toString());
		summaryLabel.setFont(FontManager.getRunescapeSmallFont());
		summaryLabel.setForeground(Color.LIGHT_GRAY);
		summary.add(summaryLabel);
		summary.setMaximumSize(new Dimension(Integer.MAX_VALUE, summary.getPreferredSize().height));
		skillsList.add(summary);
		gap(skillsList, 8);

		int shown = 0;
		for (EvaluatedSuggestion e : list)
		{
			if (skillsOnlyReady.isSelected() && e.getStatus() != EvaluatedSuggestion.Status.READY)
			{
				continue;
			}
			String badge = null;
			if (e == bestFast)
			{
				badge = "BEST FAST XP";
			}
			else if (e == bestAfk)
			{
				badge = "BEST AFK";
			}
			else if (e == bestProfit)
			{
				badge = "BEST PROFIT";
			}
			skillsList.add(card(e, badge));
			gap(skillsList, 5);
			shown++;
		}
		if (shown == 0)
		{
			skillsList.add(infoLabel("Nothing to show with this filter yet. Keep training!"));
		}
		skillsList.revalidate();
		skillsList.repaint();
	}

	/** Highest-level ready guide with the given tag. */
	private static EvaluatedSuggestion best(List<EvaluatedSuggestion> list, Suggestion.Tag tag)
	{
		EvaluatedSuggestion best = null;
		for (EvaluatedSuggestion e : list)
		{
			if (e.getStatus() == EvaluatedSuggestion.Status.READY && e.getSuggestion().getTags().contains(tag)
				&& (best == null || e.getSuggestion().getLevelFrom() >= best.getSuggestion().getLevelFrom()))
			{
				best = e;
			}
		}
		return best;
	}

	private static void appendBest(StringBuilder html, String label, EvaluatedSuggestion e)
	{
		html.append("<br>").append(label).append(": ");
		html.append(e == null ? "<font color='#888888'>none yet</font>" : escape(e.getName()));
	}

	// ---------------- Minigames tab ----------------

	private void rebuildMinigames()
	{
		PanelData d = lastData;
		minigamesList.removeAll();
		if (d == null)
		{
			return;
		}
		List<EvaluatedSuggestion> list = d.guides.stream()
			.filter(e -> e.getSuggestion().getType() == Suggestion.Type.MINIGAME)
			.sorted(Comparator.comparingInt((EvaluatedSuggestion e) ->
				e.getStatus() == EvaluatedSuggestion.Status.READY ? 0 : 1)
				.thenComparing(EvaluatedSuggestion::getName))
			.collect(Collectors.toList());

		long ready = list.stream().filter(e -> e.getStatus() == EvaluatedSuggestion.Status.READY).count();
		minigamesList.add(sectionLabel("You can play " + ready + " of " + list.size()));
		for (EvaluatedSuggestion e : list)
		{
			if (minigamesOnlyReady.isSelected() && e.getStatus() != EvaluatedSuggestion.Status.READY)
			{
				continue;
			}
			minigamesList.add(card(e, null));
			gap(minigamesList, 5);
		}
		minigamesList.revalidate();
		minigamesList.repaint();
	}

	// ======================= Components =======================

	private JPanel rankBlock(PanelData d)
	{
		JPanel block = column();
		block.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		block.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		JLabel rank = new JLabel("Rank: " + d.rank.getDisplayName());
		rank.setFont(FontManager.getRunescapeBoldFont());
		rank.setForeground(d.rank.getColor());
		rank.setAlignmentX(LEFT_ALIGNMENT);

		Rank next = d.rank.next();
		double frac;
		String barText;
		if (next == null)
		{
			frac = 1;
			barText = "Top rank reached!";
		}
		else
		{
			double current = d.total == 0 ? 0 : d.achieved / (double) d.total;
			frac = (current - d.rank.getThreshold()) / (next.getThreshold() - d.rank.getThreshold());
			barText = "Next: " + next.getDisplayName();
		}
		MiniBar bar = new MiniBar(frac, d.rank.getColor(), barText);
		bar.setAlignmentX(LEFT_ALIGNMENT);

		JLabel stats = new JLabel("<html><body style='width:170px'>"
			+ "Achieved: " + d.achieved + " / " + d.total + "<br>"
			+ "Quests & miniquests: " + d.questsDone + " / " + d.questsTotal + "<br>"
			+ "Unlocked this session: " + d.sessionUnlocks
			+ "</body></html>");
		stats.setFont(FontManager.getRunescapeSmallFont());
		stats.setForeground(Color.LIGHT_GRAY);
		stats.setAlignmentX(LEFT_ALIGNMENT);

		block.add(rank);
		block.add(Box.createRigidArea(new Dimension(0, 5)));
		block.add(bar);
		block.add(Box.createRigidArea(new Dimension(0, 5)));
		block.add(stats);
		block.setMaximumSize(new Dimension(Integer.MAX_VALUE, block.getPreferredSize().height));
		return block;
	}

	private JPanel wheelBlock()
	{
		JPanel block = new JPanel(new BorderLayout(0, 6));
		block.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		block.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		block.setAlignmentX(LEFT_ALIGNMENT);

		JButton spin = new JButton("Can't decide? Pick for me");
		spin.setFocusable(false);
		spin.addActionListener(e -> spin());

		wheelLabel.setFont(FontManager.getRunescapeBoldFont());
		wheelLabel.setForeground(Celebration.GOLD);
		wheelActions.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		block.add(spin, BorderLayout.NORTH);
		block.add(wheelLabel, BorderLayout.CENTER);
		block.add(wheelActions, BorderLayout.SOUTH);
		block.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));
		return block;
	}

	/** Slot-machine style spin through the Ready list that slows down and lands on one. */
	private void spin()
	{
		PanelData d = lastData;
		if (d == null || (spinTimer != null && spinTimer.isRunning()))
		{
			return;
		}
		List<EvaluatedSuggestion> pool = d.ready;
		wheelActions.removeAll();
		wheelActions.setVisible(false);
		if (pool.isEmpty())
		{
			wheelLabel.setText("<html><center>Nothing ready yet.<br>Keep training!</center></html>");
			return;
		}

		final int total = 22 + ThreadLocalRandom.current().nextInt(8);
		final int[] step = {0};
		final EvaluatedSuggestion[] shown = {pool.get(0)};
		spinTimer = new javax.swing.Timer(45, null);
		spinTimer.addActionListener(ev ->
		{
			EvaluatedSuggestion next = pool.size() == 1 ? pool.get(0)
				: pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
			shown[0] = next;
			wheelLabel.setText("<html><center>" + escape(next.getName()) + "</center></html>");
			step[0]++;
			spinTimer.setDelay(45 + step[0] * step[0] / 2);
			if (step[0] >= total)
			{
				spinTimer.stop();
				finishSpin(shown[0]);
			}
		});
		spinTimer.start();
	}

	private void finishSpin(EvaluatedSuggestion pick)
	{
		Suggestion s = pick.getSuggestion();
		wheelLabel.setText("<html><center>&#9733; " + escape(s.getName()) + " &#9733;<br>"
			+ "<font color='#C8C8C8' size='2'>" + escape(s.getReward()) + "</font></center></html>");

		wheelActions.removeAll();
		if (s.getLocationName() != null)
		{
			JButton go = smallButton("Show me", "Get directions");
			go.addActionListener(a -> plugin.guideTo(s.getName()));
			wheelActions.add(go);
		}
		JButton again = smallButton("Spin again", "Pick something else");
		again.addActionListener(a -> spin());
		wheelActions.add(again);
		wheelActions.setVisible(true);
		wheelActions.revalidate();
		plugin.announcePick(s.getName());
	}

	private JPanel guideBlock(String name)
	{
		JPanel block = new JPanel(new BorderLayout(6, 0));
		block.setBackground(GUIDE_BG);
		block.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
		block.setAlignmentX(LEFT_ALIGNMENT);

		JLabel label = new JLabel("<html><body style='width:120px'><b>Guiding to</b><br>"
			+ escape(name) + "</body></html>");
		label.setFont(FontManager.getRunescapeSmallFont());
		label.setForeground(Color.WHITE);

		JButton stop = smallButton("Stop", "Stop showing the way");
		stop.addActionListener(a -> plugin.stopGuide());

		block.add(label, BorderLayout.CENTER);
		block.add(stop, BorderLayout.EAST);
		block.setMaximumSize(new Dimension(Integer.MAX_VALUE, block.getPreferredSize().height));
		return block;
	}

	private void addCards(JPanel target, List<EvaluatedSuggestion> list)
	{
		for (EvaluatedSuggestion e : list)
		{
			target.add(card(e, null));
			gap(target, 5);
		}
	}

	private JPanel card(EvaluatedSuggestion e, String badge)
	{
		PanelData d = lastData;
		boolean showRec = d != null && d.showRecommended;
		boolean showDistance = d != null && d.showDistance;
		Suggestion s = e.getSuggestion();

		StringBuilder html = new StringBuilder("<html><body style='width:165px'>");
		if (badge != null)
		{
			html.append("<font color='#FFC83C'><b>&#9733; ").append(badge).append("</b></font><br>");
		}
		html.append("<b>").append(escape(s.getName())).append("</b> ");
		html.append("<font color='#999999'>").append(s.getType().getLabel()).append("</font>");
		if (e.isInProgress())
		{
			html.append(" <font color='#6FB7FF'>in progress</font>");
		}

		String meta = guideMeta(s);
		if (!meta.isEmpty())
		{
			html.append("<br><font color='#C8A0FF'>").append(meta).append("</font>");
		}
		html.append("<br>").append(escape(s.getReward()));

		if (s.getLocationName() != null)
		{
			html.append("<br><font color='#6FB7FF'>Go to: ").append(escape(s.getLocationName()));
			if (showDistance && e.getDistance() >= 0)
			{
				html.append(" (~").append(e.getDistance()).append(" tiles ").append(e.getCompass()).append(")");
			}
			html.append("</font>");
		}

		if (e.getStatus() == EvaluatedSuggestion.Status.READY)
		{
			html.append("<br><font color='#5AC86E'>You meet all requirements</font>");
		}
		else
		{
			for (String m : e.getMissing())
			{
				html.append("<br><font color='#E0A050'>Need ").append(escape(m)).append("</font>");
			}
		}
		if (s.getNote() != null)
		{
			html.append("<br><font color='#AAAAAA'>Also: ").append(escape(s.getNote())).append("</font>");
		}
		if (showRec)
		{
			for (String r : e.getRecWarnings())
			{
				html.append("<br><font color='#AAAAAA'>Rec. ").append(escape(r)).append("</font>");
			}
		}
		html.append("</body></html>");

		JLabel label = new JLabel(html.toString());
		label.setFont(FontManager.getRunescapeSmallFont());
		label.setForeground(Color.LIGHT_GRAY);
		label.setInheritsPopupMenu(true);

		JPanel card = new JPanel(new BorderLayout(0, 5));
		card.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		card.setBorder(badge != null
			? BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(0, 3, 0, 0, Celebration.GOLD),
				BorderFactory.createEmptyBorder(6, 5, 6, 8))
			: BorderFactory.createEmptyBorder(6, 8, 6, 8));
		card.add(label, BorderLayout.CENTER);

		if (e.getStatus() != EvaluatedSuggestion.Status.READY)
		{
			int pct = (int) Math.round(e.getProgress() * 100);
			MiniBar bar = new MiniBar(e.getProgress(), MISSING_COLOR, pct + "%");
			bar.setInheritsPopupMenu(true);
			card.add(bar, BorderLayout.SOUTH);
		}

		card.setComponentPopupMenu(buildMenu(e));
		card.setToolTipText("Right-click for options");
		addHover(card, card, label);
		card.setAlignmentX(LEFT_ALIGNMENT);
		card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));
		return card;
	}

	/** "Lv 43+ · Fast XP · AFK" for guides; empty for quests and PvM. */
	private static String guideMeta(Suggestion s)
	{
		if (!s.isGuide())
		{
			return "";
		}
		List<String> parts = new ArrayList<>();
		if (s.getType() == Suggestion.Type.METHOD)
		{
			parts.add("Lv " + s.getLevelFrom() + "+");
		}
		for (Suggestion.Tag t : s.getTags())
		{
			parts.add(t.getLabel());
		}
		return String.join(" &middot; ", parts);
	}

	private JPopupMenu buildMenu(EvaluatedSuggestion e)
	{
		Suggestion s = e.getSuggestion();
		JPopupMenu menu = new JPopupMenu();
		boolean isPinned = s.getName().equals(plugin.getPinnedGoalName());
		boolean alreadyAchievable = e.getStatus() == EvaluatedSuggestion.Status.READY
			&& s.getType() != Suggestion.Type.QUEST;

		if (isPinned)
		{
			JMenuItem unpin = new JMenuItem("Unpin goal");
			unpin.addActionListener(a -> plugin.setPinnedGoal(null));
			menu.add(unpin);
		}
		else if (!alreadyAchievable)
		{
			JMenuItem pin = new JMenuItem("Pin as goal");
			pin.addActionListener(a -> plugin.setPinnedGoal(s.getName()));
			menu.add(pin);
		}

		if (s.getLocationName() != null)
		{
			boolean guiding = s.getName().equals(plugin.getGuideName());
			JMenuItem guide = new JMenuItem(guiding ? "Stop guiding" : "Show me where to go");
			guide.addActionListener(a ->
			{
				if (guiding)
				{
					plugin.stopGuide();
				}
				else
				{
					plugin.guideTo(s.getName());
				}
			});
			menu.add(guide);
		}

		JMenuItem wiki = new JMenuItem("Open wiki page");
		wiki.addActionListener(a -> LinkBrowser.browse(WIKI_BASE
			+ URLEncoder.encode(s.getWikiPage().replace(' ', '_'), StandardCharsets.UTF_8)));
		menu.add(wiki);
		return menu;
	}

	private static void addHover(JPanel card, JComponent... targets)
	{
		Color normal = card.getBackground();
		MouseAdapter hover = new MouseAdapter()
		{
			@Override
			public void mouseEntered(MouseEvent e)
			{
				card.setBackground(HOVER_COLOR);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				card.setBackground(normal);
			}
		};
		for (JComponent c : targets)
		{
			c.addMouseListener(hover);
		}
	}

	// ======================= Helpers =======================

	private static JPanel column()
	{
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.setBackground(ColorScheme.DARK_GRAY_COLOR);
		p.setAlignmentX(LEFT_ALIGNMENT);
		return p;
	}

	private static void styleCheckBox(JCheckBox box)
	{
		box.setFocusable(false);
		box.setBackground(ColorScheme.DARK_GRAY_COLOR);
		box.setForeground(Color.LIGHT_GRAY);
		box.setFont(FontManager.getRunescapeSmallFont());
		box.setAlignmentX(LEFT_ALIGNMENT);
	}

	private JLabel sectionLabel(String text)
	{
		JLabel l = new JLabel(text);
		l.setFont(FontManager.getRunescapeBoldFont());
		l.setForeground(ColorScheme.BRAND_ORANGE);
		l.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
		l.setAlignmentX(LEFT_ALIGNMENT);
		return l;
	}

	private JLabel infoLabel(String text)
	{
		JLabel l = new JLabel("<html><body style='width:170px'>" + escape(text) + "</body></html>");
		l.setFont(FontManager.getRunescapeSmallFont());
		l.setForeground(Color.GRAY);
		l.setAlignmentX(LEFT_ALIGNMENT);
		return l;
	}

	private static JButton smallButton(String text, String tooltip)
	{
		JButton b = new JButton(text);
		b.setFocusable(false);
		b.setToolTipText(tooltip);
		b.setMargin(new Insets(2, 6, 2, 6));
		return b;
	}

	private static void gap(JPanel target, int px)
	{
		target.add(Box.createRigidArea(new Dimension(0, px)));
	}

	private static String escape(String s)
	{
		return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}
}
