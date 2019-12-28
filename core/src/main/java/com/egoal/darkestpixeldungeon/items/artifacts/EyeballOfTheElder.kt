package com.egoal.darkestpixeldungeon.items.artifacts

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Badges
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.DungeonTilemap
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Disarm
import com.egoal.darkestpixeldungeon.actors.buffs.Vulnerable
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.Beam
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.effects.particles.PurpleParticle
import com.egoal.darkestpixeldungeon.effects.particles.ShadowParticle
import com.egoal.darkestpixeldungeon.items.EquipableItem
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.CellSelector
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.ui.QuickSlotButton
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Random
import java.util.ArrayList
import javax.microedition.khronos.opengles.GL
import kotlin.math.min

//todo: code refactor
class EyeballOfTheElder : Artifact() {
    init {
        image = ItemSpriteSheet.EYEBALL_PAIR
        levelCap = 10
        cooldown = 0
    }

    override fun desc(): String {
        var desc = super.desc()
        desc += "\n" + Messages.get(this, "desc_hint")
        if (isEquipped(Dungeon.hero)) desc += "\n\n" + M.L(this, "desc_pair")

        return desc
    }

    override fun actions(hero: Hero): ArrayList<String> = if (isEquipped(hero)) ArrayList() else super.actions(hero)

    override fun doUnequip(hero: Hero, collect: Boolean, single: Boolean): Boolean {
        GLog.n(M.L(this, "cannot_unequip"))
        return false
    }

    override fun status(): String? = null // dont show info.

    override fun passiveBuff(): ArtifactBuff = HisEyes()

    private fun requireExp(): Int = level() * level() + 2

    private fun trigger() {
        // effects
        val p = Random.Float()
        val triggered = when {
            p < 0.35f -> zapAll()
            p < 0.65f -> gazeAll()
            p < 0.9f -> disarmAll()
            else -> kill()
        }

        if (triggered) {
            exp += 1

            if (exp > requireExp() && level() < levelCap) {
                exp -= requireExp()
                val isCursed = cursed
                upgrade()
                cursed = isCursed

                Item.curUser.takeDamage(Damage(3 + Random.Int(level()), this, Item.curUser).type(Damage.Type.MENTAL))
                Buff.prolong(Item.curUser, Disarm::class.java, 1f + level().toFloat())
                CellEmitter.get(Item.curUser.pos).burst(ShadowParticle.CURSE, 3 + level() / 2)
                Sample.INSTANCE.play(Assets.SND_BURNING)

                GLog.p(M.L(this, "levelup"))
            }
        }
    }

    private fun zapAll(): Boolean {
        if (Dungeon.hero.visibleEnemies() == 0) return false

        val zapRange = 5 + level()

        val hero = Dungeon.hero

        val enemies = (0 until hero.visibleEnemies()).map { hero.visibleEnemy(it) }.filter {
            it.isAlive && Dungeon.level.distance(it.pos, hero.pos) < zapRange
        }

        if (enemies.isEmpty()) return false

        for (e in enemies) {
            val beam = Ballistica(hero.pos, e.pos, Ballistica.WONT_STOP)
            hero.sprite.parent.add(Beam.ThickDeathRay(
                    DungeonTilemap.tileCenterToWorld(beam.sourcePos),
                    DungeonTilemap.tileCenterToWorld(beam.path[beam.dist])))

            var zapdmg = Random.IntRange(4 + level(), 6 + level() * 4)
            if (cursed) zapdmg += (zapdmg * 0.3f).toInt()
            val dmg = Damage(zapdmg, hero, e).type(Damage.Type.MAGICAL).addElement(Damage.Element.SHADOW)
            e.takeDamage(dmg)
            e.sprite.flash()
        }

        CellEmitter.center(hero.pos).burst(PurpleParticle.BURST, Random.IntRange(6, 12))
        hero.interrupt()
        if (enemies.size > 2) Buff.prolong(hero, Disarm::class.java, 3f)

        cooldown = Random.NormalIntRange(15, 25)
        return true
    }

    private fun gazeAll(): Boolean {
        if (Dungeon.hero.visibleEnemies() == 0) return false

        for (i in 0 until Dungeon.hero.visibleEnemies()) {
            val e = Dungeon.hero.visibleEnemy(i)
            if (e.isAlive) {
                Buff.prolong(e, Vulnerable::class.java, 5.1f + level()).apply {
                    ratio = if (cursed) 1.5f else 1.3f
                    dmgType = Damage.Type.MAGICAL
                }
                e.sprite.emitter().burst(Speck.factory(Speck.LIGHT), 3)
            }
        }

        cooldown = Random.NormalIntRange(8, 15)
        return true
    }

