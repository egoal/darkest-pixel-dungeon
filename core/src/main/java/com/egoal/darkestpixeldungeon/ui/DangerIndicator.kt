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
import com.egoal.darkestpixeldungeon.scenes.PixelScene
import com.watabou.noosa.BitmapText
import com.watabou.noosa.Camera
import com.watabou.noosa.Image

class DangerIndicator : Tag(0xFF4C4C) {

    private lateinit var number: BitmapText
    private lateinit var icon: Image

    private var enemyIndex = 0
    private var lastNumber = -1

    init {
        setSize(24f, 16f)

        visible = false
    }

    override fun createChildren() {
        super.createChildren()

        number = BitmapText(PixelScene.pixelFont)
        add(number)

        icon = Icons.SKULL.get()
        add(icon)
    }

    override fun layout() {
        super.layout()

        icon.x = right() - 10
        icon.y = y + (height - icon.height) / 2

        placeNumber()
    }

    private fun placeNumber() {
        number.x = right() - 11f - number.width()
        number.y = y + (height - number.baseLine()) / 2f
        PixelScene.align(number)
    }

    override fun update() {
        if (Dungeon.hero.isAlive) {
            val v = Dungeon.hero.visibleEnemies()
            if (v != lastNumber) {
                lastNumber = v
                visible = lastNumber > 0
                if (visible) {
                    number.text(lastNumber.toString())
                    number.measure()
                    placeNumber()

                    flash()
                }
            }
        } else
            visible = false

        super.update()
    }

    override fun onClick() {
        if (Dungeon.hero.visibleEnemies() > 0) {
            val target = Dungeon.hero.visibleEnemy(enemyIndex++)

            HealthIndicator.instance.target(
                    if (target === HealthIndicator.instance.target()) null
                    else target)

            if (Dungeon.hero.curAction == null) {
                Camera.main.target = null
                Camera.main.focusOn(target.sprite)
            }
        }
    }
}
