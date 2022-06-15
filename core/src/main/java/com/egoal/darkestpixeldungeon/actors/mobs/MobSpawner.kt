package com.egoal.darkestpixeldungeon.actors.mobs

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.NPC
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.sprites.MobSprite
import com.watabou.noosa.TextureFilm
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle

//todo: generic
class MobSpawner(mob: Class<out Mob>? = null, left: Int = 5) : NPC.Unbreakable() {
    var Left: Int
        private set
    lateinit var mobClass: Class<out Mob>

    init {
        spriteClass = UndeadHead::class.java

        properties.add(Property.IMMOVABLE)

        Left = left
        if (mob != null) mobClass = mob
    }

    override fun act(): Boolean {
        if (--Left <= 0) {
            rise()
            return true
        }
        say("$Left...", if (Left <= 3) CharSprite.NEGATIVE else CharSprite.DEFAULT)

        return super.act()
    }

    fun rise() {
        val mob = mobClass.newInstance().initialize()
        mob.pos = pos
        GameScene.add(mob)
        die(null)

        if (Dungeon.visible[pos]) Sample.INSTANCE.play(Assets.SND_BONES)
    }

    override fun interact(): Boolean {
        val curpos = pos
        moveSprite(pos, Dungeon.hero.pos)
        move(Dungeon.hero.pos)

        with(Dungeon.hero) {
            sprite.move(pos, curpos)
            move(curpos)

            spend(1 / speed())
            busy()
        }

        return true
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(LEFT_TICK, Left)
        bundle.put(MOB_CLASS, mobClass)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        Left = bundle.getInt(LEFT_TICK)
        mobClass = bundle.getClass(MOB_CLASS) as Class<out Mob>
    }

    class UndeadHead : MobSprite() {
        init {
            texture(Assets.UNDEAD)

            val frames = TextureFilm(texture, 12, 16)

            idle = Animation(1, true)
            idle.frames(frames, 13)

            run = idle.clone()

            attack = idle.clone()

            die = idle.clone()

            play(idle)
        }

        override fun die() {
            super.die()
            if (Dungeon.visible[ch.pos]) emitter().burst(Speck.factory(Speck.BONE), 3)
        }

        override fun blood(): Int = 0xffcccccc.toInt()
    }

    companion object {
        private const val LEFT_TICK = "left-tick"
        private const val MOB_CLASS = "mob-class"
    }
}