package app.truyencuoidangian.fragment

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.bluetooth.BluetoothHealthAppConfiguration
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.truyencuoidangian.MainActivity
import app.truyencuoidangian.R
import app.truyencuoidangian.repository.AppSetting
import io.paperdb.Paper
import kotlinx.android.synthetic.main.dialog_setting.*
import java.lang.ref.WeakReference


class SettingDialog : DialogFragment() {
    companion object {
        var sInstance: WeakReference<SettingDialog>? = null
    }

    lateinit var setting: AppSetting
    val size = MutableLiveData<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sInstance = WeakReference(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_setting, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        size.observe(this, Observer {
            tv_size.text = it.toString()
            setting.textSize = it!!
        })

        setting = Paper.book().read("SETTING")
        size.value = setting.textSize

        bt_text_color.setOnClickListener {
            MainActivity.sInstance!!.showColorDialog(234)
        }
        bt_bg_color.setOnClickListener {
            MainActivity.sInstance!!.showColorDialog(123)
        }

        bt_minus.setOnClickListener {
            if (size.value!! > 12) {
                size.value = size.value!! - 1
            }
        }

        bt_plus.setOnClickListener {
            if (size.value!! < 32) {
                size.value = size.value!! + 1
            }
        }

        bt_ok.setOnClickListener {
            Paper.book().write("SETTING", setting)
            MainActivity.sInstance!!.triggerReload()
            dismiss()
        }
        iv_bg_color.background = createCircleDrawable(setting.backgroundColor)
        iv_text_color.background = createCircleDrawable(setting.textColor)
    }

    fun setBgColor(@ColorInt color: Int) {
        iv_bg_color.background = createCircleDrawable(color)
        setting.backgroundColor = color
    }

    fun setTextColor(@ColorInt color: Int) {
        iv_text_color.background = createCircleDrawable(color)
        setting.textColor = color
    }

    fun createCircleDrawable(@ColorInt fillColor: Int): Drawable {
        val strokeWidth = 2
        val strokeColor = Color.parseColor("#8b8a8f")
        val gD = GradientDrawable()
        gD.setColor(fillColor)
        gD.shape = GradientDrawable.OVAL
        gD.setStroke(strokeWidth, strokeColor)
        return gD
    }
}