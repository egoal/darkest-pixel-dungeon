package com.egoal.darkestpixeldungeon.actors.mobs.npcs

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.perks.Perk
import com.egoal.darkestpixeldungeon.effects.PerkGain
import com.egoal.darkestpixeldungeon.effects.Wound
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.MobSprite
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndDialogue
import com.egoal.darkestpixeldungeon.windows.WndSelectPerk
import com.watabou.noosa.TextureFilm
import com.watabou.utils.Bundle
import com.watabou.utils.Random
import kotlin.math.max
import kotlin.math.min

class ArchDemon : NPC.Unbreakable() {
    init {
        spriteClass = Sprite::class.java

        properties.add(Property.IMMOVABLE)
    }

    private var dealt = false

    override fun interact(): Boolean {
        if (dealt) return false

        WndDialogue.Show(this, M.L(this, "greetings"), M.L(this, "skillmodify"), M.L(this, "skillup")) { index ->
            if (index == 0) {
                if (Dungeon.hero.heroPerk.perks.isEmpty()) tell(M.L(ArchDemon::class.java, "unqualified"))
                else GameScene.show(object : WndSelectPerk(M.L(ArchDemon::class.java, "select_perk"),
                        Dungeon.hero.heroPerk.perks) {
                    override fun onPerkSelected(perk: Perk) {
                        removePerk(Dungeon.hero, perk)
                    }
                })
            } else if (index == 1) {
                val upgradable = Dungeon.hero.heroPerk.perks.filter { it.upgradable() }
                if (upgradable.isEmpty()) tell(M.L(ArchDemon::class.java, "unqualified"))
                else GameScene.show(object : WndSelectPerk(M.L(ArchDemon::class.java, "select_perk"), upgradable) {
                    override fun onPerkSelected(perk: Perk) {
                        upgradePerk(Dungeon.hero, perk)
                    }
                })
            }
        }

        return false
    }

    private fun removePerk(hero: Hero, perk: Perk) {
        dealt = true
        hero.heroPerk.downgrade(perk)

        WndDialogue.Show(this, M.L(this, "other_deal"), M.L(this, "price_ht", 15), M.L(this, "price_ht", 25), M.L(this, "price_ht", 35), M.L(this, "price_none")) {
            if (it == 3) return@Show

            //todo: if the hero is too weak, the demon shall refuse it
            removeHT(hero, when (it) {
                0 -> 0.15f
                1 -> 0.25f
                else -> 0.35f
            })

            val p = when (it) {
                0 -> 0.5f
                1 -> 0.75f
                else -> 1f
            }
            if (Random.Float() < p) {
                val newPerk = Perk.RandomPositive(hero)
                PerkGain.Show(Dungeon.hero!!, newPerk)
                hero.heroPerk.add(newPerk)
                say(M.L(ArchDemon::class.java, "deal"))
            } else say(M.L(ArchDemon::class.java, "deal_failed"))
        }
    }

    private fun upgradePerk(hero: Hero, perk: Perk) {
        WndDialogue.Show(this, M.L(this, "price"), M.L(this, "price_blood"), M.L(this, "price_perk")) {
            dealt = true
            if (it == 0) {
                if (hero.regeneration > 0.5f && Random.Float() < 0.5f) {
                    hero.regeneration -= max(hero.regeneration* 0.5f, 0.5f)
                    GLog.n(M.L(ArchDemon::class.java, "regeneration"))
                } else if (Random.Float() < 0.3f) {
                    val index = (0 until Damage.Element.ELEMENT_COUNT).maxBy { i -> hero.elementalResistance[i] }!!
                    if (hero.elementalResistance[index] > 0f) {
                        hero.elementalResistance[index] -= 0.3f
                    } else {
                        for (i in 0 until Damage.Element.ELEMENT_COUNT) hero.elementalResistance[i] -= 0.05f
                    }
                    GLog.n(M.L(this, "resistance"))
                } else removeHT(hero, Random.Float(0.15f, 0.25f))
            } else {
                var thePerk: Perk
                do {
                    thePerk = hero.heroPerk.perks.random()
                } while (thePerk.javaClass == perk.javaClass) //todo: do not remove negative perk
                hero.heroPerk.downgrade(thePerk)
            }

            PerkGain.Show(Dungeon.hero!!, perk)
            hero.heroPerk.add(perk)
            say(M.L(ArchDemon::class.java, "deal"))
        }
    }

    private fun removeHT(char: Char, percent: Float) {
        Wound.hit(char.pos)
        char.HT = (char.HT * (1 - percent)).toInt()
        char.HP = min(char.HP, char.HT)
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(DEALT, dealt)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        dealt = bundle.getBoolean(DEALT)
    }

    class Sprite : MobSprite() {
        init {
            texture(Assets.ARCH_DEMON)

            val frames = TextureFilm(texture, 16, 16)
            idle = Animation(1, true)
            idle.frames(frames, 0, 1, 2)

            die = Animation(20, false)
            die.frames(frames, 0)

            run = idle.clone()
            attack = idle.clone()

            play(idle)
        }
    }

    companion object {
        private const val DEALT = "dealt"
    }
}