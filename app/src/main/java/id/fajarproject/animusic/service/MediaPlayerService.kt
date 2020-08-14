package id.fajarproject.animusic.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.media.MediaPlayer
import android.media.MediaPlayer.*
import android.media.session.MediaSessionManager
import android.os.*
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import android.view.KeyEvent
import androidx.core.app.NotificationCompat
import androidx.media.session.MediaButtonReceiver
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import id.fajarproject.animusic.App
import id.fajarproject.animusic.R
import id.fajarproject.animusic.data.network.model.MusicItem
import id.fajarproject.animusic.data.pref.AppPreference
import id.fajarproject.animusic.data.pref.StoragePreference
import id.fajarproject.animusic.di.component.DaggerServiceComponent
import id.fajarproject.animusic.di.component.ServiceComponent
import id.fajarproject.animusic.di.module.ServiceModule
import id.fajarproject.animusic.ui.home.HomeActivity
import id.fajarproject.animusic.utils.*
import java.io.IOException
import javax.inject.Inject


/**
 * Created by Fajar Adi Prasetyo on 11/08/2020.
 */

class MediaPlayerService : Service(),MediaPlayerContract.View, OnCompletionListener, OnPreparedListener,
    OnErrorListener, OnSeekCompleteListener, OnInfoListener,
    OnBufferingUpdateListener, OnAudioFocusChangeListener {

    @Inject lateinit var presenter: MediaPlayerContract.Presenter

    private var mediaPlayer: MediaPlayer? = null

    //Used to pause/resume MediaPlayer
    private var resumePosition = 0

    private var audioManager: AudioManager? = null

    //Handle incoming phone calls
    private var ongoingCall = false
    private var phoneStateListener: PhoneStateListener? = null
    private var telephonyManager: TelephonyManager? = null

    //List of available Audio files
    var audioList: MutableList<MusicItem?>? = null
    private var audioIndex = -1
    private var activeAudio: MusicItem? = null

    //MediaSession
    private var mediaSessionManager: MediaSessionManager? = null
    private var mediaSession: MediaSessionCompat? = null
    private var transportControls: MediaControllerCompat.TransportControls? = null

    //AudioPlayer notification ID
    private val notificationID = 101

    //// Callback activity
    private var callback : MediaPlayerContract.Callback? = null
    private var becomingNoisyReceiver: BroadcastReceiver? = null

    override var styleMusic = StyleMusic.LOOPING

    // Binder given to clients
    private val iBinder: IBinder = LocalBinder()
    override fun onBind(intent: Intent?): IBinder {
        return iBinder
    }

    override fun onCreate() {
        super.onCreate()
        // Perform one-time setup procedures
        injectDependency()
        presenter.attach(this)
        styleMusic  = AppPreference.getIntPreferenceByName(this,Constant.styleMusic)
        // Manage incoming phone calls during playback.
        // Pause MediaPlayer on incoming call,
        // Resume on hangup.
        callStateListener()
        //ACTION_AUDIO_BECOMING_NOISY -- change in audio outputs -- BroadcastReceiver
        registerBecomingNoisyReceiver()
        //Listen for new Audio to play -- BroadcastReceiver
        registerPlayNewAudio()
    }

    override fun injectDependency() {
        val component: ServiceComponent = DaggerServiceComponent.builder()
            .applicationComponent((application as App).getApplicationComponent())
            .serviceModule(ServiceModule(this))
            .build()
        component.inject(this)
    }

    override fun initMediaPlayer() {
        mediaPlayer = MediaPlayer()
        //Set up MediaPlayer event listeners
        mediaPlayer?.setOnCompletionListener(this)
        mediaPlayer?.setOnErrorListener(this)
        mediaPlayer?.setOnPreparedListener(this)
        mediaPlayer?.setOnBufferingUpdateListener(this)
        mediaPlayer?.setOnSeekCompleteListener(this)
        mediaPlayer?.setOnInfoListener(this)
        //Reset so that the MediaPlayer is not pointing to another data source
        mediaPlayer?.reset()
        mediaPlayer?.setAudioAttributes(AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
        try {
            // Set the data source to the mediaFile location
            mediaPlayer?.setDataSource(activeAudio?.linkMp3)
        } catch (e: IOException) {
            e.printStackTrace()
            stopSelf()
        }
        mediaPlayer?.isLooping = styleMusic == StyleMusic.REPEAT

        mediaPlayer?.prepareAsync()
    }

    override fun playMedia() {
        if (mediaPlayer?.isPlaying != true) {
            mediaPlayer?.start()
        }
    }

    override fun stopMedia() {
        if (mediaPlayer == null) return
        if (mediaPlayer?.isPlaying != false) {
            mediaPlayer?.stop()
        }
    }

    override fun pauseMedia() {
        if (mediaPlayer?.isPlaying != false) {
            mediaPlayer?.pause()
            resumePosition = mediaPlayer?.currentPosition ?: 0
        }
    }

    override fun resumeMedia() {
        if (mediaPlayer == null){
            initMediaPlayer()
        }else if (mediaPlayer?.isPlaying != true) {
            mediaPlayer?.seekTo(resumePosition)
            mediaPlayer?.start()
        }
    }

    override fun requestAudioFocus(): Boolean {
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val result = audioManager?.requestAudioFocus(
            this,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        )
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        //Could not gain focus
    }

    override fun removeAudioFocus(): Boolean {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                audioManager?.abandonAudioFocus(this)
    }

    override fun onBufferingUpdate(mp: MediaPlayer?, percent: Int) {
        //Invoked indicating buffering status of
        //a media resource being streamed over the network.
    }

    override fun onCompletion(mp: MediaPlayer?) {
        //Invoked when playback of a media source has completed.
//        stopMedia()
//        //stop the service
//        stopSelf()
        if (styleMusic != StyleMusic.REPEAT){
            callback?.onComplete()
            transportControls?.skipToNext()
        }
    }

    //Handle errors
    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        //Invoked when there has been an error during an asynchronous operation.
        when (what) {
            MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK -> Log.d(
                "MediaPlayer Error",
                "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK $extra"
            )
            MEDIA_ERROR_SERVER_DIED -> Log.d(
                "MediaPlayer Error",
                "MEDIA ERROR SERVER DIED $extra"
            )
            MEDIA_ERROR_UNKNOWN -> Log.d(
                "MediaPlayer Error",
                "MEDIA ERROR UNKNOWN $extra"
            )
        }
        return false
    }

    override fun onInfo(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        //Invoked to communicate some info.
        return false
    }

    override fun onPrepared(mp: MediaPlayer?) {
        //Invoked when the media source is ready for playback.
        playMedia()
        callback?.onPrepared()
    }

    override fun onSeekComplete(mp: MediaPlayer?) {
        //Invoked indicating the completion of a seek operation.
    }

    override fun onAudioFocusChange(focusChange: Int) {
        //Invoked when the audio focus of the system is updated.
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                // resume playback
                if (mediaPlayer == null)
                    initMediaPlayer()
                else if (mediaPlayer?.isPlaying != true)
                    transportControls?.play()
                mediaPlayer?.setVolume(1.0f, 1.0f)
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (mediaPlayer?.isPlaying != false)
                    transportControls?.pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ->
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mediaPlayer?.isPlaying != false)
                    transportControls?.pause()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK ->
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mediaPlayer?.isPlaying != false)
                    mediaPlayer?.setVolume(0.1f, 0.1f)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer != null) {
            stopMedia()
            mediaPlayer?.release()
        }
        removeAudioFocus()
        //Disable the PhoneStateListener
        if (phoneStateListener != null) {
            telephonyManager?.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
        }
        removeNotification()

        //unregister BroadcastReceivers
        unregisterReceiver(becomingNoisyReceiver)
        unregisterReceiver(playNewAudio)

        //clear cached playlist
