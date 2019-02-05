package com.egoal.darkestpixeldungeon.items.artifacts

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.buffs.LockedFloor
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.mobs.Wraith
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.GhostHero
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.effects.particles.ShaftParticle
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle
import com.watabou.utils.PathFinder
import com.watabou.utils.Random
import java.util.ArrayList
import kotlin.math.max

class DriedRose : Artifact() {
    init {
        image = ItemSpriteSheet.ARTIFACT_ROSE1

        levelCap = 10
        chargeCap = 100
        charge = chargeCap

        defaultAction = AC_SUMMON

        // statics 
        TalkedTo = false
        FirstSummon = false
        Spawned = false
    }

    var droppedPetals = 0

    override fun actions(hero: Hero): ArrayList<String> {
        val actions = super.actions(hero)
        if (isEquipped(hero) && charge == chargeCap && !cursed)
            actions.add(AC_SUMMON)

        return actions
    }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)

        if (action == AC_SUMMON) {
            if (Spawned) GLog.i(Messages.get(this, "spawned"))
            else if (!isEquipped(hero)) GLog.i(Messages.get(Artifact::class.java, "need_to_equip"))
            else if (charge != chargeCap) GLog.i(Messages.get(this, "no_charge"))
            else if (cursed) GLog.i(Messages.get(this, "cursed"))
            else {
                val avpos = PathFinder.NEIGHBOURS8.map { hero.pos + it }.filter {
                    Actor.findChar(it) == null && (Level.passable[it] || Level.avoid[it])
                }

                if (avpos.isEmpty()) {
                    GLog.i(Messages.get(this, "no_space"))
                } else {
                    val ghost = GhostHero(level()).apply { pos = Random.element(avpos) }

                    GameScene.add(ghost, 1f)
                    CellEmitter.get(ghost.pos).start(ShaftParticle.FACTORY, 0.3f, 4)
                    CellEmitter.get(ghost.pos).start(Speck.factory(Speck.LIGHT), 0.2f, 3)

                    hero.spend(1f)
                    hero.busy()
                    hero.sprite.operate(hero.pos)

                    if (!FirstSummon) {
                        FirstSummon = true
                        ghost.yell(Messages.get(GhostHero::class.java, "hello", Dungeon.hero.givenName()))
                        Sample.INSTANCE.play(Assets.SND_GHOST)
                    } else
                        ghost.saySpawned()

                    Spawned = true
                    charge = 0
                    updateQuickslot()
                }
            }
        }
    }

    override fun desc(): String {
        var desc = super.desc()

        if (isEquipped(Dungeon.hero))
            if (!cursed) {
                if (level() < levelCap) desc += "\n\n" + Messages.get(this, "desc_hint")
            } else desc += "\n\n" + Messages.get(this, "desc_cursed")

        return desc
    }

    override fun passiveBuff(): ArtifactBuff = Recharge()

    override fun upgrade(): Item {
        if (level() > 9) image = ItemSpriteSheet.ARTIFACT_ROSE3
        else if (level() > 4) image = ItemSpriteSheet.ARTIFACT_ROSE2

        droppedPetals = max(level(), droppedPetals)

        return super.upgrade()
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)

        with(bundle) {
            put(TALKED_TO, TalkedTo)
            put(FIRST_SUMMON, FirstSummon)
            put(SPAWNED, Spawned)
            put(PETALS, droppedPetals)
        }
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)

        TalkedTo = bundle.getBoolean(TALKED_TO)
        FirstSummon = bundle.getBoolean(FIRST_SUMMON)
        Spawned = bundle.getBoolean(SPAWNED)
        droppedPetals = bundle.getInt(PETALS)
    }

    inner class Recharge : ArtifactBuff() {
        override fun act(): Boolean {
            val lock = target.buff(LockedFloor::class.java)
            if (!Spawned && charge < chargeCap && !cursed && (lock == null || lock.regenOn())) {
                partialCharge += 100f / 400f
                if (partialCharge > 1f) {
                    charge++
                    partialCharge--
                    if (charge == chargeCap) {
                        partialCharge = 0f
                        GLog.p(Messages.get(DriedRose::class.java, "charged"))
                    }
                }
            } else if (cursed && Random.Int(100) == 0) {
                val avpos = PathFinder.NEIGHBOURS8.map { target.pos + it }.filter {
                    Actor.findChar(it) == null && (Level.passable[it] || Level.avoid[it])
                }

                if (!avpos.isEmpty()) {
                    Wraith.spawnAt(Random.element(avpos))
                    Sample.INSTANCE.play(Assets.SND_CURSED)
                }
            }

            updateQuickslot()
            spend(Actor.TICK)

            return true
        }
    }

    companion object {
        private const val AC_SUMMON = "summon"

        var TalkedTo = false
        var FirstSummon = false
        var Spawned = false

        private const val TALKED_TO = "talked-to"
        private const val FIRST_SUMMON = "first-summon"
        private const val SPAWNED = "spawned"
        private const val PETALS = "petals"

        class Petal : Item() {
            init {
                image = ItemSpriteSheet.PETAL
                stackable = true
            }

            override fun doPickUp(hero: Hero): Boolean {
                val rose = hero.belongings.getItem(DriedRose::class.java)
                if (rose == null) {
                    GLog.w(Messages.get(this, "no_rose"))
                    return false
                }

                if (rose.level() >= rose.levelCap) {
                    GLog.i(Messages.get(this, "no_room"))
                    hero.spendAndNext(Item.TIME_TO_PICK_UP)
                    return true
                } else {
                    rose.upgrade()
                    if (rose.level() == rose.levelCap) GLog.p(Messages.get(this, "maxlevel"))
                    else GLog.i(Messages.get(this, "levelup"))

                    Sample.INSTANCE.play(Assets.SND_DEWDROP)
                    hero.spendAndNext(Item.TIME_TO_PICK_UP)
                    return true
                }
            }
        }
    }
}