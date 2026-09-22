package com.nextstep;

import java.util.Arrays;
import java.util.List;
import net.runelite.api.Skill;

/**
 * Quests and PvM goals. Training methods and minigames live in GuideRepository.
 * Every rule the plugin knows about. Lower priority number = shown first.
 *
 * Locations: .at("Name", x, y) puts a marker on the map. Coordinates are
 * approximate start points; use RuneLite's Developer Tools "Location" overlay
 * (or the World Location plugin) to fine-tune any that are off.
 * .at("Name") with no coordinates is used for places reached by teleport
 * or underground, where a surface marker would be misleading.
 */
public final class SuggestionRepository
{
	private SuggestionRepository()
	{
	}

	public static List<Suggestion> all()
	{
		return Arrays.asList(
			// ================= QUESTS: early high-value =================
			Suggestion.quest("Waterfall Quest",
				"13,750 Attack & Strength XP, even at level 1", 10)
				.at("Almera's house, near Baxtorian Falls", 2521, 3495)
				.travel("Games necklace to Barbarian Outpost, then walk south"),
			Suggestion.quest("Priest in Peril",
				"Unlocks Morytania: Canifis, Barrows, Slayer Tower", 12)
				.at("King Roald, Varrock Palace", 3222, 3473)
				.travel("Varrock teleport"),
			Suggestion.quest("Druidic Ritual",
				"Unlocks the Herblore skill", 14)
				.at("Kaqemeex, Taverley stone circle", 2925, 3486)
				.travel("Falador teleport, walk west to Taverley"),
			Suggestion.quest("Rune Mysteries",
				"Unlocks the Runecraft skill", 14)
				.at("Duke Horacio, Lumbridge Castle (1st floor)", 3210, 3220, 1)
				.travel("Lumbridge Home Teleport, go upstairs"),
			Suggestion.quest("The Knight's Sword",
				"12,725 Smithing XP", 18)
				.skill(Skill.MINING, 10)
				.at("The Squire, Falador Castle courtyard", 2977, 3343)
				.travel("Falador teleport"),
			Suggestion.quest("Fight Arena",
				"12,175 Attack XP", 20)
				.at("Lady Servil, north-west of the Fight Arena", 2567, 3196)
				.travel("Ardougne teleport, walk south"),
			Suggestion.quest("Tree Gnome Village",
				"11,450 Attack XP, needed for Monkey Madness", 20)
				.at("King Bolren, Tree Gnome Village", 2541, 3169)
				.travel("Spirit tree to Tree Gnome Village"),
			Suggestion.quest("Witch's House",
				"6,325 Hitpoints XP", 22)
				.at("Boy outside the Witch's house, Taverley", 2928, 3456)
				.travel("Falador teleport, walk west"),
			Suggestion.quest("Lost City",
				"Zanaris, dragon longsword & dagger", 24)
				.skill(Skill.CRAFTING, 31)
				.skill(Skill.WOODCUTTING, 36)
				.at("Adventurers' camp, Lumbridge Swamp", 3149, 3205)
				.travel("Lumbridge Home Teleport, walk south"),
			Suggestion.quest("Client of Kourend",
				"Two XP lamps and a start in Kourend", 24)
				.at("Veos, Port Sarim docks", 3054, 3246)
				.travel("Walk south from Draynor Village"),
			Suggestion.quest("The Grand Tree",
				"Gnome glider travel network", 26)
				.skill(Skill.AGILITY, 25)
				.at("King Narnode, Grand Tree", 2465, 3495)
				.travel("Spirit tree to the Gnome Stronghold"),
			Suggestion.quest("Temple of the Eye",
				"Unlocks Guardians of the Rift (also needs Enter the Abyss)", 26)
				.skill(Skill.RUNECRAFT, 10)
				.requires("Rune Mysteries")
				.at("Wizards' Tower", 3109, 3162)
				.travel("Necklace of passage, or walk south from Draynor"),
			Suggestion.quest("The Tourist Trap",
				"Two XP rewards in skills you choose", 28)
				.skill(Skill.FLETCHING, 10)
				.skill(Skill.SMITHING, 20)
				.at("Irena, Shantay Pass", 3304, 3112)
				.travel("Walk south from Al Kharid"),

			// ================= QUESTS: mid-game unlocks =================
			Suggestion.quest("Animal Magnetism",
				"Ava's accumulator: saves and picks up arrows", 28)
				.skill(Skill.SLAYER, 18)
				.skill(Skill.CRAFTING, 19)
				.skill(Skill.RANGED, 30)
				.skill(Skill.WOODCUTTING, 35)
				.requires("Ernest the Chicken", "Priest in Peril", "The Restless Ghost")
				.at("Ava, Draynor Manor", 3093, 3357)
				.travel("Walk north from Draynor Village"),
			Suggestion.quest("Nature Spirit",
				"Mort Myre access and the blessed silver sickle", 30)
				.requires("Priest in Peril", "The Restless Ghost")
				.at("Drezel, beneath the Paterdomus temple", 3406, 3488)
				.travel("Varrock teleport, walk east to the temple"),
			Suggestion.quest("Dragon Slayer I",
				"Wear rune platebody and green d'hide body", 30)
				.questPoints(32)
				.at("Guildmaster, Champions' Guild", 3190, 3360)
				.travel("Varrock teleport, walk south-west"),
			Suggestion.quest("Monkey Madness I",
				"Dragon scimitar and big combat XP", 30)
				.requires("The Grand Tree", "Tree Gnome Village")
				.at("King Narnode, Grand Tree", 2465, 3495)
				.travel("Spirit tree to the Gnome Stronghold"),
			Suggestion.quest("Eagles' Peak",
				"Unlocks box traps, the path to chinchompas", 30)
				.skill(Skill.HUNTER, 27)
				.at("Charlie, Ardougne Zoo", 2607, 3266)
				.travel("Ardougne teleport, walk south"),
			Suggestion.quest("Sleeping Giants",
				"Unlocks Giants' Foundry", 30)
				.skill(Skill.SMITHING, 15)
				.at("Kovac, Giants' Foundry")
				.travel("Grouping (minigame) teleport to Giants' Foundry"),
			Suggestion.quest("Ghosts Ahoy",
				"Ectophial: unlimited teleport to Port Phasmatys", 32)
				.skill(Skill.AGILITY, 25)
				.skill(Skill.COOKING, 20)
				.requires("Priest in Peril", "The Restless Ghost")
				.at("Velorina, Port Phasmatys", 3676, 3490)
				.travel("Walk east from Canifis"),
			Suggestion.quest("The Dig Site",
				"Digsite pendant, and the road to Fossil Island", 34)
				.skill(Skill.AGILITY, 10)
				.skill(Skill.HERBLORE, 10)
				.skill(Skill.THIEVING, 25)
				.at("Examiner, Exam Centre", 3361, 3339)
				.travel("Varrock teleport, walk east"),
			Suggestion.quest("Bone Voyage",
				"Fossil Island (also needs 100 museum kudos)", 34)
				.requires("The Dig Site")
				.at("Varrock Museum", 3255, 3448)
				.travel("Varrock teleport, walk north-east"),
			Suggestion.quest("Horror from the Deep",
				"God books and a common quest requirement", 38)
				.skill(Skill.AGILITY, 35)
				.at("Larrissa, lighthouse north of Barbarian Outpost", 2507, 3634)
				.travel("Games necklace to Barbarian Outpost, walk north"),
			Suggestion.quest("The Fremennik Trials",
				"Rellekka, Fremennik helms, path to Lunar spells", 38)
				.at("Brundt, Rellekka longhall", 2658, 3670)
				.travel("Head north from Seers' Village"),
			Suggestion.quest("The Restless Ghost",
				"1,125 Prayer XP", 40)
				.at("Father Aereck, Lumbridge church", 3243, 3207)
				.travel("Lumbridge Home Teleport"),

			// ================= PvM =================
			Suggestion.pvm("Void Knight equipment",
				"Big accuracy/damage set, earned at Pest Control", 35)
				.combat(40)
				.skill(Skill.ATTACK, 42)
				.skill(Skill.STRENGTH, 42)
				.skill(Skill.DEFENCE, 42)
				.skill(Skill.HITPOINTS, 42)
				.skill(Skill.RANGED, 42)
				.skill(Skill.MAGIC, 42)
				.skill(Skill.PRAYER, 22)
				.at("Pest Control boat, Port Sarim", 3041, 3202)
				.travel("Grouping (minigame) teleport"),
			Suggestion.pvm("Barrows",
				"Barrows armour and a steady rune supply", 45)
				.requires("Priest in Peril")
				.recommend(Skill.MAGIC, 70)
				.recommend(Skill.DEFENCE, 70)
				.recommend(Skill.PRAYER, 43)
				.at("Barrows", 3565, 3289)
				.travel("Barrows teleport tablet"),
			Suggestion.pvm("Giant Mole",
				"Beginner-friendly boss", 60)
				.recommend(Skill.ATTACK, 60)
				.recommend(Skill.STRENGTH, 60)
				.recommend(Skill.DEFENCE, 60)
				.at("Falador Park mole hills", 2988, 3383)
				.travel("Falador teleport; bring a light source and spade")
		);
	}
}
