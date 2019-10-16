package com.egoal.darkestpixeldungeon.windows

import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.watabou.noosa.Image
import java.util.*

class DialogCommand {
    private val actions = LinkedList<Action>()
    private var lastSelectedIndex = -1
    private val window = Window()

    fun exec() {
        if (actions.isNotEmpty()) {
            window.setAction(actions.poll())
            GameScene.show(window)
        }
    }

    fun onSelect(theIndex: Int) {
        lastSelectedIndex = theIndex

        if (actions.isNotEmpty()) {
            window.setAction(actions.poll())
        }
    }

    inner class Window : WndDialogue(ItemSprite(ItemSpriteSheet.NULLWARN, null), "", "") {
        private lateinit var action: Action

        fun setAction(action: Action) {
            this.action = action

            // update window

        }

        override fun onSelect(idx: Int) {
            action.process(idx, lastSelectedIndex)
            this@DialogCommand.onSelect(idx)
        }
    }

    class Action(val image: Image?, val title: String,
                 val content: String, val options: Array<String>) {
        fun process(choice: Int, lastChoice: Int) {
        }
    }
}
