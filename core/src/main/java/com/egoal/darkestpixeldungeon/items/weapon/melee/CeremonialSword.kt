package com.egoal.darkestpixeldungeon.items.weapon.melee

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Bleeding
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.Wound
import com.egoal.darkestpixeldungeon.effects.particles.ShadowParticle
import com.egoal.darkestpixeldungeon.items.weapon.enchantments.BloodCoil
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndOptions
import com.watabou.noosa.audio.Sample

class CeremonialSword : MeleeWeapon() {
    init {
        image = ItemSpriteSheet.CEREMONIAL_SWORD

        tier = 2
        DLY = 1.25f

        defaultAction = AC_USE
    }

    override fun STRReq(lvl: Int): Int = super.STRReq(lvl) + 1

    // 15+ 3x -> 18+ 3.5x
    override fun max(lvl: Int): Int = 18 + lvl * 7 / 2

    override fun actions(hero: Hero): ArrayList<String> = super.actions(hero).apply { add(AC_USE) }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)

        if (action == AC_USE) {
            if (prickValue(hero) > hero.HP) {
                WndOptions.Confirm(ItemSprite(this), M.L(this, "name"), M.L(this, "prick_warn")) {
                    prick(hero)
                }
            } else prick(hero)
        }
    }

    private fun prickValue(hero: Hero): Int = hero.HT / 5 + hero.HP / 10
    private fun bleedValue(hero: Hero): Int = hero.HP / 10

    private fun prick(hero: Hero) {
        val dmg = Damage(prickValue(hero), hero, hero).addFeature(Damage.Feature.PURE)

        hero.sprite.operate(hero.pos)
        hero.spend(TIME_TO_USE)
        hero.busy()

        Wound.hit(hero.pos)

        GLog.w(M.L(this, "on_prick"))
        if (dmg.value <= 0) dmg.value = 1
        else {
            Sample.INSTANCE.play(Assets.SND_CURSED)
            hero.sprite.emitter().burst(ShadowParticle.CURSE, 4 + dmg.value / 10)
        }

        hero.takeDamage(dmg)

        if (!hero.isAlive) {
            Dungeon.fail(javaClass)
            GLog.n(M.L(this, "on_death"))
        } else {
            Buff.affect(hero, Bleeding::class.java).set(bleedValue(hero))
            enchant(BloodCoil::class.java, 10f)
        }
    }

    companion object {
        private const val AC_USE = "use"
        private const val TIME_TO_USE = 1f
    }
}