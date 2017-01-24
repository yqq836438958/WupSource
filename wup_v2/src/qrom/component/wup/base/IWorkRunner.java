package qrom.component.wup.base;


/**
 * 任务的执行器
 * @author wileywang
 *
 */
public interface IWorkRunner {
	public Thread getThread();
	
	public void postWork(Runnable runnable);
	public void postWorkDelayed(Runnable runnable, long timeDelayed);
}
