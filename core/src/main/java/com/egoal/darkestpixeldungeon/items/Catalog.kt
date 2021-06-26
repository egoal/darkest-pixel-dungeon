package com.egoal.darkestpixeldungeon.items

import com.egoal.darkestpixeldungeon.items.artifacts.*
import com.egoal.darkestpixeldungeon.items.rings.*
import com.egoal.darkestpixeldungeon.items.specials.Astrolabe
import com.watabou.noosa.Game
import com.watabou.utils.Bundle
import java.io.IOException

enum class Catalog(private val items: HashMap<Class<*>, Boolean>) {
    Ring(hashMapOf(
            RingOfArcane::class.java to false,
            RingOfEvasion::class.java to false,
            RingOfResistance::class.java to false,
            RingOfForce::class.java to false,
            RingOfFuror::class.java to false,
            RingOfHaste::class.java to false,
            RingOfCritical::class.java to false,
            RingOfMight::class.java to false,
            RingOfSharpshooting::class.java to false,
            RingOfHealth::class.java to false,
            RingOfWealth::class.java to false
    )),

    ARTIFACT(hashMapOf(
            Astrolabe::class.java to false,
            CapeOfThorns::class.java to false,
            ChaliceOfBlood::class.java to false,
            CloakOfShadows::class.java to false,
            CrackedCoin::class.java to false,
            HornOfPlenty::class.java to false,
            MasterThievesArmband::class.java to false,
            SandalsOfNature::class.java to false,
            TalismanOfForesight::class.java to false,
            TimekeepersHourglass::class.java to false,
            UnstableSpellbook::class.java to false,
            DriedRose::class.java to false,
            LloydsBeacon::class.java to false,
            EtherealChains::class.java to false,
            RiemannianManifoldShield::class.java to false,
            GoldPlatedStatue::class.java to false,
            HandOfTheElder::class.java to false,
            HandleOfAbyss::class.java to false,
            HeartOfSatan::class.java to false,
            CloakOfSheep::class.java to false,
            EyeballOfTheElder.Right::class.java to false,
            EyeballOfTheElder.Left::class.java to false,
            DragonsSquama::class.java to false,
            GoddessRadiance::class.java to false
            )),

    ;

    fun allItems() = items.keys

    fun allSeen() = items.all { it.value }

    fun see(itemClass: Class<*>) {
        if (items[itemClass] == false) {
            items[itemClass] = true
            changed = true
        }
    }

    fun isSeen(itemClass: Class<*>) = items[itemClass] == true

    companion object {
        private const val CATALOG_FILE = "catalog.dat"

        private const val CATELOG = "catalog"

        private var changed = false

        fun IsSeen(itemClass: Class<*>): Boolean {
            for (cat in values())
                if (cat.items.contains(itemClass))
                    return cat.items[itemClass]!!
            return false
        }

        fun SetSeen(itemClass: Class<*>) {
            for (cat in values())
                if (cat.items[itemClass] == false) {
                    cat.items[itemClass] = true
                    changed = true
                }

            // validate items identified
        }

        fun Save() {
            if (!changed) return

            val bundle = Bundle()

            val items = ArrayList<Class<*>>()
            for (cat in values()) {
                for (pr in cat.items)
                    if (pr.value) items.add(pr.key)
            }
            bundle.put(CATELOG, items.toTypedArray())

            val fout = Game.instance.openFileOutput(CATALOG_FILE, Game.MODE_PRIVATE)
            Bundle.write(bundle, fout)
            fout.close()
        }

        fun Load() {
            try {
                val fin = Game.instance.openFileInput(CATALOG_FILE)
                val bundle = Bundle.read(fin)
                fin.close()

                if (bundle.contains(CATELOG)) {
                    val items = bundle.getClassArray(CATELOG)
                    for (item in items) if (item != null) SetSeen(item)
                }

                changed = false
            } catch (e: IOException) {
            }
        }
    }
}