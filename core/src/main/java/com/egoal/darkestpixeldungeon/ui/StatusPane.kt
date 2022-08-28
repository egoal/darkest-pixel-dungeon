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
package com.egoal.darkestpixeldungeon.ui

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.Dungeon.depth
import com.egoal.darkestpixeldungeon.Dungeon.hero
import com.egoal.darkestpixeldungeon.Statistics
import com.egoal.darkestpixeldungeon.Statistics.Clock
import com.egoal.darkestpixeldungeon.actors.buffs.Pressure
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.unclassified.Amulet
import com.egoal.darkestpixeldungeon.items.unclassified.Torch
import com.egoal.darkestpixeldungeon.messages.M.L
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.scenes.PixelScene
import com.egoal.darkestpixeldungeon.sprites.HeroSprite.Companion.Portrait
import com.egoal.darkestpixeldungeon.ui.Icons
import com.egoal.darkestpixeldungeon.ui.StatusPane
import com.egoal.darkestpixeldungeon.ui.Toolbar.PickedUpItem
import com.egoal.darkestpixeldungeon.windows.WndGame
import com.egoal.darkestpixeldungeon.windows.WndHero
import com.egoal.darkestpixeldungeon.windows.WndJournal
import com.egoal.darkestpixeldungeon.windows.WndTitledMessage
import com.watabou.input.Touchscreen
import com.watabou.noosa.*
import com.watabou.noosa.audio.Sample
import com.watabou.noosa.particles.Emitter
import com.watabou.noosa.ui.Button
import com.watabou.noosa.ui.Component
import com.watabou.utils.ColorMath

// status pane in the game scene
class StatusPane : Component() {
    private lateinit var bg: NinePatch
    private lateinit var levelBg: NinePatch
    private lateinit var portrait: Image
    private var warning = 0f
    private var lastTier = 0
    private var rawShielding: Image? = null
    private var shieldedHP: Image? = null
    private lateinit var hp: Image
    private lateinit var exp: Image
    private lateinit var san: Image
    private var bossHP: BossHealthBar? = null
    private var lastLvl = -1
    private lateinit var level: BitmapText
    private lateinit var depth: BitmapText
    private lateinit var version: BitmapText
    private lateinit var hpstr: BitmapText
    private var danger: DangerIndicator? = null
    private var buffs: BuffIndicator? = null
    private var compass: Compass? = null

    //    private ClockIndicator clock;
    private var perkSelector: PerkSelectIndicator? = null
    private var torchIndicator: TorchIndicator? = null
    private var btnJournal: JournalButton? = null
    private var btnMenu: MenuButton? = null
    private var btnClock: ClockButton? = null
    private var pickedUp: PickedUpItem? = null

    override fun createChildren() {
        bg = NinePatch(Assets.DPD_STATUS, 0, 0, 176, 32, 85, 0, 58, 0)
        add(bg)

        // hero portrait touch area
        add(object : TouchArea(0f, 1f, 31f, 31f) {
            override fun onClick(touch: Touchscreen.Touch) {
                val sprite: Image = hero.sprite
                if (!sprite.isVisible) {
                    Camera.main.focusOn(sprite)
                }
                GameScene.show(WndHero())
            }
        })

        // journal
        btnJournal = JournalButton()
        add(btnJournal)
        btnMenu = MenuButton()
        add(btnMenu)
        btnClock = ClockButton()
        add(btnClock)
        portrait = Portrait(hero.heroClass, lastTier)
        add(portrait)
        var compassTarget = Dungeon.level.exit
        if (hero.belongings.getItem(Amulet::class.java) != null) compassTarget = Dungeon.level.entrance
        compass = Compass(compassTarget)
        add(compass)

        // hp bar
        rawShielding = Image(Assets.SHLD_BAR)
        rawShielding!!.alpha(0.5f)
        add(rawShielding)
        shieldedHP = Image(Assets.SHLD_BAR)
        add(shieldedHP)
        hp = Image(Assets.HP_BAR)
        add(hp)

        // sanity
        san = Image(Assets.DPD_SAN_BAR)
        add(san)

        // exp bar
        exp = Image(Assets.XP_BAR)
        add(exp)

        // boss hp
        bossHP = BossHealthBar()
        add(bossHP)

        // the others
        levelBg = NinePatch(Assets.DPD_STATUS, 0, 32, 15, 15, 3)
        add(levelBg)
        level = BitmapText(PixelScene.pixelFont)
        level.hardlight(0xFFEBA4)
        add(level)
        depth = BitmapText(Integer.toString(Dungeon.depth), PixelScene.pixelFont)
        depth.hardlight(0xCACFC2)
        depth.measure()
        add(depth)
        version = BitmapText(Dungeon.VERSION_STRING, PixelScene.pixelFont)
        version.hardlight(0xcacfc2)
        version.measure()
        if (Dungeon.VERSION_STRING.isNotEmpty()) add(version)

        hpstr = BitmapText("20/20", PixelScene.pixelFont)
        hpstr.hardlight(0xcacfc2)
        hpstr.alpha(0.5f)
        hpstr.measure()
        add(hpstr)
        danger = DangerIndicator()
        add(danger)

//        clock = new ClockIndicator();
//        add(clock);
        perkSelector = PerkSelectIndicator()
        add(perkSelector)
        torchIndicator = TorchIndicator()
        add(torchIndicator)
        buffs = BuffIndicator(hero)
        add(buffs)
        add(PickedUpItem().also { pickedUp = it })
    }

