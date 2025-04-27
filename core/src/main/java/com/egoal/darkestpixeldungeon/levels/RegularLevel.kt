package com.egoal.darkestpixeldungeon.levels

import android.util.Log
import com.egoal.darkestpixeldungeon.Bones
import com.egoal.darkestpixeldungeon.Challenge
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.mobs.Bestiary
import com.egoal.darkestpixeldungeon.actors.mobs.DarkSpirit
import com.egoal.darkestpixeldungeon.actors.mobs.RotLasher
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.BarterMan
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.Merchant
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.PotionSeller
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.ScrollSeller
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.items.Heap
import com.egoal.darkestpixeldungeon.items.scrolls.Scroll
import com.egoal.darkestpixeldungeon.levels.diggers.DigResult
import com.egoal.darkestpixeldungeon.levels.diggers.Digger
import com.egoal.darkestpixeldungeon.levels.diggers.LevelDigger
import com.egoal.darkestpixeldungeon.levels.diggers.Space
import com.egoal.darkestpixeldungeon.levels.diggers.normal.*
import com.egoal.darkestpixeldungeon.levels.diggers.secret.*
import com.egoal.darkestpixeldungeon.levels.diggers.specials.*
import com.egoal.darkestpixeldungeon.levels.traps.FireTrap
import com.egoal.darkestpixeldungeon.levels.traps.PrizeTrap
import com.egoal.darkestpixeldungeon.levels.traps.Trap
import com.egoal.darkestpixeldungeon.levels.traps.WornTrap
import com.egoal.darkestpixeldungeon.plants.Plant
import com.watabou.utils.Bundle
import com.watabou.utils.PathFinder
import com.watabou.utils.Random
import kotlin.math.max
import kotlin.math.min

abstract class RegularLevel : Level() {
    init {
        color1 = 0x48763c
        color2 = 0x59994a
    }

    protected var spaces: ArrayList<Space> = ArrayList()

    override fun setupSize() {
        if (width == 0 && height == 0) {
            height = 36
            width = height
        }

        length = width * height
    }

    open fun createLevelDigger() = LevelDigger(this)

    override fun build(iteration: Int): Boolean {
        if (iteration == 0 || chosenDiggers.isEmpty()) {
            chosenDiggers = chooseDiggers()
            Log.d("dpd", "${chosenDiggers.size} diggers chosen.")
        }

        val ld = createLevelDigger()
        if (!ld.dig(chosenDiggers))
            return false

        Log.d("dpd", "$iteration: level dag.")
        spaces = ld.spaces

        if (!setStairs())
            return false

        // do some painting...
        Log.d("dpd", "$iteration: terrains okay, now paint...")

        paintLuminary()
        paintWater()
        paintGrass()
        placeTraps()

        return true
    }

    fun secretDoors(): Int = map.count { it == Terrain.SECRET_DOOR }

    protected var chosenDiggers = ArrayList<Digger>()
    protected open fun chooseDiggers(): ArrayList<Digger> {
        val diggers = selectDiggers(Random.NormalIntRange(1, 4), Random.IntRange(12, 15))
        if (Dungeon.shopOnLevel() && !Dungeon.hero.challenges.contains(Challenge.CastingMaster)) diggers.add(MerchantDigger())

        return diggers
    }

