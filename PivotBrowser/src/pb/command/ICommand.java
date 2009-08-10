/**
 * 
 */
package pb.command;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Tony
 *
 */
public interface ICommand
{
	void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
}
