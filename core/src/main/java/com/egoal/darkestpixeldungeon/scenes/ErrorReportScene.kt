package com.egoal.darkestpixeldungeon.scenes

import com.egoal.darkestpixeldungeon.Chrome
import com.egoal.darkestpixeldungeon.DarkestPixelDungeon
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.TopExceptionHandler
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.ui.*
import com.watabou.noosa.Camera
import com.watabou.noosa.ui.Component

class ErrorReportScene : PixelScene() {

    override fun create() {
        super.create()

        val w = Camera.main.width
        val h = Camera.main.height

        val title = renderText(M.L(this, "title"), 9)
        title.hardlight(Window.TITLE_COLOR)
        title.x = (w - title.width()) / 2
        title.y = 4f
        align(title)
        add(title)

        val btnExit = ExitButton()
        btnExit.setPos(Camera.main.width - btnExit.width(), 0f)
        add(btnExit)

        // chrome window
        val pw = (w - 6).toFloat()
        val ph = (h - 20).toFloat()
        val panel = Chrome.get(Chrome.Type.WINDOW).apply {
            size(pw, ph)
            x = (w - pw) / 2
            y = title.y + title.height() + 2f
        }
        add(panel)

        val list = ScrollPane(Component())
        add(list)

        val content = list.content()
        content.clear()

        // version
        val versionstr = if (Dungeon.VERSION_STRING.isEmpty()) DarkestPixelDungeon.version
        else Dungeon.VERSION_STRING
        
        val version = renderText(versionstr, 6)
        content.add(version)

        // warning
        val warning = renderMultiline(M.L(this, "warning"), 6)
        warning.maxWidth(panel.innerWidth().toInt())
        content.add(warning)
        warning.setPos(version.x, version.y + version.height() + 4f)

        var y = warning.bottom() + 8f
        for (line in TopExceptionHandler.LoadErrorStrings()!!) {
            val log = renderMultiline(line, 6)
            content.add(log)
            log.maxWidth(panel.innerWidth().toInt())
            log.setPos(warning.left(), y)
            y = log.bottom() + 2f
        }

        // delete button
        val btnDelete = object : RedButton(M.L(this, "delete")) {
            override fun onClick() {
                TopExceptionHandler.DeleteErrorFile()
                onBackPressed()
            }
        }
        btnDelete.setRect(2f, y + 2f, panel.innerWidth() - 4f, 15f)
        content.add(btnDelete)

        content.setSize(panel.innerWidth(), btnDelete.bottom())

        list.setRect(panel.x + panel.marginLeft(), panel.y + panel.marginTop(),
                panel.innerWidth(), panel.innerHeight())
        list.scrollTo(0f, 0f)

        val archs = Archs()
        archs.setSize(Camera.main.width.toFloat(), Camera.main.height.toFloat())
        addToBack(archs)

        fadeIn()
    }

    override fun onBackPressed() {
        DarkestPixelDungeon.switchNoFade(TitleScene::class.java)
    }
}