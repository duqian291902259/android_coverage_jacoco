package site.duqian.spring.git_helper;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class JGitHelper {
    private static final Logger logger = LoggerFactory.getLogger(JGitHelper.class);// slf4j日志记录器

    //定义本地git路径
    public static final String LOCAL_PATH = "/Users/duqian/Development/MyGitHub/AndroidUI/";//System.getProperty("user.dir");//"D:/git_home/demo/";
    //.git文件路径
    public static final String LOCAL_GIT_FILE = LOCAL_PATH + ".git";
    //远程仓库地址
    public static final String REMOTE_REPOSITORY_URL = "https://github.com/duqian291902259/AndroidUI.git";
    //操作git的用户名
    public static final String USER = "duqian291902259";
    //密码
    public static final String PASSWORD = "";

    //建立与远程仓库的联系，仅需要执行一次
    public static String setupRepo() {
        String msg = "";
        try {
            Git git = Git.cloneRepository().setURI(REMOTE_REPOSITORY_URL).setCredentialsProvider(new UsernamePasswordCredentialsProvider(USER, PASSWORD)).setBranch("master").setDirectory(new File(LOCAL_PATH)).call();
            msg = "git init success！";
        } catch (Exception e) {
            msg = "git已经初始化！";
        }
        return msg;
    }

    //pull拉取远程仓库文件
    public static boolean pullBranchToLocal() {
        boolean resultFlag = false;
        //git仓库地址
        Git git;
        try {
            git = new Git(new FileRepository(LOCAL_GIT_FILE));
            git.pull().setRemoteBranchName("master").setCredentialsProvider(new UsernamePasswordCredentialsProvider(USER, PASSWORD)).call();
            resultFlag = true;
        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
        }
        return resultFlag;
    }

    //提交git
    public static boolean commitFiles() {
        Git git = null;
        try {
            git = Git.open(new File(LOCAL_GIT_FILE));
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
            git.commit().setMessage("commit").call();//推送
            git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(USER, PASSWORD)).call();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        setupRepo();
        pullBranchToLocal();
        commitFiles();
    }
}
