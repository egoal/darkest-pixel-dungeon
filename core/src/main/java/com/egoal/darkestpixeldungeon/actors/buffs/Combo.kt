package com.egoal.darkestpixeldungeon.actors.buffs

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Badges
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.effects.Pushing
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.CellSelector
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.ui.ActionIndicator
import com.egoal.darkestpixeldungeon.ui.AttackIndicator
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.Image
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle
import com.watabou.utils.Random
import kotlin.math.max

class Combo : Buff(), ActionIndicator.Action {
    private var count = 0
    private var comboTime = 0f
    private var misses = 0
    private var lastTargetId = -1
    private var focusCount = 0

    override fun icon(): Int = BuffIndicator.COMBO

    override fun toString(): String = M.L(this, "name")

    override fun detach() {
        super.detach()
        ActionIndicator.clearAction(this)
    }

    override fun act(): Boolean {
        comboTime -= Actor.TICK
        spend(Actor.TICK)
        if (comboTime <= 0f) detach()

        return true
    }

    override fun desc(): String {
        var desc = M.L(this, "desc")
        desc += when {
            count >= 10 -> "\n\n" + M.L(this, "fury_desc")
//            count >= 8 -> "\n\n" + M.L(this, "crush_desc")
//            count >= 6 -> "\n\n" + M.L(this, "slam_desc")
            count >= 5 -> M.L(this, "cleave_desc")
//            count >= 2 -> "\n\n" + M.L(this, "clobber_desc")
            else -> ""
        }

        return desc
    }

    // 
    fun hit(target: Char) {
        ++count
        comboTime = 4f
        misses = 0

        if (count >= 5) {
            ActionIndicator.setAction(this)
            Badges.validateMasteryCombo(count)
            GLog.p(M.L(this, "combo", count))
        }

        if (target.id() != lastTargetId) {
            // switch target, reset
            lastTargetId = target.id()
            focusCount = 0
        } else ++focusCount
    }

    fun miss() {
        ++misses
        comboTime = 4f
        if (misses >= 3) detach()
    }

