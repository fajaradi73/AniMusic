package id.fajarproject.animusic.di.module

import android.content.Context
import dagger.Module
import dagger.Provides
import id.fajarproject.animusic.ui.favorite.FavoriteContract
import id.fajarproject.animusic.ui.favorite.FavoritePresenter
import id.fajarproject.animusic.ui.online.OnlineContract
import id.fajarproject.animusic.ui.online.OnlinePresenter


/**
 * Created by Fajar Adi Prasetyo on 14/08/2020.
 */

@Module
class FragmentModule(var context: Context) {

    @Provides
    fun provideContext(): Context {
        return context
    }

    @Provides
    fun provideOnlinePresenter(): OnlineContract.Presenter<OnlineContract.View> {
        return OnlinePresenter()
    }

    @Provides
    fun provideFavoritePresenter(): FavoriteContract.Presenter<FavoriteContract.View> {
        return FavoritePresenter()
    }
}