package qrom.component.wup.base.utils;

/**
  *  字节流工具 
  */
public class ByteUtil {
	
	/**
	 *  判断两个数据是否一致
	 * @param src
	 * @param target
	 * @return
	 */
	public static boolean isEquals(byte[] src, byte[] target) {
		if (src == target) {
			return true;
		}
		
		if (src != null && target == null) {
			return false;
		}
		
		if (target != null && src == null) {
			return false;
		}
		
		if (src.length != target.length) {
			return false;
		}
		
		for (int index = 0; index < src.length; ++index) {
			if (src[index] != target[index]) {
				return false;
			}
		}
		return true;
	}
	
}
