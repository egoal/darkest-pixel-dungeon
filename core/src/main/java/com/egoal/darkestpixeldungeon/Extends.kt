package com.egoal.darkestpixeldungeon

import com.watabou.utils.Random

// utils & extend functions
// looks like we cannot extend static method,
object KRandom {
    fun <Key> Chances(map: Map<Key, Float>): Key? {
        val sum = map.values.sum()
        if (sum <= 0f) return null

        var value = Random.Float(sum)
        for (pr in map) {
            value -= pr.value
            if (value < 0f) return pr.key
        }

        return null // not reachable
    }

    fun <Key> Chances(map: Map<Key, Float>, count: Int): List<Key> {
        assert(map.size >= count && count > 0)

        val probmap = HashMap<Key, Float>(map) // copy

        return List(count) {
            val key = Random.chances(probmap)
            probmap.remove(key)
            key
        }
    }

    fun Percent(p: Int) = Random.Int(100) <= p

    fun <T> nOf(seq: List<T>, count: Int): List<T> {
        check(count > 0)
        if (seq.count() <= count) return seq

        val selected = ArrayList<T>()
        for (pr in seq.withIndex()) {
            val p = (count - selected.count()).toFloat() / (seq.count() - pr.index).toFloat()
            if (Random.Float() < p) {
                selected.add(pr.value)
                if (selected.count() >= count) break
            }
        }

        return selected
    }
}
