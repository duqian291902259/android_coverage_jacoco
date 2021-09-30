package site.duqian.spring;

import java.io.File;

/**
 * 常量
 */
public class Constants {

    public static final String THREAD_EXECUTOR_NAME = "dq-executor";
    public static final String REPOSITORY_URL = "https://git-cc.nie.netease.com/android/cc.git";

    public static final String KEY_PARAM_PATH = "path";
    public static final String KEY_PARAM_FILENAME = "fileName";
    public static final String KEY_PARAM_DOWNLOAD_DIR = File.separator + "cc-jacoco-download" + File.separator;
    public static final String KEY_APP_NAME = "appName";
    public static final String KEY_VERSION_CODE = "versionCode";
    public static final String KEY_PARAM_UID = "uid";
    public static final String KEY_PARAM_USER_NAME = "userName";
    public static final String KEY_BRANCH_NAME = "branch";
    public static final String KEY_BASE_BRANCH_NAME = "base_branch";
    public static final String KEY_COMMIT_ID = "commitId";
    public static final String KEY_PARAM_TYPE = "type";//type 1=ec,

    public static final int TYPE_FILE_ALL = 100;//ALL
    public static final int TYPE_FILE_EC = 0;//.ec
    public static final int TYPE_FILE_CLASS = 1;//.class
    public static final int TYPE_FILE_APK = 2;//.apk
    public static final int TYPE_FILE_ZIP = 3;//.zip
    public static final int TYPE_FILE_TXT = 4;//.txt
    public static final int TYPE_FILE_RAR = 5;//.rar
}
