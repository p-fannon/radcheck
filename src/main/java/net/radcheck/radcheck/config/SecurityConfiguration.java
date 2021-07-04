package net.radcheck.radcheck.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private DataSource dataSource;

    @Value("${spring.queries.users-query}")
    private String usersQuery;

    @Value("${spring.queries.roles-query}")
    private String rolesQuery;

    @Override
    protected void configure(AuthenticationManagerBuilder auth)
            throws Exception {
        auth.
                jdbcAuthentication()
                .usersByUsernameQuery(usersQuery)
                .authoritiesByUsernameQuery(rolesQuery)
                .dataSource(dataSource)
                .passwordEncoder(bCryptPasswordEncoder);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.
                cors().and()
                .authorizeRequests()
                .antMatchers("/").permitAll()
                .antMatchers("/**").permitAll()
                .antMatchers("/images").permitAll()
                .antMatchers("/js").permitAll()
                .antMatchers("/login").permitAll()
                .antMatchers("/registration").permitAll()
                .antMatchers("/search").permitAll()
                .antMatchers("/manual").permitAll()
                .antMatchers("/about").permitAll()
                .antMatchers("/privacy").permitAll()
                .antMatchers("/api/**").permitAll()
                .antMatchers("/view/**").hasAnyAuthority("USER", "ADMIN")
                .antMatchers("/confirm").hasAnyAuthority("USER", "ADMIN")
                .antMatchers("/save").hasAnyAuthority("USER", "ADMIN")
                .antMatchers("/edit").hasAnyAuthority("USER", "ADMIN")
                .antMatchers("/delete").hasAnyAuthority("USER", "ADMIN")
                .antMatchers("/two-by-two").hasAnyAuthority("USER", "ADMIN")
                .antMatchers("/three-by-three").hasAnyAuthority("USER", "ADMIN")
                .antMatchers("/four-by-four").hasAnyAuthority("USER", "ADMIN")
                .antMatchers("/profile").hasAnyAuthority("USER", "ADMIN")
                .antMatchers("/admin/**").hasAuthority("ADMIN").anyRequest()
                .authenticated().and().csrf().disable().formLogin()
                .loginPage("/login").failureUrl("/login?error=true")
                .defaultSuccessUrl("/search?logon=true")
                .usernameParameter("email")
                .passwordParameter("password")
                .and().logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/?logout=true").and()
                .logout().deleteCookies("JSESSIONID").and()
                .rememberMe().key("uniqueAndSecret").alwaysRemember(true)
                .and().exceptionHandling()
                .accessDeniedPage("/?denied=true");
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web
                .ignoring()
                .antMatchers("/resources/**", "/static/**", "/css/**", "/js/**", "/images/**");
    }
}
