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
package com.egoal.darkestpixeldungeon.items.artifacts

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.items.potions.Potion
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndBag
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle
import com.watabou.utils.Random

import java.util.ArrayList
import java.util.Collections

class AlchemistsToolkit : Artifact() {

    //arrays used in containing potion collections for mix logic.
    val combination = ArrayList<String>()
    var curGuess = ArrayList<String>()
    var bstGuess = ArrayList<String>()

    var numWrongPlace = 0
    var numRight = 0

    private var seedsToPotion = 0

    protected var inventoryTitle = "Select a potion"
    protected var mode: WndBag.Mode = WndBag.Mode.POTION

    protected var itemSelector: WndBag.Listener = WndBag.Listener { item ->
        if (item != null && item is Potion && item.isIdentified) {
            if (!curGuess.contains(convertName(item.javaClass.simpleName))) {

                val hero = Dungeon.hero
                hero.sprite.operate(hero.pos)
                hero.busy()
                hero.spend(2f)
                Sample.INSTANCE.play(Assets.SND_DRINK)

                item.detach(hero.belongings.backpack)

                curGuess.add(convertName(item.javaClass.simpleName))
                if (curGuess.size == 3) {
                    guessBrew()
                } else {
                    GLog.i("You mix the " + item.name() + " into your current brew.")
                }
            } else {
                GLog.w("Your current brew already contains that potion.")
            }
        } else if (item != null) {
            GLog.w("You need to select an identified potion.")
        }
    }

    init {
        name = "alchemists tool kit"
        image = ItemSpriteSheet.ARTIFACT_TOOLKIT

        levelCap = 10
    }

    init {

        for (i in 1..3) {
            var potion: String
            do {
                potion = convertName(Generator.POTION.generate().javaClass
                        .simpleName)
                //forcing the player to use experience potions would be completely
                // unfair.
            } while (combination.contains(potion) || potion == "Experience")
            combination.add(potion)
        }
    }

    override fun actions(hero: Hero): ArrayList<String> {
        val actions = super.actions(hero)
        if (isEquipped(hero) && level() < levelCap && !cursed)
            actions.add(AC_BREW)
        return actions
    }

    override fun execute(hero: Hero, action: String) {

        super.execute(hero, action)

        if (action == AC_BREW) {
            GameScene.selectItem(itemSelector, mode, inventoryTitle)
        }
    }

    fun guessBrew() {
        if (curGuess.size != 3)
            return

        var numWrongPlace = 0
        var numRight = 0

        for (potion in curGuess) {
            if (combination.contains(potion)) {
                if (curGuess.indexOf(potion) == combination.indexOf(potion)) {
                    numRight++
                } else {
                    numWrongPlace++
                }
            }
        }

        var score = numRight * 3 + numWrongPlace

        if (score == 9)
            score++

        if (score == 0) {

            GLog.i("Your mixture is complete, but none of the potions you used seem" +
                    " to react well. " +
                    "The brew is useless, you throw it away.")

        } else if (score > level()) {

            level(score)
            seedsToPotion = 0
            bstGuess = curGuess
            this.numRight = numRight
            this.numWrongPlace = numWrongPlace

            if (level() == 10) {
                bstGuess = ArrayList()
                GLog.p("The mixture you've created seems perfect, you don't think " + "there is any way to improve it!")
            } else {
                GLog.w("you finish mixing potions, " + brewDesc(numWrongPlace,
                        numRight) +
                        ". This is your best brew yet!")
            }

        } else {

            GLog.w("you finish mixing potions, " + brewDesc(numWrongPlace, numRight) +
                    ". This brew isn't as good as the current one, you throw it " +
                    "away.")
        }
        curGuess = ArrayList()

    }

    private fun brewDesc(numWrongPlace: Int, numRight: Int): String {
        var result = ""
        if (numWrongPlace > 0) {
            result += "$numWrongPlace reacted well, but in the wrong order"
            if (numRight > 0)
                result += " and "
        }
        if (numRight > 0) {
            result += "$numRight reacted perfectly"
        }
        return result
    }

    override fun passiveBuff(): Artifact.ArtifactBuff {
        return alchemy()
    }

    override fun desc(): String {
        var result = "This toolkit contains a number of regents and herbs used" +
                " to improve the process of " +
                "cooking potions.\n\n"

        if (isEquipped(Dungeon.hero))
            if (cursed)
                result += "The cursed toolkit has bound itself to your side, and " + "refuses to let you use alchemy.\n\n"
            else
                result += "The toolkit rests on your hip, the various tools inside " + "make a light jingling sound as you move.\n\n"

        if (level() == 0) {
            result += "The toolkit seems to be missing a key tool, a catalyst " +
                    "mixture. You'll have to make your own " +
                    "out of three common potions to get the most out of the toolkit."
        } else if (level() == 10) {
            result += "The mixture you have created seems perfect, and the toolkit " + "is working at maximum efficiency."
        } else if (!bstGuess.isEmpty()) {
            result += ("Your current best mixture is made from: " + bstGuess[0]
                    + ", " + bstGuess[1] + ", "
                    + bstGuess[2] + ", in that order.\n\n")
            result += "Of the potions in that mix, " + brewDesc(numWrongPlace,
                    numRight) + "."

            //would only trigger if an upgraded toolkit was gained through
            // transmutation or bones.
        } else {
            result += "The toolkit seems to have a catalyst mixture already in it, " +
                    "but it isn't ideal. Unfortunately " +
                    "you have no idea what's in the mixture."
        }
        return result
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(NUMWRONGPLACE, numWrongPlace)
        bundle.put(NUMRIGHT, numRight)

        bundle.put(SEEDSTOPOTION, seedsToPotion)

        bundle.put(COMBINATION, combination.toTypedArray())
        bundle.put(CURGUESS, curGuess.toTypedArray())
        bundle.put(BSTGUESS, bstGuess.toTypedArray())
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        numWrongPlace = bundle.getInt(NUMWRONGPLACE)
        numRight = bundle.getInt(NUMRIGHT)

        seedsToPotion = bundle.getInt(SEEDSTOPOTION)

        combination.clear()
        Collections.addAll(combination, *bundle.getStringArray(COMBINATION)!!)
        Collections.addAll(curGuess, *bundle.getStringArray(CURGUESS)!!)
        Collections.addAll(bstGuess, *bundle.getStringArray(BSTGUESS)!!)
    }


    inner class alchemy : Artifact.ArtifactBuff() {

        fun tryCook(count: Int): Boolean {

            //this logic is handled inside the class with a variable so that it may
            // be stored.
            //to prevent manipulation where a player could keep throwing in 1-2
            // seeds until they get lucky.
            if (seedsToPotion == 0) {
                if (Random.Int(20) < 10 + level()) {
                    if (Random.Int(20) < level()) {
                        seedsToPotion = 1
                    } else
                        seedsToPotion = 2
                } else
                    seedsToPotion = 3
            }

            if (count >= seedsToPotion) {
                seedsToPotion = 0
                return true
            } else
                return false

        }

    }

    companion object {

        val AC_BREW = "BREW"

        private val COMBINATION = "combination"
        private val CURGUESS = "curguess"
        private val BSTGUESS = "bstguess"

        private val NUMWRONGPLACE = "numwrongplace"
        private val NUMRIGHT = "numright"

        private val SEEDSTOPOTION = "seedstopotion"
    }

}
