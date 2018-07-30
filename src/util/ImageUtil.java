package util;

import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class ImageUtil {
	public static Mat resizeImage(byte[] data, int srcWidth, int srcHeight, int dstWidth, int dstHeight) {
		Mat mat = new Mat(srcHeight*3/2, srcWidth, CvType.CV_8UC1);
        mat.put(0, 0, data);
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_YUV2BGR_YV12);
        //Imgproc.resize(mat, mat, new Size(dstWidth, dstHeight));
        Imgproc.resize(mat, mat, new Size(), ((double)dstWidth)/srcWidth, ((double)dstHeight)/srcHeight, Imgproc.INTER_CUBIC);

        return mat;
	}
	    
    public static Mat drawRect(Mat mat, int width, int height, List<Rect> rectList) {
        if(rectList != null) {
            //for (int i = 0; i < rectList.size(); i++)
            //    Imgproc.rectangle(mat, rectList.get(i).tl(), rectList.get(i).br(), new Scalar(255, 0, 0));
        }
        
        return mat;
    }    
}
