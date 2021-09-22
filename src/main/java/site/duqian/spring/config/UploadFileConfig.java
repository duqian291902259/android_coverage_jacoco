package site.duqian.spring.config;

import javax.servlet.MultipartConfigElement;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

@Configuration
public class UploadFileConfig {
    /**
     * 文件上传配置
     * factory.setSizeThreshold(300 * 1024 * 1024);//无效
     */
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        //文件最大
        factory.setMaxFileSize(DataSize.parse(300 * 1024 + "KB")); //KB,MB
        // 设置总上传数据总大小
        factory.setMaxRequestSize(DataSize.parse(10 * 1024 * 1024 + "KB"));
        return factory.createMultipartConfig();
    }
}