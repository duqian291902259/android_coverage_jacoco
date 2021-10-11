package site.duqian.spring;

import java.io.File;

/**
 * Description:项目常量
 * @author n20241 Created by 杜小菜 on 2021/10/8 - 15:17 .
 * E-mail: duqian2010@gmail.com
 */
public class Constants {

    public static final String THREAD_EXECUTOR_NAME = "dq-executor";
    public static final String REPORT_SERVER_HOST_URL = "http://10.255.209.49:8080";//todo-dq 修改server
    public static final String REPOSITORY_URL = "https://git-cc.nie.netease.com/android/cc.git";

    //请求入参
    public static final String KEY_PARAM_PATH = "path";
    public static final String KEY_PARAM_FILENAME = "fileName";
    public static final String KEY_APP_NAME = "appName";
    public static final String KEY_VERSION_CODE = "versionCode";
    public static final String KEY_PARAM_UID = "uid";
    public static final String KEY_PARAM_USER_NAME = "userName";
    public static final String KEY_BRANCH_NAME = "branch";
    public static final String KEY_BASE_BRANCH_NAME = "base_branch";
    public static final String KEY_COMMIT_ID = "commitId";
    public static final String KEY_COMMIT_ID2 = "commitId2";//对比增量的commit
    public static final String KEY_PARAM_TYPE = "type";//type 1=ec,
    public static final String KEY_PARAM_INCREMENTAL = "incremental";//是否增量
    public static final String APP_PACKAGE_NAME = "/com/";//包名

    //文件夹名称
    public static final String REPORT_DOWNLOAD_ROOT_DIR ="cc-jacoco-download" + File.separator;// File.separator +
    public static final String REPORT_DIR_NAME = "report";
    public static final String CLASS_DIR_NAME = "classes";
    public static final String SOURCE_DIR_NAME = "src";
    public static final String DIFF_DIR_NAME = "diff";
    public static final String DIFF_FILES_NAME = "diffFiles.txt";
    public static final String JACOCO_CLI_FILE_NAME = "jacococli.jar";
    public static final String JACOCO_CLASS_ZIP_FILE_NAME = "classes.zip";

    //扩展名
    public static final String TYPE_FILE_ALL = "";//ALL
    public static final String TYPE_FILE_EC = ".ec";
    public static final String TYPE_FILE_CLASS = ".class";
    public static final String TYPE_FILE_APK = ".apk";
    public static final String TYPE_FILE_ZIP = ".zip";
    public static final String TYPE_FILE_TXT = ".txt";
    public static final String TYPE_FILE_RAR = ".rar";

    //git 命令
    public static final String GIT_GET_CURRENT_COMMIT_SHA = "git log --name-only";
    public static final String GIT_DIFF_COMMIT_FILES = "git diff %1$s %2$s --name-only";
    public static final String GIT_DIFF_COMMIT_FILES_ZIP = "git diff %1$s %2$s --name-only | xargs tar -zcvf diffCommitFile.zip";

}
