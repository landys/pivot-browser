package pb.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * @author wjd
 */
public class DbHelper
{
    private String driveName = "com.mysql.jdbc.Driver";

    private String url = "jdbc:mysql://localhost:3306/pb";

    private String username = "root";

    private String password = "654321";

    private static final DbHelper singleton = new DbHelper();

    private static final Logger logger = Logger.getLogger(DbHelper.class);

    private DbHelper()
    {
        try
        {
            Class.forName(driveName);
        }
        catch (ClassNotFoundException e)
        {
            logger.error(e.getMessage());
        }
    }

    public static DbHelper instance()
    {
        return singleton;
    }

    private Connection getConnection()
    {
        Connection con = null;
        try
        {
            con = DriverManager.getConnection(url, username, password);
            con.setAutoCommit(true);
        }
        catch (SQLException e)
        {
            logger.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }

        return con;
    }

    public void updateSql(final String sql, final List<Object> paras)
    {
        final Connection con = getConnection();
        PreparedStatement state = null;
        if (con == null)
        {
            logger.error("Cannot get connection");
            throw new RuntimeException("Cannot get connection.");
        }

        try
        {
            state = con.prepareStatement(sql);
            prepareStatement(state, sql, paras);
            state.executeUpdate();
        }
        catch (SQLException e)
        {
            logger.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
        finally
        {
            close(con, state);
        }
    }

    private void prepareStatement(final PreparedStatement state,
            final String sql, final List<Object> paras)
    {
        try
        {
            if (paras != null)
            {
                for (int i = 0; i < paras.size(); i++)
                {
                    Object para = paras.get(i);
                    int index = i + 1;
                    if (para instanceof String)
                    {
                        state.setString(index, (String) para);
                    }
                    else if (para instanceof Integer)
                    {
                        state.setInt(index, ((Integer) para).intValue());
                    }
                    else if (para instanceof Double)
                    {
                        state.setDouble(index, ((Double) para).doubleValue());
                    }
                    else if (para instanceof Float)
                    {
                        state.setFloat(index, ((Float) para).floatValue());
                    }
                    else
                    {
                        state.setObject(index, para);
                    }
                }
            }
        }
        catch (SQLException e)
        {
            logger.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    private void close(final Connection conn, final PreparedStatement state)
    {
        if (state != null)
        {
            try
            {
                state.close();
            }
            catch (SQLException e)
            {
                logger.error(e.getMessage());
                throw new RuntimeException(e.getMessage());
            }
        }

        if (conn != null)
        {
            try
            {
                conn.close();
            }
            catch (SQLException e)
            {
                logger.error(e.getMessage());
                throw new RuntimeException(e.getMessage());
            }
        }

    }

}
