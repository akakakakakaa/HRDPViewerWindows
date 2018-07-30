package video;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;

import org.opencv.core.Core;

import config.ImageListOption;
import config.Path;
import config.SocketOption;
import config.VideoOption;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import socket.NonReliableSock;
import socket.ReliableSock;
import video.VideoThread.VideoThreadMsgCallback;

public class VideoController implements Initializable {
	@FXML private VideoPanel videoPanel;
	@FXML private AnchorPane videoPopup;
	@FXML private TextField videoIp;
	@FXML private AnchorPane videoProgress;
	@FXML private AnchorPane videoIcons;
	@FXML private AnchorPane videoOptions;
	@FXML private AnchorPane videoDefaultBtns;
	@FXML private Slider videoBitrate;
	@FXML private ChoiceBox<String> videoResolution;
	@FXML private Slider videoFramerate;

	//
	private SocketOption socketOption;
	private VideoOption videoOption;
	private ImageListOption imageListOption;
	
	//
	private NonReliableSock nonReliableSock;
	private ReliableSock reliableSock;
	
	//
	private boolean isRun = false;
	private boolean isSave = false;
	private boolean isDetect = false;
	private VideoThread videoThread;
	
	private Object syncLock = new Object();

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		//
		videoPanel.setCanvasSize(videoPanel.getWidth(), videoPanel.getHeight());

		//
		videoOption = VideoOption.getDefaultOption();
		socketOption = SocketOption.getDefaultOption();
		imageListOption = ImageListOption.getDefaultOption();

		//
		nonReliableSock = new NonReliableSock();
		reliableSock = new ReliableSock();
		
		videoBitrate.setValue(videoOption.getBitRate());
		videoBitrate.valueProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				// TODO Auto-generated method stub
				
			}
		});
		//
		ObservableList<String> resolutions = FXCollections.observableArrayList("160x120", "320x240", "640x480");
		videoResolution.setItems(resolutions);
		videoResolution.setValue(videoOption.getVideoResizeWidth()+"x"+videoOption.getVideoResizeHeight());
		videoFramerate.setValue(videoOption.getFrameRate());
		videoFramerate.valueProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				// TODO Auto-generated method stub
				
			}
		});
	}

	@FXML
	private void onMouseEntered() {
		if(isRun) {
			showDefaultBtns();
			showVideoOptions();
		}
	}
	
	@FXML
	private void onMouseExited() {
		if(isRun) {
			dismissDefaultBtns();
			dismissVideoOptions();
		}
	}
	
	@FXML
	private void onConnectClicked() {
		socketOption.setServerIp(videoIp.getText());
		run();
	}

	@FXML
	private void onOptionBtnClicked() {
		//videoBitrate.getL
		showVideoOptions();
		dismissDefaultBtns();
		if(!isRun)
			dismissPopup();
	}

	@FXML
	private void onFolderClicked() {
		File file = new File(Path.getVideoPath());
		if(!file.exists())
			file.mkdirs();
		try {
			Desktop.getDesktop().open(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
			
	@FXML
	private void onPictureClicked() {
	}
	
	@FXML
	private void onSaveClicked() {
		synchronized(syncLock) {
			if(isRun && videoThread != null && !isSave)
				videoThread.saveStart();
			else if(isRun && videoThread != null && !isSave)
				videoThread.saveStop();
		}
	}
	
	@FXML
	private void onDetectClicked() {
		synchronized(syncLock) {
			if(isRun && videoThread != null && !isDetect) {
				videoOption.setMotionDetect(true);
				videoThread.motionDetectStart();
			}
			else if(isRun && videoThread != null && !isDetect) {
				videoOption.setMotionDetect(false);
				videoThread.motionDetectStop();
			}
		}
	}
	
	@FXML
	private void onOptionsOKClicked() {		
		dismissVideoOptions();
		showDefaultBtns();
		if(!isRun)
			showPopup();
	}
	
	@FXML
	private void onOptionsCancelClicked() {
		dismissVideoOptions();
		showDefaultBtns();
		if(!isRun)
			showPopup();
	}
	
	private void showDefaultBtns() {
		videoDefaultBtns.setVisible(true);
		videoDefaultBtns.setManaged(true);
	}

	private void dismissDefaultBtns() {
		videoDefaultBtns.setVisible(false);
		videoDefaultBtns.setManaged(false);
	}
	
	private void showPopup() {
		videoPopup.setManaged(true);
		videoPopup.setVisible(true);
	}
	
	private void dismissPopup() {
		videoPopup.setManaged(false);
		videoPopup.setVisible(false);
	}
	
	private void showProgress() {
		videoProgress.setVisible(true);
		videoProgress.setManaged(true);		
	}

	private void dismissProgress() {
		videoProgress.setVisible(false);
		videoProgress.setManaged(false);	
	}

	private void showVideoOptions() {
		videoOptions.setVisible(true);
		videoOptions.setManaged(true);
	}
	
	private void dismissVideoOptions() {
		videoOptions.setVisible(false);
		videoOptions.setManaged(false);
	}

	public synchronized void run() {
		if(videoThread == null) {
			dismissPopup();
			dismissDefaultBtns();
			showProgress();
			
			videoThread = new VideoThread(videoOption, socketOption, nonReliableSock, reliableSock, imageListOption);
			videoThread.setPanel(videoPanel);
			videoThread.setMessageCallback(new VideoThreadMsgCallback() {				
				@Override
				public void onMessage(int messageNum) {
					switch(messageNum) {
					case VideoThread.VIDEO_CONNECTED:
						dismissProgress();
						isRun = true;
						break;
					case VideoThread.VIDEO_NOT_CONNECTED:
						break;
					case VideoThread.VIDEO_AUTO_NOT_CONNECTED:
						break;
					case VideoThread.VIDEO_INIT_SUCCESS:
						break;
					case VideoThread.VIDEO_INIT_FAIL:
						break;
					case VideoThread.VIDEO_STOPPED:
						synchronized(syncLock) {
							dismissProgress();
							dismissVideoOptions();
							videoThread.saveStop();
							isRun = false;
							videoThread = null;
							showPopup();
							showDefaultBtns();
						}
						break;
					default:
						break;
					}
				}
			});
			videoThread.start();
		}
	}
}