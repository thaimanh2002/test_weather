package com.htnguyen.weatherapp

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.htnguyen.weatherapp.support.InputMethodManager
import com.htnguyen.weatherapp.support.hideKeyboard

abstract class BaseActivity<T : ViewDataBinding> : AppCompatActivity() {
    abstract val layout: Int
    lateinit var binding: T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, layout)
        binding.lifecycleOwner = this@BaseActivity
    }

    fun bindHideKeyboardListener(vararg view: View) {
        val input = InputMethodManager(this) ?: return
        for (v in view) {
            v.setOnClickListener { input.hideKeyboard(it) }
            v.isClickable = true
        }
    }

}