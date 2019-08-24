package com.egoal.darkestpixeldungeon.items.artifacts;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.DarkestPixelDungeon;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Actor;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.buffs.FlavourBuff;
import com.egoal.darkestpixeldungeon.actors.buffs.LifeLink;
import com.egoal.darkestpixeldungeon.actors.buffs.MustDodge;
import com.egoal.darkestpixeldungeon.actors.buffs.Paralysis;
import com.egoal.darkestpixeldungeon.actors.buffs.Roots;
import com.egoal.darkestpixeldungeon.actors.buffs.Vertigo;
import com.egoal.darkestpixeldungeon.actors.buffs.Vulnerable;
import com.egoal.darkestpixeldungeon.actors.buffs.Weakness;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.actors.mobs.Mob;
import com.egoal.darkestpixeldungeon.effects.CellEmitter;
import com.egoal.darkestpixeldungeon.effects.Speck;
import com.egoal.darkestpixeldungeon.effects.particles.BlastParticle;
import com.egoal.darkestpixeldungeon.effects.particles.ShaftParticle;
import com.egoal.darkestpixeldungeon.effects.particles.SmokeParticle;
import com.egoal.darkestpixeldungeon.items.Heap;
import com.egoal.darkestpixeldungeon.items.wands.WandOfBlastWave;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.mechanics.Ballistica;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.CellSelector;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.CharSprite;
import com.egoal.darkestpixeldungeon.sprites.ItemSprite;
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet;
import com.egoal.darkestpixeldungeon.ui.RedButton;
import com.egoal.darkestpixeldungeon.ui.Window;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.egoal.darkestpixeldungeon.windows.IconTitle;
import com.egoal.darkestpixeldungeon.windows.WndOptions;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

/**
 * Created by 93942 on 7/29/2018.
 */

public class Astrolabe extends Artifact {
  {
    image = ItemSpriteSheet.ASTROLABE;
    unique = true;
    bones = false;
    defaultAction = AC_INVOKE;

    exp = 0;
    levelCap = 10;
    cooldown = 0;
  }

  private static float TIME_TO_INVOKE = 1f;
  private static int NORMAL_COOLDOWN = 20;

  private static final String AC_INVOKE = "INVOKE";

  @Override
  public ArrayList<String> actions(Hero hero) {
    ArrayList<String> actions = super.actions(hero);
    if (isEquipped(hero))
      actions.add(AC_INVOKE);

    return actions;
  }

  @Override
  public void execute(Hero hero, String action) {
    super.execute(hero, action);
    if (action.equals(AC_INVOKE)) {
      if (!isEquipped(hero))
        GLog.i(Messages.get(Artifact.class, "need_to_equip"));
      else {
        GameScene.show(new WndInvoke(this));

        updateQuickslot();
      }
    }
  }

  private void updateSprite() {
    int magic = 0;
    if (cachedInvoker_1 != null) ++magic;
    if (cachedInvoker_2 != null) ++magic;
    switch (magic) {
      case 1:
        image = ItemSpriteSheet.ASTROLABE_1;
        break;
      case 2:
        image = ItemSpriteSheet.ASTROLABE_2;
        break;
      default:
        image = ItemSpriteSheet.ASTROLABE;
        break;
    }
  }

  private boolean blockNextNegative = false;
  private boolean nextNegativeIsImprison = false;

  Invoker cachedInvoker_1 = null;
  Invoker cachedInvoker_2 = null;

  // invoke logic
  private void invokeMagic() {
    if (cooldown > 0) {
      GLog.i(Messages.get(this, "cooldown", cooldown));
      return;
    }

    cooldown = NORMAL_COOLDOWN;
    Sample.INSTANCE.play(Assets.SND_ASTROLABE);

    boolean invokePositive = Random.Float() < (cursed ? .5f : .75f);

    if (!invokePositive && blockNextNegative) {
      blockNextNegative = false;

      curUser.sprite.showStatus(0x420000, Messages.get(Invoker.class,
              "extremely_lucky_block"));
      return;
    }

    Invoker ivk;
    if (!invokePositive && nextNegativeIsImprison) {
      ivk = new imprison();
    } else {
      ivk = invokePositive ? randomPositiveInvoke() : randomNegativeInvoke();
    }

    curUser.sprite.showStatus(ivk.color(), ivk.status());
    CellEmitter.get(curUser.pos).start(ShaftParticle.FACTORY, 0.2f, 3);

    if (ivk.positive) {
      // positive invoker will cached
      if (cachedInvoker_1 == null)
        cachedInvoker_1 = ivk;
      else if (cachedInvoker_2 == null)
        cachedInvoker_2 = ivk;
      else {
        // replace
        cachedInvoker_1 = cachedInvoker_2;
        cachedInvoker_2 = ivk;
      }
    } else {
      ivk.invoke(curUser, this);
    }
  }

