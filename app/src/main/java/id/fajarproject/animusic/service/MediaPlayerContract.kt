package id.fajarproject.animusic.service

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Intent
import android.graphics.drawable.Drawable
import id.fajarproject.animusic.utils.PlaybackStatus


/**
 * Created by Fajar Adi Prasetyo on 11/08/2020.
 */

class MediaPlayerContract {
    interface View {
        fun injectDependency()
        fun initMediaPlayer()
        fun playMedia()
        fun stopMedia()
        fun pauseMedia()
        fun resumeMedia()
        fun callStateListener()
        fun requestAudioFocus(): Boolean
        fun removeAudioFocus(): Boolean
        val playNewAudio: BroadcastReceiver
        fun registerBecomingNoisyReceiver()
        fun registerPlayNewAudio()
        fun initMediaSession()
        fun updateMetaData()
        fun skipToNext()
        fun skipToPrevious()
        fun buildNotification(playbackStatus: PlaybackStatus)
        fun removeNotification()
        fun handleIncomingActions(playbackAction: Intent?)
        fun playbackAction(actionNumber: Int): PendingIntent?
        fun setOnCallbackMedia(callback: Callback)
        val duration : Int
        val currentPosition : Int
        val isPlaying : Boolean
        val styleMusic : Int
        fun seekTo(position: Int)
        fun setOnAction(actionString: String)
        fun setIconStyle(id : Int) : Int
        fun styleMusic()
        fun saveStyle(style: Int)
        fun setIconLike(playbackStatus: PlaybackStatus) : Int
        fun setLikeAction(playbackStatus: PlaybackStatus)
    }

    interface Presenter {
        fun attach(view: View)
    }

    interface Callback{
        fun onPrepared()
        fun onComplete()
        fun changeMusic(position : Int)
        fun onAction(playbackStatus: PlaybackStatus)
    }
}