package com.egoal.darkestpixeldungeon.actors.mobs.npcs

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.items.Amulet
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.sprites.MobSprite
import com.egoal.darkestpixeldungeon.windows.WndOptions
import com.egoal.darkestpixeldungeon.windows.WndQuest
import com.watabou.noosa.MovieClip
import com.watabou.noosa.TextureFilm

class SPDBattleMage : NPC() {
    init {
        spriteClass = Sprite::class.java

        properties.add(Property.IMMOVABLE)
    }

    override fun interact(): Boolean {
        sprite.turnTo(pos, Dungeon.hero.pos)

        if (Dungeon.hero.belongings.getItem(Amulet::class.java) == null) {
        } else {
        }
        
        GameScene.show(object : WndOptions(Sprite(), name, 
                Messages.get(SPDBattleMage::class.java, "greetings"), 
                Messages.get(SPDBattleMage::class.java, "ac_yourself")){
            override fun onSelect(index: Int) {
                onSelectHero(index)
            }
        })
        
        return false
    }

    private fun onSelectHero(index: Int) {
        when (index) {
            0 -> tell(Messages.get(this, "introduction"))
        }
    }
    
    companion object {
        class Sprite : MobSprite() {
            init {
                texture(Assets.SPD_BATTLE_MAGE)

                val frames = TextureFilm(texture, 16, 16)

                idle = Animation(1, true)
                idle.frames(frames, 0, 1)

                run = Animation(20, true)
                run.frames(frames, 0)

                die = MovieClip.Animation(20, true)
                die.frames(frames, 0)

                play(idle)
            }

            override fun link(ch: Char) {
                super.link(ch)
                
                add(CharSprite.State.CHILLED)
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