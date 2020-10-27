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
package com.egoal.darkestpixeldungeon.items.potions

import android.util.Log

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Badges
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.blobs.Fire
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Burning
import com.egoal.darkestpixeldungeon.actors.buffs.Pressure
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.Splash
import com.egoal.darkestpixeldungeon.items.ItemStatusHandler
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndOptions
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.messages.M
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle

import java.util.ArrayList
import java.util.HashMap
import java.util.HashSet

open class Potion : Item() {

    protected var initials: Int? = null

    protected lateinit var color: String

    var ownedByFruit = false
    var reinforced = false

    val isKnown: Boolean
        get() = handler!!.isKnown(this)

    init {
        cursedKnown = true
        cursed = false
    }

    open fun canBeReinforced(): Boolean = false

    fun reinforce(): Potion {
        if (canBeReinforced()) reinforced = true
        else Log.e("DPD", "try to reinforce a potion cannot be.")
        return this
    }

    override fun status(): String? {
        var status = super.status()
        if (status == null) status = ""
        if (reinforced)
            status += "+"
        return status
    }

    override fun desc(): String {
        var desc = super.desc()
        if (reinforced)
            desc += "\n\n" + Messages.get(this, "reinforced_desc")
        return desc
    }

    override fun isSimilar(item: Item): Boolean {
        return javaClass == item.javaClass && reinforced == (item as Potion)
                .reinforced
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(REINFORCED, reinforced)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        reinforced = bundle.getBoolean(REINFORCED)
    }

    init {
        stackable = true
        defaultAction = AC_DRINK

        reset()
    }

    override fun reset() {
        super.reset()
        image = handler!!.image(this)
        color = handler!!.label(this)
    }

    override fun actions(hero: Hero): ArrayList<String> {
        val actions = super.actions(hero)
        actions.add(AC_DRINK)
        return actions
    }

    override fun execute(hero: Hero, action: String) {

        super.execute(hero, action)

        if (action == AC_DRINK) {

            if (isKnown) {
                // warning on bad potions
                if (this is PotionOfLiquidFlame ||
                        this is PotionOfToxicGas ||
                        this is PotionOfParalyticGas) {
                    WndOptions.Confirm(M.L(Potion::class.java, "harmful"), M.L(Potion::class.java, "sure_drink")) {
                        drink(hero)
                    }
                } else {
                    drink(hero)
                }
            } else {
                // not known
                val plvl = hero.pressure.level
                if (plvl === Pressure.Level.NERVOUS || plvl === Pressure.Level.COLLAPSE)
                    GLog.n(Messages.get(this, "nervous"))
                else
                    drink(hero)
            }
        }
    }

    override fun doThrow(hero: Hero) {

        // todo:
        if (isKnown && (this is PotionOfExperience ||
                        this is PotionOfHealing ||
                        this is PotionOfMindVision ||
                        this is PotionOfStrength ||
                        this is PotionOfInvisibility ||
                        this is PotionOfMight)) {
            
            WndOptions.Confirm(M.L(Potion::class.java, "beneficial"), M.L(Potion::class.java, "sure_throw")){
                super.doThrow(hero)
            }
        } else {
            super.doThrow(hero)
        }
    }

    protected fun drink(hero: Hero) {

        detach(hero.belongings.backpack)

        hero.spend(TIME_TO_DRINK)
        hero.busy()
        apply(hero)

        Sample.INSTANCE.play(Assets.SND_DRINK)

        hero.sprite.operate(hero.pos)
    }

    override fun onThrow(cell: Int) {
        if (Dungeon.level.map[cell] == Terrain.WELL || Level.pit[cell]) {

            super.onThrow(cell)

        } else {
            // now press the level.
            Dungeon.level.press(cell, null)
            shatter(cell)

        }
    }

    open fun apply(hero: Hero) {
        shatter(hero.pos)
    }

