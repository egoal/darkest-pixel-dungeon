package com.egoal.darkestpixeldungeon.items.specials

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Amok
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.items.weapon.enchantments.Rousing
import com.egoal.darkestpixeldungeon.items.weapon.melee.MeleeWeapon
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.CellSelectListener
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.windows.WndActionList
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle
import com.watabou.utils.PathFinder
import com.watabou.utils.Random
import kotlin.math.max
import kotlin.math.min

class KnightCore : Special() {
    var honor: Int = 0
        private set

    val SHLD: Int
        get() = honor + min(honor, 5)

    val CRIT: Float
        get() = 0.01f * honor

    init {
        image = ItemSpriteSheet.KNIGHT
    }

    private val actions = listOf(Iron(), Shield(), Rouse(), Duel())

    override fun tick() {
        for (a in actions) a.cd = max(0, a.cd - 1)

        updateQuickslot()
    }

    override fun use(hero: Hero) {
        GameScene.show(WndActionList(ItemSprite(image, null), name, actions))
    }

    fun onEnemySlayed(ch: Char) {
        val props = ch.properties()
        val dh = when {
            props.contains(Char.Property.ELITE) -> 2
            props.contains(Char.Property.MINIBOSS) -> 3
            props.contains(Char.Property.BOSS) -> 5
            else -> 1
        }

        honor = min(honor + dh, MAX_HONOR)

        updateQuickslot()
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(HONOR, honor)

        bundle.put("cds", actions.map { it.cd }.toIntArray())
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        honor = bundle.getInt(HONOR)

        val cds = bundle.getIntArray("cds")
        (0 until min(actions.size, cds.size)).forEach { actions[it].cd = cds[it] }
    }

    override fun status(): String? = if (honor > 0) "$honor" else null

    companion object {
        private const val HONOR = "honor"
        private const val MAX_HONOR = 10
    }

    abstract inner class KnightAction(val cost: Int, val cooldown: Int) : WndActionList.Action() {
        var cd = 0

        override fun Name(): String = M.L(this, "name") + "($cost)"
        override fun Info(): String = M.L(this, "info", cost, cooldown)
        override fun Disabled(): Boolean = cost > honor || cd > 0

        override fun Execute() {
            Execute(Dungeon.hero)
            updateQuickslot()
        }

        protected abstract fun Execute(hero: Hero)
    }

    inner class Iron : KnightAction(2, 10) {
        override fun Execute(hero: Hero) {
            cd = cooldown
            honor -= cost
            hero.apply {
                SHLD += 20
                Buff.affect(this, Ironed::class.java).fixedpos = hero.pos

                spend(1f)
                busy()
                sprite.operate(pos)
            }
        }
    }

    class Ironed : Buff() {
        var fixedpos: Int = 0

        override fun act(): Boolean {
            if (target.pos != fixedpos) {
                target.SHLD = 0
                target.sprite.showStatus(CharSprite.NEUTRAL, M.L(KnightCore::class.java, "ironed"))
                detach()
            }
            spend(TICK)
            return true
        }

        override fun storeInBundle(bundle: Bundle) {
            super.storeInBundle(bundle)
            bundle.put("pos", fixedpos)
        }

        override fun restoreFromBundle(bundle: Bundle) {
            super.restoreFromBundle(bundle)
            fixedpos = bundle.getInt("pos")
        }
    }

    inner class Shield : KnightAction(3, 10) {
        override fun Execute(hero: Hero) {
            GameScene.selectCell(object : CellSelectListener(FLAG_ENEMY, 1) {
                override fun onSelected(cell: Int) {
                    cd = cooldown
                    honor -= cost

                    hero.sprite.attack(cell) {
                        val enemy = Actor.findChar(cell)!!
                        val damage = Damage(hero.SHLD, hero, enemy).addFeature(Damage.Feature.ACCURATE)
                        Char.ProcessAttackDamage(damage)
                        PathFinder.NEIGHBOURS8.map { Actor.findChar(it + cell) }
                                .filter { it?.camp == Char.Camp.ENEMY }
                                .forEach {
                                    it!!.takeDamage(Damage(Random.IntRange(1, hero.SHLD), hero, it))
                                }

                        hero.spendAndNext(hero.attackDelay())
                    }
                }
            })
        }
    }

    inner class Rouse : KnightAction(5, 0) {
        override fun Execute(hero: Hero) {
            cd = cooldown
            honor -= cost

            (hero.belongings.weapon as MeleeWeapon?)?.enchant(Rousing::class.java, 10f)

            hero.apply {
                spend(1f)
                busy()
                sprite.operate(pos)
            }

            hero.sprite.centerEmitter().start(Speck.factory(Speck.SCREAM), 0.3f, 3)
            Sample.INSTANCE.play(Assets.SND_CHALLENGE)

            Dungeon.level.mobs.filter { Level.fieldOfView[it.pos] && Random.Float() < .5f }
                    .forEach { Buff.prolong(it, Amok::class.java, 5f) }
        }
    }

    inner class Duel : KnightAction(10, 0) {
        override fun Execute(hero: Hero) {
            GameScene.selectCell(object : CellSelectListener(FLAG_ENEMY, 1) {
                override fun onSelected(cell: Int) {
                    duel(hero, Actor.findChar(cell) as Mob)
                }
            })
        }

        private fun duel(hero: Hero, enemy: Mob) {
            cd = cooldown
            honor -= cost

            hero.say(M.L(this, "cry_${Random.Int(3)}"))
            doAttack(hero, enemy)
        }

        private fun doAttack(hero: Hero, enemy: Mob) {
            //todo: clean this.
            hero.sprite.attack(enemy.pos) {
                Char.ProcessAttackDamage(hero.giveDamage(enemy))
                if (!enemy.isAlive || !hero.isAlive) finish(hero, enemy)
                else {
                    enemy.sprite.attack(hero.pos) {
                        Char.ProcessAttackDamage(enemy.giveDamage(hero))
                        if (!enemy.isAlive || !hero.isAlive) finish(hero, enemy)
                        else doAttack(hero, enemy)
                    }
                }
            }
        }

        private fun finish(hero: Hero, enemy: Mob) {
            if (hero.isAlive) {
                if (enemy.Config.MaxLevel >= hero.lvl - 1) {
                    hero.MSHLD++
                    hero.say(M.L(this, "win_${Random.Int(2)}"))
                } else hero.say(M.L(this, "weak_${Random.Int(2)}"))
                hero.spendAndNext(1f)
            }
            // just, die
        }
    }
}