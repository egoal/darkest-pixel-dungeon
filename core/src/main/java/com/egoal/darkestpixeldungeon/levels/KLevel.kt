package com.egoal.darkestpixeldungeon.levels

import android.util.Log
import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Challenges
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.Statistics
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.blobs.Blob
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.LockedFloor
import com.egoal.darkestpixeldungeon.actors.mobs.Bestiary
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.effects.particles.FlowParticle
import com.egoal.darkestpixeldungeon.effects.particles.WindParticle
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.items.Heap
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.KGenerator
import com.egoal.darkestpixeldungeon.items.armor.Armor
import com.egoal.darkestpixeldungeon.items.artifacts.DriedRose
import com.egoal.darkestpixeldungeon.items.bags.ScrollHolder
import com.egoal.darkestpixeldungeon.items.bags.SeedPouch
import com.egoal.darkestpixeldungeon.items.food.Food
import com.egoal.darkestpixeldungeon.items.food.Wine
import com.egoal.darkestpixeldungeon.items.potions.PotionOfHealing
import com.egoal.darkestpixeldungeon.items.potions.PotionOfMight
import com.egoal.darkestpixeldungeon.items.potions.PotionOfStrength
import com.egoal.darkestpixeldungeon.items.rings.RingOfWealth
import com.egoal.darkestpixeldungeon.items.scrolls.Scroll
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfLullaby
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfUpgrade
import com.egoal.darkestpixeldungeon.items.unclassified.Dewdrop
import com.egoal.darkestpixeldungeon.items.unclassified.Stylus
import com.egoal.darkestpixeldungeon.items.unclassified.Torch
import com.egoal.darkestpixeldungeon.levels.diggers.Digger
import com.egoal.darkestpixeldungeon.levels.traps.Trap
import com.egoal.darkestpixeldungeon.plants.BlandfruitBush
import com.egoal.darkestpixeldungeon.plants.Plant
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.egoal.darkestpixeldungeon.ui.CustomTileVisual
import com.watabou.noosa.Game
import com.watabou.noosa.Group
import com.watabou.utils.*
import java.io.File

abstract class KLevel : Bundlable {
    enum class Feeling {
        NONE, CHASM, WATER, GRASS, DARK
    }

    protected var w = 0
    protected var h = 0
    protected var l = 0

    var version = 0

    lateinit var map: IntArray

    lateinit var visited: BooleanArray
    lateinit var mapped: BooleanArray
    lateinit var fov: BooleanArray // field of view
    lateinit var fol: BooleanArray // field of lighted
    lateinit var passable: BooleanArray
    lateinit var losBlocking: BooleanArray
    lateinit var flamable: BooleanArray
    lateinit var secret: BooleanArray
    lateinit var solid: BooleanArray
    lateinit var avoid: BooleanArray
    lateinit var water: BooleanArray
    lateinit var pit: BooleanArray
    lateinit var discoverable: BooleanArray

    lateinit var feeling: Feeling

    var entrance = 0
    var exit = 0

    var locked = false // boss level locked
    var mobs = hashSetOf<Mob>()
    var heaps = SparseArray<Heap>()
    var blobs = hashMapOf<Class<out Blob>, Blob>()
    var plants = SparseArray<Plant>()
    var traps = SparseArray<Trap>()
    var customTiles = hashSetOf<CustomTileVisual>()

    protected var itemsToSpawn = mutableListOf<Item>()

    // visuals is added each time the scene is created
    protected var visuals: Group = Group()

    val color1 = 0x004400
    val color2 = 0x88cc44

