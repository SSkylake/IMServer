import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;

@WebServlet(name = "ResetCountServlet" ,urlPatterns = "/reset_non_reading_count")
public class ResetCountServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request,response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String one = request.getParameter("one");
        String theOther = request.getParameter("the_other");

        Connection connection = (Connection) getServletContext().getAttribute(Initializer.SQL);
        HashMap<String,String> re = new HashMap<>();


        try{

            String sql = "update friends set one_nrc=0 where one=? and the_other=?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1,one);
            statement.setString(2,theOther);
            statement.execute();
            statement.setString(1,theOther);
            statement.setString(2,one);
            re.put("code","0");
            re.put("message","Reset successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            re.put("code","1");
            re.put("message","Reset failed due to database error.");
        }

        response.getWriter().write(new Gson().toJson(re));


    }
}
