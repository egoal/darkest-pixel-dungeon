package com.egoal.darkestpixeldungeon.actors.mobs

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.PropertyConfiger
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.blobs.Fire
import com.egoal.darkestpixeldungeon.actors.hero.HeroLines
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.sprites.MobSprite
import com.watabou.noosa.TextureFilm
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle
import com.watabou.utils.Random
import java.util.HashSet

class AshesSkull : Mob() {
    init {
        PropertyConfiger.set(this, "AshesSkull")

        spriteClass = Sprite::class.java
        flying = true
    }

    private var jumpcd = 0
    private var moving = 0

    // override fun viewDistance(): Int = 6

    override fun getCloser(target: Int): Boolean {
        // move slower, like great crab
        ++moving
        return if (moving < 3) super.getCloser(target)
        else {
            moving = 0
            true
        }
    }

    override fun act(): Boolean {
        if (jumpcd > 0) --jumpcd

        Dungeon.level.updateFieldOfView(this, Level.fieldOfView)

        // copy from guard
        val enemy = enemy
        return if (jumpcd <= 0 && state == HUNTING && paralysed <= 0 && enemy != null && enemy.invisible == 0 &&
                Level.fieldOfView[enemy.pos] && Dungeon.level.distance(pos, enemy.pos) <= 6 &&
                !Dungeon.level.adjacent(pos, enemy.pos) && jump(enemy))
            false
        else super.act()
    }

    private fun jump(target: Char): Boolean {
        jumpcd = COOLDOWN_JUMP //todo: ...

        val b = Ballistica(pos, target.pos, Ballistica.PROJECTILE)
        if (b.collisionPos != target.pos || b.path.size < 3 || b.dist < 1 || !Level.passable[b.path[b.dist - 1]])
            return false

        val dst = b.path[b.dist - 1]
        val attackFromShadow = !Dungeon.visible[pos]

        sprite.jump(pos, dst) {
            move(dst)
            // no press, its flying
            if (attackFromShadow && target === Dungeon.hero) {
                target.sayShort(HeroLines.WHAT)
                target.takeDamage(Damage(Random.Int(1, 5), this@AshesSkull, target).type(Damage.Type.MENTAL))
            }
            target.takeDamage(giveDamage(target).type(Damage.Type.MAGICAL)) // shift to magical

            CellEmitter.center(dst).burst(Speck.factory(Speck.DUST), 6)
            Sample.INSTANCE.play(Assets.SND_TRAP)
            spend(1f)
            next()
        }

        return true
    }

    override fun die(cause: Any?) {
        super.die(cause)

        val dis = Dungeon.level.distance(pos, Dungeon.hero.pos)
        if (Dungeon.hero.isAlive && dis <= 2) {
            if (Random.Int(4) == 0) Dungeon.hero.sayShort(HeroLines.BAD_NOISE)

            val dmg = Damage(Random.NormalIntRange(2, 8), this, Dungeon.hero).type(Damage.Type.MENTAL)
            Dungeon.hero.takeDamage(dmg)
        }

        if (dis < 4) Sample.INSTANCE.play(Assets.SND_HOWL, 1.2f, 1.2f, 1f)
    }

    override fun immunizedBuffs(): HashSet<Class<*>> = IMMUNITIES

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(COOLDOWN_JUMP_STR, jumpcd)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        jumpcd = bundle.getInt(COOLDOWN_JUMP_STR)
    }

    class Sprite : MobSprite() {
        init {
            texture(Assets.SKULL)

            val frames = TextureFilm(texture, 16, 16)

            idle = Animation(2, true)
            idle.frames(frames, 0, 0, 0, 1)

            run = Animation(2, true)
            run.frames(frames, 0, 1)

            attack = Animation(10, false)
            attack.frames(frames, 0, 1, 2, 3, 4)

            die = Animation(8, false)
            die.frames(frames, 0, 4, 3, 5)

            play(idle)
        }

        override fun link(ch: Char) {
            super.link(ch)

            add(State.BURNING)
        }

        override fun die() {
            super.die()

            remove(State.BURNING)
        }

        override fun blood(): Int = 0xffcccccc.toInt()
    }

    companion object {
        private val IMMUNITIES = hashSetOf<Class<*>>(Fire::class.java)

        private const val COOLDOWN_JUMP = 4
        private const val COOLDOWN_JUMP_STR = "jumpcd"
    }
}