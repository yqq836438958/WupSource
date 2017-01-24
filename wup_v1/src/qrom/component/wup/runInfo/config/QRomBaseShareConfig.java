package qrom.component.wup.runInfo.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import qrom.component.wup.utils.QWupLog;

public class QRomBaseShareConfig {
    
    protected String TAG = "QRomBaseShareConfig";
    protected Map<String, String> mConfigs = new HashMap<String, String>();
    
    protected String mConfiFileName = null;
    
    protected long mLastTime = -1;
    
    public QRomBaseShareConfig(String path, String name) {
        mConfiFileName = path + File.separator+name + ".ini";
    }
    
    protected void loadconfigInfo() {
        loadconfigInfo(false);
    }
    
    
    protected synchronized boolean loadconfigInfo(boolean isForceLoad) {
        
        log("loadconfigInfo -> fileName : " + mConfiFileName + ", isForceLoad = " + isForceLoad);
        
        if (isEmptyStr(mConfiFileName)) {
            log("loadconfigInfo -> fileName is empty!");
            return false;
        }
        
        File configFile = new File(mConfiFileName);
        
        if (!configFile.exists()) {  // 文件不存在
            log("loadconfigInfo -> fileName is not exists!");
            mConfigs.clear();
            return false;
        }
        
        long fileLastModif = configFile.lastModified();
        
        if (!isForceLoad && mLastTime == fileLastModif && !mConfigs.isEmpty()) { // 文件未被修改
            log("loadconfigInfo -> fileName is not changed!");
            return false;
        }
        
        mLastTime = configFile.lastModified();
        FileInputStream inputStream = null;
        
        try {
            // 清除所有信息
            mConfigs.clear();
            
            // 重新加载文件
            inputStream = new FileInputStream(configFile);
            Properties property = new Properties();
            property.load(inputStream);
            
            for (Map.Entry<Object, Object>  entry : property.entrySet()) {
                if (entry == null || entry.getKey() == null || isEmptyStr(entry.getKey().toString()) 
                        || entry.getValue() == null) {
                    continue;
                }
                
                log("load: " + entry.getKey() + " = " + entry.getValue());
                
                mConfigs.put(entry.getKey().toString(), entry.getValue().toString());                
            }
            return true;
            
        } catch (FileNotFoundException e) {
            log("loadconfigInfo -> FileNotFoundException ", e);
        } catch (Exception e) {
            log("loadconfigInfo -> err ", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }
    
    protected synchronized boolean updataConfig() {
        
        File configFile = new File(mConfiFileName);
        FileOutputStream outputStream = null;
        OutputStreamWriter outputStreamWriter = null;
        try {
            if (!configFile.exists()) {  // 文件不存在
                if (configFile.getParentFile() != null && !configFile.getParentFile().exists()) {
                    
                    configFile.mkdirs();
                }
                configFile.createNewFile();
            }
             
            outputStream = new FileOutputStream(configFile);
            outputStreamWriter = new OutputStreamWriter(outputStream);
            for (Entry<String, String> entry : mConfigs.entrySet()) {
                if (entry == null || isEmptyStr(entry.getKey())) {
                    continue;
                }
                log("write:key = value : " + entry.getKey() + " = " + entry.getValue());
                outputStreamWriter.write(entry.getKey());
                outputStreamWriter.write("=");
                outputStreamWriter.write(entry.getValue());
                outputStreamWriter.write("\n");
            }
            outputStreamWriter.flush();
            return true;
        } catch (FileNotFoundException e) {
            log("updataConfig -> FileNotFoundException ", e);
        } catch (Exception e) {
            log("updataConfig -> err ", e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (outputStreamWriter != null) {
                try {
                    outputStreamWriter.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
        return false;
    }
    
    public boolean reLoadConfig() {
        return loadconfigInfo(true);
    }
    
    public String getConfigValue(String key) {
        return mConfigs.get(key);
    }
    
    
    public boolean setConfig(String key, String value) {
        mConfigs.put(key, value);
        
        return updataConfig();
    }
    
    public int getInt(String key, int defaultValue) {
        
        loadconfigInfo();
        
        String value = mConfigs.get(key);
        if (isEmptyStr(value)) {
            return defaultValue;
        }
        int result = defaultValue;
        try {
            result = Integer.valueOf(value.trim());
        } catch (Exception e) {
            log("getInt -> valueOf err", e);
        }
        return result;
    }

    public boolean putInt(String key, int value) {
        
        return setConfig(key, String.valueOf(value));
    }
    
    public long getLong(String key, int defaultValue) {
        loadconfigInfo();
        String value = mConfigs.get(key);
        if (isEmptyStr(value)) {
            return defaultValue;
        }
        long result = defaultValue;
        try {
            result = Long.valueOf(value.trim());
        } catch (Exception e) {
            log("getLong -> valueOf err", e);
        }
        return result;
    }
    
    public boolean putLong(String key, long value) {
        
        return setConfig(key, String.valueOf(value));
    }
    
    public String getString(String key) {
        loadconfigInfo();
        return mConfigs.get(key);
    }
    
    public boolean putString(String key, String value) {
        
        return setConfig(key, value);
    }

    private boolean isEmptyStr(String str) {
        return str == null || "".equals(str);
    }
    
    protected void log(String str) {
        QWupLog.d(TAG, str);
    }
    
    protected void log(String str, Exception e) {
        QWupLog.w(TAG, str +", exception: " + e +  ", err msg: " + e.getMessage());
    }
}
