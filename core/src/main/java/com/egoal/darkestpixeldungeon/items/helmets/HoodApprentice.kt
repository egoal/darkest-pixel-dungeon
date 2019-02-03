package com.egoal.darkestpixeldungeon.items.helmets

import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.watabou.utils.Random

class HoodApprentice : Helmet(){
    init {
        image = ItemSpriteSheet.HELMET_APPRENTICE
    }

    override fun desc(): String {
        var desc = super.desc()
        if (isIdentified) {
            desc += "\n\n" + Messages.get(this, "effect-desc")
            if (cursed)
                desc += "\n\n" + Messages.get(Helmet::class.java, "cursed_desc")
        }

        return desc
    }

    inner class Apprentice: HelmetBuff(){
        override fun act(): Boolean{
            if(cursed && Random.Int(10)==0)
                target.takeDamage(Damage(1, Char.Nobody(), target).type(Damage.Type.MENTAL))

            return super.act()
        }
    }

}