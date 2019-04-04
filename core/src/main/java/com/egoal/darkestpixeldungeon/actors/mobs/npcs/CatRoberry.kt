package com.egoal.darkestpixeldungeon.actors.mobs.npcs

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.items.food.*
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfTeleportation
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.MobSprite
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.MovieClip
import com.watabou.noosa.TextureFilm

class CatRoberry : NPC() {
    init {
        spriteClass = Sprite::class.java

        properties.add(Property.IMMOVABLE)
    }

    override fun interact(): Boolean {
        sprite.turnTo(pos, Dungeon.hero.pos)

        if (tryGiveItem(MysteryMeat::class.java, "get-mystery-meat") ||
                tryGiveItem(ChargrilledMeat::class.java, "get-chargrilled-meat") ||
                tryGiveItem(StewedMeat::class.java, "get-stewed-meat") ||
                tryGiveItem(FrozenCarpaccio::class.java, "get-frozen-carpaccio"))
            return true

        jump()

        return false
    }

    private fun tryGiveItem(cls: Class<out Food>, words: String): Boolean {
        val ins = Dungeon.hero.belongings.getItem(cls)
        return if (ins != null) {
            ins.detach(Dungeon.hero.belongings.backpack)
            GLog.w(Messages.get(CatRoberry::class.java, "give-meat", ins.name()))
            yell(Messages.get(CatRoberry::class.java, words))
            true
        } else false
    }

    private fun jump() {
        val pos = Dungeon.level.randomRespawnCell()
        ScrollOfTeleportation.appear(this, pos)
        yell(Messages.get(this, "keke"))
        GLog.i(Messages.get(CatRoberry::class.java, "blinked"))
    }

    companion object {
        class Sprite : MobSprite() {
            init {
                texture(Assets.ROBERRY)

                val frames = TextureFilm(texture, 16, 16)
                idle = MovieClip.Animation(1, true)
                idle.frames(frames, 0, 1, 2, 3)

                run = MovieClip.Animation(20, true)
                run.frames(frames, 0)

                die = MovieClip.Animation(20, true)
                die.frames(frames, 0)

                play(idle)
            }
        }
    }

    // unbreakable
    override fun reset() = true

    override fun act(): Boolean {
        throwItem()
        return super.act()
    }

    override fun defenseSkill(enemy: Char): Int = 1000

    override fun takeDamage(dmg: Damage): Int = 0

    override fun add(buff: Buff) = Unit
}