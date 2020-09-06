package com.egoal.darkestpixeldungeon.actors.mobs.npcs

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.Journal
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Pressure
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.HeroLines
import com.egoal.darkestpixeldungeon.actors.mobs.DevilGhost
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.particles.ShadowParticle
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.armor.Armor
import com.egoal.darkestpixeldungeon.items.artifacts.ChaliceOfBlood
import com.egoal.darkestpixeldungeon.items.artifacts.GoddessRadiance
import com.egoal.darkestpixeldungeon.items.artifacts.UrnOfShadow
import com.egoal.darkestpixeldungeon.items.unclassified.UnholyBlood
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.items.weapon.enchantments.Holy
import com.egoal.darkestpixeldungeon.items.weapon.enchantments.Vampiric
import com.egoal.darkestpixeldungeon.items.weapon.melee.MeleeWeapon
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.sprites.MobSprite
import com.egoal.darkestpixeldungeon.ui.StatusPane
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndDialogue
import com.watabou.noosa.TextureFilm
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle
import com.watabou.utils.PathFinder
import com.watabou.utils.Random
import kotlin.math.min

class Statuary : NPC.Unbreakable() {
    enum class Type(val title: String) {
        ANGEL("angel"), DEVIL("devil"), MONSTER("monster"),
    }

    var type = Type.ANGEL
        set(value) {
            field = value
            name = M.L(this, "name_${field.title}")
        }

    private var isActive = true
    private var gold = 0

    init {
        spriteClass = Sprite::class.java
        isLiving = false
        properties.add(Property.IMMOVABLE)
    }

    fun random() {
        val idx = Random.chances(spawnChance)
        type = when (idx) {
            0 -> Type.ANGEL
            1 -> Type.DEVIL
            else -> Type.MONSTER
        }

        spawnChance[idx] /= 3f
    }

    override fun sprite(): CharSprite = Sprite().apply { setType(type) }

    override fun description(): String = M.L(this, "desc_${type.title}")

    override fun interact(): Boolean {
        if (!isActive) return false

        Journal.add(name)
        WndDialogue.Show(this, description(), M.L(this, "agree_${type.title}"), M.L(this, "disagree_${type.title}")) {
            Dungeon.hero.spend(1f)

            val agree = it == 0
            isActive = !when (type) {
                Type.ANGEL -> answerAngle(Dungeon.hero, agree)
                Type.DEVIL -> answerDevil(Dungeon.hero, agree)
                Type.MONSTER -> answerMonster(Dungeon.hero, agree)
            }

            if (!isActive) Journal.remove(name)
        }

        return false
    }

    private fun answerAngle(hero: Hero, agree: Boolean): Boolean {
        if (agree) { // pray
            val dice = Random.Int(10)

            if (dice == 0) { // unholy
                if (hero.pressure.level == Pressure.Level.NERVOUS || hero.pressure.level == Pressure.Level.COLLAPSE)
                    hero.sayShort(HeroLines.MY_RETRIBUTION)
                hero.takeDamage(Damage(Random.Int(5, 15), this, hero).type(Damage.Type.MENTAL))
                GLog.n(M.L(this, "unholy"))
            } else {
                // recover 30% pressure
                val rp = hero.pressure.pressure * 0.3f
                hero.recoverSanity(rp)
                GLog.h(M.L(this, "holy"))

                // shield if recover too less
                if (rp < 15f) hero.SHLD += Random.Int(15, hero.HT / 3)

                // enchant
                if (dice == 8 || dice == 9) {
                    if (hero.belongings.weapon is Weapon) {
                        val w = hero.belongings.weapon as Weapon
                        w.enchant(Holy())

                        GLog.h(M.L(this, "infuse"))
                    }
                } else if (dice == 1) {
                    if (GoddessRadiance().identify().collect()) {
                        hero.spendAndNext(3f)
                        GLog.p(M.L(this, "radiance"))
                    }
                }
            }
        } else { // blasphemy
            hero.busy()
            hero.sprite.operate(hero.pos)
            GLog.i(M.L(this, "blasphemy"))

            // curse an item
            val equipToCurse = hero.belongings.equippedItems().filter { it != null && !it.cursed }
            if (equipToCurse.isEmpty()) { // nothing to curse, up pressure
                hero.takeDamage(Damage(Random.Int(3, 12), this, hero).type(Damage.Type.MENTAL))
            } else {
                val item = equipToCurse.random() as Item
                item.cursed = true
                item.cursedKnown = true

                if (item is Weapon) item.enchantment = Weapon.Enchantment.randomCurse()
                else if (item is Armor) item.glyph = Armor.Glyph.randomCurse()
            }

            if (Dungeon.visible[pos]) {
                CellEmitter.get(pos).burst(ShadowParticle.UP, 5)
                Sample.INSTANCE.play(Assets.SND_CURSED)
            }

            // drop blood
            Dungeon.level.drop(UnholyBlood(), hero.pos).sprite.drop()
        }

        return true
    }

