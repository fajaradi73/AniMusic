package id.fajarproject.animusic.di.module

import android.app.Application
import dagger.Module
import dagger.Provides
import id.fajarproject.animusic.App
import id.fajarproject.animusic.di.scope.PerApplication
import javax.inject.Singleton


/**
 * Created by Fajar Adi Prasetyo on 05/08/2020.
 */

@Module
class ApplicationModule(private val baseApp: App) {

    @Provides
    @Singleton
    @PerApplication
    fun provideApplication(): Application {
        return baseApp
    }
}