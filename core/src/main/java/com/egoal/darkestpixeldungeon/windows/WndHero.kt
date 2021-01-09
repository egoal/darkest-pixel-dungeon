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
package com.egoal.darkestpixeldungeon.windows

import com.egoal.darkestpixeldungeon.Statistics
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Hunger
import com.egoal.darkestpixeldungeon.actors.buffs.Pressure
import com.egoal.darkestpixeldungeon.scenes.PixelScene
import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.hero.HeroSubClass
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.HeroSprite
import com.egoal.darkestpixeldungeon.ui.*
import com.watabou.gltextures.SmartTexture
import com.watabou.gltextures.TextureCache
import com.watabou.noosa.Group
import com.watabou.noosa.Image
import com.watabou.noosa.RenderedText
import com.watabou.noosa.TextureFilm
import com.watabou.noosa.ui.Button

import java.util.Locale
import kotlin.math.round

// window shown when press the status pane avatar
class WndHero : WndTabbed() {
    private val stats: StatsTab
    private val buffs: BuffsTab
    private val details: DetailsTab
    private val perks: PerksTab

    private val icons: SmartTexture = TextureCache.get(Assets.BUFFS_LARGE)
    // private val perkIcons = TextureCache.get(Assets.PERKS)
    private val film: TextureFilm

    init {
        film = TextureFilm(icons, 16, 16)

        stats = StatsTab()
        add(stats)

        buffs = BuffsTab()
        add(buffs)

        perks = PerksTab()
        add(perks)

        details = DetailsTab()
        add(details)

        add(object : WndTabbed.LabeledTab(Messages.get(this, "stats")) {
            override fun select(value: Boolean) {
                super.select(value)
                stats.active = selected
                stats.visible = stats.active
            }
        })
        add(object : WndTabbed.LabeledTab(Messages.get(this, "buffs")) {
            override fun select(value: Boolean) {
                super.select(value)
                buffs.active = selected
                buffs.visible = buffs.active
            }
        })
        add(object : WndTabbed.LabeledTab(Messages.get(this, "perks")) {
            override fun select(value: Boolean) {
                super.select(value)
                perks.active = selected
                perks.visible = perks.active
            }
        })
        add(object : WndTabbed.LabeledTab(Messages.get(this, "details")) {
            override fun select(value: Boolean) {
                super.select(value)
                details.active = selected
                details.visible = details.active
            }
        })

        //todo: update height
        resize(WIDTH, Math.max(stats.height(), buffs.height()).toInt())

        layoutTabs()

        select(0)
    }

    private inner class StatsTab : Group() {

        private var pos: Float = 0f

        init {

            val hero = Dungeon.hero

            val title = IconTitle(HeroSprite.avatar(hero.heroClass, hero.tier()), hero.userName)
//            title.icon(HeroSprite.avatar(hero.heroClass, hero.tier()))
//            if (hero.givenName() == hero.className())
//                title.label(Messages.get(this, "title", hero.lvl, hero.className())
//                        .toUpperCase(Locale.ENGLISH))
//            else
//                title.label((hero.givenName() + "\n" + Messages.get(this, "title",
//                        hero.lvl, hero.className())).toUpperCase(Locale.ENGLISH))
            // title.color(Window.SHPX_COLOR)
            title.setRect(0f, 0f, WIDTH.toFloat(), 0f)
            add(title)

            pos = title.bottom() + 2 * GAP5

            val className = M.L(this, "title", hero.lvl, hero.className()).toUpperCase(Locale.ENGLISH)
            val classNameText = PixelScene.renderText(className, 8)
            classNameText.x = 0f
            classNameText.y = pos
            classNameText.hardlight(Window.DPD_COLOR)
            add(classNameText)

            if (hero.lvl >= 12 && hero.subClass == HeroSubClass.NONE) {
                val btn = object : RedButton(M.L(this, "choose_way")) {
                    override fun onClick() {
                        hide()
                        WndMasterSubclass.Show(hero)
                    }
                }
                btn.setRect(WIDTH / 2f + GAP5, classNameText.y, 40f, classNameText.height())
                add(btn)
            } else {
                hero.challenge?.let {
                    val btn = object : RedButton(it.title()) {
                        override fun onClick() {
                            GameScene.show(WndMessage(it.desc()))
                        }
                    }
                    btn.setRect(classNameText.x + classNameText.width() + GAP5, classNameText.y, 40f, classNameText.height())
                    add(btn)
                }
            }

            pos = classNameText.y + classNameText.height() + 2 * GAP5

            statSlot(Messages.get(this, "str"), hero.STR())
            if (hero.SHLD > 0)
                statSlot(M.L(this, "health"), "${hero.HP}+${hero.SHLD}/${hero.HT}")
            else
                statSlot(M.L(this, "health"), "${hero.HP}/${hero.HT}")
            statSlot(M.L(this, "exp"), "${hero.exp}/${hero.maxExp()}")

            // sanity slot
            val p = hero.buff(Pressure::class.java)
            if (p != null)
                statSlot(M.L(this, "sanity"), "${p.pressure.toInt()}/${Pressure.MAX_PRESSURE.toInt()}")

            // add hunger state slot
            val hg = hero.buff(Hunger::class.java)
            if (hg != null)
                statSlot(M.L(this, "hunger"), "${hg.hunger()}/${Hunger.STARVING.toInt()}")

            pos += GAP5.toFloat()

            statSlot(Messages.get(this, "time"), Statistics.Clock.timestr)
            statSlot(Messages.get(this, "gold"), Statistics.GoldCollected)
            statSlot(Messages.get(this, "depth"), Statistics.DeepestFloor)

            pos += GAP5.toFloat()
        }

        private fun statSlot(label: String, value: String) {

            var txt = PixelScene.renderText(label, 8)
            txt.y = pos
            add(txt)

            txt = PixelScene.renderText(value, 8)
            txt.x = WIDTH * 0.6f
            txt.y = pos
            PixelScene.align(txt)
            add(txt)

            pos += GAP5 + txt.baseLine()
        }

        private fun statSlot(label: String, value: Int) {
            statSlot(label, Integer.toString(value))
        }

        fun height(): Float = pos
    }