    protected fun selectDiggers(specials: Int, total: Int): ArrayList<Digger> {
        val diggers = ArrayList<Digger>()

        // as most 1 secret per level
        if (!Dungeon.bossLevel() && Random.IntRange(2, 5) <= specials) {
            Log.d("dpd", "a secret digger chosen.")
            diggers.add(Random.chances(SecretDiggers).newInstance())
        }

        // specials
        val probs = HashMap<Class<out Digger>, Float>(SpecialDiggers)
        if (Dungeon.depth == 1) {
            probs.remove(AltarDigger::class.java)
            probs.remove(WandDigger::class.java)
        }

        if (pitRoomNeeded) {
            // todo: this is fragile
            diggers.add(PitDigger())

            // a pit room is need, remove all locked diggers
            probs.remove(ArmoryDigger::class.java)
            probs.remove(CryptDigger::class.java)
            probs.remove(LaboratoryDigger::class.java)
            probs.remove(LibraryDigger::class.java)
            probs.remove(StatueDigger::class.java)
            probs.remove(TreasuryDigger::class.java)
            probs.remove(VaultDigger::class.java)
            probs.remove(WandDigger::class.java)
            // and the weak floor digger 
            probs.remove(WeakFloorDigger::class.java)
        } else if (Dungeon.labNeed()) {
            diggers.add(LaboratoryDigger())
            probs.remove(LaboratoryDigger::class.java)

            Dungeon.limitedDrops.laboratories.count++
            Log.d("dpd", "would create lab")
        }
        if (Dungeon.demonNeed()) {
            diggers.add(DemonDigger())
            probs.remove(DemonDigger::class.java)

            Dungeon.limitedDrops.archDemons.count++
            Log.d("dpd", "would add demon")
        }

        // never fall to boss
        if (Dungeon.bossLevel(Dungeon.depth + 1))
            probs.remove(WeakFloorDigger::class.java)

        while (diggers.size < specials) {
            val cls = Random.chances(probs)
            cls ?: break

            probs[cls] = 0f // unique.
            diggers.add(cls.newInstance())
        }

        // weak floor check
        weakFloorCreated = diggers.any { it is WeakFloorDigger }

        Log.d("dpd", "${diggers.size} special diggers chosen, weak floor: $weakFloorCreated")

        // random draft normal diggers
        while (diggers.size < total)
            diggers.add(Random.chances(NormalDiggers).newInstance())

        return diggers
    }

    protected open fun setStairs(): Boolean {
        val normalSpaces = spaces.filter { it.type == DigResult.Type.Normal }

        val trySetStairs = fun(): Triple<Int, Pair<Space, Int>, Pair<Space, Int>>? {
            var theEntrance = -1
            var spaceEntrance: Space
            do {
                spaceEntrance = Random.element(normalSpaces)
                theEntrance = pointToCell(spaceEntrance.rect.random(1))
            } while (map[theEntrance] != Terrain.EMPTY)

            for (i in 1..30) {
                var theExit = -1
                val spaceExit = Random.element(normalSpaces)
                if (spaceEntrance == spaceExit) continue

                theExit = pointToCell(spaceExit.rect.random(1))

                if (map[theExit] == Terrain.EMPTY) {
                    val dis = distance(theEntrance, theExit)
                    if (dis >= 12)
                        return Triple(dis, Pair(spaceEntrance, theEntrance), Pair(spaceExit, theExit))
                }
            }
            return null
        }

        val tpl = (1..10).mapNotNull { trySetStairs() }.maxByOrNull { it.first }

        if (tpl == null) return false

        tpl.second.first.type = DigResult.Type.Entrance
        entrance = tpl.second.second
        exit = tpl.third.second
        tpl.third.first.type = DigResult.Type.Exit

        map[entrance] = Terrain.ENTRANCE
        map[exit] = Terrain.EXIT

        return true
    }

    override fun nMobs(): Int = when (Dungeon.depth) {
        0, 1 -> 0
        in 2..4 -> 3 + Dungeon.depth % 5 + Random.Int(5)
        else -> 3 + Dungeon.depth % 5 + Random.Int(7)
    }

