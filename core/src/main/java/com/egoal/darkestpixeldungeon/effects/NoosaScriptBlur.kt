package com.egoal.darkestpixeldungeon.effects

import com.watabou.glscripts.Script
import com.watabou.noosa.NoosaScript

class NoosaScriptBlur : NoosaScript() {

    override fun shader(): String = SHADER

    companion object {

        fun Get(): NoosaScriptBlur = Script.use(NoosaScriptBlur::class.java)

        private const val SHADER = 
                "uniform mat4 uCamera;" +
                "uniform mat4 uModel;" +
                "attribute vec4 aXYZW;" +
                "attribute vec2 aUV;" +
                "varying vec2 vUV;" +
                "void main(){" +
                "    gl_Position = uCamera* uModel* aXYZW;" +
                "    vUV = aUV;" +
                "}//\n" +

                "varying mediump vec2 vUV;" +
                "uniform lowp sampler2D uTex;" +
                "uniform lowp vec4 uColorM;" +
                "uniform lowp vec4 uColorA;" +
                "void main() {" +
                "    vec4 s0, s1, s2, s3;" +
                "    float fs = 0.01;" +
                "    s0 = texture2D(uTex, vec2(vUV.x- fs, vUV.y- fs));" +
                "    s1 = texture2D(uTex, vec2(vUV.x+ fs, vUV.y- fs));" +
                "    s2 = texture2D(uTex, vec2(vUV.x+ fs, vUV.y+ fs));" +
                "    s3 = texture2D(uTex, vec2(vUV.x- fs, vUV.y+ fs));" +
                "    vec4 color = (s0+ s1+ s2+ s3)/ 4.0f;" +
                "  gl_FragColor = texture2D(uTex, vUV) * uColorM + uColorA;" +
                "}"
    }

}