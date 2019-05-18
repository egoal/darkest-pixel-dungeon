package com.egoal.darkestpixeldungeon

import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.watabou.utils.Bundlable
import com.watabou.utils.Bundle

object Journal {
    // compatible 
    enum class Feature {
        WELL_OF_HEALTH,
        WELL_OF_AWARENESS,
        WELL_OF_TRANSMUTATION,
        ALCHEMY,
        GARDEN,
        SACRIFICIAL_FIRE;
        
        fun desc(): String = M.L(this, name)
    }

    val records = mutableListOf<Record>()

    fun reset() {
        records.clear()
    }

    fun add(feature: Feature) {
        add(feature.desc())
    }

    fun add(desc: String) {
        if (records.find { it.depth == Dungeon.depth && it.desc == desc } != null) return
        records.add(Record(Dungeon.depth, desc))
    }

    fun remove(feature: Feature){
        remove(feature.desc())
    }
    
    fun remove(desc: String) {
//        records.removeIf {
//            it.desc == desc && it.depth == Dungeon.depth
//        }
        val left = records.filterNot { it.depth == Dungeon.depth && it.desc == desc }
        records.clear()
        records.addAll(left)
    }

    fun storeInBundle(bundle: Bundle) {
        bundle.put(STR_JOURNAL, records)
    }

    fun restoreFromBundle(bundle: Bundle) {
        records.clear()
        for (rec in bundle.getCollection(STR_JOURNAL))
            records.add(rec as Record)
    }

    class Record(var depth: Int = -1, var desc: String = "") : Bundlable {
        override fun restoreFromBundle(bundle: Bundle) {
            desc = bundle.getString(STR_FEATURE)
            depth = bundle.getInt(STR_DEPTH)
        }

        override fun storeInBundle(bundle: Bundle) {
            bundle.put(STR_FEATURE, desc)
            bundle.put(STR_DEPTH, depth)
        }
    }

    private const val STR_FEATURE = "feature"
    private const val STR_DEPTH = "depth"

    private const val STR_JOURNAL = "journal"
}