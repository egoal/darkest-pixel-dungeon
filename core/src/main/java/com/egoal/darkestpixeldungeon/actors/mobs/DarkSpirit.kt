package com.egoal.darkestpixeldungeon.actors.mobs

import com.egoal.darkestpixeldungeon.*
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Mending
import com.egoal.darkestpixeldungeon.actors.hero.HeroClass
import com.egoal.darkestpixeldungeon.actors.hero.perks.Perk
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.PerkGain
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.armor.Armor
import com.egoal.darkestpixeldungeon.items.potions.PotionOfHealing
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.sprites.HeroSprite
import com.egoal.darkestpixeldungeon.sprites.MobSprite
import com.watabou.noosa.Game
import com.watabou.noosa.TextureFilm
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle
import com.watabou.utils.Random
import java.io.IOException
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class DarkSpirit : Mob() {
    private var potions = 0
    private var potionCD = 0

    init {
        HT = 20 + level * 4
        HP = HT

        name = userName

        potions = Random.IntRange(2, 3)

        magicalResistance = -0.25f + (armor?.MRES() ?: 0f)
        elementalResistance.fill(0.1f)

        criticalChance = 0.1f
        minDamage = 1
        maxDamage = max(10, depth + (level - depth) * 2)
        criticalRatio = 1.5f

        minDefense = armor?.DRMin() ?: 0
        maxDefense = armor?.DRMax() ?: 0

        atkSkill = 10f + level
        defSkill = 5f + level

        state = WANDERING
    }

    override fun act(): Boolean {
        HP = min(HP + 1, HT)
        potionCD -= 1
        val ratio = HP.toFloat() / HT
        if (ratio <= 0.6f && potions > 0 && potionCD <= 0 && Random.Float() < (2f * (0.6 - ratio))) {
            potions -= 1
            potionCD = 3

            Sample.INSTANCE.play(Assets.SND_DRINK)

            val value = HT / 3
            recoverHP(value)
            Buff.affect(this, Mending::class.java).set(value)

            spend(TICK)
            return true
        }

        return super.act()
    }

    override fun createLoot(): Item? {
        if (potions > 0) return PotionOfHealing()

        return super.createLoot()
    }

    override fun die(cause: Any?) {
        if (perk?.isAcquireAllowed(Dungeon.hero) == true) {
            Dungeon.hero.heroPerk.add(perk!!)
            PerkGain.Show(Dungeon.hero, perk!!)
        } else {
            Dungeon.hero.earnExp(Dungeon.hero.maxExp())
            CellEmitter.center(Dungeon.hero.pos).burst(Speck.factory(Speck.STAR), 8)
        }

        super.die(cause)
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(POTIONS, potions)
        bundle.put(POTION_CD, potionCD)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        potions = bundle.getInt(POTIONS)
        potionCD = bundle.getInt(POTION_CD)
    }

    override fun sprite(): CharSprite = Sprite()

    inner class Sprite : MobSprite() {
        init {
            texture(heroClass.spritesheet())
            updateArmor(armor?.tier ?: 0)
            idle()

            tint(0.3f, 0.1f, 0.1f, 0.75f)
            alpha(0.75f)
        }

        fun updateArmor(tier: Int) {
            val film = TextureFilm(HeroSprite.tiers(), tier, FRAME_WIDTH, FRAME_HEIGHT)

            idle = Animation(1, true)
            idle.frames(film, 0, 0, 0, 1, 0, 0, 1, 1)

            run = Animation(20, true)
            run.frames(film, 2, 3, 4, 5, 6, 7)

            die = Animation(20, false)
            die.frames(film, 0)

            attack = Animation(15, false)
            attack!!.frames(film, 13, 14, 15, 0)

            idle()
        }
    }

    companion object {
        private const val FRAME_WIDTH = 12
        private const val FRAME_HEIGHT = 15

        private const val DS_FILE = "darkspirit.dat"
        private const val DEPTH = "depth"
        private const val PERK = "perk"
        private const val USERNAME = "username"
        private const val LEVEL = "level"
        private const val ARMOR = "armor"

        private const val POTIONS = "potions"
        private const val POTION_CD = "potioncd"

        private var depth = -1

        private var heroClass = HeroClass.ROGUE
        private var perk: Perk? = null
        private var userName = "无名的怨魂"
        private var level = 1
        private var armor: Armor? = null

        fun Leave() {
            if (Dungeon.depth < 5 || abs(Dungeon.depth - Dungeon.hero.lvl) > 5) return

            // those who won, die far above their max depth, or who are challenged drop no bones.
            if (Statistics.AmuletObtained || (Statistics.DeepestFloor - 5) >= depth || Dungeon.IsChallenged())
                return

            depth = Dungeon.depth

            val hero = Dungeon.hero
            heroClass = hero.heroClass
            val initperks = heroClass.initialPerks()
            val perks = hero.heroPerk.perks.filter { p -> initperks.all { it.javaClass != p.javaClass } }
            if (perks.isEmpty()) {
                depth = -1
                return
            }

            perk = perks.random()
            userName = hero.userName
            level = hero.lvl
            armor = hero.belongings.armor

            save()
        }

        fun Gen(): DarkSpirit? {
            if (depth < 0 || !Load()) return null

            if (depth != Dungeon.depth || Dungeon.IsChallenged()) return null

            Game.instance.deleteFile(DS_FILE)

            return DarkSpirit()
        }

        private fun save() {
            val b = Bundle().apply {
                put(DEPTH, depth)
                put(PERK, perk)
                put(USERNAME, userName)
                put(LEVEL, level)
                put(ARMOR, armor)
            }
            heroClass.storeInBundle(b)

            try {
                val fout = Game.instance.openFileOutput(DS_FILE, Game.MODE_PRIVATE)
                Bundle.write(b, fout)
                fout.close()
            } catch (e: IOException) {
                DarkestPixelDungeon.reportException(e)
            }
        }

        fun Load(): Boolean {
            try {
                val fin = Game.instance.openFileInput(DS_FILE)
                val bundle = Bundle.read(fin)
                fin.close()

                depth = bundle.getInt(LEVEL)
                heroClass = HeroClass.RestoreFromBundle(bundle)
                perk = bundle.get(PERK) as Perk?
                userName = bundle.getString(USERNAME)
                level = bundle.getInt(LEVEL)
                armor = bundle.get(ARMOR) as Armor?

                return true
            } catch (e: IOException) {
                return false
            }
        }

    }
}