package com.egoal.darkestpixeldungeon.messages

// shorthand for Message, may rework Messages someday.

object M {
    // line, capitalize, title case
    fun L(key: String, vararg args: Any): String = Messages.get(key, *args)

    fun L(cls: Class<*>, key: String, vararg args: Any) = Messages.get(cls, key, *args)
    fun L(obj: Any, key: String, vararg args: Any) = L(obj.javaClass, key, *args)

    fun C(string: String): String = string.capitalize()

    fun T(string: String): String = Messages.titleCase(string)

    fun CL(key: String, vararg args: Any): String = C(L(key, *args))
    fun CL(cls: Class<*>, key: String, vararg args: Any) = C(L(cls, key, *args))
    fun CL(obj: Any, key: String, vararg args: Any): String = C(L(obj.javaClass, key, *args))

    fun TL(key: String, vararg args: Any): String = T(L(key, *args))
    fun TL(cls: Class<*>, key: String, vararg args: Any) = T(L(cls, key, *args))
    fun TL(obj: Any, key: String, vararg args: Any): String = T(L(obj.javaClass, key, *args))
}
