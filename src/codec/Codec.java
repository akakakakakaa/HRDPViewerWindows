package codec;

import org.opencv.core.Core;

public class Codec {
	static {
    	String libpath = System.getProperty("java.library.path");
    	libpath = libpath + ";D:/eclipseWorkspace/JavaViewer/libs";
    	System.setProperty("java.library.path",libpath);

		System.loadLibrary("avutil-55");
		System.loadLibrary("postproc-54");
		System.loadLibrary("swresample-2");
		System.loadLibrary("swscale-4");
		System.loadLibrary("avcodec-57");		
		System.loadLibrary("avformat-57");
		System.loadLibrary("avfilter-6");
		System.loadLibrary("avdevice-57");
		System.loadLibrary("Codec");
	}
	public static native void decode_release();
	public static native void decode_init(int videoResizeWidth, int videoResizeHeight);
	public static native byte[] decode_frame(byte[] frame);

    public static native void open_file(String filename, int width, int height, int frame_rate, int key_frame_rate, int color_format, int bit_rate, int crf);	
	public static native void write_video(byte[] decodedFrame);
    public static native void close_file();
}
