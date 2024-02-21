package com.duqian.coverage.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.duqian.coverage.BuildConfig
import com.duqian.coverage.R
import com.duqian.coverage.utils.UIUtils
import com.duqian.coverage_library.JacocoHelper
import com.duqian.coverage_library.JacocoHelper.JACOCO_HOST
import com.duqian.coverage_library.CoverageUtil
import com.duqian.coverage_library.JacocoCallback
import java.io.File
import kotlin.concurrent.thread

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val textView: TextView = root.findViewById(R.id.text_home)
        val btnDump: Button = root.findViewById(R.id.btn_dump)
        val btnUpload: Button = root.findViewById(R.id.btn_upload)
        val btnClear: Button = root.findViewById(R.id.btn_clear)
        homeViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })


        btnDump.setOnClickListener {
            thread {
                val isSuccess = JacocoHelper.generateEcFile(this.context)
                val msg = "generateEcFile $isSuccess"
                UIUtils.toast(context, msg)
                Log.d("dq-jacoco", msg)
            }
        }

        btnClear.setOnClickListener {
            val ecDir = JacocoHelper.getJacocoEcFileSaveDir(context)
            val deleteDirectory =
                CoverageUtil.deleteDirectory(ecDir)
            val msg = "deleteDirectory $ecDir=$deleteDirectory"
            Log.d("dq-jacoco", msg)
            UIUtils.toast(context, msg)
        }

        JacocoHelper.initAppData(
            isOpenCoverage = BuildConfig.IS_JACOCO_ENABLE,
            BuildConfig.CURRENT_BRANCH_NAME,
            BuildConfig.CURRENT_COMMIT_ID,
            BuildConfig.COV_APP_NAME,
            BuildConfig.JACOCO_HOST
        )

        btnUpload.setOnClickListener {
            thread {
                JacocoHelper.generateEcFileAndUpload(this.context, "6666666", object : JacocoCallback {
                    override fun onEcDumped(ecPath: String?) {
                        val msg = "onEcDumped $ecPath"
                        Log.d("dq-jacoco", msg)
                        UIUtils.toast(context, msg)
                    }

                    override fun onEcUploaded(isSingleFile: Boolean, ecFile: File) {
                        Log.d("dq-jacoco", "onEcUploaded $isSingleFile,ecFile=$ecFile")
                        UIUtils.toast(context, "上传成功 ${ecFile.name}")
                    }

                    override fun onIgnored(failedMsg: String?) {
                        Log.d("dq-jacoco", "onIgnored $failedMsg")
                        UIUtils.toast(context, "失败 $failedMsg")
                    }

                    override fun onLog(TAG: String?, msg: String?) {
                        Log.d(TAG, "$msg")
                    }
                })
            }
        }

        return root
    }

}