    protected fun createSellers() {
        val spawnSeller = { seller: Merchant ->
            seller.initSellItems()
            val s = randomSpace(DigResult.Type.Normal)
            do {
                seller.pos = pointToCell(s!!.rect.random())
            } while (findMobAt(seller.pos) != null || !passable[seller.pos])
            mobs.add(seller)
        }

        if (Dungeon.depth in 3 until 20) {
            val psProb = if (Dungeon.shopOnLevel()) .1f else .2f
            if (Random.Float() < psProb) spawnSeller(PotionSeller.Random())

            val ssProb = if (Dungeon.shopOnLevel()) .08f else .18f
            if (Random.Float() < ssProb) spawnSeller(ScrollSeller())

            val bmProb = if (Dungeon.shopOnLevel()) .05f else .1f
            if (Random.Float() < bmProb) spawnSeller(BarterMan())
        }
    }

    override fun createMobs() {
        if (!Dungeon.hero.challenges.contains(Challenge.CastingMaster))
            createSellers()

        val trySpawn = { space: Space ->
            val mob = Bestiary.mob(Dungeon.depth).apply {
                pos = pointToCell(space.rect.random())
            }
            if (passable[mob.pos] && distance(entrance, mob.pos) > 6 && findMobAt(mob.pos) == null) {
                mobs.add(mob)
                1
            } else 0
        }

        var mobsToSpawn = max(10, nMobs()) // if (Dungeon.depth == 1) 10 else nMobs()

        // well distributed in each space
        val normalSpaces = spaces.filter { it.type == DigResult.Type.Normal }
        var index = 0
        while (mobsToSpawn > 0) {
            mobsToSpawn -= trySpawn(normalSpaces[index])

            // extra one in the same space
            if (mobsToSpawn > 0 && Random.Int(3) == 0)
                mobsToSpawn -= trySpawn(normalSpaces[index])

            if (++index >= normalSpaces.size)
                index = 0
        }

        DarkSpirit.Gen()?.let {
            for (i in 1..3) {
                it.pos = randomRespawnCell()
                if (it.pos >= 0) {
                    mobs.add(it)
                    break
                }
            }
        }
    }

    override fun createItems() {
        val random_item = {
            if (Dungeon.depth > 5) Generator.generate(Dungeon.hero)
            else Generator.generate()
        }

        var nItems = 3

        // bonus from wealth
        while (nItems <= 6 && Random.Float() < .25f + Dungeon.hero.wealthBonus() * 0.05f)
            ++nItems

        for (i in 1..nItems) {
            val heap = when (Random.Int(10)) {
                0 -> Heap.Type.SKELETON
                in 1..4 -> if (Dungeon.depth > 1 && Random.Float() < 0.25f) Heap.Type.MIMIC else Heap.Type.CHEST
                else -> Heap.Type.HEAP
            }
            drop(random_item(), randomDropCell()).type = heap
        }

        // extra missile weapon
        run {
            val p = Random.Float()
            val item = when {
                p < 0.3 -> Generator.WEAPON.MISSSILE.generate()
                p < 0.5 -> Generator.generate()
                else -> null
            }
            if (item != null) drop(item, randomDropCell())
        }

        // inherent items, not dropped in generation
        for (item in itemsToSpawn) {
            var c = randomDropCell()
            // never drop scroll on fire trap
            if (item is Scroll)
                while ((map[c] == Terrain.TRAP || map[c] == Terrain.SECRET_TRAP) && traps.get(c) is FireTrap)
                    c = randomDropCell()

            drop(item, c).type = Heap.Type.HEAP
        }

        // hero remains
        val item = Bones.get()
        if (item != null)
            drop(item, randomDropCell()).type = Heap.Type.REMAINS
    }

    // paintings
    protected fun paintLuminary() {
        val availableWalls = HashSet<Int>()
        for (i in width until length - width)
            if (map[i] == Terrain.WALL && PathFinder.NEIGHBOURS4.any {
                        map[i + it] in listOf(Terrain.EMPTY, Terrain.EMPTY_SP, Terrain.EMPTY_DECO)
                    })
                availableWalls.add(i)

        val ratioLight = if (Dungeon.depth < 10) 0.15f else 0.1f
        val ratioLightOn = if (feeling == Level.Feeling.DARK) 0.5f else 0.7f
        for (i in availableWalls) {
            if (Random.Float() < ratioLight)
                map[i] = if (Random.Float() < ratioLightOn) Terrain.WALL_LIGHT_ON
                else Terrain.WALL_LIGHT_OFF
        }
    }

