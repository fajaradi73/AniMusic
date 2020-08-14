package id.fajarproject.animusic.ui.home

import android.content.ServiceConnection
import androidx.fragment.app.Fragment
import id.fajarproject.animusic.data.network.model.MusicItem
import id.fajarproject.animusic.ui.base.BaseContract


/**
 * Created by Fajar Adi Prasetyo on 05/08/2020.
 */

class HomeContract {
    interface View : BaseContract.View {
        fun checkLocal()
        fun addFragment(fragments: Fragment, tag: String)
        fun setOpenFragment()
        fun setViewExpand()
        fun setViewMinimize()
        fun setMusicPlayer(item: MusicItem)
        fun setVolume()
        fun setAction()
        fun previousMusic()
        fun nextMusic()
        fun runPlayer(view: android.view.View)
        fun updateProgress()
        fun styleIcon(styleMusic : Int)
        val serviceConnection: ServiceConnection
        fun playAudio(list: MutableList<MusicItem?>,audioIndex: Int)
        fun setCallBackMusic()
        fun setImageRotate()
        fun setIconLike(likeType : String)
    }

    interface Presenter<V> : BaseContract.Presenter<V>{
        fun loadData()
    }
}