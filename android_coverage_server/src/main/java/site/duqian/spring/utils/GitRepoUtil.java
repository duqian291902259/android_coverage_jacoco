package site.duqian.spring.utils;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import site.duqian.spring.Constants;
import site.duqian.spring.bean.CommonParams;
import site.duqian.spring.exception.CustomException;

import java.io.File;

/**
 * Description:git工具类
 *
 * @author n20241 Created by 杜小菜 on 2021/9/30 - 11:50 .
 * E-mail: duqian2010@gmail.com
 */
public class GitRepoUtil {

    private static final Logger Logger = LoggerFactory.getLogger(GitRepoUtil.class);

    public static void checkOut(CommonParams commonParams) {
        try {
            String cmd = "git checkout -b " + commonParams.getBranchName() + " " + commonParams.getCommitId();
            System.out.println("runProcess checkOut cmd:" + cmd);
            int result = CmdUtil.runProcess(cmd);
            System.out.println("checkOut end:" + result);
        } catch (Exception e) {
            System.out.println("checkOut error:" + e);
        }
    }

    public static boolean cloneSrc(CommonParams commonParams) {
        String sourceDir = FileUtils.getGitCloneDir(commonParams);
        boolean checkGitWorkSpace = GitRepoUtil.checkGitWorkSpace(Constants.REPOSITORY_URL, sourceDir);
        //todo 源码重复下载的问题
        System.out.println("cloneSrc " + checkGitWorkSpace);
        try {
            String cmd = "";
            String cmdPull = "git -C " + sourceDir + " pull";
            if (!checkGitWorkSpace) {
                cmd = "git clone -b " + commonParams.getBranchName() + " " + Constants.REPOSITORY_URL + " " + sourceDir;
            } else {
                cmd = cmdPull;
            }
            System.out.println("runProcess cmd:" + cmd);
            int result = CmdUtil.runProcess(cmd);
            if (!checkGitWorkSpace && result == 128) {
                result = CmdUtil.runProcess(cmdPull);
                System.out.println("cmdPull:" + result);
            }
            if (result == -1 || result == 128) {
                return false;
            }
            System.out.println("clone or update end:" + result);
            return true;
        } catch (Exception e) {
            System.out.println("clone or update error:" + e);
        }
        return false;
    }

    /**
     * 获取当前分支最新的commit id
     */
    public static String getCurrentCommitId() {
        try {
            int len = "c02f0bbe4a102ffbc85bc91e8fb421bccfefdb39".length();
            String cmd = Constants.GIT_GET_CURRENT_COMMIT_SHA;
            String result = CmdUtil.execute(cmd);
            Logger.debug(len + ",cmd:" + cmd + ",result=" + result);
            String tag = "commit ";
            int index = result.indexOf(tag);
            if (index >= 0) {
                return result.substring(index + tag.length(), len+tag.length());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 克隆代码到本地
     */
    public static Git cloneRepository(String gitUrl, String codePath, String commitId, String gitUserName, String gitPassWord) {
        Git git = null;
        try {
            if (!checkGitWorkSpace(gitUrl, codePath)) {
                boolean delete = new File(codePath).delete();
                System.out.printf("本地代码不存在，clone=%s,codePath=%s,delete=" + delete + "%n", gitUrl, codePath);
                git = Git.cloneRepository()
                        .setURI(gitUrl)
                        .setCredentialsProvider(new UsernamePasswordCredentialsProvider(gitUserName, gitPassWord))
                        .setDirectory(new File(codePath))
                        .setBranch(commitId)
                        .call();
                // 下载指定commitId/branch
                git.checkout().setName(commitId).call();
            } else {
                System.out.println("本地代码存在,直接使用" + codePath);
                git = Git.open(new File(codePath));
                git.getRepository().getFullBranch();
                //判断是分支还是commitId，分支做更新，commitId无法改变用原有的
                //todo-dq if (git.getRepository().exactRef("HEAD").isSymbolic()) {
                //更新代码
                git.pull().setCredentialsProvider(new UsernamePasswordCredentialsProvider(gitUserName, gitPassWord)).call();
                // }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return git;
    }

    /**
     * 将代码转成树状
     */
    public static AbstractTreeIterator prepareTreeParser(Repository repository, String branchName) {
        try {
            RevWalk walk = new RevWalk(repository);
            RevTree tree;
            if (null == repository.resolve(branchName)) {
                throw new CustomException(100, "PARSE_BRANCH_ERROR");
            }
            tree = walk.parseTree(repository.resolve(branchName));
            CanonicalTreeParser treeParser = new CanonicalTreeParser();
            ObjectReader reader = repository.newObjectReader();
            treeParser.reset(reader, tree.getId());
            walk.dispose();
            return treeParser;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 判断工作目录是否存在，可以每次拉去代码时删除再拉取，但是代码多IO大，所以代码可以复用
     */
    public static boolean checkGitWorkSpace(String gitUrl, String codePath) {
        boolean isExist = false;
        File gitRootFile = new File(codePath);
        try {
            Git git = Git.open(gitRootFile);
            Repository repository = git.getRepository();
            //解析本地代码，获取远程uri,是否是我们需要的git远程仓库
            String repoUrl = repository.getConfig().getString("remote", "origin", "url");
            if (gitUrl.equals(repoUrl)) {
                isExist = true;
            } else {
                Logger.info("本地存在其他仓的代码，先删除");
                FileUtils.deleteDirectory(codePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
            gitRootFile.delete();
        }
        return isExist;
    }

    /**
     * 获取class文件的地址
     */
    public String getClassFile(Git git, String classPackage) {
        return git.getRepository().getDirectory().getParent() + "/" + classPackage;
    }

}