    protected abstract fun water(): BooleanArray
    protected abstract fun grass(): BooleanArray

    protected fun paintWater() {
        val water = water()
        for (i in 0 until length)
            if (map[i] == Terrain.EMPTY && water[i])
                map[i] = Terrain.WATER

    }

    protected fun paintGrass() {
        val grass = grass()

        if (feeling == Feeling.GRASS)
            for (space in spaces) {
                val rect = space.rect
                grass[xy2cell(rect.x1, rect.y1)] = Random.Int(2) == 0
                grass[xy2cell(rect.x2, rect.y1)] = Random.Int(2) == 0
                grass[xy2cell(rect.x1, rect.y2)] = Random.Int(2) == 0
                grass[xy2cell(rect.x2, rect.y2)] = Random.Int(2) == 0
            }

        for (i in width + 1 until length - width - 1) {
            if (map[i] == Terrain.EMPTY && grass[i]) {
                val count = PathFinder.NEIGHBOURS8.count { grass[i + it] }

                map[i] = if (Random.Float() < count / 12f) {
                    if (Random.Float() < 0.015 && Dungeon.depth > 0) {
                        //^^ not in the village
                        plant(Generator.SEED.generate() as Plant.Seed, i)
                        Terrain.GRASS
                    } else
                        Terrain.HIGH_GRASS
                } else Terrain.GRASS
            }
        }

        if (Dungeon.depth > 1) {
            for (i in width + 1 until length - width - 1) {
                if (map[i] == Terrain.HIGH_GRASS || map[i] == Terrain.HIGH_GRASS_COLLECTED) {
                    val count = PathFinder.NEIGHBOURS8.count { map[i + it] == Terrain.HIGH_GRASS }
                    if (Random.Float() < (count - 2) / 8f && Random.Float() < 0.1f) {
                        mobs.add(RotLasher().apply {
                            pos = i
                            setLevel(Dungeon.depth)
                        })
                        Digger.Set(this, i, Terrain.GRASS)
                    }
                }
            }
        }
    }

    // traps
    protected open fun nTraps() = Random.NormalIntRange(1 + Dungeon.depth / 3, 3 + Dungeon.depth / 2)

    protected open fun trapClasses(): Array<Class<out Trap>> = arrayOf(WornTrap::class.java)

    protected open fun trapChances(): FloatArray = floatArrayOf(1f)

    protected open fun placeTraps() {
        val trapChances = trapChances()
        val trapClasses = trapClasses()

        val validCells = (1 until length).filter { map[it] == Terrain.EMPTY && findMobAt(it) == null }.shuffled()
        val traps = min(nTraps(), (validCells.size * 0.15).toInt())

        // todo:
        // bonus from wealth
        var nPrize = 1
        val bonus = Dungeon.hero.wealthBonus()
        while (Random.Float() < .2f + bonus * 0.05f) if (++nPrize >= 5) break

        val trapsToSpawn = List(min(traps + nPrize, validCells.size)) {
            if (it < traps)
                trapClasses[Random.chances(trapChances)].newInstance().hide()
            else
                PrizeTrap().hide()
        }

        Log.d("dpd", "would add ${trapsToSpawn.size} traps, of which ${trapsToSpawn.size - traps} are prizes.")

        for (pr in trapsToSpawn.withIndex()) {
            val cell = validCells[pr.index]
            val trap = pr.value

            setTrap(trap, cell)
            map[cell] = if (trap.visible) Terrain.TRAP else Terrain.SECRET_TRAP
        }
    }

