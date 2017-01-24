package qrom.component.wup.sync.security.util;

import java.security.Key;
import java.security.PublicKey;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

public class CipherKeyUtil {
    
    private static final String PREFERENCE_NAME = "ciperkeyutil_preferences";
    private static final boolean DEBUG_ENABLE_STORAGE = true;

    public static PublicKey loadPublicKey(Context context, String name) {
        if (context == null || TextUtils.isEmpty(name))
            return null;
        
        String keyString = getSharedPreferences(context).getString(name, "");
        if (TextUtils.isEmpty(keyString))
            return null;
        
        PublicKey result = null;
        try {
            result = RSAHelper.getPublicKey(keyString);
        } catch (Exception e) {
            e.printStackTrace();
            
        }
        return result;
    }
    
    public static boolean storeKey(Context context, String name, Key key) {
        if (context == null || TextUtils.isEmpty(name) || key == null)
            return false;
        
        if (!DEBUG_ENABLE_STORAGE)
            return false;
        
        boolean result = false;
        try {
            String keyString = RSAHelper.getKeyString(key);
            result = getSharedPreferences(context).edit().putString(name, keyString).commit();
        } catch (Exception e) {
            e.printStackTrace();
        } 
        return result;
    }
    
    public static int loadKeyVersion(Context context, String name) {
        if (context == null || TextUtils.isEmpty(name))
            return 0;
        return getSharedPreferences(context).getInt(name, 0);
    }
    
    public static boolean storeKeyVersion(Context context, String name, int version) {
        if (context == null || TextUtils.isEmpty(name) || version <= 0)
            return false;
        if (!DEBUG_ENABLE_STORAGE)
            return false;
        return getSharedPreferences(context).edit().putInt(name, version).commit();
    }
    
    public static boolean isKeyExist(Context context, String name) {
        if (context == null || TextUtils.isEmpty(name))
            return false;
        return !TextUtils.isEmpty(getSharedPreferences(context).getString(name, ""));
    }
    
    public static void removeKey(Context context, String key) {
        if (context == null || TextUtils.isEmpty(key))
            return;
        getSharedPreferences(context).edit().remove(key).commit();
    }
    
    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
    }
}
