package com.egoal.darkestpixeldungeon.items.weapon.melee

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.Statistics
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Invisibility
import com.egoal.darkestpixeldungeon.actors.buffs.Unbalance
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.HeroClass
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.CellSelector
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.sprites.MissileSprite
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Callback
import com.watabou.utils.Random
import java.util.ArrayList
import kotlin.math.max

class MagicBow : MeleeWeapon() {

    init {
        image = ItemSpriteSheet.RANGER_BOW

        tier = 2
        defaultAction = AC_SHOOT
        usesTargeting = true
        DLY = 1.25f
    }

    override fun min(lvl: Int): Int = lvl + 1
    override fun max(lvl: Int): Int = 4 * (tier + 1) + lvl * (tier + 1)

    override fun actions(hero: Hero?): ArrayList<String> = super.actions(hero).apply { add(AC_SHOOT) }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)
        if (action == AC_SHOOT) {
            if (!isEquipped(hero)) GLog.w(M.L(this, "need_equipped"))
            else {
                Item.curUser = hero
                Item.curItem = this
                GameScene.selectCell(shooter)
            }
        }
    }

    //todo: handle the proc things.
    private fun giveShootDamage(hero: Hero, enemy: Char) =
            giveDamage(hero, enemy).type(Damage.Type.MAGICAL).addFeature(Damage.Feature.RANGED)

    private fun onShot(enemy: Char) {
        val hero = Item.curUser
        if (Dungeon.level.adjacent(hero.pos, enemy.pos)) {
            val dur = if (hero.heroClass == HeroClass.HUNTRESS) 1.5f else 2.5f
            Buff.prolong(hero, Unbalance::class.java, dur)
        }

        val dmg = giveShootDamage(hero, enemy)
        // still need a hit check
        dmg.type(Damage.Type.NORMAL) // remove magical, add later: affect checkHit
        if (enemy.checkHit(dmg)) {
            dmg.type(Damage.Type.MAGICAL)
            //todo: may proc enchantment here.
            enemy.defendDamage(dmg)
            enemy.takeDamage(dmg)
            enemy.sprite.burst(0x57e14e, level() / 2 + 2)

            Statistics.HighestDamage = max(Statistics.HighestDamage, dmg.value)
            if (!enemy.isAlive) hero.onKillChar(enemy)

            Sample.INSTANCE.play(Assets.SND_HIT, 1f, 1f, Random.Float(0.8f, 1.25f))
        } else {
            enemy.sprite.showStatus(CharSprite.NEUTRAL, enemy.defenseVerb())
        }
        Invisibility.dispel()
    }

    private val shooter = object : CellSelector.Listener {
        override fun onSelect(cell: Int?) {
            if (cell == null) return

            val hero = Item.curUser
            val shot = Ballistica(hero.pos, cell, Ballistica.PROJECTILE)
            val shotpos = shot.collisionPos

            if (cell == hero.pos || shotpos == hero.pos) {
                GLog.i(M.L(MagicBow::class.java, "not_yourself"))
                return
            }

            hero.sprite.zap(shotpos)
            hero.busy()

            Sample.INSTANCE.play(Assets.SND_MISS, 0.6f, 0.6f, 1.5f)
            val enemy = Actor.findChar(shotpos)
            val delay = Item.TIME_TO_THROW

            (hero.sprite.parent.recycle(MissileSprite::class.java) as MissileSprite).reset(
                    hero.pos, shotpos, ItemSpriteSheet.MAGIC_DART, null, Callback {
                if (enemy != null) onShot(enemy)

                hero.spendAndNext(delay)
            })
        }

        override fun prompt(): String = M.L(MagicBow::class.java, "prompt")
    }

    companion object {
        private const val AC_SHOOT = "shoot"
    }

    class Broken : Item() {
        init {
            image = ItemSpriteSheet.RANGER_BOW
        }

        override fun price(): Int = 10

        override fun isIdentified(): Boolean = true
        override fun isUpgradable(): Boolean = false
    }
}