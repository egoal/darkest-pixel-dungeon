package com.egoal.darkestpixeldungeon.actors.mobs

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Bleeding
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.Pushing
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfPsionicBlast
import com.egoal.darkestpixeldungeon.items.unclassified.Gold
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.MimicSprite
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle
import com.watabou.utils.PathFinder
import com.watabou.utils.Random
import java.util.HashSet
import kotlin.math.max

class Mimic : Mob() {
    init {
        spriteClass = MimicSprite::class.java

        properties.add(Property.DEMONIC)
    }

    private var level = 0
    var items = mutableListOf<Item>()

    override fun giveDamage(target: Char): Damage = Damage(Random.NormalIntRange(HT / 10, HT / 4), this, target)

    override fun attackSkill(target: Char): Float = 9f + level

    override fun die(cause: Any?) {
        super.die(cause)

        for (item in items)
            Dungeon.level.drop(item, pos).sprite.drop()
    }

    override fun reset(): Boolean {
        state = WANDERING
        return true
    }

    fun adjustStatus(level: Int) {
        this.level = level

        HT = (1 + level) * 6
        HP = HT
        EXP = 2 + 2 * (level - 1) / 5
        defSkill = attackSkill(this) / 2

        enemySeen = true
    }

    override fun immunizedBuffs(): HashSet<Class<*>> = IMMUNITIES

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(ITEMS, items)
        bundle.put(LEVEL, level)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        for (i in bundle.getCollection(ITEMS))
            items.add(i as Item)
        adjustStatus(bundle.getInt(LEVEL))
        super.restoreFromBundle(bundle)
    }

    companion object {
        private const val LEVEL = "level"
        private const val ITEMS = "items"

        private val IMMUNITIES = hashSetOf<Class<*>>(ScrollOfPsionicBlast::class.java, Bleeding::class.java)

        fun SpawnAt(pos: Int, items: List<Item>): Mimic? {
            Actor.findChar(pos)?.let { ch ->
                // somebody is there...
                val candidates = PathFinder.NEIGHBOURS8.map { it + pos }.filter {
                    (Level.passable[it] || Level.avoid[it]) && Actor.findChar(it) == null
                }
                if (candidates.isEmpty()) return null

                val newPos = candidates.random()
                Actor.addDelayed(Pushing(ch, ch.pos, newPos), -1f)

                ch.pos = newPos
                //fixme
                if (ch is Mob) Dungeon.level.mobPress(ch)
                else Dungeon.level.press(newPos, ch)
            }

            val m = Mimic().apply {
                adjustStatus(Dungeon.depth)
                this.pos = pos
                state = HUNTING
            }
            m.items.addAll(items)
            GameScene.add(m, 1f)
            m.sprite.turnTo(pos, Dungeon.hero.pos)

            if (Dungeon.visible[m.pos]) {
                CellEmitter.get(pos).burst(Speck.factory(Speck.STAR), 10)
                Sample.INSTANCE.play(Assets.SND_MIMIC)

                // suprise!
                if (Dungeon.level.adjacent(pos, Dungeon.hero.pos))
                    Dungeon.hero.takeDamage(Damage(max(Random.Int(Dungeon.depth / 2), 4), m, Dungeon.hero).type(Damage.Type.MENTAL))
            }

            // items
            val item = when (Random.Int(5)) {
                in 0..1 -> Gold().random()
                2 -> Generator.ARMOR.generate()
                3 -> Generator.WEAPON.generate()
                else -> Generator.RING.generate() // 4
            }
            m.items.add(item.identify())

            return m
        }
    }
}