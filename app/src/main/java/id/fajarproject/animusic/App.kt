package id.fajarproject.animusic

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import id.fajarproject.animusic.di.component.ApplicationComponent
import id.fajarproject.animusic.di.component.DaggerApplicationComponent
import id.fajarproject.animusic.di.module.ApplicationModule
import id.fajarproject.animusic.utils.Constant
import io.realm.Realm
import io.realm.RealmConfiguration


/**
 * Created by Fajar Adi Prasetyo on 05/08/2020.
 */
class App : Application() {

    lateinit var component: ApplicationComponent

    override fun onCreate() {
        super.onCreate()
        setup()
        Realm.init(this)
        val realmConfig = RealmConfiguration.Builder()
            .deleteRealmIfMigrationNeeded()
            .build()
        Realm.setDefaultConfiguration(realmConfig)
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constant.ChannelID,
                Constant.ChannelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun setup() {
        component = DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(this)).build()
        component.inject(this)
    }

    fun getApplicationComponent(): ApplicationComponent {
        return component
    }

    companion object {
        lateinit var instance: App private set
    }
}