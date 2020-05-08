package com.egoal.darkestpixeldungeon.items.unclassified

import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Bleeding
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import java.util.ArrayList

class FishBone : Item() {
    init {
        image = ItemSpriteSheet.FISH_BONE
        bones = true

        stackable = true
    }

    override fun isIdentified(): Boolean = true

    override fun actions(hero: Hero): ArrayList<String> {
        val actions = super.actions(hero)
        if (hero.buff(Bleeding::class.java) != null) actions.add(AC_USE)
        return actions
    }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)
        if (action == AC_USE) {
            if (hero.buff(Bleeding::class.java) == null) GLog.w(M.L(this, "not_bleed"))
            else {
                detach(hero.belongings.backpack)

                Buff.detach(hero, Bleeding::class.java)
                if (hero.HP > 1) hero.takeDamage(Damage(1, hero, hero).addFeature(Damage.Feature.PURE))
                hero.spend(1f)
                hero.busy()
                hero.sprite.operate(hero.pos)
            }
        }
    }


    companion object {
        private const val AC_USE = "use"
    }
}