package video;

import java.awt.EventQueue;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import config.ImageListOption;
import config.Path;
import config.SocketOption;
import config.VideoOption;
import socket.NonReliableSock;
import socket.ReliableSock;
import util.ImageUtil;
import util.Util;
import video.VideoPanel;
import codec.Codec;

public class VideoThread extends Thread {
	public static final int VIDEO_CONNECTED = 0x10000000;
	public static final int VIDEO_NOT_CONNECTED = 0x20000000;
	public static final int VIDEO_AUTO_NOT_CONNECTED = 0x30000000;
	public static final int VIDEO_INIT_SUCCESS = 0x40000000;
	public static final int VIDEO_INIT_FAIL = 0x50000000;
	public static final int VIDEO_STOPPED = 0x60000000;
	
	public interface VideoThreadMsgCallback {
		public void onMessage(int messageNum);
	};
	private VideoThreadMsgCallback callback;
	
	//
	private VideoOption videoOption;
	private SocketOption socketOption;
	private ImageListOption imageListOption;
	
	//
	private VideoPanel videoPanel;

	//
	private boolean isStop = false;
	private boolean isSet = false;
	private boolean isVideoSave = false;
	private boolean isDrawRect = false;
	
	//
	private int strangeCount = 0;
	
	//
	private NonReliableSock nonReliableSock;
	private ReliableSock reliableSock;
	
	//
	private List<Long> timestamps = new ArrayList();
	
	//
	private List<Rect> currentRectList = new ArrayList();
	private int rectClear = 0;
	
	public VideoThread(VideoOption videoOption, SocketOption socketOption, NonReliableSock nonReliableSock, ReliableSock reliableSock, ImageListOption imageListOption) {
		this.videoOption = videoOption;
		this.socketOption = socketOption;
		this.nonReliableSock = nonReliableSock;
		this.reliableSock = reliableSock;
		this.imageListOption = imageListOption;
	}

	public void setPanel(VideoPanel videoPanel) {
		this.videoPanel = videoPanel;
	}

	public void setMessageCallback(VideoThreadMsgCallback callback) {
		this.callback = callback;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
        if(initialize() != VIDEO_INIT_SUCCESS)
        	return;
		
        while (!isStop) {
        	int message = connect();
            if(message == VIDEO_CONNECTED)
            	process();
            else if(message == VIDEO_NOT_CONNECTED)
            	break;
            else if(message == VIDEO_AUTO_NOT_CONNECTED)
            	continue;
        }
        
        callback.onMessage(VIDEO_STOPPED);
    }

	public synchronized int initialize() {		
		int err;
		
		//
		err = initializeCodec();
		if(err != 0) {
			callback.onMessage(VIDEO_INIT_FAIL);
			return err;
		}
		//
		err = initializeSocket();
		if(err != 0) {
			callback.onMessage(VIDEO_INIT_FAIL);
			return err;
		}
		
		initializeFile();
		callback.onMessage(VIDEO_INIT_SUCCESS);
		initializePanel();
		return VIDEO_INIT_SUCCESS;
	}
	
    private int initializeCodec() {
        if(isSet)
            Codec.decode_release();
        Codec.decode_init(videoOption.getVideoResizeWidth(), videoOption.getVideoResizeHeight());
        isSet = true;

        return 0;
    }
    
	private int initializeSocket() {
        try {
			reliableSock.initialize(socketOption.getServerReliablePort(),
					socketOption.getClientReliablePort(),
					InetAddress.getByName(socketOption.getServerIp()),
					socketOption.getClientReliableSegSize(),
					socketOption.getClientReliableSleepTime(),
					socketOption.getClientReliableTimeout());

	        nonReliableSock.initialize(socketOption.getServerNonReliablePort(),
	        		socketOption.getClientNonReliablePort(),
	        		InetAddress.getByName(socketOption.getServerIp()),
	        		socketOption.getClientNonReliableSegSize(),
	        		socketOption.getClientNonReliableSleepTime(),
	        		socketOption.getClientNonReliableTimeout());
	        System.out.println("serverIp: " + socketOption.getServerIp() +
	        		", SegSize: " + socketOption.getClientNonReliableSegSize() + 
	        		", SleepTime: " + socketOption.getClientNonReliableSleepTime());
	        
        } catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
        return 0;
	}
    
	private void initializePanel() {
		videoPanel.setCanvasSize(videoOption.getVideoWidth(), videoOption.getVideoHeight());
	}

