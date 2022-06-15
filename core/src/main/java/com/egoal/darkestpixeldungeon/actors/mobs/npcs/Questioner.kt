package com.egoal.darkestpixeldungeon.actors.mobs.npcs

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.effects.particles.ElmoParticle
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.items.unclassified.Gold
import com.egoal.darkestpixeldungeon.items.Heap
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.diggers.Rect
import com.egoal.darkestpixeldungeon.levels.traps.RockfallTrap
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.MobSprite
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndDialogue
import com.watabou.noosa.TextureFilm
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle
import com.watabou.utils.Random
import java.lang.RuntimeException

/**
 * Created by 93942 on 10/13/2018.
 */

class Questioner : NPC.Unbreakable() {

    private var question = "pain"
    private lateinit var heldRect: Rect

    init {
        spriteClass = Sprite::class.java
        properties.add(Property.IMMOVABLE)
        properties.add(Property.STATIC)
    }

    fun hold(rect: Rect): Questioner {
        heldRect = rect
        return this
    }

    override fun interact(): Boolean {
        WndDialogue.Show(this, M.L(this, question),
                M.L(this, question + "_0"), M.L(this, question + "_1"), M.L(this, "attack")) {
            onAnswered(it)
        }

        return true
    }

    fun random(): Questioner {
        question = Random.oneOf("pain", "goal", "honour", "fear")
        return this
    }

    private fun onAnswered(index: Int) {
        if (index == 2) {
            yell(Messages.get(this, "silly"))
            RockfallTrap.fallRocks(pos)
        } else {
            yell(Messages.get(this, question + "_pass"))
            Dungeon.hero.takeDamage(Damage(Random.Int(2, 6), this, Dungeon
                    .hero).type(Damage.Type.MENTAL))
            GLog.n(Messages.get(this, "tough"))
        }

        // paint the room
        val mimicRatio = if (index == 2) .35f else .2f
        randomPlaceItem(Gold().random(), Random.Float() < mimicRatio)

        var sp: Item = when (index) {
            0 -> Random.oneOf(Generator.POTION, Generator.SCROLL).generate()
            1 -> Random.oneOf(Generator.WEAPON, Generator.ARMOR).generate()
            2 -> Gold().random()
            else -> throw RuntimeException("cannot be here")
        }
        randomPlaceItem(sp, Random.Float() < mimicRatio)

        if (index == 2) {
            randomPlaceItem(Random.oneOf(Generator.HELMET, Generator.WAND, Generator.ARTIFACT).generate(), true)
        } else
            randomPlaceItem(Generator.generate(), Random.Float() < mimicRatio)

        open()
    }

    private fun randomPlaceItem(item: Item, mimic: Boolean) {
        val heap = Heap()
        heap.type = if (mimic) Heap.Type.MIMIC else Heap.Type.CHEST
        heap.drop(item)

        do {
            heap.pos = Dungeon.level.pointToCell(heldRect.random(0))
        } while (!Level.passable[heap.pos] || Dungeon.level.heaps.get(heap.pos) != null)

        Dungeon.level.heaps.put(heap.pos, heap)
        GameScene.add(heap)
    }

    private fun open() {
        // destroy wall
        Level[pos] = Terrain.EMBERS
        GameScene.updateMap(pos)
        Dungeon.observe()

        // die
        die(null)
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)

        bundle.put(QUESTION, question)
        bundle.put(THE_ROOM, heldRect)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)

        question = bundle.getString(QUESTION)
        heldRect = bundle.get(THE_ROOM) as Rect
    }

    class Sprite : MobSprite() {
        init {
            texture(Assets.QUESTIONER)

            // animations
            val frames = TextureFilm(texture, 16, 16)
            idle = Animation(2, true)
            idle.frames(frames, 0, 1, 2, 3)

            run = Animation(1, true)
            run.frames(frames, 0)

            die = Animation(1, false)
            die.frames(frames, 0)

            play(idle)
        }

        override fun die() {
            super.die()
            emitter().burst(ElmoParticle.FACTORY, 4)

            if (visible)
                Sample.INSTANCE.play(Assets.SND_BURNING)
        }
    }

    companion object {
        private const val THE_ROOM = "room"
        private const val QUESTION = "question"
    }
}
