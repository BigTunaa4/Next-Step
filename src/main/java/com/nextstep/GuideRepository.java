package com.nextstep;

import static com.nextstep.Suggestion.Tag.AFK;
import static com.nextstep.Suggestion.Tag.CHEAP;
import static com.nextstep.Suggestion.Tag.COSTLY;
import static com.nextstep.Suggestion.Tag.FAST;
import static com.nextstep.Suggestion.Tag.PROFIT;
import static com.nextstep.Suggestion.method;
import static com.nextstep.Suggestion.minigame;
import static net.runelite.api.Skill.AGILITY;
import static net.runelite.api.Skill.ATTACK;
import static net.runelite.api.Skill.COOKING;
import static net.runelite.api.Skill.CONSTRUCTION;
import static net.runelite.api.Skill.CRAFTING;
import static net.runelite.api.Skill.DEFENCE;
import static net.runelite.api.Skill.FARMING;
import static net.runelite.api.Skill.FIREMAKING;
import static net.runelite.api.Skill.FISHING;
import static net.runelite.api.Skill.FLETCHING;
import static net.runelite.api.Skill.HERBLORE;
import static net.runelite.api.Skill.HITPOINTS;
import static net.runelite.api.Skill.HUNTER;
import static net.runelite.api.Skill.MAGIC;
import static net.runelite.api.Skill.MINING;
import static net.runelite.api.Skill.PRAYER;
import static net.runelite.api.Skill.RANGED;
import static net.runelite.api.Skill.RUNECRAFT;
import static net.runelite.api.Skill.SLAYER;
import static net.runelite.api.Skill.SMITHING;
import static net.runelite.api.Skill.STRENGTH;
import static net.runelite.api.Skill.THIEVING;
import static net.runelite.api.Skill.WOODCUTTING;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Training methods for every skill, and minigames.
 *
 * method(skill, level, name, summary, tags...) - level is a hard requirement.
 * .alsoFor(...) lists the guide under other skills' tabs too.
 * .featured(priority) also puts it in the Next Steps list.
 *
 * Names must be unique across all guides, quests and PvM entries.
 * Levels and requirements should be checked against the OSRS Wiki before publishing.
 * Coordinates are approximate; places without coordinates show text directions only.
 */
public final class GuideRepository
{
	private GuideRepository()
	{
	}

	public static List<Suggestion> all()
	{
		List<Suggestion> all = new ArrayList<>();
		all.addAll(melee());
		all.addAll(rangedMagicPrayer());
		all.addAll(artisan());
		all.addAll(gathering());
		all.addAll(supportSkills());
		all.addAll(minigames());
		return all;
	}

	// ======================= Attack / Strength / Defence / Hitpoints =======================
	private static List<Suggestion> melee()
	{
		return Arrays.asList(
			method(ATTACK, 1, "Lumbridge cows", "Safe starter training; bank the cowhides for Crafting", CHEAP)
				.alsoFor(STRENGTH, DEFENCE, HITPOINTS)
				.at("Lumbridge cow field", 3257, 3268)
				.travel("Lumbridge Home Teleport, walk north-east"),
			method(ATTACK, 1, "Sand Crabs", "Low-defence crabs you can AFK about 10 minutes at a time", AFK, CHEAP)
				.alsoFor(STRENGTH, DEFENCE, HITPOINTS, RANGED, MAGIC)
				.at("Hosidius south coast, Kourend"),
			method(ATTACK, 1, "Hill Giants", "Big bones for Prayer and giant keys for Obor", PROFIT)
				.alsoFor(STRENGTH, DEFENCE, HITPOINTS)
				.recommend(ATTACK, 20)
				.at("Edgeville Dungeon (bring a brass key)", 3097, 3468)
				.travel("Amulet of glory to Edgeville"),
			method(ATTACK, 1, "Ammonite Crabs", "The best crab XP; very AFK", AFK)
				.alsoFor(STRENGTH, DEFENCE, HITPOINTS, RANGED)
				.recommend(ATTACK, 50)
				.requires("Bone Voyage")
				.at("Fossil Island")
				.travel("Digsite pendant to Fossil Island"),
			method(ATTACK, 1, "Gemstone Crab", "Huge-HP crab; long, relaxed kills", AFK)
				.alsoFor(STRENGTH, DEFENCE, HITPOINTS, RANGED, MAGIC)
				.at("Varlamore (check the wiki for current spawn spots)")
				.wiki("Gemstone Crab")
		);
	}

