package com.egoal.darkestpixeldungeon.actors.mobs

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Badges
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.PropertyConfiger
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.*
import com.egoal.darkestpixeldungeon.actors.hero.HeroSubClass
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.items.artifacts.LloydsBeacon
import com.egoal.darkestpixeldungeon.items.books.TomeOfPerk
import com.egoal.darkestpixeldungeon.items.keys.SkeletonKey
import com.egoal.darkestpixeldungeon.items.unclassified.MoonStone
import com.egoal.darkestpixeldungeon.items.unclassified.TomeOfMastery
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
import java.util.HashSet

class Tengu : Mob() {
    init {
        PropertyConfiger.set(this, "Tengu")

        spriteClass = TenguSprite::class.java
        HUNTING = Hunting()
    }

    override fun viewDistance(): Int = 6

    private var attackStage = 0
    private val phantoms = hashSetOf<Phantom>()

    override fun onAdd() {
        phantoms.addAll(Dungeon.level.mobs.filter { it is Phantom }.map { it as Phantom })
    }

    override fun giveDamage(enemy: Char): Damage = super.giveDamage(enemy).addFeature(Damage.Feature.RANGED)

    override fun checkHit(dmg: Damage): Boolean {
        return super.checkHit(dmg) && (!dmg.isFeatured(Damage.Feature.RANGED) || super.checkHit(dmg))
    }

    override fun canAttack(enemy: Char): Boolean {
        return Ballistica(pos, enemy.pos, Ballistica.PROJECTILE).collisionPos == enemy.pos
    }

    override fun doAttack(enemy: Char): Boolean {
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

        if (Dungeon.hero === enemy) Dungeon.hero.resting = false

        sprite.attack(enemy.pos)
        spend(attackDelay())

        return true
    }

    override fun takeDamage(dmg: Damage): Int {
        val value = super.takeDamage(dmg)

        if (!isAlive) return value

        Dungeon.hero.buff(LockedFloor::class.java)?.addTime(value * 2f)

        val hpBracket = if (attackStage == 0) 15 else 20
        val bracketExceed = HP < (HT - 30) && (HP + value) / hpBracket != HP / hpBracket

        //todo: code cleanse
        if (attackStage == 0) {
            val switchStage = HP < HT / 2
            if (switchStage) {
                HP = HT / 2 // avoid directly death

                // turn off lights and give blind
                val level = Dungeon.level as PrisonBossLevel
                if (level.isLighted) level.turnLights(false)
                Buff.prolong(Dungeon.hero, Blindness::class.java, 2f)

                Dungeon.hero.buff(Light::class.java)?.detach()

                jumpAway(pos)

                Dungeon.observe()
                GameScene.flash(0x444444)
                Sample.INSTANCE.play(Assets.SND_BLAST)

                yell(M.L(this, "interesting"))

                attackStage = 1
            } else {
                // jump away when hard attack from face
                if (bracketExceed || (value > 10 &&
                                dmg.from is Char && Dungeon.level.adjacent((dmg.from as Char).pos, pos)))
                    jumpAway(pos)
            }
        } else if (attackStage == 1) {
            if (phantoms.isNotEmpty()) {
                // destroy phantoms, jump away
                clearPhantoms()
                jumpAway(pos)
            } else {
                // attack from face, or ranged but bracket exceed
                // phantom strike
                if (dmg.from is Char) {
                    val c = dmg.from as Char
                    if (Dungeon.level.adjacent(c.pos, pos) && Random.Int(5) == 0) jumpAway(pos)
                    else jumpPhantomAttack(c.pos)
                } else if (bracketExceed) jumpAway(pos)
            }
        }

        return value
    }

    override fun die(cause: Any?) {
        clearPhantoms()
        Buff.detach(Dungeon.hero, Ignorant::class.java)
        Buff.detach(Dungeon.hero, MoonNight::class.java)

        Dungeon.level.drop(TomeOfPerk(), pos).sprite.drop()
        Dungeon.level.drop(MoonStone(), pos).sprite.drop()
        Dungeon.level.drop(SkeletonKey(Dungeon.depth), pos).sprite.drop()

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
            if (enemyInFOV && !isCharmedBy(enemy) && canAttack(enemy)) return doAttack(enemy)
            else {
                if (enemyInFOV) target = enemy.pos
                else {
                    chooseEnemy()
                    target = enemy.pos
                }

                spend(Actor.TICK)
                return true
            }
        }
    }

    private val IMMUNITIES = hashSetOf<Class<*>>(Terror::class.java, Corruption::class.java,
            Charm::class.java, Chill::class.java, MagicalSleep::class.java)

    override fun immunizedBuffs(): HashSet<Class<*>> = IMMUNITIES

    class Phantom : Mob() {
        init {
            spriteClass = TenguSprite.Phantom::class.java

            HT = 1
            HP = HT
            EXP = 0
            maxLvl = 1

            defSkill = 10f

            name = M.L(Tengu::class.java, "name")
        }

        override fun description(): String = M.L(Tengu::class.java, "desc")

        fun imitate(tengu: Tengu) {
            HP = tengu.HP
            HT = tengu.HT

            tengu.magicalResistance = magicalResistance // reset tengu's magical resistance.
        }

        override fun attackSkill(target: Char): Float = 20f

        override fun takeDamage(dmg: Damage): Int {
            HP = 0
            die(dmg.from)

            //todo: sfx here
            return 0
        }
    }

    companion object {
        private const val ATTACK_STAGE = "attack_stage"
    }
}