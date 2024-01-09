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
package com.egoal.darkestpixeldungeon.ui

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.scenes.PixelScene
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.watabou.noosa.Game
import com.watabou.utils.Random
import java.util.*

class AttackIndicator : Tag(0xFF4C4C) {
    private var sprite: CharSprite? = null
    private val candidates = ArrayList<Mob?>()

    init {
        instance = this
        lastTarget = null
        setSize(24f, 24f)
        visible(false)
        enable(false)
    }

    override fun layout() {
        super.layout()

        sprite?.let {
            it.x = x + (width - it.width()) / 2
            it.y = y + (height - it.height()) / 2
            PixelScene.align(it)
        }
    }

    override fun update() {
        super.update()
        if (!bg.visible) {
            enable(false)
            if (delay > 0f) delay -= Game.elapsed
            if (delay <= 0f) active = false
        } else {
            delay = 0.75f
            active = true
            if (Dungeon.hero.isAlive) {
                enable(Dungeon.hero.ready)
            } else {
                visible(false)
                enable(false)
            }
        }
    }

    private fun checkEnemies() {
        candidates.clear()
        candidates.addAll((0 until Dungeon.hero.visibleEnemies())
                .map {
                    Dungeon.hero.visibleEnemy(it)
                }.filter {
                    Dungeon.hero.canAttack(it)
                })

        if (!candidates.contains(lastTarget)) {
            if (candidates.isEmpty()) {
                lastTarget = null
            } else {
                active = true
                lastTarget = Random.element(candidates)
                updateImage()
                flash()
            }
        } else {
            if (!bg.visible) {
                active = true
                flash()
            }
        }
        visible(lastTarget != null)
        enable(bg.visible)
    }

    private fun updateImage() {
        if (sprite != null) {
            sprite!!.killAndErase()
            sprite = null
        }

        sprite = lastTarget!!.spriteClass.newInstance()
        active = true

        add(sprite!!.let {
            it.idle()
            it.paused = true

            it.x = x + (width - it.width()) / 2 + 1
            it.y = y + (height - it.height()) / 2
            PixelScene.align(it)

            it
        })
    }

    private var enabled = true
    private fun enable(value: Boolean) {
        enabled = value
        if (sprite != null) {
            sprite!!.alpha(if (value) ENABLED else DISABLED)
        }
    }

    private fun visible(value: Boolean) {
        bg.visible = value
        if (sprite != null) {
            sprite!!.visible = value
        }
    }

    override fun onClick() {
        if (enabled) {
            if (Dungeon.hero.handle(lastTarget!!.pos)) {
                Dungeon.hero.next()
            }
        }
    }

    companion object {
        private const val ENABLED = 1.0f
        private const val DISABLED = 0.3f
        private var delay = 0f
        private lateinit var instance: AttackIndicator
        private var lastTarget: Mob? = null
        fun target(target: Char?) {
            lastTarget = target as Mob?
            instance.updateImage()
            HealthIndicator.instance.target(target)
        }

        fun updateState() {
            instance.checkEnemies()
        }
    }
}