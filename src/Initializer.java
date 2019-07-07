import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import javax.servlet.http.HttpSessionBindingEvent;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

@WebListener()
public class Initializer implements ServletContextListener{


    private Connection connection;
    public static final String SQL = "sqlConnection";
    public static final String RECEIVER_THREAD = "RECEIVER_THREAD";
    public static final String SENDER_THREAD = "SENDER_THREAD";
    public static final String BUFFER_LIST = "BUFFER_LIST";
    public static final String SOCKET = "SOCKET";
    public static final int BUFFER_SIZE = 256;

    private static String driver = "com.mysql.cj.jdbc.Driver";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        initBufferList(sce.getServletContext());
        initSocket(sce.getServletContext());
        initSQL(sce.getServletContext());
        initThread(sce.getServletContext());

    }



    @Override
    public void contextDestroyed(ServletContextEvent sce) {

        try {
            if(connection!=null)
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void initSocket(ServletContext sc){
        try {
            DatagramSocket socket = new DatagramSocket();
            sc.setAttribute(SOCKET,socket);

            System.out.println("socket.getPort:"+socket.getPort());
            System.out.println("socket.getLocalPort:"+socket.getLocalPort());
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
    private void initSQL(ServletContext sc){


        String username = sc.getInitParameter("username");
        String password = sc.getInitParameter("password");
        String portNum = sc.getInitParameter("port");
        String url = String.format("jdbc:mysql://localhost:%s/imserver?useUnicode=true&characterEncoding=UTF-8",portNum);


        try{
            Class.forName(driver);
            connection = DriverManager.getConnection(url,username,password);
            sc.setAttribute(SQL,connection);
            System.out.println("database connected.");
            initSQLTable(connection);


        }catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public static void initSQLOut(ServletContext sc){
        String username = sc.getInitParameter("username");
        String password = sc.getInitParameter("password");
        String portNum = sc.getInitParameter("port");
        String url = String.format("jdbc:mysql://localhost:%s/imserver?useUnicode=true&characterEncoding=UTF-8",portNum);


        try{
            Class.forName(driver);
            Connection connection = DriverManager.getConnection(url,username,password);
            sc.setAttribute(SQL,connection);


        }catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void initSQLTable(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();

        statement.execute("create table `user` (username varchar(20) primary key ,password varchar(20),is_online tinyint default 0)");
        statement.execute("create table `friends` (one varchar(20),the_other varchar(20),recent_chat text," +
                "recent_time long ,one_nrc int,the_other_nrc int ,primary key(one,the_other))");
        statement.execute("create table route (username varchar(20) primary key ,ip_address varchar(15),port int)");
        statement.close();
    }

    private void initThread(ServletContext sc){

        Thread r = new Thread(new ReceiverThread(sc));
        Thread s = new Thread(new SenderThread(sc));
        sc.setAttribute(RECEIVER_THREAD,r);
        sc.setAttribute(SENDER_THREAD,s);
        r.start();
        s.start();

    }


    public void initBufferList(ServletContext sc){
        BlockingDeque<TransportPayload> list = new LinkedBlockingDeque<>(BUFFER_SIZE);
        sc.setAttribute(BUFFER_LIST,list);
    }
}
