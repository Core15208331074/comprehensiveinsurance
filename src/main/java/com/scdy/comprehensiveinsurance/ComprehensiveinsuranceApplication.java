package com.scdy.comprehensiveinsurance;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.optimize.JsqlParserCountOptimize;
import io.github.yedaxia.apidocs.Docs;
import io.github.yedaxia.apidocs.DocsConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.File;
import java.io.IOException;

@SpringBootApplication
@MapperScan("com.scdy.comprehensiveinsurance.dao")
//@ComponentScan(basePackages="com.scdy.comprehensiveinsurance")//扫描common工程下的类
//@IntegrationComponentScan
public class ComprehensiveinsuranceApplication {


    public static void main(String[] args) {
        SpringApplication.run(ComprehensiveinsuranceApplication.class, args);

        try {
            DocsConfig config = new DocsConfig();
            config.setProjectPath(System.getProperty("user.dir")); // 项目根目录
            config.setProjectName("comprehensiveinsurance"); // 项目名称
            config.setApiVersion("V1.0.0");       // 声明该API的版本
            config.setDocsPath(new File("src/main/resources/").getCanonicalPath()); // 生成API 文档所在目录
            config.setAutoGenerate(Boolean.TRUE);  // 配置自动生成
            Docs.buildHtmlDocs(config); // 执行生成文档
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Bean
    public PaginationInterceptor paginationInterceptor() {
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        // 设置请求的页面大于最大页后操作， true调回到首页，false 继续请求  默认false
        // paginationInterceptor.setOverflow(false);
        // 设置最大单页限制数量，默认 500 条，-1 不受限制
        // paginationInterceptor.setLimit(500);
        // 开启 count 的 join 优化,只针对部分 left join
        paginationInterceptor.setCountSqlParser(new JsqlParserCountOptimize(true));
        return paginationInterceptor;
    }

}
