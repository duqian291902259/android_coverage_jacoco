package site.duqian.spring.config;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import site.duqian.spring.Constants;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@SpringBootConfiguration
@EnableAsync(proxyTargetClass = true) // 强制使用CGLIB代理
public class ExecutorConfig implements AsyncConfigurer {

    @Bean(name = Constants.THREAD_EXECUTOR_NAME)
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        // 线程池核心容量
        taskExecutor.setCorePoolSize(1);
        // 线程池最大容量
        taskExecutor.setMaxPoolSize(20);
        // 等候线容量
        taskExecutor.setQueueCapacity(50);
        // 空闲存活时间
        taskExecutor.setKeepAliveSeconds(60);
        // 被拒绝任务的处理程序，直接在{@code execute}方法的调用线程中运行被拒绝的任务，除非执行程序已关闭，在这种情况下，任务将被丢弃。
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 初始化
        taskExecutor.initialize();
        return taskExecutor;
    }
}
