/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015  Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2016 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.egoal.darkestpixeldungeon.sprites;

public class ItemSpriteSheet {

    private static final int WIDTH = 16;

    private static int xy(int x, int y) {
        x -= 1;
        y -= 1;
        return x + WIDTH * y;
    }

    private static final int PLACEHOLDERS = xy(1, 1);   //8 slots
    //null warning occupies space 0, should only show up if there's a bug.
    public static final int NULLWARN = PLACEHOLDERS + 0;
    public static final int WEAPON_HOLDER = PLACEHOLDERS + 1;
    public static final int ARMOR_HOLDER = PLACEHOLDERS + 2;
    public static final int HELMET_HOLDER = PLACEHOLDERS + 3;
    public static final int RING_HOLDER = PLACEHOLDERS + 4;
    public static final int SOMETHING = PLACEHOLDERS + 5;

    private static final int UNCOLLECTIBLE = xy(9, 1);   //8 slots
    public static final int GOLD = UNCOLLECTIBLE + 0;
    public static final int DEWDROP = UNCOLLECTIBLE + 1;
    public static final int PETAL = UNCOLLECTIBLE + 2;
    public static final int SANDBAG = UNCOLLECTIBLE + 3;
    public static final int DBL_BOMB = UNCOLLECTIBLE + 4;
    public static final int RUNE = UNCOLLECTIBLE + 5;

    private static final int CONTAINERS = xy(1, 2);   //16 slots
    public static final int BONES = CONTAINERS + 0;
    public static final int REMAINS = CONTAINERS + 1;
    public static final int TOMB = CONTAINERS + 2;
    public static final int GRAVE = CONTAINERS + 3;
    public static final int CHEST = CONTAINERS + 4;
    public static final int LOCKED_CHEST = CONTAINERS + 5;
    public static final int CRYSTAL_CHEST = CONTAINERS + 6;

    private static final int SINGLE_USE = xy(1, 3);   //32 slots
    public static final int ANKH = SINGLE_USE + 0;
    public static final int STYLUS = SINGLE_USE + 1;
    public static final int WEIGHT = SINGLE_USE + 2;
    public static final int SEAL = SINGLE_USE + 3;
    public static final int TORCH = SINGLE_USE + 4;
    public static final int BEACON = SINGLE_USE + 5;
    public static final int BOMB = SINGLE_USE + 6;
    public static final int HONEYPOT = SINGLE_USE + 7;
    public static final int SHATTPOT = SINGLE_USE + 8;
    public static final int IRON_KEY = SINGLE_USE + 9;
    public static final int GOLDEN_KEY = SINGLE_USE + 10;
    public static final int SKELETON_KEY = SINGLE_USE + 11;
    public static final int MASTERY = SINGLE_USE + 12;
    public static final int KIT = SINGLE_USE + 13;
    public static final int AMULET = SINGLE_USE + 14;
    public static final int DPD_TEST_PAPER = SINGLE_USE + 15;
    public static final int ENHANCED_SEAL = SINGLE_USE + 16;
    public static final int DPD_CAT_GIFT = SINGLE_USE + 17;
    public static final int GREAT_BLUEPRINT = SINGLE_USE + 18;
    public static final int GOLDEN_CLAW = SINGLE_USE + 19;
    public static final int EVIL_GOLDEN_CLAW = GOLDEN_CLAW + 1;
    public static final int HEAL_REAGENT = SINGLE_USE + 21;
    public static final int FISH_BONE = SINGLE_USE + 22;
    public static final int TOME_BLUE = SINGLE_USE + 23;
    public static final int TOME_YELLOW = SINGLE_USE + 24;
    public static final int FIRE_BUTTERFLY = SINGLE_USE + 25;
    public static final int POISON_POWDER = SINGLE_USE + 26;
    public static final int LUCKY_COIN = SINGLE_USE + 27;

    private static final int WEP_TIER1 = xy(1, 5);   //16 slots
    public static final int WORN_SHORTSWORD = WEP_TIER1 + 0;
    public static final int DPD_BATTLE_GLOVES = WEP_TIER1 + 1;
    public static final int KNUCKLEDUSTER = WEP_TIER1 + 2;
    public static final int RED_HANDLE_DAGGER = WEP_TIER1 + 3;
    public static final int DAGGER = WEP_TIER1 + 4;
    public static final int MAGES_STAFF = WEP_TIER1 + 5;
    public static final int DPD_SORCERESS_WAND = WEP_TIER1 + 6;
    public static final int SHORTSPEAR = WEP_TIER1 + 7;
    public static final int BLOCK_DAGGER = WEP_TIER1 + 8;

