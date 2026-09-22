package com.nextstep;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.runelite.api.Quest;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * One thing the player could do: a quest, a training method, a minigame, or a PvM goal.
 * Built with a fluent API in SuggestionRepository and GuideRepository.
 */
public class Suggestion
{
	private static final Logger log = LoggerFactory.getLogger(Suggestion.class);

	public enum Type
	{
		QUEST("Quest"),
		METHOD("Method"),
		MINIGAME("Minigame"),
		PVM("PvM");

		private final String label;

		Type(String label)
		{
			this.label = label;
		}

		public String getLabel()
		{
			return label;
		}
	}

	public enum Tag
	{
		FAST("Fast XP"),
		AFK("AFK"),
		PROFIT("Profit"),
		CHEAP("Cheap"),
		COSTLY("Costs GP");

		private final String label;

		Tag(String label)
		{
			this.label = label;
		}

		public String getLabel()
		{
			return label;
		}
	}

	private final String name;
	private final String reward;
	private final Type type;
	private final String questName; // only for QUEST
	private int priority;           // lower = more valuable (Next Steps ranking)
	private boolean featured;       // guides that also appear in Next Steps
	private String wikiPage;

	private WorldPoint location;
	private String locationName;
	private String travelTip;

	// Guide info
	private final Set<Skill> guideSkills = EnumSet.noneOf(Skill.class);
	private int levelFrom = 1;
	private final Set<Tag> tags = EnumSet.noneOf(Tag.class);

	// Requirements
	private final Map<Skill, Integer> skillReqs = new EnumMap<>(Skill.class);
	private final Map<Skill, Integer> recommended = new EnumMap<>(Skill.class);
	private final List<String> questReqNames = new ArrayList<>();
	private int questPointReq;
	private int combatReq;
	private Skill[] combinedSkills;
	private int combinedTotal;
	private String note; // extra requirement we can't check automatically

	// Filled in by resolve()
	private Quest quest;
	private final List<Quest> questReqs = new ArrayList<>();

	private Suggestion(String name, String reward, Type type, String questName, int priority)
	{
		this.name = name;
		this.reward = reward;
		this.type = type;
		this.questName = questName;
		this.priority = priority;
		this.wikiPage = name;
	}

	// ---------------- Factories ----------------

	public static Suggestion quest(String questName, String reward, int priority)
	{
		return new Suggestion(questName, reward, Type.QUEST, questName, priority);
	}

	public static Suggestion pvm(String name, String reward, int priority)
	{
		return new Suggestion(name, reward, Type.PVM, null, priority);
	}

	/** A training method for a skill. The level is a hard requirement in that skill. */
	public static Suggestion method(Skill skill, int level, String name, String reward, Tag... tags)
	{
		Suggestion s = new Suggestion(name, reward, Type.METHOD, null, 100);
		s.guideSkills.add(skill);
		s.levelFrom = level;
		s.wikiPage = "Pay-to-play " + skill.getName() + " training";
		if (level > 1)
		{
			s.skillReqs.put(skill, level);
		}
		Collections.addAll(s.tags, tags);
		return s;
	}

	public static Suggestion minigame(String name, String reward, Tag... tags)
	{
		Suggestion s = new Suggestion(name, reward, Type.MINIGAME, null, 100);
		Collections.addAll(s.tags, tags);
		return s;
	}

	// ---------------- Builders ----------------

	public Suggestion skill(Skill skill, int level)
	{
		skillReqs.put(skill, level);
		if (type == Type.MINIGAME && guideSkills.isEmpty())
		{
			levelFrom = level;
		}
		return this;
	}

	public Suggestion recommend(Skill skill, int level)
	{
		recommended.put(skill, level);
		return this;
	}

	public Suggestion requires(String... questNames)
	{
		Collections.addAll(questReqNames, questNames);
		return this;
	}

	public Suggestion questPoints(int qp)
	{
		questPointReq = qp;
		return this;
	}

	public Suggestion combat(int level)
	{
		combatReq = level;
		return this;
	}

	/** e.g. Warriors' Guild: Attack + Strength >= 130. */
	public Suggestion combined(int total, Skill... skills)
	{
		combinedTotal = total;
		combinedSkills = skills;
		return this;
	}

	/** Also list this guide under other skills' tabs. */
	public Suggestion alsoFor(Skill... skills)
	{
		Collections.addAll(guideSkills, skills);
		return this;
	}

	/** Also show this guide in the Next Steps list with the given priority. */
	public Suggestion featured(int priority)
	{
		this.featured = true;
		this.priority = priority;
		return this;
	}

	public Suggestion note(String note)
	{
		this.note = note;
		return this;
	}

	public Suggestion at(String name, int x, int y)
	{
		return at(name, x, y, 0);
	}

	public Suggestion at(String name, int x, int y, int plane)
	{
		this.locationName = name;
		this.location = new WorldPoint(x, y, plane);
		return this;
	}

	public Suggestion at(String name)
	{
		this.locationName = name;
		return this;
	}

	public Suggestion travel(String tip)
	{
		this.travelTip = tip;
		return this;
	}

	public Suggestion wiki(String page)
	{
		wikiPage = page;
		return this;
	}

	/** Maps quest names to RuneLite quests. Returns false if this suggestion can't be used. */
	boolean resolve()
	{
		if (questName != null)
		{
			quest = QuestLookup.find(questName);
			if (quest == null)
			{
				log.warn("Next Step: unknown quest '{}', skipping suggestion", questName);
				return false;
			}
		}
		for (String req : questReqNames)
		{
			Quest q = QuestLookup.find(req);
			if (q == null)
			{
				log.warn("Next Step: unknown prerequisite quest '{}' on '{}', ignoring it", req, name);
			}
			else
			{
				questReqs.add(q);
			}
		}
		return true;
	}

	/** Shown in the Next Steps list (quests, PvM, and featured guides). */
	public boolean isNextStep()
	{
		return type == Type.QUEST || type == Type.PVM || featured;
	}

	public boolean isGuide()
	{
		return type == Type.METHOD || type == Type.MINIGAME;
	}

	// ---------------- Getters ----------------

	public String getName()
	{
		return name;
	}

	public String getReward()
	{
		return reward;
	}

	public Type getType()
	{
		return type;
	}

	public Quest getQuest()
	{
		return quest;
	}

	public int getPriority()
	{
		return priority;
	}

	public String getWikiPage()
	{
		return wikiPage;
	}

	public WorldPoint getLocation()
	{
		return location;
	}

	public String getLocationName()
	{
		return locationName;
	}

	public String getTravelTip()
	{
		return travelTip;
	}

	public Set<Skill> getGuideSkills()
	{
		return guideSkills;
	}

	public int getLevelFrom()
	{
		return levelFrom;
	}

	public Set<Tag> getTags()
	{
		return tags;
	}

	public Map<Skill, Integer> getSkillReqs()
	{
		return skillReqs;
	}

	public Map<Skill, Integer> getRecommended()
	{
		return recommended;
	}

	public List<Quest> getQuestReqs()
	{
		return questReqs;
	}

	public int getQuestPointReq()
	{
		return questPointReq;
	}

	public int getCombatReq()
	{
		return combatReq;
	}

	public Skill[] getCombinedSkills()
	{
		return combinedSkills;
	}

	public int getCombinedTotal()
	{
		return combinedTotal;
	}

	public String getNote()
	{
		return note;
	}
}
