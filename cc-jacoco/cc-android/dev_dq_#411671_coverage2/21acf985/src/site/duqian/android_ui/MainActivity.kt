package site.duqian.android_ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import site.duqian.android_ui.fragment.BaseFragment
import site.duqian.android_ui.fragment.CommonDialogFragment
import site.duqian.android_ui.fragment.MainFragment

class MainActivity : AppCompatActivity() {

    private var mMainFragment: BaseFragment? = null
    private var mCurrentFragment: BaseFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            launchMainFragment()
        }

        //launchFragment(ImageEffectFragment.newInstance())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun launchMainFragment() {
        if (mMainFragment == null) {
            mMainFragment = MainFragment.newInstance()
        }
        launchFragment(mMainFragment!!)
    }

    fun launchFragment(fragment: BaseFragment) {
        this.mCurrentFragment = fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commitNow()
    }

    override fun onBackPressed() {
        if (mCurrentFragment is MainFragment) {
            super.onBackPressed()
        } else {
            launchMainFragment()
        }
    }
}