package com.egoal.darkestpixeldungeon.levels.diggers.secret

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.blobs.Blob
import com.egoal.darkestpixeldungeon.actors.blobs.Fire
import com.egoal.darkestpixeldungeon.actors.blobs.RoaringFire
import com.egoal.darkestpixeldungeon.actors.blobs.ToxicGas
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.mobs.Statue
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.items.Heap
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.diggers.*
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.StatueSprite
import com.watabou.utils.Random

class SecretGuardianDigger : Digger() {
    val guardian = Guard()

    override fun chooseDigArea(wall: Wall): Rect {
        val len = Random.Int(3, 5) * 2 + 1
        val w = 3

        return if (wall.direction.horizontal) chooseCenteredRect(wall, len, w)
        else chooseCenteredRect(wall, w, len)
    }

    override fun dig(level: Level, wall: Wall, rect: Rect): DigResult {
        val door = rect.center
        if (wall.direction.horizontal)
            door.x = wall.x1
        else door.y = wall.y1
        Set(level, door, Terrain.SECRET_DOOR)

        val cen = rect.center
        val plat = rect.center
        val guard = plat
        when (wall.direction) {
            Direction.Left -> {
                Fill(level, Rect(cen.x + 1, rect.x2, rect.y1, rect.y2), Terrain.STATUE_SP)
                LinkHorizontal(level, cen.y, rect.x1, rect.x2, Terrain.EMPTY_SP)
                plat.x = rect.x1
                guard.x = plat.x + 1
            }
            Direction.Right -> {
                Fill(level, Rect(rect.x1, cen.x - 1, rect.y1, rect.y2), Terrain.STATUE_SP)
                LinkHorizontal(level, cen.y, rect.x1, rect.x2, Terrain.EMPTY_SP)
                plat.x = rect.x2
                guard.x = plat.x - 1
            }
            Direction.Up -> {
                Fill(level, Rect(rect.x1, rect.x2, cen.y + 1, rect.y2), Terrain.STATUE_SP)
                LinkVertical(level, cen.x, rect.y1, rect.y2, Terrain.EMPTY_SP)
                plat.y = rect.y1
                guard.y = plat.y + 1
            }
            Direction.Down -> {
                Fill(level, Rect(rect.x1, rect.x2, rect.y1, cen.y - 1), Terrain.STATUE_SP)
                LinkVertical(level, cen.x, rect.y1, rect.y2, Terrain.EMPTY_SP)
                plat.y = rect.y2
                guard.y = plat.y - 1
            }
        }

        Set(level, cen, Terrain.DOOR)
        guardian.pos = level.pointToCell(guard)
        level.mobs.add(guardian)

        // prize
        val prize = when (Random.Int(3)) {
            0 -> Generator.random(Generator.Category.RING)
            1 -> Generator.random(Generator.Category.ARTIFACT)
            else -> Generator.random(Random.oneOf(Generator.Category.WEAPON, Generator.Category.ARMOR))
        }
        level.drop(prize, level.pointToCell(plat)).type = Heap.Type.CHEST

        return DigResult(rect, DigResult.Type.Secret)
    }

    companion object {
        
        // guardian
        class Guard : Statue() {
            init {
                spriteClass = GuardSprite::class.java
                
                // normal weapon
                weapon.enchant(null)
                weapon.degrade(weapon.level())
            }

            override fun die(cause: Any?) {
                // when killed, revenge
                if (Random.Float() < 0.7f)
                    GameScene.add(Blob.seed(Dungeon.hero.pos, 1000, ToxicGas::class.java))
                else
                    GameScene.add(Blob.seed(Dungeon.hero.pos, 2, RoaringFire::class.java))

                super.die(cause)
            }
        }

        class GuardSprite : StatueSprite() {
            init {
                tint(.5f, .2f, 0f, 0.2f)
            }

            override fun resetColor() {
                super.resetColor()
                tint(.5f, .2f, 0f, 0.2f)
            }
        }
    }
}
