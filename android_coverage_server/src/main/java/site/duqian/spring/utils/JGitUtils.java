package site.duqian.spring.utils;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import site.duqian.spring.Constants;
import site.duqian.spring.manager.ThreadManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class JGitUtils {
    public static final String LOCAL_GIT_ROOT_PATH = FileUtils.getRootDir() + File.separator + Constants.GIT_SOURCE_DIR_NAME + File.separator;
    public static final String LOCAL_MASTER_PATH = LOCAL_GIT_ROOT_PATH + Constants.GIT_SOURCE_MASTER + File.separator;
    public static final String LOCAL_MASTER_GIT_PATH = LOCAL_MASTER_PATH + Constants.GIT_DATA_DIR_NAME;

    public static CredentialsProvider getProvider() {
        UsernamePasswordCredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider("oauth2", "fQxsftqfUYL3Uz9rHsPs");
        //UsernamePasswordCredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(email, password);
        return credentialsProvider;
    }

    public static boolean cloneProject(String gitUrl) {
        try {
            String localPath = LOCAL_MASTER_PATH;
            if (Files.exists(Paths.get(localPath))) {
                FileUtils.deleteDirectory(localPath);
            }
            Files.createDirectories(Paths.get(localPath));
            System.out.println("localPath=" + localPath);

            Git call = Git.cloneRepository().setURI(gitUrl)
                    .setCloneAllBranches(false)
                    .setCredentialsProvider(getProvider())
                    .setBranch(Constants.GIT_SOURCE_MASTER)
                    .setDirectory(new File(localPath))
                    .call();

            //List<Ref> branchList = call.branchList().call();
            //System.out.println("branchList=" + branchList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void diffMethod(String Child, String Parent) {
        Git git = null;
        try {
            git = Git.open(new File(LOCAL_MASTER_GIT_PATH));
            Repository repository = git.getRepository();
            ObjectReader reader = repository.newObjectReader();
            CanonicalTreeParser treeParser = new CanonicalTreeParser();
            ObjectId old = repository.resolve(Child + "^{tree}");
            ObjectId head = repository.resolve(Parent + "^{tree}");
            treeParser.reset(reader, old);
            CanonicalTreeParser newTreeParser = new CanonicalTreeParser();
            newTreeParser.reset(reader, head);
            List<DiffEntry> diffs = git.diff()
                    .setNewTree(newTreeParser)
                    .setOldTree(treeParser)
                    .call();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DiffFormatter df = new DiffFormatter(out);
            df.setRepository(git.getRepository());
            for (DiffEntry diffEntry : diffs) {
                df.format(diffEntry);
                String diffText = out.toString("UTF-8");
                System.out.println("diffText" + diffText);
                System.out.println("diff new path=" + diffEntry.getNewPath());
            }
            out.close();
            df.close();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ThreadManager.getBackgroundPool().execute(() -> {
            String repoUrl = Constants.REPOSITORY_URL;
            //cloneProject(repoUrl);
            diffMethod("Child", "Parent");
        });
    }
}