	// ======================= Ranged / Magic / Prayer =======================
	private static List<Suggestion> rangedMagicPrayer()
	{
		return Arrays.asList(
			// ---- Ranged ----
			method(RANGED, 1, "Chickens with a shortbow", "Easy first levels; bank feathers for Fletching", CHEAP)
				.at("Lumbridge chicken farm", 3230, 3298)
				.travel("Lumbridge Home Teleport, walk north"),
			method(RANGED, 1, "Dwarf multicannon", "Big boost for Slayer tasks and multi-combat", FAST, COSTLY)
				.requires("Dwarf Cannon")
				.at("Nulodion, Dwarven Mine near Ice Mountain", 3012, 3453)
				.wiki("Dwarf multicannon"),
			method(RANGED, 45, "Chinning", "Fastest Ranged XP: chinchompas in multi-combat", FAST, COSTLY)
				.at("Multi-combat areas, e.g. the Kourend Catacombs")
				.wiki("Chinchompa (weapon)"),

			// ---- Magic ----
			method(MAGIC, 1, "Strike spells", "The first few Magic levels on chickens or cows", CHEAP)
				.at("Lumbridge chicken farm", 3230, 3298),
			method(MAGIC, 1, "Splashing", "AFK Magic with a -65 magic attack bonus", AFK, CHEAP)
				.at("Any low-level monster, e.g. Lumbridge"),
			method(MAGIC, 43, "Superheat Item", "Magic and Smithing XP at the same time", COSTLY)
				.alsoFor(SMITHING),
			method(MAGIC, 55, "High Level Alchemy", "Steady XP, often break-even or profit", AFK, PROFIT)
				.wiki("High Level Alchemy"),
			method(MAGIC, 70, "Bursting and barraging", "Fastest Magic XP on multi-combat Slayer tasks", FAST, COSTLY)
				.requires("Desert Treasure I")
				.at("Kourend Catacombs"),
			method(MAGIC, 86, "Plank Make", "Magic XP and planks for Construction", PROFIT)
				.alsoFor(CONSTRUCTION)
				.requires("Lunar Diplomacy")
				.wiki("Plank Make"),

			// ---- Prayer ----
			method(PRAYER, 1, "Burying big bones", "Simple early Prayer; hill giants drop them", CHEAP),
			method(PRAYER, 1, "Gilded altar", "3.5x bone XP at a house party (World 330)", FAST, COSTLY)
				.at("Rimmington house portal", 2953, 3224)
				.travel("Teleport to House (Rimmington), or walk from Port Sarim"),
			method(PRAYER, 1, "Ectofuntus", "4x bone XP, cheaper per XP than the altar", COSTLY)
				.requires("Priest in Peril")
				.at("Ectofuntus, Port Phasmatys", 3660, 3520)
				.travel("Ectophial (from Ghosts Ahoy)"),
			method(PRAYER, 1, "Chaos Temple (Wilderness)", "3.5x XP and a 50% chance to keep each bone; risky", FAST)
				.at("Chaos Temple, level 38 Wilderness")
				.wiki("Chaos Temple (Wilderness)")
		);
	}