    private fun answerDevil(hero: Hero, agree: Boolean): Boolean {
        if (agree) { // sacrifice
            if (hero.HP < hero.HT / 2) {
                GLog.i(M.L(this, "lowhp"))
                return false
            }

            hero.takeDamage(Damage(hero.HT / 5, this, hero))
            hero.busy()
            hero.sprite.operate(hero.pos)
            GLog.h(M.L(this, "sacrifice"))

            if (Random.Int(20) == 0) GLog.i(M.L(this, "nothing"))
            else {
                // deserve more
                val reqBlood = Random.Float() < 0.75f
                var reqValue: Int
                if (reqBlood) {
                    reqValue = hero.HT * Random.Float(0.25f, 0.5f).toInt()
                    if (reqValue >= hero.HP) reqValue = hero.HP - 1

                    hero.takeDamage(Damage(reqValue, this, hero).addFeature(Damage.Feature.PURE))
                    GLog.h(M.L(this, "more_blood"))
                } else {
                    reqValue = Random.Int(18, 30)
                    hero.takeDamage(Damage(reqValue, this, hero).type(Damage.Type.MENTAL).addFeature(Damage.Feature.PURE))
                    GLog.h(M.L(this, "more_sanity"))
                }

                // reward
                val uos = hero.belongings.getItem(UrnOfShadow::class.java)
                if (uos != null && uos.level() <= 8) {
                    uos.upgrade(2)
                    return true
                }

                if (reqBlood) {
                    hero.HT += Random.Int(8, 15)
                    GLog.i(M.L(this, "upht"))

                    if (Random.Int(5) == 0) {
                        (hero.belongings.weapon as Weapon?)?.enchant(Vampiric())
                        GLog.h(M.L(this, "infuse"))
                    }
                } else {
                    if (!Dungeon.limitedDrops.chaliceOfBlood.dropped() && Random.Int(4) == 0) {
                        Dungeon.limitedDrops.chaliceOfBlood.drop()
                        Dungeon.level.drop(ChaliceOfBlood().random(), hero.pos).sprite.drop()
                        hero.sprite.emitter().start(ShadowParticle.UP, 0.05f, 10)
                        Sample.INSTANCE.play(Assets.SND_BURNING)
                    } else {
                        hero.earnExp(hero.maxExp())
                        GLog.i(M.L(this, "upexp"))
                    }
                }
            }
        } else { // blasphemy
            hero.busy()
            hero.sprite.operate(hero.pos)
            GLog.i(M.L(this, "blasphemy"))

            var count = if (Random.Int(4) == 0) 2 else 1
            for (i in PathFinder.NEIGHBOURS8.map { it + pos }
                    .filter { Level.passable[it] && Actor.findChar(it) == null }.shuffled()) {
                count--
                DevilGhost.SpawnAt(i)
                if (count <= 0) break
            }
        }

        return true
    }

