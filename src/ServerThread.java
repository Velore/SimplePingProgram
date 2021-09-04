import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 处理数据报的处理线程
 * 无需主动编译，编译PingServer时会自动编译该类
 * @author chenzhuohong
 */
public class ServerThread extends Thread{

    private final DatagramSocket socket;
    private final DatagramPacket packet;

    public ServerThread(DatagramSocket socket, DatagramPacket packet){
        this.socket = socket;
        this.packet = packet;
    }

    @Override
    public void run(){
        System.out.println("\t接收数据包\n" + new String(packet.getData()).trim());
        //新建回送数据报
        DatagramPacket sendPacket = new DatagramPacket(
                packet.getData(),
                packet.getData().length,
                packet.getAddress(),
                packet.getPort());
        try{
            // 延迟随机时间(0-1399)后,回送数据报,
            // [默认]延迟大于1000时,视为丢包,即可接受延迟的范围为0-1000
            // 可接受延迟的范围可在客户端调整
            sleep(ThreadLocalRandom.current().nextLong(1400));
            socket.send(sendPacket);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
