package id.fajarproject.animusic.ui.favorite

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import id.fajarproject.animusic.R
import id.fajarproject.animusic.data.network.model.MusicItem
import id.fajarproject.animusic.data.pref.StoragePreference
import id.fajarproject.animusic.data.realm.RealmHelper
import id.fajarproject.animusic.ui.base.BaseFragment
import id.fajarproject.animusic.ui.customView.OnItemClickListener
import id.fajarproject.animusic.ui.home.HomeActivity
import id.fajarproject.animusic.ui.home.HomeContract
import id.fajarproject.animusic.utils.Constant
import io.realm.Realm
import kotlinx.android.synthetic.main.fragment_favorite.*
import java.util.*
import javax.inject.Inject


/**
 * Created by Fajar Adi Prasetyo on 14/08/2020.
 */

class FavoriteFragment : BaseFragment(), FavoriteContract.View {

    @Inject
    lateinit var presenter: FavoriteContract.Presenter<FavoriteContract.View>

    private var viewHome: HomeContract.View? = null
    private var adapter: FavoriteAdapter? = null
    private var realm = Realm.getDefaultInstance()
    private var realmHelper: RealmHelper? = null
    lateinit var storage: StoragePreference

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
        return inflater.inflate(R.layout.fragment_favorite, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.attach(this)
        storage = StoragePreference(activity)
        realmHelper = RealmHelper(realm)
        updateFavorite()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            updateFavorite()
        }
    }

    override fun checkData() {
        val count = adapter?.itemCount ?: 0
        if (count > 0) {
            noData.visibility = View.GONE
            clFavorite.visibility = View.VISIBLE
        } else {
            noData.visibility = View.VISIBLE
            clFavorite.visibility = View.GONE
        }
    }

    override fun updateFavorite() {
        val list = realmHelper?.loadMusic()
        list?.let { showDataSuccess(it) }.run { showDataFailed("No Data") }
    }


    override fun showDataSuccess(list: MutableList<MusicItem?>) {
        Collections.sort(list, presenter.sortDate())
        val layoutManager = LinearLayoutManager(activity)
        rvFavorite.layoutManager = layoutManager
        adapter = FavoriteAdapter(activity, list)
        rvFavorite.adapter = adapter
        adapter?.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(view: View?, position: Int) {
                storage.storeAudio(list)
                viewHome?.playAudio(list, position)
            }
        })
        adapter?.notifyDataSetChanged()

        checkData()
        btnPlay.setOnClickListener {
            storage.storeAudio(list)
            viewHome?.playAudio(list, 0)
        }
    }

    override fun showDataFailed(message: String) {
        Log.e(Constant.tag, message)
        checkData()
    }

    override fun showLoading() {
    }

    override fun hideLoading() {
    }
}