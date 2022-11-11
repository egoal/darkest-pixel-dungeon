package com.egoal.darkestpixeldungeon.ui

import android.content.Intent
import android.net.Uri
import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.PixelScene
import com.egoal.darkestpixeldungeon.windows.WndOptions
import com.watabou.noosa.BitmapText
import com.watabou.noosa.Game
import com.watabou.noosa.Image
import com.watabou.noosa.audio.Sample
import com.watabou.noosa.ui.Button

class QQGroupButton : Button() {
    lateinit var image: Image
    lateinit var idstr: BitmapText

    init {
        width = image.width
        height = image.height + idstr.height()
    }

    override fun createChildren() {
        super.createChildren()

        image = Icons.EXIT.get()
        add(image)

        idstr = BitmapText("QQ: 818725226", PixelScene.pixelFont)
        idstr.measure()
        idstr.hardlight(0xcccccc)
        add(idstr)
    }

    override fun layout() {
        super.layout()
        image.x = x
        image.y = y
        idstr.x = x
        idstr.y = image.y + image.height
    }

    override fun onTouchDown() {
        image.brightness(1.5f)
        Sample.INSTANCE.play(Assets.SND_CLICK)
    }

    override fun onTouchUp() {
        image.resetColor()
    }

    override fun onClick() {
        if (!joinQQGroup("8b5zo1eBuFe00y43QfoLoiTChOzjPKEn")) {
            val wnd = WndOptions.CreateConfirm(Icons.WARNING.get(), M.L(this, "qq_title"), M.L(this, "qq_desc")) {}
            Game.scene().addToFront(wnd)
        }
    }

    /****************
     *
     * 发起添加群流程。群号：黑暗的像素地牢在重生(818725226) 的 key 为： 8b5zo1eBuFe00y43QfoLoiTChOzjPKEn
     * 调用 joinQQGroup(8b5zo1eBuFe00y43QfoLoiTChOzjPKEn) 即可发起手Q客户端申请加群 黑暗的像素地牢在重生(818725226)
     *
     * @param key 由官网生成的key
     * @return 返回true表示呼起手Q成功，返回false表示呼起失败
     */
    fun joinQQGroup(key: String): Boolean {
        val intent = Intent()
        intent.data = Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26jump_from%3Dwebapi%26k%3D$key")
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            Game.instance.startActivity(intent)
            true
        } catch (e: Exception) {
            // 未安装手Q或安装的版本不支持
            false
        }
    }
}