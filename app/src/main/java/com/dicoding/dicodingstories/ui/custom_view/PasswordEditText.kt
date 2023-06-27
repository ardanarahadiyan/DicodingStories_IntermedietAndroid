package com.dicoding.dicodingstories.ui.custom_view

import android.content.Context
import android.graphics.Canvas
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import com.google.android.material.textfield.TextInputEditText

class PasswordEditText : TextInputEditText{

    constructor(context: Context) : super(context) {
        init()
    }
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                error = if (p0.toString().count() < 8) {
                    "Password minimal 8 karakter"
                } else {
                    null
                }
                hint = if (p0.toString().isNotEmpty()){
                    ""
                } else {
                    "Password"
                }

                if (p0.toString().isEmpty()){
                    error = "Password tidak boleh kosong"
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        hint = "Password"
    }

}