	// ======================= Artisan skills =======================
	private static List<Suggestion> artisan()
	{
		return Arrays.asList(
			// ---- Cooking ----
			method(COOKING, 1, "Cooking shrimp", "First levels on the Lumbridge range (burns less)", CHEAP)
				.at("Lumbridge Castle kitchen", 3211, 3215),
			method(COOKING, 15, "Cooking trout and salmon", "Cheap early levels; salmon at 25", CHEAP)
				.at("Rogues' Den fire, Burthorpe", 2905, 3537),
			method(COOKING, 30, "1-tick karambwans", "Very fast XP; click-intensive", FAST)
				.note("Needs the karambwan cooking lesson from Tai Bwo Wannai Trio")
				.at("Rogues' Den fire, Burthorpe", 2905, 3537),
			method(COOKING, 35, "Jugs of wine", "Very fast XP; bank-standing, no burning", FAST, COSTLY)
				.at("Grand Exchange", 3164, 3487),
			method(COOKING, 40, "Cooking lobsters", "Solid XP; sellable food", PROFIT)
				.at("Hosidius kitchen, Kourend"),
			method(COOKING, 80, "Cooking sharks", "Good XP, high-value food", PROFIT)
				.at("Hosidius kitchen, Kourend"),

			// ---- Smithing ----
			method(SMITHING, 1, "Bronze bars and items", "First levels; smelt at the Lumbridge furnace", CHEAP)
				.at("Lumbridge furnace", 3226, 3254),
			method(SMITHING, 33, "Platebodies at Varrock", "Iron at 33, steel at 48, mithril at 68", CHEAP)
				.at("Varrock west anvils", 3188, 3426)
				.travel("Varrock teleport"),
			method(SMITHING, 35, "Cannonballs", "Very AFK and usually profitable", AFK, PROFIT)
				.requires("Dwarf Cannon")
				.at("Edgeville furnace", 3108, 3499)
				.travel("Amulet of glory to Edgeville"),
			method(SMITHING, 40, "Gold bars at Blast Furnace", "Huge XP with goldsmith gauntlets", FAST, COSTLY)
				.requires("Family Crest")
				.at("Blast Furnace, Keldagrim")
				.travel("Grouping (minigame) teleport"),

			// ---- Crafting ----
			method(CRAFTING, 1, "Leather items", "First levels from cowhides", CHEAP)
				.at("Lumbridge cow field", 3257, 3268),
			method(CRAFTING, 5, "Gold jewellery", "Cheap, quick early levels", CHEAP)
				.at("Edgeville furnace", 3108, 3499),
			method(CRAFTING, 20, "Cutting gems", "Fast bank-standing XP from sapphire up")
				.at("Grand Exchange", 3164, 3487),
			method(CRAFTING, 46, "Glassblowing", "Cheap XP; unpowered orbs at 46, light orbs at 87", CHEAP)
				.at("Grand Exchange", 3164, 3487),
			method(CRAFTING, 63, "Green d'hide bodies", "Fast XP", FAST, COSTLY)
				.at("Grand Exchange", 3164, 3487),
			method(CRAFTING, 66, "Air battlestaves", "Often profitable using Zaff's daily staves", PROFIT)
				.at("Grand Exchange", 3164, 3487),
			method(CRAFTING, 84, "Black d'hide bodies", "Fastest bank-standing XP", FAST, COSTLY)
				.at("Grand Exchange", 3164, 3487),

			// ---- Fletching ----
			method(FLETCHING, 1, "Arrow shafts", "Free first levels from normal logs", CHEAP),
			method(FLETCHING, 10, "Darts", "Very fast XP you can do while walking", FAST, COSTLY)
				.requires("The Tourist Trap"),
			method(FLETCHING, 40, "Stringing longbows", "Relaxed XP from willow (40) up to magic (85)", AFK)
				.at("Grand Exchange", 3164, 3487),
			method(FLETCHING, 52, "Broad arrows", "Fast XP; needs a Slayer reward unlock", FAST)
				.note("Unlock 'Broader fletching' from a Slayer master"),

			// ---- Herblore ----
			method(HERBLORE, 3, "Attack potions", "First potions after Druidic Ritual", CHEAP)
				.requires("Druidic Ritual")
				.at("Grand Exchange", 3164, 3487),
			method(HERBLORE, 3, "Cleaning herbs", "Small XP that adds value to herbs", PROFIT)
				.requires("Druidic Ritual"),
			method(HERBLORE, 38, "Prayer potions", "Good XP; always in demand", PROFIT)
				.requires("Druidic Ritual")
				.at("Grand Exchange", 3164, 3487),
			method(HERBLORE, 63, "Super restores", "Strong mid-level XP")
				.requires("Druidic Ritual")
				.at("Grand Exchange", 3164, 3487),
			method(HERBLORE, 81, "Saradomin brews", "Fast XP", FAST, COSTLY)
				.requires("Druidic Ritual")
				.at("Grand Exchange", 3164, 3487),

			// ---- Firemaking ----
			method(FIREMAKING, 1, "Burning normal and oak logs", "First levels; oak at 15", CHEAP)
				.at("Grand Exchange", 3164, 3487),
			method(FIREMAKING, 30, "Burning willow logs", "Cheap, steady XP", CHEAP)
				.at("Grand Exchange", 3164, 3487),
			method(FIREMAKING, 45, "Burning maple logs", "Faster mid-level XP")
				.at("Grand Exchange", 3164, 3487),
			method(FIREMAKING, 60, "Burning yew logs", "Good XP")
				.at("Grand Exchange", 3164, 3487),
			method(FIREMAKING, 75, "Burning magic logs", "Fast XP", FAST, COSTLY)
				.at("Grand Exchange", 3164, 3487),
			method(FIREMAKING, 90, "Burning redwood logs", "Fastest log-burning XP", FAST, COSTLY)
				.at("Grand Exchange", 3164, 3487),

			// ---- Construction ----
			method(CONSTRUCTION, 19, "Oak chairs", "Cheap early levels", CHEAP)
				.at("Your player-owned house")
				.travel("Teleport to House; a butler fetches planks"),
			method(CONSTRUCTION, 33, "Oak larders", "Classic affordable method", CHEAP)
				.at("Your player-owned house")
				.travel("Teleport to House; a butler fetches planks"),
			method(CONSTRUCTION, 52, "Mahogany tables", "Very fast XP", FAST, COSTLY)
				.at("Your player-owned house")
				.travel("Teleport to House; a demon butler fetches planks"),
			method(CONSTRUCTION, 74, "Oak dungeon doors", "Fast XP, cheaper than mahogany", FAST)
				.at("Your player-owned house")
				.travel("Teleport to House; a demon butler fetches planks"),
			method(CONSTRUCTION, 77, "Gnome benches", "Fastest mahogany method", FAST, COSTLY)
				.at("Your player-owned house")
				.travel("Teleport to House; a demon butler fetches planks")
		);
	}