    override fun layout() {
        height = 32f
        bg.size(width, bg.height)
        portrait.x = bg.x + 15 - portrait.width / 2f
        portrait.y = bg.y + 16 - portrait.height / 2f
        PixelScene.align(portrait)
        compass!!.x = portrait.x + portrait.width / 2f - compass!!.origin.x
        compass!!.y = portrait.y + portrait.height / 2f - compass!!.origin.y
        PixelScene.align(compass)
        rawShielding!!.x = 30f
        shieldedHP!!.x = rawShielding!!.x
        hp!!.x = shieldedHP!!.x
        rawShielding!!.y = 3f
        shieldedHP!!.y = rawShielding!!.y
        hp!!.y = shieldedHP!!.y
        hpstr.y = hp!!.y - 1f
        hpstr!!.x = hp!!.x + 24f - hpstr!!.width() / 2f
        san!!.x = hp!!.x
        san!!.y = 8f
        bossHP!!.setPos(6 + (width - bossHP!!.width()) / 2, 20f)
        depth!!.x = width - 50.5f - depth!!.width() / 2f
        depth!!.y = 8f - depth!!.baseLine() / 2f
        PixelScene.align(depth)
        version!!.x = 2f
        version!!.y = bg!!.height + 2
        PixelScene.align(version)
        danger!!.setPos(width - danger!!.width(), 20f)

//        clock.setPos(width - clock.width(), danger.bottom() + 4);
        torchIndicator!!.setPos(0f, version!!.y + version!!.height + 4)
        perkSelector!!.setPos(0f, torchIndicator!!.bottom() + 4)
        buffs!!.setPos(34f, 12f)
        btnClock!!.setPos(width - 45, 1f)
        btnJournal!!.setPos(width - 42, 1f)
        btnMenu!!.setPos(width - btnMenu!!.width(), 1f)
    }

    override fun update() {
        super.update()
        if (needsCompassUpdate) {
            needsCompassUpdate = false
            compass!!.visible = false
            compass!!.update()
        }
        val health = hero.HP.toFloat()
        val shield = hero.SHLD.toFloat()
        val max = hero.HT.toFloat()
        val p = hero.pressure

        // the portrait effect
        if (!hero.isAlive) {
            portrait!!.tint(0x000000, 0.5f)
        } else if (health / max < 0.3f) {
            warning += Game.elapsed * 5f * (0.4f - health / max)
            warning %= 1f
            portrait!!.tint(ColorMath.interpolate(warning, 0x660000, 0xCC0000,
                    0x660000), 0.5f)
        } else if (p.level === Pressure.Level.NERVOUS || p.level ===
                Pressure.Level.COLLAPSE) {
            warning += Game.elapsed * 5f * (0.4f - health / max)
            warning %= 1f
            portrait!!.tint(ColorMath.interpolate(warning, 0x333333, 0x666666,
                    0x333333), 0.5f)
        } else {
            portrait!!.resetColor()
        }
        levelBg!!.x = 27.5f - levelBg!!.width() / 2f
        levelBg!!.y = 28f - levelBg!!.height() / 2f
        PixelScene.align(levelBg)

        // bars
        hp!!.scale.x = Math.max(0f, (health - shield) / max)
        if (hero.SHLD > 0) hpstr!!.text(String.format("%d+%d/%d",
                hero.HP, hero.SHLD, hero.HT)) else hpstr!!.text(String.format("%d/%d", hero.HP, hero.HT))
        hpstr!!.measure()
        hpstr!!.x = hp!!.x + 24f - hpstr!!.width() / 2f
        shieldedHP!!.scale.x = health / max
        rawShielding!!.scale.x = shield / max
        san!!.scale.x = Math.max(0f, p.pressure / Pressure.MAX_PRESSURE)
        exp!!.scale.x = width / exp!!.width * hero.exp / hero
                .maxExp()
        if (hero.lvl != lastLvl) {
            if (lastLvl != -1) {
                val emitter = recycle(Emitter::class.java) as Emitter
                emitter.revive()
                emitter.pos(27f, 27f)
                emitter.burst(Speck.factory(Speck.STAR), 12)
            }
            lastLvl = hero.lvl
            level!!.text(Integer.toString(lastLvl))
            level!!.measure()
            level!!.x = 27.5f - level!!.width() / 2f
            level!!.y = 28.0f - level!!.baseLine() / 2f
            PixelScene.align(level)
        }
        val tier = hero.tier()
        if (tier != lastTier) {
            lastTier = tier
            portrait!!.copy(Portrait(hero.heroClass, tier))
        }
    }

