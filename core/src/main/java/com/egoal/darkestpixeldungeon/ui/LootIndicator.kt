package com.egoal.darkestpixeldungeon.ui

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.items.Heap
import com.egoal.darkestpixeldungeon.items.Item

class LootIndicator : Tag(0x1F75CC) {
    init {
        setSize(24f, 24f)

        visible = false
    }

    private lateinit var slot: ItemSlot
    private var lastItem: Item? = null
    private var lastQuantity = 0

    override fun createChildren() {
        super.createChildren()

        slot = object : ItemSlot() {
            override fun onClick() {
                if (Dungeon.hero.handle(Dungeon.hero.pos))
                    Dungeon.hero.next()
            }
        }
        slot.showParams(true, false, false)
        add(slot)
    }

    override fun layout() {
        super.layout()

        slot.setRect(x + 2, y + 3, width - 2, height - 6)
    }

    override fun update() {
        if (Dungeon.hero.ready) {
            val heap = Dungeon.level.heaps.get(Dungeon.hero.pos)

            if (heap != null) {
                val item = when (heap.type) {
                    Heap.Type.CHEST, Heap.Type.MIMIC -> ItemSlot.CHEST
                    Heap.Type.LOCKED_CHEST -> ItemSlot.LOCKED_CHEST
                    Heap.Type.CRYSTAL_CHEST -> ItemSlot.CRYSTAL_CHEST
                    Heap.Type.TOMB -> ItemSlot.TOMB
                    Heap.Type.SKELETON -> ItemSlot.SKELETON
                    Heap.Type.REMAINS -> ItemSlot.REMAINS
                    else -> heap.peek()!!
                }
                if (item !== lastItem || item.quantity() != lastQuantity) {
                    lastItem = item
                    lastQuantity = item.quantity()

                    slot.item(item)
                    flash()
                }
                visible = true
            } else {
                lastItem = null
                visible = false
            }
        }

        slot.enable(visible && Dungeon.hero.ready)

        super.update()
    }
}