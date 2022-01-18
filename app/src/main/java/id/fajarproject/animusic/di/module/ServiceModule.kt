package id.fajarproject.animusic.di.module

import android.app.Service
import dagger.Module
import dagger.Provides
import id.fajarproject.animusic.service.MediaPlayerContract
import id.fajarproject.animusic.service.MediaPlayerPresenter


/**
 * Created by Fajar Adi Prasetyo on 12/08/2020.
 */

@Module
class ServiceModule(val service: Service) {

    @Provides
    fun provideService(): Service {
        return service
    }

    @Provides
    fun providePresenterService(): MediaPlayerContract.Presenter {
        return MediaPlayerPresenter(service)
    }
}