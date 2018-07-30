package socket;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;

/**
 * Created by yj on 16. 9. 13.
 */
public class ReliableSock {
	private static ReliableSock instance = null;
    private String TAG = "ReliableSock";
    private DatagramSocket datagramSocket = null;
    private boolean isTimeout = false;
    private int segSize = 0;
    private int sleepTime = 0;
    private int serverPort;
    private int clientPort;
    private InetAddress ip;    
    private int timeout;

    public ReliableSock() {
    	
    }

    /*
    public ReliableSock(int port, InetAddress ip, int segSize, int sleepTime, int timeout) throws SocketException {
    	datagramSocket = new DatagramSocket(null);
    	datagramSocket.setReuseAddress(true);
    	setTimeout(timeout);
    	
        this.port = port;
        this.ip = ip;
        this.segSize = segSize;
        this.sleepTime = sleepTime;
        this.timeout = timeout;
    }
	*/
    public void initialize(int serverPort, int clientPort, InetAddress ip, int segSize, int sleepTime, int timeout) throws SocketException {
    	datagramSocket = new DatagramSocket(clientPort);
    	datagramSocket.setReuseAddress(true);
    	setTimeout(timeout);
    	
        this.serverPort = serverPort;
        this.clientPort = clientPort;
        this.ip = ip;
        this.segSize = segSize;
        this.sleepTime = sleepTime;
        this.timeout = timeout;
    }
    
    public void close() {
    	datagramSocket.close();
    }

    public void setTimeout(int timeout) throws SocketException {
        datagramSocket.setSoTimeout(timeout);
        isTimeout = true;
    }

    //if
    public void clear() {
        if(isTimeout == true) {
            try {
                byte[] seg = new byte[segSize];
                while (true) {
                    DatagramPacket pkt = new DatagramPacket(seg, seg.length);
                    datagramSocket.receive(pkt);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean send(byte[] buf) {
        int i = 0;
        while(true) {
            try {
                for (; i < buf.length / segSize; i++) {
                    DatagramPacket pkt = new DatagramPacket(buf, i * segSize, segSize, ip, serverPort);
                    datagramSocket.send(pkt);
                    //Log.d(TAG, "send!!");
                    Thread.sleep(sleepTime);
                }
                DatagramPacket pkt = new DatagramPacket(buf, i * segSize, buf.length - i * segSize, ip, serverPort);
                datagramSocket.send(pkt);
                Thread.sleep(sleepTime);
                i++;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                byte[] response = new byte[5];
                DatagramPacket pkt = new DatagramPacket(response, response.length);

                try {
                	datagramSocket.receive(pkt);
                    if(!pkt.getAddress().equals(ip))
                        return false;
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }

                //Log.d(TAG, "[0]: "+response[0]+"[1]: "+response[1]+"[2]: "+response[2]+"[3]: "+response[3]+"[4]: "+response[4]);
                if(response[0] == 0x34 && response[1] == 0x56 && response[2] == 0x78) {
                    if(response[3] == 0x00)
                        break;
                    else if(response[3] == 0x01)
                        i = response[4];
                }
            }
        }
        return true;
    }

    public byte[] recv(int size) {
        byte[] recvBuf = new byte[size];

        int i = 0;
        int count = 0;
        while(true) {
            try {
                for (; i < size / segSize; i++) {
                    DatagramPacket pkt = new DatagramPacket(recvBuf, i * segSize, segSize);
                    datagramSocket.receive(pkt);
                    if(!pkt.getAddress().equals(ip))
                        return new byte[0];
                    //Log.d(TAG, "recved!! "+i);
                }
                DatagramPacket pkt = new DatagramPacket(recvBuf, i * segSize, size - i * segSize);
                datagramSocket.receive(pkt);
                if(!pkt.getAddress().equals(ip))
                    return new byte[0];
                i++;
            } catch (IOException e) {
                e.printStackTrace();
                count++;
            }

            if(i > 0) {
                if (i == 1 + size / segSize) {
                    byte[] response = new byte[]{0x34, 0x56, 0x78, 0x00, 0x00};
                    DatagramPacket pkt = new DatagramPacket(response, response.length, ip, serverPort);

                    try {
                    	datagramSocket.send(pkt);
                        System.out.println("send1!!");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                } else {
                    byte[] response = new byte[]{0x34, 0x56, 0x78, 0x01, 0x00};
                    DatagramPacket pkt = new DatagramPacket(response, response.length, ip, serverPort);
                    response[4] = (byte) i;

                    try {
                    	datagramSocket.send(pkt);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (count >= 10)
                    return new byte[0];
            }
            else
                return new byte[0];
        }
        return recvBuf;
    }
}