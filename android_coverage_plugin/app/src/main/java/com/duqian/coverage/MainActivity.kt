package com.duqian.coverage

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.duqian.coverage.utils.TestJava
import com.duqian.coverage.utils.UIUtils
import com.duqian.coverage.utils.dp
import com.duqian.coverage_library.JacocoHelper
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


        UIUtils.dp2px(this, 20f)

        Log.d("dq-jacoco", "ext test66667888,20dp=  ${20.dp}")

        TestJava.test()

        requestSdcardPermissions()
    }

    private fun requestSdcardPermissions() {
        try {
            //需要的权限
            val permArr = arrayOf<String>(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS
            )
            var needReq = false
            for (i in permArr.indices) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        permArr[i]
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    needReq = true
                    break
                }
            }
            if (needReq) ActivityCompat.requestPermissions(this, permArr, 1)
        } catch (e: Exception) {
            Log.e("dq-main", "error=$e")
        }
    }

    override fun finish() {
        super.finish()
        thread {
            JacocoHelper.generateEcFileAndUpload(this, "88888", null)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}