    protected fun randomSpace(type: DigResult.Type, tries: Int = Int.MAX_VALUE): Space? {
        for (i in 1..tries) {
            val s = Random.element(spaces)
            if (s.type == type)
                return s
        }

        return null
    }

    //!!! this function's behaviour can be undefined!!!
    fun spaceAt(cell: Int): Space? = spaces.find {
        it.rect.inside(cellToPoint(cell))
    }

    override fun randomRespawnCell(): Int {
        for (i in 1..30) {
            val s = randomSpace(DigResult.Type.Normal, 10)
            if (s != null) {
                val cell = pointToCell(s.rect.random())
                if (!Dungeon.visible[cell] && passable[cell] && Actor.findChar(cell) == null)
                    return cell
            }
        }
        return -1
    }

    override fun randomDestination(): Int {
        while (true) {
            val cell = Random.Int(length())
            if (passable[cell])
                return cell
        }
    }

    protected fun randomDropCell(): Int {
        while (true) {
            val s = randomSpace(DigResult.Type.Normal, 1)
            if (s != null) {
                val cell = pointToCell(s.rect.random())
                if (passable[cell] && findMobAt(cell) == null)
                    return cell
            }
        }
    }

    override fun pitCell(): Int {
        val s = spaces.find { it.type == DigResult.Type.Pit }
        return if (s == null) super.pitCell() else pointToCell(s.rect.random(1))
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(SPACES, spaces)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)

        Log.d("dpd", Generator.SEED.javaClass.name)

        spaces = bundle.getCollection(SPACES) as ArrayList<Space>
        weakFloorCreated = spaces.any { it.type == DigResult.Type.WeakFloor }

        Log.d("dpd", String.format("%d spaces restored.", spaces.size))
    }

    companion object {
        private val SPACES = "spaces"

        val SpecialDiggers: Map<Class<out Digger>, Float> = mapOf(
                ArmoryDigger::class.java to 0.5f,
                GardenDigger::class.java to 1f,
                LaboratoryDigger::class.java to 1f,
                LibraryDigger::class.java to 1f,
                MagicWellDigger::class.java to 1f,
                PitDigger::class.java to 0f,
                PoolDigger::class.java to 1f,
                QuestionerDigger::class.java to 1f,
                MerchantDigger::class.java to 0f,
                StatuaryDigger::class.java to 1f,
                StatueDigger::class.java to 1f,
                StorageDigger::class.java to 0.8f,
                TrapsDigger::class.java to 1f,
                TreasuryDigger::class.java to 0.75f,
                VaultDigger::class.java to 0.75f,
                WeakFloorDigger::class.java to 0.75f,
                AltarDigger::class.java to 0.75f,
                WandDigger::class.java to 0.75f,
                CryptDigger::class.java to 0.75f,
                DemonDigger::class.java to 0f // quota
        )

        val SecretDiggers: HashMap<Class<out Digger>, Float> = hashMapOf(
                SecretGuardianDigger::class.java to 1f,
                SecretLibraryDigger::class.java to 1f,
                SecretSummoningDigger::class.java to 1f,
                SecretTreasuryDigger::class.java to 1f,
                SecretGardenDigger::class.java to 1f,
                SecretMerchantDigger::class.java to 0.5f
        )

        val NormalDiggers: HashMap<Class<out Digger>, Float> = hashMapOf(
                BrightDigger::class.java to .075f,
                CellDigger::class.java to .1f,
                CircleDigger::class.java to .05f,
                DiamondDigger::class.java to .05f,
                LatticeDigger::class.java to .025f,
                RectDigger::class.java to 1f,
                RoundDigger::class.java to .05f,
                StripDigger::class.java to .1f,
                CrossDigger::class.java to .05f,
                PatchDigger::class.java to .05f,
                PillarDigger::class.java to .05f,
                GraveyardDigger::class.java to .05f,
                SmallCornerDigger::class.java to 0.075f,
                PuddleDigger::class.java to .05f,
        )
    }
}