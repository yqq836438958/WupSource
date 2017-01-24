package qrom.component.wup.sync.security;

public interface StatisticListener {
    
    void onVerifyRootKeyResult(String packageName, boolean success);
    void onVerfiyAppKeyResult(String packageName, boolean success);
    void onStartDuration(String packageName, long millis);
}
