package com.egoal.darkestpixeldungeon.actors.mobs

import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.SheepSprite
import com.watabou.utils.Bundle
import com.watabou.utils.Random

class Frog : Mob() {
    var lifespan: Float = 0f
    lateinit var mob_: Mob
    var mob: Mob
        get() = mob_
        set(value) {
            mob_ = value
            HP = mob_.HP
            HT = mob_.HT
        }

    private var initialized = false

    init {
        spriteClass = SheepSprite::class.java
    }

    override fun act(): Boolean {
        if (initialized) {
            // time over
            mob_.resetTime()
            mob_.HP = HP
            mob_.pos = pos // todo: Frog should be movable.
            GameScene.add(mob_, 1f)

            HP = 0
            destroy()
            sprite.die()
        } else {
            initialized = true
            spend(lifespan + Random.Float(2f))
        }

        return true
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put("MOB", mob_)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        mob_ = bundle.get("MOB") as Mob
    }
}