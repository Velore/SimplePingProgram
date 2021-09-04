import java.io.IOException;
import java.net.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Ping客户端
 * src文件夹内已保存编译文件，但是建议运行前再编译一次
 * 在src目录下
 * 编译:javac -encoding utf8 -target 1.8 PingClient.java
 * 运行2:java PingClient 127.0.0.1 6666 12 1000
 * @author chenzhuohong
 */
public class PingClient {
    /**
     * 客户端Socket
     */
    private DatagramSocket socket;
    /**
     * 发送分组的数量
     */
    private int sendNum = 10;
    /**
     * 服务器地址
     */
    private InetAddress serAddress;
    /**
     * 服务器端口
     */
    private int serPort;
    /**
     * 客户端最长等待时间
     * 在等待时间内没有接收到数据报代表丢失，直接发送下一个数据报
     */
    private int maxWaitTime = 1000;
    /**
     * 存放RTT的列表，长度不定
     */
    private final ArrayList<Long> rttList;
    /**
     * Ping客户端
     * @param serAdd 服务器地址
     * @param serPort 服务器端口
     */
    public PingClient(String serAdd, int serPort){
        this.rttList = new ArrayList<>();
        try{
            this.serAddress = InetAddress.getByName(serAdd);
            this.serPort = serPort;
            //本地任一可用端口作为客户端socket
            this.socket = new DatagramSocket();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Ping客户端
     * @param serAdd Ping服务器地址
     * @param serPort Ping服务器端口
     * @param sendNum 发送分组的数量
     */
    public PingClient(String serAdd, int serPort, int sendNum){
        this.sendNum = sendNum;
        this.rttList = new ArrayList<>();
        try{
            this.serAddress = InetAddress.getByName(serAdd);
            this.serPort = serPort;
            this.socket = new DatagramSocket();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Ping客户端
     * @param serAdd Ping服务器地址
     * @param serPort Ping服务器端口
     * @param sendNum 发送分组的数量
     * @param maxWaitTime 最大可接收延迟时间
     */
    public PingClient(String serAdd, int serPort, int sendNum, int maxWaitTime){
        this.sendNum = sendNum;
        this.maxWaitTime = maxWaitTime;
        this.rttList = new ArrayList<>();
        try{
            this.serAddress = InetAddress.getByName(serAdd);
            this.serPort = serPort;
            this.socket = new DatagramSocket();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 新建一个[发送分组]packet
     * @param sendNo 分组编号
     * @param sendTime 分组发送时间
     * @param address 目的地址
     * @param port 目的端口
     * @return 新的分组
     */
    public static DatagramPacket newSendPacket(int sendNo, LocalDateTime sendTime, InetAddress address, int port){
        //匹配时间戳
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss.SS");
        //发送分组附带的信息
        String data = "head\tPingUDP\tSeq:" + sendNo + "\t" + formatter.format(sendTime);
        //返回[发送分组]packet
        return new DatagramPacket(data.getBytes(), data.getBytes().length, address, port);
    }

    /**
     * 客户端主运行方法
     */
    public void run(){
        System.out.println("ping ：" + serAddress);
        for(int i = 0 ; i < sendNum ; i++){
            //发送时间sendTime
            LocalDateTime sendTime = LocalDateTime.now();
            //新建发送分组packet
            DatagramPacket sendPacket = PingClient.newSendPacket(i, sendTime, serAddress, serPort);
            //新建接收分组packet
            DatagramPacket receivePacket = new DatagramPacket(
                    new byte[sendPacket.getData().length],
                    sendPacket.getData().length);
            try{
                System.out.println("准备发送数据报" + i);
                socket.send(sendPacket);
                System.out.println("发送完毕，准备接收数据报" + i);
                //设置最大等待时间,若数据报没有在等待时间内接收,说明已丢失,直接发送下一个,防止阻塞
                socket.setSoTimeout(maxWaitTime);
                socket.receive(receivePacket);
            }catch (SocketException e){
                System.out.println("socket time out");
            }catch (IOException ignored){
            }
            String data = new String(receivePacket.getData());
            Duration receiveTime = Duration.between(sendTime, LocalDateTime.now());
            // 计算RTT并放入列表，等待全部分组发送完成后计算
            this.rttList.add(receiveTime.toMillis());
            // 如果大于1000ms，则认为请求丢失或者对请求的回复丢失
            if(receiveTime.toMillis()>maxWaitTime){
                data = "Packet " + i + " timed out!";
            }else {
                data = data + "\n" + "[RTT:" + rttList.get(i) +"ms]";
            }
            System.out.println(data);
        }
    }

    public String countTime(){
        //最大RTT
        long maxRTT = this.rttList.get(0);
        //最小RTT
        long minRTT = this.rttList.get(0);
        //接收的分组的RTT总和
        long sumRTT = 0;
        //
        int receiveNum = sendNum;
        for (Long rtt : this.rttList) {
            //RTT大于1000说明丢包，不计入接收
            if (rtt > 1000) {
                receiveNum--;
                continue;
            }
            //计算最小往返时间
            if (minRTT > rtt) { minRTT = rtt; }
            //计算最大往返时间
            if (maxRTT < rtt) { maxRTT = rtt; }
            //计算总往返时间，计算平均往返时间时使用
            sumRTT += rtt;
        }
        //以字符串形式,返回计算结果的总结
        return "Ping " + serAddress + ":" + serPort + "\n"
                + "Sent:" + sendNum
                + ", Received:" + receiveNum
                + ", Lost:" + (sendNum - receiveNum) + "\n"
                + "minRTT:" + minRTT
                + "ms, maxRTT:" + maxRTT
                + "ms, averRTT:" + (int)(sumRTT*1.0/receiveNum)
                + "ms";
    }

    public static void main(String[] args) {
        PingClient client;
        // 测试代码
        // PingClient client = new PingClient("127.0.0.1", Integer.parseInt("6666"));
        // 根据输入参数的不同，使用不同的构造方法，即自定义参数
        switch (args.length){
            case 3:
                client = new PingClient(args[0]
                        , Integer.parseInt(args[1])
                        , Integer.parseInt(args[2]));
                break;
            case 4:
                client = new PingClient(args[0]
                        , Integer.parseInt(args[1])
                        , Integer.parseInt(args[2])
                        , Integer.parseInt(args[3]));
                break;
            default:
                client = new PingClient(args[0], Integer.parseInt(args[1]));
        }
        client.run();
        System.out.println(client.countTime());
    }
}
