package id.fajarproject.animusic.ui.base

import id.fajarproject.animusic.data.network.ApiServiceInterface
import io.reactivex.disposables.CompositeDisposable


/**
 * Created by Fajar Adi Prasetyo on 05/08/2020.
 */

open class BasePresenter<V> : BaseContract.Presenter<V> {
    val subscriptions = CompositeDisposable()
    val api: ApiServiceInterface = ApiServiceInterface.create()

    var view: V? = null

    override fun attach(view: V) {
        this.view = view
    }
}