    fun create() {
        setupSize()
        PathFinder.setMapSize(width, height)

        // allocate
        map = IntArray(l) { 0 }

        visited = BooleanArray(l) { false }
        mapped = BooleanArray(l) { false }
        fov = BooleanArray(l) { false }
        passable = BooleanArray(l) { false }
        losBlocking = BooleanArray(l) { false }
        flamable = BooleanArray(l) { false }
        secret = BooleanArray(l) { false }
        solid = BooleanArray(l) { false }
        avoid = BooleanArray(l) { false }
        water = BooleanArray(l) { false }
        pit = BooleanArray(l) { false }
        fol = BooleanArray(l) { false }
        discoverable = BooleanArray(l) { false }

        // feeling
        feeling = Feeling.NONE
        if (Dungeon.depth > 1) {
            val p = Random.Float()
            feeling = when {
                p < 0.2 -> Feeling.DARK
                p < 0.275 -> Feeling.WATER
                p < 0.35 -> Feeling.GRASS
                else -> Feeling.NONE
            }
        }

        // init items 
        val stationaryItems = selectStationaryItems()

        // build 
        PitRoomNeeded = Dungeon.depth > 1 && WeakFloorCreated
        WeakFloorCreated = false

        Generator.push() // save generator status
        KGenerator.stash()

        var iteration = 0
        while (true) {
            // reset status
            itemsToSpawn = stationaryItems.toMutableList()
            map.fill(Terrain.WALL)
            mobs = HashSet()
            heaps = SparseArray()
            blobs = HashMap()
            plants = SparseArray()
            traps = SparseArray()
            customTiles = HashSet()

            if (build(iteration)) {
                Log.d("dpd", "level build okay after $iteration trails.")
                break
            }

            Generator.pop() // restore status 
            KGenerator.recover()
        }

        decorate()

        buildFlagMaps()
        cleanWalls()

        createMobs()
        createItems()
    }

    val width: Int
        get() {
            if (w <= 0) setupSize()
            return w
        }
    val height: Int
        get() {
            if (h <= 0) setupSize()
            return h
        }

    val length: Int
        get() {
            if (l <= 0) setupSize()
            return l
        }

    fun xy2cell(x: Int, y: Int): Int = x + y * width
    fun pointToCell(p: Point) = xy2cell(p.x, p.y)
    fun cellToPoint(cell: Int): Point = Point(cell % width, cell / width)

    fun distance(p0: Int, p1: Int): Int = Point.DistanceInf(cellToPoint(p0), cellToPoint(p1))
    fun adjacent(a: Int, b: Int) = distance(a, b) == 1

    fun insideMap(cell: Int): Boolean = !(cell < width || cell >= length - width || cell % width == 0 || cell % width == width - 1)

    fun reset() {
        mobs = mobs.filter { it.reset() }.toHashSet()
        createMobs()
    }

    fun trackMusic(): String = Assets.TRACK_CHAPTER_1

    fun tilesTex(): String = Assets.TILES_SEWERS

    fun waterTex(): String = Assets.WATER_SEWERS

    abstract fun build(iteration: Int): Boolean

    abstract fun decorate()

    abstract fun createMobs()

    abstract fun createItems()

    fun loadMapDataFromFile(mapFile: String) {
        val lines = File(mapFile).readLines()

        for (i in 0 until lines.size) {
            val line = lines[i]

            if (i == 0) {
                val whs = line.split(" ")
                w = whs[0].toInt()
                h = whs[1].toInt()
                l = w * h
            } else {
                // map data 
                val r = i - 1
                val eles = line.split(" ")
                for (c in 0..w) {
                    val tile = eles[c].toInt()
                    val pos = xy2cell(c, r)

                    map[pos] = tile
                    when (tile) {
                        Terrain.ENTRANCE -> entrance = pos
                        Terrain.LOCKED_EXIT, Terrain.UNLOCKED_EXIT, Terrain.EXIT -> exit = pos
                    }
                }
            }
        }
    }

    fun seal() {
        if (!locked) {
            locked = true
            Buff.affect(Dungeon.hero, LockedFloor::class.java)
        }
    }

    fun unseal() {
        if (locked) locked = false
    }

    fun addVisuals(): Group {
        if (visuals.parent == null) visuals = Group()
        else visuals.clear()

        for (i in 0 until length) {
            if (pit[i]) {
                visuals.add(WindParticle.Wind(i))
                if (i >= width && water[i - width])
                    visuals.add(FlowParticle.Flow(i - width))
            }
            //todo: add light visuals
        }

        return visuals
    }

    fun nMobs(): Int = 0

    fun findMob(pos: Int): Mob? = mobs.find { it.pos == pos }

    fun respawnTime(): Float = when (Statistics.Clock.state) {
        Statistics.ClockTime.State.Day -> 50f
        Statistics.ClockTime.State.Night -> 40f
        Statistics.ClockTime.State.MidNight -> 30f
    }