  //fixme: bad parameter, index should be 1 or 2, bad logical
  private void invoke(int index) {
    if (index == 1 && cachedInvoker_1 != null) {
      cachedInvoker_1.invoke(curUser, this);

      cachedInvoker_1 = cachedInvoker_2;
      cachedInvoker_2 = null;
    } else if (index == 2 && cachedInvoker_2 != null) {
      cachedInvoker_2.invoke(curUser, this);
      cachedInvoker_2 = null;
    }

    updateQuickslot();
  }

  private static Class<?>[] positiveInvokers = new Class<?>[]{
          foresight.class, purgation.class, life_link.class,
          extremely_lucky.class, pardon.class, faith.class,
          overload.class, guide.class, prophesy.class, sun_strike.class,
  };
  private static float[] positiveProbs = new float[]{
          10, 10, 10, 5, 10, 5, 10, 10, 10, 5
  };
  private static Class<?>[] negativeInvokers = new Class<?>[]{
          punish.class, vain.class, feedback.class, imprison.class,
  };
  private static float[] negativeProbs = new float[]{
          10, 10, 10, 10,
  };

  private Invoker randomPositiveInvoke() {
    try {
      return (Invoker) positiveInvokers[Random.chances(positiveProbs)]
              .newInstance();
    } catch (Exception e) {
      DarkestPixelDungeon.reportException(e);
      return null;
    }
  }

  private Invoker randomNegativeInvoke() {
    try {
      return (Invoker) negativeInvokers[Random.chances(negativeProbs)]
              .newInstance();
    } catch (Exception e) {
      DarkestPixelDungeon.reportException(e);
      return null;
    }
  }

  private static final String COOLDOWN = "cooldown";
  private static final String BLOCK_NEXT_NEGATIVE = "block_next_negative";
  private static final String NEXT_IS_IMPRISON = "next_negative_is_imprison";

  private static final String CACHED_INVOKER_1 = "cached_invoker_1";
  private static final String CACHED_INVOKER_2 = "cached_invoker_2";

  @Override
  public void storeInBundle(Bundle bundle) {
    super.storeInBundle(bundle);
    bundle.put(COOLDOWN, cooldown);
    bundle.put(BLOCK_NEXT_NEGATIVE, blockNextNegative);
    bundle.put(NEXT_IS_IMPRISON, nextNegativeIsImprison);

    if (cachedInvoker_1 != null)
      bundle.put(CACHED_INVOKER_1, cachedInvoker_1.getClass());
    if (cachedInvoker_2 != null)
      bundle.put(CACHED_INVOKER_2, cachedInvoker_2.getClass());
  }

  @Override
  public void restoreFromBundle(Bundle bundle) {
    super.restoreFromBundle(bundle);
    cooldown = bundle.getInt(COOLDOWN);
    blockNextNegative = bundle.getBoolean(BLOCK_NEXT_NEGATIVE);
    nextNegativeIsImprison = bundle.getBoolean(NEXT_IS_IMPRISON);

    try {
      Class c = bundle.getClass(CACHED_INVOKER_1);
      if (c != null)
        cachedInvoker_1 = (Invoker) c.newInstance();

      c = bundle.getClass(CACHED_INVOKER_2);
      if (c != null)
        cachedInvoker_2 = (Invoker) c.newInstance();

    } catch (Exception e) {
    }

    updateSprite();
  }

  @Override
  public int price() {
    return 0;
  }

  @Override
  protected ArtifactBuff passiveBuff() {
    return new AstrolabeRecharge();
  }

  public class AstrolabeRecharge extends ArtifactBuff {
    public boolean act() {
      if (cooldown > 0)
        --cooldown;

      updateQuickslot();
      spend(TICK);
      return true;
    }

  }

