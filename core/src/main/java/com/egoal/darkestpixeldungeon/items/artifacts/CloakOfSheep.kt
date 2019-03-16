package com.egoal.darkestpixeldungeon.items.artifacts

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.Sheep
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfTeleportation
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.traps.FlockTrap
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.CellSelector
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Random
import java.util.ArrayList

class CloakOfSheep : Artifact() {
    init {
        image = ItemSpriteSheet.CLOAK_OF_SHEEP

        levelCap = 10

        cooldown = 0

        defaultAction = AC_BLINK
    }

    override fun actions(hero: Hero): ArrayList<String> {
        val actions = super.actions(hero)
        if (isEquipped(hero) && !cursed && cooldown <= 0)
            actions.add(AC_BLINK)
        return actions
    }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)

        if (action == AC_BLINK) {
            if (!isEquipped(hero))
                GLog.i(Messages.get(Artifact::class.java, "need_to_equip"))
            else if (cursed)
                GLog.i(Messages.get(this, "cursed"))
            else if (cooldown > 0)
                GLog.i(Messages.get(this, "not-ready"))
            else if (hero.rooted)
                GLog.i(Messages.get(this, "cannot-move"))
            else {
                // blink
                GameScene.selectCell(caster)
            }
        }
    }

    private fun cd(): Int = 25 - level()
    private fun range(): Int = 5 + level()
    private fun requireExp(): Int = (level() + 2) * (level() + 1)

    private fun blink(cell: Int) {
        cooldown = cd()
        exp += 1
        if (exp > requireExp() && level() < levelCap) {
            exp -= requireExp()
            upgrade()
            GLog.p(Messages.get(this, "levelup"))
        }

        // create a sheep here
        val s = Sheep().apply {
            lifespan = 2f + Random.Int(Dungeon.depth + 10).toFloat()
            pos = Item.curUser.pos
        }
        GameScene.add(s)
        CellEmitter.get(cell).burst(Speck.factory(Speck.WOOL), 4)
        s.yell(Messages.get(s, "baa?"))

        // jump there
        ScrollOfTeleportation.appear(Item.curUser, cell)
        Item.curUser.spendAndNext(.5f)
        Dungeon.observe()

        Sample.INSTANCE.play(Assets.SND_PUFF)

        updateQuickslot()
    }

    override fun passiveBuff(): ArtifactBuff = Recharge()

    override fun desc(): String {
        var desc = super.desc()
        if (isEquipped(Dungeon.hero)) {
            desc += "\n\n" + Messages.get(this, "desc_hint", range())
        }
        return desc
    }

    inner class Recharge : Artifact.ArtifactBuff() {
        override fun act(): Boolean {
            if (cooldown > 0 && !cursed)
                --cooldown

            if (cursed && Random.Int(100) == 0) {
                FlockTrap.ActivateAt(target.pos)
            }

            updateQuickslot()
            spend(Actor.TICK)
            return true
        }
    }

    private val caster = object : CellSelector.Listener {
        override fun onSelect(cell: Int?) {
            if (cell != null && (Dungeon.level.visited[cell] || Dungeon.level.mapped[cell])) {
                // valid 
                if (Dungeon.level.distance(cell, Item.curUser.pos) > range())
                    GLog.w(Messages.get(CloakOfSheep::class.java, "out-of-range"))
                else if (Level.solid[cell] || Actor.findChar(cell) != null)
                    GLog.w(Messages.get(CloakOfSheep::class.java, "cannot-go-there"))
                else
                    blink(cell)
            }
        }

        override fun prompt(): String = Messages.get(CloakOfSheep::class.java, "prompt")
    }

    companion object {
        private const val AC_BLINK = "blink"
    }
}