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
package com.egoal.darkestpixeldungeon.items.wands

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.DungeonTilemap
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.*
import com.egoal.darkestpixeldungeon.effects.Beam
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.effects.particles.RainbowParticle
import com.egoal.darkestpixeldungeon.effects.particles.ShadowParticle
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfMagicMapping
import com.egoal.darkestpixeldungeon.items.weapon.melee.MagesStaff
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Callback
import com.watabou.utils.PathFinder
import com.watabou.utils.PointF
import com.watabou.utils.Random

class WandOfPrismaticLight : DamageWand(isMissile = false) {

    init {
        image = ItemSpriteSheet.WAND_PRISMATIC_LIGHT

        collisionProperties = Ballistica.MAGIC_BOLT
    }

    override fun min(lvl: Int): Int = 2 + 2 * lvl

    override fun max(lvl: Int): Int = 9 + 3 * lvl

    override fun giveDamage(enemy: Char): Damage {
        val damage = super.giveDamage(enemy).convertToElement(Damage.Element.HOLY)
        if (enemy.properties().contains(Char.Property.DEMONIC) || enemy.properties().contains(Char.Property.UNDEAD))
            damage.value += damage.value / 4
        return damage
    }

    override fun onZap(beam: Ballistica) {
        super.onZap(beam)

        affectMap(beam)

        Buff.affect(curUser, Light::class.java).prolong(4f + level() * 4f)
    }

    override fun onHit(damage: Damage) {
        super.onHit(damage)

        val ch = damage.to as Char
        // view mark
        Buff.prolong(ch, ViewMark::class.java, 4f + level()).observer = curUser.id()

        //three in (5+lvl) chance of failing
        if (Random.Int(5 + level()) >= 3) {
            Buff.prolong(ch, Blindness::class.java, 2f + level() * 0.333f)
            ch.sprite.emitter().burst(Speck.factory(Speck.LIGHT), 6)
        }

        if (ch.properties().contains(Char.Property.DEMONIC) || ch.properties().contains(Char.Property.UNDEAD)) {
            ch.sprite.emitter().start(ShadowParticle.UP, 0.05f, 10 + level())
            Sample.INSTANCE.play(Assets.SND_BURNING)
        } else {
            ch.sprite.centerEmitter().burst(RainbowParticle.BURST, 10 + level())
        }
    }

    private fun affectMap(beam: Ballistica) {
        var noticed = false
        for (c in beam.subPath(0, beam.dist)) {
            for (n in PathFinder.NEIGHBOURS9) {
                val cell = c + n

                if (Level.discoverable[cell])
                    Dungeon.level.mapped[cell] = true

                val terr = Dungeon.level.map[cell]
                if (Terrain.flags[terr] and Terrain.SECRET != 0) {

                    Dungeon.level.discover(cell)

                    GameScene.discoverTile(cell, terr)
                    ScrollOfMagicMapping.discover(cell)

                    noticed = true
                }
            }

            CellEmitter.center(c).burst(RainbowParticle.BURST, Random.IntRange(1, 2))
        }
        if (noticed)
            Sample.INSTANCE.play(Assets.SND_SECRET)

        TempPathLight.Light(beam.path, 5f)
    }

    override fun fx(beam: Ballistica, callback: Callback) {
        curUser.sprite.parent.add(Beam.LightRay(
                DungeonTilemap.tileCenterToWorld(beam.sourcePos),
                DungeonTilemap.tileCenterToWorld(beam.collisionPos)))
        callback.call()
    }

    override fun onHit(staff: MagesStaff, damage: Damage) {
        //cripples enemy
        Buff.prolong(damage.to as Char, Cripple::class.java, 1f + staff.level())
    }

    override fun staffFx(particle: MagesStaff.StaffParticle) {
        particle.color(Random.Int(0x1000000))
        particle.am = 0.3f
        particle.setLifespan(1f)
        particle.speed.polar(Random.Float(PointF.PI2), 2f)
        particle.setSize(1f, 2.5f)
        particle.radiateXY(1f)
    }

}
