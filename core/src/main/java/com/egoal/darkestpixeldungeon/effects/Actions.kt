package com.egoal.darkestpixeldungeon.effects

import com.watabou.noosa.Visual
import com.watabou.utils.GameMath

//todo: embed in visual
abstract class Action {
    abstract fun update(dt: Float, visual: Visual)

    abstract fun done(): Boolean

    abstract class ActionInterval(protected val duration: Float) : Action() {
        protected var time = 0f

        override fun update(dt: Float, visual: Visual) {
            if (!done()) time += dt
        }

        override fun done() = time >= duration
    }

    class Delay(duration: Float) : ActionInterval(duration)

    open class Fade(duration: Float, private val from: Float, private val to: Float) : ActionInterval(duration) {
        override fun update(dt: Float, visual: Visual) {
            super.update(dt, visual)

            visual.alpha(GameMath.Lerp(time / duration, from, to))
        }
    }

    class FadeIn(duration: Float) : Fade(duration, 0f, 1f)

    class FadeOut(duration: Float) : Fade(duration, 1f, 0f)

    class FuncCall(private val callback: (Visual) -> Unit) : Action() {
        private var called = false

        override fun update(dt: Float, visual: Visual) {
            callback(visual)
            called = true
        }

        override fun done(): Boolean = called
    }

    class Sequence(vararg actions: Action) : Action() {
        private val remainActions = actions
        private var index = 0

        override fun update(dt: Float, visual: Visual) {
            if (done()) return

            remainActions[index].update(dt, visual)
            if (remainActions[index].done()) ++index
        }

        override fun done(): Boolean = index >= remainActions.size
    }
}
