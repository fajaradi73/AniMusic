package id.fajarproject.animusic.di.component

import dagger.Component
import id.fajarproject.animusic.di.module.ServiceModule
import id.fajarproject.animusic.di.scope.PerService
import id.fajarproject.animusic.service.MediaPlayerService


/**
 * Created by Fajar Adi Prasetyo on 12/08/2020.
 */

@PerService
@Component(dependencies = [ApplicationComponent::class],modules = [ServiceModule::class])
interface ServiceComponent {
    fun inject(mediaPlayerService: MediaPlayerService)
}