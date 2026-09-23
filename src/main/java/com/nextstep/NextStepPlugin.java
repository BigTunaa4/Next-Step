package com.nextstep;

import com.google.inject.Provides;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import net.runelite.api.ChatMessageType;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.StatChanged;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PluginDescriptor(
	name = "Next Step",
	description = "Tells you the most valuable things to do next, tracks goals, and celebrates your progress",
	tags = {"quest", "goals", "planner", "advisor", "progress"}
)
public class NextStepPlugin extends Plugin
{
	private static final Logger log = LoggerFactory.getLogger(NextStepPlugin.class);

	static final String CONFIG_GROUP = "nextstep";
	static final String PINNED_KEY = "pinnedGoal";

	private static final int QUEST_POINTS_VARP = 101;
	private static final int PERIODIC_REFRESH_TICKS = 50; // ~30s; quests have no completion event
	private static final int LOGIN_SETTLE_TICKS = 5;      // let stats/quests load before celebrating
	private static final int MAX_INDIVIDUAL_POPUPS = 3;
	private static final int LATER_LIMIT = 10;
	private static final int QUEST_CHAT_REFRESH_DELAY = 2;

	private static final String CLOG_PREFIX = "New item added to your collection log:";
	private static final Pattern KC_PATTERN = Pattern.compile(
		"Your (?:completed |subdued )?(.+?) (?:kill |chest |completion |success )?count is: ([\\d,]+)");
	private static final int[] KC_MARKS = {50, 100, 250, 500, 1000, 2000, 5000};
	private static final int[] TOTAL_MARKS = {500, 750, 1000, 1250, 1500, 1750, 2000, 2100, 2200};
	private static final int[] QUEST_MARKS = {25, 50, 75, 100};
	private static final long[] XP_MARKS = {1_000_000L, 5_000_000L, 25_000_000L, 50_000_000L, 100_000_000L, 200_000_000L};
	private static final String[] XP_LABELS = {"1M", "5M", "25M", "50M", "100M", "200M"};

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ConfigManager configManager;

	@Inject
	private Notifier notifier;

	@Inject
	private NextStepConfig config;

	@Inject
	private CelebrationOverlay celebrationOverlay;

	@Inject
	private GoalOverlay goalOverlay;

	@Inject
	private Navigator navigator;

	private NextStepPanel panel;
	private NavigationButton navButton;
	private List<Suggestion> everything = new ArrayList<>();
	private int questChatRefreshIn = -1;

	// Drops & milestones
	private int tickCount;
	private int lastPetTick = -100;
	private final Map<Skill, Integer> lastXp = new EnumMap<>(Skill.class);
	private final Map<String, int[]> lastKc = new HashMap<>(); // boss -> {kc, tick}
	private final List<Object[]> pendingDrops = new ArrayList<>(); // {item, tick}
	private int prevTotalLevel = -1;
	private int prevQuestPct = -1;

	private final Map<Skill, Integer> lastLevels = new EnumMap<>(Skill.class);
	private boolean dirty = true;
	private int ticksSinceRefresh;
	private int ticksSinceLogin;

	// Session state used to detect what just changed
	private boolean baselineSet;
	private Set<String> prevDone = new HashSet<>();
	private Set<String> prevReady = new HashSet<>();
	private Map<String, Integer> prevGaps = new HashMap<>();
	private Rank prevRank;
	private final Set<String> nudged = new HashSet<>();
	private int sessionUnlocks;
	private int questsDone;
	private int questsTotal;

	private volatile EvaluatedSuggestion pinnedEval;

	// Navigation
	private Suggestion guideTarget;
	private boolean arrivedAnnounced;
	private String autoGuidedFor;
	private volatile GuideInfo guideInfo;

