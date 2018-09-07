package app.truyencuoidangian.fragment

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sInstance = WeakReference(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_setting, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setting = Paper.book().read("SETTING")
        bt_text_color.setOnClickListener {
            MainActivity.sInstance!!.showColorDialog(234)
        }
        bt_bg_color.setOnClickListener {
            MainActivity.sInstance!!.showColorDialog(123)
        }
        bt_ok.setOnClickListener {
            Paper.book().write("SETTING", setting)
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
        val strokeWidth = 5
        val strokeColor = Color.parseColor("#000000")
        val gD = GradientDrawable()
        gD.setColor(fillColor)
        gD.shape = GradientDrawable.OVAL
        gD.setStroke(strokeWidth, strokeColor)
        return gD
    }
}