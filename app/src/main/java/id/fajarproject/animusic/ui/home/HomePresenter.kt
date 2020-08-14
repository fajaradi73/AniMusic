package id.fajarproject.animusic.ui.home

import id.fajarproject.animusic.ui.base.BasePresenter

/**
 * Created by Fajar Adi Prasetyo on 05/08/2020.
 */

class HomePresenter<V : HomeContract.View> : BasePresenter<V>(),HomeContract.Presenter<V>{

    override fun loadData() {

    }
}