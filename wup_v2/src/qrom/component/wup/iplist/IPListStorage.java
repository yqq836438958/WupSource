package qrom.component.wup.iplist;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Locale;

import org.json.JSONObject;

import qrom.component.log.QRomLog;
import qrom.component.wup.base.RunEnvType;
import qrom.component.wup.base.utils.FileUtil;
import qrom.component.wup.core.PathService;
import qrom.component.wup.iplist.node.IPRootNode;
import qrom.component.wup.threads.StorageThread;

/**
 *  IPList的后端存储
 * @author wileywang
 *
 */
public class IPListStorage {
	private static final String TAG = IPListStorage.class.getSimpleName();
	
	private IPRootNode mRootNode;
	private final File mStorageFile;
	
	public IPListStorage(RunEnvType runEnvType) {
		mRootNode = new IPRootNode();
		mStorageFile = new File(PathService.getFilesDir(), "/wup/iplist_" 
				+ runEnvType.name().toLowerCase(Locale.getDefault()));
		
		if (mStorageFile.exists()) {
			readFromFile();
		}
	}
	
	public IPRootNode getIPRootNode() {
		return mRootNode;
	}
	
	public void edit() {
	}
	
	public void commit() {
		JSONObject rootObject = new JSONObject();
		try {
			mRootNode.toJSON(rootObject);
			writeToFile(rootObject);
		} catch (Throwable e) {
			QRomLog.e(TAG, e.getMessage(), e);
		}
	}
	
	private void readFromFile() {
		try {
			mRootNode.fromJSON(new JSONObject(new String(FileUtil.readFile(mStorageFile), "UTF-8")));
		} catch (Throwable e) {
			QRomLog.e(TAG, e.getMessage(), e);
		} 
	}
	
	private void writeToFile(final JSONObject jsonObject) {
		if (StorageThread.get() == Thread.currentThread()) {
			FileUtil.writeFile(mStorageFile, jsonObject.toString().getBytes(Charset.forName("UTF-8")));
		} else {
			StorageThread.get().getHandler().post(new Runnable() {

				@Override
				public void run() {
					FileUtil.writeFile(mStorageFile, jsonObject.toString().getBytes(Charset.forName("UTF-8")));
				}
			});
		}
	}
	
}
