package site.duqian.spring.git_helper;

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
import site.duqian.spring.utils.FileUtil;
import site.duqian.spring.exception.CustomException;

import java.io.File;
import java.io.IOException;

public class GitRepoUtil {

    private static final Logger Logger = LoggerFactory.getLogger(GitRepoUtil.class);

    /**
     * 克隆代码到本地
     */
    public static Git cloneRepository(String gitUrl, String codePath, String commitId, String gitUserName, String gitPassWord) {
        Git git = null;
        try {
            if (!checkGitWorkSpace(gitUrl, codePath)) {
                boolean delete = new File(codePath).delete();
                System.out.println(String.format("本地代码不存在，clone=%s,codePath=%s,delete=" + delete, gitUrl, codePath));
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
                //todo-dq if (git.getRepository().exactRef(Constants.HEAD).isSymbolic()) {
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
     * 判断工作目录是否存在，本来可以每次拉去代码时删除再拉取，但是这样代码多的化IO比较大，所以就代码可以复用
     */
    public static boolean checkGitWorkSpace(String gitUrl, String codePath) throws IOException {
        boolean isExist = false;
        File gitRootFile = new File(codePath);
        try {
            File repoGitDir = new File(codePath + "/.git");
            if (!repoGitDir.exists()) {
                return false;
            }
            Git git = Git.open(gitRootFile);
            Repository repository = git.getRepository();
            //解析本地代码，获取远程uri,是否是我们需要的git远程仓库
            String repoUrl = repository.getConfig().getString("remote", "origin", "url");
            if (gitUrl.equals(repoUrl)) {
                isExist = true;
            } else {
                Logger.info("本地存在其他仓的代码，先删除");
                FileUtil.removeDir(gitRootFile);
                gitRootFile.delete();
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
        StringBuilder builder = new StringBuilder(git.getRepository().getDirectory().getParent());
        return builder.append("/").append(classPackage).toString();
    }

}
