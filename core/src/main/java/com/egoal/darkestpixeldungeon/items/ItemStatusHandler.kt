/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015  Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2016 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.egoal.darkestpixeldungeon.items

import com.watabou.utils.Bundle
import com.watabou.utils.Random

import java.util.ArrayList
import java.util.HashMap
import java.util.HashSet

class ItemStatusHandler<T : Item> {
    private var items: Array<Class<out T>>
    private var itemLabels: HashMap<Class<out T>, String>
    private var labelImages: HashMap<String, Int>
    private var known: HashSet<Class<out T>>

    constructor(items: Array<Class<out T>>, labelImages: HashMap<String, Int>) {
        // connect item with spritesheet index
        this.items = items

        this.itemLabels = HashMap()
        this.labelImages = HashMap(labelImages)
        known = HashSet()

        val labelsLeft = ArrayList(labelImages.keys)

        for (i in items.indices) {

            val item = items[i]

            val index = Random.Int(labelsLeft.size)

            itemLabels[item] = labelsLeft[index]
            labelsLeft.removeAt(index)

        }
    }

    constructor(items: Array<Class<out T>>, labelImages: HashMap<String, Int>, bundle: Bundle) {
        this.items = items

        this.itemLabels = HashMap()
        this.labelImages = HashMap(labelImages)
        known = HashSet()

        val allLabels = ArrayList(labelImages.keys)

        restore(bundle, allLabels)
    }

    fun save(bundle: Bundle) {
        for (i in items.indices) {
            val itemName = items[i].toString()
            bundle.put(itemName + PFX_LABEL, itemLabels[items[i]])
            bundle.put(itemName + PFX_KNOWN, known.contains(items[i]))
        }
    }

    fun saveSelectively(bundle: Bundle, itemsToSave: ArrayList<T>) {
        for (item in itemsToSave) {
            if (items.contains(item.javaClass)) {
                val cls = items[items.indexOf(item.javaClass)]
                val itemName = cls.toString()
                bundle.put(itemName + PFX_LABEL, itemLabels[cls])
                bundle.put(itemName + PFX_KNOWN, known.contains(cls))
            }
        }
    }

    private fun restore(bundle: Bundle, labelsLeft: ArrayList<String>) {
        val unlabelled = ArrayList<Class<out T>>()

        for (i in items.indices) {
            val item = items[i]
            val itemName = item.toString()

            if (bundle.contains(itemName + PFX_LABEL)) {
                val label = bundle.getString(itemName + PFX_LABEL)
                itemLabels[item] = label
                labelsLeft.remove(label)

                if (bundle.getBoolean(itemName + PFX_KNOWN)) {
                    known.add(item)
                }

            } else {

                unlabelled.add(items[i])

            }
        }

        for (item in unlabelled) {
            val itemName = item.toString()

            val index = Random.Int(labelsLeft.size)
            itemLabels[item] = labelsLeft[index]
            labelsLeft.removeAt(index)

            if (bundle.contains(itemName + PFX_KNOWN) && bundle.getBoolean(itemName + PFX_KNOWN)) {
                known.add(item)
            }
        }
    }

    fun image(item: T): Int = labelImages[label(item)]!!

    fun label(item: T): String = itemLabels[item.javaClass]!!

    fun isKnown(item: T): Boolean = known.contains(item.javaClass)

    fun know(item: T) {
        known.add(item.javaClass as Class<out T>)

        // the last one
        if (known.size == items.size - 1) {
            for (i in items.indices) {
                if (!known.contains(items[i])) {
                    known.add(items[i])
                    break
                }
            }
        }
    }

    fun known(): HashSet<Class<out T>> = known

    fun unknown(): HashSet<Class<out T>> = items.filter { !known.contains(it) }.toHashSet()

    companion object {
        private const val PFX_LABEL = "_label"
        private const val PFX_KNOWN = "_known"
    }
}
