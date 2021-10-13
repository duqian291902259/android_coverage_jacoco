package site.duqian.spring;

import java.io.File;

/**
 * Description:项目常量
 *
 * @author n20241 Created by 杜小菜 on 2021/10/8 - 15:17 .
 * E-mail: duqian2010@gmail.com
 */
public class Constants {

    public static final String THREAD_EXECUTOR_NAME = "dq-executor";
    public static final String REPORT_SERVER_HOST_URL = "http://10.255.209.49:8082";//todo-dq 修改server
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
    //public static final String APP_PACKAGE_NAME = File.separator + "com" + File.separator + "netease" + File.separator + "cc";//包名
    public static final String APP_PACKAGE_NAME ="/com/netease/cc";//包名
    public static final String APP_PACKAGE_NAME2 ="\\com\\netease\\cc";//包名

    //文件夹名称
    public static final String REPORT_DOWNLOAD_ROOT_DIR = "cc-jacoco" + File.separator;// File.separator +
    public static final String REPORT_DIR_NAME = "report";
    public static final String CLASS_DIR_NAME = "classes";
    public static final String SOURCE_DIR_NAME = "src";
    public static final String GIT_SOURCE_DIR_NAME = "repository";
    public static final String DIFF_DIR_NAME = "diff";
    public static final String DIFF_FILES_NAME = "diffFiles.txt";
    public static final String JACOCO_CLI_FILE_NAME = "jacococli.jar";
    public static final String CLASS_ZIP_FILE_NAME = "classes.zip";
    public static final String SRC_ZIP_FILE_NAME = "src.zip";

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
    //git diff c8447a2fe972c7925bd1c52e905f91071ee8d5a2 84f1ad08fafcd71a8cdca2faceabd0e920c6cc61 --name-only > ../diffCommits.txt
    //public static final String GIT_DIFF_COMMIT_FILES_ZIP = "git diff %1$s %2$s --name-only | xargs tar -zcvf diffCommitFile.zip";
    public static final String GIT_DIFF_COMMIT_FILES_ZIP = "git diff %1$s %2$s --name-only > diffCommits.txt";
    public static final String CMD_HTTP_SERVER_REPORT = "http-server cc-jacoco-download/ -p 8086";


    //状态码
    public static final int CODE_SUCCESS = 0;
    public static final int CODE_FAILED = -1;
    public static final int ERROR_CODE_NO_FILES = 1000;
    public static final int ERROR_CODE_NO_CLASSES = 1001;
    public static final int ERROR_CODE_NO_SRC = 1002;
    public static final int ERROR_CODE_NO_EC_FILE = 1003;

}
