package com.egoal.darkestpixeldungeon.actors.hero;

import com.egoal.darkestpixeldungeon.messages.Messages;
import com.watabou.utils.Bundle;

/**
 * Created by 93942 on 5/31/2018.
 */

public class HeroPerk {

  public enum Perk {
    NONE("none", 0x0000),
    DRUNKARD("drunkard", 0x0001),  // 酒徒：喝酒不会受伤，也不会醉
    CRITICAL_STRIKE("critical_strike", 0x0002),  // 致命一击：额外的暴击率
    KEEN("keen", 0x0004),  // 敏锐：更容易发现隐藏
    FEARLESS("fearless", 0x0008),  // 无畏：不会在血量低的时候因受伤加压
    NIGHT_VISION("night_vision", 0x0010),  // 夜视：额外的视野
    SHOOTER("shooter", 0x0020),  // 射手：
    SHREWD("shrewd", 0x0040),  // 精打细算：商店打折
    POSITIVE("positive", 0x0080),  // 乐观：一定几率抵挡精神伤害
    ASSASSIN("assassin", 0x0100),  // 刺客：偷袭加成
    ALCHEMIST("alchemist", 0x0200), // 炼金术士：炼药加成
    INTENDED_TRANSPORTATION("transportation", 0x0400), // 定点传送
    ;

    public String title;
    public int mask;

    public String desc() {
      return Messages.get(HeroPerk.class, title + "_desc");
    }

    public String title() {
      return Messages.get(HeroPerk.class, title);
    }

    Perk(String title, int mask) {
      this.title = title;
      this.mask = mask;
    }
  }

  private int perk = 0x00;  // nothing

  public HeroPerk(int p) {
    perk = p;
  }

  public HeroPerk add(Perk p) {
    perk |= p.mask;
    return this;
  }

  public boolean contain(Perk p) {
    return (perk & p.mask) != 0;
  }

  // storage
  private static final String PERK = "perk";

  public void storeInBundle(Bundle bundle) {
    bundle.put(PERK, perk);
  }

  public static HeroPerk restoreFromBundle(Bundle bundle) {
    int p = bundle.getInt(PERK);
    return new HeroPerk(p);
  }

}
