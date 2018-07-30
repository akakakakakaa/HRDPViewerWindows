package util;

public class Util {
	public static byte[] intToBytes(int i) {
		byte[] result = new byte[4];

    	result[0] = (byte) (i >> 24);
    	result[1] = (byte) (i >> 16);
    	result[2] = (byte) (i >> 8);
    	result[3] = (byte) (i /*>> 0*/);

    	return result;
	}
	
	public static int bytesToInt(byte[] data, int offset) {
        return (data[offset] & 0xff) << 24 | (data[1+offset] & 0xff) << 16 |
                (data[2+offset] & 0xff) << 8  | (data[3+offset] & 0xff);
    }
	 
    public static short bytesToShort(byte[] data, int offset) {
        return (short)((data[offset] & 0xff) << 8 | (data[1+offset] & 0xff));
    }	
    
    public static byte[] longToBytes(long i) {
        byte[] result = new byte[8];

        result[0] = (byte) (i >> 56);
        result[1] = (byte) (i >> 48);
        result[2] = (byte) (i >> 40);
        result[3] = (byte) (i >> 32);
        result[4] = (byte) (i >> 24);
        result[5] = (byte) (i >> 16);
        result[6] = (byte) (i >> 8);
        result[7] = (byte) (i /*>> 0*/);

        return result;
    }
}