    private static final int WEP_TIER2 = xy(1, 6);   //16 slots
    public static final int SHORTSWORD = WEP_TIER2 + 0;
    public static final int HAND_AXE = WEP_TIER2 + 1;
    public static final int SPEAR = WEP_TIER2 + 2;
    public static final int QUARTERSTAFF = WEP_TIER2 + 3;
    public static final int DIRK = WEP_TIER2 + 4;
    public static final int DRIED_LEG = WEP_TIER2 + 5;
    public static final int SICKLE = WEP_TIER2 + 6;

    public static final int RANGER_BOW = WEP_TIER2 + 8;
    public static final int TULWAR = WEP_TIER2 + 9;
    public static final int CEREMONIAL_SWORD = WEP_TIER2 + 10;
    public static final int BUTCHERS_KNIFE = WEP_TIER2 + 11;
    public static final int SHORT_STICKS = WEP_TIER2 + 12;

    private static final int WEP_TIER3 = xy(1, 7);   //16 slots
    public static final int SWORD = WEP_TIER3 + 0;
    public static final int MACE = WEP_TIER3 + 1;
    public static final int SCIMITAR = WEP_TIER3 + 2;
    public static final int ROUND_SHIELD = WEP_TIER3 + 3;
    public static final int SAI = WEP_TIER3 + 4;
    public static final int WHIP = WEP_TIER3 + 5;
    public static final int DPD_CRYSTALS_SWORDS = WEP_TIER3 + 6;
    public static final int DAGGER_AXE = WEP_TIER3 + 7;
    public static final int CANDLESTICK = WEP_TIER3 + 8;
    public static final int INVISIBLE_BLADE = WEP_TIER3 + 9;
    public static final int BOETHIAHS_BLADE = WEP_TIER3 + 10;
    public static final int FLAG = WEP_TIER3 + 11;
    public static final int KATANA = WEP_TIER3 + 12;

    private static final int WEP_TIER4 = xy(1, 8);   //16 slots
    public static final int LONGSWORD = WEP_TIER4 + 0;
    public static final int BATTLE_AXE = WEP_TIER4 + 1;
    public static final int FLAIL = WEP_TIER4 + 2;
    public static final int RUNIC_BLADE = WEP_TIER4 + 3;
    public static final int ASSASSINS_BLADE = WEP_TIER4 + 4;
    public static final int SPIKE_SHIELD = WEP_TIER4 + 5;
    public static final int PITCHFORK = WEP_TIER4 + 6;
    public static final int HALBERD = WEP_TIER4 + 7;
    public static final int SCYTHE = WEP_TIER4 + 8;
    // 9, 10
    public static final int KUSARIGAMA = WEP_TIER4 + 11;

    private static final int WEP_TIER5 = xy(1, 9);   //16 slots
    public static final int Claymore = WEP_TIER5 + 0;
    public static final int WAR_HAMMER = WEP_TIER5 + 1;
    public static final int GLAIVE = WEP_TIER5 + 2;
    public static final int GREATAXE = WEP_TIER5 + 3;
    public static final int GREATSHIELD = WEP_TIER5 + 4;
    public static final int PAIR_SWORDS = WEP_TIER5 + 5;
    public static final int LANCE = WEP_TIER5 + 6;

    private static final int MISSILE_WEP = xy(1, 10);  //16 slots
    public static final int DART = MISSILE_WEP + 0;
    public static final int BOOMERANG = MISSILE_WEP + 1;
    public static final int INCENDIARY_DART = MISSILE_WEP + 2;
    public static final int SHURIKEN = MISSILE_WEP + 3;
    public static final int CURARE_DART = MISSILE_WEP + 4;
    public static final int JAVELIN = MISSILE_WEP + 5;
    public static final int TOMAHAWK = MISSILE_WEP + 6;
    public static final int SMOKE_SPARKS = MISSILE_WEP + 7;
    public static final int SWALLOW_DART = MISSILE_WEP + 8;
    public static final int FLY_CUTTER = MISSILE_WEP + 9;
    public static final int ENHANCED_BOOMERANG = MISSILE_WEP + 10;
    public static final int DART_SEVENTH = MISSILE_WEP + 11;
    public static final int CEREMONIAL_DAGGER = MISSILE_WEP + 12;
    public static final int MAGIC_DART = MISSILE_WEP + 13;
    public static final int SALT = MISSILE_WEP + 14;
    public static final int SALT_2 = MISSILE_WEP + 15;

