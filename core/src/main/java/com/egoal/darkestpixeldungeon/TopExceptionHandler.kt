package com.egoal.darkestpixeldungeon

import android.app.Activity
import android.util.Log
import com.watabou.noosa.Game
import com.watabou.utils.Bundle
import java.io.IOException

class TopExceptionHandler(private val app: Activity) : Thread.UncaughtExceptionHandler {
    private val defaultUEH = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(t: Thread, e: Throwable) {
        WriteErrorFile(e)
        defaultUEH.uncaughtException(t, e)
    }

    companion object {
        //todo: may rework this 
        fun WriteErrorFile(tr: Throwable) {
            val strError = mutableListOf<String>()

            tr.message?.let {
                strError.add("MESSAGE: ")
                strError.add(it)
            }

            // stack
            strError.add("STACK: ")
            for (stk in tr.stackTrace.map { it.toString().trim { c -> c != '(' && c != ')' } }) {
                strError.add(stk)
                if (strError.size >= 20) break
            }

            val bundle = Bundle().apply {
                put(STR_ERROR, strError.toTypedArray()) // Log.getStackTraceString(tr))
            }
            try {
                val fout = Game.instance.openFileOutput(ERROR_FILE, Game.MODE_PRIVATE)
                Bundle.write(bundle, fout)
                fout.close()
            } catch (ignored: IOException) {
            }

            Log.e("dpd", Log.getStackTraceString(tr))
        }

        fun LoadErrorString(): String? = try {
            val fin = Game.instance.openFileInput(ERROR_FILE)
            val bundle = Bundle.read(fin)
            fin.close()

            bundle.getString(STR_ERROR)
        } catch (e: IOException) {
            null
        }

        fun LoadErrorStrings(): Array<String>? = try {
            val fin = Game.instance.openFileInput(ERROR_FILE)
            val bundle = Bundle.read(fin)
            fin.close()
            bundle.getStringArray(STR_ERROR)
        } catch (e: IOException) {
            null
        }

        fun HasErrorFile(): Boolean = try {
            Game.instance.openFileInput(ERROR_FILE).close()
            true
        } catch (e: IOException) {
            false
        }

        fun DeleteErrorFile() {
            Game.instance.deleteFile(ERROR_FILE)
        }

        private const val ERROR_FILE = "error.dat"
        private const val STR_ERROR = "error"
    }
}