package com.egoal.darkestpixeldungeon.actors.mobs.npcs

import android.content.Intent
import android.net.Uri
import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.sprites.SimpleMobSprite
import com.egoal.darkestpixeldungeon.windows.WndDialogue
import com.watabou.noosa.Game

class Seeker : NPC.Unbreakable() {
    init {
        spriteClass = Sprite::class.java

        properties.add(Property.IMMOVABLE)
    }

    override fun interact(): Boolean {
        WndDialogue.Show(this, M.L(this, "greetings"), M.L(this, "desc_yourself")) {
            WndDialogue.Show(this, M.L(this, "dungeons"), M.L(this, "check_info")) {
                WndDialogue.Show(this, M.L(this, "desc_state"), M.L(this, "info_dpd"), M.L(this, "info_all"), M.L(this, "maybe_nexttime")) {
                    if (it == 0) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(WIKI_DPD))
                        Game.instance.startActivity(intent)
                    } else if (it == 1) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(WIKI_IDX))
                        Game.instance.startActivity(intent)
                    }
                }
            }
        }
        return false
    }

    class Sprite : SimpleMobSprite(Assets.SEEKER)

    companion object {
        private const val WIKI_DPD = "https://pixeldungeon.fandom.com/wiki/Mod-Darkest_Pixel_Dungeon"
        private const val WIKI_IDX = "https://pixeldungeon.fandom.com/wiki/Main_Page"
    }
}