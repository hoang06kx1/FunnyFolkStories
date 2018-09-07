package app.truyencuoidangian

import android.app.Application
import android.graphics.Color
import app.truyencuoidangian.repository.AppSetting
import com.google.android.gms.ads.MobileAds
import io.paperdb.Paper

class CustomApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this, if (BuildConfig.DEBUG) debugAdmobAppId else releaseAdmobAppId)
        Paper.init(this)
        if (!Paper.book().contains("SETTING")) {
            Paper.book().write("SETTING", AppSetting(Color.parseColor("#FFFFFF"), Color.parseColor("#000000"), 18))
        }
    }
}