	// ======================= Gathering skills =======================
	private static List<Suggestion> gathering()
	{
		return Arrays.asList(
			// ---- Mining ----
			method(MINING, 1, "Copper and tin", "First levels", CHEAP)
				.at("Varrock east mine", 3285, 3365)
				.travel("Varrock teleport, walk south-east"),
			method(MINING, 10, "Shooting stars", "Relaxed group Mining with stardust rewards", AFK)
				.at("Random locations; use a star-finding plugin"),
			method(MINING, 15, "Iron power mining", "Fast early-mid XP", FAST)
				.at("Al Kharid mine", 3300, 3310)
				.travel("Ring of dueling to Emir's Arena, walk north"),
			method(MINING, 40, "Gem rocks", "Profitable gems", PROFIT)
				.requires("Shilo Village")
				.at("Shilo Village gem mine"),
			method(MINING, 43, "Blast Mine", "Good XP and ores using dynamite", PROFIT)
				.at("Lovakengj, Kourend"),
			method(MINING, 45, "Granite", "Fast XP in the desert quarry", FAST)
				.at("Desert Quarry, south of Shantay Pass")
				.travel("Bring waterskins and a desert amulet if you have one"),
			method(MINING, 92, "Amethyst", "Very AFK; crafts into ammo", AFK)
				.at("Mining Guild, Falador"),

			// ---- Fishing ----
			method(FISHING, 1, "Shrimp and anchovies", "First levels", CHEAP)
				.at("Draynor Village fishing spot", 3087, 3228),
			method(FISHING, 20, "Fly fishing trout and salmon", "Classic fast early XP", FAST)
				.at("Barbarian Village", 3104, 3431)
				.travel("Varrock teleport, walk west"),
			method(FISHING, 40, "Lobsters", "Relaxed profit", AFK, PROFIT)
				.at("Catherby shore", 2837, 3432)
				.travel("Camelot teleport, walk east"),
			method(FISHING, 43, "Aerial fishing", "Fishing + Hunter XP, golden tench, pearls")
				.skill(HUNTER, 35)
				.alsoFor(HUNTER)
				.featured(50)
				.at("Lake Molch, Kourend", 1365, 3632)
				.wiki("Aerial fishing"),
			method(FISHING, 47, "Drift net fishing", "Fishing and Hunter XP together", FAST)
				.skill(HUNTER, 44)
				.alsoFor(HUNTER)
				.requires("Bone Voyage")
				.at("Fossil Island underwater area")
				.wiki("Drift net fishing"),
			method(FISHING, 48, "Barbarian fishing", "Fishing plus passive Agility & Strength XP", FAST)
				.skill(AGILITY, 15)
				.skill(STRENGTH, 15)
				.featured(60)
				.at("Otto's Grotto, south of Barbarian Outpost", 2500, 3488)
				.travel("Games necklace to Barbarian Outpost")
				.wiki("Barbarian Fishing"),
			method(FISHING, 62, "Monkfish", "Good money", PROFIT)
				.requires("Swan Song")
				.at("Piscatoris Fishing Colony"),
			method(FISHING, 65, "Karambwans", "AFK profit", AFK, PROFIT)
				.requires("Tai Bwo Wannai Trio")
				.at("Karambwan spot, Karamja")
				.travel("Fairy ring DKP"),
			method(FISHING, 76, "Sharks", "Good money", PROFIT)
				.at("Fishing Guild", 2611, 3393),
			method(FISHING, 82, "Anglerfish", "Top-tier food and profit", PROFIT)
				.at("Port Piscarilius, Kourend"),

			// ---- Woodcutting ----
			method(WOODCUTTING, 1, "Normal and oak trees", "First levels; oaks from 15", CHEAP)
				.at("Oaks west of Varrock", 3166, 3415),
			method(WOODCUTTING, 30, "Willow trees", "Classic AFK willows", AFK)
				.at("Draynor Village willows", 3087, 3235),
			method(WOODCUTTING, 35, "Teak trees", "Fast XP", FAST)
				.at("Fossil Island or Ape Atoll"),
			method(WOODCUTTING, 45, "Maple trees", "Steady mid-level XP")
				.at("Seers' Village maples", 2727, 3501)
				.travel("Camelot teleport"),
			method(WOODCUTTING, 60, "Yew trees", "AFK and profitable", AFK, PROFIT)
				.at("Edgeville yews", 3087, 3470)
				.travel("Amulet of glory to Edgeville"),
			method(WOODCUTTING, 65, "Sulliuscep", "Good XP and fossils", FAST)
				.requires("Bone Voyage")
				.at("Fossil Island swamp")
				.wiki("Sulliuscep"),
			method(WOODCUTTING, 75, "Magic trees", "Slow but profitable", PROFIT)
				.at("Woodcutting Guild, Hosidius"),
			method(WOODCUTTING, 90, "Redwood trees", "Extremely AFK", AFK)
				.at("Woodcutting Guild, Hosidius"),

			// ---- Hunter ----
			method(HUNTER, 1, "Crimson swifts", "First levels with bird snares", CHEAP)
				.at("Feldip Hills")
				.travel("Fairy ring AKS"),
			method(HUNTER, 5, "Birdhouse runs", "Passive Hunter XP and bird nests", AFK)
				.skill(CRAFTING, 5)
				.requires("Bone Voyage")
				.featured(30)
				.at("Verdant Valley, Fossil Island", 3764, 3880)
				.travel("Digsite pendant to Fossil Island")
				.wiki("Birdhouse"),
			method(HUNTER, 19, "Tropical wagtails", "Bird snares; quick early levels", CHEAP)
				.at("Feldip Hills")
				.travel("Fairy ring AKS"),
			method(HUNTER, 29, "Swamp lizards", "Net traps", CHEAP)
				.at("Swamp near Canifis, Morytania"),
			method(HUNTER, 46, "Hunter Rumours", "Guild contracts with great rewards")
				.at("Hunters' Guild, Civitas illa Fortis")
				.wiki("Hunter Rumours"),
			method(HUNTER, 47, "Orange salamanders", "Net traps in the desert", CHEAP)
				.at("Kharidian Desert, near Uzer"),
			method(HUNTER, 59, "Red salamanders", "Great XP", FAST)
				.at("Ourania Hunter area"),
			method(HUNTER, 63, "Red chinchompas", "Great XP and ranged ammo money", FAST, PROFIT)
				.requires("Eagles' Peak")
				.featured(45)
				.at("Feldip Hills hunter area", 2555, 2915)
				.travel("Fairy ring AKS")
				.wiki("Red chinchompa"),
			method(HUNTER, 80, "Herbiboar", "Hunter XP and free herbs", PROFIT)
				.skill(HERBLORE, 31)
				.alsoFor(HERBLORE)
				.requires("Bone Voyage")
				.at("Fossil Island")
				.wiki("Herbiboar"),

			// ---- Farming ----
			method(FARMING, 9, "Herb runs", "One of the best low-effort money makers; ranarr at 32", PROFIT)
				.featured(32)
				.at("Falador herb patch", 3058, 3307)
				.travel("Explorer's ring or Falador teleport")
				.wiki("Herb patch"),
			method(FARMING, 15, "Tree runs", "Big XP for a few minutes a day", FAST, COSTLY)
				.at("Tree patches: Lumbridge, Varrock, Falador, Taverley, Gnome Stronghold")
				.wiki("Tree patch"),
			method(FARMING, 23, "Giant seaweed", "Pairs perfectly with glassblowing")
				.requires("Bone Voyage")
				.at("Fossil Island underwater patches")
				.wiki("Giant seaweed"),
			method(FARMING, 27, "Fruit tree runs", "Big XP for little time", FAST)
				.at("Fruit tree patches: Gnome Stronghold, Catherby, Brimhaven, Tree Gnome Village")
				.wiki("Fruit tree patch"),
			method(FARMING, 35, "Hardwood trees", "Huge XP per tree (teak 35, mahogany 55)", FAST, COSTLY)
				.requires("Bone Voyage")
				.at("Fossil Island hardwood patches")
				.wiki("Hardwood tree patch")
		);
	}

