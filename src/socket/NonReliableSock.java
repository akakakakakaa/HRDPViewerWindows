package socket;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * Created by yj on 16. 9. 13.
 */
public class NonReliableSock {
    private String TAG = "ReliableSock";
    private DatagramSocket datagramSocket = null;
    private boolean isTimeout = false;
    private int segSize = 0;
    private int sleepTime = 0;
    private int serverPort;
    private int clientPort;
    private InetAddress ip;
    private int timeout;

    public NonReliableSock() {
    	
    }
    
    /*
    public NonReliableSock(int port, InetAddress ip, int segSize, int sleepTime, int timeout) throws SocketException {
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

    public void setTimeout(int timeout) throws SocketException {
        datagramSocket.setSoTimeout(timeout);
        isTimeout = true;
    }
    
    public void close() {
    	datagramSocket.close();
    }
    
    //if
    public void clear() throws SocketTimeoutException, SocketException {
        if(isTimeout == true) {
            setTimeout(1);
            try {
                byte[] seg = new byte[segSize];
                while (true) {
                    long start = System.currentTimeMillis();
                    DatagramPacket pkt = new DatagramPacket(seg, seg.length);
                    datagramSocket.receive(pkt);
                    System.out.println(System.currentTimeMillis() - start);
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
            setTimeout(timeout);
        }
    }

    public boolean send(byte[] buf) {
        int i;
        try {
            for(i=0; i<buf.length /segSize; i++) {
                DatagramPacket pkt = new DatagramPacket(buf, i * segSize, segSize, ip, serverPort);
                datagramSocket.send(pkt);
                Thread.sleep(sleepTime);
            }
            if(buf.length - i*segSize != 0) {
                DatagramPacket pkt = new DatagramPacket(buf, i * segSize, buf.length - i * segSize, ip, serverPort);
                datagramSocket.send(pkt);
                Thread.sleep((int) (sleepTime * (buf.length - i * segSize) / (float) segSize));
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public byte[] recv(int size) {
        byte[] recvBuf = new byte[size];

        int i;
        int realSize = 0;
        try {
            for(i=0; i<size/segSize; i++) {
            	DatagramPacket pkt = new DatagramPacket(recvBuf, i*segSize, segSize);
                datagramSocket.receive(pkt);
                realSize += pkt.getLength();
                if(!pkt.getAddress().equals(ip))
                    return new byte[0];
            }

            if(size - i*segSize != 0) {
                DatagramPacket pkt = new DatagramPacket(recvBuf, i * segSize, size - i * segSize);
                datagramSocket.receive(pkt);
                realSize += pkt.getLength();
                if (!pkt.getAddress().equals(ip))
                    return new byte[0];
            }

            if(size != realSize) {
                byte[] realBuf = new byte[realSize];
                System.arraycopy(recvBuf, 0, realBuf, 0, realSize);
                //Log.d(TAG, "realSize = " + realSize + " size = " + size);
                return realBuf;
            }
            else
                return recvBuf;
        } catch(IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }
}