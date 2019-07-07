import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.DatagramSocket;
import java.sql.*;
import java.util.HashMap;

@WebServlet(name = "RegisterOrLogServlet",urlPatterns = "/register_or_log")
public class RegisterOrLogServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        Connection connection = (Connection) getServletContext().getAttribute(Initializer.SQL);

        HashMap<String,String> result = new HashMap<>();

        try {

            if(connection.isClosed()){
                Initializer.initSQLOut(getServletContext());
                connection = (Connection) getServletContext().getAttribute(Initializer.SQL);

            }

            Statement statement = connection.createStatement();
            String sql = "select count(*) from user where username=?";
            String sql2 = "select count(*) from user where username=? and password=?";

            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1,username);

            ResultSet set1 = ps.executeQuery();
            set1.first();
            if (set1.getInt(1) == 0){
                //注册

                statement.execute("create table chat_history_"+username +" (name varchar(20),content text,chat_time long," +
                        "receive_or_send tinyint)");

                String sql3 = "insert into user values(?,?,1)";
                PreparedStatement ps2 = connection.prepareStatement(sql3);
                ps2.setString(1,username);
                ps2.setString(2,password);
                ps2.execute();
                DatagramSocket socket = (DatagramSocket) getServletContext().getAttribute(Initializer.SOCKET);
                result.put("code","0");
                result.put("message","Register successfully");
                result.put("port",String.valueOf(socket.getLocalPort()));

            }else{

                PreparedStatement ps3 = connection.prepareStatement(sql2);
                ps3.setString(1,username);
                ps3.setString(2,password);
                ResultSet set2 = ps3.executeQuery();
                set2.first();
                if(set2.getInt(1) == 0){
                    //用户名已经被占用或密码错误
                    result.put("code","1");
                    result.put("message","wrong password or illegal username");
                }else{
                    //成功登录
                    result.put("code","0");
                    result.put("message","Log in successfully");
                    DatagramSocket socket = (DatagramSocket) getServletContext().getAttribute(Initializer.SOCKET);
                    result.put("port",String.valueOf(socket.getLocalPort()));
                }

            }

        } catch (SQLException e) {
            result.put("code","1");
            result.put("message","Unable to connect to the database");

            e.printStackTrace();



        }

        response.getWriter().write(new Gson().toJson(result));


    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
