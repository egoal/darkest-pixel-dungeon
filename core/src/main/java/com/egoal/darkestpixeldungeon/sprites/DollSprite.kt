package com.egoal.darkestpixeldungeon.sprites

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.DarkestPixelDungeon
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.HeroClass
import com.watabou.gltextures.TextureCache
import com.watabou.glwrap.Vertexbuffer
import com.watabou.noosa.Camera
import com.watabou.noosa.Game
import com.watabou.noosa.Image
import com.watabou.noosa.TextureFilm
import com.watabou.utils.Callback
import com.watabou.utils.Point
import com.watabou.utils.PointF
import org.w3c.dom.Text

// paper doll for hero

class DollSprite : CharSprite() {
    private lateinit var fly: IndexedAnimation
    private lateinit var read: IndexedAnimation

    // char sprite is not a group, make this ugly
    private val head = Head()
    private val armor = Armor()

    init {
        initBodyAnimation()

        link(Dungeon.hero)
        updateHead()
        updateArmor()

        armor.flipHorizontal = flipHorizontal
        head.flipHorizontal = head.flipHorizontal

        if (ch.isAlive) idle() else die()
    }

    private fun initBodyAnimation() {
        texture(Assets.HERO_BODY)
        val body = TextureFilm(texture, FRAME_WIDTH, FRAME_HEIGHT)

        idle = IndexedAnimation(1, true)
        idle.frames(body, 0, 0, 0, 1, 0, 0, 1, 1)

        run = IndexedAnimation(RUN_FRAMERATE, true)
        run.frames(body, 2, 3, 4, 5, 6, 7)

        die = IndexedAnimation(20, false)
        die.frames(body, 8, 9, 10, 11, 12, 11)

        attack = IndexedAnimation(15, false)
        attack!!.frames(body, 13, 14, 15, 0)

        zap = attack!!.clone()

        operate = IndexedAnimation(8, false)
        operate!!.frames(body, 16, 17, 16, 17)

        fly = IndexedAnimation(1, true)
        fly.frames(body, 18)

        read = IndexedAnimation(20, false)
        read.frames(body, 19, 20, 20, 20, 20, 20, 20, 20, 20, 19)
    }

    fun updateHead() {
        val hero = ch as Hero
        head.row(hero.heroClass.ordinal)
    }

    fun updateArmor() {
        val hero = ch as Hero
        val tier = hero.tier()
        //todo: clear up
        if (tier == 0 && (hero.heroClass != HeroClass.HUNTRESS && hero.heroClass != HeroClass.SORCERESS))
            armor.row(0)
        else armor.row(tier + 1)
    }

    override fun place(cell: Int) {
        super.place(cell)
         Camera.main.target = this
    }

    override fun turnTo(from: Int, to: Int) {
        super.turnTo(from, to)
        armor.flipHorizontal = flipHorizontal
        head.flipHorizontal = head.flipHorizontal
    }

    override fun move(from: Int, to: Int) {
        super.move(from, to)
        if (ch.flying) play(fly)
         Camera.main.target = this
    }

    override fun jump(from: Int, to: Int, callback: Callback?) {
        super.jump(from, to, callback)
        play(fly)
    }

    fun read() {
        animCallback = Callback {
            idle()
            ch.onOperateComplete()
        }
        play(read)
    }

    // do nothing to reduce the violence rating of the game
    override fun bloodBurstA(from: PointF, damage: Int) {}

    override fun update() {
        sleeping = ch.isAlive && (ch as Hero).resting
        super.update()

        // sync pos
        head.point(point())
        armor.point(point())
    }

    override fun updateAnimation() {
        if (curAnim != null && curAnim.delay > 0 && (curAnim.looped || !finished)) {
            val lastFrame = curFrame

            frameTimer += Game.elapsed
            while (frameTimer > curAnim.delay) {
                frameTimer -= curAnim.delay
                if (curFrame == curAnim.frames.size - 1) {
                    if (curAnim.looped) curFrame = 0
                    finished = true
                    if (listener != null) {
                        listener.onComplete(curAnim)
                        if (curAnim == null) return
                    }
                } else curFrame++
            }

            if (curFrame != lastFrame) {
                // update frames
                frame(curAnim.frames[curFrame])

                val index = (curAnim as IndexedAnimation).frameIndexes[curFrame]
                head.col(index)
                armor.col(index)
            }
        }
    }

    override fun draw() {
        super.draw()
        head.draw()
        armor.draw()
    }

    fun sprint(on: Boolean): Boolean {
        run.delay = if (on) 0.667f / RUN_FRAMERATE else 1f / RUN_FRAMERATE
        return on
    }

    // record indexes
    class IndexedAnimation(fps: Int, looped: Boolean) : Animation(fps, looped) {
        lateinit var frameIndexes: Array<Int>

        override fun frames(film: TextureFilm, vararg frames: Any?): Animation {
            super.frames(film, *frames)

            frameIndexes = Array(frames.size) { frames[it] as Int }

            return this
        }
    }

    open class Component(texFile: String) : Image() {
        private val texRows: TextureFilm
        private lateinit var texGrids: TextureFilm

        init {
            val tex = TextureCache.get(texFile)
            texture(tex)
            texRows = TextureFilm(tex, tex.width, FRAME_HEIGHT)
            set(0, 0)
        }

        fun set(row: Int, col: Int) {
            row(row).col(col)
        }

        fun row(row: Int): Component {
            texGrids = TextureFilm(texRows, row, FRAME_WIDTH, FRAME_HEIGHT)
            return this
        }

        fun col(index: Int): Component {
            frame(texGrids.get(index))
            return this
        }
    }

    class Head : Component(Assets.HERO_HEAD) {}

    class Armor : Component(Assets.HERO_ARMOR) {}

    companion object {
        private const val FRAME_WIDTH = 12
        private const val FRAME_HEIGHT = 15
        private const val RUN_FRAMERATE = 20

        private const val PORTRAIT_SIZE = 26

        private lateinit var tiers: TextureFilm

        private fun Tiers(): TextureFilm {
            if (!::tiers.isInitialized) {
                val tex = TextureCache.get(Assets.HERO_BODY)
                tiers = TextureFilm(tex, tex.width, FRAME_HEIGHT)
            }
            return tiers
        }

        fun Avatar(hc: HeroClass, tier: Int): Image {
            return DollSprite().apply {

            }

//            val patch = Tiers().get(0)
//            val avatar = Image(Assets.HERO_BODY)
//            val frame = avatar.texture.uvRect(1, 0, FRAME_WIDTH, FRAME_HEIGHT)
//            frame.offset(patch.left, patch.top)
//            avatar.frame(frame)
//
//            return avatar
        }

        fun Portrait(hc: HeroClass, tier: Int): Image {
            return Image(Assets.PORTRAITS, 0, PORTRAIT_SIZE * hc.ordinal, PORTRAIT_SIZE, PORTRAIT_SIZE)
        }
    }
}