  public class WndInvoke extends Window {
    private static final int WIDTH = 120;
    private static final int MARGIN = 2;
    private static final int BTN_HEIGHT = 20;

    private static final int WIDTH_HELP_BUTTON = 15;

    Astrolabe astrolabe_;

    public WndInvoke(Astrolabe a) {
      astrolabe_ = a;

      IconTitle ic = new IconTitle(new ItemSprite(a.image(), null),
              Messages.get(Astrolabe.class, "select_invoker"));
      ic.setRect(0, 0, WIDTH, 0);
      add(ic);

      float pos = ic.bottom() + MARGIN;

      int index = 0;
      pos = addInvoker(pos, WIDTH, Messages.get(Astrolabe.class, "ac_invoke")
              , "", 0xFFFFFF, index++);

      if (a.cachedInvoker_1 != null)
        pos = addInvoker(pos, WIDTH, a.cachedInvoker_1.status(),
                a.cachedInvoker_1.desc(), a.cachedInvoker_1.color(), index++);

      if (a.cachedInvoker_2 != null)
        pos = addInvoker(pos, WIDTH, a.cachedInvoker_2.status(),
                a.cachedInvoker_2.desc(), a.cachedInvoker_2.color(), index++);

      resize(WIDTH, (int) pos);
    }

    float addInvoker(float pos, int width, final String name, final
    String help, int color, final int idx) {
      RedButton btn = new RedButton(name) {
        @Override
        protected void onClick() {
          hide();
          onSelect(idx);
        }
      };
      btn.textColor(color);

      if (help.length() != 0) {
        RedButton btnHelp = new RedButton("?") {
          @Override
          protected void onClick() {
            GameScene.show(new WndOptions(name, help));
          }
        };
        btnHelp.textColor(color);

        btn.setRect(MARGIN, pos, width - MARGIN * 3 - WIDTH_HELP_BUTTON,
                BTN_HEIGHT);
        add(btn);
        btnHelp.setRect(width - MARGIN - WIDTH_HELP_BUTTON, pos,
                WIDTH_HELP_BUTTON, BTN_HEIGHT);
        add(btnHelp);
      } else {
        btn.setRect(MARGIN, pos, width - MARGIN * 2, BTN_HEIGHT);
        add(btn);
      }

      return pos + BTN_HEIGHT + MARGIN;
    }

    protected void onSelect(int index) {
      switch (index) {
        case 1:
          invoke(1);
          break;
        case 2:
          invoke(2);
          break;
        default:
          invokeMagic();
          break;
      }

      updateSprite();
      updateQuickslot();
    }
  }

  //////////////////////////////////////////////////////////////////////////////
  //* invokers
  public static class Invoker {
    protected String name_ = "invoker";
    protected boolean needTarget_ = false;
    protected Hero user_ = null;
    protected Astrolabe a_ = null;
    public boolean positive = true;

    public String status() {
      return Messages.get(Invoker.class, name_);
    }

    public String desc() {
      return Messages.get(Invoker.class, name_ + "_desc");
    }

    public int color() {
      return positive ? 0xCC5252 : 0x000026;
    }

    public void invoke(Hero user, Astrolabe a) {
      user_ = user;
      a_ = a;
      if (needTarget_) {
        GameScene.selectCell(caster);
      } else {
        invoke_directly(user, a);

        user_.spend(TIME_TO_INVOKE);
        user_.busy();
        user_.sprite.operate(user_.pos);
      }
    }

    // impl
    protected void invoke_directly(Hero user, Astrolabe a) {
    }

    protected void invoke_on_target(Hero user, Astrolabe a, Char c) {
    }

    protected boolean check_is_other(Char c) {
      if (c == null || c == user_) {
        GLog.w(Messages.get(Astrolabe.Invoker.class, "not_select_target"));
        return false;
      }
      return true;
    }

    protected CellSelector.Listener caster = new CellSelector.Listener() {
      @Override
      public void onSelect(Integer cell) {
        if (cell != null) {
          final Ballistica shot = new Ballistica(curUser.pos, cell,
                  Ballistica.MAGIC_BOLT);
          Char c = Actor.findChar(shot.collisionPos);

          invoke_on_target(user_, a_, c);

          user_.spend(TIME_TO_INVOKE);
          user_.busy();
          user_.sprite.operate(user_.pos);
        }
      }

      @Override
      public String prompt() {
        return Messages.get(Astrolabe.Invoker.class, "prompt");
      }
    };
  }