    private inner class BuffsTab : Group() {

        private var pos: Float = 0f

        init {
            for (buff in Dungeon.hero.buffs()) {
                if (buff.icon() != BuffIndicator.NONE) {
                    val slot = BuffSlot(buff)
                    slot.setRect(0f, pos, WIDTH.toFloat(), slot.icon.height())
                    add(slot)
                    pos += GAP2 + slot.height()
                }
            }
        }

        fun height(): Float = pos

        private inner class BuffSlot(private val buff: Buff) : Button() {

            internal var icon: Image
            internal var txt: RenderedText

            init {
                val index = buff.icon()

                icon = Image(icons)
                icon.frame(film.get(index))
                icon.y = this.y
                add(icon)

                txt = PixelScene.renderText(buff.toString(), 8)
                txt.x = icon.width + GAP2
                txt.y = this.y + (icon.height - txt.baseLine()).toInt() / 2
                add(txt)

            }

            override fun layout() {
                super.layout()
                icon.y = this.y
                txt.x = icon.width + GAP2
                txt.y = pos + (icon.height - txt.baseLine()).toInt() / 2
            }

            override fun onClick() {
                GameScene.show(WndInfoBuff(buff))
            }
        }
    }

    private inner class PerksTab : Group() {
        init {
            // 5
            val left = 4f // 116- 20x5- 4x2 = 8
            val top = 4f
            for (i in 0 until Dungeon.hero.heroPerk.perks.size) {
                val slot = PerkSlot(Dungeon.hero.heroPerk.perks[i])
                val r = i / 5
                val c = i % 5
                slot.setRect(left + (20 + GAP2) * c, top + (20 + GAP2) * r, 20f, 20f)
                add(slot)
            }
        }
    }

    private inner class DetailsTab : Group() {
        lateinit var resistIcons: SmartTexture

        init {
            // resistance
//            var top = 0f
//            top = layoutResistances(top)

            val resistanceIndicator = ResistanceIndicator(Dungeon.hero)
            add(resistanceIndicator)
            resistanceIndicator.setRect(3f, 3f, WIDTH.toFloat() - 6f, 0f)

            // extra
            var thetop = resistanceIndicator.bottom() + 3f
            val hero = Dungeon.hero
            thetop = addLine(thetop, M.L(this, "critical_chance", round(hero.criticalChance() * 100).toInt()))
            thetop = addLine(thetop, M.L(this, "evasion_chance", round(hero.evasionProbability() * 100).toInt()))
            if (hero.isAlive) addLine(thetop, M.L(this, "regeneration", hero.regenerateSpeed()))
        }

        private fun layoutResistances(top: Float): Float {
            var thetop = top
            val ICON_SIZE = 8
            val FONT_SIZE = 6
            val GAP = 3f

            resistIcons = TextureCache.get(Assets.DPD_CONS_ICONS)

            val rt = PixelScene.renderText(M.L(this, "elemental_resistance"), FONT_SIZE)
            rt.y = thetop
            add(rt)

            val hero = Dungeon.hero

            for (i in 0 until Damage.Element.ELEMENT_COUNT) {
                val icon = Image(resistIcons)
                icon.frame(ICON_SIZE * i, 16, ICON_SIZE, ICON_SIZE)
                icon.x = GAP
                icon.y = rt.y + rt.height() + ((GAP + ICON_SIZE) * i).toFloat()
                add(icon)

                val txt = PixelScene.renderText(String.format("%+2d%%", (hero.elementalResistance[i] * 100).toInt()), FONT_SIZE)
                txt.x = icon.width + GAP
                txt.y = (icon.height - txt.baseLine()) / 2 + icon.y
                add(txt)

                thetop = icon.y + icon.height() + GAP
            }

            thetop = addLine(thetop, M.L(this, "magical_resistance", (hero.magicalResistance() * 100).toInt()))
            thetop = addLine(thetop, M.L(this, "critical_chance", round(hero.criticalChance() * 100).toInt()))
            thetop = addLine(thetop, M.L(this, "evasion_chance", round(hero.evasionProbability() * 100).toInt()))
            if (hero.isAlive)
                thetop = addLine(thetop, M.L(this, "regeneration", hero.regenerateSpeed()))

            return thetop
        }

        private fun addLine(top: Float, line: String): Float {
            val lbl = PixelScene.renderText(line, 6)
            lbl.x = 3f
            lbl.y = top
            add(lbl)

            return top + lbl.height() + 3f
        }
    }

    companion object {
        private const val WIDTH = 116
        private const val GAP5 = 5
        private const val GAP2 = 2
    }
}
