package id.fajarproject.animusic.ui.home

import android.animation.ObjectAnimator
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.AudioManager
import android.os.*
import android.view.Gravity
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import id.fajarproject.animusic.R
import id.fajarproject.animusic.data.network.model.MusicItem
import id.fajarproject.animusic.data.pref.AppPreference
import id.fajarproject.animusic.data.pref.StoragePreference
import id.fajarproject.animusic.data.realm.RealmHelper
import id.fajarproject.animusic.service.MediaPlayerContract
import id.fajarproject.animusic.service.MediaPlayerService
import id.fajarproject.animusic.service.MediaPlayerService.LocalBinder
import id.fajarproject.animusic.ui.base.BaseActivity
import id.fajarproject.animusic.ui.download.DownloadFragment
import id.fajarproject.animusic.ui.favorite.FavoriteFragment
import id.fajarproject.animusic.ui.online.OnlineFragment
import id.fajarproject.animusic.ui.settings.SettingsFragment
import id.fajarproject.animusic.utils.Constant
import id.fajarproject.animusic.utils.Constant.Broadcast_PLAY_NEW_AUDIO
import id.fajarproject.animusic.utils.PlaybackStatus
import id.fajarproject.animusic.utils.StyleMusic
import id.fajarproject.animusic.utils.Util
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.media_player.*
import javax.inject.Inject

class HomeActivity : BaseActivity(), HomeContract.View {

    @Inject
    lateinit var presenter: HomeContract.Presenter<HomeContract.View>

    private lateinit var audioManager: AudioManager
    private var handler: Handler? = null
    private var styleMusic = 0

    private val mRunnable: Runnable = Runnable { updateProgress() }
    private var player: MediaPlayerService? = null
    private var serviceBound = false
    private var anim: ObjectAnimator? = null
    private var doubleBackToExitPressedOnce = false

    private val fragmentManager = supportFragmentManager
    private var currentFragment: Fragment? = null
    private var previousPosition = -1
    private var listMusic: MutableList<MusicItem?>? = null

    private var realm = Realm.getDefaultInstance()
    private var realmHelper: RealmHelper? = null
    private var idMusic = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        getActivityComponent().inject(this)
        presenter.attach(this)
        setToolbar()
        styleMusic = AppPreference.getIntPreferenceByName(this, Constant.styleMusic)
        styleIcon(styleMusic)

        handler = Handler(Looper.getMainLooper())
        realmHelper = RealmHelper(realm)

