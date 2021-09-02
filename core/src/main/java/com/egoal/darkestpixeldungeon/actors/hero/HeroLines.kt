package com.egoal.darkestpixeldungeon.actors.hero

import com.egoal.darkestpixeldungeon.messages.M

//todo: refactor, consider configuration
object HeroLines {
    const val DAMN = "damn"
    const val WHAT = "what"
    const val BAD_NOISE = "bad_noise"
    const val DELICIOUS = "delicious"
    const val AWFUL = "awful"
    const val I_MAY_DIE = "i_may_die"
    const val GRIN = "grin"
    const val WHY_NOT_EAT = "why_not_eat"
    const val WHAT_ABOUT_NEXT = "what_about_next"
    const val THIS_IS_IT = "this_is_it"
    const val DIE = "die"
    const val MY_WEAPON_IS_BAD = "my_weapon_is_bad"
    const val USELESS = "useless"
    const val NO_GOLD = "no_gold"
    const val HEADACHE = "headache"
    const val MY_RETRIBUTION = "my_retribution"
    const val SAVED_ME = "saved_me"
    const val NOT_NOW = "not_now"
    const val I_CANT = "icant"

    fun Line(tag: String, vararg args: Any): String = M.L(Hero::class.java, "line_$tag", args)

    fun PushShort(tag: String, vararg args: Any) {
        Push(Line(tag, args))
    }

    fun Push(line: String) {
        lines.add(line)
    }

    // get and clear stack
    fun Poll(): String {
        val line = lines.random()
        lines.clear()
        return line
    }

    private val lines = ArrayList<String>()
}