    private static final int ARMOR = xy(1, 11);  //16 slots
    public static final int ARMOR_CLOTH = ARMOR + 0;
    public static final int ARMOR_LEATHER = ARMOR + 1;
    public static final int ARMOR_MAIL = ARMOR + 2;
    public static final int ARMOR_SCALE = ARMOR + 3;
    public static final int ARMOR_PLATE = ARMOR + 4;
    public static final int ARMOR_WARRIOR = ARMOR + 5;
    public static final int ARMOR_MAGE = ARMOR + 6;
    public static final int ARMOR_ROGUE = ARMOR + 7;
    public static final int ARMOR_HUNTRESS = ARMOR + 8;
    public static final int ARMOR_SORCERESS = ARMOR + 9;
    public static final int ARMOR_MAGE_ENHANCED = ARMOR + 10;
    public static final int ARMOR_RAGGED = ARMOR + 11;
    public static final int ARMOR_EXILE = ARMOR + 12;

    private static final int HELMET = xy(1, 12);
    public static final int HELMET_CRUSADER = HELMET + 0;
    public static final int HELMET_BARBARIAN = HELMET + 1;
    public static final int HELMET_APPRENTICE = HELMET + 2;
    public static final int HELMET_EMERALD = HELMET + 3;
    public static final int DWARF_CROWN = HELMET + 4;
    public static final int HEADDRESS_OF_REGENERATION = HELMET + 5;
    public static final int WIZARD_HAT = HELMET + 6;
    public static final int HELMET_CLOWN = HELMET + 7;
    public static final int HELMET_HORROR = HELMET + 8;
    public static final int HELMET_RANGER = HELMET + 9;
    public static final int TURTLE_SCARF_BLUE = HELMET + 10;
    // 3 scarf here.
    public static final int LITTLE_PAIL = HELMET + 14;
    public static final int RIDER_MASK = HELMET + 15;
    public static final int HELMET_GUARD = HELMET + 16;
    public static final int STRAW_HAT = HELMET + 17;
    public static final int MANTILLA = HELMET + 18;
    public static final int SLAVE_COLLAR = HELMET + 19;

    //16 free slots

    private static final int WANDS = xy(1, 14);  //16 slots
    public static final int WAND_MAGIC_MISSILE = WANDS + 0;
    public static final int WAND_FIREBOLT = WANDS + 1;
    public static final int WAND_FROST = WANDS + 2;
    public static final int WAND_LIGHTNING = WANDS + 3;
    public static final int WAND_DISINTEGRATION = WANDS + 4;
    public static final int WAND_PRISMATIC_LIGHT = WANDS + 5;
    public static final int WAND_VENOM = WANDS + 6;
    public static final int WAND_HYPNOSIS = WANDS + 7;
    public static final int WAND_BLAST_WAVE = WANDS + 8;
    public static final int WAND_CORRUPTION = WANDS + 9;
    public static final int WAND_WARDING = WANDS + 10;
    public static final int WAND_REGROWTH = WANDS + 11;
    public static final int WAND_TRANSFUSION = WANDS + 12;
    public static final int WAND_SWAP = WANDS + 13;

    private static final int RINGS = xy(1, 15);  //16 slots
    public static final int RING_GARNET = RINGS + 0;
    public static final int RING_RUBY = RINGS + 1;
    public static final int RING_TOPAZ = RINGS + 2;
    public static final int RING_EMERALD = RINGS + 3;
    public static final int RING_ONYX = RINGS + 4;
    public static final int RING_OPAL = RINGS + 5;
    public static final int RING_TOURMALINE = RINGS + 6;
    public static final int RING_SAPPHIRE = RINGS + 7;
    public static final int RING_AMETHYST = RINGS + 8;
    public static final int RING_QUARTZ = RINGS + 9;
    public static final int RING_AGATE = RINGS + 10;
    public static final int RING_DIAMOND = RINGS + 11;

