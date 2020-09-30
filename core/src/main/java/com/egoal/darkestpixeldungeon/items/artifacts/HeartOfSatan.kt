package com.egoal.darkestpixeldungeon.items.artifacts

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.particles.ShadowParticle
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.plants.Earthroot
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndOptions
import com.watabou.noosa.audio.Sample
import java.util.ArrayList

// check hero::regenerationSpeed
class HeartOfSatan : Artifact() {
    init {
        image = ItemSpriteSheet.HEART_OF_SATAN
        levelCap = 10
    }

    override fun actions(hero: Hero): ArrayList<String> {
        val actions = super.actions(hero)
        if (isEquipped(hero) && level() < levelCap && !cursed)
            actions.add(AC_PRICK)
        return actions
    }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)

        if (action == AC_PRICK) {
            if (prickValue() > hero.HP * 3 / 4) {
                // warning
                WndOptions.Confirm(ItemSprite(this), M.L(this, "name"), M.L(this, "prick_warn")) {
                    prick(hero)
                }
            } else
                prick(hero)
        }
    }

    private fun prickValue(): Int = 3 * level() * level()

    private fun prick(hero: Hero) {
        val dmg = Damage(prickValue(), hero, hero)

        hero.buff(Earthroot.Armor::class.java)?.procTakenDamage(dmg)

        hero.defendDamage(dmg)
        hero.sprite.operate(hero.pos)
        hero.spend(3f)
        hero.busy()

        GLog.w(Messages.get(this, "onprick"))
        if (dmg.value <= 0) dmg.value = 1
        else {
            Sample.INSTANCE.play(Assets.SND_CURSED)
            hero.sprite.emitter().burst(ShadowParticle.CURSE, 4 + dmg.value / 10)
        }

        hero.takeDamage(dmg)

        if (!hero.isAlive) {
            Dungeon.fail(javaClass)
            GLog.n(Messages.get(this, "ondeath"))
        } else upgrade()
    }

    // no sprite update~

    override fun passiveBuff(): ArtifactBuff = Regeneration()

    override fun desc(): String {
        var desc = super.desc()
        if (isEquipped(Dungeon.hero)) {
            if (cursed)
                desc += "\n\n" + Messages.get(this, "desc_cursed")
            else if (level() > 0)
                desc += "\n\n" + Messages.get(this, "desc_hint")
        }

        return desc
    }

    inner class Regeneration : Artifact.ArtifactBuff()

    companion object {
        private const val AC_PRICK = "prick"
    }
}