package qrom.component.wup.base.utils;

/**
 *  值内存缓存工具类
 *  
 *     适用于获取一次之后，可以永久缓存在内存中的情况
 * @author wileywang
 *
 */
public abstract class ValueCacheHolder<V> {
	
	protected V mValue;
	
	public V getValue() {
		if (!isValueValid()) {
			synchronized(this) {
				if (!isValueValid()) {
					mValue = buildValue();
				}
			}
		}
		return mValue;
	}
	
	public void clear() {
		mValue = null;
	}
	
	protected abstract V buildValue();
	
	protected boolean isValueValid() {
		return mValue != null;
	}
}
