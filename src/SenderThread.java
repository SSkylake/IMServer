import javax.servlet.ServletContext;
import java.io.IOException;
import java.net.*;
import java.sql.*;
import java.util.concurrent.BlockingQueue;

public class SenderThread implements Runnable {

    private BlockingQueue<TransportPayload> queue;
    private DatagramSocket socket;
    private Connection connection;
    private static final int LENGTH = 1024;


    public SenderThread(ServletContext sc) {
        connection = (Connection) sc.getAttribute(Initializer.SQL);
        queue = (BlockingQueue<TransportPayload>) sc.getAttribute(Initializer.BUFFER_LIST);
        socket = (DatagramSocket) sc.getAttribute(Initializer.SOCKET);


    }

    @Override
    public void run() {

        while (true) {
            try {
                TransportPayload payload = queue.take();

                switch (payload.getCommand()) {
                    case TransportPayload.Type.HEART_PACKET://更新路由表

                        String sql = "replace into route values(?,?,?)";
                        try {
                            PreparedStatement ps = connection.prepareStatement(sql);
                            ps.setString(1,payload.getFrom());
                            ps.setString(2,payload.getFromAddress());
                            ps.setInt(3,payload.getPort());
                            ps.execute();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                        break;
                    case TransportPayload.Type.IM_TO_SERVER:
                        try {
                            //1、查询目的机路由表
                            Statement statement = connection.createStatement();
                            ResultSet resultSet = statement.executeQuery("select * from route where username='"+payload.getTo()+"'");
                            resultSet.first();
                            String address = resultSet.getString("ip_address");
                            int port = resultSet.getInt("port");

                            //2、向目的机转发
                            payload.setCommand(TransportPayload.Type.IM_SERVER_FORWARD);//转换为客户端指令
                            byte[] buffer = payload.getBytes();
                            DatagramPacket packet = new DatagramPacket(buffer,0,buffer.length, InetAddress.getByName(address),port);
                            socket.send(packet);

                            //3、增加聊天记录历史记录
                            String sql1 = String.format("insert into %s values(?,?,?,?)","chat_history_"+payload.getFrom());
                            PreparedStatement ps = connection.prepareStatement(sql1);
                            ps.setString(1,payload.getTo());
                            ps.setString(2,payload.getContent());
                            ps.setLong(3,payload.getTime());
                            ps.setInt(4,1);//1表示发送
                            ps.execute();
                            ps.close();


                            String sql2 = String.format("insert into %s values(?,?,?,?)","chat_history_"+payload.getTo());

                            PreparedStatement ps2 = connection.prepareStatement(sql2);

                            ps2.setString(1,payload.getFrom());
                            ps2.setString(2,payload.getContent());
                            ps2.setLong(3,payload.getTime());
                            ps2.setInt(4,0);//0表示接收
                            ps2.execute();
                            ps2.close();

                            //4、发回一个确认包
                            TransportPayload confirmPayload = new TransportPayload(TransportPayload.Type.IM_SERVER_CONFIRM,
                                    payload.getFrom(),
                                    payload.getTo(),
                                    null);
                            confirmPayload.setTime(payload.getTime());
                            byte[] confirmBytes = confirmPayload.getBytes();
                            DatagramPacket confirmPacket = new DatagramPacket(
                                    confirmBytes,confirmBytes.length,
                                    InetAddress.getByName(payload.getFromAddress()),
                                    payload.getPort());
                            socket.send(confirmPacket);

                            //5、更新关系表
                            String sql3 = "select count(*) from friends where one=? and the_other=?";
                            PreparedStatement ps3 = connection.prepareStatement(sql3);
                            ps3.setString(1,payload.getFrom());
                            ps3.setString(2,payload.getTo());
                            ResultSet resultSet1 = ps3.executeQuery();
                            resultSet1.first();

                            if(resultSet1.getInt(1) ==0){
                                String sql4 = "update friends set recent_time=?,recent_chat=?,one_nrc=one_nrc+1 where one=? and the_other=?";
                                PreparedStatement preparedStatement = connection.prepareStatement(sql4);
                                preparedStatement.setLong(1,payload.getTime());
                                preparedStatement.setString(2,payload.getContent());
                                preparedStatement.setString(3,payload.getTo());
                                preparedStatement.setString(4,payload.getFrom());
                                preparedStatement.execute();

                            }else{
                                String sql4 = "update friends set recent_time=?,recent_chat=?,the_other_nrc=the_other_nrc+1 where one=? and the_other=?";
                                PreparedStatement preparedStatement = connection.prepareStatement(sql4);
                                preparedStatement.setLong(1,payload.getTime());
                                preparedStatement.setString(2,payload.getContent());
                                preparedStatement.setString(3,payload.getFrom());
                                preparedStatement.setString(4,payload.getTo());
                                preparedStatement.execute();
                            }



                        } catch (SQLException | IOException e) {
                            e.printStackTrace();
                        }


                        break;
                    default:

                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }
}