    fun respawner(): Actor? = object : Actor() {
        init {
            actPriority = 1 // as if it were a buff 
        }

        override fun act(): Boolean {
            if (mobs.size < nMobs()) {
                val mob = Bestiary.mutable(Dungeon.depth).apply {
                    state = WANDERING
                    pos = randomRespawnCell()
                }
                if (Dungeon.hero.isAlive && mob.pos != -1 && distance(Dungeon.hero.pos, mob.pos) >= 4) {
                    GameScene.add(mob)
                    if (Statistics.AmuletObtained) mob.beckon(Dungeon.hero.pos)
                }
            }

            spend(respawnTime())
            return true
        }
    }

    fun randomRespawnCell(): Int {
        var cell = Random.Int(length)
        while (!passable[cell] || Dungeon.visible[cell] || Actor.findChar(cell) != null)
            cell = Random.Int(length)
        return cell
    }

    fun randomDestination(): Int {
        var cell: Int
        do {
            cell = Random.Int(length)
        } while (!passable[cell])
        return cell
    }

    fun addItemToSpawn(item: Item) {
        itemsToSpawn.add(item)
    }

    fun findPrizeItem(match: ((Item) -> Boolean)? = null): Item? {
        if (itemsToSpawn.isEmpty()) return null

        if (match == null) {
            val item = Random.element(itemsToSpawn)
            itemsToSpawn.remove(item)
            return item
        }

        return itemsToSpawn.find(match)
    }

    fun destroy(pos: Int) {
        if (Terrain.flags[map[pos]] and Terrain.UNSTITCHABLE == 0) {
            set(pos, Terrain.EMBERS)
        } else {
            val flood = PathFinder.NEIGHBOURS4.any { water[pos + it] }

            set(pos, if (flood) getWaterTile(pos) else Terrain.EMBERS)
        }
    }

    fun set(cell: Int, terrain: Int) {
        // todo: set this 
        // Digger.Set(this, cell, terrain)

        if (terrain !in listOf(Terrain.TRAP, Terrain.SECRET_TRAP, Terrain.INACTIVE_TRAP))
            traps.remove(cell)

        val flags = Terrain.flags[terrain]
        passable[cell] = flags and Terrain.PASSABLE != 0
        losBlocking[cell] = flags and Terrain.LOS_BLOCKING != 0
        flamable[cell] = flags and Terrain.FLAMABLE != 0
        secret[cell] = flags and Terrain.SECRET != 0
        solid[cell] = flags and Terrain.SOLID != 0
        avoid[cell] = flags and Terrain.AVOID != 0
        pit[cell] = flags and Terrain.PIT != 0
        water[cell] = terrain == Terrain.WATER || terrain >= Terrain.WATER_TILES
    }

    fun drop(item: Item?, cell: Int): Heap {
        // challenges
        if ((Dungeon.isChallenged(Challenges.NO_FOOD) && (item is Food || item is BlandfruitBush.Seed)) ||
                (Dungeon.isChallenged(Challenges.NO_ARMOR) && item is Armor) ||
                (Dungeon.isChallenged(Challenges.NO_HEALING) && item is PotionOfHealing) ||
                (Dungeon.isChallenged(Challenges.NO_HERBALISM) && (item is Plant.Seed || item is Dewdrop || item is SeedPouch)) ||
                (Dungeon.isChallenged(Challenges.NO_SCROLLS) && (item is ScrollHolder || (item is Scroll && item !is ScrollOfUpgrade))) ||
                item == null) {
            // create a dummy heap, dont add to game, remove it later
            return Heap().apply {
                sprite = ItemSprite()
                sprite.link(this)
            }
        }

        // alchemy pot reworked

        var heap = heaps.get(cell)
        if (heap == null) {
            heap = Heap().apply {
                seen = Dungeon.visible[cell]
                pos = cell
            }
            if (map[cell] == Terrain.CHASM || pit[cell]) {
                Dungeon.dropToChasm(item)
                GameScene.discard(heap)
            } else {
                heaps.put(cell, heap)
                GameScene.add(heap)
            }
        } else if (heap.type == Heap.Type.LOCKED_CHEST || heap.type == Heap.Type.CRYSTAL_CHEST) {
            // cannot drop on a box, throw it 
            val avials = PathFinder.NEIGHBOURS8.map { it + cell }.filter { passable[it] || avoid[it] }
            return drop(item, Random.element(avials))
        }

        heap.drop(item)
        Dungeon.level?.press(cell, null)
        
        return heap
    }

