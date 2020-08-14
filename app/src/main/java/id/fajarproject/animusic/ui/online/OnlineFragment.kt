package id.fajarproject.animusic.ui.online

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import id.fajarproject.animusic.R
import id.fajarproject.animusic.data.network.model.MusicItem
import id.fajarproject.animusic.data.pref.AppPreference
import id.fajarproject.animusic.ui.base.BaseFragment
import id.fajarproject.animusic.ui.customView.OnItemClickListener
import id.fajarproject.animusic.ui.home.HomeActivity
import id.fajarproject.animusic.ui.home.HomeContract
import id.fajarproject.animusic.utils.Constant
import id.fajarproject.animusic.data.pref.StoragePreference
import id.fajarproject.animusic.utils.StyleMusic
import id.fajarproject.animusic.utils.Util
import kotlinx.android.synthetic.main.fragment_online.*
import javax.inject.Inject

/**
 * Created by Fajar Adi Prasetyo on 14/08/2020.
 */

class OnlineFragment : BaseFragment(),OnlineContract.View {


    private var list : MutableList<MusicItem?>? = null
    private var adapter : OnlineAdapter? = null

    @Inject
    lateinit var presenter: OnlineContract.Presenter<OnlineContract.View>

    var viewHome : HomeContract.View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        viewHome = activity as HomeActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_online, container, false)
    }
    lateinit var storage : StoragePreference

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        presenter.attach(this)
        storage = StoragePreference(activity)

        presenter.loadData()

    }

    override fun showDataSuccess(list: MutableList<MusicItem?>) {
        if (storage.loadAudio() == null){
            storage.storeAudio(list)
        }
        this.list               = list
        rvMusic.layoutManager   = LinearLayoutManager(activity)
        adapter                 = OnlineAdapter(activity, list)
        rvMusic.adapter         = adapter
        adapter?.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(view: View?, position: Int) {
                storage.storeAudio(list)
                viewHome?.playAudio(list,position)
            }
        })
        checkData()
        btnRandom.setOnClickListener {
            storage.storeAudio(list)
            AppPreference.writePreference(activity, Constant.styleMusic,StyleMusic.RANDOM)
            val randomIndex = Util.randomNumber(0,list.size.minus(1))
            viewHome?.playAudio(list,randomIndex)
        }
    }

    override fun showDataFailed(message: String) {
        Log.e("Error",message)
        checkData()
    }

    override fun checkData() {
        val count = adapter?.itemCount ?: 0
        if(count > 0){
            noData.visibility   = View.GONE
            clMusic.visibility  = View.VISIBLE
        }else{
            noData.visibility   = View.VISIBLE
            clMusic.visibility  = View.GONE
        }
    }

    override fun setToolbar() {
    }

    override fun setUI() {
    }

    override fun showLoading() {
        progressBar.visibility  = View.VISIBLE
        clMusic.visibility      = View.GONE
        activity.window?.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    override fun hideLoading() {
        progressBar.visibility  = View.GONE
        clMusic.visibility      = View.VISIBLE
        activity.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }
}