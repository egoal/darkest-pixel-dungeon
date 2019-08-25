package com.egoal.darkestpixeldungeon.actors.mobs.npcs

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.hero.perks.IntendedTransportation
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.effects.Wound
import com.egoal.darkestpixeldungeon.items.Heap
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.items.books.textbook.YvettesDiary
import com.egoal.darkestpixeldungeon.items.food.Food
import com.egoal.darkestpixeldungeon.items.helmets.RangerHat
import com.egoal.darkestpixeldungeon.items.keys.IronKey
import com.egoal.darkestpixeldungeon.items.potions.Potion
import com.egoal.darkestpixeldungeon.items.potions.PotionOfHealing
import com.egoal.darkestpixeldungeon.items.scrolls.Scroll
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfTeleportation
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.sprites.MobSprite
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndBag
import com.egoal.darkestpixeldungeon.windows.WndOptions
import com.egoal.darkestpixeldungeon.windows.WndQuest
import com.watabou.noosa.TextureFilm
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle
import com.watabou.utils.Random

class Yvette : NPC() {

    init {
        spriteClass = Sprite::class.java

        properties.add(Property.IMMOVABLE)
    }

    private var questGiven = false
    private var seenBefore = false // no store
    private var foodGotten = false

    override fun interact(): Boolean {
        sprite.turnTo(pos, Dungeon.hero.pos)

        if (Quest.Completed) {
            // safe on the land
            tell(Messages.get(this, "see-again"))
            return false
        }

        if (questGiven) {
            GameScene.show(object : WndOptions(Sprite(), name, Messages.get(this, "reminder"),
                    Messages.get(this, "opt-ok"), Messages.get(this, "opt-betray")) {
                override fun onSelect(index: Int) {
                    onAnswered(index)
                }
            })
        } else {
            questGiven = true
            GameScene.show(object : WndOptions(Sprite(), name, Messages.get(this, "task"),
                    Messages.get(this, "opt-onit"), Messages.get(this, "opt-betray")) {
                override fun onSelect(index: Int) {
                    onAnswered(index)
                }
            })
        }

        return false
    }

    private fun onAnswered(index: Int) {
        if (index == 0) {
            ScrollOfTeleportation().let {
                if (!it.isKnown) {
                    it.setKnown()
                    GLog.w(Messages.get(Yvette::class.java, "scroll-identified", this@Yvette.name))
                }
            }

            GameScene.selectItem(selectorGiveItem, Messages.get(this, "give-item"), WndBag.Filter {
                it is Food || it is Scroll
            })
        } else {
            // kill her? you dirty adventurer!
            kill()
        }
    }

    override fun act(): Boolean {
        if (!Quest.Completed && !questGiven && Dungeon.visible[pos]) {
            if (!seenBefore)
                yell(Messages.get(this, "hey", Dungeon.hero.givenName()))
            seenBefore = true
        }

        throwItem()
        return super.act()
    }

    private val selectorGiveItem = WndBag.Listener {
        it.let { item ->
            if (item is Food) {
                item.detach(Dungeon.hero.belongings.backpack)
                onFoodGotten(item)
            } else if (item is Scroll) {
                item.detach(Dungeon.hero.belongings.backpack)
                onScrollGotten(item)
            }
        }
    }

    private fun onFoodGotten(food: Food) {
        if (foodGotten) {
            tell(Messages.get(this, "full"))
            if (!food.collect())
                Dungeon.level.drop(food, Dungeon.hero.pos)
        } else {
            foodGotten = true
            tell(Messages.get(this, "thanks-for-food"))
        }
    }

    private fun onScrollGotten(scroll: Scroll) {
        if (scroll is ScrollOfTeleportation) {
            val content = Messages.get(this, "thanks-for-scroll") +
                    if (foodGotten) "\n\n" + Messages.get(this, "teach-teleportation") else ""

            GameScene.show(object : WndQuest(this, content) {
                override fun onBackPressed() {
                    super.onBackPressed()
                    leave()
                }
            })
        } else {
            tell(Messages.get(this, "not-teleportation"))
            if (!scroll.collect())
                Dungeon.level.drop(scroll, Dungeon.hero.pos)
        }
    }

