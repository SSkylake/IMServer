import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

@WebServlet(name = "FriendListServlet" ,urlPatterns = "/get_friend_list")
public class FriendListServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        doGet(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


        String username = request.getParameter("username");
        List<friendEntity> list = new LinkedList<>();


        Connection connection = (Connection) getServletContext().getAttribute(Initializer.SQL);

        String sql = "select * from friends where one=? or the_other=? order by recent_time";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, username);

            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                String name = resultSet.getString(1).equals(username)?resultSet.getString(2):resultSet.getString(1);
                list.add(new friendEntity(name,username,resultSet.getString(3),resultSet.getLong(4),Math.max(resultSet.getInt(5),resultSet.getInt(6))));
            }

            response.getWriter().write(new Gson().toJson(list));
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    class friendEntity {
        private String theOther;
        private String currentName;
        private String recentChat;
        private long recentTime;
        private int nonReadingCount;


        public friendEntity(String theOther, String currentName, String recentChat, long recentTime, int nonReadingCount) {
            this.theOther = theOther;
            this.currentName = currentName;
            this.recentChat = recentChat;
            this.recentTime = recentTime;
            this.nonReadingCount = nonReadingCount;
        }
    }
}
