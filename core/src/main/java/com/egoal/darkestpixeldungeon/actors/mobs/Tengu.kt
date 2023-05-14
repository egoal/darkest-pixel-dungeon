package com.egoal.darkestpixeldungeon.actors.mobs

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Badges
import com.egoal.darkestpixeldungeon.Database
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.*
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.items.artifacts.LloydsBeacon
import com.egoal.darkestpixeldungeon.items.books.TomeOfPerk
import com.egoal.darkestpixeldungeon.items.keys.SkeletonKey
import com.egoal.darkestpixeldungeon.items.unclassified.MoonStone
import com.egoal.darkestpixeldungeon.items.weapon.melee.TengusKatana
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.PrisonBossLevel
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.TenguSprite
import com.egoal.darkestpixeldungeon.ui.BossHealthBar
import com.egoal.darkestpixeldungeon.ui.HealthIndicator
import com.egoal.darkestpixeldungeon.ui.QuickSlotButton
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle
import com.watabou.utils.PathFinder
import com.watabou.utils.Random
import java.util.*

class Tengu : Mob() {
    init {
        spriteClass = TenguSprite::class.java
        HUNTING = Hunting()
    }

    enum class AttackAction {
        NORMAL, JUMP_AWAY, JUMP_PHANHOM_ATTACK,
    }

    private var nextAction = AttackAction.NORMAL
    //^^^ this is a very stupid compatible fix, but works for now.

    override fun viewDistance(): Int = 6

    private var attackStage = 0
    private val phantoms = hashSetOf<Phantom>()

    override fun onAdd() {
        phantoms.addAll(Dungeon.level.mobs.filter { it is Phantom }.map { it as Phantom })
    }

    override fun giveDamage(enemy: Char): Damage = super.giveDamage(enemy).addFeature(Damage.Feature.RANGED)

    override fun dexRoll(damage: Damage): Float {
        var dex = super.dexRoll(damage)
        if (damage.isFeatured(Damage.Feature.RANGED)) dex *= 2f
        return dex
    }

    override fun canAttack(enemy: Char): Boolean {
        return Ballistica(pos, enemy.pos, Ballistica.PROJECTILE).collisionPos == enemy.pos
    }

    override fun doAttack(enemy: Char): Boolean {
        when (nextAction) {
            AttackAction.NORMAL -> {
                if (phantoms.isNotEmpty()) {
                    // if hero is not adjacent to self or any of phantoms, don't attack
                    val shouldFollow = !Dungeon.level.adjacent(enemy.pos, pos) ||
                            phantoms.any { !Dungeon.level.adjacent(enemy.pos, it.pos) }

                    if (shouldFollow) {
                        clearPhantoms()
                        jumpPhantomAttack(enemy.pos)

                        return true
                    }
                }

//                if (Dungeon.hero === enemy) Dungeon.hero.resting = false

                sprite.attack(enemy.pos)
                spend(attackDelay())
            }
            AttackAction.JUMP_AWAY -> jumpAway(pos)
            AttackAction.JUMP_PHANHOM_ATTACK -> jumpPhantomAttack(enemy.pos)
        }

        return true
    }

    override fun takeDamage(dmg: Damage): Int {
        val value = super.takeDamage(dmg)

        if (!isAlive) return value

        Dungeon.hero.buff(LockedFloor::class.java)?.addTime(value * 2f)

        val hpBracket = if (attackStage == 0) 15 else 20
        val bracketExceed = HP < (HT - 15) && (HP + value) / hpBracket != HP / hpBracket

        //todo: code cleanse
        if (attackStage == 0) {
            val switchStage = HP < HT / 2
            if (switchStage) {
                HP = HT / 2 // avoid directly death

                // turn off lights and give blind
                val level = Dungeon.level as PrisonBossLevel
                if (level.isLighted) level.turnLights(false)
                Buff.prolong(Dungeon.hero, Blindness::class.java, 2f)

                Dungeon.observe()
                GameScene.flash(0x444444)
                Sample.INSTANCE.play(Assets.SND_BLAST)

                yell(M.L(this, "interesting"))

                nextAction = AttackAction.JUMP_AWAY
                attackStage = 1
            } else {
                // jump away when hard attack from face / no where
                if (bracketExceed ||
                        (value >= 15 && (!enemySeen || (dmg.from is Char && Dungeon.level.adjacent((dmg.from as Char).pos, pos)))))
                    nextAction = AttackAction.JUMP_AWAY
            }
        } else if (attackStage == 1) {
            if (phantoms.isNotEmpty()) {
                // destroy phantoms, jump away
                clearPhantoms()
                nextAction = AttackAction.JUMP_AWAY
            } else {
                // attack from face, or ranged but bracket exceed
                // phantom strike
                if (dmg.from is Char) {
                    val c = dmg.from as Char
                    if (!enemySeen || (Dungeon.level.adjacent(c.pos, pos) && Random.Int(4) == 0)) nextAction = AttackAction.JUMP_AWAY
                    else nextAction = AttackAction.JUMP_PHANHOM_ATTACK
                } else if (bracketExceed || (!enemySeen && Random.Int(2) == 0)) nextAction = AttackAction.JUMP_AWAY
            }
        }

        return value
    }