    private fun disarmAll(): Boolean {
        if (Dungeon.hero.visibleEnemies() == 0) return false

        for (i in 0 until Dungeon.hero.visibleEnemies()) {
            val e = Dungeon.hero.visibleEnemy(i)
            if (e.isAlive) {
                Buff.prolong(e, Disarm::class.java, 1.1f + level())
                e.sprite.emitter().burst(Speck.factory(Speck.LIGHT), 12)
            }
        }

        Sample.INSTANCE.play(Assets.SND_DEGRADE)

        cooldown = Random.NormalIntRange(15, 20)
        return true
    }

    private fun kill(): Boolean {
        if (Dungeon.hero.visibleEnemies() == 0) return false

        // dont slay bosses.
        val avails = (0 until Dungeon.hero.visibleEnemies()).map { Dungeon.hero.visibleEnemy(it) }.filter {
            it.isAlive && !it.properties().contains(Char.Property.BOSS)
        }
        if (avails.isEmpty()) return false

        val target = avails.random()
        target.takeDamage(Damage(target.HT, Dungeon.hero, target).type(Damage.Type.MAGICAL).addFeature(Damage.Feature.PURE))
        target.sprite.emitter().burst(ShadowParticle.UP, 10)
        if (!target.isAlive) {
            GLog.w(M.L(this, "instant_kill", target.name))
            Dungeon.hero.takeDamage(Damage(Random.IntRange(1, 4), this, Dungeon.hero).type(Damage.Type.MENTAL))
        }

        cooldown = Random.NormalIntRange(10, 20)
        return true
    }

    inner class HisEyes : ArtifactBuff() {
        override fun act(): Boolean {
            if (cooldown > 0) --cooldown
            else {
                Item.curUser = Dungeon.hero
                trigger()
            }

            spend(Actor.TICK)
            return true
        }
    }

    class Left : Artifact() {
        init {
            image = ItemSpriteSheet.ARTIFACT_EYEBALL
            levelCap = 10

            cooldown = 0
            defaultAction = "NONE"
        }

        override fun random(): Item = this.apply {
            cursed = Random.Float() < 0.7f
        }

        override fun desc(): String {
            var desc = M.L(EyeballOfTheElder::class.java, "desc")
            desc += "\n" + M.L(EyeballOfTheElder::class.java, "desc_hint")
            if (isEquipped(Dungeon.hero)) desc += "\n\n" + M.L(this, "desc_left")

            return desc
        }

        private fun cd(): Int = 20
        private fun zapRange(): Int = 5 + level()
        private fun zapDamage(): Int {
            val value = Random.IntRange(3 + level(), 4 + level() * 3)
            return if (cursed) (value * 1.3f).toInt() else value
        }

        private fun requireExp(): Int = level() * level() + 2

        private fun zapDeath(pos: Int) {
            cooldown = cd()
            exp += 1
            if (exp > requireExp() && level() < levelCap) {
                exp -= requireExp()
                // not clear curse state
                val isCursed = cursed
                upgrade()
                cursed = isCursed

                Item.curUser.takeDamage(Damage(2 + Random.Int(0, level()), this, Item.curUser).type(Damage.Type.MENTAL))
                CellEmitter.get(Item.curUser.pos).burst(ShadowParticle.CURSE, 5)
                Sample.INSTANCE.play(Assets.SND_BURNING)

                GLog.p(M.L(EyeballOfTheElder::class.java, "levelup"))
            }

            val beam = Ballistica(Item.curUser.pos, pos, Ballistica.WONT_STOP)
            val dist = min(zapRange(), beam.dist)

            val dstCell = beam.path[dist]
            Item.curUser.sprite.parent.add(Beam.ThickDeathRay(
                    DungeonTilemap.tileCenterToWorld(beam.sourcePos),
                    DungeonTilemap.tileCenterToWorld(dstCell)))

            // do it
            // var lightAffected = false
            var terrainAffected = false
            for (cell in beam.subPath(1, dist)) {
                // like disintegration, but can destroy walls
                if (Level.flamable[cell]) { // || Level.losBlocking[cell]) {
                    Dungeon.level.destroy(cell)
                    GameScene.updateMap(cell)
//                if (Level.luminary[cell]) {
//                    Level.luminary[cell] = false
//                    lightAffected = true
//                }
                    terrainAffected = true
                }

                Actor.findChar(cell)?.let {
                    val dmg = Damage(zapDamage(), Item.curUser, it).type(Damage.Type.MAGICAL).addElement(Damage.Element.SHADOW)
                    // no need to check damage
                    it.takeDamage(dmg)
                    it.sprite.flash()
                    CellEmitter.center(cell).burst(PurpleParticle.BURST, Random.IntRange(2, 3))
                }
            }

            if (terrainAffected) Dungeon.observe()

            Item.curUser.sprite.turnTo(Item.curUser.pos, pos)
            Item.curUser.interrupt()
            GLog.n(M.L(this, "on-zap"))
        }

