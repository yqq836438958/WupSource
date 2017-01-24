package qrom.component.wup.base;

import qrom.component.wup.core.WupStartUp;
import qrom.component.wup.core.WupV1Support;
import android.content.Context;

/**
 *  持有全局的ApplicationContext
 * @author wileywang
 *
 */
public class ContextHolder {
	private static Context sApplicationContext;
	
	public static void setApplicationContext(Context context) {
		if (context == null) {
			throw new IllegalArgumentException("context should not be null");
		}
		
		if (sApplicationContext == null) {
			synchronized(ContextHolder.class) {
				if (sApplicationContext == null) {
					sApplicationContext = context.getApplicationContext();
					if (sApplicationContext == null) {
						throw new IllegalArgumentException(
								"please check context, context's getApplicationContext returns null");
					}
					
					// 非常不想要这句话，为了让恶心的广播的API运作起来，破坏了一定的结构
					// 但是作为初始化的唯一入口，目前依赖这里较为不破坏上层的使用
					WupV1Support.get().onContextAttached(sApplicationContext);
					WupStartUp.onContextAttached(sApplicationContext);
				}
			}
		}
	}
	
	public static Context getApplicationContext() {
		return sApplicationContext;
	}
	
	public static Context getApplicationContextForSure() {
		if (sApplicationContext == null) {
			throw new IllegalStateException("please init application context!");
		}
		
		return sApplicationContext;
	}
	
}
