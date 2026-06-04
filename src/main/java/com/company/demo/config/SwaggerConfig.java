package com.company.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

/**
 * @author jiaolei
 * @date 2026/6/4 17:44
 * @description http://localhost:8080/swagger-ui/index.html
 */
public class SwaggerConfig {
    public OpenAPI customOpenAPI(){
        return new OpenAPI()
                .info(new Info()
                        .title("用户管理系统 API")
                        .version("1.0")
                        .description("用户管理系统的后端接口文档")
                        .contact(new Contact()
                                .name("leij56789")
                                .email("leij123321@outlook.com")));
    }
}