    protected fun setupSize() {
        if (w == 0 || h == 0) {
            w = 36
            h = 36
        }
        l = w * h
    }

    private fun selectStationaryItems(): List<Item> {
        if (Dungeon.depth <= 0 || Dungeon.bossLevel() || Dungeon.depth == 21) return listOf()
        val items = mutableListOf<Item>()

        items.add(KGenerator.FOOD.generate()) // a food 

        // quota
        val bonus = RingOfWealth.getBonus(Dungeon.hero, RingOfWealth.Wealth::class.java)
        if (Dungeon.posNeeded()) {
            if (Random.Float() > Math.pow(0.925, bonus.toDouble()))
                items.add(PotionOfMight())
            else items.add(PotionOfStrength())
            Dungeon.limitedDrops.strengthPotions.count++
        }
        if (Dungeon.souNeeded()) {
            items.add(ScrollOfUpgrade())
            Dungeon.limitedDrops.upgradeScrolls.count++
        }
        if (Dungeon.asNeeded()) {
            if (Random.Float() > Math.pow(0.925, bonus.toDouble()))
                items.add(Stylus())
            items.add(Stylus())
            Dungeon.limitedDrops.arcaneStyli.count++
        }
        if (Dungeon.wineNeeded()) {
            if (Random.Float() > Math.pow(0.925, bonus.toDouble()))
                items.add(Wine())
            items.add(Wine())
            Dungeon.limitedDrops.wine.count++
        }
        if (Dungeon.scrollOfLullabyNeed()) {
            if (Random.Float() > Math.pow(0.925, bonus.toDouble()))
                items.add(ScrollOfLullaby())
            items.add(ScrollOfLullaby())
            Dungeon.limitedDrops.lullabyScrolls.count++
        }

        // specials 
        Dungeon.hero.belongings.getItem(DriedRose::class.java)?.let { rose ->
            if (!rose.cursed) {
                // player still has a chance to max rose out even if the rose dropped later in the dungeon
                val petalsNeed = Math.ceil(((Dungeon.depth / 2 - rose.droppedPetals).toFloat() / 3).toDouble()).toInt();
                repeat(petalsNeed) {
                    if (rose.droppedPetals < 11) {
                        items.add(DriedRose.Companion.Petal())
                        rose.droppedPetals++
                    }
                }
            }
        }

        // extras
        val prop = 0.35f - Dungeon.depth / 5 * 0.05f
        if (Random.Float() < prop) items.add(Torch())

        if (Random.Int(10) == 0)
            items.add(Wine())

        return items
    }

    private fun buildFlagMaps() {
        val lightArea = arrayOf(-w * 2,
                -w - 1, -w, -w + 1,
                -2, -1, 0, 1, 2,
                w - 1, w, w + 1,
                w * 2)

        for (i in 0..length) {
            val flag = Terrain.flags[map[i]]
            passable[i] = (flag and Terrain.PASSABLE) != 0
            losBlocking[i] = (flag and Terrain.LOS_BLOCKING) != 0
            flamable[i] = (flag and Terrain.FLAMABLE) != 0
            secret[i] = (flag and Terrain.SECRET) != 0
            solid[i] = (flag and Terrain.SOLID) != 0
            avoid[i] = (flag and Terrain.AVOID) != 0
            water[i] = (flag and Terrain.LIQUID) != 0
            pit[i] = (flag and Terrain.PIT) != 0

            // field of light
            if ((flag and Terrain.LUMINARY) != 0)
                for (np in lightArea)
                    if (i + np in 0 until length)
                        fol[i + np] = true
        }

        // border
        for (i in 0 until width) {
            passable[i] = false
            avoid[i] = false

            passable[length - width + i] = false
            avoid[length - width + i] = false
        }
        for (r in 0 until height) {
            passable[xy2cell(0, r)] = false
            avoid[xy2cell(0, r)] = false

            passable[xy2cell(width - 1, r)] = false
            avoid[xy2cell(width - 1, r)] = false
        }

        // water, pit visuals 
        for (i in width until (length - width)) {
            if (water[i]) map[i] = getWaterTile(i)

            if (pit[i]) {
                if (!pit[i - width]) {
                    // use front of the terrain
                    val tile = map[i - width]
                    map[i] = when {
                        tile == Terrain.EMPTY_SP || tile == Terrain.STATUE_SP -> Terrain.CHASM_FLOOR_SP
                        water[i - width] -> Terrain.CHASM_WATER
                        (Terrain.flags[tile] and Terrain.UNSTITCHABLE) != 0 -> Terrain.CHASM_WALL
                        else -> Terrain.CHASM_FLOOR
                    }
                }
            }
        }
    }

