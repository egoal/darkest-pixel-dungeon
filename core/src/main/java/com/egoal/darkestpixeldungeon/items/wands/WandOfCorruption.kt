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

import com.egoal.darkestpixeldungeon.*
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.*
import com.egoal.darkestpixeldungeon.actors.mobs.Bee
import com.egoal.darkestpixeldungeon.actors.mobs.King
import com.egoal.darkestpixeldungeon.actors.mobs.Mimic
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.actors.mobs.Piranha
import com.egoal.darkestpixeldungeon.actors.mobs.Statue
import com.egoal.darkestpixeldungeon.actors.mobs.Swarm
import com.egoal.darkestpixeldungeon.actors.mobs.Wraith
import com.egoal.darkestpixeldungeon.actors.mobs.Yog
import com.egoal.darkestpixeldungeon.effects.MagicMissile
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.weapon.melee.MagesStaff
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Callback
import com.watabou.utils.Random

import java.util.HashMap

class WandOfCorruption : DamageWand.NoDamage(true) {
    init {
        image = ItemSpriteSheet.WAND_CORRUPTION
    }

    override fun onHit(damage: Damage) {
        super.onHit(damage)

        // do corruption
        val mob = damage.to as Char
        if (mob is Mob) {
            val corruptingPower = 2f + level()
            // todo: clean
            var enemyResist = when {
                mob is Mimic || mob is Statue -> 1f + Dungeon.depth
                mob is Piranha || mob is Bee -> 1f + Dungeon.depth / 2f
                mob is Wraith -> 0.5f + Dungeon.depth / 8f
                mob is Yog.Larva || mob is King.Undead -> 1f + 30f
                mob is Swarm -> 1f + 3f
                else -> 1f + mob.Config.EXP
            }

            //100% health: 3x resist   75%: 2.1x resist   50%: 1.5x resist   25%: 1.1
            enemyResist *= 1f + 2f * Math.pow(mob.HP / mob.HT.toDouble(), 2.0).toFloat()

            // placed debuffs reduce their resistance
            for (buff in mob.buffs()) {
                var r = 1f
                if (buff.type == Buff.buffType.NEGATIVE) r = MINOR_DEBUFF_WEAKEN
                if (buff is FlavourBuff) {
                    if (buff.javaClass in MAJOR_DEBUFFS) r = MAJOR_DEBUFF_WEAKEN
                    else if (buff.javaClass in MINOR_DEBUFFS) r = MINOR_DEBUFF_WEAKEN
                }

                enemyResist *= r
            }

            //cannot re-corrupt or doom an enemy, so give them a major debuff instead
            if (mob.buff(Corruption::class.java) != null) enemyResist = corruptingPower * .99f

            if (corruptingPower > enemyResist) corruptEnemy(mob)
            else {
                val debuffChance = corruptingPower / enemyResist
                if (Random.Float() < debuffChance) debuffEnemy(mob, MAJOR_DEBUFFS)
                else debuffEnemy(mob, MINOR_DEBUFFS)
            }
        }

    }

    private fun debuffEnemy(enemy: Mob, category: HashMap<Class<out FlavourBuff>, Float>) {
        val candidates = category.filter { enemy.buff(it.key) == null && !enemy.immunizedBuffs().contains(it.key) }
        val cls = KRandom.Chances(candidates)
        if (cls != null)
            Buff.append(enemy, cls, 6f + level() * 3f)
        else
            if (category === MINOR_DEBUFFS) debuffEnemy(enemy, MAJOR_DEBUFFS)
            else if (category === MAJOR_DEBUFFS) corruptEnemy(enemy)
    }

    private fun corruptEnemy(enemy: Mob) {
        //cannot re-corrupt or doom an enemy, so give them a major debuff instead
        if (enemy.buff(Corruption::class.java) != null || enemy.buff(Doom::class.java) != null) {
            GLog.w(Messages.get(this, "already_corrupted"))
            return
        }

        val canBeCorruptted = (!enemy.immunizedBuffs().contains(Corruption::class.java) &&
                !enemy.properties().contains(Char.Property.BOSS) &&
                !enemy.properties().contains(Char.Property.MINIBOSS) &&
                !enemy.properties().contains(Char.Property.MACHINE))


        if (canBeCorruptted) {
            enemy.HP = enemy.HT
            for (buff in enemy.buffs()) {
                if (buff.type == Buff.buffType.NEGATIVE && buff !is SoulMark)
                    buff.detach()
                else if (buff is PinCushion)
                    buff.detach() // drop ranged weapon
            }
            Buff.affect(enemy, Corruption::class.java)

            // in dpd, enemy would not dead directly...
//            Statistics.enemiesSlain++;
//            Badges.validateMonstersSlain();
//            Statistics.qualifiedForNoKilling = false;
//            if (enemy.EXP > 0 && curUser.lvl <= enemy.maxLvl) {
//                curUser.sprite.showStatus(CharSprite.POSITIVE, Messages.get(enemy,
//                        "exp", enemy.EXP));
//                curUser.earnExp(enemy.EXP);
//            }

        } else
            Buff.affect(enemy, Doom::class.java)
    }

    override fun onHit(staff: MagesStaff, damage: Damage) {
        // lvl 0 - 25%
        // lvl 1 - 40%
        // lvl 2 - 50%
        if (Random.Int(level() + 4) >= 3) {
            Buff.prolong(damage.to as Char, Amok::class.java, (3 + level()).toFloat())
        }
    }

    override fun fx(bolt: Ballistica, callback: Callback) {
        MagicMissile.shadow(Item.curUser.sprite.parent, bolt.sourcePos, bolt
                .collisionPos, callback)
        Sample.INSTANCE.play(Assets.SND_ZAP)
    }

    override fun staffFx(particle: MagesStaff.StaffParticle) {
        particle.color(0)
        particle.am = 0.6f
        particle.setLifespan(0.8f)
        particle.acc.set(0f, 20f)
        particle.setSize(0f, 3f)
        particle.shuffleXY(2f)
    }

    companion object {

        private const val MINOR_DEBUFF_WEAKEN = .8f
        private val MINOR_DEBUFFS = hashMapOf<Class<out FlavourBuff>, Float>(
                Weakness::class.java to 0f,
                Cripple::class.java to 1f,
                Blindness::class.java to 1f,
                Terror::class.java to 1f,

                Chill::class.java to 0f,
                Roots::class.java to 0f,
                Vertigo::class.java to 0f

//                Bleeding::class.java to 0f, 
//                Burning::class.java to 0f, 
//                Poison::class.java to 0f
        )

        private const val MAJOR_DEBUFF_WEAKEN = .667f
        private val MAJOR_DEBUFFS = hashMapOf<Class<out FlavourBuff>, Float>(
                Amok::class.java to 3f,
                Slow::class.java to 2f,
                Paralysis::class.java to 1f,
                SoulMark::class.java to 1f,
                Decayed::class.java to 1f,

                Charm::class.java to 0f,
                SoulMark::class.java to 0f,
                Frost::class.java to 0f
        )
    }

}
