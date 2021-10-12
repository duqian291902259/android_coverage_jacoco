package site.duqian.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import site.duqian.spring.utils.CmdUtil;
import site.duqian.spring.utils.FileUtil;
import site.duqian.spring.utils.SpringContextUtil;

import java.io.File;
import java.io.IOException;

@SpringBootApplication
public class SpringMainApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringMainApplication.class, args);

        SpringContextUtil.get().execute(new Runnable() {
            @Override
            public void run() {
                String path = FileUtil.getReportRootDir() + File.separator;
                System.out.println("path=" + path);
                CmdUtil.executeShellCmd("cmdShell.sh", path);
            }
        });
    }
}
