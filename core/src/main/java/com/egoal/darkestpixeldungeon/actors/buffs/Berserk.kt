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
package com.egoal.darkestpixeldungeon.actors.buffs

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.effects.SpellSprite
import com.egoal.darkestpixeldungeon.items.unclassified.BrokenSeal.WarriorShield
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round

class Berserk : Buff() {
    private var state = State.NORMAL
    private var exhaustion: Int = 0
    private var levelRecovery: Float = 0f
    private var level: Int = 1

    private enum class State {
        NORMAL, BERSERK, EXHAUSTED, RECOVERING
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(STATE, state)
        if (state == State.EXHAUSTED) bundle.put(EXHAUSTION, exhaustion)
        if (state == State.EXHAUSTED || state == State.RECOVERING)
            bundle.put(LEVEL_RECOVERY, levelRecovery)
        bundle.put(LEVEL, level)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        state = bundle.getEnum(STATE, State::class.java)
        if (state == State.EXHAUSTED) exhaustion = bundle.getInt(EXHAUSTION)
        if (state == State.EXHAUSTED || state == State.RECOVERING)
            levelRecovery = bundle.getFloat(LEVEL_RECOVERY)

        level = bundle.getInt(LEVEL)
        if (level == 0) level = 1
    }

    override fun act(): Boolean {
        if (berserking()) {
            if (target.HP <= 0) {
                target.SHLD -= min(target.SHLD, 2)
                if (target.SHLD == 0) {
                    target.die(this)
                    if (!target.isAlive) Dungeon.fail(this.javaClass)
                }
            } else {
                state = State.EXHAUSTED
                exhaustion = EXHAUSTION_START
                levelRecovery = LEVEL_RECOVER_START
                BuffIndicator.refreshHero()
                target.SHLD = 0
            }
        } else if (state == State.EXHAUSTED) {
            exhaustion--
            if (exhaustion == 0) {
                state = State.RECOVERING
                BuffIndicator.refreshHero()
                level += 1
            }
        }
        spend(TICK)
        return true
    }

    fun damageFactor(dmg: Int): Int {
        val bonus: Float

        bonus = if (state == State.EXHAUSTED) (50 - exhaustion) / 50f else {
            val percentMissing = 1f - target.HP / target.HT.toFloat()
            1f + percentMissing.pow(3.6f - 0.65f * level + 0.05f * level * level)
        }

        return round(dmg * bonus).toInt()
    }

    fun berserking(): Boolean {
        if (target.HP == 0 && state == State.NORMAL) {

            val shield = target.buff(WarriorShield::class.java)
            if (shield != null) {
                state = State.BERSERK
                BuffIndicator.refreshHero()
                target.SHLD = shield.maxShield() * 5

                SpellSprite.show(target, SpellSprite.BERSERK)
                Sample.INSTANCE.play(Assets.SND_CHALLENGE)
                GameScene.flash(0xFF0000)
            }

        }

        return state == State.BERSERK
    }

    fun recover(percent: Float) {
        if (levelRecovery > 0) {
            levelRecovery -= percent
            if (levelRecovery <= 0) {
                state = State.NORMAL
                BuffIndicator.refreshHero()
                levelRecovery = 0f
            }
        }
    }

    override fun icon(): Int = when (state) {
        State.NORMAL -> BuffIndicator.ANGERED
        State.BERSERK -> BuffIndicator.FURY
        State.EXHAUSTED -> BuffIndicator.EXHAUSTED
        State.RECOVERING -> BuffIndicator.RECOVERING
    }

    override fun toString(): String = when (state) {
        State.NORMAL -> Messages.get(this, "angered")
        State.BERSERK -> Messages.get(this, "berserk")
        State.EXHAUSTED -> Messages.get(this, "exhausted")
        State.RECOVERING -> Messages.get(this, "recovering")
    }

    override fun desc(): String {
        val dispDamage = damageFactor(10000) / 100f
        return when (state) {
            State.NORMAL -> Messages.get(this, "angered_desc", level, dispDamage)
            State.BERSERK -> Messages.get(this, "berserk_desc")
            State.EXHAUSTED -> Messages.get(this, "exhausted_desc", exhaustion, dispDamage)
            State.RECOVERING -> Messages.get(this, "recovering_desc", levelRecovery, dispDamage)
        }
    }

    companion object {
        private const val EXHAUSTION_START = 40

        private const val LEVEL_RECOVER_START = 3f

        private const val STATE = "state"
        private const val EXHAUSTION = "exhaustion"
        private const val LEVEL_RECOVERY = "levelrecovery"

        private const val LEVEL = "level"
    }
}
