package id.fajarproject.animusic.ui.download

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import id.fajarproject.animusic.R
import id.fajarproject.animusic.databinding.FragmentDownloadBinding
import id.fajarproject.animusic.ui.base.BaseFragment
import id.fajarproject.animusic.ui.home.HomeActivity
import id.fajarproject.animusic.ui.home.HomeContract


/**
 * Created by Fajar Adi Prasetyo on 14/08/2020.
 */

class DownloadFragment : BaseFragment() {

    var viewHome: HomeContract.View? = null
    private lateinit var downloadBinding: FragmentDownloadBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        viewHome = activity as HomeActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        downloadBinding = FragmentDownloadBinding.inflate(inflater,container,false)
        return downloadBinding.root
    }

}