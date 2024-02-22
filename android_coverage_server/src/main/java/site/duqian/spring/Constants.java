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
    public static final int LIMIT_REPORT_NUM = 100;
    public static final String APP_CC_ANDROID = "android";
    public static final String APP_CC_AUDIO = "cc-audio";
    public static final String APP_CC_TASK_DEMO = "cc-task-demo";
    public static final String APP_CC_COVERAGE = "coverage-demo";
    public static final String SERVER_HOST_DOMAIN = "duqian.cn";
    public static final String LOCAL_SERVER_HOST_URL = "http://192.168.11.201:18090";//本地调试服务器地址.http://127.0.0.1:8090
    public static final String LOCAL_REPORT_HOST_URL = "http://192.168.11.201:18080";////本地调试report报告地址
    public static final String JACOCO_SERVER_HOST_URL = LOCAL_SERVER_HOST_URL; //android覆盖率平台服务器地址
    public static final String REPORT_SERVER_HOST_URL = "http://report.dev.mobile.cn";//报告地址，report前缀
    public static final String REPOSITORY_URL = "https://git-cc.nie.duqian.com/android/cc.git";

    //os
    public static final String OS_ANDROID = "Android";
    public static final String OS_IOS = "IOS";
    public static final String OS_PC = "PC";
    public static final String CC_IOS_DIR = "cc-ios";
    public static final String CC_PC_DIR = "cc-pc";
    public static final String IOS_REPORT_PATH = CC_IOS_DIR + "/dev_coverage/37e9e766/";
    public static final String IOS_REPORT_FILE_NAME = "ios_37e9e766_8cbe0fe8";
    public static final String PC_REPORT_PATH = CC_PC_DIR + "/dev/1a34635a/";
    public static final String PC_REPORT_FILE_NAME = "pc_dev_1a34635a";

    //请求入参
    public static final String KEY_PARAM_PATH = "path";
    public static final String KEY_PARAM_NEW_PATH = "newpath";
    public static final String KEY_PARAM_DIR = "dir";
    public static final String KEY_PARAM_FILENAME = "fileName";
    public static final String KEY_APP_NAME = "appName";
    public static final String KEY_UPLOAD_DIR = "upload_dir";
    public static final String KEY_PARAM_UID = "uid";
    public static final String KEY_PARAM_USER_NAME = "userName";
    public static final String KEY_PARAM_OS = "os";
    public static final String KEY_BRANCH_NAME = "branch";
    public static final String KEY_BASE_BRANCH_NAME = "base_branch";
    public static final String KEY_COMMIT_ID = "commitId";
    public static final String KEY_COMMIT_ID2 = "commitId2";//对比增量的commit
    public static final String KEY_PARAM_TYPE = "type";//type 1=ec,
    public static final String KEY_PARAM_INCREMENTAL = "incremental";//是否增量
    //public static final String APP_PACKAGE_NAME = File.separator + "com" + File.separator + "netease" + File.separator + "cc";//包名
    public static final String APP_PACKAGE_NAME = "/com/duqian/coverage";//包名
    public static final String APP_PACKAGE_NAME2 = "\\com\\duqian\\coverage";//包名

    //文件夹名称
    public static final String CC_JACOCO_DIR = "dq-coverage";
    public static final String REPORT_DOWNLOAD_ROOT_DIR = CC_JACOCO_DIR + File.separator;
    public static final String FILE_UPLOAD_ROOT_DIR = "upload";
    public static final String REPORT_DIR_NAME = "report";
    public static final String CLASS_DIR_NAME = "classes";
    public static final String SOURCE_DIR_NAME = "src";
    public static final String EC_FILES_DIR_NAME = "ec";//ec文件目录名称
    public static final String GIT_SOURCE_DIR_NAME = "repository";
    public static final String GIT_SOURCE_MASTER = "master";
    public static final String GIT_SOURCE_DEV = "dev";
    public static final String GIT_DATA_DIR_NAME = ".git";
    public static final String DIFF_DIR_NAME = "diff";
    public static final String FILE_NAME_SPLIT = "_";
    public static final String DIFF_FILES_NAME = "diffFiles.txt";
    public static final String JACOCO_CLI_FILE_NAME = "jacococli.jar";
    public static final String CLASS_ZIP_FILE_NAME = "classes.zip";
    public static final String SRC_ZIP_FILE_NAME = "src.zip";

    //文件上传类型：1，有扩展名 2，通用的自定义存储，提供dir和fileName
    public static final String TYPE_FILE_CUSTOM = "custom";//自定义路径的上传
    public static final String TYPE_FILE_EC = ".ec";
    public static final String TYPE_FILE_CLASS = ".class";
    public static final String TYPE_FILE_APK = ".apk";
    public static final String TYPE_FILE_ZIP = ".zip";
    public static final String TYPE_FILE_TXT = ".txt";
    public static final String TYPE_FILE_RAR = ".rar";

    //git 命令
    public static final String GIT_GET_CURRENT_COMMIT_SHA = "git log --name-only";
    public static final String GIT_DIFF_COMMIT_FILES_ZIP = "git diff %1$s %2$s --name-only > diffCommits.txt";

    //状态码
    public static final int CODE_SUCCESS = 0;
    public static final int CODE_FAILED = -1;
    public static final int ERROR_CODE_NO_FILES = 1000;
    public static final int ERROR_CODE_NO_CLASSES = 1001;
    public static final int ERROR_CODE_NO_SRC = 1002;
    public static final int ERROR_CODE_NO_EC_FILE = 1003;
    public static final int ERROR_CODE_DIFF_FAILED = 1004;
    public static final int ERROR_CODE_CMD_FAILED = 1005;

}