//        StorageUtil(applicationContext).clearCachedAudioPlaylist()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            //Load data from SharedPreferences
            val storage =
                StoragePreference(applicationContext)
            audioList = storage.loadAudio()
            audioIndex = storage.loadAudioIndex() ?: 0
            if (audioIndex != -1 && audioIndex < audioList?.size ?: 0) {
                //index is in a valid range
                activeAudio = audioList?.get(audioIndex)
            } else {
                stopSelf()
            }
        } catch (e: NullPointerException) {
            stopSelf()
        }

        //Request audio focus
        if (!requestAudioFocus()) {
            //Could not gain focus
            stopSelf()
        }
        if (mediaSessionManager == null) {
            try {
                initMediaSession()
                initMediaPlayer()
            } catch (e: RemoteException) {
                e.printStackTrace()
                stopSelf()
            }
            buildNotification(PlaybackStatus.PLAYING)
        }

        //Handle Intent action from MediaSession.TransportControls
        handleIncomingActions(intent)

        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return super.onStartCommand(intent, flags, startId)
    }

    //Handle incoming phone calls
    override fun callStateListener() {
        // Get the telephony manager
        telephonyManager =
            getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        //Starting listening for PhoneState changes
        phoneStateListener = object : PhoneStateListener() {
            override fun onCallStateChanged(state: Int, incomingNumber: String) {
                when (state) {
                    TelephonyManager.CALL_STATE_OFFHOOK, TelephonyManager.CALL_STATE_RINGING -> if (mediaPlayer != null) {
                        transportControls?.pause()
                        ongoingCall = true
                    }
                    TelephonyManager.CALL_STATE_IDLE ->
                        // Phone idle. Start playing.
                        if (mediaPlayer != null) {
                            if (ongoingCall) {
                                ongoingCall = false
                                transportControls?.play()
                            }
                        }
                }
            }
        }
        // Register the listener with the telephony manager
        // Listen for changes to the device call state.
        telephonyManager?.listen(
            phoneStateListener,
            PhoneStateListener.LISTEN_CALL_STATE
        )
    }

    override fun registerBecomingNoisyReceiver() {
        //register after getting audio focus
        val intentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        becomingNoisyReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                //pause audio on ACTION_AUDIO_BECOMING_NOISY
                pauseMedia()
                buildNotification(PlaybackStatus.PAUSED)
            }
        }

        registerReceiver(becomingNoisyReceiver, intentFilter)
    }

    override val playNewAudio: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            //Get the new media index form SharedPreferences
            audioIndex = StoragePreference(
                applicationContext
            ).loadAudioIndex() ?: 0
            if (audioIndex != -1 && audioIndex < audioList?.size ?: 0) {
                //index is in a valid range
                activeAudio = audioList?.get(audioIndex) ?: MusicItem()
            } else {
                stopSelf()
            }

            //A PLAY_NEW_AUDIO action received
            //reset mediaPlayer to play the new Audio
            stopMedia()
            mediaPlayer?.reset()
            initMediaPlayer()
            updateMetaData()
            buildNotification(PlaybackStatus.PLAYING)
        }
    }

    override fun registerPlayNewAudio() {
        //Register playNewMedia receiver
        val filter = IntentFilter(Constant.Broadcast_PLAY_NEW_AUDIO)
        registerReceiver(playNewAudio, filter)
    }

    @Throws(RemoteException::class)
    override fun initMediaSession() {
        if (mediaSessionManager != null) return
        //mediaSessionManager exists
        mediaSessionManager =
            getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
        // Create a new MediaSession
        mediaSession = MediaSessionCompat(applicationContext, "AudioPlayer")
        //Get MediaSessions transport controls
        transportControls = mediaSession?.controller?.transportControls
        //set MediaSession -> ready to receive media commands
        mediaSession?.isActive = true
        //indicate that the MediaSession handles transport control commands
        // through its MediaSessionCompat.Callback.
        mediaSession?.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
        mediaSession?.setMediaButtonReceiver(null)
        //Set mediaSession's MetaData
        updateMetaData()

        // Attach Callback to receive MediaSession updates
        mediaSession?.setCallback(object : MediaSessionCompat.Callback() {
            // Implement callbacks
            override fun onPlay() {
                super.onPlay()
                resumeMedia()
                callback?.onAction(PlaybackStatus.PLAYING)
                buildNotification(PlaybackStatus.PLAYING)
            }

            override fun onPause() {
                super.onPause()
                pauseMedia()
                callback?.onAction(PlaybackStatus.PAUSED)
                buildNotification(PlaybackStatus.PAUSED)
            }

            override fun onSkipToNext() {
                super.onSkipToNext()
                skipToNext()
                updateMetaData()
                callback?.onAction(PlaybackStatus.PLAYING)
                buildNotification(PlaybackStatus.PLAYING)
            }

            override fun onSkipToPrevious() {
                super.onSkipToPrevious()
                skipToPrevious()
                updateMetaData()
                callback?.onAction(PlaybackStatus.PLAYING)
                buildNotification(PlaybackStatus.PLAYING)
            }

            override fun onStop() {
                super.onStop()
                removeNotification()
                //Stop the service
                stopSelf()
                callback?.onAction(PlaybackStatus.STOP)
            }

            override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
                val ke: KeyEvent? = mediaButtonEvent?.getParcelableExtra(Intent.EXTRA_KEY_EVENT)
                if (ke?.action == KeyEvent.ACTION_DOWN){
                    when (ke.keyCode) {
                        KeyEvent.KEYCODE_MEDIA_PLAY -> {
                            transportControls?.pause()
                        }
                        KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                            transportControls?.play()
                        }
                        KeyEvent.KEYCODE_MEDIA_NEXT -> {
                            transportControls?.skipToNext()
                        }
                        KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                            transportControls?.skipToPrevious()
                        }
                    }
                }
                return super.onMediaButtonEvent(mediaButtonEvent)
            }
        })
    }

    override fun updateMetaData() {
        val albumArt = BitmapFactory.decodeResource(
            resources,
            R.drawable.ic_placeholder
        ) //replace with medias albumArt
        // Update the current metadata
        mediaSession?.setMetadata(
            MediaMetadataCompat.Builder()
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, activeAudio?.namaBand)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, activeAudio?.musicCover)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, activeAudio?.judulMusic)
                .build()
        )
    }

    override fun skipToNext() {
        when {
            styleMusic == StyleMusic.RANDOM -> {
                audioIndex  = Util.randomNumber(0,audioList?.size?.minus(1) ?: 0)
                activeAudio = audioList?.get(audioIndex)
            }
            audioIndex == audioList?.size?.minus(1) ?: 0 -> {
                //if last in playlist
                audioIndex  = 0
                activeAudio = audioList?.get(audioIndex)
            }
            else -> {
                //get next in playlist
                activeAudio = audioList?.get(++audioIndex)
            }
        }

        //Update stored index
        StoragePreference(applicationContext).storeAudioIndex(audioIndex)
        stopMedia()
        //reset mediaPlayer
        mediaPlayer?.reset()
        initMediaPlayer()
        callback?.changeMusic(audioIndex)
    }

    override fun skipToPrevious() {
        when {
            styleMusic == StyleMusic.RANDOM -> {
                audioIndex  = Util.randomNumber(0,audioList?.size?.minus(1) ?: 0)
                activeAudio = audioList?.get(audioIndex)
            }
            audioIndex == 0 -> {
                //if first in playlist
                //set index to the last of audioList
                audioIndex  = audioList?.size?.minus(1) ?: 0
                activeAudio = audioList?.get(audioIndex)
            }
            else -> {
                //get previous in playlist
                activeAudio = audioList?.get(--audioIndex)
            }
        }

        //Update stored index
        StoragePreference(applicationContext).storeAudioIndex(audioIndex)
        stopMedia()
        //reset mediaPlayer
        mediaPlayer?.reset()
        initMediaPlayer()
        callback?.changeMusic(audioIndex)
    }

    override fun buildNotification(playbackStatus: PlaybackStatus) {
        var notificationAction = R.drawable.ic_pause_grey //needs to be initialized
        var playPauseAction: PendingIntent? = null

        //Build a new notification according to the current state of the MediaPlayer
        if (playbackStatus === PlaybackStatus.PLAYING) {
            notificationAction = R.drawable.ic_pause_grey
            //create the pause action
            playPauseAction = playbackAction(1)
        } else if (playbackStatus === PlaybackStatus.PAUSED) {
            notificationAction = R.drawable.ic_play_grey
            //create the play action
            playPauseAction = playbackAction(0)
        }

        val likeAction = if (playbackStatus == PlaybackStatus.LIKE){
            playbackAction(7)
        }else{
            playbackAction(6)
        }

        val intent = Intent(this,HomeActivity::class.java)
        intent.flags = (Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT)

        // Create a new Notification
        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this,Constant.ChannelPlay)
            .setShowWhen(false)
            .setAutoCancel(false)
            .setSmallIcon(R.drawable.ic_headphone) // Set Notification content information
            .setContentText(activeAudio?.namaBand)
            .setContentTitle(activeAudio?.judulMusic?.replace(".mp3",""))
            .setContentInfo(activeAudio?.musicCover) // Add playback actions
            .addAction(setIconStyle(styleMusic),"style",playbackAction(5))
            .addAction(R.drawable.ic_previous_grey, "previous", playbackAction(3))
            .addAction(notificationAction, "pause", playPauseAction)
            .addAction(
                R.drawable.ic_next_grey,
                "next",
                playbackAction(2)
            )
            .addAction(setIconLike(playbackStatus),"like",likeAction)
            .setContentIntent(pendingIntent)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(1,2,3)
                .setShowCancelButton(true)
                .setCancelButtonIntent(playbackAction(4))
                .setMediaSession(mediaSession?.sessionToken))
        var mChannel: NotificationChannel? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            mChannel = NotificationChannel(Constant.ChannelPlay, Constant.ChannelPlay, importance)
            mChannel.setShowBadge(true)
            mChannel.setSound(null,null)
            notificationBuilder.setChannelId(Constant.ChannelPlay)
        }

        val mNotifyMgr =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mChannel?.let { mNotifyMgr.createNotificationChannel(it) }
        }

        Glide.with(applicationContext)
            .asBitmap()
            .load(Constant.BaseUrlImage + activeAudio?.cover)
            .error(R.drawable.ic_placeholder)
            .into(object : CustomTarget<Bitmap>(){
                override fun onLoadCleared(placeholder: Drawable?) {
                }

                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    notificationBuilder.setLargeIcon(resource)
                    startForeground(2,notificationBuilder.build())
                    if (playbackStatus == PlaybackStatus.PAUSED){
                        Handler().postDelayed({
                            if (mediaPlayer?.isPlaying != true) {
                                stopForeground(false)
                            }
                        },5000)
                    }
                }
            })
    }

    override fun setIconStyle(id: Int): Int {
        return when(id) {
            StyleMusic.REPEAT -> {
                R.drawable.ic_repeat_grey
            }
            StyleMusic.RANDOM -> {
                R.drawable.ic_random_grey
            }
            else -> {
                R.drawable.ic_looping_grey
            }
        }
    }

    override fun setIconLike(playbackStatus: PlaybackStatus): Int {
        return if (playbackStatus == PlaybackStatus.LIKE){
            R.drawable.ic_like_grey
        }else{
            R.drawable.ic_unlike_grey
        }
    }

    override fun styleMusic() {
        when(styleMusic) {
            StyleMusic.LOOPING -> {
                saveStyle(StyleMusic.RANDOM)
                buildNotification(PlaybackStatus.STYLE)
                callback?.onAction(PlaybackStatus.STYLE)
                mediaPlayer?.isLooping = false
            }
            StyleMusic.RANDOM -> {
                saveStyle(StyleMusic.REPEAT)
                buildNotification(PlaybackStatus.STYLE)
                callback?.onAction(PlaybackStatus.STYLE)
                mediaPlayer?.isLooping = true
            }
            else -> {
                saveStyle(StyleMusic.LOOPING)
                mediaPlayer?.isLooping = false
                buildNotification(PlaybackStatus.STYLE)
                callback?.onAction(PlaybackStatus.STYLE)
            }
        }
    }

    override fun saveStyle(style: Int) {
        styleMusic = style
        AppPreference.writePreference(this,Constant.styleMusic,style)
    }

    override fun removeNotification() {
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationID)
    }

    override fun playbackAction(actionNumber: Int): PendingIntent? {
        val playbackAction = Intent(this, MediaPlayerService::class.java)
        when (actionNumber) {
            0 -> {
                // Play
                playbackAction.action = Constant.ACTION_PLAY
                return PendingIntent.getService(this, actionNumber, playbackAction, 0)
            }
            1 -> {
                // Pause
                playbackAction.action = Constant.ACTION_PAUSE
                return PendingIntent.getService(this, actionNumber, playbackAction, 0)
            }
            2 -> {
                // Next track
                playbackAction.action = Constant.ACTION_NEXT
                return PendingIntent.getService(this, actionNumber, playbackAction, 0)
            }
            3 -> {
                // Previous track
                playbackAction.action = Constant.ACTION_PREVIOUS
                return PendingIntent.getService(this, actionNumber, playbackAction, 0)
            }
            4 -> {
                // Stop
                playbackAction.action = Constant.ACTION_STOP
                return PendingIntent.getService(this, actionNumber, playbackAction, 0)
            }
            5 -> {
                // Style Music
                playbackAction.action = Constant.ACTION_STYLE
                return PendingIntent.getService(this, actionNumber, playbackAction, 0)
            }
            6 -> {
                // Like
                playbackAction.action = Constant.ACTION_LIKE
                return PendingIntent.getService(this, actionNumber, playbackAction, 0)
            }
            7 -> {
                // Like
                playbackAction.action = Constant.ACTION_UNLIKE
                return PendingIntent.getService(this, actionNumber, playbackAction, 0)
            }
            else -> {
            }
        }
        return null
    }

    override fun handleIncomingActions(playbackAction: Intent?) {
        if (playbackAction == null || playbackAction.action == null) return
        val actionString = playbackAction.action
        when {
            actionString.equals(Constant.ACTION_PLAY, ignoreCase = true) -> {
                transportControls?.play()
            }
            actionString.equals(Constant.ACTION_PAUSE, ignoreCase = true) -> {
                transportControls?.pause()
            }
            actionString.equals(Constant.ACTION_NEXT, ignoreCase = true) -> {
                transportControls?.skipToNext()
            }
            actionString.equals(Constant.ACTION_PREVIOUS, ignoreCase = true) -> {
                transportControls?.skipToPrevious()
            }
            actionString.equals(Constant.ACTION_STOP, ignoreCase = true) -> {
                transportControls?.stop()
            }
            actionString.equals(Constant.ACTION_STYLE,ignoreCase = true) -> {
                styleMusic()
            }
            actionString.equals(Constant.ACTION_LIKE,ignoreCase = true) -> {
                setLikeAction(PlaybackStatus.LIKE)
            }
            actionString.equals(Constant.ACTION_UNLIKE,ignoreCase = true) -> {
                setLikeAction(PlaybackStatus.UNLIKE)
            }
        }
    }

    override fun setLikeAction(playbackStatus: PlaybackStatus) {
        if (playbackStatus == PlaybackStatus.LIKE){
            buildNotification(PlaybackStatus.LIKE)
        }else{
            buildNotification(PlaybackStatus.UNLIKE)
        }
        callback?.onAction(playbackStatus)
    }

    override fun setOnCallbackMedia(callback: MediaPlayerContract.Callback) {
        this.callback = callback
    }

    override fun setOnAction(actionString: String) {
        when {
            actionString.equals(Constant.ACTION_PLAY, ignoreCase = true) -> {
                transportControls?.play()
            }
            actionString.equals(Constant.ACTION_PAUSE, ignoreCase = true) -> {
                transportControls?.pause()
            }
            actionString.equals(Constant.ACTION_NEXT, ignoreCase = true) -> {
                transportControls?.skipToNext()
            }
            actionString.equals(Constant.ACTION_PREVIOUS, ignoreCase = true) -> {
                transportControls?.skipToPrevious()
            }
            actionString.equals(Constant.ACTION_STOP, ignoreCase = true) -> {
                transportControls?.stop()
            }
            actionString.equals(Constant.ACTION_STYLE,ignoreCase = true) -> {
                styleMusic()
            }
            actionString.equals(Constant.ACTION_LIKE,ignoreCase = true) -> {
                setLikeAction(PlaybackStatus.LIKE)
            }
            actionString.equals(Constant.ACTION_UNLIKE,ignoreCase = true) -> {
                setLikeAction(PlaybackStatus.UNLIKE)
            }
        }
    }

    override fun seekTo(position: Int) {
        resumePosition = position
        mediaPlayer?.seekTo(resumePosition)
    }

    override val duration: Int
        get() = mediaPlayer?.duration ?: 0

    override val currentPosition: Int
        get() = mediaPlayer?.currentPosition ?: 0

    override val isPlaying: Boolean
        get() = mediaPlayer?.isPlaying ?: false

    inner class LocalBinder : Binder() {
        val service: MediaPlayerService
            get() = this@MediaPlayerService

    }
}