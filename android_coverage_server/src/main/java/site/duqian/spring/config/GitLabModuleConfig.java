/*
package site.duqian.spring.config;

import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import site.duqian.spring.Constants;
import site.duqian.spring.gitlab.GitlabApi;
import site.duqian.spring.gitlab.GitlabConstants;
import java.util.concurrent.TimeUnit;

@Configuration
public class GitLabModuleConfig {
    @Bean
    OkHttpClient.Builder provideOkHttpClientBuilder() {
        return new OkHttpClient.Builder()
                .connectTimeout(120L, TimeUnit.SECONDS)
                .readTimeout(120L, TimeUnit.SECONDS)
                .writeTimeout(120L, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true);
    }

    @Bean
    OkHttpClient provideOkHttpClient(OkHttpClient.Builder builder) {
        return builder.build();
    }

    @Bean(name = Constants.GIT_LAB_BEAN_NAME)
    GitlabApi provideGitlabApi(OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(GitlabConstants.GITLAB_API_URL)
                //.addConverterFactory(GsonConverterFactory.create())
                //.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(GitlabApi.class);
    }
}*/
