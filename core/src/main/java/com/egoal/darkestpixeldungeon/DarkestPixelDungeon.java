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
package com.egoal.darkestpixeldungeon;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;

import com.egoal.darkestpixeldungeon.messages.Languages;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.scenes.PixelScene;
import com.egoal.darkestpixeldungeon.scenes.WelcomeScene;
import com.watabou.noosa.Game;
import com.watabou.noosa.RenderedText;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;

import javax.microedition.khronos.opengles.GL10;

import java.util.Locale;

public class DarkestPixelDungeon extends Game {

  public DarkestPixelDungeon() {
    super(WelcomeScene.class);

    // we can add alias to compatible with older saves, but no need for me 23333
    // like:
//    com.watabou.utils.Bundle.addAlias(
//            Grim.class,
//            "com.egoal.darkestpixeldungeon.items.weapon.enchantments.Death");
//    com.watabou.utils.Bundle.addAlias(
//            com.egoal.darkestpixeldungeon.items.weapon.enchantments.Blazing.class,
//            "com.egoal.darkestpixeldungeon.items.weapon.enchantments.Fire");
  }

  @SuppressWarnings("deprecation")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler(this));

    updateImmersiveMode();

    DisplayMetrics metrics = new DisplayMetrics();
    instance.getWindowManager().getDefaultDisplay().getMetrics(metrics);
    boolean landscape = metrics.widthPixels > metrics.heightPixels;

    if (Preferences.INSTANCE.getBoolean(Preferences.KEY_LANDSCAPE, false) !=
            landscape) {
      landscape(!landscape);
    }

    Music.INSTANCE.enable(music());
    Sample.INSTANCE.enable(soundFx());
    Sample.INSTANCE.volume(SFXVol() / 10f);

    Sample.INSTANCE.load(
            Assets.SND_CLICK,
            Assets.SND_BADGE,
            Assets.SND_GOLD,

            Assets.SND_STEP,
            Assets.SND_WATER,
            Assets.SND_OPEN,
            Assets.SND_UNLOCK,
            Assets.SND_ITEM,
            Assets.SND_DEWDROP,
            Assets.SND_HIT,
            Assets.SND_MISS,

            Assets.SND_DESCEND,
            Assets.SND_EAT,
            Assets.SND_READ,
            Assets.SND_LULLABY,
            Assets.SND_DRINK,
            Assets.SND_SHATTER,
            Assets.SND_ZAP,
            Assets.SND_LIGHTNING,
            Assets.SND_LEVELUP,
            Assets.SND_DEATH,
            Assets.SND_CHALLENGE,
            Assets.SND_CURSED,
            Assets.SND_EVOKE,
            Assets.SND_TRAP,
            Assets.SND_TOMB,
            Assets.SND_ALERT,
            Assets.SND_MELD,
            Assets.SND_BOSS,
            Assets.SND_BLAST,
            Assets.SND_PLANT,
            Assets.SND_RAY,
            Assets.SND_BEACON,
            Assets.SND_TELEPORT,
            Assets.SND_CHARMS,
            Assets.SND_MASTERY,
            Assets.SND_PUFF,
            Assets.SND_ROCKS,
            Assets.SND_BURNING,
            Assets.SND_FALLING,
            Assets.SND_GHOST,
            Assets.SND_SECRET,
            Assets.SND_BONES,
            Assets.SND_BEE,
            Assets.SND_DEGRADE,
            Assets.SND_MIMIC,

            Assets.SND_ASTROLABE,
            Assets.SND_CRITICAL,
            Assets.SND_RELOAD,
            Assets.SND_HOWL,
            Assets.SND_BLOCK,
            Assets.SND_MOONLIGHT,
            Assets.SND_HIT2);

    if (classicFont()) {
      RenderedText.setFont("pixelfont.ttf");
    } else {
      RenderedText.setFont("font.ttf");
    }
  }

  @Override
  public void onWindowFocusChanged(boolean hasFocus) {

    super.onWindowFocusChanged(hasFocus);

    if (hasFocus) {
      updateImmersiveMode();
    }
  }

  public static void switchNoFade(Class<? extends PixelScene> c) {
    switchNoFade(c, null);
  }

  public static void switchNoFade(Class<? extends PixelScene> c,
                                  SceneChangeCallback callback) {
    PixelScene.noFade = true;
    switchScene(c, callback);
  }

  /*
   * ---> Prefernces
   */

  public static void debug(boolean value) {
    Preferences.INSTANCE.put(Preferences.KEY_DEBUG, value);
  }

  public static boolean debug() {
    return Preferences.INSTANCE.getBoolean(Preferences.KEY_DEBUG, false);
  }

  public static void changeListChecked(boolean value) {
    Preferences.INSTANCE.put(Preferences.KEY_CHANGE_LIST_CHECKED, value);
  }

  public static boolean changeListChecked() {
    return Preferences.INSTANCE.getBoolean(Preferences
            .KEY_CHANGE_LIST_CHECKED, false);
  }

  public static void landscape(boolean value) {
    if (android.os.Build.VERSION.SDK_INT >= 9) {
      Game.instance.setRequestedOrientation(value ?
              ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE :
              ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
    } else {
      Game.instance.setRequestedOrientation(value ?
              ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE :
              ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
    Preferences.INSTANCE.put(Preferences.KEY_LANDSCAPE, value);
  }

  public static boolean landscape() {
    return width > height;
  }

  public static void scale(int value) {
    Preferences.INSTANCE.put(Preferences.KEY_SCALE, value);
  }

  // *** IMMERSIVE MODE ****

  private static boolean immersiveModeChanged = false;

  @SuppressLint("NewApi")
  public static void immerse(boolean value) {
    Preferences.INSTANCE.put(Preferences.KEY_IMMERSIVE, value);

    instance.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        updateImmersiveMode();
        immersiveModeChanged = true;
      }
    });
  }

  @Override
  public void onSurfaceChanged(GL10 gl, int width, int height) {
    super.onSurfaceChanged(gl, width, height);

    if (immersiveModeChanged) {
      requestedReset = true;
      immersiveModeChanged = false;
    }
  }

  @SuppressLint("NewApi")
  public static void updateImmersiveMode() {
    if (android.os.Build.VERSION.SDK_INT >= 19) {
      try {
        // Sometime NullPointerException happens here
        instance.getWindow().getDecorView().setSystemUiVisibility(
                immersed() ?
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                                View.SYSTEM_UI_FLAG_FULLSCREEN |
                                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        :
                        0);
      } catch (Exception e) {
        reportException(e);
      }
    }
  }

  public static boolean immersed() {
    return Preferences.INSTANCE.getBoolean(Preferences.KEY_IMMERSIVE, false);
  }

  // *****************************

  public static int scale() {
    return Preferences.INSTANCE.getInt(Preferences.KEY_SCALE, 0);
  }

  public static void zoom(int value) {
    Preferences.INSTANCE.put(Preferences.KEY_ZOOM, value);
  }

  public static int zoom() {
    return Preferences.INSTANCE.getInt(Preferences.KEY_ZOOM, 0);
  }

  public static void music(boolean value) {
    Music.INSTANCE.enable(value);
    Music.INSTANCE.volume(musicVol() / 10f);
    Preferences.INSTANCE.put(Preferences.KEY_MUSIC, value);
  }

  public static boolean music() {
    return Preferences.INSTANCE.getBoolean(Preferences.KEY_MUSIC, true);
  }

  public static void musicVol(int value) {
    Preferences.INSTANCE.put(Preferences.KEY_MUSIC_VOL, value);
  }

  public static int musicVol() {
    return Preferences.INSTANCE.getInt(Preferences.KEY_MUSIC_VOL, 10, 0, 10);
  }

  public static void soundFx(boolean value) {
    Sample.INSTANCE.enable(value);
    Preferences.INSTANCE.put(Preferences.KEY_SOUND_FX, value);
  }

  public static boolean soundFx() {
    return Preferences.INSTANCE.getBoolean(Preferences.KEY_SOUND_FX, true);
  }

  public static void SFXVol(int value) {
    Preferences.INSTANCE.put(Preferences.KEY_SFX_VOL, value);
  }

  public static int SFXVol() {
    return Preferences.INSTANCE.getInt(Preferences.KEY_SFX_VOL, 10, 0, 10);
  }

  public static void brightness(int value) {
    Preferences.INSTANCE.put(Preferences.KEY_BRIGHTNESS, value);
    GameScene.updateFog();
  }

  public static int brightness() {
    return Preferences.INSTANCE.getInt(Preferences.KEY_BRIGHTNESS, 0, -2, 2);
  }

  public static void language(Languages lang) {
    Preferences.INSTANCE.put(Preferences.KEY_LANG, lang.getCode());
  }

  public static Languages language() {
    String code = Preferences.INSTANCE.getString(Preferences.KEY_LANG, null);
    if (code == null) {
      Languages lang = Languages.Companion.matchLocale(Locale.getDefault());
      if (lang.getStatus() == Languages.Status.REVIEWED)
        return lang;
      else
        return Languages.ENGLISH;
    } else return Languages.Companion.matchCode(code);
  }

  public static void classicFont(boolean classic) {
    Preferences.INSTANCE.put(Preferences.KEY_CLASSICFONT, classic);
    if (classic) {
      RenderedText.setFont("pixelfont.ttf");
    } else {
      RenderedText.setFont("font.ttf");
    }
  }

  public static boolean classicFont() {
//    return Preferences.INSTANCE.getBoolean(Preferences.KEY_CLASSICFONT,
//            (language() != Languages.KOREAN && language() != Languages
//                    .CHINESE));
    return Preferences.INSTANCE.getBoolean(Preferences.KEY_CLASSICFONT, true)
            ; //,
//            language() != Languages.CHINESE);
  }

  public static void lastClass(int value) {
    Preferences.INSTANCE.put(Preferences.KEY_LAST_CLASS, value);
  }

  public static int lastClass() {
    return Preferences.INSTANCE.getInt(Preferences.KEY_LAST_CLASS, 0, 0, 5);
  }

  public static void quickSlots(int value) {
    Preferences.INSTANCE.put(Preferences.KEY_QUICKSLOTS, value);
  }

  public static int quickSlots() {
    return Preferences.INSTANCE.getInt(Preferences.KEY_QUICKSLOTS, 5, 0, 8);
  }

  public static void moreQuickSlots(boolean value){
    Preferences.INSTANCE.put(Preferences.KEY_MORE_SLOTS, value);
  }

  public static boolean moreQuickSlots(){
    return Preferences.INSTANCE.getBoolean(Preferences.KEY_MORE_SLOTS, false);
  }

  public static void flipToolbar(boolean value) {
    Preferences.INSTANCE.put(Preferences.KEY_FLIPTOOLBAR, value);
  }

  public static boolean flipToolbar() {
    return Preferences.INSTANCE.getBoolean(Preferences.KEY_FLIPTOOLBAR, false);
  }

  public static void flipTags(boolean value) {
    Preferences.INSTANCE.put(Preferences.KEY_FLIPTAGS, value);
  }

  public static boolean flipTags() {
    return Preferences.INSTANCE.getBoolean(Preferences.KEY_FLIPTAGS, false);
  }

  public static void toolbarMode(String value) {
    Preferences.INSTANCE.put(Preferences.KEY_BARMODE, value);
  }

  public static String toolbarMode() {
//    return Preferences.INSTANCE.getString(Preferences.KEY_BARMODE, !landscape
//            () ? "SPLIT" : "GROUP");
    return Preferences.INSTANCE.getString(Preferences.KEY_BARMODE, "SPLIT");
  }

  public static void intro(boolean value) {
    Preferences.INSTANCE.put(Preferences.KEY_INTRO, value);
  }

  public static boolean intro() {
    return Preferences.INSTANCE.getBoolean(Preferences.KEY_INTRO, true);
  }

  public static void version(int value) {
    Preferences.INSTANCE.put(Preferences.KEY_VERSION, value);
  }

  public static int version() {
    return Preferences.INSTANCE.getInt(Preferences.KEY_VERSION, 0);
  }

  public static void lastHeroName(String name) { Preferences.INSTANCE.put(Preferences.KEY_HERO_NAME, name); }

  public static String lastHeroName(){ return Preferences.INSTANCE.getString(Preferences.KEY_HERO_NAME, "无名"); }

  /*
   * <--- Preferences
   */

  public static void reportException(Throwable tr) {
    TopExceptionHandler.Companion.WriteErrorFile(tr);
  }
}