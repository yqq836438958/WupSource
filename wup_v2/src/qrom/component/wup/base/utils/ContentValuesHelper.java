package qrom.component.wup.base.utils;

import android.content.ContentValues;

/**
 *  获取ContentValues的值，可以传递默认值，避免空原始对象
 * @author wileywang
 *
 */
public class ContentValuesHelper {
	private ContentValues mContentValues;
	
	public ContentValuesHelper(ContentValues contentValues) {
		if (contentValues == null) {
			throw new IllegalArgumentException("contentValues should not be null");
		}
		
		mContentValues = contentValues;
	}
	
	public boolean getAsBoolean(String key, boolean defaultValue) {
		Boolean value = mContentValues.getAsBoolean(key);
		if (value == null) {
			return defaultValue;
		}
		return value.booleanValue();
	}
	
	public short getAsShort(String key, short defaultValue) {
		Short value = mContentValues.getAsShort(key);
		if (value == null) {
			return defaultValue;
		}
		return value.shortValue();
	}
	
	public int getAsInt(String key, int defaultValue) {
		Integer value = mContentValues.getAsInteger(key);
		if (value == null) {
			return defaultValue;
		}
		return value.intValue();
	}
	
	public long getAsLong(String key, long defaultValue) {
		Long value = mContentValues.getAsLong(key);
		if (value == null) {
			return defaultValue;
		}
		return value.longValue();
	}
	
	public String getAsString(String key, String defaultValue) {
		String value = mContentValues.getAsString(key);
		if (value == null) {
			return defaultValue;
		}
		return value;
	}
	
	public double getAsDouble(String key, double defaultValue) {
		Double value = mContentValues.getAsDouble(key);
		if (value == null) {
			return defaultValue;
		}
		return value.doubleValue();
	}
	
	public float getAsFloat(String key, float defaultValue) {
		Float value = mContentValues.getAsFloat(key);
		if (value == null) {
			return defaultValue;
		}
		return value.floatValue();
	}
	
}