	// ======================= Agility / Thieving / Runecraft / Slayer =======================
	private static List<Suggestion> supportSkills()
	{
		return Arrays.asList(
			// ---- Agility ----
			method(AGILITY, 1, "Gnome Stronghold course", "First levels", CHEAP)
				.at("Gnome Stronghold Agility Course", 2474, 3436)
				.travel("Spirit tree to the Gnome Stronghold")
				.wiki("Gnome Stronghold Agility Course"),
			method(AGILITY, 10, "Draynor Village rooftop", "First rooftop course; marks of grace")
				.at("Draynor rooftop start", 3103, 3279)
				.wiki("Draynor Village Rooftop Course"),
			method(AGILITY, 20, "Al Kharid rooftop", "Rooftop course with marks of grace")
				.at("Al Kharid rooftop start", 3273, 3195)
				.wiki("Al Kharid Rooftop Course"),
			method(AGILITY, 30, "Varrock rooftop", "Rooftop course with marks of grace")
				.at("Varrock rooftop start", 3221, 3414)
				.travel("Varrock teleport")
				.wiki("Varrock Rooftop Course"),
			method(AGILITY, 40, "Canifis rooftop course", "Solid XP and lots of marks of grace", PROFIT)
				.requires("Priest in Peril")
				.featured(42)
				.at("Canifis course start", 3507, 3488)
				.travel("Kharyrll teleport or fairy ring CKS")
				.wiki("Canifis Rooftop Course"),
			method(AGILITY, 50, "Falador rooftop", "Rooftop course with marks of grace")
				.at("Falador rooftop start", 3036, 3341)
				.travel("Falador teleport")
				.wiki("Falador Rooftop Course"),
			method(AGILITY, 50, "Colossal Wyrm course", "Good XP and termites; advanced route at 62")
				.at("Colossal Wyrm Remains, Varlamore")
				.wiki("Colossal Wyrm Agility Course"),
			method(AGILITY, 52, "Wilderness Agility Course", "Fast XP and loot; dangerous", FAST)
				.at("Level 52 Wilderness")
				.wiki("Wilderness Agility Course"),
			method(AGILITY, 60, "Seers' Village rooftop", "Best mid-level rooftop", FAST)
				.featured(50)
				.at("Seers' course start", 2729, 3489)
				.travel("Camelot teleport (Seers' option after the Kandarin hard diary)")
				.wiki("Seers' Village Rooftop Course"),
			method(AGILITY, 70, "Pollnivneach rooftop", "Rooftop course with marks of grace")
				.at("Pollnivneach rooftop start", 3351, 2961)
				.wiki("Pollnivneach Rooftop Course"),
			method(AGILITY, 80, "Rellekka rooftop", "Rooftop course with marks of grace")
				.at("Rellekka rooftop start", 2625, 3677)
				.wiki("Rellekka Rooftop Course"),
			method(AGILITY, 90, "Ardougne rooftop", "Fastest rooftop course", FAST)
				.at("Ardougne rooftop start", 2673, 3297)
				.travel("Ardougne teleport or cloak")
				.wiki("Ardougne Rooftop Course"),

			// ---- Thieving ----
			method(THIEVING, 1, "Men and women", "First levels", CHEAP)
				.at("Lumbridge", 3222, 3218),
			method(THIEVING, 5, "Ardougne cake stall", "Easy early levels and free food", CHEAP)
				.at("Ardougne market", 2662, 3305)
				.travel("Ardougne teleport"),
			method(THIEVING, 38, "Master Farmers", "Seeds for Farming", PROFIT)
				.alsoFor(FARMING)
				.at("Draynor Village market", 3080, 3250),
			method(THIEVING, 49, "Stealing artefacts", "Good XP and coins", PROFIT)
				.at("Port Piscarilius, Kourend")
				.wiki("Stealing artefacts"),
			method(THIEVING, 50, "Wealthy citizens", "Relaxed thieving in Varlamore", AFK)
				.at("Civitas illa Fortis, Varlamore")
				.wiki("Wealthy citizen"),
			method(THIEVING, 55, "Ardougne knights", "Classic fast XP and coins", FAST, PROFIT)
				.at("Ardougne market", 2662, 3305)
				.travel("Ardougne teleport or cloak")
				.wiki("Knight of Ardougne"),

			// ---- Runecraft ----
			method(RUNECRAFT, 1, "Ourania altar (ZMI)", "Mixed runes from pure essence at any level", PROFIT)
				.at("Ourania Cave")
				.travel("Ourania Teleport (Lunar spellbook) makes this much faster")
				.wiki("Ourania Altar"),
			method(RUNECRAFT, 23, "Lava runes", "Fast XP with binding necklaces", FAST, COSTLY)
				.at("Fire altar, Kharidian Desert")
				.travel("Ring of dueling to Emir's Arena")
				.wiki("Lava rune"),
			method(RUNECRAFT, 40, "Astral runes", "Profitable; needed for Lunar spells", PROFIT)
				.requires("Lunar Diplomacy")
				.at("Astral altar, Lunar Isle"),
			method(RUNECRAFT, 77, "Blood runes (Arceuus)", "AFK and profitable, no essence to buy", AFK, PROFIT)
				.at("Arceuus dense essence mine, Kourend")
				.wiki("True Blood Altar"),
			method(RUNECRAFT, 90, "Soul runes", "AFK and profitable", AFK, PROFIT)
				.at("Soul altar, Arceuus, Kourend")
				.wiki("Soul Altar"),

			// ---- Slayer ----
			method(SLAYER, 1, "Turael", "Easiest tasks for your first Slayer levels", CHEAP)
				.at("Turael, Burthorpe", 2931, 3536)
				.travel("Games necklace to Burthorpe")
				.wiki("Turael"),
			method(SLAYER, 1, "Vannaka", "Steady tasks for mid-level accounts")
				.combat(40)
				.featured(35)
				.at("Edgeville Dungeon trapdoor", 3097, 3468)
				.travel("Amulet of glory to Edgeville")
				.wiki("Vannaka"),
			method(SLAYER, 1, "Chaeldar", "Better tasks and more points")
				.combat(70)
				.requires("Lost City")
				.at("Chaeldar, Zanaris")
				.wiki("Chaeldar"),
			method(SLAYER, 1, "Konar quo Maten", "Location-based tasks with brimstone keys", PROFIT)
				.combat(75)
				.at("Mount Karuulm, Kourend")
				.wiki("Konar quo Maten"),
			method(SLAYER, 1, "Nieve / Steve", "Best master for XP and boss tasks", FAST)
				.combat(85)
				.featured(50)
				.at("Nieve, Gnome Stronghold", 2432, 3423)
				.travel("Spirit tree to the Gnome Stronghold")
				.wiki("Nieve"),
			method(SLAYER, 50, "Duradel", "Highest-level tasks and the most points", FAST)
				.combat(100)
				.requires("Shilo Village")
				.at("Duradel, Shilo Village")
				.wiki("Duradel")
		);
	}

