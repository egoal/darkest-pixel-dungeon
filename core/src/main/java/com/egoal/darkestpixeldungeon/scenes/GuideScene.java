package com.egoal.darkestpixeldungeon.scenes;

import com.egoal.darkestpixeldungeon.Chrome;
import com.egoal.darkestpixeldungeon.DarkestPixelDungeon;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.ui.Archs;
import com.egoal.darkestpixeldungeon.ui.ExitButton;
import com.egoal.darkestpixeldungeon.ui.RenderedTextMultiline;
import com.egoal.darkestpixeldungeon.ui.ScrollPane;
import com.egoal.darkestpixeldungeon.ui.Window;
import com.watabou.noosa.Camera;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Image;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.RenderedText;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.Point;

import java.util.ArrayList;

/**
 * Created by 93942 on 5/27/2018.
 */

public class GuideScene extends PixelScene {

  private ArrayList<ListItem> items_ = new ArrayList<>();
  private RenderedTextMultiline rtmInfo_ = null;
  private int currentIdx = -1;

  @Override
  public void create() {
    super.create();
    
    int w = Camera.main.width;
    int h = Camera.main.height;

    // title
    RenderedText title = renderText(Messages.get(this, "title"), 9);
    title.hardlight(Window.TITLE_COLOR);
    title.x = (w - title.width()) / 2;
    title.y = 4;
    align(title);
    add(title);

    // exit
    ExitButton btnExit = new ExitButton();
    btnExit.setPos(w - btnExit.width(), 0);
    add(btnExit);

    // background
    Archs archs = new Archs();
    archs.setSize(Camera.main.width, Camera.main.height);
    addToBack(archs);

    // 
    NinePatch panel = Chrome.get(Chrome.Type.WINDOW);
    {
      int pw = w - 6;
      int ph = h - 20;
      panel.size(pw, ph);
      panel.x = (w - pw) / 2;
      panel.y = title.y + title.height() + 2;
    }
    add(panel);

    ScrollPane sp = new ScrollPane(new Component()) {
      @Override
      public void onClick(float x, float y) {
        for (ListItem li : items_) {
          if (li.onClick(x, y))
            break;
        }
      }
    };
    add(sp);
    {
      final float WIDTH_LISTITEM = 40f;
      final float HEIGHT_LISTITEM = 12f;
      final float GAP_LISTITEM = 1;
      final int CNT_GUIDES = Integer.valueOf(Messages.get(this, "pages"));

      Component content = sp.content();
      content.clear();

      // clickable items
      float libtm = 0f;
      for (int idx = 0; idx < CNT_GUIDES; ++idx) {
        final int index = idx;
        ListItem li = new ListItem(Messages.get(this, "title_" + Integer
                .toString(index))) {
          @Override
          protected void onClick() {
            showDescription(index);
          }
        };

        li.setRect(0f, index * (HEIGHT_LISTITEM + GAP_LISTITEM),
                WIDTH_LISTITEM, HEIGHT_LISTITEM);
        content.add(li);
        items_.add(li);

        libtm = li.bottom();
      }

      // guide
      ColorBlock cb = new ColorBlock(1, panel.innerHeight(), 0xFF000000);
      cb.x = WIDTH_LISTITEM + 1;
      content.add(cb);

      rtmInfo_ = PixelScene.renderMultiline(6);
      rtmInfo_.text("select to display");
      rtmInfo_.maxWidth((int) (panel.innerWidth() - WIDTH_LISTITEM - 2f));
      rtmInfo_.setPos(cb.x + 2, 2f);
      content.add(rtmInfo_);

      content.setSize(panel.innerWidth(), libtm);
    }

    sp.setRect(panel.x + panel.marginLeft(), panel.y + panel.marginTop(),
            panel.innerWidth(), panel.innerHeight());
    sp.scrollTo(0, 0);

    fadeIn();

    showDescription(0);
  }

  void showDescription(int idx) {
    if (idx == currentIdx)
      return;

    if (currentIdx >= 0)
      items_.get(currentIdx).highlight(false);
    currentIdx = idx;
    items_.get(currentIdx).highlight(true);
    rtmInfo_.text(Messages.get(this, "body_" + Integer.toString(idx)));
  }

  @Override
  protected void onBackPressed() {
    DarkestPixelDungeon.switchNoFade(TitleScene.class);
  }

  private static class ListItem extends Component {
    private RenderedTextMultiline feature;
    private ColorBlock line;

    public ListItem(String text) {
      super();

      feature.text(text);
    }

    public void highlight(boolean hl) {
      if (hl)
        feature.hardlight(0xFFFF44);
      else
        feature.hardlight(0xFFFFFF);
    }

    @Override
    protected void createChildren() {
      feature = PixelScene.renderMultiline(6);
      add(feature);

      line = new ColorBlock(1, 1, 0xFF222222);
      add(line);
    }

    @Override
    protected void layout() {
      line.size(width, 1);
      line.x = 0;
      line.y = y;

      feature.maxWidth((int) (width - 8 - 1));
      feature.setPos(4, y + 1 + (height() - 1 - feature.height()) / 2);
      PixelScene.align(feature);
    }

    protected boolean onClick(float x, float y) {
      if (inside(x, y)) {
        onClick();
        return true;
      } else {
        return false;
      }
    }

    protected void onClick() {
    }
  }
}
