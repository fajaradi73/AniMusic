package id.fajarproject.animusic.di.component

import dagger.Component
import id.fajarproject.animusic.di.module.FragmentModule
import id.fajarproject.animusic.di.scope.PerFragment
import id.fajarproject.animusic.ui.download.DownloadFragment
import id.fajarproject.animusic.ui.favorite.FavoriteFragment
import id.fajarproject.animusic.ui.online.OnlineFragment
import id.fajarproject.animusic.ui.settings.SettingsFragment


/**
 * Created by Fajar Adi Prasetyo on 14/08/2020.
 */

@PerFragment
@Component(dependencies = [ApplicationComponent::class], modules = [FragmentModule::class])
interface FragmentComponent {
    fun inject(onlineFragment: OnlineFragment)
    fun inject(downloadFragment: DownloadFragment)
    fun inject(favoriteFragment: FavoriteFragment)
    fun inject(settingsFragment: SettingsFragment)
}