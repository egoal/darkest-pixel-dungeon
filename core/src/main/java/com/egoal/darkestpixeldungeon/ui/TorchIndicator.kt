package com.egoal.darkestpixeldungeon.ui

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.TorchLight
import com.egoal.darkestpixeldungeon.effects.particles.FlameParticle
import com.egoal.darkestpixeldungeon.scenes.PixelScene
import com.watabou.noosa.BitmapText
import com.watabou.noosa.Image

class TorchIndicator : Tag(0xff4c4c) {
    private lateinit var number: BitmapText
    private lateinit var icon: Image
    private var lastNumber = -100 //fixme

    init {
        setSize(24f, 20f)

        visible = true
    }

    override fun createChildren() {
        super.createChildren()

        bg.flipHorizontal(true)

        icon = Icons.TORCH.get()
        add(icon)
        icon.alpha(0.5f)

        number = BitmapText(PixelScene.pixelFont)
        add(number)
    }

    override fun layout() {
        super.layout()

        icon.x = x + (width - icon.width) / 2
        icon.y = y + (height - icon.height) / 2

        placeNumber()
    }

    private fun placeNumber() {
        number.x = x + (width - number.width()) / 2
        number.y = y + (height - number.baseLine()) / 2
        PixelScene.align(number)
    }

    override fun update() {
        var cnt = Dungeon.torch.toInt()
        if (Dungeon.torch - (cnt) > 0f) cnt += 1

        if (visible && cnt != lastNumber) {
            lastNumber = cnt
            number.text("$lastNumber")
            number.measure()
            placeNumber()

            flash()
        }

        if (Dungeon.torch < 0f) {
            bg.alpha(0.5f)
        } else bg.alpha(1f)

        super.update()
    }

    override fun onClick() {
        if (Dungeon.torch <= 0f) return

        val hero = Dungeon.hero
        hero.spend(1f)
        hero.busy()
        hero.sprite.operate(hero.pos)

        // toggle
        val buff = hero.buff(TorchLight::class.java)
        if (buff != null){
            buff.detach()

            icon.alpha(0.5f)
        }
        else {
            Buff.affect(hero, TorchLight::class.java)
            val emitter = hero.sprite.centerEmitter()
            emitter.start(FlameParticle.FACTORY, 0.2f, 3)

            icon.alpha(1f)
        }
    }
}