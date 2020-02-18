package cn.zongkuiy.grafana4tb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.thingsboard.server.dao.audit.AuditLogLevelFilter;

import java.util.HashMap;

@Configuration
public class ServerConfiguration extends WebMvcConfigurationSupport{

    @Bean
    public AuditLogLevelFilter emptyAuditLogLevelFilter() {
        return new AuditLogLevelFilter(new HashMap<>());
    }
    
    @Bean
    public CorsFilter corsFilter() throws Exception {
        return new CorsFilter();
    }
    
}
