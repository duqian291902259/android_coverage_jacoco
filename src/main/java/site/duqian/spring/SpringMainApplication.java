package site.duqian.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import site.duqian.spring.utils.CmdUtil;
import site.duqian.spring.utils.FileUtils;
import site.duqian.spring.utils.SpringContextUtil;

import java.io.File;

@SpringBootApplication
public class SpringMainApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringMainApplication.class, args);

        SpringContextUtil.get().execute(new Runnable() {
            @Override
            public void run() {
                String path = FileUtils.getJacocoDownloadDir() + File.separator;
                System.out.println("path=" + path);
                CmdUtil.executeShellCmd("cmdShell.sh", path);
            }
        });
    }
}
