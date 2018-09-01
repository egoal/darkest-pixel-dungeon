package com.egoal.darkestpixeldungeon.actors.blobs;

import com.egoal.darkestpixeldungeon.effects.BlobEmitter;
import com.egoal.darkestpixeldungeon.effects.Speck;
import com.egoal.darkestpixeldungeon.messages.Messages;

/**
 * Created by 93942 on 8/18/2018.
 */

// currently, fog is only an effect for the new tile which is passable, but 
// cannot see through
public class Fog extends Blob {

  @Override
  public void use(BlobEmitter emitter) {
    super.use(emitter);

    emitter.pour(Speck.factory(Speck.DPD_FOG), .4f);
  }

  @Override
  public String tileDesc() {
    return Messages.get(this, "desc");
  }
}
