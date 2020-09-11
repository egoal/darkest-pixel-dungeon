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
package com.egoal.darkestpixeldungeon.actors.blobs

import com.egoal.darkestpixeldungeon.actors.buffs.Awareness
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.BlobEmitter
import com.egoal.darkestpixeldungeon.effects.Identification
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Badges
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.DungeonTilemap
import com.egoal.darkestpixeldungeon.Journal
import com.egoal.darkestpixeldungeon.items.Item
import com.watabou.noosa.audio.Sample

class WaterOfAwareness : WellWater() {

    override fun affectHero(hero: Hero): Boolean {

        Sample.INSTANCE.play(Assets.SND_DRINK)
        emitter!!.parent.add(Identification(DungeonTilemap.tileCenterToWorld(pos)))

        hero.belongings.observe()

        for (i in 0 until Dungeon.level.length()) {

            val terr = Dungeon.level.map[i]
            if (Terrain.flags[terr] and Terrain.SECRET != 0) {

                Dungeon.level.discover(i)

                if (Dungeon.visible[i]) {
                    GameScene.discoverTile(i, terr)
                }
            }
        }

        Buff.affect(hero, Awareness::class.java, Awareness.DURATION)
        Dungeon.observe()

        hero.interrupt()

        GLog.p(Messages.get(this, "procced"))

        Journal.remove(Journal.Feature.WELL_OF_AWARENESS)

        return true
    }

    override fun affectItem(item: Item): Item? {
        if (item.isIdentified) {
            return null
        }

        item.identify()
        Badges.validateItemLevelAquired(item)

        emitter!!.parent.add(Identification(DungeonTilemap.tileCenterToWorld(pos)))

        Journal.remove(Journal.Feature.WELL_OF_AWARENESS)

        return item
    }

    override fun use(emitter: BlobEmitter) {
        super.use(emitter)
        emitter.pour(Speck.factory(Speck.QUESTION), 0.3f)
    }

    override fun tileDesc(): String = Messages.get(this, "desc")
}