    private fun leave() {
        Quest.Completed = true

        if (foodGotten) {
            Dungeon.hero.heroPerk.add(IntendedTransportation())
            GLog.p(Messages.get(this, "taught", name))
        }

        // ScrollOfTeleportation.appear(this, -1) // move outside
        GLog.i(Messages.get(this, "disappear", name))

        Dungeon.hero.recoverSanity(Random.Float(4f, 10f))

        if (Random.Int(2) == 0) {
            var potion: Potion
            do {
                potion = Generator.POTION.generate() as Potion
            } while (potion is PotionOfHealing)
            Dungeon.level.drop(potion, pos)
        } else {
            var scroll: Scroll
            do {
                scroll = Generator.SCROLL.generate() as Scroll
            } while (scroll is ScrollOfTeleportation)
            Dungeon.level.drop(scroll, pos)
        }

        if (Random.Int(3) == 0)
            Dungeon.level.drop(Random.oneOf(Generator.ARTIFACT, Generator.RING).generate().apply {
                cursed = false
                identify()
            }, pos)
        else
            Dungeon.level.drop(RangerHat().identify(), pos)

        destroy()
        (sprite as Sprite).leave()
    }

    private fun kill() {
        yell(Messages.get(this, "you-bastard"))
        GLog.w(Messages.get(this, "killed", name))

        Dungeon.hero.takeDamage(Damage(Random.Int(4, 10), Dungeon.hero, Dungeon.hero).type(Damage.Type.MENTAL))

        Dungeon.level.drop(Bow(), pos)
        Dungeon.level.drop(IronKey(20), pos)
        Dungeon.level.drop(RangerHat().identify(), pos)
        Dungeon.level.drop(YvettesDiary(), pos)

        Wound.hit(pos)
        Sample.INSTANCE.play(Assets.SND_CRITICAL)
        destroy()
        sprite.die()
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(STR_QUEST_GIVEN, questGiven)
        bundle.put(STR_FOOD_GOTTEN, foodGotten)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        questGiven = bundle.getBoolean(STR_QUEST_GIVEN)
        foodGotten = bundle.getBoolean(STR_FOOD_GOTTEN)
    }

    // unbreakable
    override fun reset() = true // no removed on reset

    override fun defenseSkill(enemy: Char) = 1000

    override fun takeDamage(dmg: Damage) = 0

    override fun add(buff: Buff) {}

    companion object {
        private const val STR_QUEST_GIVEN = "quest-given"
        private const val STR_FOOD_GOTTEN = "food-gotten"

        private const val STR_QUEST_NODE = "yvette"
        private const val STR_QUEST_COMPLETED = "completed"
        private const val STR_QUEST_SPAWNED = "spawned"

        fun CreateSkeletonHeap(): Heap = Heap().apply {
            type = Heap.Type.SKELETON
            drop(IronKey(20))
            drop(YvettesDiary())
            drop(Bow())

        }
    }

    object Quest {
        var Completed = false
        var Spawned = false

        fun StoreInBundle(bundle: Bundle) {
            bundle.put(STR_QUEST_NODE, Bundle().apply {
                put(STR_QUEST_COMPLETED, Completed)
                put(STR_QUEST_SPAWNED, Spawned)
            })
        }

        fun RestoreFromBundle(bundle: Bundle) {
            val node = bundle.getBundle(STR_QUEST_NODE)
            if (node == null)
                Reset()
            else {
                Completed = node.getBoolean(STR_QUEST_COMPLETED)
                Spawned = node.getBoolean(STR_QUEST_SPAWNED)
            }
        }

        fun Reset() {
            Completed = false
            Spawned = false
        }
    }

    // sprite class
    class Sprite : MobSprite() {
        init {
            texture(Assets.YVETTE)

            // set animations
            val frames = TextureFilm(texture, 10, 14)
            idle = Animation(1, true)
            idle.frames(frames, 0, 1)

            run = Animation(20, true)
            run.frames(frames, 0)

            die = Animation(20, true)
            die.frames(frames, 0)

            play(idle)
        }

        fun leave() {
            die()

            ch.sprite.emitter().start(Speck.factory(Speck.LIGHT), 0.2f, 3)
            Sample.INSTANCE.play(Assets.SND_TELEPORT)
        }
    }

    class Bow : Item() {
        init {
            image = ItemSpriteSheet.RANGER_BOW
        }

        override fun isIdentified(): Boolean = true

        override fun isUpgradable(): Boolean = false

        override fun price(): Int = 100
    }
}