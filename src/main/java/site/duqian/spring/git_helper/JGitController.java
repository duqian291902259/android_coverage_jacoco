package site.duqian.spring.git_helper;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@RestController
public class JGitController {
    private static final Logger log = LoggerFactory.getLogger(JGitController.class);// slf4j日志记录器
    /**
     * git仓路径
     */
    private static final String gitLocalDir = "/Users/duqian/Development/MyGitHub/springweb/git/";
    private static final String gitLocalPath = "/Users/duqian/Development/MyGitHub/AndroidUI/.git";
    private static final String gitUrl = "https://github.com/duqian291902259/AndroidUI.git";

    /**
     * 代码分支
     */
    private static final String branch = "origin/main";
    private static final String gitUserName = "duqian291902259";
    private static final String gitPassword = "";

    /**
     * 拉取
     */
    @RequestMapping("/pull")
    public String pull() {
        String result;
        Repository repo = null;
        try {
            repo = new FileRepository(new File(gitLocalPath));
            Git git = new Git(repo);
            log.info("开始重置");
            //重置
            git.reset()
                    .setMode(ResetCommand.ResetType.HARD)
                    .setRef(branch).call();

            log.info("开始拉取");
            //拉取
            git.pull()
                    .setRemote("origin")
                    .setRemoteBranchName("main")
                    .call();
            result = "拉取成功!";
            log.info(result);
        } catch (Exception e) {
            result = e.getMessage();
        } finally {
            if (repo != null) {
                repo.close();
            }
        }
        return result;
    }

    /**
     * 重置
     **/
    @RequestMapping("/reset")
    public String reset() {
        String result;
        Repository repo = null;
        try {
            repo = new FileRepository(new File(gitLocalPath));
            Git git = new Git(repo);
            git.reset().setMode(ResetCommand.ResetType.HARD).setRef(branch).call();
            result = "重置成功!";
        } catch (Exception e) {
            result = e.getMessage();
        } finally {
            if (repo != null) {
                repo.close();
            }
        }
        return result;
    }

    /**
     * 恢复
     */
    @RequestMapping("/revert")
    public String revert() {
        String result;
        Repository repo = null;
        try {
            repo = new FileRepository(new File(gitLocalPath));
            Git git = new Git(repo);
            git.revert().call();
            result = "恢复成功!";
        } catch (Exception e) {
            result = e.getMessage();
        } finally {
            if (repo != null) {
                repo.close();
            }
        }
        return result;
    }

    /**
     * 克隆
     */
    @RequestMapping("/clone")
    public String clone() {
        String result;
        try {
            /*Git.cloneRepository()
                    .setURI(gitUrl)
                    .setDirectory(new File(System.getProperty("user.dir") + "/git/"))
                    .call();*/

            GitRepoUtil.cloneRepository(gitUrl, gitLocalDir, "ed512db04d45c5a1148658fef775b6ac9aec846a", gitUserName, gitPassword);
            result = "克隆成功了!";
        } catch (Exception e) {
            result = e.getMessage();
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 状态
     */
    @RequestMapping("/status")
    public static void status() {
        File RepoGitDir = new File("/git/.git");
        Repository repo = null;
        try {
            repo = new FileRepository(RepoGitDir.getAbsolutePath());
            Git git = new Git(repo);
            Status status = git.status().call();
            log.info("Git Change: " + status.getChanged());
            log.info("Git Modified: " + status.getModified());
            log.info("Git UncommittedChanges: " + status.getUncommittedChanges());
            log.info("Git Untracked: " + status.getUntracked());
        } catch (Exception e) {
            log.info(e.getMessage());
        } finally {
            if (repo != null) {
                repo.close();
            }
        }
    }
}