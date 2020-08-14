package id.fajarproject.animusic.di.module

import android.app.Activity
import dagger.Module
import dagger.Provides
import id.fajarproject.animusic.ui.home.HomeContract
import id.fajarproject.animusic.ui.home.HomePresenter


/**
 * Created by Fajar Adi Prasetyo on 05/08/2020.
 */

@Module
class ActivityModule (private var activity: Activity) {

    @Provides
    fun provideActivity(): Activity {
        return activity
    }

    @Provides
    fun provideHomePresenter() : HomeContract.Presenter<HomeContract.View>{
        return HomePresenter()
    }
}