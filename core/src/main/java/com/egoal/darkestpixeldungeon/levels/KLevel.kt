package com.egoal.darkestpixeldungeon.levels

import android.util.Log
import com.egoal.darkestpixeldungeon.Assets
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
import com.egoal.darkestpixeldungeon.items.Heap
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.KGenerator
import com.egoal.darkestpixeldungeon.items.artifacts.DriedRose
import com.egoal.darkestpixeldungeon.items.food.Wine
import com.egoal.darkestpixeldungeon.items.potions.PotionOfMight
import com.egoal.darkestpixeldungeon.items.potions.PotionOfStrength
import com.egoal.darkestpixeldungeon.items.rings.RingOfWealth
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfLullaby
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfUpgrade
import com.egoal.darkestpixeldungeon.items.unclassified.Stylus
import com.egoal.darkestpixeldungeon.items.unclassified.Torch
import com.egoal.darkestpixeldungeon.levels.traps.Trap
import com.egoal.darkestpixeldungeon.plants.Plant
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.ui.CustomTileVisual
import com.watabou.noosa.Group
import com.watabou.utils.*
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max

abstract class KLevel : Bundlable {
    // data
    var version = 0
    protected var w = 0
    protected var h = 0
    protected var len = 0
    lateinit var map: IntArray

    // flags
    lateinit var visited: BooleanArray
    lateinit var mapped: BooleanArray
    lateinit var fieldOfView: BooleanArray
    lateinit var passable: BooleanArray
    lateinit var losBlocking: BooleanArray
    lateinit var flamable: BooleanArray
    lateinit var secret: BooleanArray
    lateinit var solid: BooleanArray
    lateinit var avoid: BooleanArray
    lateinit var water: BooleanArray
    lateinit var pit: BooleanArray
    lateinit var luminary: BooleanArray
    lateinit var lighted: BooleanArray
    lateinit var discoverable: BooleanArray

    var entrance = 0
    var exit = 0

    var locked = false

    val mobs = mutableSetOf<Mob>()
    val heaps = SparseArray<Heap>()
    val blobs = mutableMapOf<Class<out Blob>, Blob>()
    val plants = SparseArray<Plant>()
    val traps = SparseArray<Trap>()
    val customTiles = mutableSetOf<CustomTileVisual>()

    protected var visuals: Group? = null

    protected var itemsToSpawn = mutableListOf<Item>()

    val color1 = 0x004400
    val color2 = 0x88cc44

    val width: Int
        get() = if (w == 0) {
            setupSize()
            w
        } else w
    val height: Int
        get() = if (h == 0) {
            setupSize()
            h
        } else h
    val length: Int
        get() = if (len == 0) {
            setupSize()
            len
        } else len

    fun create() {
        setupSize()
        PathFinder.setMapSize(w, h)

        map = IntArray(len)
        visited = BooleanArray(len) { false }
        mapped = BooleanArray(len) { false }

        //todo: feeling

        val stationaryItems = chooseStationaryItems()
        PitRoomNeeded = Dungeon.depth > 1 && WeakFloorCreated
        WeakFloorCreated = false

        KGenerator.stash() // some status will by modified during building...

        // generate
        for (i in 1..Int.MAX_VALUE) {
            itemsToSpawn.clear()
            itemsToSpawn.addAll(stationaryItems)

            map.fill(Terrain.WALL)

            mobs.clear()
            heaps.clear()
            blobs.clear()
            plants.clear()
            traps.clear()
            customTiles.clear()

            if (build(i)) {
                Log.d("dpd", "level build okay after $i trails.")
                break
            }

            KGenerator.recover()
        }

        decorate()

        buildFlagsMap()
        cleanWalls()

        createMobs()
        createItems()
    }

    fun reset() {
        mobs.filter { it.reset() }
        createMobs()
    }

    open fun trackMusic(): String = Assets.TRACK_CHAPTER_1

    open fun seal() {
        if (!locked) {
            locked = true
            Buff.affect(Dungeon.hero, LockedFloor::class.java)
        }
    }

    open fun unseal() {
        locked = false
    }

    open fun addVisuals(): Group {
        if (visuals?.parent == null) visuals = Group()
        else visuals!!.clear()

        for (i in 0 until length) {
            if (pit[i]) {
                visuals!!.add(WindParticle.Wind(i))
                if (i >= width && water[i - width])
                    visuals!!.add(FlowParticle.Flow(i - width))
            }
            TODO("add light visuals")
        }

        return visuals!!
    }

    open fun nMobs(): Int = 0

