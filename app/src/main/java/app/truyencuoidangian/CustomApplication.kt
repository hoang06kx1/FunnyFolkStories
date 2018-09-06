package app.truyencuoidangian

import android.app.Application
import com.google.android.gms.ads.MobileAds

class CustomApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this, if (BuildConfig.DEBUG) debugAdmobAppId else releaseAdmobAppId)
    }
}