package site.duqian.spring.utils;

import org.eclipse.jgit.api.*;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import site.duqian.spring.Constants;

import java.io.File;
import java.util.Set;

/**
 * github的操作有效，gitlab无效
 */
public class JGitHelper {
    private static final Logger logger = LoggerFactory.getLogger(JGitHelper.class);// slf4j日志记录器

    //定义本地git路径
    public static final String LOCAL_PATH = FileUtils.getRootDir() + File.separator + Constants.GIT_SOURCE_DIR_NAME + File.separator;
    //.git文件路径
    public static final String LOCAL_GIT_FILE = LOCAL_PATH + ".git";
    //远程仓库地址
    public static final String REMOTE_REPOSITORY_URL = "https://github.com/duqian291902259/AndroidUI.git";
    //操作git的用户名
    public static final String GIT_USER_NAME = "duqian291902259";
    //密码
    public static final String GIT_PASSWORD = ".";
    public static final String GIT_BRANCH = "main";
    public static final String GIT_COMMIT_ID = "6b4767f5344eb99319356be242e8d9b0b20830c1";

    //建立与远程仓库的联系，仅需要执行一次
    public static String setUpRepo() {
        String msg = "";
        try {
            Git git = Git.cloneRepository().setURI(REMOTE_REPOSITORY_URL).setCredentialsProvider(new UsernamePasswordCredentialsProvider(GIT_USER_NAME, GIT_PASSWORD)).setBranch(GIT_BRANCH).setDirectory(new File(LOCAL_PATH)).call();
            msg = "git init success！" + git;
        } catch (Exception e) {
            msg = "setUpRepo onError:" + e;
        }
        logger.debug("setUpRepo " + msg);
        return msg;
    }

    //pull拉取远程仓库文件
    public static boolean pullBranchToLocal() {
        boolean resultFlag = false;
        try {
            Git git = new Git(new FileRepository(LOCAL_GIT_FILE));
            PullResult pullResult = git.pull().setRemoteBranchName(GIT_BRANCH).setCredentialsProvider(new UsernamePasswordCredentialsProvider(GIT_USER_NAME, GIT_PASSWORD)).call();
            resultFlag = true;
            logger.debug("pullBranchToLocal=" + pullResult);
        } catch (Exception e) {
            logger.error("pullBranchToLocal on error: " + e);
        }
        return resultFlag;
    }

    //提交git
    public static boolean commitFiles() {
        try {
            Git git = Git.open(new File(LOCAL_GIT_FILE));
            AddCommand addCommand = git.add();//add操作 add -A操作在jgit不知道怎么用 没有尝试出来 有兴趣的可以看下jgitAPI研究一下 欢迎留言
            addCommand.addFilepattern(".").call();

            RmCommand rm = git.rm();
            Status status = git.status().call();//循环add missing 的文件 没研究出missing和remove的区别 就是删除的文件也要提交到git
            Set<String> missing = status.getMissing();
            for (String m : missing) {
                logger.info("missing files: " + m);
                rm.addFilepattern(m).call(); //每次需重新获取rm status 不然会报错
                rm = git.rm();
                status = git.status().call();
            }
            //循环add remove 的文件
            Set<String> removed = status.getRemoved();
            for (String r : removed) {
                logger.info("removed files: " + r);
                rm.addFilepattern(r).call();
                rm = git.rm();
                status = git.status().call();
            }//提交
            git.commit().setMessage("commit test by jgit").call();//推送
            git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(GIT_USER_NAME, GIT_PASSWORD)).call();
            return true;
        } catch (Exception e) {
            logger.error("commitFiles on error: " + e);
            return false;
        }
    }

    public static void main(String[] args) {
        setUpRepo();
        pullBranchToLocal();
        commitFiles();
        //GitRepoUtil.cloneRepository(REMOTE_REPOSITORY_URL, LOCAL_PATH, GIT_COMMIT_ID, GIT_USER_NAME, GIT_PASSWORD);
    }
}
