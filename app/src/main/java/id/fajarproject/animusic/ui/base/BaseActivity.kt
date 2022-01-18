package id.fajarproject.animusic.ui.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import id.fajarproject.animusic.App
import id.fajarproject.animusic.di.component.ActivityComponent
import id.fajarproject.animusic.di.component.DaggerActivityComponent
import id.fajarproject.animusic.di.module.ActivityModule
import id.fajarproject.animusic.ui.customView.DialogListener
import id.fajarproject.animusic.utils.Util
import io.reactivex.disposables.CompositeDisposable


/**
 * Created by Fajar Adi Prasetyo on 05/08/2020.
 */

open class BaseActivity : AppCompatActivity() {
    lateinit var activity: Activity

    private val subscriptions = CompositeDisposable()

    var isConnection = true

    lateinit var component: ActivityComponent

    override fun setContentView(layoutResID: Int) {
        if (!Util.isInternetAvailable(this)) {
            showDialogInternet()
            isConnection = false
            return
        }
        super.setContentView(layoutResID)
        activity = this
        component = DaggerActivityComponent.builder()
            .activityModule(ActivityModule(this))
            .applicationComponent((application as App).getApplicationComponent())
            .build()
    }

    fun getActivityComponent(): ActivityComponent {
        return component
    }

    override fun onDestroy() {
        super.onDestroy()
        onUnsubscribe()
    }

    private fun onUnsubscribe() {
        subscriptions.clear()
    }

    private fun showDialogInternet() {
        Util.showDialogInternet(this, object : DialogListener {
            override fun onYes() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val panelIntent = Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
                    startActivityForResult(panelIntent, 545)
                } else {
                    startActivityForResult(Intent(Settings.ACTION_WIRELESS_SETTINGS), 545)
                }
            }

            override fun onNo() {

            }
        })
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm =
                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 545) {
            recreate()
        }
    }
}