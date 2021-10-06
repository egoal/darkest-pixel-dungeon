package com.egoal.darkestpixeldungeon.items.specials

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.DungeonTilemap
import com.egoal.darkestpixeldungeon.Statistics
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Invisibility
import com.egoal.darkestpixeldungeon.actors.buffs.Paralysis
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.Beam
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.particles.ShaftParticle
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.CellSelector
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle

class Shadowmoon : Special() {
    private var rest = 0
    private var hits = 0

    init {
        image = ItemSpriteSheet.SHADOWMOON
    }

    private val isNight get() = Statistics.Clock.state != Statistics.ClockTime.State.Day

    fun evasionProb(): Float = if (isNight) 0.1f else 0f

    fun rest() {
        if (!isNight) return

        rest++
        if (rest >= 3) {
            Sample.INSTANCE.play(Assets.SND_MELD)
            Buff.affect(Dungeon.hero, Sha::class.java).pos = Dungeon.hero.pos
        }
    }

    override fun use(hero: Hero) {
        if (hits >= HIT_TIMES) GameScene.selectCell(selector)
    }

    private val selector = object : CellSelector.Listener {
        override fun onSelect(cell: Int?) {
            if (cell == null || !Dungeon.visible[cell]) return
            val enemy = Actor.findChar(cell)
            if (enemy == null || curUser.isCharmedBy(enemy))
                GLog.w(M.L(Shadowmoon::class.java, "bad_target"))
            else
                moonlight(enemy)
        }

        override fun prompt(): String = M.L(Shadowmoon::class.java, "prompt")
    }

    fun hit() {
        if (hits < HIT_TIMES) {
            hits++
            if (hits == HIT_TIMES) {
                GLog.w(M.L(this, "ready"))
                image = ItemSpriteSheet.SHADOWMOON_RDY
            }
            updateQuickslot()
        }
    }

    override fun status(): String? = if (hits == HIT_TIMES) null else "${HIT_TIMES - hits}"

    private fun moonlight(enemy: Char) {
        hits = 0
        image = ItemSpriteSheet.SHADOWMOON
        updateQuickslot()

        val hero = curUser

        val dmg = hero.giveDamage(enemy).type(Damage.Type.MAGICAL)
        enemy.takeDamage(enemy.defendDamage(dmg))
        if (enemy.isAlive && isNight) Buff.prolong(enemy, Paralysis::class.java, 1f)

        curUser.sprite.parent.add(Beam.LightRay(
                DungeonTilemap.tileCenterToWorld(enemy.pos - Dungeon.level.width()),
                DungeonTilemap.tileCenterToWorld(enemy.pos)
        ))

        if (isNight) {
            CellEmitter.get(enemy.pos).start(ShaftParticle.FACTORY, .4f, 6)
            Sample.INSTANCE.play(Assets.SND_MOONLIGHT)
        } else CellEmitter.get(enemy.pos).start(ShaftParticle.FACTORY, .2f, 3)

        hero.sprite.operate(enemy.pos)
//        hero.spendAndNext(1f)
        hero.busy()
        hero.spend(1f)
    }

    class Sha : Invisibility() {
        var pos: Int = 0

        override fun act(): Boolean {
            if (target.pos != pos) detach()
            else spend(TICK)

            return true
        }

        override fun detach() {
            (target as Hero).belongings.getSpecial(Shadowmoon::class.java)?.rest = 0
            super.detach()
        }

        override fun toString(): String = M.L(this, "name")

        override fun desc(): String = M.L(this, "desc")

        override fun storeInBundle(bundle: Bundle) {
            super.storeInBundle(bundle)
            bundle.put(POS, pos)
        }

        override fun restoreFromBundle(bundle: Bundle) {
            super.restoreFromBundle(bundle)
            pos = bundle.getInt(POS)
        }
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(HIT_STR, hits)
        bundle.put(REST_STR, rest)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        hits = bundle.getInt(HIT_STR)
        rest = bundle.getInt(REST_STR)
    }


    companion object {
        private const val HIT_TIMES = 6
        private const val POS = "pos"
        private const val REST_STR = "rest"
        private const val HIT_STR = "hits"
    }
}