    private fun answerMonster(hero: Hero, agree: Boolean): Boolean {
        if (agree) {
            val supply = min(100, Dungeon.gold)
            Dungeon.gold -= supply
            Sample.INSTANCE.play(Assets.SND_GOLD)
            GLog.i(M.L(this, "supply", supply))

            gold += supply
            if (supply < 100) GLog.i(M.L(this, "nothing", supply))
            else {
                val p = (gold - 100) * .6f / 400f + .3f // 100: 0.3 -> 500: 0.9
                if (Random.Float() < p) {
                    val dice = Random.Float()
                    val item = when {
                        dice < 0.3f -> Generator.WEAPON.generate()
                        dice < 0.5f -> Generator.ARMOR.generate()
                        else -> Generator.generate()
                    }
                    Dungeon.level.drop(item, hero.pos).sprite.drop()
                }
            }

            return gold >= 500
        } else { // blasphemy, lost memory
            Dungeon.level.mapped.fill(false)
            Dungeon.level.visited.fill(false)
            StatusPane.needsCompassUpdate = true

            Dungeon.observe()
            GameScene.updateFog()

            val item = if (Random.Float() < 0.4f) Generator.WEAPON.generate() else Generator.ARMOR.generate()
            if (item is Armor) {
                item.inscribe(Armor.Glyph.randomCurse())
                item.cursed = true
            } else if (item is MeleeWeapon) {
                item.enchant(Weapon.Enchantment.randomCurse())
                item.cursed = true
            }

            item.cursedKnown = true
            if (item.isUpgradable) {
                item.upgrade(if (Random.Int(5) == 1) 2 else 1)
            }

            Dungeon.level.drop(item, hero.pos).sprite.drop()

            GameScene.flash(0x5a7878)
            Sample.INSTANCE.play(Assets.SND_BLAST)

            GLog.i(M.L(this, "blasphemy"))
            GLog.w(M.L(this, "greedy"))

            return true
        }
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(TYPE, type.toString())
        bundle.put(ACTIVE, isActive)
        bundle.put(GOLD, gold)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        val value = bundle.getString(TYPE)
        type = if (value.isNotEmpty()) Type.valueOf(value) else Type.ANGEL
        isActive = bundle.getBoolean(ACTIVE)
        gold = bundle.getInt(GOLD)
    }

    companion object {
        private const val TYPE = "type"
        private const val ACTIVE = "active"
        private const val GOLD = "gold"

        private val spawnChance = floatArrayOf(100f, 100f, 100f)

        private const val NODE = "statuary"
        private const val SPAWN_CHANCE = "spawnchance"

        fun Reset() {
            spawnChance.fill(100f)
        }

        fun Save(bundle: Bundle) {
            val node = Bundle()
            node.put(SPAWN_CHANCE, spawnChance)
            bundle.put(NODE, node)
        }

        fun Load(bundle: Bundle) {
            val node = bundle.getBundle(NODE)
            if (!node.isNull) node.getFloatArray(SPAWN_CHANCE).copyInto(spawnChance)
        }
    }

    class Sprite : MobSprite() {
        private val idleAngel: Animation
        private val idleDevil: Animation
        private val idleMonster: Animation

        init {
            texture(Assets.STATUARY)

            val frames = TextureFilm(texture, 14, 16)
            idle = Animation(10, true)
            idle.frames(frames, 0)

            run = Animation(20, true)
            run.frames(frames, 0)

            die = Animation(20, false)
            die.frames(frames, 0)

            play(idle)

            idleAngel = idle
            idleDevil = Animation(10, true)
            idleDevil.frames(frames, 1)
            idleMonster = Animation(10, true)
            idleMonster.frames(frames, 2)
        }

        fun setType(type: Type) {
            val anim = when (type) {
                Type.ANGEL -> idleAngel
                Type.DEVIL -> idleDevil
                Type.MONSTER -> idleMonster
            }
            play(anim)
        }
    }
}