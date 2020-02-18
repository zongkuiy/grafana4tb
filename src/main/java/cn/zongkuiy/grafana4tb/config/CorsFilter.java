package cn.zongkuiy.grafana4tb.config;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

public class CorsFilter extends OncePerRequestFilter {
	 
    static final String ORIGIN = "Origin";
 
    protected void doFilterInternal(
        HttpServletRequest request, 
        HttpServletResponse response, 
        FilterChain filterChain) throws ServletException, IOException {
    
        String origin = request.getHeader(ORIGIN);
        response.setHeader("Access-Control-Allow-Origin", origin);
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "*");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "authorization,content-type");
    
        if (request.getMethod().equals("OPTIONS"))
            response.setStatus(HttpServletResponse.SC_OK);
        else 
            filterChain.doFilter(request, response);
    
    }
}
