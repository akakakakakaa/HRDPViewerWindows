package video;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import org.opencv.core.Mat;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;

public class VideoPanel extends Pane {
	private Canvas canvas;
	private char isCanvasInitialized = 0;
	//private Queue<BufferedImage> imageQueue = new PriorityBlockingQueue<BufferedImage>();
	private BufferedImage bufferedImage = null;
	
	public VideoPanel() {
		canvas = new Canvas();
		getChildren().add(canvas);
		widthProperty().addListener(e -> setCanvasWidth());
		heightProperty().addListener(e -> setCanvasHeight());
	}
		
	private void setCanvasWidth() {
		canvas.setWidth(getWidth());
		isCanvasInitialized |= 0x01;
	}
	
	private void setCanvasHeight() {
		canvas.setHeight(getHeight());
		isCanvasInitialized |= 0x10;
	}

	public synchronized void setCanvasSize(double width, double height) {
		isCanvasInitialized = 0x00;
		setPrefSize(width, height);
		requestLayout();
	}
	
	public synchronized void drawImage(Mat image) {
//		if(isCanvasInitialized == 0x11) {
	    	BufferedImage bufferedImage = new BufferedImage(image.width(), image.height(), BufferedImage.TYPE_3BYTE_BGR);
	    	byte[] data = ((DataBufferByte) bufferedImage.getRaster().getDataBuffer()).getData();
	    	image.get(0, 0, data);
	    	
	    	File file = new File("test.jpg");
	    	try {
				ImageIO.write(bufferedImage, "jpg", file);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//imageQueue.add(bufferedImage);
	    	this.bufferedImage = bufferedImage;
			requestLayout();
			//System.out.println("add queue");
//		}
	}
	
	@Override
	protected synchronized void layoutChildren() {
		super.layoutChildren();

		/*
		if(imageQueue.size() != 0) {
			WritableImage writableImage = SwingFXUtils.toFXImage(imageQueue.remove(), null);
			if(isCanvasInitialized == 0x11)
				canvas.getGraphicsContext2D().drawImage(writableImage, 0, 0, canvas.getWidth(), canvas.getHeight());
			requestLayout();
		}
		*/
		if(bufferedImage != null) {
			WritableImage writableImage = SwingFXUtils.toFXImage(bufferedImage, null);
			if(isCanvasInitialized == 0x11)
				canvas.getGraphicsContext2D().drawImage(writableImage, 0, 0, canvas.getWidth(), canvas.getHeight());
			bufferedImage = null;
		}
		//System.out.println("requestLayout!!");
	}
}