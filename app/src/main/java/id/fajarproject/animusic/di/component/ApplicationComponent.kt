package id.fajarproject.animusic.di.component

import dagger.Component
import id.fajarproject.animusic.App
import id.fajarproject.animusic.di.module.ApplicationModule


/**
 * Created by Fajar Adi Prasetyo on 05/08/2020.
 */

@Component(modules = [ApplicationModule::class])
interface ApplicationComponent {
    fun inject(app: App)
}