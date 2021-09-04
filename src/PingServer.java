import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * 服务器端
 * src文件夹内已保存编译文件，但是建议运行前再编译一次
 * 在src目录下
 * 编译:javac -encoding utf8 -target 1.8 PingServer.java
 * 运行2:java PingServer 6666
 * @author chenzhuohong
 */
public class PingServer {

    /**
     * 服务器端的端口监听套接字
     */
    private DatagramSocket socket;

    public PingServer(int port){
        try{
            this.socket = new DatagramSocket(port);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 服务器运行
     * 暂未设置结束方法，只能强行结束
     */
    public void run(){
        //数据报长度
        int byteLength = 1024;
        DatagramPacket packet = new DatagramPacket(new byte[byteLength], byteLength);
        System.out.println("Ping服务器已启动");
        while(true){
            try{
                //接收数据报
                socket.receive(packet);
            }catch (Exception e){
                e.printStackTrace();
            }
            //开启处理线程处理数据报
            ServerThread st = new ServerThread(socket, packet);
            st.start();
        }
    }

    public static void main(String[] args) {
        PingServer server = new PingServer(Integer.parseInt(args[0]));
//        测试代码
//        PingServer server = new PingServer(Integer.parseInt("6666"));
        server.run();
    }
}
