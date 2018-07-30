package config;

//multi video를 볼경우를 생각해 singleton으로 하지 않음.
public class VideoOption {
	private int videoResizeWidth = 320;
	private int videoResizeHeight = 240;
	private int videoWidth = 640;
	private int videoHeight = 480;
	private int bitRate = 32;
	private int frameRate = 10;
	private int keyFrameRate = 20;
	private int colorFormat = 1;
	private int crf = 40;
	
	private int abnormalFrameNum = 10;
	private boolean isMotionDetect = false;
	public VideoOption() {
		
	}
	
	public VideoOption(int videoResizeWidth,
			int videoResizeHeight,
			int videoWidth,
			int videoHeight,
			int bitRate,
			int frameRate,
			int keyFrameRate,
			int colorFormat,
			int crf,
			int abnormalFrameNum,
			boolean isMotionDetect) {
		this.videoResizeWidth = videoResizeWidth;
		this.videoResizeHeight = videoResizeHeight;
		this.videoWidth = videoWidth;
		this.videoHeight = videoHeight;
		this.bitRate = bitRate;
		this.frameRate = frameRate;
		this.keyFrameRate = keyFrameRate;
		this.colorFormat = colorFormat;
		this.crf = crf;
		this.abnormalFrameNum = abnormalFrameNum;
		this.setMotionDetect(isMotionDetect);
	}

	public static VideoOption getDefaultOption() {
		VideoOption videoOption = new VideoOption(320, 240, 640, 480, 32768, 10, 20, 1, 40, 10, false);
		return videoOption;
	}
	
	public int getVideoResizeWidth() {
		return videoResizeWidth;
	}

	public void setVideoResizeWidth(int videoResizeWidth) {
		this.videoResizeWidth = videoResizeWidth;
	}

	public int getVideoResizeHeight() {
		return videoResizeHeight;
	}

	public void setVideoResizeHeight(int videoResizeHeight) {
		this.videoResizeHeight = videoResizeHeight;
	}

	public int getVideoWidth() {
		return videoWidth;
	}

	public void setVideoWidth(int videoWidth) {
		this.videoWidth = videoWidth;
	}

	public int getVideoHeight() {
		return videoHeight;
	}

	public void setVideoHeight(int videoHeight) {
		this.videoHeight = videoHeight;
	}

	public int getBitRate() {
		return bitRate;
	}

	public void setBitRate(int bitRate) {
		this.bitRate = bitRate;
	}

	public int getFrameRate() {
		return frameRate;
	}

	public void setFrameRate(int frameRate) {
		this.frameRate = frameRate;
	}

	public int getKeyFrameRate() {
		return keyFrameRate;
	}

	public void setKeyFrameRate(int keyFrameRate) {
		this.keyFrameRate = keyFrameRate;
	}	
	
	public int getCrf() {
		return crf;
	}

	public void setCrf(int crf) {
		this.crf = crf;
	}

	public int getAbnormalFrameNum() {
		return abnormalFrameNum;
	}

	public void setAbnormalFrameNum(int abnormalFrameNum) {
		this.abnormalFrameNum = abnormalFrameNum;
	}

	public int getColorFormat() {
		return colorFormat;
	}

	public void setColorFormat(int colorFormat) {
		this.colorFormat = colorFormat;
	}

	public boolean isMotionDetect() {
		return isMotionDetect;
	}

	public void setMotionDetect(boolean isMotionDetect) {
		this.isMotionDetect = isMotionDetect;
	}
	
}
