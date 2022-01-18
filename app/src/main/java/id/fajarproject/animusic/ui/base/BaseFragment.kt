package id.fajarproject.animusic.ui.base

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import id.fajarproject.animusic.App
import id.fajarproject.animusic.di.component.DaggerFragmentComponent
import id.fajarproject.animusic.di.component.FragmentComponent
import id.fajarproject.animusic.di.module.FragmentModule
import io.reactivex.disposables.CompositeDisposable


/**
 * Created by Fajar Adi Prasetyo on 14/08/2020.
 */

open class BaseFragment : Fragment() {
    lateinit var component: FragmentComponent
    lateinit var activity: Activity

    private val subscriptions = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component = DaggerFragmentComponent.builder()
            .fragmentModule(FragmentModule(requireContext()))
            .applicationComponent((requireActivity().application as App).getApplicationComponent())
            .build()
        activity = requireActivity()
    }

    override fun onDestroy() {
        super.onDestroy()
        subscriptions.clear()
    }
}