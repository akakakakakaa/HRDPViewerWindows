package imagelist;

public class ImageInfo {
	private String path;
	private long timestamp;
	
	public ImageInfo(String path, long timestamp) {
		this.setPath(path);
		this.setTimestamp(timestamp);
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
}