    override fun die(cause: Any?) {
        clearPhantoms()
        Buff.detach(Dungeon.hero, Ignorant::class.java)
        Buff.detach(Dungeon.hero, MoonNight::class.java)

        val avals = PathFinder.NEIGHBOURS8.map { it + pos }.filter {
            (Level.passable[it] || Level.avoid[it])
        }

        for (item in listOf(SkeletonKey(Dungeon.depth), MoonStone(), TomeOfPerk())) {
            val cell = if (avals.isEmpty()) pos else avals.random()
            Dungeon.level.drop(item, cell).sprite.drop(pos)
        }
        if (Random.Int(3) == 0) Dungeon.level.drop(TengusKatana().identify(), pos).sprite.drop()

        GameScene.bossSlain()
        super.die(cause)

        Badges.validateBossSlain()
        Dungeon.hero.belongings.getItem(LloydsBeacon::class.java)?.upgrade()

        yell(M.L(this, "defeated"))
    }

    override fun notice() {
        super.notice()
        BossHealthBar.assignBoss(this)
        yell(M.L(this, "notice_mine", Dungeon.hero.givenName()))
    }

    private fun clearPhantoms() {
        phantoms.filter { it.isAlive }.forEach { it.die(null) }
        phantoms.clear()
    }

    private fun jumpPhantomAttack(enemypos: Int) {
        val availables = PathFinder.NEIGHBOURS8.map { it + enemypos }.filter {
            Level.passable[it] && Dungeon.level.findMobAt(it) == null
        }

        // hide health bar,
        HealthIndicator.instance.target(null)
        QuickSlotButton.target(null)

        if (availables.size < 4) jumpAway(enemypos)
        else {
            // remove buffs
            buffs().filter { it?.type == Buff.buffType.NEGATIVE }.forEach { it.detach() }

            val shuffledposes = availables.shuffled()

            val cntPhantoms = if (Random.Int(3) == 0) 3 else 2
            for (i in 1..cntPhantoms) {
                val p = Phantom().apply {
                    imitate(this@Tengu)

                    pos = shuffledposes[i]
                    state = this.HUNTING
                }
                GameScene.add(p, Random.Float(0.01f, 0.05f))
                phantoms.add(p)
            }

            sprite.move(pos, shuffledposes[0])
            sprite.turnTo(pos, enemypos)
            move(shuffledposes[0])

            spend(Random.Float(0.01f, 0.05f))
        }

        nextAction = AttackAction.NORMAL
    }

    private fun jumpAway(curpos: Int) {
        val JUMP_MIN_DISTANCE = 5

        var newpos: Int
        val waterpos = (Dungeon.level as PrisonBossLevel).hallCenter()
        val waterOK = Dungeon.level.distance(waterpos, curpos) >= 4 &&
                Actor.findChar(waterpos) == null &&
                Dungeon.level.blobs.all { it.value.cur[waterpos] <= 0 }

        if (waterOK && (buff(Burning::class.java) != null || Random.Int(5) == 0))
            newpos = waterpos // jump into water
        else {
            do {
                newpos = Dungeon.level.pointToCell((Dungeon.level as PrisonBossLevel).rtHall.random(1))
            } while (Level.solid[newpos] || Dungeon.level.map[newpos] == Terrain.TRAP ||
                    Dungeon.level.distance(newpos, curpos) < JUMP_MIN_DISTANCE ||
                    Actor.findChar(newpos) != null)
        }

        if (Dungeon.visible[pos]) CellEmitter.get(pos).burst(Speck.factory(Speck.WOOL), 6)

        sprite.move(pos, newpos)
        move(newpos)

        if (Dungeon.visible[newpos]) CellEmitter.get(newpos).burst(Speck.factory(Speck.WOOL), 6)

        Sample.INSTANCE.play(Assets.SND_PUFF)
        spend(1 / speed())

        nextAction = AttackAction.NORMAL
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(ATTACK_STAGE, attackStage)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)

        attackStage = bundle.getInt(ATTACK_STAGE)
        BossHealthBar.assignBoss(this)
        if (HP <= HT / 2) BossHealthBar.bleed(true)
    }

    // tengu is always hunting...
    private inner class Hunting : Mob.Hunting() {
        override fun act(enemyInFOV: Boolean, justAlerted: Boolean): Boolean {
            enemySeen = enemyInFOV

            // jump away does not need to see the enemy.
            if (nextAction == AttackAction.JUMP_AWAY) {
                jumpAway(pos)
                return true
            }

            if (enemyInFOV && !isCharmedBy(enemy!!) && buff(Disarm::class.java) == null && canAttack(enemy!!))
                return doAttack(enemy!!)
            else {
                if (enemyInFOV) target = enemy!!.pos
                else {
                    chooseEnemy()
                    target = enemy!!.pos
                }

                spend(TICK)
                return true
            }
        }
    }

    override fun immunizedBuffs(): HashSet<Class<*>> = IMMUNITIES

    class Phantom : Mob() {
        init {
            Config = Database.ConfigOfMob("Tengu_Phantom")!!

            spriteClass = TenguSprite.Phantom::class.java

            name = M.L(Tengu::class.java, "name")
        }

        override fun description(): String = M.L(Tengu::class.java, "desc")

        fun imitate(tengu: Tengu) {
            HP = tengu.HP
            HT = tengu.HT

            tengu.magicalResistance = magicalResistance // reset tengu's magical resistance.
        }

        override fun takeDamage(dmg: Damage): Int {
            HP = 0
            die(dmg.from)

            //todo: sfx here
            return 0
        }
    }

    companion object {
        private const val ATTACK_STAGE = "attack_stage"

        private val IMMUNITIES = hashSetOf<Class<*>>(Terror::class.java, Corruption::class.java,
                Charm::class.java, Chill::class.java, MagicalSleep::class.java)
    }
}