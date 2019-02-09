package com.egoal.darkestpixeldungeon.windows

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.DungeonTilemap
import com.egoal.darkestpixeldungeon.items.unclassified.ExtractionFlask
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.features.AlchemyPot
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.ui.RedButton
import com.egoal.darkestpixeldungeon.ui.Window
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.audio.Sample

private const val WIDTH = 116f
private const val BTN_SIZE = 32f
private const val BTN_GAP = 2f

class WndAlchemy : Window() {
    private var inputButtons: Array<BtnItem>

    private var refineButton: RedButton

    init {
        val titleBar = IconTitle().apply {
            icon(DungeonTilemap.tile(Terrain.ALCHEMY))
            label(Messages.get(WndAlchemy::class.java, "title"))
            setRect(0f, 0f, WIDTH, 0f)
        }
        add(titleBar)

        var h = titleBar.height() + 6f

        val inputLeft = (WIDTH - BTN_SIZE * AlchemyPot.MAX_INPUTS - BTN_GAP * (AlchemyPot.MAX_INPUTS - 1)) / 2f

        inputButtons = Array(AlchemyPot.MAX_INPUTS) { index: Int ->
            object : BtnItem() {
                override fun onSlotClick() {
                    super.onSlotClick()
                    // give back to bag failed.
                    if (item != null && !item!!.collect())
                        Dungeon.level.drop(item, Dungeon.hero.pos)
                    item(null)
                    GameScene.selectItem(itemSelector, WndBag.Mode.ALCHEMY, Messages.get(WndAlchemy::class.java, "select"))
                }
            }.also {
                it.setRect(inputLeft + (BTN_SIZE + BTN_GAP) * (index % 3), h + (BTN_SIZE + BTN_GAP) * (index / 3), BTN_SIZE, BTN_SIZE)
                add(it)
            }
        }

        h += (BTN_SIZE + BTN_GAP) * ((inputButtons.size + 1) / 3)

        refineButton = object : RedButton(Messages.get(this, "refine")) {
            override fun onClick() {
                super.onClick()
                combine()
            }
        }.apply {
            enable(false)
            setRect(0f, h + 10f, WIDTH, 20f)
        }
        add(refineButton)

        resize(WIDTH.toInt(), refineButton.bottom().toInt())
    }

    private val itemSelector = object : WndBag.Listener {
        override fun onSelect(item: Item?) {
            if (item != null) {
                for (btn in inputButtons)
                    if (btn.item == null) {
                        btn.item(item.detach(Dungeon.hero.belongings.backpack))
                        break
                    }
                updateState()
            }
        }
    }

    var result: Item? = null
    // called then item changed
    private fun updateState() {
        val pr = AlchemyPot.VerifyRefinement(inputButtons.mapNotNull { it.item })
        result = pr.second
        refineButton.enable(pr.first)
    }

    private fun combine() {
        if(result==null)
            GLog.n(Messages.get(ExtractionFlask::class.java, "refine_failed"))
        else
            AlchemyPot.OnCombined(inputButtons.mapNotNull { it.item }, result!!)
        for (btn in inputButtons)
            btn.item(null)
        refineButton.enable(false)

        Sample.INSTANCE.play(Assets.SND_PUFF)
    }

    override fun destroy() {
        // give back items
        synchronized(inputButtons) {
            for (btn in inputButtons) {
                if (btn.item != null)
                    if (!btn.item!!.collect())
                        Dungeon.level.drop(btn.item, Dungeon.hero.pos)
            }
        }

        super.destroy()
    }

    //todo: player may lost items if quit with alchemy window open.
}