package id.fajarproject.animusic.ui.splashScreen

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import id.fajarproject.animusic.R
import id.fajarproject.animusic.data.pref.AppPreference
import id.fajarproject.animusic.ui.home.HomeActivity
import id.fajarproject.animusic.utils.Constant


/**
 * Created by Fajar Adi Prasetyo on 05/08/2020.
 */

@SuppressLint("CustomSplashScreen")
class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppPreference.writePreference(this, Constant.tag, Constant.online)

        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = (Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)

        startActivity(intent)
        overridePendingTransition(
            R.anim.fade_in,
            R.anim.fade_out
        )
        finish()
    }
}