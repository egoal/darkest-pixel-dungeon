package com.egoal.darkestpixeldungeon.items.armor

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Invisibility
import com.egoal.darkestpixeldungeon.actors.buffs.Terror
import com.egoal.darkestpixeldungeon.actors.buffs.Venom
import com.egoal.darkestpixeldungeon.effects.Flare
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.watabou.noosa.audio.Sample

class SorceressArmor : ClassArmor() {
    init {
        image = ItemSpriteSheet.DPD_ARMOR_SORCERESS
    }

    override fun doSpecial() {
        for (mob in Dungeon.level.mobs) {
            if (Level.fieldOfView[mob.pos] && Dungeon.level.distance(mob.pos, Item.curUser.pos) < 8) {
                Venom().apply {
                    set(5f, Item.curUser.giveDamage(mob).value / 5 + 2)
                }.attachTo(mob)

                Buff.affect(mob, Terror::class.java, 3f).`object` = Item.curUser.id()
            }
        }

        Flare(8, 48f).color(0xff0000, true).show(Item.curUser.sprite, 3f)
        Sample.INSTANCE.play(Assets.SND_DEGRADE)
        Invisibility.dispel()

        Item.curUser.HP -= Item.curUser.HT / 3
        Item.curUser.spendAndNext(Actor.TICK)
    }
}