  // positive
  public static class foresight extends Invoker {
    {
      name_ = "foresight";
    }

    @Override
    protected void invoke_directly(Hero user, Astrolabe a) {
      Buff.prolong(user, MustDodge.class, 3f).addDodgeTypeAll();
    }
  }

  public static class purgation extends Invoker {
    {
      name_ = "purgation";
      needTarget_ = true;
    }

    @Override
    protected void invoke_on_target(Hero user, Astrolabe a, Char c) {
      if (check_is_other(c)) {
        int dmg = (int) ((c.HT - c.HP) * .6f) + 1;
        //todo: add effect
        c.takeDamage(new Damage(dmg, user, c).addFeature(Damage.Feature.PURE
                | Damage.Feature.ACCURATE));
      }
    }
  }

  public static class life_link extends Invoker {
    {
      name_ = "life_link";
      needTarget_ = true;
    }

    @Override
    protected void invoke_on_target(Hero user, Astrolabe a, Char c) {
      if (check_is_other(c)) {
        Buff.prolong(user, LifeLink.class, 3f).linker = c.id();
      }
    }
  }

  public static class extremely_lucky extends Invoker {
    {
      name_ = "extremely_lucky";
    }

    @Override
    protected void invoke_directly(Hero user, Astrolabe a) {
      // recover hp
      int heal = (user.HT - user.HP) / 10 + 1;
      user.HP = heal > (user.HT - user.HP) ? user.HT : (user.HP + heal);
      user.sprite.showStatus(CharSprite.POSITIVE, Integer.toString(heal));

      a.blockNextNegative = true;
    }
  }

  public static class pardon extends Invoker {
    {
      name_ = "pardon";
      needTarget_ = true;
    }

    @Override
    protected void invoke_on_target(Hero user, Astrolabe a, Char c) {
      if (check_is_other(c)) {
        int dhp = c.HP / 4 + 1;
        c.HP += dhp;
        if (c.HP > c.HT)
          c.HT = c.HP;
        c.sprite.showStatus(CharSprite.POSITIVE, Integer.toString(dhp));
        Buff.prolong(c, Vulnerable.class, Vulnerable.DURATION).setRatio(2f);
      }
    }
  }

  public static class faith extends Invoker {
    {
      name_ = "faith";
    }

    @Override
    protected void invoke_directly(Hero user, Astrolabe a) {
      user.recoverSanity(Random.Int(1, 4));
      a.cooldown -= NORMAL_COOLDOWN / 2;
    }
  }

  public static class overload extends Invoker {
    {
      name_ = "overload";
      needTarget_ = true;
    }

    @Override
    protected void invoke_on_target(Hero user, Astrolabe a, Char c) {
      if (check_is_other(c)) {
        int cost = user.HT / 10;
        if (cost >= user.HP) cost = user.HP - 1;
        int dmg = cost * 2;

        c.takeDamage(new Damage(dmg, user, c).type(Damage.Type.MAGICAL));
        user.takeDamage(new Damage(cost, a, c).addFeature(Damage.Feature.PURE
                | Damage.Feature.ACCURATE));
      }
    }
  }

  public static class guide extends Invoker {
    {
      name_ = "guide";
      needTarget_ = true;
    }

    @Override
    protected void invoke_on_target(Hero user, Astrolabe a, Char c) {
      if (check_is_other(c)) {
        Ballistica shot = new Ballistica(curUser.pos, c.pos, Ballistica
                .MAGIC_BOLT);
        if (shot.path.size() > shot.dist + 1)
          WandOfBlastWave.throwChar(c,
                  new Ballistica(c.pos, shot.path.get(shot.dist + 1),
                          Ballistica.MAGIC_BOLT), 3);
      }
    }
  }

  public static class prophesy extends Invoker {
    {
      name_ = "prophesy";
    }

    @Override
    protected void invoke_directly(Hero user, Astrolabe a) {
      for (int i : PathFinder.NEIGHBOURS8) {
        Char ch = Actor.findChar(user.pos + i);
        if (ch != null) {
          Buff.prolong(ch, Paralysis.class, 3f);
          ch.sprite.emitter().burst(Speck.factory(Speck.LIGHT), 12);
        }
      }
    }
  }

