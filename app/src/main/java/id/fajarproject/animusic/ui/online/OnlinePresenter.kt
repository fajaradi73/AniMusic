package id.fajarproject.animusic.ui.online

import id.fajarproject.animusic.data.network.model.MusicModel
import id.fajarproject.animusic.ui.base.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


/**
 * Created by Fajar Adi Prasetyo on 14/08/2020.
 */

class OnlinePresenter<V : OnlineContract.View> : BasePresenter<V>(), OnlineContract.Presenter<V> {

    override fun loadData() {
        view?.showLoading()
        val subscribe = api.getMusic().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ data: MusicModel ->
                view?.hideLoading()
                view?.showDataSuccess(data.music ?: arrayListOf())
            }, { error ->
                view?.hideLoading()
                view?.showDataFailed(error.message ?: "")
            })

        subscriptions.add(subscribe)
    }

}