	@Provides
	NextStepConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(NextStepConfig.class);
	}

	@Override
	protected void startUp()
	{
		List<Suggestion> raw = new ArrayList<>(SuggestionRepository.all());
		raw.addAll(GuideRepository.all());
		everything = resolveAll(raw);
		resetSession();

		panel = new NextStepPanel(this);
		navButton = NavigationButton.builder()
			.tooltip("Next Step")
			.icon(ImageUtil.loadImageResource(getClass(), "panel_icon.png"))
			.priority(7)
			.panel(panel)
			.build();
		clientToolbar.addNavigation(navButton);
		overlayManager.add(celebrationOverlay);
		overlayManager.add(goalOverlay);

		clientThread.invokeLater(() -> refresh(true));
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(celebrationOverlay);
		overlayManager.remove(goalOverlay);
		celebrationOverlay.clear();
		clientThread.invoke(() ->
		{
			navigator.clear();
			guideTarget = null;
			guideInfo = null;
			autoGuidedFor = null;
		});
		clientToolbar.removeNavigation(navButton);
		panel = null;
		navButton = null;
		pinnedEval = null;
		lastLevels.clear();
	}

	// ======================= Events =======================

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		GameState state = event.getGameState();
		if (state == GameState.LOGGED_IN)
		{
			ticksSinceLogin = 0;
			dirty = true;
		}
		else if (state == GameState.LOGIN_SCREEN)
		{
			resetSession();
			lastLevels.clear();
			pinnedEval = null;
			navigator.clear();
			guideTarget = null;
			guideInfo = null;
			autoGuidedFor = null;
			NextStepPanel p = panel;
			if (p != null)
			{
				SwingUtilities.invokeLater(p::showLoggedOut);
			}
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		// StatChanged fires on every XP drop; only a level change matters here.
		Integer old = lastLevels.put(event.getSkill(), event.getLevel());
		if (old == null || old != event.getLevel())
		{
			dirty = true;
		}

		Integer oldXp = lastXp.put(event.getSkill(), event.getXp());
		if (!baselineSet || !config.milestones())
		{
			return;
		}
		String skillName = event.getSkill().getName();
		if (old != null && old < 99 && event.getLevel() >= 99)
		{
			announce(Celebration.level99(skillName));
		}
		if (oldXp != null)
		{
			for (int i = 0; i < XP_MARKS.length; i++)
			{
				if (oldXp < XP_MARKS[i] && event.getXp() >= XP_MARKS[i])
				{
					Celebration.Tier tier = i >= 4 ? Celebration.Tier.MEGA : i >= 2 ? Celebration.Tier.RARE : Celebration.Tier.NORMAL;
					announce(Celebration.xpMilestone(skillName, XP_LABELS[i], tier));
				}
			}
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (CONFIG_GROUP.equals(event.getGroup()))
		{
			dirty = true;
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() != ChatMessageType.GAMEMESSAGE && event.getType() != ChatMessageType.SPAM)
		{
			return;
		}
		String msg = Text.removeTags(event.getMessage());

		// Quests have no completion event; the game message is the fastest signal.
		if (msg.contains("completed a quest"))
		{
			questChatRefreshIn = QUEST_CHAT_REFRESH_DELAY;
			return;
		}

		if (msg.startsWith(CLOG_PREFIX))
		{
			String item = msg.substring(CLOG_PREFIX.length()).trim();
			// Wait a couple of ticks so the matching kill count message has arrived.
			pendingDrops.add(new Object[]{item, tickCount});
			return;
		}

		if (msg.startsWith("You have a funny feeling like you") || msg.startsWith("You feel something weird sneaking"))
		{
			lastPetTick = tickCount;
			if (config.pets())
			{
				announce(Celebration.pet());
			}
			return;
		}

		Matcher m = KC_PATTERN.matcher(msg);
		if (m.find())
		{
			String boss = m.group(1).trim();
			int kc;
			try
			{
				kc = Integer.parseInt(m.group(2).replace(",", ""));
			}
			catch (NumberFormatException e)
			{
				return;
			}
			lastKc.put(RareDropTable.key(boss), new int[]{kc, tickCount});
			if (!config.bossKills())
			{
				return;
			}
			if (kc == 1)
			{
				announce(Celebration.firstKill(boss));
				return;
			}
			for (int mark : KC_MARKS)
			{
				if (kc == mark)
				{
					announce(Celebration.killMilestone(boss, kc));
				}
			}
		}
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		ticksSinceLogin++;
		ticksSinceRefresh++;
		tickCount++;
		updateGuide();
		processPendingDrops();

		if (questChatRefreshIn >= 0 && questChatRefreshIn-- == 0)
		{
			refresh(true);
			return;
		}

		boolean settleNow = !baselineSet && ticksSinceLogin >= LOGIN_SETTLE_TICKS;
		if (settleNow || ticksSinceRefresh >= PERIODIC_REFRESH_TICKS)
		{
			refresh(true);
		}
		else if (dirty)
		{
			refresh(false);
		}
	}

	// ======================= Panel API =======================

	void requestRefresh()
	{
		clientThread.invokeLater(() -> refresh(true));
	}

	void previewCelebration()
	{
		clientThread.invokeLater(() ->
		{
			if (config.popups())
			{
				celebrationOverlay.enqueue(Celebration.preview());
			}
			else
			{
				chat("Celebration popups are turned off in the plugin settings.", Celebration.GOLD);
			}
		});
	}

	String getPinnedGoalName()
	{
		return config.pinnedGoal();
	}

	void setPinnedGoal(String name)
	{
		String old = config.pinnedGoal();
		if (name == null || name.isEmpty())
		{
			configManager.unsetConfiguration(CONFIG_GROUP, PINNED_KEY);
			if (old != null && !old.isEmpty())
			{
				clientThread.invokeLater(() ->
				{
					if (guideTarget != null && guideTarget.getName().equals(old))
					{
						stopGuideNow();
					}
				});
			}
		}
		else
		{
			configManager.setConfiguration(CONFIG_GROUP, PINNED_KEY, name);
			if (config.autoGuidePinned())
			{
				guideTo(name);
			}
		}
	}

	void guideTo(String name)
	{
		clientThread.invokeLater(() ->
		{
			for (Suggestion s : everything)
			{
				if (s.getName().equals(name))
				{
					startGuide(s);
					return;
				}
			}
		});
	}

	void stopGuide()
	{
		clientThread.invokeLater(this::stopGuideNow);
	}

	String getGuideName()
	{
		Suggestion g = guideTarget;
		return g == null ? null : g.getName();
	}

	GuideInfo getGuideInfo()
	{
		return guideInfo;
	}

	EvaluatedSuggestion getPinnedEval()
	{
		return pinnedEval;
	}

	// ======================= Core logic =======================

	/** Runs on the client thread (quest lookups execute client scripts). */
	private void refresh(boolean full)
	{
		dirty = false;
		if (full)
		{
			ticksSinceRefresh = 0;
		}

		NextStepPanel p = panel;
		if (p == null)
		{
			return;
		}

		Player local = client.getLocalPlayer();
		if (client.getGameState() != GameState.LOGGED_IN || local == null)
		{
			SwingUtilities.invokeLater(p::showLoggedOut);
			return;
		}

		Map<Quest, QuestState> cache = new HashMap<>();
		Function<Quest, QuestState> stateOf = q -> cache.computeIfAbsent(q, k -> k.getState(client));

		if (full || questsTotal == 0)
		{
			int done = 0;
			int total = 0;
			for (Quest q : Quest.values())
			{
				total++;
				if (stateOf.apply(q) == QuestState.FINISHED)
				{
					done++;
				}
			}
			questsDone = done;
			questsTotal = total;
		}

		int questPoints = client.getVarpValue(QUEST_POINTS_VARP);
		int combat = local.getCombatLevel();
		WorldPoint playerPos = local.getWorldLocation();

		List<EvaluatedSuggestion> evaluated = new ArrayList<>();
		for (Suggestion s : everything)
		{
			EvaluatedSuggestion e = evaluate(s, stateOf, questPoints, combat);
			int dist = Navigator.distance(playerPos, s.getLocation());
			if (dist >= 0)
			{
				e.setDistance(dist, Navigator.compassShort(playerPos, s.getLocation()));
			}
			evaluated.add(e);
		}

		// "all" = the Next Steps list (quests, PvM, featured guides); guides = every method & minigame.
		List<EvaluatedSuggestion> all = evaluated.stream()
			.filter(e -> e.getSuggestion().isNextStep())
			.collect(Collectors.toList());
		List<EvaluatedSuggestion> guides = evaluated.stream()
			.filter(e -> e.getSuggestion().isGuide())
			.collect(Collectors.toList());
		Map<Skill, Integer> levels = new EnumMap<>(Skill.class);
		for (EvaluatedSuggestion g : guides)
		{
			for (Skill skill : g.getSuggestion().getGuideSkills())
			{
				levels.computeIfAbsent(skill, client::getRealSkillLevel);
			}
		}

		int achieved = (int) all.stream().filter(EvaluatedSuggestion::isAchieved).count();
		Rank rank = Rank.forFraction(all.isEmpty() ? 0 : achieved / (double) all.size());

		Set<String> done = new HashSet<>();
		Set<String> ready = new HashSet<>();
		Map<String, Integer> gaps = new HashMap<>();
		for (EvaluatedSuggestion e : evaluated)
		{
			if (e.getStatus() == EvaluatedSuggestion.Status.DONE)
			{
				done.add(e.getName());
			}
			else
			{
				gaps.put(e.getName(), e.getLevelGap());
				if (e.getStatus() == EvaluatedSuggestion.Status.READY)
				{
					ready.add(e.getName());
				}
			}
		}

		String pinnedName = config.pinnedGoal();
		EvaluatedSuggestion pinned = null;
		for (EvaluatedSuggestion e : evaluated)
		{
			if (e.getName().equals(pinnedName))
			{
				pinned = e;
				break;
			}
		}

		int totalLevel = client.getTotalLevel();
		int questPct = questsTotal > 0 ? questsDone * 100 / questsTotal : 0;
		if (baselineSet)
		{
			celebrateChanges(evaluated, pinnedName, rank);
			sendNudges(all);
			if (config.milestones())
			{
				for (int mark : TOTAL_MARKS)
				{
					if (prevTotalLevel > 0 && prevTotalLevel < mark && totalLevel >= mark)
					{
						announce(Celebration.totalLevel(mark));
					}
				}
				if (full)
				{
					for (int mark : QUEST_MARKS)
					{
						if (prevQuestPct >= 0 && prevQuestPct < mark && questPct >= mark)
						{
							announce(Celebration.questProgress(mark));
						}
					}
				}
			}
		}
		prevTotalLevel = totalLevel;
		if (full)
		{
			prevQuestPct = questPct;
		}
		else if (ticksSinceLogin >= LOGIN_SETTLE_TICKS)
		{
			baselineSet = true;
		}

		prevDone = done;
		prevReady = ready;
		prevGaps = gaps;
		prevRank = rank;

		// A finished goal un-pins itself (its celebration was already queued above).
		if (pinned != null && pinned.isAchieved())
		{
			if (guideTarget != null && guideTarget.getName().equals(pinned.getName()))
			{
				stopGuideNow();
			}
			setPinnedGoal(null);
			pinned = null;
		}
		pinnedEval = pinned;

		// Show the way to the pinned goal once per pin (the player can stop it).
		if (pinned != null && config.autoGuidePinned() && guideTarget == null
			&& !pinned.getName().equals(autoGuidedFor))
		{
			autoGuidedFor = pinned.getName();
			startGuide(pinned.getSuggestion());
		}

		int max = config.maxShown();
		List<EvaluatedSuggestion> visible = all.stream()
			.filter(e -> isTypeShown(e.getSuggestion().getType()))
			.collect(Collectors.toList());

		Comparator<EvaluatedSuggestion> byValue = Comparator.comparingInt(e -> e.getSuggestion().getPriority());
		Comparator<EvaluatedSuggestion> byDistance = Comparator
			.comparingInt((EvaluatedSuggestion e) -> e.getDistance() < 0 ? Integer.MAX_VALUE : e.getDistance())
			.thenComparing(byValue);
		List<EvaluatedSuggestion> readyList = visible.stream()
			.filter(e -> e.getStatus() == EvaluatedSuggestion.Status.READY)
			.sorted(config.sortByDistance() ? byDistance : byValue)
			.limit(max)
			.collect(Collectors.toList());
		List<EvaluatedSuggestion> almostList = visible.stream()
			.filter(e -> e.getStatus() == EvaluatedSuggestion.Status.ALMOST)
			.sorted(Comparator.comparingInt(EvaluatedSuggestion::getLevelGap)
				.thenComparingInt(e -> e.getSuggestion().getPriority()))
			.limit(max)
			.collect(Collectors.toList());
		List<EvaluatedSuggestion> laterList = visible.stream()
			.filter(e -> e.getStatus() == EvaluatedSuggestion.Status.LOCKED)
			.sorted(Comparator.comparingDouble(EvaluatedSuggestion::getProgress).reversed()
				.thenComparingInt(e -> e.getSuggestion().getPriority()))
			.limit(LATER_LIMIT)
			.collect(Collectors.toList());

		PanelData data = new PanelData(readyList, almostList, laterList, pinned, rank, achieved, all.size(),
			questsDone, questsTotal, sessionUnlocks, config.showRecommended(), getGuideName(), config.showDistance(),
			guides, levels);
		SwingUtilities.invokeLater(() -> p.update(data));
	}

	private EvaluatedSuggestion evaluate(Suggestion s, Function<Quest, QuestState> stateOf, int questPoints, int combat)
	{
		boolean inProgress = false;
		if (s.getQuest() != null)
		{
			QuestState qs = stateOf.apply(s.getQuest());
			if (qs == QuestState.FINISHED)
			{
				return new EvaluatedSuggestion(s, EvaluatedSuggestion.Status.DONE, 0,
					new ArrayList<>(), new ArrayList<>(), 1.0, false);
			}
			inProgress = qs == QuestState.IN_PROGRESS;
		}

		List<String> missing = new ArrayList<>();
		List<String> recWarnings = new ArrayList<>();
		int levelGap = 0;
		boolean hardBlock = false;
		double progressSum = 0;
		int parts = 0;

		for (Map.Entry<Skill, Integer> req : s.getSkillReqs().entrySet())
		{
			int need = req.getValue();
			int have = client.getRealSkillLevel(req.getKey());
			parts++;
			progressSum += Math.min(1.0, have / (double) need);
			if (have < need)
			{
				levelGap += need - have;
				missing.add(need + " " + req.getKey().getName() + " (you: " + have + ")");
			}
		}

		if (s.getCombatReq() > 0)
		{
			parts++;
			progressSum += Math.min(1.0, combat / (double) s.getCombatReq());
			if (combat < s.getCombatReq())
			{
				levelGap += s.getCombatReq() - combat;
				missing.add(s.getCombatReq() + " Combat (you: " + combat + ")");
			}
		}

		if (s.getCombinedSkills() != null && s.getCombinedTotal() > 0)
		{
			int sum = 0;
			List<String> names = new ArrayList<>();
			for (Skill sk : s.getCombinedSkills())
			{
				sum += client.getRealSkillLevel(sk);
				names.add(sk.getName());
			}
			parts++;
			progressSum += Math.min(1.0, sum / (double) s.getCombinedTotal());
			if (sum < s.getCombinedTotal())
			{
				levelGap += s.getCombinedTotal() - sum;
				missing.add(s.getCombinedTotal() + " combined " + String.join(" + ", names) + " (you: " + sum + ")");
			}
		}

		for (Quest q : s.getQuestReqs())
		{
			parts++;
			if (stateOf.apply(q) == QuestState.FINISHED)
			{
				progressSum += 1;
			}
			else
			{
				hardBlock = true;
				missing.add("Quest: " + q.getName());
			}
		}

		if (s.getQuestPointReq() > 0)
		{
			parts++;
			progressSum += Math.min(1.0, questPoints / (double) s.getQuestPointReq());
			if (questPoints < s.getQuestPointReq())
			{
				hardBlock = true;
				missing.add(s.getQuestPointReq() + " quest points (you: " + questPoints + ")");
			}
		}

		for (Map.Entry<Skill, Integer> rec : s.getRecommended().entrySet())
		{
			int have = client.getRealSkillLevel(rec.getKey());
			if (have < rec.getValue())
			{
				recWarnings.add(rec.getValue() + " " + rec.getKey().getName() + " (you: " + have + ")");
			}
		}

		double progress = parts == 0 ? 1.0 : progressSum / parts;

		EvaluatedSuggestion.Status status;
		if (missing.isEmpty())
		{
			status = EvaluatedSuggestion.Status.READY;
		}
		else if (!hardBlock && levelGap <= config.almostThreshold())
		{
			status = EvaluatedSuggestion.Status.ALMOST;
		}
		else
		{
			status = EvaluatedSuggestion.Status.LOCKED;
		}

		return new EvaluatedSuggestion(s, status, levelGap, missing, recWarnings, progress, inProgress);
	}

	private void celebrateChanges(List<EvaluatedSuggestion> all, String pinnedName, Rank rank)
	{
		List<Celebration> items = new ArrayList<>();
		Celebration goal = null;

		for (EvaluatedSuggestion e : all)
		{
			String name = e.getName();
			boolean newlyDone = e.getStatus() == EvaluatedSuggestion.Status.DONE && !prevDone.contains(name);
			boolean newlyReady = e.getStatus() == EvaluatedSuggestion.Status.READY
				&& !prevReady.contains(name) && !prevDone.contains(name);
			if (!newlyDone && !newlyReady)
			{
				continue;
			}
			boolean guideOnly = e.getSuggestion().isGuide() && !e.getSuggestion().isNextStep();
			if (guideOnly && !config.celebrateGuides() && !name.equals(pinnedName))
			{
				continue;
			}

			boolean goalHit = name.equals(pinnedName)
				&& (newlyDone || e.getSuggestion().getType() != Suggestion.Type.QUEST);
			if (goalHit)
			{
				goal = Celebration.goal(e.getSuggestion());
			}
			else if (newlyDone)
			{
				items.add(Celebration.questDone(e.getSuggestion()));
			}
			else
			{
				items.add(Celebration.unlocked(e.getSuggestion()));
			}
		}

		sessionUnlocks += items.size() + (goal != null ? 1 : 0);

		if (goal != null)
		{
			announce(goal);
		}
		if (items.size() > MAX_INDIVIDUAL_POPUPS)
		{
			announce(Celebration.batch(items.size()));
		}
		else
		{
			items.forEach(this::announce);
		}
		if (prevRank != null && rank.ordinal() > prevRank.ordinal())
		{
			announce(Celebration.rankUp(rank));
		}
	}

	private void sendNudges(List<EvaluatedSuggestion> all)
	{
		if (!config.nudges())
		{
			return;
		}
		for (EvaluatedSuggestion e : all)
		{
			if (e.getStatus() != EvaluatedSuggestion.Status.ALMOST || e.getLevelGap() != 1)
			{
				continue;
			}
			Integer prevGap = prevGaps.get(e.getName());
			if (prevGap != null && prevGap > 1 && nudged.add(e.getName()))
			{
				chat("Just 1 more level until " + e.getName() + "!", Celebration.BLUE);
			}
		}
	}

	// ---------------- Navigation internals (client thread) ----------------

	private void startGuide(Suggestion s)
	{
		guideTarget = s;
		arrivedAnnounced = false;
		navigator.navigateTo(s);
		updateGuide();
		if (s.getLocation() == null && s.getLocationName() != null)
		{
			chat("Head to " + s.getLocationName()
				+ (s.getTravelTip() != null ? " (" + s.getTravelTip() + ")" : ""), Celebration.BLUE);
		}
		dirty = true;
	}

	private void stopGuideNow()
	{
		navigator.clear();
		guideTarget = null;
		guideInfo = null;
		dirty = true;
	}

	private void updateGuide()
	{
		Suggestion g = guideTarget;
		if (g == null)
		{
			guideInfo = null;
			return;
		}
		Player local = client.getLocalPlayer();
		WorldPoint pos = local == null ? null : local.getWorldLocation();
		String directions = g.getLocation() == null ? null : Navigator.describe(pos, g.getLocation());

		int dist = Navigator.distance(pos, g.getLocation());
		if (dist >= 0 && dist <= 8 && !arrivedAnnounced)
		{
			arrivedAnnounced = true;
			chat("You've arrived: " + g.getLocationName(), Celebration.GREEN);
		}

		navigator.tick(pos);
		guideInfo = new GuideInfo(g.getName(), g.getLocationName(), g.getTravelTip(), directions);
	}

	void announcePick(String name)
	{
		clientThread.invokeLater(() -> announce(Celebration.wheel(name)));
	}

	private void processPendingDrops()
	{
		Iterator<Object[]> it = pendingDrops.iterator();
		while (it.hasNext())
		{
			Object[] drop = it.next();
			if (tickCount - (int) drop[1] < 2)
			{
				continue;
			}
			it.remove();
			handleCollectionLog((String) drop[0]);
		}
	}

	private void handleCollectionLog(String item)
	{
		// A pet popup already covers the pet's own collection log entry.
		if (tickCount - lastPetTick <= 5 || !config.collectionLog())
		{
			return;
		}
		Celebration.Tier tier = RareDropTable.tierFor(item);
		RareDropTable.Entry entry = RareDropTable.find(item);
		if (entry != null && config.luck())
		{
			int[] kc = lastKc.get(RareDropTable.key(entry.source));
			if (kc != null && tickCount - kc[1] <= 20 && kc[0] > 0)
			{
				if ((long) kc[0] * 4 <= entry.rate)
				{
					announce(Celebration.spooned(item, kc[0], entry.rate, tier));
					return;
				}
				if (kc[0] >= (long) entry.rate * 2)
				{
					announce(Celebration.finallyDrop(item, kc[0], entry.rate, tier));
					return;
				}
			}
		}
		announce(Celebration.collectionLog(item, tier));
	}

	private void announce(Celebration c)
	{
		if (config.popups())
		{
			celebrationOverlay.enqueue(c);
		}
		chat(c.getTitle() + " " + c.getSubtitle(), c.getColor());
		if (config.desktopNotify())
		{
			notifier.notify("Next Step: " + c.getTitle() + " " + c.getSubtitle());
		}
	}

	private void chat(String text, Color color)
	{
		if (!config.chatMessages() || client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "",
			ColorUtil.wrapWithColorTag("[Next Step] ", color) + text, null);
	}

	private boolean isTypeShown(Suggestion.Type type)
	{
		switch (type)
		{
			case QUEST:
				return config.showQuests();
			case METHOD:
			case MINIGAME:
				return config.showTraining();
			case PVM:
				return config.showPvm();
			default:
				return true;
		}
	}

	private void resetSession()
	{
		baselineSet = false;
		prevDone = new HashSet<>();
		prevReady = new HashSet<>();
		prevGaps = new HashMap<>();
		prevRank = null;
		nudged.clear();
		sessionUnlocks = 0;
		questsDone = 0;
		questsTotal = 0;
		ticksSinceLogin = 0;
		lastXp.clear();
		lastKc.clear();
		pendingDrops.clear();
		prevTotalLevel = -1;
		prevQuestPct = -1;
		lastPetTick = -100;
		celebrationOverlay.clear();
	}

	private static List<Suggestion> resolveAll(List<Suggestion> raw)
	{
		List<Suggestion> ok = new ArrayList<>();
		Set<String> names = new HashSet<>();
		for (Suggestion s : raw)
		{
			if (!names.add(s.getName()))
			{
				log.warn("Next Step: duplicate name '{}', skipping the second one", s.getName());
				continue;
			}
			if (s.resolve())
			{
				ok.add(s);
			}
		}
		log.debug("Next Step loaded {} of {} suggestions", ok.size(), raw.size());
		return ok;
	}
}