  public static class sun_strike extends Invoker implements CellSelector
          .Listener {
    {
      name_ = "sun_strike";
    }

    private static float TIME_TO_CHANT = 3f;

    @Override
    public void invoke(Hero user, Astrolabe a) {
      user_ = user;
      a_ = a;
      GameScene.selectCell(this);
    }

    @Override
    public void onSelect(Integer cell) {
      if (cell != null) {
        if (Dungeon.level.visited[cell] || Dungeon.level.mapped[cell]) {
          Buff.prolong(user_, buff.class, 2f).target = cell;

          user_.spend(TIME_TO_CHANT);
          user_.busy();
          user_.sprite.operate(user_.pos);
        } else
          GLog.w(Messages.get(Astrolabe.Invoker.class, "not_select_target"));
      }
    }

    @Override
    public String prompt() {
      return Messages.get(Astrolabe.Invoker.class, "prompt");
    }

    public static class buff extends FlavourBuff {
      public int target = 0;

      private static final String TARGET = "target";

      @Override
      public boolean act() {
        // cast! like bomb...
        Sample.INSTANCE.play(Assets.SND_BLAST);
        if (Dungeon.visible[target]) {
          CellEmitter.center(target).burst(BlastParticle.FACTORY, 50);
        }

        boolean terrainAffected = false;
        ArrayList<Char> enemies = new ArrayList<>();
        for (int n : PathFinder.NEIGHBOURS9) {
          int c = target + n;
          if (c >= 0 && c < Dungeon.level.length()) {
            if (Dungeon.visible[c]) {
              CellEmitter.get(c).burst(SmokeParticle.FACTORY, 4);
            }

            if (Level.flamable[c]) {
              Dungeon.level.destroy(c);
              GameScene.updateMap(c);
              terrainAffected = true;
            }

            //destroys items / triggers bombs caught in the blast.
            Heap heap = Dungeon.level.heaps.get(c);
            if (heap != null) heap.explode();

            Char ch = Actor.findChar(c);
            if (ch != null && ch != Dungeon.hero) {
              enemies.add(ch);
            }
          }
        }

        if (!enemies.isEmpty()) {
          int totalDamage = 0;
          for (Char ch : enemies) if (ch.HT > totalDamage) totalDamage = ch.HT;
          totalDamage = Math.max(50, totalDamage);

          int dmg = totalDamage / enemies.size();
          for (Char ch : enemies) {
            Damage d = new Damage(Random.IntRange(dmg * 7 / 10, dmg * 12 / 10),
                    curUser, ch).addFeature(Damage.Feature.DEATH);
            //^ cannot be pure, which will kill boss directly.
            if (ch.pos == target) d.value *= 1.25f;
            ch.defendDamage(d);
            ch.takeDamage(d);
          }
        }

        if (terrainAffected) {
          Dungeon.observe();
        }

        return super.act();
      }

      @Override
      public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(TARGET, target);

      }

      @Override
      public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        target = bundle.getInt(TARGET);
      }
    }
  }

  // negative
  public static class punish extends Invoker {
    {
      name_ = "punish";
      positive = false;
    }

    @Override
    protected void invoke_directly(Hero user, Astrolabe a) {
      user.takeDamage(new Damage((int) (user.HP * .25f), this, user
      ).type(Damage.Type.MAGICAL).addFeature(Damage.Feature.ACCURATE));
    }
  }

  public static class vain extends Invoker {
    {
      name_ = "vain";
      positive = false;
    }

    @Override
    protected void invoke_directly(Hero user, Astrolabe a) {
      Buff.prolong(user, Vertigo.class, 5f);
      Buff.prolong(user, Weakness.class, 5f);
    }
  }

  public static class feedback extends Invoker {
    {
      name_ = "feedback";
      positive = false;
    }

    @Override
    protected void invoke_directly(Hero user, Astrolabe a) {
      user.takeDamage(new Damage(Random.Int(1, 10), this, user).type(Damage
              .Type.MENTAL)
              .addFeature(Damage.Feature.ACCURATE));
    }
  }

  public static class imprison extends Invoker {
    {
      name_ = "imprison";
      positive = false;
    }

    @Override
    protected void invoke_directly(Hero user, Astrolabe a) {
      Buff.prolong(user, Roots.class, 3f);
      a.cooldown += NORMAL_COOLDOWN;
    }
  }
}
