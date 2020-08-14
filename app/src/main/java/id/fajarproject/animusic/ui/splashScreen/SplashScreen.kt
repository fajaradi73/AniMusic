package id.fajarproject.animusic.ui.splashScreen

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import id.fajarproject.animusic.R
import id.fajarproject.animusic.ui.home.HomeActivity


/**
 * Created by Fajar Adi Prasetyo on 05/08/2020.
 */

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(this,HomeActivity::class.java)
        intent.flags = (Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
        overridePendingTransition(
            R.anim.fade_in,
            R.anim.fade_out
        )
        finish()
    }
}