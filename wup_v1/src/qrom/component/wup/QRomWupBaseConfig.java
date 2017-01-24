package qrom.component.wup;

public abstract class QRomWupBaseConfig {

	/**
	 * 获取使用wup sdk 应用的包名（manifest的packageName）
	 * @return
	 */
	public abstract String getAppPackageName();
	
	/**
	 * 是否强制运行在测试环境
	 * @return  true: 强制切换到测试换， 可通过getTestWupProxyAddr() 修改测试环境地址
	 *                 false: 默认正式环境
	 */
	public abstract boolean isRunTestForced();
	
    /**
     * 返回wup proxy 测试环境地址
     * @return null: 用默认测试地址
     */
    public String getTestWupProxyAddr() {
        return null;
    }
    
    /**
     * 返回wup socket proxy 测试环境地址
     * @return null: 用默认测试地址
     */
    public String getTestWupSocketProxyAddr() {
        return null;
    }
    
    /**
     * 是否强制使用调试wup代理地址 <p>
     *    -- 当isRunTestForced() 返回true : 测试环境，优先测试环境，即该接口无效； 
     *    
     * @return  true: 使用getTestWupProxyAddr()返回的代理地址;<p>
     *                false: 按默认iplist逻辑访问代理地址
     */
    public boolean isForcedUsedDebugAddress() {
        return false;
    }
}
