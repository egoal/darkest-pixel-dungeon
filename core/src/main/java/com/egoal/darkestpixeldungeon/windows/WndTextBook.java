package com.egoal.darkestpixeldungeon.windows;

import com.egoal.darkestpixeldungeon.Chrome;
import com.egoal.darkestpixeldungeon.items.books.Book;
import com.egoal.darkestpixeldungeon.items.books.TextBook;
import com.egoal.darkestpixeldungeon.scenes.PixelScene;
import com.egoal.darkestpixeldungeon.ui.RenderedTextMultiline;
import com.egoal.darkestpixeldungeon.ui.Window;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.watabou.input.Touchscreen;
import com.watabou.noosa.TouchArea;
import com.watabou.utils.PointF;

/**
 * Created by 93942 on 5/10/2018.
 */

public class WndTextBook extends Window {

  private static final int WIDTH = 96;
  private static final int MARGIN = 6;

  private static final int HEIGHT = 144;

  private RenderedTextMultiline title_, content_, pageinfo_;
  private int page_ = 0;
  private TextBook book_;

  public WndTextBook(final TextBook book) {
    super(0, 0, Chrome.get(Chrome.Type.DPD_BOOK));

    book_ = book;

    title_ = PixelScene.renderMultiline(book.bookName(), 6);
    title_.maxWidth(WIDTH - MARGIN * 2);
    title_.invert();
    title_.setPos((WIDTH - (int) (title_.width())) / 2, 2);
    add(title_);

    content_ = PixelScene.renderMultiline(6);
    content_.maxWidth(WIDTH - MARGIN * 2);
    content_.setPos(MARGIN, title_.bottom() + 4);
    add(content_);

    pageinfo_ = PixelScene.renderMultiline("0/0", 6);
    pageinfo_.invert();
    pageinfo_.maxWidth(WIDTH);
    pageinfo_.setPos((WIDTH - (int) (pageinfo_.width())) / 2, HEIGHT - 6);
    add(pageinfo_);

    updatePage();

    add(new TouchArea(chrome) {
      @Override
      protected void onClick(Touchscreen.Touch t) {
        PointF ps = camera().screenToCamera((int) t.current.x, (int) t
                .current.y);
        if (ps.x < WIDTH * 0.4 && page_ > 0) {
          --page_;
          updatePage();
        } else if (ps.x > WIDTH * 0.6 && page_ < book.pageSize() - 1) {
          ++page_;
          updatePage();
        }
      }
    });

    resize(WIDTH + MARGIN * 2, HEIGHT);
  }

  private void updatePage() {
    content_.text(book_.page(page_));
    content_.invert();
    pageinfo_.text(String.format("%d/%d", page_ + 1, book_.pageSize()));
    pageinfo_.invert();
  }
}