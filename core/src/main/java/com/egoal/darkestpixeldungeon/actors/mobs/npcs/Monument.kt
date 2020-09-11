package com.egoal.darkestpixeldungeon.actors.mobs.npcs

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Challenge
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.sprites.MobSprite
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndDialogue
import com.egoal.darkestpixeldungeon.windows.WndSelectChallenge
import com.watabou.noosa.TextureFilm
import com.watabou.utils.Bundle

class Monument : NPC.Unbreakable() {
    init {
        spriteClass = Sprite::class.java
        properties.add(Property.IMMOVABLE)
    }

    private var activated = false

    override fun description(): String = super.description() + "\n\n" + M.L(this, if (activated) "desc_activated" else "desc_inactivated")

    override fun interact(): Boolean {
        if (!activated && Dungeon.hero.lvl == 1) //todo: rework this
            GameScene.show(object : WndSelectChallenge() {
                override fun onChallengeWouldActivate(challenge: Challenge) {
                    val hero = Dungeon.hero
                    if (hero.challenge != null) return

                    activated = true
                    challenge.affect(hero)
                    hero.challenge = challenge
                    hide()
                    GLog.n(M.L(Monument::class.java, "activated", challenge.title()))
                }
            })

        return false
    }

    override fun sprite(): CharSprite {
        val sprite = Sprite()
        if (activated) sprite.activate()

        return sprite
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(ACTIVATED, activated)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        activated = bundle.getBoolean(ACTIVATED)
    }

    companion object {
        private const val ACTIVATED = "activated"
    }

    class Sprite : MobSprite() {
        init {
            texture(Assets.MONUMENT)

            val frames = TextureFilm(texture, 16, 32)
            idle = Animation(1, true)
            idle.frames(frames, 0)

            run = Animation(2, true)
            run.frames(frames, 1, 2, 3, 4, 5)

            die = idle.clone()
            attack = idle.clone()

            play(idle)
        }

        fun activate() {
            play(run)
        }

        fun deactivate() {
            play(idle)
        }
    }
}