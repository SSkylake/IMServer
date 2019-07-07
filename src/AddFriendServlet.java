import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;

@WebServlet(name = "AddFriendServlet",urlPatterns = "/add_friend")
public class AddFriendServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        doGet(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String host = request.getParameter("host");
        String friend = request.getParameter("friend");


        Connection connection = (Connection) getServletContext().getAttribute(Initializer.SQL);

        HashMap<String,String> re = new HashMap<>();


        //TODO: CHECK IF THIS FRIEND EXIST

        String sql3 = String.format("select count(*) from user where username='%s' ",friend);
        try (Statement statement1 = connection.createStatement()) {
            ResultSet resultSet1 = statement1.executeQuery(sql3);
            resultSet1.first();
            if(resultSet1.getInt(1)==0){
                re.put("code","1");
                re.put("message",friend+" not exists");
            }else {

                String sql = "select count(*) from friends where (one=? and the_other=?) or (the_other=? and one=?)";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setString(1, host);
                    ps.setString(2,friend);
                    ps.setString(3,host);
                    ps.setString(4,friend);
                    ResultSet resultSet = ps.executeQuery();
                    resultSet.first();
                    if(resultSet.getInt(1)==0){
                        //添加好友
                        String sql2 = "insert into friends values(?,?,?,?,0,0)";
                        PreparedStatement ps2 = connection.prepareStatement(sql2);
                        ps2.setString(1,host);
                        ps2.setString(2,friend);
                        ps2.setString(3,"No chat");
                        ps2.setLong(4,System.currentTimeMillis());
                        ps2.execute();

                        re.put("code","0");
                        re.put("message","Add friend successfully.");


                    }else{
                        //已经是好友
                        re.put("code","1");
                        re.put("message","Already been friend with "+friend);
                    }
                } catch (SQLException e) {

                    e.printStackTrace();

                }


            }
        } catch (SQLException e) {
            e.printStackTrace();
        }




        response.getWriter().write(new Gson().toJson(re));

    }
}
