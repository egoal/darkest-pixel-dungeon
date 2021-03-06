package com.egoal.darkestpixeldungeon.actors.mobs.npcs

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.hero.HeroClass
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.sprites.MobSprite
import com.egoal.darkestpixeldungeon.windows.WndDialogue
import com.watabou.noosa.TextureFilm
import com.watabou.utils.Bundle

class Passerby : NPC.Unbreakable() {
    init {
        spriteClass = Sprite::class.java

        properties.add(Property.IMMOVABLE)
    }

    var everMet = false

    override fun interact(): Boolean {
        if (everMet) {
            if (Dungeon.hero.heroClass == HeroClass.MAGE) tell(M.L(this, "didnt_mean_that"))
            else tell(M.L(this, "grow"))
        } else {
            //todo: rework this
            if (Dungeon.hero.heroClass == HeroClass.MAGE) {
                everMet = true
                WndDialogue.Show(this, M.L(this, "mage"), M.L(this, "mage_leave")) {
                    say(M.L(this, "grow_mage"))
                }
            } else
                WndDialogue.Show(this, M.L(this, "greetings"), M.L(this, "what")) {
                    WndDialogue.Show(this, M.L(this, "magic"), M.L(this, "showme")) {
                        WndDialogue.Show(this, M.L(this, "cast"), M.L(this, "what_happend")) {
                            WndDialogue.Show(this, M.L(this, "friendship"), M.L(this, "smile")) {
                                everMet = true
                            }
                        }
                    }
                }
        }

        return false
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(MET, everMet)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        everMet = bundle.getBoolean(MET)
    }

    companion object {
        private const val MET = "met"
    }

    class Sprite : MobSprite() {
        init {
            texture(Assets.PASSERBY)

            // set animations
            val frames = TextureFilm(texture, 16, 16)
            idle = Animation(3, true)
            idle.frames(frames, 0, 1, 2, 3)

            run = Animation(20, true)
            run.frames(frames, 0)

            die = Animation(20, true)
            die.frames(frames, 0)

            play(idle)
        }
    }
}