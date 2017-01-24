package qrom.component.wup.switcher;

import java.io.File;

import qrom.component.log.QRomLog;
import qrom.component.wup.base.utils.ListenerList;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * @author wileywang
 *
 */
public class FileSwitchProvider  implements ISwitchProvider {
	private static final String LOG_TAG = FileSwitchProvider.class.getSimpleName();
	
	private static final String ALL_CLOSE_FILENAME = "wup_all_closed";
	private File mSwitchFile;
	private Context mApplicationContext;
	private BroadcastReceivorProxy mReceivorProxy;
	
	public FileSwitchProvider(Context applicationContext) {
		mApplicationContext = applicationContext;
		mSwitchFile = new File(applicationContext.getFilesDir().getAbsoluteFile() + "/" + ALL_CLOSE_FILENAME);
		mReceivorProxy = new BroadcastReceivorProxy();
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(mReceivorProxy.getIntentAction());
		mApplicationContext.registerReceiver(mReceivorProxy, filter);
	}
	
	@Override
	public boolean isAllClosed() {
		return mSwitchFile.exists();
	}

	@Override
	public void setCloseAll(boolean needClose) {
		if (needClose == isAllClosed()) {
			return ;
		}
		
		try {
			if (needClose) {
				if (!mSwitchFile.exists()) {
					mSwitchFile.createNewFile();
				}
			} else {
				if (mSwitchFile.exists()) {
					mSwitchFile.delete();
				}
			}
		} catch (Exception e) {
			QRomLog.e(LOG_TAG, e);
		}
		Intent intent = new Intent();
		intent.setAction(mReceivorProxy.getIntentAction());
		mApplicationContext.sendBroadcast(intent);
	}

	@Override
	public void registerSwitchListener(ISwitchListener listener) {
		mReceivorProxy.registerSwitchListener(listener);
	}

	@Override
	public void unRegisterSwitchListener(ISwitchListener listener) {
		mReceivorProxy.unreigsterSwitchListener(listener);
	}
	
	private class BroadcastReceivorProxy extends BroadcastReceiver {
		private ListenerList<ISwitchListener> mSwitchListenerList;
		private String mIntentAction;
		
		public BroadcastReceivorProxy() {
			mIntentAction = mApplicationContext.getPackageName() + ".wup.switchChanged";
			mSwitchListenerList = new ListenerList<ISwitchListener>() {
				@Override
				protected void onNotifyListener(ISwitchListener listener,
						Object... params) {
					listener.onSwitchChanged();
				}
			};
		}
		
		public void registerSwitchListener(ISwitchListener listener) {
			mSwitchListenerList.registerListener(listener);
		}
		
		public void unreigsterSwitchListener(ISwitchListener listener) {
			mSwitchListenerList.unregisterListener(listener);
		}
		
		public String getIntentAction() {
			return mIntentAction;
		}
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction() != null && intent.getAction().equalsIgnoreCase(getIntentAction())) {
				mSwitchListenerList.notifyListeners();
			}
		}
		
	}
	
}
