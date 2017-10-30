package dotest.module.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.doext.module.activity.R;

import core.DoServiceContainer;
import core.helper.DoIOHelper;
import core.interfaces.DoActivityListener;
import core.interfaces.DoActivityResultListener;
import core.interfaces.DoBaseActivityListener;
import core.interfaces.DoIPage;
import core.interfaces.DoIPageView;
import core.interfaces.DoKeyCodeListener;
import core.interfaces.DoNewIntentListener;
import core.object.DoModule;
import dotest.module.frame.debug.DoApp;
import dotest.module.frame.debug.DoPage;
import dotest.module.frame.debug.DoPageViewFactory;
import dotest.module.frame.debug.DoService;
import dotest.module.frame.debug.DoUtils;

/**
 * 测试扩展组件Activity需继承此类，并重写相应测试方法；
 */
public class DoTestActivity extends Activity implements DoIPageView {

	protected DoModule model;
	private ArrayList<DoActivityResultListener> activityResultListeners = new ArrayList<DoActivityResultListener>();
	private ArrayList<DoBaseActivityListener> baseActivityListeners = new ArrayList<DoBaseActivityListener>();
	private ArrayList<DoNewIntentListener> newIntentListeners = new ArrayList<DoNewIntentListener>();
	private ArrayList<DoKeyCodeListener> keyCodeListeners = new ArrayList<DoKeyCodeListener>();
	protected DoPage currentPage;
	protected DoApp currentApp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.deviceone_test);
		currentApp = new DoApp(this);
		currentPage = new DoPage(currentApp, this);
		DoService.Init(this, currentApp, currentPage);
		DoPageViewFactory doPageViewFactory = (DoPageViewFactory) DoServiceContainer.getPageViewFactory();
		doPageViewFactory.setApplicationContext(this.getApplication());
		doPageViewFactory.setCurrentActivity(this);
		try {
			initModuleModel();
			initUIView();
			initData();
		} catch (Exception e) {
			e.printStackTrace();
		}
		onEvent();
	}

	/**
	 * 初始化UIView，扩展组件是UIModule类型需要重写此方法；
	 */
	protected void initUIView() throws Exception {

	}

	/**
	 * 初始化Model对象
	 */
	protected void initModuleModel() throws Exception {

	}

	/**
	 * 测试属性
	 * 
	 * @param view
	 */
	public void doTestProperties(View view) {

	}

	/**
	 * 测试（同步/异步）方法
	 * 
	 * @param view
	 */
	public void doTestMethod(View view) {
		doTestSyncMethod();
		doTestAsyncMethod();
	}

	/**
	 * 测试同步方法
	 */
	protected void doTestSyncMethod() {

	}

	/**
	 * 测试异步方法
	 */
	protected void doTestAsyncMethod() {

	}

	/**
	 * 测试Module订阅事件消息
	 */
	protected void onEvent() {

	}

	/**
	 * 测试模拟触发一个Module消息事件
	 * 
	 * @param view
	 */
	public void doTestFireEvent(View view) {

	}

	private void initData() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				AssetManager assetManager = getAssets();
				String[] files = null;
				try {
					files = assetManager.list("");
					for (String fileName : files) {
						InputStream in = assetManager.open(fileName);
						copyFile(DoUtils.getDataRootPath(DoTestActivity.this, currentApp.getAppID()), fileName, in);
					}
				} catch (Exception e) {
					Log.e("tag", e.getMessage());
				}
			}
		}).start();
	}

	private static void copyFile(String fileToPath, String fileName, InputStream in) throws Exception {
		OutputStream out = null;
		try {
			DoIOHelper.createDirectory(fileToPath);

			out = new FileOutputStream(fileToPath + File.separator + fileName);
			byte[] buffer = new byte[1024];
			while (true) {
				int ins = in.read(buffer);
				if (ins == -1) {
					break;
				}
				out.write(buffer, 0, ins);
			}
		} finally {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.flush();
				out.close();
			}
		}
	}

	@Override
	public void disposeView() {

	}

	@Override
	public DoIPage getPageModel() {
		return this.currentPage;
	}

	@Override
	public void onNewIntent(Intent intent) {
		setIntent(intent);
		for (int i = 0; i < this.newIntentListeners.size(); i++) {
			this.newIntentListeners.get(i).onNewIntent(intent);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		for (int i = 0; i < this.baseActivityListeners.size(); i++) {
			this.baseActivityListeners.get(i).onResume();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		for (int i = 0; i < this.baseActivityListeners.size(); i++) {
			this.baseActivityListeners.get(i).onPause();
		}
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		for (int i = 0; i < this.baseActivityListeners.size(); i++) {
			this.baseActivityListeners.get(i).onRestart();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		for (int i = 0; i < this.baseActivityListeners.size(); i++) {
			this.baseActivityListeners.get(i).onStop();
		}
	}

	@Override
	public void setKeyCodeListener(DoKeyCodeListener _listener) {

	}

	@Override
	public void registActivityListener(DoActivityListener activityListener) {
		if (activityListener instanceof DoActivityResultListener) {
			this.activityResultListeners.add((DoActivityResultListener) activityListener);
		}
		if (activityListener instanceof DoBaseActivityListener) {
			this.baseActivityListeners.add((DoBaseActivityListener) activityListener);
		}
		if (activityListener instanceof DoKeyCodeListener) {
			this.keyCodeListeners.add((DoKeyCodeListener) activityListener);
		}
		if (activityListener instanceof DoNewIntentListener) {
			this.newIntentListeners.add((DoNewIntentListener) activityListener);
		}
	}

	@Override
	public void unregistActivityListener(DoActivityListener activityListener) {
		if (activityListener instanceof DoActivityResultListener) {
			this.activityResultListeners.remove(activityListener);
		}
		if (activityListener instanceof DoBaseActivityListener) {
			this.baseActivityListeners.remove(activityListener);
		}
		if (activityListener instanceof DoKeyCodeListener) {
			this.keyCodeListeners.remove(activityListener);
		}
		if (activityListener instanceof DoNewIntentListener) {
			this.newIntentListeners.remove(activityListener);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		for (int i = 0; i < this.keyCodeListeners.size(); i++) {
			this.keyCodeListeners.get(i).keyDown(keyCode, event);
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	@Deprecated
	public void setBaseActivityListener(DoBaseActivityListener _baseActivityListener) {

	}

	@Override
	@Deprecated
	public void unregistActivityResultListener(DoActivityResultListener activityResultListener) {

	}

	@Override
	@Deprecated
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		for (int i = 0; i < this.activityResultListeners.size(); i++) {
			this.activityResultListeners.get(i).onActivityResult(requestCode, resultCode, intent);
		}
	}

	@Override
	@Deprecated
	public void registActivityResultListener(DoActivityResultListener activityResultListener) {
		this.activityResultListeners.add(activityResultListener);
	}
}
