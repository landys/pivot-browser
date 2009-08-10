package pb.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class RoFilter implements Filter
{
    public static ThreadLocal<String> sessionIds;
    static
    {
        sessionIds = new ThreadLocal<String>();
    }

    public void destroy()
    {

    }

    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException
    {
        sessionIds.set(((HttpServletRequest) request).getSession().getId());
        chain.doFilter(request, response);
    }

    public void init(FilterConfig config) throws ServletException
    {

    }

}
