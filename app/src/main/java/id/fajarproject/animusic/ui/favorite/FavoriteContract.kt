package id.fajarproject.animusic.ui.favorite

import id.fajarproject.animusic.data.network.model.MusicItem
import id.fajarproject.animusic.ui.base.BaseContract
import java.util.Comparator

/**
 * Created by Fajar Adi Prasetyo on 21/08/2020.
 */

class FavoriteContract {
    interface View{
        fun checkData()
        fun showDataSuccess(list: MutableList<MusicItem?>)
        fun showDataFailed(message : String)
        fun showLoading()
        fun hideLoading()
        fun updateFavorite()
    }
    interface Presenter<V> : BaseContract.Presenter<V>{
        fun sortDate() : Comparator<MusicItem?>
    }
}