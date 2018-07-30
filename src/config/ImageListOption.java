package config;

public class ImageListOption {
	private int maxNode;
	private int sumnailWidth;
	private int sumnailHeight;

	public ImageListOption() {
	}
	
	public ImageListOption(int maxNode, int sumnailWidth, int sumnailHeight) {
		this.maxNode = maxNode;
		this.sumnailWidth = sumnailWidth;
		this.sumnailHeight = sumnailHeight;
	}
	
	public static ImageListOption getDefaultOption() {
		ImageListOption option = new ImageListOption(200, 64, 64);
		return option;
	}
	
	public int getMaxNode() {
		return maxNode;
	}

	public void setMaxNode(int maxNode) {
		this.maxNode = maxNode;
	}

	public int getSumnailWidth() {
		return sumnailWidth;
	}

	public void setSumnailWidth(int sumnailWidth) {
		this.sumnailWidth = sumnailWidth;
	}

	public int getSumnailHeight() {
		return sumnailHeight;
	}

	public void setSumnailHeight(int sumnailHeight) {
		this.sumnailHeight = sumnailHeight;
	}
}