    open fun respawner(): Actor? {
        return object : Actor() {
            init {
                actPriority = 1
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
    }

    open fun randomRespawnCell(): Int {
        var cell: Int
        do {
            cell = Random.Int(len)
        } while (!passable[cell] || Dungeon.visible[cell] || Actor.findChar(cell) != null)
        return cell
    }

    open fun randomDestination(): Int {
        var cell: Int
        do {
            cell = Random.Int(len)
        } while (!passable[cell])
        return cell
    }

    fun addItemToSpawn(item: Item) {
        itemsToSpawn.add(item)
    }

    fun findPrizeItem(): Item? {
        val item = Random.element(itemsToSpawn)
        if (item != null) itemsToSpawn.remove(item)
        return item
    }

    fun findPrizeItem(filter: (Item) -> Boolean): Item? {
        if (itemsToSpawn.isEmpty()) return null

        val item = itemsToSpawn.find(filter)
        if (item != null) itemsToSpawn.remove(item)

        return item
    }

    fun distance(a: Int, b: Int): Int {
        val ax = a % width
        val ay = a / width
        val bx = b % width
        val by = b / width
        return max(abs(ax - bx), abs(ay - by))
    }

    fun adjacent(a: Int, b: Int): Boolean = distance(a, b) == 1

    fun inMap(cell: Int): Boolean {
        val p = cellToPoint(cell)
        return p.x in 1..(w - 2) && p.y in 1..(h - 2)
    }

    fun cellToPoint(cell: Int): Point = Point(cell % width, cell / width)

    fun pointToCell(p: Point): Int = xy2cell(p.x, p.y)

    fun xy2cell(x: Int, y: Int): Int = x + y * width

    fun loadMapFromFile(mapfile: String) {
        TODO()
    }

    protected open fun setupSize() {
        if (w == 0 || h == 0) {
            w = 36
            h = 36
            len = w * h
        }
    }

    protected open fun chooseStationaryItems(): MutableList<Item> {
        val stationaryItems = mutableListOf<Item>()
        if (Dungeon.depth <= 0 || Dungeon.depth == 21 || Dungeon.bossLevel())
            return stationaryItems

        stationaryItems.add(KGenerator.FOOD.generate())

        val bonus = RingOfWealth.getBonus(Dungeon.hero, RingOfWealth.Wealth::class.java).toDouble()

        if (Dungeon.posNeeded()) {
            if (Random.Float() > Math.pow(0.925, bonus))
                stationaryItems.add(PotionOfMight())
            else stationaryItems.add(PotionOfStrength())
            Dungeon.limitedDrops.strengthPotions.count++
        }
        if (Dungeon.souNeeded()) {
            stationaryItems.add(ScrollOfUpgrade())
            Dungeon.limitedDrops.upgradeScrolls.count++
        }
        if (Dungeon.asNeeded()) {
            if (Random.Float() > Math.pow(0.925, bonus))
                stationaryItems.add(Stylus())
            stationaryItems.add(Stylus())
            Dungeon.limitedDrops.arcaneStyli.count++
        }
        if (Dungeon.wineNeeded()) {
            if (Random.Float() > Math.pow(0.925, bonus))
                stationaryItems.add(Wine())
            stationaryItems.add(Wine())
            Dungeon.limitedDrops.wine.count++
        }
        if (Dungeon.scrollOfLullabyNeed()) {
            if (Random.Float() > Math.pow(0.925, bonus))
                stationaryItems.add(ScrollOfLullaby())
            stationaryItems.add(ScrollOfLullaby())
            Dungeon.limitedDrops.lullabyScrolls.count++
        }

        Dungeon.hero.belongings.getItem(DriedRose::class.java)?.let { rose ->
            if (!rose.cursed) {
                // this way if a rose is dropped later in the game, player still has a chance to max it out.
                val petals = ceil((Dungeon.depth / 2 - rose.droppedPetals) / 3f).toInt()
                repeat(petals) {
                    if (rose.droppedPetals < 11) {
                        stationaryItems.add(DriedRose.Companion.Petal())
                        rose.droppedPetals++
                    }
                }
            }
        }

        val prop = 0.3f - Dungeon.depth / 5 * 0.025f
        while (Random.Float() < prop)
            stationaryItems.add(Torch())

        if (Random.Int(10) == 0) stationaryItems.add(Wine())

        return stationaryItems
    }

    protected abstract fun build(iteration: Int): Boolean

    protected abstract fun decorate()

    protected abstract fun createMobs()

    protected abstract fun createItems()

    abstract fun tilesTex(): String

    abstract fun waterTex(): String

    private fun buildFlagsMap() {
        fieldOfView = BooleanArray(len)
        passable = BooleanArray(len)
        losBlocking = BooleanArray(len)
        flamable = BooleanArray(len)
        secret = BooleanArray(len)
        solid = BooleanArray(len)
        avoid = BooleanArray(len)
        water = BooleanArray(len)
        pit = BooleanArray(len)
        luminary = BooleanArray(len)
        lighted = BooleanArray(len)

        for (i in 0 until len) {
            val flags = Terrain.flags[map[i]]
            passable[i] = flags and Terrain.PASSABLE != 0
            losBlocking[i] = flags and Terrain.LOS_BLOCKING != 0
            flamable[i] = flags and Terrain.FLAMABLE != 0
            secret[i] = flags and Terrain.SECRET != 0
            solid[i] = flags and Terrain.SOLID != 0
            avoid[i] = flags and Terrain.AVOID != 0
            water[i] = flags and Terrain.LIQUID != 0
            pit[i] = flags and Terrain.PIT != 0
            luminary[i] = flags and Terrain.LUMINARY != 0
        }

        // TODO("update light map")

        val lastRow = len - w
        passable.fill(false, 0, width)
        passable.fill(false, lastRow, len)
        avoid.fill(false, 0, width)
        avoid.fill(false, lastRow, len)

        for (r in 0 until height) {
            passable[xy2cell(0, r)] = false
            passable[xy2cell(w - 1, r)] = false
            avoid[xy2cell(0, r)] = false
            avoid[xy2cell(w - 1, r)] = false
        }

        for (i in w until (len - w)) {
        }
    }

    private fun cleanWalls() {}

    private fun respawnTime(): Float = when (Statistics.Clock.state) {
        Statistics.ClockTime.State.Day -> 50f
        Statistics.ClockTime.State.Night -> 40f
        Statistics.ClockTime.State.MidNight -> 30f
    }

    override fun storeInBundle(bundle: Bundle) {
    }

    override fun restoreFromBundle(bundle: Bundle) {
    }

    companion object {
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