    open fun shatter(cell: Int) {
        if (Dungeon.visible[cell]) {
            GLog.i(Messages.get(Potion::class.java, "shatter"))
            Sample.INSTANCE.play(Assets.SND_SHATTER)
            splash(cell)
        }
    }

    override fun cast(user: Hero, dst: Int) {
        super.cast(user, dst)
    }

    fun setKnown() {
        if (!ownedByFruit) {
            if (!isKnown) {
                handler!!.know(this)
            }

            Badges.validateAllPotionsIdentified()
        }
    }

    override fun identify(): Item {

        setKnown()
        return this
    }

    override fun name(): String {
        return if (isKnown) {
            if (reinforced)
                super.name() + "+"
            else
                super.name()
        } else
            Messages.get(Potion::class.java, color)
    }

    override fun info(): String {
        return if (isKnown)
            desc()
        else
            Messages.get(Potion::class.java, "unknown_desc")
    }

    fun initials(): Int? {
        return if (isKnown) initials else null
    }

    override val isIdentified: Boolean
        get() = isKnown
    override val isUpgradable: Boolean
        get() = false

    protected fun splash(cell: Int) {
        val color = ItemSprite.pick(image, 8, 10)
        Splash.at(cell, color, 5)

        (Dungeon.level.blobs[Fire::class.java] as Fire?)?.clear(cell)

        Actor.findChar(cell)?.let { Buff.detach(it, Burning::class.java) }
    }

    override fun price(): Int {
        return (30 * quantity * if (reinforced) 1.5f else 1.0f).toInt()
    }

    companion object {

        private const val AC_DRINK = "DRINK"

        private const val TIME_TO_DRINK = 1f

        private val potions = arrayOf(
                PotionOfHealing::class.java, PotionOfExperience::class.java, PotionOfToxicGas::class.java,
                PotionOfLiquidFlame::class.java, PotionOfStrength::class.java, PotionOfParalyticGas::class.java,
                PotionOfLevitation::class.java, PotionOfMindVision::class.java, PotionOfPurity::class.java,
                PotionOfInvisibility::class.java, PotionOfMight::class.java, PotionOfFrost::class.java,
                PotionOfPhysique::class.java)

        private val colors = object : HashMap<String, Int>() {
            init {
                put("crimson", ItemSpriteSheet.POTION_CRIMSON)
                put("amber", ItemSpriteSheet.POTION_AMBER)
                put("golden", ItemSpriteSheet.POTION_GOLDEN)
                put("jade", ItemSpriteSheet.POTION_JADE)
                put("turquoise", ItemSpriteSheet.POTION_TURQUOISE)
                put("azure", ItemSpriteSheet.POTION_AZURE)
                put("indigo", ItemSpriteSheet.POTION_INDIGO)
                put("magenta", ItemSpriteSheet.POTION_MAGENTA)
                put("bistre", ItemSpriteSheet.POTION_BISTRE)
                put("charcoal", ItemSpriteSheet.POTION_CHARCOAL)
                put("silver", ItemSpriteSheet.POTION_SILVER)
                put("ivory", ItemSpriteSheet.POTION_IVORY)
                put("darkgreen", ItemSpriteSheet.POTION_DARK_GREEN)
            }
        }

        private lateinit var handler: ItemStatusHandler<Potion>

        private const val REINFORCED = "reinforced"

        fun initColors() {
            handler = ItemStatusHandler(potions, colors)
        }

        fun save(bundle: Bundle) {
            handler.save(bundle)
        }

        fun saveSelectively(bundle: Bundle, items: ArrayList<Potion>) {
            handler.saveSelectively(bundle, items)
        }

        fun restore(bundle: Bundle) {
            handler = ItemStatusHandler(potions, colors, bundle)
        }

        val known: HashSet<Class<out Potion>>
            get() = handler.known()

        val unknown: HashSet<Class<out Potion>>
            get() = handler.unknown()

        fun allKnown(): Boolean = handler.known().size == potions.size
    }
}
