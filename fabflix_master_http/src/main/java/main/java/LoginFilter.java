package main.java;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Servlet Filter implementation class LoginFilter
 */
@WebFilter(filterName = "LoginFilter", urlPatterns = "/*")
public class LoginFilter implements Filter {
    private final ArrayList<String> allowedURIs = new ArrayList<>();
    private final ArrayList<String> adminURIs = new ArrayList<>();

    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        System.out.println("LoginFilter: " + httpRequest.getRequestURI());

        // Check if this URL is allowed to access without logging in
        if (this.isUrlAllowedWithoutLogin(httpRequest.getRequestURI())) {
            // Keep default action: pass along the filter chain
            chain.doFilter(request, response);
            return;
        }

        if(this.isURLForAdmin(httpRequest.getRequestURI())){
            if (httpRequest.getSession().getAttribute("user") == null) {
                httpResponse.sendRedirect("dashboard-login.html");
            } else {
                User u = (User)httpRequest.getSession().getAttribute("user");
                if(u.getUserType().equals("customers")){
                    httpResponse.setStatus(401);
                    return;
                }
                chain.doFilter(request, response);
                return;
            }
        }

        // Redirect to login page if the "user" attribute doesn't exist in session
        if (httpRequest.getSession().getAttribute("user") == null) {
            httpResponse.sendRedirect("login.html");
        } else {
            User u = (User)httpRequest.getSession().getAttribute("user");
            if(u.getUserType().equals("admin")){
                httpResponse.sendRedirect("login.html");
            }
            chain.doFilter(request, response);
            return;
        }
    }

    private boolean isURLForAdmin(String requestURI){
        return adminURIs.stream().anyMatch(requestURI.toLowerCase()::endsWith);
    }

    private boolean isUrlAllowedWithoutLogin(String requestURI) {
        /*
         Setup your own rules here to allow accessing some resources without logging in
         Always allow your own login related requests(html, js, servlet, etc..)
         You might also want to allow some CSS files, etc..
         */
        return allowedURIs.stream().anyMatch(requestURI.toLowerCase()::endsWith);
    }

    public void init(FilterConfig fConfig) {
        allowedURIs.add("/");
        allowedURIs.add("main.html");
        allowedURIs.add("main.js");
        allowedURIs.add("main.css");
        allowedURIs.add("mainbackground.jpeg");
        allowedURIs.add("_dashboard");
        allowedURIs.add("dashboard-login.html");
        allowedURIs.add("dashboard-login.js");
        allowedURIs.add("login.html");
        allowedURIs.add("login.js");
        allowedURIs.add("api/login");
        adminURIs.add("_dashboard.html");
        adminURIs.add("_dashboard.js");
        adminURIs.add("dashboard.css");
        adminURIs.add("_dashboard/action");
        adminURIs.add("_dashboard/metadata");
    }

    public void destroy() {
        // ignored.
    }

}
