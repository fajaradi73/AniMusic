package id.fajarproject.animusic.ui.favorite

import id.fajarproject.animusic.data.network.model.MusicItem
import id.fajarproject.animusic.ui.base.BasePresenter
import java.util.*


/**
 * Created by Fajar Adi Prasetyo on 24/08/2020.
 */

class FavoritePresenter<V : FavoriteContract.View> : BasePresenter<V>(),
    FavoriteContract.Presenter<V> {

    override fun sortDate() = Comparator<MusicItem?> { o1, o2 ->
        return@Comparator o1?.createDate?.let { o2?.createDate?.compareTo(it) } ?: 0
    }

}