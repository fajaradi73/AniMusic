package id.fajarproject.animusic.ui.base

import id.fajarproject.animusic.ui.customView.OnItemClickListener


/**
 * Created by Fajar Adi Prasetyo on 05/08/2020.
 */

class BaseContract {
    interface Presenter<in V> {
        fun attach(view: V)
    }

    interface View {
        fun setToolbar()
        fun setUI()
        fun showLoading()
        fun hideLoading()
    }


    interface Holder<M> {
        fun setOnItemClickListener(onItemClickListener: OnItemClickListener?)
        fun getItem(position: Int): M
    }
}