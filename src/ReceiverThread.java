import javax.servlet.ServletContext;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;

public class ReceiverThread implements Runnable {

    private BlockingQueue<TransportPayload> queue;
    private DatagramSocket socket;
    private static final int LENGTH = 1024;

    public ReceiverThread(ServletContext sc) {
        queue = (BlockingQueue<TransportPayload>) sc.getAttribute(Initializer.BUFFER_LIST);
        socket = (DatagramSocket) sc.getAttribute(Initializer.SOCKET);
    }

    @Override
    public void run() {

        DatagramPacket packet = new DatagramPacket(new byte[LENGTH],LENGTH);
        while (true){
            try {
                socket.receive(packet);
                System.out.println(new String(packet.getData(),0,packet.getLength()));
                TransportPayload payload = TransportPayload.getInstance(packet.getData(),packet.getLength());
                payload.setPort(packet.getPort());
                payload.setFromAddress(packet.getAddress().getHostAddress());
                queue.put(payload);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