    // check Hero::attackDelay
    fun speedFactor(): Float = 0.2f + 0.8f * Math.pow(0.8, focusCount.toDouble()).toFloat()

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(COUNT, count)
        bundle.put(TIME, comboTime)
        bundle.put(MISSES, misses)
        bundle.put(TARGET, lastTargetId)
        bundle.put(FOCUS_COUNT, focusCount)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        count = bundle.getInt(COUNT)
        if (count >= 5) ActionIndicator.setAction(this)
        comboTime = bundle.getFloat(TIME)
        misses = bundle.getInt(MISSES)
        lastTargetId = bundle.getInt(TARGET)
        focusCount = bundle.getInt(FOCUS_COUNT)
    }

    // ActionIndicator.Action
    override fun getIcon(): Image {
        val icon = if ((target as Hero).belongings.weapon != null)
            ItemSprite((target as Hero).belongings.weapon.image, null)
        else ItemSprite(Item().apply {
            image = ItemSpriteSheet.WEAPON_HOLDER
        })

        val tint = when {
            count >= 10 -> 0xffff0000
//            count >= 8 -> 0xffffcc00
//            count >= 6 -> 0xffffff00
            count >= 5 -> 0xffccff00
            else -> 0xff00ff00
        }
        icon.tint(tint.toInt())

        return icon
    }

    override fun doAction() {
        GameScene.selectCell(finisher)
    }

    private enum class FinisherType {
        CLOBBER, CLEAVE, SLAM, CRUSH, FURY
    }

    private val finisher = object : CellSelector.Listener {
        private lateinit var type: FinisherType

        override fun onSelect(cell: Int?) {
            if (cell == null) return
            val enemy = Actor.findChar(cell)
            if (enemy == null || !(target as Hero).canAttack(enemy) || target.isCharmedBy(enemy))
                GLog.w(M.L(Combo::class.java, "bad_target"))
            else {
                target.sprite.attack(cell) {
                    type = when {
                        count >= 10 -> FinisherType.FURY
//                        count >= 8 -> FinisherType.CRUSH
//                        count >= 6 -> FinisherType.SLAM
                        count >= 5 -> FinisherType.CLEAVE
                        else -> FinisherType.CLOBBER
                    }
                    doAttack(enemy)
                }
            }
        }

        override fun prompt(): String = when {
            count >= 10 -> M.L(Combo::class.java, "fury_prompt")
//            count >= 8 -> M.L(Combo::class.java, "crush_prompt")
//            count >= 6 -> M.L(Combo::class.java, "slam_prompt")
            count >= 5 -> M.L(Combo::class.java, "cleave_prompt")
            else -> M.L(Combo::class.java, "clobber_prompt")
        }

        private fun doAttack(enemy: Char) {
            AttackIndicator.target(enemy)
            val dmg = target.giveDamage(enemy)
            when (type) {
                FinisherType.CLOBBER -> dmg.value = (dmg.value * 0.6f).toInt()
                FinisherType.CLEAVE -> dmg.value = (dmg.value * 1.5f).toInt()
                FinisherType.SLAM -> dmg.value = (max(dmg.value, target.giveDamage(enemy).value) * 1.6f).toInt()
                FinisherType.CRUSH -> dmg.value = (max(dmg.value, (1..4).map { target.giveDamage(enemy).value }.max()!!) * 2.5f).toInt()
                FinisherType.FURY -> dmg.value = (dmg.value * 0.6f).toInt()
            }
            dmg.addFeature(Damage.Feature.CRITICAL)

            // Char::attack
            if (!dmg.isFeatured(Damage.Feature.PURE))
                enemy.defendDamage(dmg)
            target.attackProc(dmg)
            enemy.defenseProc(dmg)
            enemy.takeDamage(dmg)

            // special effects
            when (type) {
                FinisherType.CLOBBER -> {
                    // push(but not throw) & vertigo
                    if (enemy.isAlive) {
                        if (Dungeon.level.adjacent(target.pos, enemy.pos) &&
                                !enemy.properties().contains(Char.Property.IMMOVABLE)) {
                            val newpos = enemy.pos + (enemy.pos - target.pos)
                            if ((Level.passable[newpos] || Level.avoid[newpos]) && Actor.findChar(newpos) == null) {
                                Actor.addDelayed(Pushing(enemy, enemy.pos, newpos), -1f);

                                enemy.pos = newpos
                                if (enemy is Mob) Dungeon.level.mobPress(enemy)
                                else Dungeon.level.press(newpos, enemy)
                            }
                        }
                    }
                    prolong(enemy, Vertigo::class.java, Random.NormalIntRange(1, 4).toFloat())
                }
                FinisherType.SLAM -> target.SHLD = max(target.SHLD, dmg.value / 2) // add shield
            }

            target.buff(FireImbue::class.java)?.proc(enemy)
            target.buff(EarthImbue::class.java)?.proc(enemy)

            Sample.INSTANCE.play(Assets.SND_CRITICAL, 1f, 1f, Random.Float(0.8f, 1.25f))
            enemy.sprite.bloodBurstB(target.sprite.center(), dmg.value)
            enemy.sprite.spriteBurst(target.sprite.center(), dmg.value)
            enemy.sprite.flash()

            if (!enemy.isAlive) GLog.i(M.CL(Char::class.java, "defeat", enemy.name))

            val hero = target as Hero
            // post behaviour
            when (type) {
                FinisherType.CLEAVE -> {
                    // if killed, dont reset combo
                    if (!enemy.isAlive) {
                        hit(enemy)
                        comboTime = 10f
                    } else {
                        detach()
                        ActionIndicator.clearAction(this@Combo)
                    }
                    hero.spendAndNext(hero.attackDelay())
                }
                FinisherType.FURY -> {
                    // death!!!!
                    --count
                    if (count > 0 && enemy.isAlive)
                        target.sprite.attack(enemy.pos) {
                            doAttack(enemy)
                        }
                    else {
                        detach()
                        ActionIndicator.clearAction(this@Combo)
                        hero.spendAndNext(hero.attackDelay())
                    }
                }
                else -> {
                    detach()
                    ActionIndicator.clearAction(this@Combo)
                    hero.spendAndNext(hero.attackDelay())
                }
            }
        }

    }

    companion object {
        private const val COUNT = "count"
        private const val TIME = "combotime"
        private const val MISSES = "misses"
        private const val TARGET = "target"
        private const val FOCUS_COUNT = "focus-count"
    }
}