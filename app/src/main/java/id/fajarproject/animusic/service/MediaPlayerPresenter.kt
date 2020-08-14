package id.fajarproject.animusic.service

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import id.fajarproject.animusic.utils.Constant
import id.fajarproject.animusic.utils.PlaybackStatus


/**
 * Created by Fajar Adi Prasetyo on 12/08/2020.
 */

class MediaPlayerPresenter(val context: Context) : MediaPlayerContract.Presenter {

    private var view : MediaPlayerContract.View? = null


    override fun attach(view: MediaPlayerContract.View) {
        this.view = view
    }


}