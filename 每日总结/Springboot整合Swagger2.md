#### **引入依赖**

```xml
<!-- Swagger-->
<dependency>
    <groupId>io.springfox</groupId>
    <artifactId>springfox-swagger2</artifactId>
    <version>2.6.0</version>
</dependency>
<dependency>
    <groupId>io.springfox</groupId>
    <artifactId>springfox-swagger-ui</artifactId>
    <version>2.6.0</version>
</dependency>
```

#### **编写配置文件**

```java
package com.cmft.cmuop.shared.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableSwagger2
//用于控制Swagger开关
@ConditionalOnProperty(name = "swagger.enable", havingValue = "true")
public class Swagger2Config {
    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()  .apis(RequestHandlerSelectors.basePackage("com.cmft.cmuop.management.presentation.rest"))
                .paths(PathSelectors.any())
                .build()
                .globalOperationParameters(setHeaderToken());  //配置全局Header Token
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("招商随行后台管理API")
                .description("招商随行后台管理API")
                .termsOfServiceUrl("NO terms of service")
                .version("1.0")
                .build();
    }

	//全局全局Header Token配置函数
    private List<Parameter> setHeaderToken() {
        ParameterBuilder tokenPar = new ParameterBuilder();
        List<Parameter> params = new ArrayList<>();
        tokenPar.name("Authorization").description("NUC Token").modelRef(new ModelRef("string")).parameterType("header").required(false).build();
        params.add(tokenPar.build());
        return params;
    }

}
```

#### **添加资源映射**

```java
package com.cmft.cmuop.shared;

import java.util.Date;
import java.util.List;

import com.cmft.cmuop.shared.common.CustomJsonDateDeserializer;
import com.cmft.cmuop.shared.common.CustomJsonDateSerializer;
import com.cmft.marathon.config.AclConfig;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

@Configuration
@EnableWebMvc
public class WebConfig extends AclConfig {


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
        super.addResourceHandlers(registry);
    }


}
```

#### **添加配置文件**

```properties
# 生产环境注释此配置则关闭Swagger
swagger.enable = true
```

#### **Swagger页面地址**

http://localhost:8080/context-path/swagger-ui.html

