package com.egoal.darkestpixeldungeon.items.books;

import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.windows.WndTextBook;

/**
 * Created by 93942 on 10/14/2018.
 */

public class TextBook extends Book{
  
  public int pageSize(){
    return Integer.parseInt(Messages.get(this, "pagesize"));
  }
  
  public String page(int i){
    return Messages.get(this, String.format("page%d", i));
  }
  
  @Override
  protected void doRead(){
    identify();
    GameScene.show(new WndTextBook(this));
  }
}
