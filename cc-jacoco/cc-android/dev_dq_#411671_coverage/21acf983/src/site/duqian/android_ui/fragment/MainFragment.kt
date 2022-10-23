package site.duqian.android_ui.fragment

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.zhy.view.flowlayout.FlowLayout
import com.zhy.view.flowlayout.TagAdapter
import com.zhy.view.flowlayout.TagFlowLayout
import site.duqian.android_ui.MainActivity
import site.duqian.android_ui.R
import site.duqian.android_ui.viewmodel.MainViewModel


class MainFragment : BaseFragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var mTagFlowLayout: TagFlowLayout
    private val mTabs = arrayListOf<String>()
    private val mDecs = arrayListOf<String>()
    private var mAdapter: TagAdapter<String>? = null

    override fun getLayoutId(): Int {
        return R.layout.main_fragment
    }

    override fun initView(view: View) {
        mTagFlowLayout = view.findViewById(R.id.tag_flow_layout)
    }

    override fun initData() {
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModel.mTabs.observe(this, Observer {
            this.mTabs.clear()
            this.mTabs.addAll(it)
            initTagFlowLayout()
        })
        viewModel.mDescriptions.observe(this, Observer {
            this.mDecs.clear()
            this.mDecs.addAll(it)
        })
    }

    private fun initTagFlowLayout() {
        mTagFlowLayout.adapter = object : TagAdapter<String>(mTabs) {
            override fun getView(parent: FlowLayout?, position: Int, s: String?): View? {
                val tv = LayoutInflater.from(activity).inflate(
                    R.layout.main_tag_textview,
                    mTagFlowLayout, false
                ) as TextView
                tv.text = s
                return tv
            }
        }.also { mAdapter = it }

        mAdapter?.setSelectedList(0)

        mTagFlowLayout.setOnTagClickListener { view, position, parent ->
            val text = mTabs[position]
            val dec = mDecs[position]
            Toast.makeText(activity, dec, Toast.LENGTH_SHORT).show()
            handleTextClicked(position, text)
            true
        }
    }

    private fun handleTextClicked(position: Int, text: String) {
        val mainActivity = activity as MainActivity?
        lateinit var baseFragment: BaseFragment
        when (position) {
            0 -> baseFragment = ImageEffectFragment.newInstance()
            1 -> baseFragment = ProgressFragment.newInstance()
            2 -> baseFragment = MainDialogFragment.newInstance()
        }

        mainActivity?.launchFragment(baseFragment)
    }
}