    private fun getWaterTile(pos: Int): Int {
        val NEIGHBOUR4 = arrayOf(-width, 1, width, -1)
        var t = Terrain.WATER_TILES
        for (j in NEIGHBOUR4)
            if ((Terrain.flags[map[pos + j]] and Terrain.UNSTITCHABLE) != 0)
                t += 1 shl j

        return t
    }

    private fun cleanWalls() {
        for (i in 0 until length) {
            // not surround all by walls 
            var d = PathFinder.NEIGHBOURS9.map { it + i }.any {
                it in 0 until length && map[it] !in listOf(
                        Terrain.WALL, Terrain.WALL_DECO, Terrain.WALL_LIGHT_OFF, Terrain.WALL_LIGHT_ON)
            }

            if (d) {
                d = PathFinder.NEIGHBOURS9.map { it + i }.any { it in 0 until length && !pit[it] }
            }

            discoverable[i] = d
        }
    }

    override fun storeInBundle(bundle: Bundle) {
        bundle.put(VERSION, Game.versionCode)
        bundle.put("width", w)
        bundle.put("height", h)
        bundle.put(MAP, map)
        bundle.put(VISITED, visited)
        bundle.put(MAPPED, mapped)
        bundle.put(ENTRANCE, entrance)
        bundle.put(EXIT, exit)
        bundle.put(LOCKED, locked)
        bundle.put(HEAPS, heaps.values())
        bundle.put(PLANTS, plants.values())
        bundle.put(TRAPS, traps.values())
        bundle.put(CUSTOM_TILES, customTiles)
        bundle.put(MOBS, mobs)
        bundle.put(BLOBS, blobs.values)
        bundle.put(FEELING, feeling)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        version = bundle.getInt(VERSION)

        if (bundle.contains("width") && bundle.contains("height")) {
            w = bundle.getInt("width")
            h = bundle.getInt("height")
        } else setupSize()
        PathFinder.setMapSize(width, height)

        mobs = HashSet()
        heaps = SparseArray()
        blobs = HashMap()
        plants = SparseArray()
        traps = SparseArray()
        customTiles = HashSet()

        map = bundle.getIntArray(MAP)
        visited = bundle.getBooleanArray(VISITED)
        mapped = bundle.getBooleanArray(MAPPED)

        entrance = bundle.getInt(ENTRANCE)
        exit = bundle.getInt(EXIT)

        locked = bundle.getBoolean(LOCKED)

        for (h in bundle.getCollection(HEAPS))
            if (!(h as Heap).isEmpty)
                heaps.put(h.pos, h)

        for (p in bundle.getCollection(PLANTS))
            if (p is Plant)
                plants.put(p.pos, p)

        for (t in bundle.getCollection(TRAPS))
            if (t is Trap)
                traps.put(t.pos, t)

        for (ct in bundle.getCollection(CUSTOM_TILES))
            customTiles.add(ct as CustomTileVisual)

        for (m in bundle.getCollection(MOBS))
            (m as Mob?).let { mobs.add(it!!) }

        for (b in bundle.getCollection(BLOBS))
            if (b is Blob) blobs.put(b.javaClass, b)

        feeling = bundle.getEnum(FEELING, Feeling::class.java)

        buildFlagMaps()
        cleanWalls()

        WeakFloorCreated = false
    }

    companion object {
        // bad design, 
        var PitRoomNeeded = false
        var WeakFloorCreated = false

        private const val VERSION = "version"
        private const val MAP = "map"
        private const val VISITED = "visited"
        private const val MAPPED = "mapped"
        private const val ENTRANCE = "entrance"
        private const val EXIT = "exit"
        private const val LOCKED = "locked"
        private const val HEAPS = "heaps"
        private const val PLANTS = "plants"
        private const val TRAPS = "traps"
        private const val CUSTOM_TILES = "customTiles"
        private const val MOBS = "mobs"
        private const val BLOBS = "blobs"
        private const val FEELING = "feeling"
    }
}