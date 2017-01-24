package qrom.component.wup.core;

import qrom.component.log.QRomLog;
import qrom.component.wup.QRomQuaFactory;
import qrom.component.wup.base.ContextHolder;
import qrom.component.wup.base.utils.PhoneStatUtils;
import qrom.component.wup.base.utils.StringUtil;
import qrom.component.wup.base.utils.ValueCacheHolder;


/**
 *  基础信息管理类
 * @author wileywang
 *
 */
public class BaseInfoManager {
	
	private static final String TAG = "BaseInfoManager";
	
	private static BaseInfoManager sInstance;
	protected BaseInfoManager() {
	}
	
	public static BaseInfoManager get() {
		if (sInstance == null) {
			synchronized(BaseInfoManager.class) {
				if (sInstance == null) {
					sInstance = new BaseInfoManager();
				}
			}
		}
		return sInstance;
	}

	private  ValueCacheHolder<String> mQuaCacheHolder = new ValueCacheHolder<String>() {
		@Override
		protected String buildValue() {
			String qua = QRomQuaFactory.buildQua(ContextHolder.getApplicationContextForSure());
			QRomLog.i(TAG, "buildQua qua is " + qua);
			return qua;
		}
	};
	
	private ValueCacheHolder<String> mImeiCacheHolder = new ValueCacheHolder<String>() {

		@Override
		protected String buildValue() {
			return PhoneStatUtils.getImei(ContextHolder.getApplicationContextForSure());
		}
		
		protected boolean isValueValid() {
			return !StringUtil.isEmpty(mValue);
		}
		
	};
	
	private ValueCacheHolder<String> mAppLCCacheHolder = new ValueCacheHolder<String>() {

		@Override
		protected String buildValue() {
			return QRomQuaFactory.getLC(ContextHolder.getApplicationContextForSure());
		}
		
	};
	
	private ValueCacheHolder<String> mCurrentProcessNameCacheHolder = new ValueCacheHolder<String>() {

		@Override
		protected String buildValue() {
			return PhoneStatUtils.getCurProcessName(
						ContextHolder.getApplicationContextForSure());
		}
		
	};
	
	public String getQua() {
		return mQuaCacheHolder.getValue();
	}
	
	public String getImei() {
		return mImeiCacheHolder.getValue();
	}
	
	public String getLC() {
		return mAppLCCacheHolder.getValue();
	}
		
	public String getCurrentProcessName() {
		return mCurrentProcessNameCacheHolder.getValue();
	}
	
}
