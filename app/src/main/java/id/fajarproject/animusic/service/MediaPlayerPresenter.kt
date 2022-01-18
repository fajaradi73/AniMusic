package id.fajarproject.animusic.service

import android.content.Context


/**
 * Created by Fajar Adi Prasetyo on 12/08/2020.
 */

class MediaPlayerPresenter(val context: Context) : MediaPlayerContract.Presenter {

    private var view: MediaPlayerContract.View? = null


    override fun attach(view: MediaPlayerContract.View) {
        this.view = view
    }


}