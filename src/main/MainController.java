package main;

import java.net.URL;
import java.util.ResourceBundle;

import config.ImageListOption;
import config.SocketOption;
import config.VideoOption;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import socket.NonReliableSock;
import socket.ReliableSock;

public class MainController implements Initializable {
	
	
	@FXML private TabPane mainViewRoot;
	@FXML private AnchorPane mainView;
	
	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {		
		mainView.setPrefWidth(mainViewRoot.getWidth());
		mainView.setPrefHeight(mainViewRoot.getHeight());
	}
}
