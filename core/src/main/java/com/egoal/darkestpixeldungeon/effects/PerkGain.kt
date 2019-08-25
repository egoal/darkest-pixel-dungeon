package com.egoal.darkestpixeldungeon.effects

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.perks.Perk
import com.watabou.gltextures.TextureCache
import com.watabou.noosa.Game
import com.watabou.noosa.Image
import com.watabou.noosa.TextureFilm

class PerkGain(private val hero: Hero, perk: Perk) : Image(icons) {
    init {
        frame(film.get(perk.image()))
        origin.set(SIZE / 2f)
    }

    private val action = Action.Sequence(Action.Fade(0.2f, 0f, ALPHA), Action.Delay(1f),
            Action.Fade(0.4f, ALPHA, 0f), Action.FuncCall {
        kill()
    })

    override fun update() {
        super.update()

        x = hero.sprite.center().x - SIZE / 2
        y = hero.sprite.y - SIZE

        action.update(Game.elapsed, this)
    }

    companion object {
        //todo: refer to PerkSlot
        private const val SIZE = 16
        private val icons = TextureCache.get(Assets.PERKS)
        private val film = TextureFilm(icons, SIZE, SIZE)

        private const val ALPHA = 0.6f

        fun Show(hero: Hero, perk: Perk) {
            if (!hero.sprite.visible) return

            hero.sprite.parent.add(PerkGain(hero, perk))
        }
    }
}