package imagelist;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ImageListItemController {
	@FXML ImageView itemImageView;
	
	public void setImage(String path) {
		Image image = new Image(path);
		itemImageView.setImage(image);
	}
}
