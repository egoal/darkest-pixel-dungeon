package com.egoal.darkestpixeldungeon.windows;


import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.CatLix;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.PixelScene;
import com.egoal.darkestpixeldungeon.sprites.CatLixSprite;
import com.egoal.darkestpixeldungeon.ui.RedButton;
import com.egoal.darkestpixeldungeon.ui.RenderedTextMultiline;
import com.egoal.darkestpixeldungeon.ui.Window;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.watabou.noosa.RenderedText;

public class WndCatLix extends Window {

  private static final String TXT_MESSAGE = "message";
  private static final String TXT_AGREE = "agree";
  private static final String TXT_DISAGREE = "disagree";

  private static final int WIDTH = 120;
  private static final float GAP = 2.f;
  private static final int BTN_HEIGHT = 20;

  private CatLix catLix_;

  public WndCatLix(final CatLix catLix) {
    super();

    catLix_ = catLix;

    IconTitle titleBar = new IconTitle();
    titleBar.icon(new CatLixSprite());
    titleBar.label(catLix.name);
    titleBar.setRect(0, 0, WIDTH, 0);
    add(titleBar);

    RenderedTextMultiline rtmMessage = PixelScene.renderMultiline(
            Messages.get(this, TXT_MESSAGE), 6);
    rtmMessage.maxWidth(WIDTH);
    rtmMessage.setPos(0f, titleBar.bottom() + GAP);
    add(rtmMessage);

    // add buttons
    RedButton btnAgree = new RedButton(Messages.get(this, TXT_AGREE)) {
      @Override
      protected void onClick() {
        onAnswered(true);
      }
    };
    btnAgree.setRect(0, rtmMessage.bottom() + GAP, WIDTH, BTN_HEIGHT);
    add(btnAgree);

    RedButton btnDisagree = new RedButton(Messages.get(this, TXT_DISAGREE)) {
      @Override
      protected void onClick() {
        onAnswered(false);
      }
    };
    btnDisagree.setRect(0, btnAgree.bottom() + GAP, WIDTH, BTN_HEIGHT);
    add(btnDisagree);

    resize(WIDTH, (int) btnDisagree.bottom());
  }

  private void onAnswered(boolean isPraise) {
    catLix_.setAnswered_(isPraise);

    hide();
    if (catLix_.gift.doPickUp(Dungeon.hero)) {
      GLog.i(Messages.get(Dungeon.hero, "you_now_have", catLix_.gift.name()));
    } else
      Dungeon.level.drop(catLix_.gift, Dungeon.hero.pos).sprite.drop();

    if (isPraise)
      catLix_.yell(Messages.get(this, "happy", Dungeon.hero.className()));
    else
      catLix_.yell(Messages.get(this, "normal"));
  }
}
