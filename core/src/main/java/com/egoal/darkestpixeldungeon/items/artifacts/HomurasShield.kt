package com.egoal.darkestpixeldungeon.items.artifacts

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.GhostHero
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.scenes.InterlevelScene
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndOptions
import com.watabou.noosa.Game
import java.util.ArrayList

class HomurasShield : Artifact() {
    init {
        image = ItemSpriteSheet.ARTIFACT_SHIELD

        levelCap = 1
    }

    override fun actions(hero: Hero): ArrayList<String> = super.actions(hero).apply { add(AC_REFLUX) }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)

        if (action == AC_REFLUX) {
            if (!isEquipped(hero)) GLog.w(M.L(Artifact::class.java, "need_to_equip"))
            else if (cursed) GLog.w(M.L(this, "cursed"))
            else {
                GameScene.show(object : WndOptions(ItemSprite(this), M.L(this, "name"),
                        M.L(this, "return_warn"), M.L(this, "yes"), M.L(this, "no")) {
                    override fun onSelect(index: Int) {
                        if (index == 0) reflux()
                    }
                })
            }
        }
    }

    private fun reflux() {
        Dungeon.hero.buff(TimekeepersHourglass.TimeFreeze::class.java)?.detach()

        Dungeon.level.mobs.filter { it is GhostHero }.forEach { it.destroy() }

        InterlevelScene.mode = InterlevelScene.Mode.REFLUX
        Game.switchScene(InterlevelScene::class.java)
    }

    override fun desc(): String {
        var desc = super.desc()
        if (isEquipped(Dungeon.hero)) desc += "\n\n" + M.L(this, "desc_hint")
        return desc
    }

    override fun passiveBuff(): ArtifactBuff = Recharge()

    inner class Recharge : ArtifactBuff()

    companion object {
        private const val AC_REFLUX = "reflux"
    }
}