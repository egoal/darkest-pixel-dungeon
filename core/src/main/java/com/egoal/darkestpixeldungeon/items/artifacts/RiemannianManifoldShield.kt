package com.egoal.darkestpixeldungeon.items.artifacts

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.ResistAny
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.ExpandHalo
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Random

/**
 * Created by 93942 on 9/4/2018.
 */

//* buff used in Hero::resistDamage
class RiemannianManifoldShield : Artifact() {
    init {
        image = ItemSpriteSheet.RIEMANNIAN_SHIELD

        levelCap = 10
        exp = 0
        cooldown = 1

        defaultAction = "NONE" // to put into quick slot
    }

    // called when ResistAny detached
    fun recharge() {
        // curUser is assigned in the execute method!!!
        if (isEquipped(Dungeon.hero)) {
            if (level() <= 5) cooldown = 56 - level() * 4

            if (isFullyUpgraded) {
                Dungeon.level.mobs.filter {
                    Level.fieldOfView[it.pos] && Dungeon.level.distance(it.pos, Dungeon.hero.pos) <= 3
                }.forEach {
                    it.takeDamage(Damage(Random.NormalIntRange(10, it.HT / 5), Dungeon.hero, it).type(Damage.Type.MAGICAL))
                    it.sprite.flash()
                }

                ExpandHalo(8f, 24f, 0xccf1ff).show(Dungeon.hero.sprite, .25f)
                Sample.INSTANCE.play(Assets.SND_SHATTER)
            }
        }
    }

    // more likely to be cursed.
    override fun random(): Item = this.apply { cursed = Random.Float() < 0.7f }

    override fun doUnequip(hero: Hero, collect: Boolean, single: Boolean): Boolean {
        GLog.w(Messages.get(this, "unequipped"))
        recharge()

        return super.doUnequip(hero, collect, single)
    }

    // recharge buff
    override fun passiveBuff(): ArtifactBuff = Recharge()

    inner class Recharge : Artifact.ArtifactBuff() {
        override fun act(): Boolean {
            if (cursed) {
                spend(Actor.TICK)
                return true
            }

            if (--cooldown == 0 && Dungeon.hero.buff(ResistAny::class.java) == null) {
                Buff.affect(Dungeon.hero, ResistAny::class.java).set(1)

                exp += 1
                // check upgrade
                val requireExp = level() * level() / 3 + level() + 1
                if (exp > requireExp && level() < levelCap) {
                    exp -= requireExp
                    upgrade()
                    GLog.p(Messages.get(RiemannianManifoldShield::class.java, "levelup"))
                }
            }
            updateQuickslot()

            spend(Actor.TICK)
            return true
        }
    }

    override fun status(): String? = if (cooldown > 0) "$cooldown" else null

    override fun desc(): String {
        var desc = super.desc()
        if (isFullyUpgraded) desc += "\n" + M.L(this, "desc_max")

        if (isIdentified && cursed)
            desc += "\n\n" + Messages.get(this, "desc_cursed")

        return desc
    }
}
