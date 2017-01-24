package qrom.component.wup.core;

import qrom.component.wup.guid.GuidProxy;
import qrom.component.wup.threads.TempThreadPool;
import android.content.Context;

/**
 *  
 * @author wileywang
 *
 */
public class WupStartUp {
	
	public static void onContextAttached(Context applicationContext) {
		/**
		 *  主要是为了加速GUID的获取
		 *  以及触发无GUID情况下的促使Provider中GUIDEngine的初始化，加速GUID的获取(对单发模式效果更好)
		 */
		TempThreadPool.get().post(new Runnable() {
			@Override
			public void run() {
				GuidProxy.get().getGuidBytes();
			}
		});
	}
}
