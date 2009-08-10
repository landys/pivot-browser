package pb.servlet;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import pb.command.ICommand;
import utils.PbUtil;

/**
 * Servlet implementation class for Servlet: OnlineDocServlet
 * 
 */
public class PbServlet extends javax.servlet.http.HttpServlet implements
        javax.servlet.Servlet
{
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 8601612699991989280L;
    
    private static final Logger logger = Logger.getLogger(PbServlet.class);
    
    /**
     * initial properties of the servlet, from web.xml
     */
    Properties initProps = new Properties();
    
    /*
     * (non-Java-doc)
     * 
     * @see javax.servlet.http.HttpServlet#HttpServlet()
     */
    public PbServlet()
    {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.Servlet#destroy()
     */
    public void destroy()
    {
        // TODO Auto-generated method stub
        super.destroy();
    }

    /*
     * (non-Java-doc)
     * 
     * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request,
     *      HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException
    {
        request.setCharacterEncoding("UTF-8");
        
        final String uri = request.getRequestURI();
        String action = PbUtil.getUriName(uri);
        if (action == null)
        {
            action = "survey";
        }
        
        logger.info("Action is " + action + ".");
        
        // Execute the command
        String strCommand = initProps.getProperty(action + "Command");
        if (strCommand != null)
        {
            // Attribute executeResult is used to store the result of execute
            request.removeAttribute("executeResult");
            try
            {
                Class claCommand = Class.forName(strCommand);
                ICommand command = (ICommand) claCommand.newInstance();
                command.execute(request, response);
            }
            catch (ClassNotFoundException e)
            {
                logger.error(e.getMessage());
            }
            catch (InstantiationException e)
            {
                logger.error(e.getMessage());
            }
            catch (IllegalAccessException e)
            {
                logger.error(e.getMessage());
            }
        }
        
        String result = "";
        if (request.getAttribute("executeResult") != null)
        {
            result = (String)request.getAttribute("executeResult");
        }
        String strPage = initProps.getProperty(action + "Page");
        if (strPage != null)
        {
            final String[] ss = strPage.split("\\:");
            if ("redirect".equals(ss[0]))
            {
                response.sendRedirect(ss[1] + "?re=" + result);
            }
            else
            {
                RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(strPage);
                dispatcher.forward(request, response);
            }
        }
        
        response.setCharacterEncoding("UTF-8");
    }

    /*
     * (non-Java-doc)
     * 
     * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request,
     *      HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException
    {
        doGet(request, response);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.GenericServlet#init()
     */
    public void init() throws ServletException
    {
        super.init();
        for (Enumeration en = getInitParameterNames(); en.hasMoreElements();)
        {
            String name = (String)en.nextElement();
            initProps.put(name, getInitParameter(name));
        }
    }
}