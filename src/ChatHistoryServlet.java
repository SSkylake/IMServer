import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "ChatHistoryServlet",urlPatterns = "/get_chat_history")
public class ChatHistoryServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        doGet(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String host = request.getParameter("host");
        String theOther = request.getParameter("the_other");

        Connection connection = (Connection) getServletContext().getAttribute(Initializer.SQL);
        List<chatEntity> list = new ArrayList<>();


        try (Statement ps = connection.createStatement()) {

            String sql = String.format("select * from %s where name='%s' order by chat_time desc","chat_history_"+host,theOther);
            ResultSet resultSet = ps.executeQuery(sql);
            while (resultSet.next()){

                list.add(new chatEntity(host,resultSet.getString(1),resultSet.getString(2),resultSet.getLong(3),
                        resultSet.getInt(4) == 1));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        response.getWriter().write(new Gson().toJson(list));
    }

    class chatEntity{
        String host;
        String theOther;
        String text;
        long time;
        boolean sendOrReceive;

        chatEntity(String host,String theOther, String text, long time, boolean sendOrReceive) {
            this.host = host;
            this.theOther = theOther;
            this.text = text;
            this.time = time;
            this.sendOrReceive = sendOrReceive;
        }
    }
}
