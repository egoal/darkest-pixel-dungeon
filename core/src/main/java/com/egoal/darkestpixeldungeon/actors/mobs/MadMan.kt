package com.egoal.darkestpixeldungeon.actors.mobs

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.MagicMissile
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.armor.RaggedArmor
import com.egoal.darkestpixeldungeon.items.food.Humanity
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.sprites.MobSprite
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.MovieClip
import com.watabou.noosa.TextureFilm
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Callback
import com.watabou.utils.Random
import kotlin.math.max
import kotlin.math.min

/**
 * Created by 93942 on 8/18/2018.b
 */

class MadMan : Mob(), Callback {
    init {
        spriteClass = Sprite::class.java

        HT = 10 + 2 * Dungeon.depth
        HP = HT
        defSkill = 5f + Dungeon.depth

        Config = Config.copy(EXP = min(3 + Dungeon.depth / 2, 12), MaxLevel = Dungeon.depth + 3)
    }

    override fun giveDamage(enemy: Char): Damage {
        if (enemy is Hero) {
            //fixme: bad design
            val lvl = (Config.MaxLevel - 3) / 5
            val dis = Dungeon.level.distance(pos, enemy.pos)
            val value = max(1, Random.IntRange(2, 5) + lvl - dis)
            return Damage(value, this, enemy).type(Damage.Type.MENTAL)
                    .addFeature(Damage.Feature.ACCURATE)
        } else
            return Damage(1, this, enemy).addFeature(Damage.Feature.PURE)
                    .addFeature(Damage.Feature.ACCURATE)
    }

    override fun attackDelay(): Float = 1f

    override fun defendDamage(dmg: Damage): Damage {
        // lower physical defense,
        // magic resistance is really high
        if (dmg.type == Damage.Type.NORMAL)
            dmg.value -= Random.NormalIntRange(0, Dungeon.depth / 5 * 3)

        return dmg
    }

    override fun canAttack(enemy: Char): Boolean {
        return Dungeon.level.distance(pos, enemy.pos) <= SHOUT_RANGE && Ballistica(pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos == enemy.pos
    }

    override fun doAttack(enemy: Char): Boolean {
        if (Dungeon.level.distance(pos, enemy.pos) <= 1)
            return super.doAttack(enemy)

        val visible = Level.fieldOfView[pos] || Level.fieldOfView[enemy.pos]

        if (visible) {
            sprite.zap(enemy.pos)
        } else {
            shout()
        }

        return !visible
    }

    private fun shout() {
        // shout!
        spend(TIME_TO_SHOUT)

        val enemy = enemy!!
        val dmg = giveDamage(enemy)
        if (enemy.checkHit(dmg)) {
            enemy.takeDamage(dmg)
            if (!enemy.isAlive && enemy === Dungeon.hero) {
                Dungeon.fail(javaClass)
                GLog.n(Messages.get(this, "shout_kill"))
            }
        } else
            enemy.sprite.showStatus(CharSprite.NEUTRAL, enemy.defenseVerb())
    }

    private fun onZapComplete() {
        // called when zap animation completed
        shout()
        next()
    }

    override fun call() {
        next()
    }

    // sprite
    class Sprite : MobSprite() {
        init {

            texture(Assets.MADMAN)

            val frames = TextureFilm(texture, 12, 14)

            idle = MovieClip.Animation(8, true)
            idle.frames(frames, 0, 0, 0, 0, 0, 0, 1, 1, 2)

            run = MovieClip.Animation(8, true)
            run.frames(frames, 3, 4, 5, 6)

            attack = MovieClip.Animation(8, false)
            attack!!.frames(frames, 10, 11, 12)

            zap = attack!!.clone()

            die = MovieClip.Animation(8, false)
            die.frames(frames, 7, 8, 9)

            play(idle)
        }

        override fun zap(cell: Int) {
            turnTo(ch.pos, cell)
            play(zap)

            MagicMissile.shadow(parent, ch.pos, cell) { (ch as MadMan).onZapComplete() }
            Sample.INSTANCE.play(Assets.SND_BADGE)
        }

        override fun onComplete(anim: MovieClip.Animation) {
            if (anim === zap)
                idle()
            super.onComplete(anim)
        }
    }

    companion object {
        private const val TIME_TO_SHOUT = 1f
        private const val SHOUT_RANGE = 3
    }

}