	// ======================= Minigames =======================
	private static List<Suggestion> minigames()
	{
		return Arrays.asList(
			minigame("Tempoross", "Fishing XP, angler outfit, tome of water", FAST)
				.skill(FISHING, 35)
				.alsoFor(FISHING)
				.featured(40)
				.at("Tempoross Cove, Ruins of Unkah", 3153, 2833)
				.travel("Grouping (minigame) teleport"),
			minigame("Wintertodt", "Fast Firemaking XP and supply crates", FAST, PROFIT)
				.skill(FIREMAKING, 50)
				.alsoFor(FIREMAKING)
				.featured(40)
				.at("Wintertodt Camp", 1630, 3944)
				.travel("Games necklace to Wintertodt Camp"),
			minigame("Guardians of the Rift", "Best early Runecraft XP and the raiments outfit", FAST)
				.skill(RUNECRAFT, 27)
				.requires("Temple of the Eye")
				.alsoFor(RUNECRAFT)
				.featured(40)
				.at("Temple of the Eye")
				.travel("Grouping (minigame) teleport"),
			minigame("Giants' Foundry", "Smithing XP without buying tons of bars")
				.skill(SMITHING, 15)
				.requires("Sleeping Giants")
				.alsoFor(SMITHING)
				.featured(45)
				.at("Giants' Foundry")
				.travel("Grouping (minigame) teleport"),
			minigame("Blast Furnace", "Fastest bar smelting; no coffer fee at 60", FAST)
				.alsoFor(SMITHING)
				.recommend(SMITHING, 60)
				.featured(55)
				.at("Keldagrim (minecart from the Grand Exchange)", 3164, 3487)
				.travel("Grouping (minigame) teleport"),
			minigame("Motherlode Mine", "Low-attention Mining and the prospector kit", AFK)
				.skill(MINING, 30)
				.alsoFor(MINING)
				.featured(55)
				.at("Dwarven Mine entrance, north-east Falador", 3061, 3377)
				.travel("Falador teleport"),
			minigame("Volcanic Mine", "Very fast Mining XP in a team", FAST)
				.skill(MINING, 50)
				.requires("Bone Voyage")
				.alsoFor(MINING)
				.at("Fossil Island volcano")
				.travel("Digsite pendant to Fossil Island"),
			minigame("Tithe Farm", "Farming XP and the farmer's outfit", FAST)
				.skill(FARMING, 34)
				.alsoFor(FARMING)
				.featured(55)
				.at("Tithe Farm, Hosidius", 1802, 3501)
				.travel("Grouping (minigame) teleport"),
			minigame("Mahogany Homes", "Construction contracts with good XP and a carpenter outfit", CHEAP)
				.alsoFor(CONSTRUCTION)
				.at("Any Mahogany Homes office: Falador, Varrock, Ardougne or Hosidius"),
			minigame("Hallowed Sepulchre", "Top Agility XP and valuable loot", FAST, PROFIT)
				.skill(AGILITY, 52)
				.requires("Sins of the Father")
				.alsoFor(AGILITY)
				.featured(50)
				.at("Darkmeyer, Morytania"),
			minigame("Brimhaven Agility Arena", "Agility XP and tickets for rewards")
				.alsoFor(AGILITY)
				.at("Brimhaven Agility Arena", 2809, 3193)
				.travel("Boat from Ardougne to Brimhaven"),
			minigame("Mastering Mixology", "Herblore XP and useful rewards")
				.skill(HERBLORE, 60)
				.alsoFor(HERBLORE)
				.featured(50)
				.at("Aldarin, Varlamore"),
			minigame("Pyramid Plunder", "Great Thieving XP and sceptre chance", FAST)
				.skill(THIEVING, 21)
				.alsoFor(THIEVING)
				.note("Needs Icthlarin's Little Helper started to enter Sophanem")
				.at("Sophanem, Kharidian Desert"),
			minigame("Rogues' Den", "The rogue outfit: double loot while thieving")
				.skill(THIEVING, 50)
				.skill(AGILITY, 50)
				.alsoFor(THIEVING, AGILITY)
				.at("Rogues' Den, Burthorpe", 2905, 3537)
				.travel("Games necklace to Burthorpe"),
			minigame("Fishing Trawler", "Angler outfit and Fishing XP")
				.skill(FISHING, 15)
				.alsoFor(FISHING)
				.at("Port Khazard", 2676, 3170),
			minigame("Trouble Brewing", "Cooking XP and pirate rewards")
				.skill(COOKING, 40)
				.requires("Cabin Fever")
				.alsoFor(COOKING)
				.at("Mos Le'Harmless"),
			minigame("Mage Training Arena", "Infinity robes, mage's book, Bones to Peaches")
				.alsoFor(MAGIC)
				.at("Mage Training Arena, north of Emir's Arena", 3363, 3298)
				.travel("Ring of dueling to Emir's Arena"),
			minigame("Puro-Puro", "Impling hunting for Hunter XP and loot")
				.skill(HUNTER, 17)
				.requires("Lost City")
				.alsoFor(HUNTER)
				.at("Wheat field crop circle in Zanaris"),
			minigame("Tears of Guthix", "Free weekly XP in your lowest skill")
				.questPoints(43)
				.at("Lumbridge Swamp Caves (bring a light source)"),
			minigame("Nightmare Zone", "Premier AFK melee and ranged training", AFK, FAST)
				.alsoFor(ATTACK, STRENGTH, DEFENCE, HITPOINTS, RANGED, MAGIC)
				.note("Needs at least 5 NMZ-eligible quests completed")
				.featured(45)
				.at("Nightmare Zone, north-east Yanille", 2608, 3115)
				.travel("Grouping (minigame) teleport"),
			minigame("Pest Control", "Void Knight equipment and combat XP")
				.combat(40)
				.alsoFor(ATTACK, STRENGTH, DEFENCE, HITPOINTS, RANGED, MAGIC, PRAYER)
				.featured(45)
				.at("Pest Control boat, Port Sarim", 3041, 3202)
				.travel("Grouping (minigame) teleport"),
			minigame("Barbarian Assault", "Fighter torso, Penance gear, Hitpoints XP")
				.alsoFor(HITPOINTS)
				.featured(50)
				.at("Barbarian Assault, Barbarian Outpost", 2531, 3570)
				.travel("Games necklace to Barbarian Outpost"),
			minigame("Warriors' Guild", "Rune and dragon defenders")
				.combined(130, ATTACK, STRENGTH)
				.alsoFor(ATTACK, STRENGTH, DEFENCE)
				.note("Or 99 in Attack or Strength")
				.featured(40)
				.at("Warriors' Guild, Burthorpe", 2878, 3546)
				.travel("Games necklace to Burthorpe"),
			minigame("Castle Wars", "Team capture-the-flag with decorative armour rewards")
				.at("Castle Wars", 2442, 3090)
				.travel("Ring of dueling to Castle Wars"),
			minigame("Soul Wars", "Team battles that pay out combat XP", AFK)
				.combat(40)
				.alsoFor(ATTACK, STRENGTH, DEFENCE, HITPOINTS, RANGED, MAGIC, PRAYER)
				.at("Isle of Souls")
				.travel("Grouping (minigame) teleport"),
			minigame("Last Man Standing", "Risk-free PvP battle royale")
				.at("Ferox Enclave")
				.travel("Ring of dueling to Ferox Enclave"),
			minigame("TzHaar Fight Cave", "The fire cape")
				.recommend(RANGED, 70)
				.recommend(PRAYER, 43)
				.at("TzHaar City, Karamja volcano")
		);
	}
}