    private void initializeFile() {
        //Log.d(TAG, "fileInitialize");
        File file = new File(Path.getFrPath());

        if(file.exists()) {
            if (file.isDirectory()) {
                String[] children = file.list();
                for (int i = 0; i < children.length; i++)
                    new File(file, children[i]).delete();
            }
            file.delete();
        }

        if(!file.exists())
            file.mkdir();
    }
	
	private int connect() {
        //Log.d(TAG, "connect");

        int count = 0;
        while(!sendConfig() && !isStop) {
            count++;
            if(count % socketOption.getConnectReRequestNum() == 0) {
            	if(socketOption.isAutoConnect()) {
            		callback.onMessage(VIDEO_AUTO_NOT_CONNECTED);
            		return VIDEO_AUTO_NOT_CONNECTED;
            	}
            	else {
            		callback.onMessage(VIDEO_NOT_CONNECTED);
            		return VIDEO_NOT_CONNECTED;
            	}
            }
        }
        callback.onMessage(VIDEO_CONNECTED);        
        return VIDEO_CONNECTED;
    }
	
    private boolean sendConfig() {
        byte[] configBytes = new byte[24];
        System.arraycopy(new byte[]{0x12,0x34,0x56,0x00},0,configBytes,0,4);
        System.arraycopy(Util.intToBytes(videoOption.getVideoResizeWidth()),0,configBytes,4,4);
        System.arraycopy(Util.intToBytes(videoOption.getVideoResizeHeight()),0,configBytes,8,4);
        System.arraycopy(Util.intToBytes(videoOption.getBitRate()),0,configBytes,12,4);
        System.arraycopy(Util.intToBytes(videoOption.getFrameRate()),0, configBytes,16,4);
        System.arraycopy(Util.intToBytes(videoOption.getKeyFrameRate()),0,configBytes,20,4);
        switch (videoOption.getBitRate()) {
            case 16384:
            	socketOption.setClientNonReliableSleepTime(60);
            	socketOption.setClientReliableSleepTime(60);
            	videoOption.setCrf(47);
                break;
            case 24576:
                socketOption.setClientNonReliableSleepTime(45);
                socketOption.setClientReliableSleepTime(45);
                videoOption.setCrf(44);
                break;
            case 32768:
            	socketOption.setClientNonReliableSleepTime(1);
                socketOption.setClientReliableSleepTime(1);
                videoOption.setCrf(40);
                break;
            case 40960:
            	socketOption.setClientNonReliableSleepTime(26);
            	socketOption.setClientReliableSleepTime(26);
            	videoOption.setCrf(39);
                break;
            case 49152:
            	socketOption.setClientNonReliableSleepTime(22);
            	socketOption.setClientReliableSleepTime(22);
            	videoOption.setCrf(38);
                break;
            case 57344:
            	socketOption.setClientNonReliableSleepTime(17);
            	socketOption.setClientReliableSleepTime(17);
            	videoOption.setCrf(37);
                break;
            case 65536:
            	socketOption.setClientNonReliableSleepTime(15);
            	socketOption.setClientReliableSleepTime(15);
            	videoOption.setCrf(35);
                break;
            default:
            	videoOption.setCrf(40);
                break;
        }

    	boolean result = reliableSock.send(configBytes);
        return result;
        /*
    	try {
			//nonReliableSock.clear();

        } catch (SocketTimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch(SocketException e) {
			e.printStackTrace();
			return false;
		}
		*/
    }
	    
	private void process() {
        while (!isStop) {
            if(!recvFrame())
                strangeCount++;
            else
                strangeCount = 0;

            if(strangeCount >= videoOption.getAbnormalFrameNum()) {
                strangeCount = 0;
                break;
            }
        }		
	}
    
