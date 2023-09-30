package com.framework.security.config;

import com.framework.common.utils.security.Md5Utils;
import com.framework.security.JwtUserServiceImpl;
import com.framework.security.handler.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * @version V1.0
 * @program: teamwork
 * @package: com.framework.security.config
 * @description: Permission configuration class
 * @create: 2020-06-25 14:39
 **/
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${custom.ignored-role-path}")
    private String[] strings;
    /*zedtou*/
    @Value("${security-configuration}")
    private boolean securityConfiguration;
    /*zedtou*/


    @Autowired
    private JwtUserServiceImpl hrService;

    /**
     * token authentication filter
     */
    @Autowired
    private JwtAuthorizationFilter authorizationFilter;

    /**
     * Solution can not be injected directly AuthenticationManager
     *
     * @return
     * @throws Exception
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    /**
     * @Description: Configure the data source of userDetails, password encryption format
     * @Date: 2019/3/28-9:24
     * @Param: [auth]
     * @return: void
     **/
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(hrService).passwordEncoder(new PasswordEncoder() {

            @Override
            public String encode(CharSequence rawPassword) {
                return Md5Utils.hash((String) rawPassword);
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return encodedPassword.equals(Md5Utils.hash((String) rawPassword));
            }
        });
        auth.userDetailsService(hrService)
                .passwordEncoder(new BCryptPasswordEncoder());
    }

    /**
     * @Description:Configure released resources
     * @Date: 2019/3/28-9:23
     * @Param: [web]
     * @return: void
     **/
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/js/**")
        // Release swagger; resources that do not require permission to access
        .antMatchers("/swagger-ui.html", "/swagger-resources/**", "/images/**", "/webjars/**", "/v2/api-docs", "/configuration/ui", "/configuration/security");
    }

    /**
     * @Description: HttpSecurity Contains original data (mainly url)
     * Inject MyFilterInvocationSecurityMetadataSource and MyAccessDecisionManager through withObjectPostProcessor
     * This url is first processed by MyFilterInvocationSecurityMetadataSource, and then thrown to MyAccessDecisionManager for processing
     * If there is no match, return MyAccessDeniedHandler
     * @Date: 2019/3/27-17:41
     * @Param: [http]
     * @return: void
     **/

    @Override
    protected void configure(HttpSecurity http) throws Exception {
    	if(securityConfiguration) {
    		http
            .authorizeRequests().anyRequest().permitAll();
    	}else {
        http.cors().and().csrf().disable()
                .authorizeRequests().antMatchers(strings).permitAll()
                .anyRequest().authenticated()
                .and()
                .logout().logoutUrl("/project/login/_out").logoutSuccessHandler(new MyLogoutSuccessHandler()).invalidateHttpSession(true).and()
                // no session required
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(new JwtAuthenticationEntryPoint());
        // Add JWT filter
        http.addFilterBefore(authorizationFilter, UsernamePasswordAuthenticationFilter.class);
        http.headers().frameOptions().disable();
    }
    }

}
