package qrom.component.wup.statics;

import java.util.HashMap;
import java.util.Map;

import qrom.component.log.QRomLog;
import qrom.component.wup.QRomWupConstants.BASEINFO_ERR_CODE;
import qrom.component.wup.base.ContextHolder;
import qrom.component.wup.build.BuildInfo;
import qrom.component.wup.sync.security.StatisticListener;

/**
 *  统计的代理层，主要屏蔽当统计jar包不存在时的相关问题
 * @author wileywang
 *
 */
public class StatProxy implements StatisticListener {
	private static final String TAG = StatProxy.class.getSimpleName();
	
	private static StatProxy sInstance;
	
	public static StatProxy get() {
		if (sInstance == null) {
			synchronized(StatProxy.class) {
				if (sInstance == null) {
					sInstance = new StatProxy();
				}
			}
		}
		
		return sInstance;
	}
	
	private IStatExecutor mStatExecutor;
	
	private StatProxy() {
		try {
			Class.forName("qrom.component.statistic.QStatExecutor");
			mStatExecutor = new QStatExecutorImpl(ContextHolder.getApplicationContextForSure());
		} catch (ClassNotFoundException e) {
			QRomLog.e(TAG, "init StatProxy failed, not found for " + e.getMessage());
		} catch (Throwable e) {
			QRomLog.e(TAG, e.getMessage(), e);
		}
	}
	
	
	// 获取QIMEI
	public String getQImei() {
		if (mStatExecutor == null) {
			return BASEINFO_ERR_CODE.QIME_NO_FIND_STAT;
		}
		
		return mStatExecutor.getQImei();
	}
	
	private static final int MILLIS_FOR_DAY = 24 * 60 * 60 * 1000;
	
	// 请求统计上报
	public void reportReqStat(ReqStatBaseInfo reqStatInfo) {
		if (mStatExecutor == null) {
			return ;
		}
		
		// 抽样, 抽取百分之一的用户进行上报, 复用上一版本的抽样逻辑
		int checkFlg = 0;
        if (reqStatInfo.getGuidBytes() != null && reqStatInfo.getGuidBytes().length >0) {
            checkFlg = reqStatInfo.getGuidBytes()[reqStatInfo.getGuidBytes().length -1] & 0Xff;
        }
        checkFlg = checkFlg % 100;
        int dayFlg = (int) (System.currentTimeMillis() / MILLIS_FOR_DAY) % 100;
        if (dayFlg != checkFlg) {
        	return ;
        }
        
        if (reqStatInfo.getReqConnectInfo() != null 
        	&& !reqStatInfo.getReqConnectInfo().isConnected()
        	&& reqStatInfo.getRespConnectInfo() != null
        	&& !reqStatInfo.getRespConnectInfo().isConnected()) {
        	// 请求时和失败时网咯状况都是无，则不需要上报
        	return ;
        }
        
        mStatExecutor.triggerWupMonitorData(reqStatInfo.buildStatMap());
	}

	@Override
	public void onStartDuration(String packageName, long duration) {
		if (mStatExecutor == null) {
			return ;
		}
		
		Map<String, String> info = new HashMap<String, String>();
		info.put(StatConstants.KEY.SDK_VER, BuildInfo.BUILD_VERSION_DATE);
		info.put(StatConstants.KEY.ASYM_APP_PACKAGE, packageName);
		info.put(StatConstants.KEY.ASYM_START_DURATION, String.valueOf(duration));
		
		mStatExecutor.triggerWupMonitorData(info);
	}

	@Override
	public void onVerfiyAppKeyResult(String packageName, boolean sucess) {
		if (mStatExecutor == null) {
			return ;
		}
		
		Map<String, String> info = new HashMap<String, String>();
		info.put(StatConstants.KEY.SDK_VER, BuildInfo.BUILD_VERSION_DATE);
		String res = StatConstants.VALUE.RESULT_FAIL;
		if (sucess) {
			res = StatConstants.VALUE.RESULT_SUCCESS;
		}
		info.put(StatConstants.KEY.ASYM_APP_PACKAGE, packageName);
		info.put(StatConstants.KEY.ASYM_APPKEY, res);
		
		mStatExecutor.triggerWupMonitorData(info);
	}

	@Override
	public void onVerifyRootKeyResult(String packageName, boolean sucess) {
		if (mStatExecutor == null) {
			return ;
		}
		
		Map<String, String> info = new HashMap<String, String>();
		
		info.put(StatConstants.KEY.SDK_VER, BuildInfo.BUILD_VERSION_DATE);
		
        String res =  StatConstants.VALUE.RESULT_FAIL;
        if (sucess) {
            res = StatConstants.VALUE.RESULT_SUCCESS;
        }
        info.put(StatConstants.KEY.ASYM_APP_PACKAGE, packageName);
        info.put(StatConstants.KEY.ASYM_ROOTKEY, res);
        
        mStatExecutor.triggerWupMonitorData(info);
	}
}
