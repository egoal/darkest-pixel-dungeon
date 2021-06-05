package com.egoal.darkestpixeldungeon.actors.mobs

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Badges
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.PropertyConfiger
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.blobs.ToxicGas
import com.egoal.darkestpixeldungeon.actors.buffs.*
import com.egoal.darkestpixeldungeon.effects.Flare
import com.egoal.darkestpixeldungeon.items.artifacts.LloydsBeacon
import com.egoal.darkestpixeldungeon.items.books.TomeOfUpgrade
import com.egoal.darkestpixeldungeon.items.helmets.CrownOfDwarf
import com.egoal.darkestpixeldungeon.items.keys.SkeletonKey
import com.egoal.darkestpixeldungeon.items.unclassified.ArmorKit
import com.egoal.darkestpixeldungeon.items.weapon.inscriptions.Grim
import com.egoal.darkestpixeldungeon.levels.CityBossLevel
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.KingSprite
import com.egoal.darkestpixeldungeon.sprites.UndeadSprite
import com.egoal.darkestpixeldungeon.ui.BossHealthBar
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle
import com.watabou.utils.PathFinder
import com.watabou.utils.Random
import java.util.*
import kotlin.math.min

class King : Mob() {
    init {
        spriteClass = KingSprite::class.java

        PropertyConfiger.set(this, "King")
        if (!Revivable) criticalChance += 0.15f
    }

    private var anger = 0f

    override fun viewDistance(): Int = 6 // do not affect by daytime

    override fun takeDamage(dmg: Damage): Int {
        val value = super.takeDamage(dmg)
        Dungeon.hero.buff(LockedFloor::class.java)?.addTime(dmg.value.toFloat())

        val p = HP / HT.toFloat()
        var ratio = when {
            p > 0.5f -> 1f
            p > 0.1f -> 1.5f
            else -> 2f
        }
        if (Revivable) ratio *= 0.75f

        anger = min(120f, anger + value.toFloat() * ratio)

        return value
    }

    override fun act(): Boolean {
        // GLog.i(anger.toString())
        if (anger > 100f) {
            doSpecial()
            return true
        }
        return super.act()
    }

    private fun doSpecial() {
        // rise
        if ((Dungeon.level as CityBossLevel).activateAllStatuaries()) {
            say(M.L(this, "arise"))
            Flare(3, 32f).color(0x000000, false).show(sprite, 2f)
            spend(2f)

            anger -= 100f
            val undeads = Dungeon.level.mobs.filter { it is Undead }
            undeads.forEach { it.beckon(Dungeon.hero.pos) }
            Buff.prolong(this, LifeLink::class.java, 8f).linker = undeads.random().id()
            return
        }

        val heads = Dungeon.level.mobs.filter { it is MobSpawner }
        if (heads.size >= 3) {
            heads.forEach { (it as MobSpawner).rise() }
            say(M.L(this, "arise"))
            Flare(3, 32f).color(0x000000, false).show(sprite, 2f)
            spend(heads.size.toFloat() / 2f)

            anger -= min(100f, heads.size * 20f)
            Dungeon.level.mobs.filter { it is Undead }.forEach { it.beckon(Dungeon.hero.pos) }
            return
        }

        if (buff(LifeLink::class.java) == null) {
            // do life link
            Dungeon.level.mobs.find { it is Undead }!!.let {
                Buff.prolong(this, LifeLink::class.java, 8f).linker = it.id()
            }
            spend(1f)
        } else {
            SHLD = min(SHLD + 40, 60)
            spend(1f)
        }

        anger -= 40f
    }

    override fun getCloser(target: Int): Boolean {
        // retreat
        if (HP < HT / 2 && Dungeon.level.mobs.count { it is Undead } > 3)
            return super.getFurther(target)

        return super.getCloser(target)
    }

    override fun die(cause: Any?) {
        if (Revivable) {
            Revivable = false

            // summon
            Dungeon.level.mobs.filterIsInstance<MobSpawner>().forEach { it.rise() }

            super.die(cause)

            val head = MobSpawner(King::class.java, 5)
            head.pos = pos
            GameScene.add(head)
            head.sprite.tint(0x111100)

            if (Dungeon.visible[pos]) Sample.INSTANCE.play(Assets.SND_BONES)

            yell(M.L(this, "immortal", Dungeon.hero.givenName()))
        } else {
            Revivable = true

            // win the level
            GameScene.bossSlain()

            val avals = PathFinder.NEIGHBOURS8.map { it + pos }.filter {
                (Level.passable[it] || Level.avoid[it])
            }

            for (item in listOf(SkeletonKey(Dungeon.depth), ArmorKit(), CrownOfDwarf(), TomeOfUpgrade())) {
                val cell = if (avals.isEmpty()) pos else avals.random()
                Dungeon.level.drop(item, cell).sprite.drop(pos)
            }

            // remove undead
            Dungeon.level.mobs.filter { it is Undead }.forEach { (it as Undead).realDie() }
            Dungeon.level.mobs.filter { it is MobSpawner }.forEach { (it as MobSpawner).die(null) } // fixme: do not kill hero's ally

            super.die(cause)

            Badges.validateBossSlain()

            Dungeon.hero.belongings.getItem(LloydsBeacon::class.java)?.upgrade()

            yell(M.L(this, "defeated"))
        }
    }

    override fun notice() {
        super.notice()
        BossHealthBar.assignBoss(this)
        val k = if (Revivable) "notice" else "notice2"
        if (Dungeon.visible[pos]) say(M.L(this, k))
        else yell(M.L(this, k))
    }

    override fun immunizedBuffs(): HashSet<Class<*>> = IMMUNITIES

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(ANGER, anger)
        bundle.put(REVIVABLE, Revivable)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        anger = bundle.getFloat(ANGER)
        if (bundle.contains(REVIVABLE))
            Revivable = bundle.getBoolean(REVIVABLE)
    }

    class Undead : Mob() {
        init {
            PropertyConfiger.set(this, "King.Undead")
            spriteClass = UndeadSprite::class.java
            state = WANDERING
        }

        override fun attackProc(dmg: Damage): Damage {
            if (Random.Float() < 0.15f) Buff.prolong(dmg.to as Char, Paralysis::class.java, 1f)
            return super.attackProc(dmg)
        }

        override fun takeDamage(dmg: Damage): Int {
            if (dmg.from is ToxicGas) (dmg.from as ToxicGas).clear(pos)
            return super.takeDamage(dmg)
        }

        override fun die(cause: Any?) {
            super.die(cause)

            val head = MobSpawner(Undead::class.java, Random.Int(10, 20))
            head.pos = pos
            GameScene.add(head)

            if (Dungeon.visible[pos]) Sample.INSTANCE.play(Assets.SND_BONES)
        }

        fun realDie() {
            super.die(null)
            if (Dungeon.visible[pos]) Sample.INSTANCE.play(Assets.SND_BONES)
        }

        override fun immunizedBuffs(): HashSet<Class<*>> = UNDEAD_IMMUNITIES
    }

    companion object {
        private val IMMUNITIES = hashSetOf<Class<*>>(
                Paralysis::class.java, Vertigo::class.java, Corruption::class.java,
                Terror::class.java, Charm::class.java, MagicalSleep::class.java
        )

        private val UNDEAD_IMMUNITIES = hashSetOf<Class<*>>(
                Grim::class.java, Paralysis::class.java
        )

        private const val ANGER = "anger"
        private const val REVIVABLE = "revivable"

        //todo: this is fragile
        private var Revivable: Boolean = true
    }

}