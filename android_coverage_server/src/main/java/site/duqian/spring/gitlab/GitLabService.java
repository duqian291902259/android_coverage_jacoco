package site.duqian.spring.gitlab;

import com.google.gson.JsonObject;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import site.duqian.spring.Constants;
import site.duqian.spring.bean.CommonParams;
import site.duqian.spring.utils.SpringContextUtil;
import site.duqian.spring.utils.TextUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class GitLabService {
    //@Autowired//需要为bean
    private GitlabApi gitlabApi;

    private static final Logger logger = LoggerFactory.getLogger(GitLabService.class);
    private static final int PROJECT_ID_CC_ANDROID = 1323;
    private static final int PROJECT_ID_CC_AUDIO = 5356;
    private static final int PROJECT_ID_CC_COVERAGE_PLUGIN = 7736;
    private static final int PROJECT_ID_CC_TASK_PLUGIN = 7934;
    private int mProjectId = PROJECT_ID_CC_ANDROID;

    public static GitLabService getGitlabService() {
        return SpringContextUtil.getBean(GitLabService.class);
    }

    public GitlabApi getGitlabApi() {
        if (gitlabApi == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(GitlabConstants.GITLAB_API_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    //.addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .build();
            gitlabApi = retrofit.create(GitlabApi.class);
        }
        return gitlabApi;
    }

    public Response<JsonObject> testGitlabApi() {
        try {
            GitlabApi gitlabApi = getGitlabApi();
            Callback<ResponseBody> callback = new Callback<ResponseBody>() {

                @Override
                public void onResponse(@NotNull Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        logger.info("getBranchByName response=" + response.body().string());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable throwable) {
                    logger.info("getBranchByName error=" + throwable);
                }
            };
            String branchName = "dev_dq_#411671_coverage";
            Call<ResponseBody> response = gitlabApi.getBranchByName(mProjectId, branchName);
            //Call<ResponseBody> response2 = gitlabApi.getBranchBySearch(mProjectId, branchName);
            //Call<ResponseBody> response3 = gitlabApi.getGitInfo();
            //dev
            //Call<ResponseBody> response4 = gitlabApi.getCompareCommits(mProjectId,"16ba6db0f531414097bcb7e389977a10d513c605","fca16af02910b7bc4343b0df9684afe61c092f51",mProjectId,true);
            Call<ResponseBody> response4 = gitlabApi.getCompareCommits(mProjectId, "16ba6db0f531414097bcb7e389977a10d513c605", "fca16af02910b7bc4343b0df9684afe61c092f51", 0, false);
            //感兴趣的嘉宾
            //Call<ResponseBody> response4 = gitlabApi.getCompareCommits(mProjectId,"439494e53f893167cbae3cba706cf8422e2b3390","3178934700111813e2b8425b97da5afa0f7f9409",mProjectId,true);
            //Call<ResponseBody> response4 = gitlabApi.getCompareCommits(mProjectId,"16ba6db0","31789347",mProjectId,true);

            response.enqueue(callback);
            //response2.enqueue(callback);
            //response3.enqueue(callback);
            response4.enqueue(callback);

            getGitDiffInfo(null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void getGitDiffInfo(CommonParams commonParams, DiffCallBack callBack) {
        //936 classes
        //Call<GitLabDiffBean> response = getGitlabApi().getCompareDiffBean(mProjectId, "dev_dq_#411671_coverage", "dev", mProjectId, true);
        //Call<GitLabDiffBean> response = getGitlabApi().getCompareDiffBean(mProjectId, "439494e5", "31789347", mProjectId, true);
        //4766
        //Call<GitLabDiffBean> response = getGitlabApi().getCompareDiffBean(mProjectId, "21acf983", "84f1ad08", mProjectId, true);

        //2022/3/31 根据项目决定projectId
        initProjectId(commonParams);

        Call<GitLabDiffBean> response = getGitlabApi().getCompareDiffBean(mProjectId, "458e6b6a", "15c5e627", mProjectId, true);
        Call<GitLabDiffBean> response2 = getGitlabApi().getCompareDiffBean(mProjectId, "458e6b6a", "9ba0b2ea", mProjectId, true);
        Callback<GitLabDiffBean> diffBeanCallback = new Callback<GitLabDiffBean>() {

            @Override
            public void onResponse(@NotNull Call<GitLabDiffBean> call, Response<GitLabDiffBean> response) {
                try {
                    GitLabDiffBean gitLabDiffBean = response.body();
                    List<String> diffFiles = new ArrayList<>();
                    if (gitLabDiffBean != null) {
                        List<GitLabDiffBean.DiffsDTO> diffs = gitLabDiffBean.getDiffs();
                        if (diffs != null && diffs.size() > 0) {
                            for (GitLabDiffBean.DiffsDTO diffItem : diffs) {
                                String newPath = diffItem.getNewPath();
                                //logger.info("getCompareDiffBean newPath=" + newPath);
                                diffFiles.add(newPath);
                            }
                            logger.info("getCompareDiffBean diffs.size=" + diffs.size());
                        }
                        logger.info("getCompareDiffBean response=" + response.code());
                    } else {
                        logger.info("getCompareDiffBean null");
                    }
                    if (callBack != null) {
                        callBack.onDiff(diffFiles);
                    }
                } catch (Exception e) {
                    logger.info("getGitDiffInfo error " + e + ",request=" + commonParams);
                    if (callBack != null) {
                        callBack.onDiff(null);
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<GitLabDiffBean> call, @NotNull Throwable throwable) {
                logger.info("getGitDiffInfo error=" + throwable + ",request=" + commonParams);
                if (callBack != null) {
                    callBack.onDiff(null);
                }
            }
        };
        response.enqueue(diffBeanCallback);
        response2.enqueue(diffBeanCallback);
    }

    private void initProjectId(CommonParams commonParams) {
        String appName = commonParams.getAppName();
        if (Constants.APP_CC_ANDROID.equalsIgnoreCase(appName)) {
            mProjectId = PROJECT_ID_CC_ANDROID;
        } else if (Constants.APP_CC_AUDIO.equalsIgnoreCase(appName)) {
            mProjectId = PROJECT_ID_CC_AUDIO;
        } else if (Constants.APP_CC_COVERAGE.equalsIgnoreCase(appName)) {
            mProjectId = PROJECT_ID_CC_COVERAGE_PLUGIN;
        } else if (Constants.APP_CC_TASK_DEMO.equalsIgnoreCase(appName)) {
            mProjectId = PROJECT_ID_CC_TASK_PLUGIN;
        }
    }

    /**
     * 同步获取
     */
    public List<String> getGitDiffInfoSync(CommonParams commonParams) {
        List<String> diffFiles = new ArrayList<>();
        try {
            //2022/3/31 根据项目决定projectId
            initProjectId(commonParams);

            Call<GitLabDiffBean> response = null;
            boolean isDiffBranch = !TextUtils.isEmpty(commonParams.getBaseBranchName());
            if (isDiffBranch) {
                response = getGitlabApi().getCompareDiffBean(mProjectId, commonParams.getBranchName(), commonParams.getBaseBranchName(), mProjectId, true);
            } else {
                response = getGitlabApi().getCompareDiffBean(mProjectId, commonParams.getCommitId(), commonParams.getCommitId2(), mProjectId, true);
            }
            logger.info("getGitDiffInfoSync isDiffBranch=" + isDiffBranch + ",request=" + commonParams);

            Response<GitLabDiffBean> diffBeanResponse = response.execute();
            GitLabDiffBean gitLabDiffBean = diffBeanResponse.body();
            if (gitLabDiffBean != null) {
                List<GitLabDiffBean.DiffsDTO> diffs = gitLabDiffBean.getDiffs();
                if (diffs != null && diffs.size() > 0) {
                    for (GitLabDiffBean.DiffsDTO diffItem : diffs) {
                        String newPath = diffItem.getNewPath();
                        if (newPath != null && (newPath.endsWith(".java") || newPath.endsWith(".kt"))) {
                            logger.info("getCompareDiffBean newPath=" + newPath);
                            diffFiles.add(newPath);
                        }
                    }
                    logger.info("getCompareDiffBean diffs.size=" + diffs.size());
                }
            } else {
                logger.info("getCompareDiffBean null");
            }
        } catch (Exception e) {
            logger.info("getGitDiffInfoSync error=" + e + ",request=" + commonParams);
        }
        return diffFiles;
    }
}