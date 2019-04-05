package com.egoal.darkestpixeldungeon.items.artifacts

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.DungeonTilemap
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.effects.Beam
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.particles.PurpleParticle
import com.egoal.darkestpixeldungeon.effects.particles.ShadowParticle
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Random
import kotlin.math.min

class EyeballOfTheElder : Artifact() {
    init {
        image = ItemSpriteSheet.ARTIFACT_EYEBALL

        levelCap = 10

        random()
        cooldown = 0

        defaultAction = "NONE"
    }

    private var left: Boolean = true // left eye only for now

    override fun random(): Item {
        cursedKnown = true
        cursed = true
        return this
    }

    override fun desc(): String {
        var desc = super.desc()
        if (isEquipped(Dungeon.hero))
            desc += "\n\n" + Messages.get(this, if (left) "desc_left" else "desc_right") +
                    "\n" + Messages.get(this, "desc_hint")

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
            // dont change curse status 
            val isCursed = cursed
            upgrade()
            cursed = isCursed

            Item.curUser.takeDamage(Damage(2 + Random.IntRange(0, level()), this, Item.curUser).type(Damage.Type.MENTAL))
            CellEmitter.get(Item.curUser.pos).burst(ShadowParticle.CURSE, 5)
            Sample.INSTANCE.play(Assets.SND_BURNING)

            GLog.p(Messages.get(this, "levelup"))
        }

        val beam = Ballistica(Item.curUser.pos, pos, Ballistica.WONT_STOP)
        val dist = min(zapRange(), beam.dist)

        val dstCell = beam.path.get(dist)
        Item.curUser.sprite.parent.add(Beam.DeathRay(
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
                it.takeDamage(it.defendDamage(dmg))
                it.sprite.flash()
                CellEmitter.center(cell).burst(PurpleParticle.BURST, Random.IntRange(2, 3))
            }
        }

//        if (lightAffected) {
//            Dungeon.level.updateLightMap()
//            Dungeon.observe()
//        } else 
        if (terrainAffected)
            Dungeon.observe()


        Item.curUser.sprite.turnTo(Item.curUser.pos, pos)
        Item.curUser.interrupt()
        GLog.n(Messages.get(this, "on-zap"))
    }

    override fun passiveBuff(): ArtifactBuff = Prepare()

    inner class Prepare : ArtifactBuff() {
        override fun act(): Boolean {
            if (cooldown > 0)
                --cooldown

            if (cooldown <= 0 && left) {
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
}
