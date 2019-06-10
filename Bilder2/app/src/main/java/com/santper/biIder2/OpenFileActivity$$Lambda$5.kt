package com.santper.biIder2

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.EditText

internal /* synthetic */ class `OpenFileActivity$$Lambda$5`(private val `arg$1`: OpenActivity, private val `arg$2`: Bundle, private val `arg$3`: EditText, private val `arg$4`: Context) : OnClickListener {

    override fun onClick(view: View) {
        this.`arg$1`.open(this.`arg$2`, this.`arg$3`, this.`arg$4`)
    }
}
