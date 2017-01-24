package qrom.component.wup.statics;

import java.util.Map;

public interface IStatExecutor {
	public String getQImei();
	
	public void triggerWupMonitorData(Map<String, String> monitorDataMap);
}
