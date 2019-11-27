package com.egoal.darkestpixeldungeon.items.weapon.missiles

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Bleeding
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.particles.ShadowParticle
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.plants.Earthroot
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndOptions
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Random
import kotlin.math.min

class CeremonialDagger(number: Int = 1) : MissileWeapon(2) {
    init {
        image = ItemSpriteSheet.CEREMONIAL_DAGGER

        quantity = number
    }

    override fun min(lvl: Int): Int = 2 + tier
    override fun max(lvl: Int): Int = 4 + tier * 3

    override fun random(): Item = this.apply {
        quantity = if (Random.Float() < 0.25f) 2 else 1
    }

    override fun actions(hero: Hero): ArrayList<String> = super.actions(hero).apply { add(AC_USE) }

    override fun desc(): String {
        return super.desc() + "\n\n" + M.L(this, "used_times", Dungeon.limitedDrops.ceremonialDaggerUsed.count)
    }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)

        if (action == AC_USE) {
            if (prickValue(hero) > hero.HP * 3 / 4) {
                GameScene.show(object : WndOptions(ItemSprite(this), M.L(this, "name"),
                        M.L(this, "prick_warn"),
                        M.L(this, "yes"), M.L(this, "no")) {
                    override fun onSelect(index: Int) {
                        if (index == 0) prick(hero)
                    }
                })
            } else prick(hero)
        }
    }

    override fun price(): Int = 50 * quantity

    private fun prickValue(hero: Hero): Int = hero.HT / 4 + hero.HT / 10 * Dungeon.limitedDrops.ceremonialDaggerUsed.count
    private fun bleedValue(hero: Hero): Int = hero.HT / 8 + hero.HT / 20 * Dungeon.limitedDrops.ceremonialDaggerUsed.count

    private fun prick(hero: Hero) {
        detach(hero.belongings.backpack)

        val dmg = Damage(prickValue(hero), this, hero)
        hero.buff(Earthroot.Armor::class.java)?.procTakenDamage(dmg)

        hero.defendDamage(dmg)
        hero.sprite.operate(hero.pos)
        hero.spend(TIME_TO_USE)
        hero.busy()

        GLog.w(M.L(this, "on_prick"))
        if (dmg.value <= 0) dmg.value = 1
        else {
            Sample.INSTANCE.play(Assets.SND_CURSED)
            hero.sprite.emitter().burst(ShadowParticle.CURSE, 4 + dmg.value / 10)
        }

        hero.takeDamage(dmg)
        Dungeon.limitedDrops.ceremonialDaggerUsed.count++

        if (!hero.isAlive) {
            Dungeon.fail(javaClass)
            GLog.n(M.L(this, "on_death"))
        } else {
            Buff.affect(hero, Bleeding::class.java).set(bleedValue(hero))
            hero.recoverSanity(min(Random.Float(20f, hero.pressure.pressure * 0.4f), 30f))
        }
    }

    companion object {
        private const val AC_USE = "use"
        private const val TIME_TO_USE = 2f
    }
}