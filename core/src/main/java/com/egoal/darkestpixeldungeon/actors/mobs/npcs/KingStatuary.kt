package com.egoal.darkestpixeldungeon.actors.mobs.npcs

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.items.helmets.CrownOfDwarf
import com.egoal.darkestpixeldungeon.items.unclassified.GreatBlueprint
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.sprites.MobSprite
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndOptions
import com.watabou.noosa.TextureFilm
import com.watabou.utils.Bundle

class KingStatuary : NPC.Unbreakable() {
    init {
        spriteClass = Sprite::class.java

        properties.add(Property.IMMOVABLE)
    }

    private var hasCrown = false

    override fun description(): String = M.L(this, "desc") +
            if (hasCrown) M.L(this, "desc_wear") else M.L(this, "desc_unwear")

    override fun interact(): Boolean {
        if (hasCrown) return false

        GameScene.show(object : WndOptions(Sprite(), name, description(),
                M.L(this, "wear"), M.L(this, "unworthy")) {
            override fun onSelect(index: Int) {
                if (index == 0) onWear()
            }
        })

        return false
    }

    private fun onWear() {
        val crown = Dungeon.hero.belongings.getItem(CrownOfDwarf::class.java)
        if (crown == null) {
            GLog.w(M.L(this, "no_crown"))
            return
        }

        if (crown.isEquipped(Dungeon.hero)) crown.doUnequip(Dungeon.hero, false)
        crown.detach(Dungeon.hero.belongings.backpack)
        GLog.w(M.L(this, "on_wear"))

        hasCrown = true
        (sprite as Sprite).wear()
        Dungeon.level.drop(GreatBlueprint(), Dungeon.hero.pos)
    }

    override fun sprite(): CharSprite = Sprite().apply { if (hasCrown) wear() }

    class Sprite : MobSprite() {
        private val idleCrown: Animation

        init {
            texture(Assets.KING_STATUARY)

            val film = TextureFilm(texture, 16, 16)
            idle = Animation(10, true).apply { frames(film, 0) }

            run = Animation(10, true).apply { frames(film, 0) }

            die = Animation(10, true).apply { frames(film, 0) }

            play(idle)

            idleCrown = Animation(10, true).apply { frames(film, 1) }
        }

        fun wear() {
            play(idleCrown)
        }
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(HAS_CROWN, hasCrown)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        hasCrown = bundle.getBoolean(HAS_CROWN)
    }

    companion object {
        private const val HAS_CROWN = "has-crown"
    }
}