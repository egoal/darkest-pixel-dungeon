package com.egoal.darkestpixeldungeon.sprites

import com.egoal.darkestpixeldungeon.DungeonTilemap
import com.egoal.darkestpixeldungeon.items.Item
import com.watabou.noosa.tweeners.PosTweener
import com.watabou.noosa.tweeners.Tweener
import com.watabou.utils.Callback
import com.watabou.utils.PointF

class MissileSprite : ItemSprite(), Tweener.Listener {
    private var callback: Callback? = null

    init {
        originToCenter()
    }

    fun reset(from: Int, to: Int, item: Item?, listener: Callback?) {
        if (item == null) reset(from, to, 0, null, listener)
        else reset(from, to, item.image(), item.glowing(), listener)
    }

    fun reset(from: Int, to: Int, image: Int, glowing: Glowing?, listener: Callback?) {
        revive()

        view(image, glowing)

        callback = listener

        point(DungeonTilemap.tileToWorld(from))
        val dst = DungeonTilemap.tileToWorld(to)

        val d = PointF.diff(dst, point())
        speed.set(d).normalize().scale(SPEED)

        if (image in FIXED_ROTATION_IMAGES) {
            angularSpeed = 0f
            angle = 135 - (Math.atan2(d.x.toDouble(), d.y.toDouble()) / 3.1415926 * 180).toFloat()
        } else
            angularSpeed = (if (image == 15 || image == 106) 1800 else 900).toFloat()

        val tweener = PosTweener(this, dst, d.length() / SPEED)
        tweener.listener = this
        parent.add(tweener)
    }

    override fun onComplete(tweener: Tweener?) {
        kill()
        callback?.call()
    }

    companion object {
        private const val SPEED = 50f

        private val FIXED_ROTATION_IMAGES = arrayOf(
                ItemSpriteSheet.DART, ItemSpriteSheet.INCENDIARY_DART,
                ItemSpriteSheet.CURARE_DART, ItemSpriteSheet.JAVELIN,
                ItemSpriteSheet.SWALLOW_DART
        )
    }
}