/**
 * MediaProxyServlet.java
 * 
 * Copyright (c) 2006 Tony
 * All rights free.
 *
 * 
 * Revision History
 *
 * Date				Programmer			Notes
 * -------------	-----------------	---------------------------
 * Feb 03, 2007		Developer Name		initial
*/

package pb.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

public class MediaProxyServlet extends HttpServlet
{

    /**
     * Constructor of the object.
     */
    public MediaProxyServlet()
    {
        super();
    }

    /**
     * Destruction of the servlet. <br>
     */
    public void destroy()
    {
        super.destroy(); // Just puts "destroy" string in log
        // Put your code here
    }

    /**
     * The doGet method of the servlet. <br>
     *
     * This method is called when a form has its tag value method equals to get.
     * 
     * @param request the request send by the client to the server
     * @param response the response send by the server to the client
     * @throws ServletException if an error occurred
     * @throws IOException if an error occurred
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
            final String uriPrefix = request.getContextPath() + "/MediaProxy";
            final String uri = request.getRequestURI();
            if (!uri.startsWith(uriPrefix)) {
                return;
            }
            final String realUrl = "http:/" + uri.substring(uriPrefix.length());

            final HttpClient httpClient = new HttpClient();
            GetMethod method = new GetMethod(realUrl);

            int status = httpClient.executeMethod(method);
            if (status != HttpStatus.SC_OK) {
                // do some log
                //return;
            }

            InputStream is = method.getResponseBodyAsStream();
            Header header = method.getResponseHeader("Content-Encoding");
            if (header != null && "gzip".equalsIgnoreCase(header.getValue())) {
                is = new GZIPInputStream(is);
            }
            
            OutputStream out = response.getOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = is.read(buffer)) >= 0) {
                out.write(buffer, 0, len);
            }
        
    }

    /**
     * The doPost method of the servlet. <br>
     *
     * This method is called when a form has its tag value method equals to post.
     * 
     * @param request the request send by the client to the server
     * @param response the response send by the server to the client
     * @throws ServletException if an error occurred
     * @throws IOException if an error occurred
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Initialization of the servlet. <br>
     *
     * @throws ServletException if an error occure
     */
    public void init() throws ServletException
    {
        // Put your code here
    }

}
