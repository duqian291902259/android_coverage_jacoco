package site.duqian.spring.config;

import javax.servlet.MultipartConfigElement;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.util.unit.DataUnit;

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
        factory.setMaxFileSize(DataSize.parse("5", DataUnit.GIGABYTES)); //GB
        // 设置总上传数据总大小
        factory.setMaxRequestSize(DataSize.parse("1000", DataUnit.GIGABYTES));//GB
        return factory.createMultipartConfig();
    }
}