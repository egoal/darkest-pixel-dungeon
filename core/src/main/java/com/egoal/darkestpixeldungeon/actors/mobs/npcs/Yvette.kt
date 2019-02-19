package com.egoal.darkestpixeldungeon.actors.mobs.npcs

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.MobSprite
import com.egoal.darkestpixeldungeon.windows.WndOptions
import com.watabou.noosa.MovieClip
import com.watabou.noosa.TextureFilm
import com.watabou.utils.Bundle

class Yvette : NPC() {

    init {
        spriteClass = Sprite::class.java
    }

    private var seenBefore = false

    override fun interact(): Boolean {
        sprite.turnTo(pos, Dungeon.hero.pos)
        
        if(Quest.given){
           GameScene.show(object :WndOptions(Sprite(), name, Messages.get(this, "reminder"), 
                   Messages.get(this, "opt-ok"), Messages.get(this, "opt-betray")){
               override fun onSelect(index: Int) {
                   onAnswered(index)
               }
           }) 
        }else{
            GameScene.show(object :WndOptions(Sprite(), name, Messages.get(this, "task"),
                    Messages.get(this, "opt-onit"), Messages.get(this, "opt-betray")){
                override fun onSelect(index: Int) {
                    onAnswered(index)
                }
            })
        }

        return false
    }
    
    private fun onAnswered(index: Int){
        
    }

    override fun act(): Boolean {
        if (Dungeon.visible[pos]) {
            if (!seenBefore)
                yell(Messages.get(this, "hey", Dungeon.hero.givenName()))
            seenBefore = true
        }

        throwItem()
        return super.act()
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
    }

    companion object {

        class Sprite : MobSprite() {
            init {
                texture(Assets.YVETTE)

                // set animations
                val frames = TextureFilm(texture, 10, 14)
                idle = MovieClip.Animation(1, true)
                idle.frames(frames, 0, 1)

                run = MovieClip.Animation(20, true)
                run.frames(frames, 0)

                die = MovieClip.Animation(20, true)
                die.frames(frames, 0)

                play(idle)
            }
        }

        object Quest {
            var given = false
        }
    }

    // unbreakable
    override fun reset() = true

    override fun defenseSkill(enemy: Char) = 1000

    override fun takeDamage(dmg: Damage) = 0

    override fun add(buff: Buff) {}
}