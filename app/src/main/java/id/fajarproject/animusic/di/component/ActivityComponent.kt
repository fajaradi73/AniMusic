package id.fajarproject.animusic.di.component

import dagger.Component
import id.fajarproject.animusic.di.module.ActivityModule
import id.fajarproject.animusic.di.scope.PerActivity
import id.fajarproject.animusic.ui.home.HomeActivity


/**
 * Created by Fajar Adi Prasetyo on 05/08/2020.
 */

@PerActivity
@Component(dependencies = [ApplicationComponent::class],modules = [ActivityModule::class])
interface ActivityComponent {
    fun inject(homeActivity: HomeActivity)
}