    fun pickup(item: Item?) {
        if (item is Torch) pickedUp!!.reset(item, torchIndicator!!.centerX(), torchIndicator!!.centerY(), true) else pickedUp!!.reset(item,
                btnJournal!!.icon!!.x + btnJournal!!.icon!!.width() / 2f,
                btnJournal!!.icon!!.y + btnJournal!!.icon!!.height() / 2f,
                true)
    }

    private class JournalButton : Button() {
        private var bg: Image? = null

        //used to display key state to the player
        var icon: Image? = null
        override fun createChildren() {
            super.createChildren()
            bg = Image(Assets.DPD_MENU, 2, 2, 13, 11)
            add(bg)
            icon = Image(Assets.DPD_MENU, 31, 0, 11, 7)
            add(icon)
            needsKeyUpdate = true
        }

        override fun layout() {
            super.layout()
            bg!!.x = x + 13
            bg!!.y = y + 2
            icon!!.x = bg!!.x + (bg!!.width() - icon!!.width()) / 2f
            icon!!.y = bg!!.y + (bg!!.height() - icon!!.height()) / 2f
            PixelScene.align(icon)
        }

        override fun update() {
            super.update()
            if (needsKeyUpdate) updateKeyDisplay()
        }

        fun updateKeyDisplay() {
            needsKeyUpdate = false
            var foundKeys = false
            var blackKey = false
            var specialKey = false
            var ironKeys = 0
            for (i in 0..Math.min(depth, 25)) {
                if (hero.belongings.ironKeys[i] > 0 || hero
                                .belongings.specialKeys[i] > 0) {
                    foundKeys = true
                    if (i < depth) {
                        blackKey = true
                    } else {
                        if (hero.belongings.specialKeys[i] > 0) {
                            specialKey = true
                        }
                        ironKeys = hero.belongings.ironKeys[i]
                    }
                }
            }
            if (!foundKeys) {
                icon!!.frame(31, 0, 11, 7)
            } else {
                var left = 46
                var top = 0
                var width = 0
                val height = 7
                if (blackKey) {
                    left = 43
                    width += 3
                }
                if (specialKey) {
                    top = 8
                    width += 3
                }
                width += ironKeys * 3
                width = Math.min(width, 9)
                icon!!.frame(left, top, width, height)
            }
            layout()
        }

        override fun onTouchDown() {
            bg!!.brightness(1.5f)
            icon!!.brightness(1.5f)
            Sample.INSTANCE.play(Assets.SND_CLICK)
        }

        override fun onTouchUp() {
            bg!!.resetColor()
            icon!!.resetColor()
        }

        override fun onClick() {
            GameScene.show(WndJournal())
        }

        init {
            width = bg!!.width + 13 //includes the depth display to the left
            height = bg!!.height + 4
        }
    }

    private class MenuButton : Button() {
        private var image: Image? = null
        override fun createChildren() {
            super.createChildren()
            image = Image(Assets.DPD_MENU, 17, 2, 12, 11)
            add(image)
        }

        override fun layout() {
            super.layout()
            image!!.x = x + 2
            image!!.y = y + 2
        }

        override fun onTouchDown() {
            image!!.brightness(1.5f)
            Sample.INSTANCE.play(Assets.SND_CLICK)
        }

        override fun onTouchUp() {
            image!!.resetColor()
        }

        override fun onClick() {
            GameScene.show(WndGame())
        }

        init {
            width = image!!.width + 4
            height = image!!.height + 4
        }
    }

    private class ClockButton : Button() {
        private var image: Image? = null
        var lastState = Statistics.ClockTime.State.Day
        override fun createChildren() {
            super.createChildren()
            image = Image(Assets.DPD_MENU, 0, 16, 12, 11)
            add(image)
        }

        override fun layout() {
            super.layout()
            image!!.x = x + 2
            image!!.y = y + 2
        }

        override fun onTouchDown() {
            image!!.brightness(1.5f)
            Sample.INSTANCE.play(Assets.SND_CLICK)
        }

        override fun onTouchUp() {
            image!!.resetColor()
        }

        override fun onClick() {
            GameScene.show(WndTitledMessage(Icons.INFO.get(), L(StatusPane::class.java, "clock"),
                    L(StatusPane::class.java, "clock_desc", Clock.timestr)))
        }

        override fun update() {
            if (Clock.state !== lastState) {
                lastState = Clock.state
                var left = 0
                left = when (lastState) {
                    Statistics.ClockTime.State.Day -> 0
                    Statistics.ClockTime.State.Night -> 12
                    Statistics.ClockTime.State.MidNight -> 24
                }
                image!!.frame(left, 16, 12, 11)
            }
        }

        init {
            width = image!!.width + 4
            height = image!!.height + 4
        }
    }

    companion object {
        //fixme: due to the design, this is used as an interface, not a good idea.
        var needsKeyUpdate = false
        var needsCompassUpdate = false
    }
}