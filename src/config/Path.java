package config;

public class Path {
	private static String configPath = System.getProperty("user.dir")+"/configPath";
	private static String frPath = System.getProperty("user.dir")+"/frPath";
	private static String videoPath = System.getProperty("user.dir")+"/videoPath";
	
	public static String getConfigPath() {
		return configPath;
	}

	public static void setConfigPath(String configPath) {
		Path.configPath = configPath;
	}

	public static String getFrPath() {
		return frPath;
	}

	public static void setFrPath(String frPath) {
		Path.frPath = frPath;
	}

	public static String getVideoPath() {
		return videoPath;
	}

	public static void setVideoPath(String videoPath) {
		Path.videoPath = videoPath;
	}
}
