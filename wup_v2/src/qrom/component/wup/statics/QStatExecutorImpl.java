package qrom.component.wup.statics;

import java.util.Map;

import qrom.component.statistic.QStatExecutor;

import android.content.Context;

/**
 *  编译时携带统计的jar包，但是真正运行时，并不对统计有必然依赖
 * @author wileywang
 *
 */
class QStatExecutorImpl implements IStatExecutor {
	
	public QStatExecutorImpl(Context context) {
		QStatExecutor.init(context);
	}
	
	@Override
	public String getQImei() {
		return QStatExecutor.getQIMEI();
	}

	@Override
	public void triggerWupMonitorData(Map<String, String> monitorDataMap) {
		QStatExecutor.triggerWupMonitorData(monitorDataMap);
	}

}
