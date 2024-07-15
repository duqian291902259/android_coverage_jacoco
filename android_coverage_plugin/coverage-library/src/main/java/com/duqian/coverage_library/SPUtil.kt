package com.duqian.coverage_library

import android.content.Context
import android.content.SharedPreferences

/**
 * Description:SP工具类
 *
 * Created by 杜乾 on 2024/7/15 - 10:17.
 * E-mail: duqian2010@gmail.com
 */
class SPUtil {
    companion object {
        private var instance: SPUtil? = null

        /**
         * 保存在手机里面的文件名
         */
        private const val FILE_NAME = "share_date_cc"

        fun get(context: Context): SPUtil {
            if (instance == null) {
                synchronized(SPUtil::class.java) {
                    if (instance == null) {
                        instance = SPUtil()
                    }
                }
            }
            instance?.init(context)
            return instance!!
        }
    }

    @Volatile
    private var mSharedPreferences: SharedPreferences? = null

    @Synchronized
    private fun init(context: Context): SharedPreferences {
        if (mSharedPreferences == null) {
            synchronized(SPUtil::class.java) {
                if (mSharedPreferences == null) {
                    mSharedPreferences = context.applicationContext.getSharedPreferences(
                        FILE_NAME,
                        Context.MODE_PRIVATE
                    )
                }
            }
        }
        return mSharedPreferences!!
    }

    fun putBoolean(key: String?, value: Boolean) {
        mSharedPreferences!!.edit().putBoolean(key, value).apply()
    }

    fun getBoolean(key: String?, defValue: Boolean): Boolean {
        return mSharedPreferences!!.getBoolean(key, defValue)
    }

    fun putString(key: String?, value: String?) {
        mSharedPreferences!!.edit().putString(key, value).apply()
    }

    fun getString(key: String?, defValue: String?): String? {
        return mSharedPreferences!!.getString(key, defValue)
    }

    fun putInt(key: String?, value: Int) {
        mSharedPreferences!!.edit().putInt(key, value).apply()
    }

    fun getInt(key: String?, defValue: Int): Int {
        return mSharedPreferences!!.getInt(key, defValue)
    }

    fun putLong(key: String?, value: Long) {
        mSharedPreferences!!.edit().putLong(key, value).apply()
    }

    fun getLong(key: String?, defValue: Long): Long {
        return mSharedPreferences!!.getLong(key, defValue)
    }

    /**
     * 保存数据的方法，我们需要拿到保存数据的具体类型，然后根据类型调用不同的保存方法
     */
    fun setParam(key: String?, `object`: Any) {
        try {
            val type = `object`.javaClass.simpleName
            val editor = mSharedPreferences!!.edit()
            if ("String" == type) {
                editor.putString(key, `object` as String)
            } else if ("Integer" == type) {
                editor.putInt(key, (`object` as Int))
            } else if ("Boolean" == type) {
                editor.putBoolean(key, (`object` as Boolean))
            } else if ("Float" == type) {
                editor.putFloat(key, (`object` as Float))
            } else if ("Long" == type) {
                editor.putLong(key, (`object` as Long))
            }
            editor.apply()
        } catch (e: Exception) {
        }
    }

    /**
     * 得到保存数据的方法，我们根据默认值得到保存的数据的具体类型，然后调用相对于的方法获取值
     */
    fun getParam(key: String?, defaultObject: Any): Any? {
        try {
            val type = defaultObject.javaClass.simpleName
            //SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
            val sp = mSharedPreferences!!
            if ("String" == type) {
                return sp.getString(key, defaultObject as String)
            } else if ("Integer" == type) {
                return sp.getInt(key, (defaultObject as Int))
            } else if ("Boolean" == type) {
                return sp.getBoolean(key, (defaultObject as Boolean))
            } else if ("Float" == type) {
                return sp.getFloat(key, (defaultObject as Float))
            } else if ("Long" == type) {
                return sp.getLong(key, (defaultObject as Long))
            }
        } catch (e: Exception) {
        }
        return null
    }

    /**
     * 清除所有数据
     */
    fun clear() {
        val editor = mSharedPreferences!!.edit()
        editor.clear().apply()
    }

    /**
     * 清除指定数据
     */
    fun clearAll(key: String?) {
        val editor = mSharedPreferences!!.edit()
        editor.remove(key)
        editor.apply()
    }
}