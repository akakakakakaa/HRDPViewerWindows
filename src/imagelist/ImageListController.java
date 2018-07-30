package imagelist;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

import config.Path;
import config.SocketOption;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import socket.ReliableSock;
import util.Util;

public class ImageListController implements Initializable {
	@FXML private ImageView topImageView;
	@FXML private ListView<ImageInfo> imageListView;
	
	private ReliableSock reliableSock;
	private SocketOption socketOption;
	public void setReliableSock(ReliableSock reliableSock) {
		this.reliableSock = reliableSock;
	}
	
	public void setSockOption(SocketOption socketOption) {
		this.socketOption = socketOption;
	}
	
	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				getImageList();
			}
		}).start();
	}
	
	public void getImageList() {
        int strangeCount = 0;
        while(true) {
            ArrayList<Long> timestamps = getTimestampList();
            if (timestamps != null) {
            	ObservableList<ImageInfo> imageList = FXCollections.observableArrayList();
                File f = new File(Path.getFrPath());
                if (!f.exists())
                    f.mkdir();
                File file[] = f.listFiles();
                for (int i = 0; i < file.length; i++)
                    if (file[i].getName().indexOf("-SN.jpg") != -1) {
                        long timestamp = Long.parseLong(file[i].getName().substring(0, file[i].getName().indexOf("-")));
                        int index = timestamps.indexOf(timestamp);
                        if(index != -1) {
                            ImageInfo imageInfo = new ImageInfo(Path.getFrPath() + file[i].getName(), timestamps.get(index)); // 수정 필요
                            imageList.add(imageInfo);
                        }
                    }

                if(imageList.size() != 0) {
                	imageListView.setCellFactory(lv -> new ListCell<ImageInfo>() {
                		private Node graphic;
                		private ImageListItemController controller;
                		
                		{
                			try {
                				FXMLLoader loader = new FXMLLoader(getClass().getResource("imageListViewItem.fxml"));
                				graphic = loader.load();
                				controller = loader.getController();
                			} catch(IOException e) {
                				e.printStackTrace();
                			}
                		}
						@Override
						protected void updateItem(ImageInfo imageInfo, boolean empty) {
						    super.updateItem(imageInfo, empty);
						    if (empty) {
						        setGraphic(null);
						    } else {
						        controller.setImage(imageInfo.getPath());
						        setGraphic(graphic);
						    }
						}                	
                	});
                	imageListView.setOnMouseClicked(new EventHandler<Event>() {

						@Override
						public void handle(Event event) {
							// TODO Auto-generated method stub
							ImageInfo info = imageListView.getSelectionModel().getSelectedItem();
							topImageView.setImage(new Image(info.getPath()));
						}
					});
                	imageListView.setItems(imageList);
                }
                else
                    strangeCount = 10;

                break;
            } else
                strangeCount++;

            if(strangeCount >= 10)
                break;
        }

        /*
        if(strangeCount >= 10)
            finish();
		*/
	}
	
    public ArrayList<Long> getTimestampList() {
        //Config.reliableSock.clear();

        ArrayList<Long> timestamps = new ArrayList<>();
        byte[] requestTsListData = new byte[]{0x34, 0x56, 0x78, 0x12, 0x01};
        if(!reliableSock.send(requestTsListData))
            return null;

        byte[] timestampSize = reliableSock.recv(4);
        if(timestampSize.length != 0) {
            byte[] timestampList = reliableSock.recv(8 * Util.bytesToInt(timestampSize, 0));
            if(timestampList.length != 0) {
                ByteBuffer buf = ByteBuffer.wrap(timestampList);
                LongBuffer longBuf = buf.asLongBuffer();
                long l[] = new long[longBuf.capacity()];
                longBuf.get(l);
                for (int j = 0; j < Util.bytesToInt(timestampSize, 0); j++) {
                    //Log.d(TAG, "timestamp " + j + " : " + l[j]);
                    timestamps.add(l[j]);
                }

                Collections.sort(timestamps, new Comparator<Long>() {
                    @Override
                    public int compare(Long aLong, Long t1) {
                        return aLong.compareTo(t1);
                    }
                });
            }
        }
        else
            return null;

        return timestamps;
    }

    public byte[] requestImg(long timestamp, int preX, int preY, int postX, int postY) {
        reliableSock.clear();

        byte[] requestImgData = new byte[]{0x34, 0x56, 0x78, 0x12, 0x02};
        if(!reliableSock.send(requestImgData))
            return null;

        byte[] rect = new byte[24];
        //Log.d(TAG, "prex: "+preX+" prey: "+preY+" postX: "+postX+" postY: "+postY);
        System.arraycopy(Util.longToBytes(timestamp), 0, rect, 0, 8);
        System.arraycopy(Util.intToBytes(preX), 0, rect, 8, 4);
        System.arraycopy(Util.intToBytes(preY), 0, rect, 12, 4);
        System.arraycopy(Util.intToBytes(postX), 0, rect, 16, 4);
        System.arraycopy(Util.intToBytes(postY), 0, rect, 20, 4);
        if(!reliableSock.send(rect))
            return null;

        byte[] rawDataSize = reliableSock.recv(4);
        if(rawDataSize.length != 0) {
            //Log.d(TAG, "rawData SIze is : " + Util.bytesToInt(rawDataSize, 0));
            if (Util.bytesToInt(rawDataSize, 0) >= 100000)
                return null;
            else {
                //System.out.println("jpeg Size is "+ Config.toInt(rawDataSize, 0));
                byte[] wantedImg = reliableSock.recv(Util.bytesToInt(rawDataSize, 0));
                if (wantedImg.length != 0)
                    return wantedImg;
            }
        }
        return null;
    }    
}