    private static final int ARTIFACTS = xy(1, 16);  //32 slots
    public static final int ARTIFACT_CLOAK = ARTIFACTS + 0;
    public static final int ARTIFACT_ARMBAND = ARTIFACTS + 1;
    public static final int ARTIFACT_CAPE = ARTIFACTS + 2;
    public static final int ARTIFACT_TALISMAN = ARTIFACTS + 3;
    public static final int ARTIFACT_HOURGLASS = ARTIFACTS + 4;
    public static final int ARTIFACT_TOOLKIT = ARTIFACTS + 5;
    public static final int ARTIFACT_SPELLBOOK = ARTIFACTS + 6;
    public static final int ARTIFACT_BEACON = ARTIFACTS + 7;
    public static final int ARTIFACT_CHAINS = ARTIFACTS + 8;
    public static final int ARTIFACT_HORN1 = ARTIFACTS + 9;
    public static final int ARTIFACT_HORN2 = ARTIFACTS + 10;
    public static final int ARTIFACT_HORN3 = ARTIFACTS + 11;
    public static final int ARTIFACT_HORN4 = ARTIFACTS + 12;
    public static final int ARTIFACT_CHALICE1 = ARTIFACTS + 13;
    public static final int ARTIFACT_CHALICE2 = ARTIFACTS + 14;
    public static final int ARTIFACT_CHALICE3 = ARTIFACTS + 15;
    public static final int ARTIFACT_SANDALS = ARTIFACTS + 16;
    public static final int ARTIFACT_SHOES = ARTIFACTS + 17;
    public static final int ARTIFACT_BOOTS = ARTIFACTS + 18;
    public static final int ARTIFACT_GREAVES = ARTIFACTS + 19;
    public static final int ARTIFACT_ROSE1 = ARTIFACTS + 20;
    public static final int ARTIFACT_ROSE2 = ARTIFACTS + 21;
    public static final int ARTIFACT_ROSE3 = ARTIFACTS + 22;
    public static final int EXTRACTION_FLASK = ARTIFACTS + 23;

    public static final int DEMONIC_SKULL = ARTIFACTS + 25;
    public static final int UNHOLY_BLOOD = ARTIFACTS + 26;
    public static final int MASK_OF_MADNESS = ARTIFACTS + 27;

    public static final int BONE_HAND = ARTIFACTS + 31;
    public static final int HANDLE_OF_ABYSS = ARTIFACTS + 32;
    public static final int CLOAK_OF_SHEEP = ARTIFACTS + 33;
    public static final int HEART_OF_SATAN = ARTIFACTS + 34;
    public static final int HEART_OF_SATAN_1 = ARTIFACTS + 35;
    public static final int HEART_OF_SATAN_2 = ARTIFACTS + 36;
    public static final int ARTIFACT_EYEBALL = ARTIFACTS + 37;
    public static final int EYEBALL_PAIR = ARTIFACTS + 38;
    public static final int RIEMANNIAN_SHIELD = ARTIFACTS + 39;
    public static final int GOLD_PLATE_STATUE = ARTIFACTS + 40;
    public static final int EXTRACTION_FLASK_ENHANCED = ARTIFACTS + 41;
    public static final int ARTIFACT_CLOAK_ENHANCED = ARTIFACTS + 42;
    public static final int CRACKED_COIN = ARTIFACTS + 43;
    public static final int ARTIFACT_SHIELD = ARTIFACTS + 44;
    public static final int DARGONS_SQUAMA = ARTIFACTS + 45;
    public static final int GODESS_RADIANCE = ARTIFACTS + 46;
    public static final int RANGERS_HOOK = ARTIFACTS + 47;

    //32 free slots
    private static final int SCROLLS = xy(1, 20);  //16 slots
    public static final int SCROLL_KAUNAN = SCROLLS + 0;
    public static final int SCROLL_SOWILO = SCROLLS + 1;
    public static final int SCROLL_LAGUZ = SCROLLS + 2;
    public static final int SCROLL_YNGVI = SCROLLS + 3;
    public static final int SCROLL_GYFU = SCROLLS + 4;
    public static final int SCROLL_RAIDO = SCROLLS + 5;
    public static final int SCROLL_ISAZ = SCROLLS + 6;
    public static final int SCROLL_MANNAZ = SCROLLS + 7;
    public static final int SCROLL_NAUDIZ = SCROLLS + 8;
    public static final int SCROLL_BERKANAN = SCROLLS + 9;
    public static final int SCROLL_ODAL = SCROLLS + 10;
    public static final int SCROLL_TIWAZ = SCROLLS + 11;
    public static final int SCROLL_QI = SCROLLS + 12;
    public static final int SCROLL_LINGEL = SCROLLS + 13;