        override fun passiveBuff(): ArtifactBuff = Prepare()

        inner class Prepare : ArtifactBuff() {
            override fun act(): Boolean {
                if (cooldown > 0) --cooldown
                else {
                    Item.curUser = Dungeon.hero
                    for (i in 0 until Dungeon.hero.visibleEnemies()) {
                        val enemy = Dungeon.hero.visibleEnemy(i)
                        if (enemy.isAlive && Dungeon.level.distance(enemy.pos, Dungeon.hero.pos) <= zapRange()) {
                            zapDeath(enemy.pos)
                            break
                        }
                    }
                }

                updateQuickslot()
                spend(Actor.TICK)
                return true
            }
        }

        override fun doPickUp(hero: Hero): Boolean {
            val picked = super.doPickUp(hero)

            if (picked && hero.belongings.getItem(Right::class.java) != null)
                Combin(hero)
            return picked
        }
    }

    class Right : Artifact() {
        init {
            image = ItemSpriteSheet.ARTIFACT_EYEBALL
            levelCap = 10

            cooldown = 0
            defaultAction = GAZE
            usesTargeting = true
        }

        override fun actions(hero: Hero): ArrayList<String> {
            val actions = super.actions(hero)
            if (isEquipped(hero) && cooldown <= 0) actions.add(GAZE)
            return actions
        }

        override fun execute(hero: Hero, action: String) {
            super.execute(hero, action)
            if (action == GAZE) {
                if (!isEquipped(hero)) {
                    GLog.i(Messages.get(Artifact::class.java, "need_to_equip"))
                    QuickSlotButton.cancel()
                } else if (cooldown > 0) {
                    GLog.w(Messages.get(this, "no_charge"))
                    QuickSlotButton.cancel()
                } else GameScene.selectCell(charSelector)
            }
        }

        private fun cooldown(): Int = 20 - level()
        private fun duration(): Float = 2f + level() / 2f

        private fun gaze(ch: Char) {
            cooldown = cooldown()

            Item.curUser.sprite.zap(ch.pos)
            Item.curUser.spendAndNext(1f)

            Buff.prolong(ch, Disarm::class.java, duration())
            ch.sprite.emitter().burst(Speck.factory(Speck.LIGHT), 12)

            // todo: audio

            updateQuickslot()
        }

        override fun random(): Item = this.apply {
            cursed = Random.Float() < 0.7f
        }

        override fun desc(): String {
            var desc = M.L(EyeballOfTheElder::class.java, "desc")
            desc += "\n" + M.L(EyeballOfTheElder::class.java, "desc_hint")
            if (isEquipped(Dungeon.hero)) desc += "\n\n" + M.L(this, "desc_right")

            return desc
        }

        override fun passiveBuff(): ArtifactBuff = Gaze()

        override fun doPickUp(hero: Hero): Boolean {
            val picked = super.doPickUp(hero)

            if (picked && hero.belongings.getItem(Left::class.java) != null)
                Combin(hero)
            return picked
        }

        inner class Gaze : ArtifactBuff() {
            override fun act(): Boolean {
                if (cooldown > 0) {
                    --cooldown
                    updateQuickslot()
                }

                for (i in 0 until Dungeon.hero.visibleEnemies()) {
                    val e = Dungeon.hero.visibleEnemy(i)
                    if (e.isAlive) {
                        Buff.prolong(e, Vulnerable::class.java, 1.1f).apply {
                            ratio = if (cursed) 1.4f else 1.25f
                            dmgType = Damage.Type.MAGICAL
                        }
                        e.sprite.emitter().burst(Speck.factory(Speck.LIGHT), 3)
                    }
                }

                spend(Actor.TICK)
                return true
            }
        }

        private val charSelector = object : CellSelector.Listener {
            override fun onSelect(cell: Int?) {
                if (cell != null) {
                    val ch = Actor.findChar(cell)
                    if (ch != null && ch != Item.curUser) gaze(ch)
                }
            }

            override fun prompt(): String = M.L(Right::class.java, "gaze_prompt")
        }
    }

    companion object {
        private const val GAZE = "gaze"

        private fun Combin(hero: Hero) {
            val detach = { it: EquipableItem ->
                val cursed = it.cursed
                if (it.isEquipped(hero)) {
                    it.cursed = false
                    it.doUnequip(hero, false)
                }
                it.cursed = cursed
                it.detachAll(hero.belongings.backpack)
            }

            val left = hero.belongings.getItem(Left::class.java)
            val right = hero.belongings.getItem(Right::class.java)

            detach(left)
            detach(right)

            EyeballOfTheElder().apply {
                level(left.level())
                cursed = left.cursed || right.cursed
                cursedKnown = true
            }.collect()

            GLog.n(M.L(EyeballOfTheElder::class.java, "combined"))
        }
    }
}
