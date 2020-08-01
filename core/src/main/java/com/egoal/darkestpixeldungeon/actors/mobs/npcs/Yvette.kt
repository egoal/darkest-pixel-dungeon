package com.egoal.darkestpixeldungeon.actors.mobs.npcs

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.hero.perks.IntendedTransportation
import com.egoal.darkestpixeldungeon.effects.PerkGain
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
import com.egoal.darkestpixeldungeon.items.weapon.melee.MagicBow
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.sprites.MobSprite
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndBag
import com.egoal.darkestpixeldungeon.windows.WndDialogue
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
    private var potionGotten = false

    override fun interact(): Boolean {
        sprite.turnTo(pos, Dungeon.hero.pos)

        if (Quest.Completed) {
            // safe on the land
            //todo:
            val bow = Dungeon.hero.belongings.getItem(MagicBow::class.java)
            if (bow == null) {
                tell(Messages.get(this, "see-again"))
            } else {
                WndDialogue.Show(this, M.L(this, "see-again"), M.L(this, "return-bow"), M.L(this, "bye")) {
                    if (it == 0) {
                        bow.cursed = false
                        if (bow.isEquipped(Dungeon.hero)) bow.doUnequip(Dungeon.hero, false)
                        else bow.detach(Dungeon.hero.belongings.backpack)
                        tell(M.L(this, "bow-got", Dungeon.hero.className()))
                    }
                }
            }

            return false
        }

        if (questGiven) {
            WndDialogue.Show(this, M.L(this, "reminder"), M.L(this, "opt-ok"), M.L(this, "opt-betray")) {
                onAnswered(it)
            }
        } else {
            questGiven = true
            WndDialogue.Show(this, M.L(this, "task"), M.L(this, "opt-onit"), M.L(this, "opt-betray")) {
                onAnswered(it)
            }
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
                it is Food || it is Scroll || it is Potion
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
            when (item) {
                is Food -> {
                    item.detach(Dungeon.hero.belongings.backpack)
                    onFoodGotten(item)
                }
                is Scroll -> {
                    item.detach(Dungeon.hero.belongings.backpack)
                    onScrollGotten(item)
                }
                is Potion -> {
                    item.detach(Dungeon.hero.belongings.backpack)
                    onPotionGotten(item)
                }
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
        if (scroll !is ScrollOfTeleportation) {
            returnItem(scroll)
            return
        }

        //todo: clean this
        if (potionGotten) {
            val content = M.L(this, "thanks-for-scroll") + "\n\n" + M.L(this, "give-bow")
            WndDialogue.Show(this, content, M.L(this, "decline_bow"), M.L(this, "bye")) {
                if (it == 0) {
                    GameScene.show(object : WndQuest(this, M.L(this, "give_key")) {
                        override fun onBackPressed() {
                            super.onBackPressed()
                            Quest.Completed = true
                            Dungeon.hero.recoverSanity(Random.Float(10f, 25f)) // recover much more.
                            Dungeon.level.drop(IronKey(20), pos).sprite.drop()

                            if (foodGotten) {
                                val perk = IntendedTransportation()
                                Dungeon.hero.heroPerk.add(perk)

                                PerkGain.Show(Dungeon.hero, perk)
                                GLog.p(Messages.get(Yvette::class.java, "taught", name))
                            }

                            this@Yvette.destroy()
                            (sprite as Sprite).leave()
                        }
                    })
                } else {
                    Quest.Completed = true
                    Dungeon.hero.recoverSanity(Random.Float(4f, 10f))
                    Dungeon.level.drop(MagicBow().identify(), pos).sprite.drop()

                    this@Yvette.destroy()
                    (sprite as Sprite).leave()
                }
            }
        } else {
            val content = M.L(this, "thanks-for-scroll") + M.L(this, "give-items") +
                    if (foodGotten) "\n\n" + Messages.get(this, "teach-teleportation") else ""

            GameScene.show(object : WndQuest(this, content) {
                override fun onBackPressed() {
                    super.onBackPressed()
                    leave()
                }
            })
        }
    }

    private fun onPotionGotten(potion: Potion) {
        if (potion is PotionOfHealing) {
            potionGotten = true
            tell(M.L(this, "thanks-for-potion"))
            sprite.emitter().start(Speck.factory(Speck.HEALING), 0.4f, 8)
        } else returnItem(potion)
    }

    private fun returnItem(item: Item) {
        tell(M.L(this, "not-this"))
        if (!item.collect()) Dungeon.level.drop(item, Dungeon.hero.pos)
    }

    private fun leave() {
        Quest.Completed = true

        if (foodGotten) {
            val perk = IntendedTransportation()
            Dungeon.hero.heroPerk.add(perk)

            PerkGain.Show(Dungeon.hero, perk)
            GLog.p(Messages.get(this, "taught", name))
        }

        // ScrollOfTeleportation.appear(this, -1) // move outside
        GLog.i(Messages.get(this, "disappear", name))

        Dungeon.hero.recoverSanity(Random.Float(4f, 10f))

        Dungeon.level.drop(Generator.WEAPON.generate(), pos)

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
        else Dungeon.level.drop(RangerHat().identify(), pos)

        destroy()
        (sprite as Sprite).leave()
    }

    private fun kill() {
        yell(Messages.get(this, "you-bastard"))
        GLog.w(Messages.get(this, "killed", name))

        Dungeon.hero.takeDamage(Damage(Random.Int(4, 10), Dungeon.hero, Dungeon.hero).type(Damage.Type.MENTAL))

//        Dungeon.level.drop(Bow(), pos)
        Dungeon.level.drop(IronKey(20), pos)
        Dungeon.level.drop(RangerHat().identify(), pos)
        Dungeon.level.drop(Generator.WEAPON.generate(), pos)
        Dungeon.level.drop(YvettesDiary(), pos)
        Dungeon.level.drop(MagicBow.Broken(), pos)

        Wound.hit(pos)
        Sample.INSTANCE.play(Assets.SND_CRITICAL)
        destroy()
        sprite.die()
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(STR_QUEST_GIVEN, questGiven)
        bundle.put(STR_FOOD_GOTTEN, foodGotten)
        bundle.put(STR_POTION_GOTTEN, potionGotten)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        questGiven = bundle.getBoolean(STR_QUEST_GIVEN)
        foodGotten = bundle.getBoolean(STR_FOOD_GOTTEN)
        potionGotten = bundle.getBoolean(STR_POTION_GOTTEN)
    }

    // unbreakable
    override fun reset() = true // no removed on reset

    override fun defenseSkill(enemy: Char) = 1000f

    override fun takeDamage(dmg: Damage) = 0

    override fun add(buff: Buff) {}

    companion object {
        private const val STR_QUEST_GIVEN = "quest-given"
        private const val STR_FOOD_GOTTEN = "food-gotten"
        private const val STR_POTION_GOTTEN = "potion-gotten"

        private const val STR_QUEST_NODE = "yvette"
        private const val STR_QUEST_COMPLETED = "completed"
        private const val STR_QUEST_SPAWNED = "spawned"

        fun CreateSkeletonHeap(): Heap = Heap().apply {
            type = Heap.Type.SKELETON
            drop(IronKey(20))
            drop(YvettesDiary())
            drop(Generator.WEAPON.generate())
            drop(Generator.ARMOR.generate())
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
}