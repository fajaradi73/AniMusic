package id.fajarproject.animusic.ui.online

import id.fajarproject.animusic.data.network.model.MusicItem
import id.fajarproject.animusic.ui.base.BaseContract


/**
 * Created by Fajar Adi Prasetyo on 14/08/2020.
 */

class OnlineContract {

    interface View : BaseContract.View {
        fun showDataSuccess(list: MutableList<MusicItem?>)
        fun showDataFailed(message: String)
        fun checkData()
    }

    interface Presenter<V> : BaseContract.Presenter<V> {
        fun loadData()
    }
}