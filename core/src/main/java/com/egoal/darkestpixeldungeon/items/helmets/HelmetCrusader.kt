package com.egoal.darkestpixeldungeon.items.helmets

import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.watabou.utils.Random

class HelmetCrusader : Helmet() {
    init {
        image = ItemSpriteSheet.HELMET_CRUSADER
    }

    override fun desc(): String {
        var desc = super.desc()
        if (isIdentified) {
            desc += "\n\n" + Messages.get(this, "effect_desc")
            if (cursed)
                desc += "\n\n" + Messages.get(this, "cursed_desc")
        }

        return desc
    }

    override fun random(): Item {
        cursed = Random.Float() < 0.4f
        return this
    }

    override fun buff(): HelmetBuff = Protect()
    
    inner class Protect: HelmetBuff(){
        override fun act(): Boolean {
            if(cursed && Random.Int(10)==0)
                target.takeDamage(Damage(1, com.egoal.darkestpixeldungeon.actors.Char.Nobody(), target).type(Damage.Type.MENTAL))
                
            return super.act()
        }
    }
}