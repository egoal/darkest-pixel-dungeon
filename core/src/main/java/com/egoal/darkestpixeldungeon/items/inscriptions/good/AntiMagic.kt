package com.egoal.darkestpixeldungeon.items.inscriptions.good

import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.items.EquipableItem
import com.egoal.darkestpixeldungeon.items.inscriptions.Inscription
import com.egoal.darkestpixeldungeon.sprites.ItemSprite

class AntiMagic : Inscription() {
    override fun procTakenDamage(equipment: EquipableItem, dmg: Damage) {
        if (dmg.type == Damage.Type.MAGICAL)
            dmg.value = (dmg.value.toFloat() * 0.75f).toInt()
    }
    
    override fun glowing(): ItemSprite.Glowing = ItemSprite.Glowing(0x88eeff) // teal
}