package com.egoal.darkestpixeldungeon.actors.blobs

import com.egoal.darkestpixeldungeon.Journal
import com.egoal.darkestpixeldungeon.effects.BlobEmitter
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.KGenerator
import com.egoal.darkestpixeldungeon.items.artifacts.Artifact
import com.egoal.darkestpixeldungeon.items.helmets.Helmet
import com.egoal.darkestpixeldungeon.items.potions.Potion
import com.egoal.darkestpixeldungeon.items.potions.PotionOfMight
import com.egoal.darkestpixeldungeon.items.potions.PotionOfStrength
import com.egoal.darkestpixeldungeon.items.rings.Ring
import com.egoal.darkestpixeldungeon.items.scrolls.Scroll
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfUpgrade
import com.egoal.darkestpixeldungeon.items.wands.Wand
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.items.weapon.melee.MagesStaff
import com.egoal.darkestpixeldungeon.items.weapon.melee.MeleeWeapon
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.plants.Plant

class WaterOfTransmutation : WellWater() {
    override fun affectItem(item: Item): Item? {
        val new = when (item) {
            is MagesStaff -> changeStaff(item)
            is MeleeWeapon -> changeWeapon(item)
            is Scroll -> changeScroll(item)
            is Potion -> changePotion(item)
            is Ring -> changeRing(item)
            is Wand -> changeWand(item)
            is Plant.Seed -> changeSeed(item)
            is Artifact -> changeArtifact(item)
            is Helmet -> changeHelmet(item)
            else -> null
        }

        if (new != null) Journal.remove(Journal.Feature.WELL_OF_TRANSMUTATION)
        return new
    }

    private fun changeStaff(staff: MagesStaff): MagesStaff? {
        return staff.wandClass()?.let {
            var wand = KGenerator.WAND.generate() as Wand
            while (wand.javaClass == it)
                wand = KGenerator.WAND.generate() as Wand
            wand.level(0)
            staff.imbueWand(wand, null)

            staff
        }
    }

    private fun changeWeapon(weap: MeleeWeapon): Weapon? {
        var newWeap = KGenerator.WEAPON.tier(weap.tier).generate()
        while (newWeap !is MeleeWeapon || weap.javaClass == newWeap.javaClass)
            newWeap = KGenerator.WEAPON.tier(weap.tier).generate()

        val n = newWeap as Weapon
        n.level(0)
        val lvl = weap.level()
        if (lvl > 0) n.upgrade(lvl) else if (lvl < 0) n.degrade(-lvl)

        n.enchantment = weap.enchantment
        n.levelKnown = weap.levelKnown
        n.cursedKnown = weap.cursedKnown
        n.cursed = weap.cursed
        n.imbue = weap.imbue

        return n
    }

    private fun changeRing(r: Ring): Ring? {
        var n = KGenerator.RING.generate() as Ring
        while (n.javaClass == r.javaClass) n = KGenerator.RING.generate() as Ring
        n.level(0)
        
        val lvl = r.level()
        if (lvl > 0) n.upgrade(lvl) else if (lvl < 0) n.degrade(-lvl)

        n.levelKnown = r.levelKnown
        n.cursedKnown = r.cursedKnown
        n.cursed = r.cursed

        return n
    }

    private fun changeArtifact(a: Artifact): Artifact? {
        val n = KGenerator.ARTIFACT.generate()

        if (n !is Artifact) return null // if we run out of artifacts, do nothing

        n.cursedKnown = a.cursedKnown
        n.cursed = a.cursed
        n.levelKnown = a.levelKnown
        n.transferUpgrade(a.visiblyUpgraded())

        return n
    }

    private fun changeWand(w: Wand): Wand? {
        var n = KGenerator.WAND.generate() as Wand
        while (n.javaClass == w.javaClass) n = KGenerator.WAND.generate() as Wand
        n.level(0)
        
        n.upgrade(w.level())

        n.levelKnown = w.levelKnown
        n.cursedKnown = w.cursedKnown
        n.cursed = w.cursed

        return n
    }

    private fun changeSeed(s: Plant.Seed): Plant.Seed? {
        var n = KGenerator.SEED.generate() as Plant.Seed
        while (n.javaClass == s.javaClass) n = KGenerator.SEED.generate() as Plant.Seed

        return n
    }

    private fun changeScroll(s: Scroll): Scroll? = if (s is ScrollOfUpgrade) null else {
        var n = KGenerator.SCROLL.generate() as Scroll
        while (n.javaClass == s.javaClass) n = KGenerator.SCROLL.generate() as Scroll
        n
    }

    private fun changePotion(p: Potion): Potion? = when (p) {
        is PotionOfStrength -> PotionOfMight()
        is PotionOfMight -> PotionOfStrength()
        else -> {
            var n = KGenerator.POTION.generate() as Potion
            while (n.javaClass == p.javaClass) n = KGenerator.POTION.generate() as Potion
            n
        }
    }

    private fun changeHelmet(h: Helmet): Helmet? {
        var n = KGenerator.HELMET.generate() as Helmet
        while (n.javaClass == h.javaClass) n = KGenerator.HELMET.generate() as Helmet
        return n
    }

    override fun use(emitter: BlobEmitter) {
        super.use(emitter)
        emitter.pour(Speck.factory(Speck.CHANGE), 0.3f)
    }
    
    override fun tileDesc(): String = Messages.get(this, "desc")
}