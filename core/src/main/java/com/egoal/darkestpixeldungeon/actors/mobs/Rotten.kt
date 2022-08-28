package com.egoal.darkestpixeldungeon.actors.mobs

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Ooze
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.mobs.abilities.OozeAttack
import com.egoal.darkestpixeldungeon.effects.MagicMissile
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.sprites.MobSprite
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.TextureFilm
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Callback
import com.watabou.utils.Random

class Rotten : Mob(), Callback {
    init {
        spriteClass = Sprite::class.java

        abilities.add(OozeAttack())
    }

    override fun giveDamage(enemy: Char): Damage {
        val damage = super.giveDamage(enemy)
        if (enemy !is Hero) damage.type(Damage.Type.MAGICAL)
        return damage
    }

    override fun canAttack(enemy: Char): Boolean {
        return Dungeon.level.distance(pos, enemy.pos) <= 4 &&
                Ballistica(pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos == enemy.pos
    }

    override fun defenseProc(dmg: Damage): Damage {
        if (!!dmg.isFeatured(Damage.Feature.RANGED) && Random.Int(3) == 0) Buff.affect(dmg.from as Char, Ooze::class.java)
        return super.defenseProc(dmg)
    }

    override fun doAttack(enemy: Char): Boolean {
        if (Dungeon.level.distance(pos, enemy.pos) <= 1)
            return super.doAttack(enemy)

        val visible = Level.fieldOfView[pos] || Level.fieldOfView[enemy.pos]

        if (visible) {
            sprite.zap(enemy.pos)
        } else {
            hit()
        }

        return !visible
    }

    private fun onZapCompeleted() {
        hit()
        next()
    }

    private fun hit() {
        spend(attackDelay())

        val enemy = enemy!!
        val dmg = giveDamage(enemy)
        if (enemy.checkHit(dmg)) {
            enemy.takeDamage(dmg)
            if (!enemy.isAlive && enemy === Dungeon.hero) {
                Dungeon.fail(javaClass)
                GLog.n(Messages.get(this, "kill"))
            }
        } else
            enemy.sprite.showStatus(CharSprite.NEUTRAL, enemy.defenseVerb())
    }

    override fun call() {
        next()
    }

    class Sprite : MobSprite() {
        init {
            texture(Assets.ROTTEN)

            val frames = TextureFilm(texture, 16, 16)
            idle = Animation(8, true).frames(frames, 0, 1, 2, 3)

            run = idle.clone()

            attack = Animation(8, false).frames(frames, 4, 5, 6, 7)

            zap = attack!!.clone()

            die = Animation(8, false).frames(frames, 8, 9, 10, 11)

            play(idle)
        }

        override fun zap(cell: Int) {
            turnTo(ch.pos, cell)
            play(zap)

            MagicMissile.shadow(parent, ch.pos, cell) { (ch as Rotten).onZapCompeleted() }
            Sample.INSTANCE.play(Assets.SND_TRAP)
        }

        override fun onComplete(anim: Animation) {
            if (anim === zap) idle()
            super.onComplete(anim)
        }
    }
}