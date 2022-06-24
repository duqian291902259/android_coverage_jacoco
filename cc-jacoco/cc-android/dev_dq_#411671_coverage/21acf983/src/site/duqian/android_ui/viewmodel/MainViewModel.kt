package site.duqian.android_ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {


    val mTabs: MutableLiveData<List<String>> = MutableLiveData()
    val mDescriptions: MutableLiveData<List<String>> = MutableLiveData()


    private val mDataMap = mutableMapOf(
        "Image Effect" to "自定义图片效果：各种图片融合特效",
        "ProgressBar" to "自定义进度条，圆角矩形，环形，时钟，表",
        "Dialog" to "自定义对话框：弹窗，提示，警告，说明",
        "Shape" to "自定义形状：圆环，矩形，渐变，按钮",
        "FloatWindow" to "悬浮窗特效",
    )

    init {
        val tabs = arrayListOf<String>()
        val decs = arrayListOf<String>()
        mDataMap.forEach {
            tabs.add(it.key)
            decs.add(it.value)
        }
        mTabs.value = tabs
        mDescriptions.value = decs
    }
}