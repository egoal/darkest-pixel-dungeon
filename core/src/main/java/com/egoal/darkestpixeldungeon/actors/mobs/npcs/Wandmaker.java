/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015  Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2016 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.egoal.darkestpixeldungeon.actors.mobs.npcs;

import com.egoal.darkestpixeldungeon.Journal;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
;
import com.egoal.darkestpixeldungeon.items.Generator;
import com.egoal.darkestpixeldungeon.items.quest.CorpseDust;
import com.egoal.darkestpixeldungeon.items.quest.Embers;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.levels.Terrain;
import com.egoal.darkestpixeldungeon.levels.diggers.Digger;
import com.egoal.darkestpixeldungeon.levels.diggers.Rect;
import com.egoal.darkestpixeldungeon.levels.diggers.unordinary.MassGraveDigger;
import com.egoal.darkestpixeldungeon.levels.diggers.unordinary.RitualSiteDigger;
import com.egoal.darkestpixeldungeon.levels.diggers.unordinary.RotGardenDigger;
import com.egoal.darkestpixeldungeon.windows.WndWandmaker;
import com.egoal.darkestpixeldungeon.Challenges;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.items.quest.CeremonialCandle;
import com.egoal.darkestpixeldungeon.items.wands.Wand;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.plants.Rotberry;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.WandmakerSprite;
import com.egoal.darkestpixeldungeon.windows.WndQuest;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class Wandmaker extends NPC {

  {
    spriteClass = WandmakerSprite.class;

    properties.add(Property.IMMOVABLE);
  }

  @Override
  protected boolean act() {
    throwItem();
    return super.act();
  }

  @Override
  public float defenseSkill(Char enemy) {
    return 1000;
  }

  @Override
  public int takeDamage(Damage dmg) {
    return 0;
  }

  @Override
  public void add(Buff buff) {
  }

  @Override
  public boolean reset() {
    return true;
  }

  @Override
  public boolean interact() {

    sprite.turnTo(pos, Dungeon.hero.pos);
    if (Quest.given) {

      Item item;
      switch (Quest.type) {
        case 1:
        default:
          item = Dungeon.hero.getBelongings().getItem(CorpseDust.class);
          break;
        case 2:
          item = Dungeon.hero.getBelongings().getItem(Embers.class);
          break;
        case 3:
          item = Dungeon.hero.getBelongings().getItem(Rotberry.Seed.class);
          break;
      }

      if (item != null) {
        GameScene.show(new WndWandmaker(this, item));
      } else {
        String msg = "";
        switch (Quest.type) {
          case 1:
            msg = Messages.get(this, "reminder_dust", Dungeon.hero.givenName());
            break;
          case 2:
            msg = Messages.get(this, "reminder_ember", Dungeon.hero.givenName
                    ());
            break;
          case 3:
            msg = Messages.get(this, "reminder_berry", Dungeon.hero.givenName
                    ());
            break;
        }
        GameScene.show(new WndQuest(this, msg));
      }

    } else {

      String msg1 = "";
      String msg2 = "";
      switch (Dungeon.hero.getHeroClass()) {
        case WARRIOR:
          msg1 += Messages.get(this, "intro_warrior");
          break;
        case ROGUE:
          msg1 += Messages.get(this, "intro_rogue");
          break;
        case MAGE:
          msg1 += Messages.get(this, "intro_mage", Dungeon.hero.givenName());
          break;
        case HUNTRESS:
          msg1 += Messages.get(this, "intro_huntress");
          break;
        case SORCERESS:
          msg1 += Messages.get(this, "intro_sorceress");
      }

      msg1 += Messages.get(this, "intro_1");

      switch (Quest.type) {
        case 1:
          msg2 += Messages.get(this, "intro_dust");
          break;
        case 2:
          msg2 += Messages.get(this, "intro_ember");
          break;
        case 3:
          msg2 += Messages.get(this, "intro_berry");
          break;
      }

      msg2 += Messages.get(this, "intro_2");
      final String msg2final = msg2;
      final NPC wandmaker = this;

      GameScene.show(new WndQuest(wandmaker, msg1) {
        @Override
        public void hide() {
          super.hide();
          GameScene.show(new WndQuest(wandmaker, msg2final));
        }
      });

      Journal.INSTANCE.add(name);
      Quest.given = true;
    }

    return false;
  }

  public static class Quest {

    private static int type;
    // 1 = corpse dust quest
    // 2 = elemental embers quest
    // 3 = rotberry quest

    private static boolean spawned;

    private static boolean given;

    public static Wand wand1;
    public static Wand wand2;

    public static void reset() {
      spawned = false;
      type = 0;

      wand1 = null;
      wand2 = null;
    }

    private static final String NODE = "wandmaker";

    private static final String SPAWNED = "spawned";
    private static final String TYPE = "type";
    private static final String GIVEN = "given";
    private static final String WAND1 = "wand1";
    private static final String WAND2 = "wand2";

    private static final String RITUALPOS = "ritualpos";

    public static void storeInBundle(Bundle bundle) {

      Bundle node = new Bundle();

      node.put(SPAWNED, spawned);

      if (spawned) {

        node.put(TYPE, type);

        node.put(GIVEN, given);

        node.put(WAND1, wand1);
        node.put(WAND2, wand2);

        if (type == 2) {
          node.put(RITUALPOS, CeremonialCandle.Companion.getRitualPos());
        }

      }

      bundle.put(NODE, node);
    }

    public static void restoreFromBundle(Bundle bundle) {

      Bundle node = bundle.getBundle(NODE);

      if (!node.isNull() && (spawned = node.getBoolean(SPAWNED))) {

        //TODO remove when pre-0.3.2 saves are no longer supported
        if (node.contains(TYPE)) {
          type = node.getInt(TYPE);
        } else {
          type = node.getBoolean("alternative") ? 1 : 3;
        }

        given = node.getBoolean(GIVEN);

        wand1 = (Wand) node.get(WAND1);
        wand2 = (Wand) node.get(WAND2);

        if (type == 2) {
          CeremonialCandle.Companion.setRitualPos(node.getInt(RITUALPOS));
        }

      } else {
        reset();
      }
    }

    // new spawn function, in two stages!
    public static Digger GiveDigger() {
      if (!spawned && (type != 0 || (Dungeon.depth > 6 && Random.Int(10 -
              Dungeon.depth) == 0))) {
        // now spawn
        if (type == 0)
          type = Random.Int(
                  Dungeon.isChallenged(Challenges.NO_HERBALISM) ? 2 : 3) + 1;

        // give digger
        switch (type) {
          case 1:
            return new MassGraveDigger();
          case 2:
            return new RitualSiteDigger();
          case 3:
            return new RotGardenDigger();
          default:
            return null;
        }

        // remember to add wand maker outside!
      }

      return null;
    }

    public static void Spawn(Level level, Rect rect) {
      Wandmaker w = new Wandmaker();
      do {
        w.pos = level.pointToCell(rect.random(0));
      } while (level.getMap()[w.pos] == Terrain.ENTRANCE ||
              (Terrain.flags[level.getMap()[w.pos]] & Terrain.PASSABLE) == 0);
      level.getMobs().add(w);

      spawned = true;
      given = false;
      wand1 = (Wand) Generator.WAND.INSTANCE.generate();
      wand1.cursed = false;
      wand1.identify();
      wand1.upgrade();

      do {
        wand2 = (Wand) Generator.WAND.INSTANCE.generate();
      } while (wand2.getClass().equals(wand1.getClass()));
      wand2.cursed = false;
      wand2.identify();
      wand2.upgrade();
    }

    //
    public static void complete() {
      wand1 = null;
      wand2 = null;

      Journal.INSTANCE.remove(Messages.get(Wandmaker.class, "name"));
    }
  }
}