        setUI()
        checkLocal()
    }

    override fun checkLocal() {
        val storage =
            StoragePreference(this)
        previousPosition = storage.loadAudioIndex() ?: -1
        listMusic = storage.loadAudio()
        if (listMusic != null && previousPosition != -1) {
            listMusic?.get(previousPosition)?.let {
                setMusicPlayer(it)
                setAction(it)
            }
        }
    }

    override fun setToolbar() {
        setSupportActionBar(toolbar)
    }

    override fun setUI() {
        ///// setUI Bottom Navigation
        nav_view.setOnItemSelectedListener{ item: MenuItem ->
            when (item.itemId) {
                R.id.action_online -> {
                    addFragment(OnlineFragment(), Constant.online)
                    AppPreference.writePreference(activity, Constant.tag, Constant.online)
                    true
                }
                R.id.action_download -> {
                    addFragment(DownloadFragment(), Constant.download)
                    AppPreference.writePreference(activity, Constant.tag, Constant.download)
                    true
                }
                R.id.action_favorite -> {
                    addFragment(FavoriteFragment(), Constant.favorite)
                    AppPreference.writePreference(activity, Constant.tag, Constant.favorite)
                    true
                }
                R.id.action_settings -> {
                    addFragment(SettingsFragment(), Constant.setting)
                    AppPreference.writePreference(activity, Constant.tag, Constant.setting)
                    true
                }
                else -> {
                    false
                }
            }
        }
        setOpenFragment()

        ///// setUi SlideLayout
        slideLayout.panelState = SlidingUpPanelLayout.PanelState.HIDDEN
        slideLayout.addPanelSlideListener(object : SlidingUpPanelLayout.PanelSlideListener {
            override fun onPanelSlide(panel: View?, slideOffset: Float) {
            }

            override fun onPanelStateChanged(
                panel: View?,
                previousState: SlidingUpPanelLayout.PanelState?,
                newState: SlidingUpPanelLayout.PanelState?
            ) {
                if (newState == SlidingUpPanelLayout.PanelState.EXPANDED ||
                    newState == SlidingUpPanelLayout.PanelState.DRAGGING
                ) {
                    setViewExpand()
                } else {
                    slideLayout.panelState = SlidingUpPanelLayout.PanelState.HIDDEN
                }
            }

        })
        cvBackground.setOnClickListener {
            if (player != null || listMusic != null) {
                slideLayout.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
            } else {
                val storageUtil =
                    StoragePreference(activity)
                val list = storageUtil.loadAudio()
                list?.let { item -> playAudio(item, 0) }
            }
        }
        setVolume()
    }

    override fun showLoading() {
    }

    override fun hideLoading() {
    }

    override fun setViewMinimize() {
        loadingMusic.visibility = View.VISIBLE
        clActionMinimize.visibility = View.VISIBLE
        ivMenu.visibility = View.GONE
        ivDown.visibility = View.GONE
        cvBackground.visibility = View.VISIBLE
        judul.gravity = Gravity.CENTER_VERTICAL
        name.gravity = Gravity.CENTER_VERTICAL
    }

    override fun addFragment(fragments: Fragment, tag: String) {

        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()

        var curFrag: Fragment? = null
        if (fragmentManager.primaryNavigationFragment != null) {
            curFrag = fragmentManager.primaryNavigationFragment
        }
        if (curFrag != null) {
            fragmentTransaction.hide(curFrag)
        }

        var fragment: Fragment? =
            fragmentManager.findFragmentByTag(tag)
        if (fragment == null) {
            fragment = fragments
            fragmentTransaction.add(container.id, fragment, tag)
        } else {
            if (tag == Constant.setting) {
                fragmentTransaction.detach(fragment).attach(fragment)
                fragmentTransaction.show(fragment)
            } else {
                fragmentTransaction.show(fragment)
            }
        }
        currentFragment = fragment
        fragmentTransaction.setPrimaryNavigationFragment(fragment)
        fragmentTransaction.commit()
    }

    override fun setOpenFragment() {
        when (AppPreference.getStringPreferenceByName(this, Constant.tag)) {
            Constant.favorite -> {
                nav_view.selectedItemId = R.id.action_favorite
            }
            Constant.download -> {
                nav_view.selectedItemId = R.id.action_download
            }
            Constant.setting -> {
                nav_view.selectedItemId = R.id.action_settings
            }
            else -> {
                nav_view.selectedItemId = R.id.action_online
            }
        }
    }

    override fun setViewExpand() {
        clActionMinimize.visibility = View.GONE
        ivMenu.visibility = View.VISIBLE
        ivDown.visibility = View.VISIBLE
        judul.gravity = Gravity.CENTER
        name.gravity = Gravity.CENTER
    }

    override fun onBackPressed() {
        if (slideLayout != null && (slideLayout.panelState == SlidingUpPanelLayout.PanelState.EXPANDED ||
                    slideLayout.panelState == SlidingUpPanelLayout.PanelState.ANCHORED)
        ) {
            slideLayout.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
        } else {
            if (doubleBackToExitPressedOnce) {
                moveTaskToBack(true)
                return
            }

            this.doubleBackToExitPressedOnce = true
            Toast.makeText(
                activity,
                "Ketuk lagi untuk mendorong apl ke latar belakang",
                Toast.LENGTH_SHORT
            ).show()

            Handler(Looper.getMainLooper()).postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
        }
    }

    override fun setMusicPlayer(item: MusicItem) {
        judul.text = item.judulMusic?.replace(".mp3", "")
        name.text = item.namaBand
        Glide.with(activity)
            .load(Constant.BaseUrlImage + item.cover)
            .error(R.drawable.ic_placeholder)
            .placeholder(Util.circleLoading(activity))
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(ivBackground)

        Glide.with(activity)
            .load(Constant.BaseUrlImage + item.cover)
            .error(R.drawable.ic_placeholder)
            .placeholder(Util.circleLoading(activity))
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(ivMinimize)

        // Favorite Music
        idMusic = item.id ?: -1
        if (realmHelper?.checkData(idMusic) != false)
            setIconLike(Constant.ACTION_LIKE)
        else
            setIconLike(Constant.ACTION_UNLIKE)
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putBoolean("ServiceState", serviceBound)
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        serviceBound = savedInstanceState.getBoolean("ServiceState")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (serviceBound) {
            unbindService(serviceConnection)
            //service is active
            player?.stopSelf()
            handler?.removeCallbacks(mRunnable)
        }
    }

    override fun updateProgress() {
        val mCurrentPosition = player?.currentPosition ?: 0
        sbProgress.setValue(mCurrentPosition.toFloat() / 1000, false)
        loadingMusic.progress = mCurrentPosition / 1000
        timeStart.text = Util.getTime(mCurrentPosition.toLong())
        handler?.postDelayed(mRunnable, 50)
    }

    override fun runPlayer(view: View) {
        if (player != null) {
            when {
                player?.isPlaying != false -> {
                    player?.setOnAction(Constant.ACTION_PAUSE)
                }
                player?.isPlaying != true -> {
                    player?.setOnAction(Constant.ACTION_PLAY)
                    updateProgress()
                }
            }
        } else {
            listMusic?.let { playAudio(it, previousPosition) }
        }
    }

    override fun setVolume() {
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        sbVolume.setValueRange(0, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), true)
        sbVolume.setValue(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat(), true)
        sbVolume.setOnPositionChangeListener { _, _, _, _, _, newValue ->
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newValue, 0)
        }
    }

    override fun setAction(item: MusicItem) {
        ivPrevious.setOnClickListener {
            if (player != null) {
                player?.setOnAction(Constant.ACTION_PREVIOUS)
            } else {
                previousMusic()
            }
        }
        ivPreviousMM.setOnClickListener {
            if (player != null) {
                player?.setOnAction(Constant.ACTION_PREVIOUS)
            } else {
                previousMusic()
            }
        }

        ivNext.setOnClickListener {
            if (player != null) {
                player?.setOnAction(Constant.ACTION_NEXT)
            } else {
                nextMusic()
            }
        }
        ivNextMM.setOnClickListener {
            if (player != null) {
                player?.setOnAction(Constant.ACTION_NEXT)
            } else {
                nextMusic()
            }
        }

        ivStyle.setOnClickListener {
            player?.setOnAction(Constant.ACTION_STYLE)
        }
        ivFavorite.setOnClickListener {
            if (realmHelper?.checkData(idMusic) != false) {
                if (player != null)
                    player?.setOnAction(Constant.ACTION_UNLIKE)
                else
                    realmHelper?.deleteMusic(idMusic)
                setLikeUpdate()
            } else {
                if (player != null)
                    player?.setOnAction(Constant.ACTION_LIKE)
                else
                    realmHelper?.save(item)
                setLikeUpdate()
            }
        }
    }

    override fun setLikeUpdate() {
        if (realmHelper?.checkData(idMusic) != false) {
            setIconLike(Constant.ACTION_LIKE)
            Toast.makeText(this, "Sukses menambahkan lagu ke favorite", Toast.LENGTH_SHORT).show()
        } else {
            setIconLike(Constant.ACTION_UNLIKE)
            Toast.makeText(this, "Sukses menghapus lagu dari favorite", Toast.LENGTH_SHORT).show()
        }
        if (currentFragment?.tag == Constant.favorite) {
            val viewFavorite = currentFragment as FavoriteFragment
            viewFavorite.updateFavorite()
        }
    }

    override fun previousMusic() {
        if (styleMusic == StyleMusic.RANDOM) {
            previousPosition = Util.randomNumber(0, listMusic?.size?.minus(1) ?: 0)
            listMusic?.let { playAudio(it, previousPosition) }
        } else {
            listMusic?.let { playAudio(it, --previousPosition) }
        }
    }

    override fun nextMusic() {
        if (styleMusic == StyleMusic.RANDOM) {
            previousPosition = Util.randomNumber(0, listMusic?.size?.minus(1) ?: 0)
            listMusic?.let { playAudio(it, previousPosition) }
        } else {
            listMusic?.let { playAudio(it, ++previousPosition) }
        }
    }

    override fun styleIcon(styleMusic: Int) {
        when (styleMusic) {
            StyleMusic.LOOPING -> {
                ivStyle.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_looping))
            }
            StyleMusic.RANDOM -> {
                ivStyle.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_random))
            }
            StyleMusic.REPEAT -> {
                ivStyle.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_repeat))
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_RAISE,
                    AudioManager.FLAG_PLAY_SOUND or AudioManager.FLAG_SHOW_UI
                )
                sbVolume.setValue(
                    audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat(),
                    true
                )
                return true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_LOWER,
                    AudioManager.FLAG_PLAY_SOUND or AudioManager.FLAG_SHOW_UI
                )
                sbVolume.setValue(
                    audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat(),
                    true
                )
                return true
            }
            else -> {
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    //Binding this Client to the AudioPlayer Service
    override val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as LocalBinder
            player = binder.service
            serviceBound = true
            setCallBackMusic()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            serviceBound = false
        }
    }

    override fun playAudio(list: MutableList<MusicItem?>, audioIndex: Int) {
        list[audioIndex]?.let {
            setMusicPlayer(it)
            setAction(it)
        }

        styleMusic = AppPreference.getIntPreferenceByName(this, Constant.styleMusic)
        styleIcon(styleMusic)
        setImageRotate()

        //Check is service is active
        if (!serviceBound) {
            //Store Serializable audioList to SharedPreferences
            val storage =
                StoragePreference(applicationContext)
            storage.storeAudioIndex(audioIndex)
            val playerIntent = Intent(this, MediaPlayerService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(playerIntent)
            } else {
                startService(playerIntent)
            }
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        } else {
            //Store the new audioIndex to SharedPreferences
            val storage =
                StoragePreference(applicationContext)
            storage.storeAudioIndex(audioIndex)

            //Service is active
            //Send a broadcast to the service -> PLAY_NEW_AUDIO
            val broadcastIntent = Intent(Broadcast_PLAY_NEW_AUDIO)
            sendBroadcast(broadcastIntent)
            setCallBackMusic()
        }
        slideLayout.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
    }

    override fun setCallBackMusic() {
        player?.setOnCallbackMedia(object : MediaPlayerContract.Callback {

            override fun onPrepared() {
                val duration = player?.duration ?: 0
                sbProgress.setValueRange(0, duration / 1000, false)
                loadingMusic.max = duration / 1000
                timeEnd.text = Util.getTime(duration.toLong())
                updateProgress()
                ivPlay.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_pause))
                ivPlayMM.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_pause))
            }

            override fun onComplete() {
            }

            override fun changeMusic(position: Int) {
                player?.audioList?.get(position)?.let { setMusicPlayer(it) }
            }

            override fun onAction(playbackStatus: PlaybackStatus) {
                when (playbackStatus) {
                    PlaybackStatus.PAUSED -> {
                        ivPlay.setImageDrawable(
                            ContextCompat.getDrawable(
                                activity,
                                R.drawable.ic_play
                            )
                        )
                        ivPlayMM.setImageDrawable(
                            ContextCompat.getDrawable(
                                activity,
                                R.drawable.ic_play
                            )
                        )
                        anim?.pause()
                    }
                    PlaybackStatus.PLAYING -> {
                        updateProgress()
                        anim?.resume()
                        ivPlay.setImageDrawable(
                            ContextCompat.getDrawable(
                                activity,
                                R.drawable.ic_pause
                            )
                        )
                        ivPlayMM.setImageDrawable(
                            ContextCompat.getDrawable(
                                activity,
                                R.drawable.ic_pause
                            )
                        )
                    }
                    PlaybackStatus.STYLE -> {
                        player?.styleMusic?.let { styleIcon(it) }
                    }
                    PlaybackStatus.LIKE -> {
                        setLikeUpdate()
                    }
                    PlaybackStatus.UNLIKE -> {
                        setLikeUpdate()
                    }
                    else -> {}
                }
            }

        })

        sbProgress.setOnPositionChangeListener { _, fromUser, _, _, _, newValue ->
            if (fromUser) {
                val runnable = Runnable {
                    if (player != null) {
                        player?.seekTo(newValue * 1000)
                    } else {
                        listMusic?.let { playAudio(it, previousPosition) }
                    }
                }
                runnable.run()
            }
        }
    }

    override fun setIconLike(likeType: String) {
        if (likeType == Constant.ACTION_LIKE) {
            ivFavorite.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_like))
        } else {
            ivFavorite.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_unlike))
        }
    }

    override fun setImageRotate() {
        if (anim != null) anim?.cancel()
        anim =
            ObjectAnimator.ofFloat(ivMinimize, View.ROTATION, 0f, 360f)
                .setDuration(15000)
        anim?.repeatCount = Animation.INFINITE
        anim?.interpolator = LinearInterpolator()
        anim?.start()
    }
}