	int count = 0;
    private synchronized boolean recvFrame() {
        //Log.d(TAG, "recvFrame");
        byte[] packetSize = nonReliableSock.recv(socketOption.getClientNonReliableSegSize());
        
        if(packetSize.length == 0)
            return false;
        else {
            strangeCount = 0;
            if(packetSize[0] == 0x78 && packetSize[1] == 0x56 && packetSize[2] == 0x34 && packetSize[3] == 0x12) {

            	int intPacketSize = Util.bytesToInt(packetSize, 4);
                ByteBuffer buf = ByteBuffer.wrap(packetSize);
                LongBuffer longBuf = buf.asLongBuffer();
                long l[] = new long[longBuf.capacity()];
                longBuf.get(l);
                long frameTimestamp = l[1];
                boolean isSave = false;
                if(packetSize[16] == 1)
                    isSave = true;
                byte[] completeFrame;

                if(intPacketSize > packetSize.length) {
                	byte[] frame = nonReliableSock.recv(intPacketSize - packetSize.length);

                	completeFrame = new byte[packetSize.length - 17 + frame.length];
                    System.arraycopy(packetSize, 17, completeFrame, 0, packetSize.length - 17);
                    System.arraycopy(frame, 0, completeFrame, packetSize.length - 17, frame.length);
                }
                else {
                    completeFrame = new byte[packetSize.length - 17];
                    System.arraycopy(packetSize, 17, completeFrame, 0, packetSize.length - 17);
                }
                
                if (completeFrame.length != 0) {
                    byte[] decodedFrame = Codec.decode_frame(completeFrame);

                    if (isVideoSave)
                        Codec.write_video(decodedFrame);
                    if (decodedFrame.length == 1) {
                    	System.out.println("asdasd");
                        sendConfig();
                        initializeCodec();
                        return false;
                    } else {
                    	Mat result = ImageUtil.resizeImage(decodedFrame,
                        		videoOption.getVideoResizeWidth(), videoOption.getVideoResizeHeight(),
                        		videoOption.getVideoWidth(), videoOption.getVideoHeight());

                        if(isSave == true) {
                        	Highgui.imwrite(frameTimestamp+".jpg", result);
                            Imgproc.resize(result, result, new Size(imageListOption.getSumnailWidth(), imageListOption.getSumnailHeight()), 0, 0, Imgproc.INTER_CUBIC);
                            Highgui.imwrite(frameTimestamp+"-SN.jpg", result);
                            
                            timestamps.add(frameTimestamp);
                            if(timestamps.size() == imageListOption.getMaxNode()) {
                                File firstSNImg = new File(Path.getFrPath() + timestamps.get(0) + "-SN.jpg");
                                File firstOrImg = new File(Path.getFrPath() + timestamps.get(0) + "-original.jpg");
                                boolean deleted = firstSNImg.delete();
                                boolean deleted2 = firstOrImg.delete();

                                if (!deleted)
                                    System.out.println("file delete error occured!!");

                                if (!deleted2)
                                    System.out.println("file delete error occured!!");

                                timestamps.remove(0);
                            }
                        }
                        if(isDrawRect) {
                            Mat rectResult = ImageUtil.drawRect(result, videoOption.getVideoWidth(), videoOption.getVideoHeight(), currentRectList);
                            rectClear++;
                            if (rectClear >= videoOption.getFrameRate() * 2)
                                currentRectList = null;
							videoPanel.drawImage(rectResult);
                        }
                        else
                        	videoPanel.drawImage(result);
                        //System.out.println("video draw!!!!!!!!!!!!!!!!!!");
                    }
                }
            }
            else if(packetSize[0] == 0x78 && packetSize[1] == 0x56 && packetSize[2] == 0x34 && packetSize[3] == 0x11) {
                List<Rect> rectList = new ArrayList<>();
                short number = Util.bytesToShort(packetSize, 4);
                byte[] result = new byte[number*8];
                System.arraycopy(packetSize, 6, result, 0, packetSize.length - 6);
                //Log.d(TAG, number+"");
                if(6 + number*8 > packetSize.length) {
                    byte[] remain = nonReliableSock.recv(6 + number*8 - packetSize.length);
                    System.arraycopy(remain, 0, result, packetSize.length - 6, remain.length);
                }

                for(int i=0; i<number; i++) {
                    rectList.add(new Rect(new Point((double)Util.bytesToShort(result, i*8), (double)Util.bytesToShort(result, 2+i*8)),
                            new Point((double)Util.bytesToShort(result, 4+i*8), (double)Util.bytesToShort(result, 6+i*8))));
                }
                currentRectList = rectList;
                rectClear = 0;
            }
        }

        return true;
    }
    
    
    public synchronized void saveStart() {
    	this.isVideoSave = true;
    	Codec.open_file(new SimpleDateFormat("dd-MM-yyyy").format(new Date()),
    			videoOption.getVideoResizeWidth(),
    			videoOption.getVideoResizeHeight(),
    			videoOption.getFrameRate(),
    			videoOption.getKeyFrameRate(),
    			videoOption.getColorFormat(),
    			videoOption.getBitRate(),
    			videoOption.getCrf());
    }
    
    public synchronized void saveStop() {
    	this.isVideoSave = false;
    	Codec.close_file();
    }
    
    public void motionDetectStart() {
    	this.isDrawRect = true;
    }
    
    public void motionDetectStop() {
    	this.isDrawRect = false;
    }
    
    public void threadStop() {
    	this.isStop = true;
    }
}