package coinche7.config;

import coinche7.security.AuthTokenFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<AuthTokenFilter> authTokenFilterRegistration(AuthTokenFilter authTokenFilter) {
        FilterRegistrationBean<AuthTokenFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(authTokenFilter);
        registrationBean.addUrlPatterns("/api/private/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }
}
