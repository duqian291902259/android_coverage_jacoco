package site.duqian.spring;

import org.springframework.boot.ExitCodeEvent;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import site.duqian.spring.manager.ThreadManager;
import site.duqian.spring.utils.CmdUtil;
import site.duqian.spring.utils.CommonUtils;
import site.duqian.spring.utils.FileUtils;
import site.duqian.spring.utils.SpringContextUtil;

@SpringBootApplication
public class JacocoMainApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(JacocoMainApplication.class, args);
        //System.exit(SpringApplication.exit(context));
        initWhenLaunch();
    }

    private static void initWhenLaunch() {
        ThreadManager.getBackgroundPool().execute(() -> {
            //SpringContextUtil.get().execute(() -> {
            //String path = FileUtils.getReportRootDir();
            String path = FileUtils.getRootDir();
            System.out.println("initWhenLaunch 2.0.0 getJacocoDownloadDir=" + path);
            System.out.println("please open localServer=" + Constants.LOCAL_SERVER_HOST_URL);
            System.out.println("or open remoteServer url=" + Constants.JACOCO_SERVER_HOST_URL);
            System.out.println("report local http-server=" + Constants.LOCAL_REPORT_HOST_URL);
            //handleExit();
            //FileUtils.removeCacheFile(path);
            //只是本地调试启动服务，服务器部署后，这个镜像不安装node了。会另外启动http-server服务（因为java:8-jdk-alpine镜像的node版本太低6.7.0,无法正常打开静态页面，尝试过在这个系统里面升级node但是无果）
            CmdUtil.executeShellCmd("cmdShell.sh", path);
        });
    }

    @Bean
    public ExitCodeGenerator exitCodeGenerator() {
        handleExit();
        return () -> 666;
    }

    private static void handleExit() {
        if (!CommonUtils.isWindowsOS()) {
            return;
        }
        try {
            System.out.println("exitCodeGenerator");
            String command = "killall node";
            if (CommonUtils.isWindowsOS()) {//成功: 已终止进程 "node.exe"，其 PID 为 27844。
                command = "taskkill /f /im node.exe";
            }
            //CmdUtil.runProcess(command);
            String execCmd = CmdUtil.execCmd(command);
            System.out.println("exitCodeGenerator kill server: " + command + ",result=" + execCmd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Bean
    ExitListener ExitListenerBean() {
        return new ExitListener();
    }

    private static class ExitListener {
        @EventListener
        public void exitEvent(ExitCodeEvent event) {
            System.out.println("Exit code: " + event.getExitCode());
        }
    }

}