    private static final int POTIONS = xy(1, 21);  //16 slots
    public static final int POTION_CRIMSON = POTIONS + 0;
    public static final int POTION_AMBER = POTIONS + 1;
    public static final int POTION_GOLDEN = POTIONS + 2;
    public static final int POTION_JADE = POTIONS + 3;
    public static final int POTION_TURQUOISE = POTIONS + 4;
    public static final int POTION_AZURE = POTIONS + 5;
    public static final int POTION_INDIGO = POTIONS + 6;
    public static final int POTION_MAGENTA = POTIONS + 7;
    public static final int POTION_BISTRE = POTIONS + 8;
    public static final int POTION_CHARCOAL = POTIONS + 9;
    public static final int POTION_SILVER = POTIONS + 10;
    public static final int POTION_IVORY = POTIONS + 11;
    public static final int POTION_DARK_GREEN = POTIONS + 12;
    public static final int POTION_STEEL_BLUE = POTIONS + 13;

    private static final int SEEDS = xy(1, 22);  //16 slots
    public static final int SEED_ROTBERRY = SEEDS + 0;
    public static final int SEED_FIREBLOOM = SEEDS + 1;
    public static final int SEED_STARFLOWER = SEEDS + 2;
    public static final int SEED_BLINDWEED = SEEDS + 3;
    public static final int SEED_SUNGRASS = SEEDS + 4;
    public static final int SEED_ICECAP = SEEDS + 5;
    public static final int SEED_STORMVINE = SEEDS + 6;
    public static final int SEED_SORROWMOSS = SEEDS + 7;
    public static final int SEED_DREAMFOIL = SEEDS + 8;
    public static final int SEED_EARTHROOT = SEEDS + 9;
    public static final int SEED_FADELEAF = SEEDS + 10;
    public static final int SEED_BLANDFRUIT = SEEDS + 11;
    public static final int SEED_CORRODE_CYAN = SEEDS + 12;

    public static final int BOOKS = xy(1, 23);
    public static final int DPD_BOOKS = BOOKS + 0;
    public static final int DPD_NOTES = BOOKS + 1;

    //16 free slots

    private static final int FOOD = xy(1, 25);  //32m slots
    public static final int MEAT = FOOD + 0;
    public static final int STEAK = FOOD + 1;
    public static final int OVERPRICED = FOOD + 2;
    public static final int CARPACCIO = FOOD + 3;
    public static final int BLANDFRUIT = FOOD + 4;
    public static final int RATION = FOOD + 5;
    public static final int PASTY = FOOD + 6;
    public static final int CANDY_CANE = FOOD + 7;
    public static final int DPD_HUMANITY = FOOD + 8;
    public static final int DPD_WINE = FOOD + 9;
    public static final int BROWN_ALE = FOOD + 10;
    public static final int MOON_STONE = FOOD + 11;
    public static final int STEWED = FOOD + 12;
    public static final int SKEWER = FOOD + 13;
    public static final int GLAND = FOOD + 14;
    public static final int ROOT = FOOD + 15;
    public static final int RICE_WINE = FOOD + 16;

    private static final int QUEST = xy(1, 27);  //16 slots
    public static final int SKULL = QUEST + 0;
    public static final int DUST = QUEST + 1;
    public static final int CANDLE = QUEST + 2;
    public static final int EMBER = QUEST + 3;
    public static final int PICKAXE = QUEST + 4;
    public static final int ORE = QUEST + 5;
    public static final int TOKEN = QUEST + 6;

    private static final int BAGS = xy(1, 28);  //16 slots
    public static final int POUCH = BAGS + 0;
    public static final int HOLDER = BAGS + 1;
    public static final int BANDOLIER = BAGS + 2;
    public static final int HOLSTER = BAGS + 3;
    public static final int VIAL = BAGS + 4;
    public static final int GOURD = BAGS + 5;
    public static final int PURSE = BAGS + 6;
    public static final int SKILL_TREE = BAGS + 7;

    private static final int SPECIALS = xy(1, 30);
    public static final int STRENGTH_OFFERING = SPECIALS + 0;
    public static final int URN_OF_SHADOW = SPECIALS + 1;
    public static final int ASTROLABE = SPECIALS + 2;
    public static final int ASTROLABE_1 = ASTROLABE + 1;
    public static final int ASTROLABE_2 = ASTROLABE + 2;
    public static final int PENETRATION_RDY = SPECIALS + 5;
    public static final int PENETRATION = PENETRATION_RDY + 1;
    public static final int SHADOWMOON_RDY = SPECIALS + 7;
    public static final int SHADOWMOON = SHADOWMOON_RDY + 1;

    //32 free slots

}
