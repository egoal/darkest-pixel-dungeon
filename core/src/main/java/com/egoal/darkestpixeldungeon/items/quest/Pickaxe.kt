package com.egoal.darkestpixeldungeon.items.quest

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Hunger
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.mobs.Bat
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle
import com.watabou.utils.PathFinder
import java.util.ArrayList

class Pickaxe : Weapon() {
    init {
        image = ItemSpriteSheet.PICKAXE
        unique = true
        defaultAction = AC_MINE
    }

    var bloodStained = false

    override fun min(lvl: Int): Int = 2

    override fun max(lvl: Int): Int = 15

    override fun STRReq(lvl: Int): Int = 14

    override fun actions(hero: Hero): ArrayList<String> = super.actions(hero).apply { add(AC_MINE) }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)

        if (action == AC_MINE) {
            if (Dungeon.depth !in 11..15) {
                GLog.w(M.L(this, "no_vein"))
                return
            }

            val i = PathFinder.NEIGHBOURS8.map { it + hero.pos }.find { Dungeon.level.map[it] == Terrain.WALL_DECO }
            if (i == null) {
                GLog.w(M.L(this, "no_vein"))
                return
            }

            // mine
            hero.spend(TIME_TO_MINE)
            hero.busy()
            hero.sprite.attack(i) {
                CellEmitter.center(i).burst(Speck.factory(Speck.STAR), 7)
                Sample.INSTANCE.play(Assets.SND_EVOKE)

                Level.set(i, Terrain.WALL)
                GameScene.updateMap(i)

                val gold = DarkGold()
                if (gold.doPickUp(hero)) GLog.i(M.L(hero, "you_now_have", gold.name()))
                else Dungeon.level.drop(gold, hero.pos).sprite.drop()

                hero.buff(Hunger::class.java)?.let {
                    if (!it.isStarving) {
                        it.reduceHunger(-Hunger.STARVING / 15)
                        BuffIndicator.refreshHero()
                    }
                }

                hero.onOperateComplete()
            }
        }
    }

    override fun isUpgradable(): Boolean = false
    override fun isIdentified(): Boolean = true

    override fun proc(dmg: Damage): Damage {
        if (!bloodStained && dmg.to is Bat && (dmg.to as Char).HP <= dmg.value) {
            //fixme: ^^^ may not killed the bat
            bloodStained = true
            updateQuickslot()
        }

        return dmg
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)

        bundle.put(BLOODSTAINED, bloodStained)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)

        bloodStained = bundle.getBoolean(BLOODSTAINED)
    }

    override fun glowing(): ItemSprite.Glowing? = if (bloodStained) BLOODY else null

    companion object {
        private const val AC_MINE = "MINE"
        private const val TIME_TO_MINE = 5f
        private const val BLOODSTAINED = "bloodStained"

        private val BLOODY = ItemSprite.Glowing(0x550000)
    }
}