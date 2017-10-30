package doext.implement;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.DecimalFormat;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSPlainTextAKSKCredentialProvider;
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectResult;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;

import core.DoServiceContainer;
import core.helper.DoIOHelper;
import core.helper.DoJsonHelper;
import core.interfaces.DoIScriptEngine;
import core.object.DoEventCenter;
import core.object.DoInvokeResult;
import core.object.DoSingletonModule;
import doext.define.do_AliOSS_IMethod;

/**
 * 自定义扩展SM组件Model实现，继承DoSingletonModule抽象类，并实现do_AliOSS_IMethod接口方法；
 * #如何调用组件自定义事件？可以通过如下方法触发事件：
 * this.model.getEventCenter().fireEvent(_messageName, jsonResult);
 * 参数解释：@_messageName字符串事件名称，@jsonResult传递事件参数对象； 获取DoInvokeResult对象方式new
 * DoInvokeResult(this.getUniqueKey());
 */
public class do_AliOSS_Model extends DoSingletonModule implements do_AliOSS_IMethod {

	public do_AliOSS_Model() throws Exception {
		super();
	}

	/**
	 * 同步方法，JS脚本调用该组件对象方法时会被调用，可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V），获取参数值使用API提供DoJsonHelper类；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public boolean invokeSyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		// ...do something
		return super.invokeSyncMethod(_methodName, _dictParas, _scriptEngine, _invokeResult);
	}

	/**
	 * 异步方法（通常都处理些耗时操作，避免UI线程阻塞），JS脚本调用该组件对象方法时会被调用， 可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V），获取参数值使用API提供DoJsonHelper类；
	 * @_scriptEngine 当前page JS上下文环境
	 * @_callbackFuncName 回调函数名 #如何执行异步方法回调？可以通过如下方法：
	 *                    _scriptEngine.callback(_callbackFuncName,
	 *                    _invokeResult);
	 *                    参数解释：@_callbackFuncName回调函数名，@_invokeResult传递回调函数参数对象；
	 *                    获取DoInvokeResult对象方式new
	 *                    DoInvokeResult(this.getUniqueKey());
	 */
	@Override
	public boolean invokeAsyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception {
		if ("simpleDownload".equals(_methodName)) {
			simpleDownload(_dictParas, _scriptEngine, _callbackFuncName);
			return true;
		}
		if ("simpleUpload".equals(_methodName)) {
			simpleUpload(_dictParas, _scriptEngine, _callbackFuncName);
			return true;
		}
		return super.invokeAsyncMethod(_methodName, _dictParas, _scriptEngine, _callbackFuncName);
	}

	/**
	 * 从阿里云下载文件；
	 * 
	 * @throws Exception
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_callbackFuncName 回调函数名
	 */
	@Override
	public void simpleDownload(JSONObject _dictParas, final DoIScriptEngine _scriptEngine, final String _callbackFuncName) throws Exception {
		String _accessKeyId = DoJsonHelper.getString(_dictParas, "accessKeyId", "");
		String _accessKeySecret = DoJsonHelper.getString(_dictParas, "accessKeySecret", "");
		String _endPoint = DoJsonHelper.getString(_dictParas, "endPoint", "");
		final String _fileName = DoJsonHelper.getString(_dictParas, "fileName", "");
		String _path = DoJsonHelper.getString(_dictParas, "path", "");
		String _bucket = DoJsonHelper.getString(_dictParas, "bucket", "");

		final String fileFullPath = DoIOHelper.getLocalFileFullPath(_scriptEngine.getCurrentPage().getCurrentApp(), _path);

		OSS oss = getOssClient(_accessKeyId, _accessKeySecret, _endPoint);
		GetObjectRequest get = new GetObjectRequest(_bucket, _fileName);
		oss.asyncGetObject(get, new OSSCompletedCallback<GetObjectRequest, GetObjectResult>() {

			@Override
			public void onSuccess(GetObjectRequest arg0, GetObjectResult arg1) {
				// 请求成功
				try {
					double contentLength = arg1.getContentLength();
					InputStream inputStream = arg1.getObjectContent();

					int length;
					long lengtsh = 0;
					byte[] buffer = new byte[1024];
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					while ((length = inputStream.read(buffer)) != -1) {
						bos.write(buffer, 0, length);
						lengtsh += length;
						fireProgress("simpleDownloadProgress", contentLength, lengtsh / contentLength);
					}
					bos.close();
					byte[] getData = bos.toByteArray();

					File tempFile = new File(fileFullPath);
					// 父目录是否存在
					if (!tempFile.getParentFile().exists()) {
						// 不存在就建立此目录
						tempFile.getParentFile().mkdir();
					}
					DoIOHelper.createFile(fileFullPath);

					FileOutputStream fos = new FileOutputStream(fileFullPath);
					fos.write(getData);

					if (fos != null) {
						fos.close();
					}
					if (inputStream != null) {
						inputStream.close();
					}
				} catch (Exception e) {
					String aaa = e.getMessage();
				}
				callBack(true, _scriptEngine, _callbackFuncName);
			}

			@Override
			public void onFailure(GetObjectRequest arg0, ClientException arg1, ServiceException arg2) {
				// 请求异常
				if (arg1 != null) {
					// 本地异常如网络异常等
					arg1.printStackTrace();
					DoServiceContainer.getLogEngine().writeError("do_AliOSS simpleDownload", new Exception("本地异常如网络异常等"));
				}
				if (arg1 != null) {
					// 服务异常
					try {
						DoServiceContainer.getLogEngine().writeError("do_AliOSS simpleDownload", getException(arg2));
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				callBack(false, _scriptEngine, _callbackFuncName);
			}
		});
	}

	/**
	 * 上传文件；
	 * 
	 * @throws Exception
	 * 
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_callbackFuncName 回调函数名
	 */
	@Override
	public void simpleUpload(JSONObject _dictParas, final DoIScriptEngine _scriptEngine, final String _callbackFuncName) throws Exception {
		String _accessKeyId = DoJsonHelper.getString(_dictParas, "accessKeyId", "");
		String _accessKeySecret = DoJsonHelper.getString(_dictParas, "accessKeySecret", "");
		String _endPoint = DoJsonHelper.getString(_dictParas, "endPoint", "");
		String _filePath = DoJsonHelper.getString(_dictParas, "filePath", "");
		String _bucket = DoJsonHelper.getString(_dictParas, "bucket", "");
		String _savePath = DoJsonHelper.getString(_dictParas, "savePath", "");

		OSS oss = getOssClient(_accessKeyId, _accessKeySecret, _endPoint);

		String _path = DoIOHelper.getLocalFileFullPath(_scriptEngine.getCurrentPage().getCurrentApp(), _filePath);
		if (TextUtils.isEmpty(_savePath) || _savePath.length() == 0) {
			_savePath = _path.substring(_path.lastIndexOf('/') + 1);
		}

		PutObjectRequest put = new PutObjectRequest(_bucket, _savePath, _path);
		put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
			@Override
			public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
				double percent = currentSize / (double) totalSize;
				fireProgress("simpleUploadProgress", totalSize, percent);
			}
		});

		oss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
			@Override
			public void onSuccess(final PutObjectRequest request, PutObjectResult result) {
				callBack(true, _scriptEngine, _callbackFuncName);
			}

			@Override
			public void onFailure(PutObjectRequest arg0, ClientException arg1, ServiceException arg2) {
				// 请求异常
				if (arg1 != null) {
					// 本地异常如网络异常等
					arg1.printStackTrace();
					DoServiceContainer.getLogEngine().writeError("do_AliOSS simpleUpload", new Exception("本地异常如网络异常等"));
				}
				if (arg2 != null) {
					// 服务异常
					try {
						DoServiceContainer.getLogEngine().writeError("do_AliOSS simpleUpload", getException(arg2));
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				callBack(false, _scriptEngine, _callbackFuncName);
			}
		});
	}

	private void callBack(boolean result, DoIScriptEngine _scriptEngine, String _callbackFuncName) {
		final DoInvokeResult doInvokeResult = new DoInvokeResult(this.getUniqueKey());
		doInvokeResult.setResultBoolean(result);
		_scriptEngine.callback(_callbackFuncName, doInvokeResult);
	}

	private OSS getOssClient(String accessKeyId, String accessKeySecret, String endPoint) {
		Context context = DoServiceContainer.getPageViewFactory().getAppContext();
		OSSCredentialProvider credentialProvider = new OSSPlainTextAKSKCredentialProvider(accessKeyId, accessKeySecret);
		ClientConfiguration conf = new ClientConfiguration();
		OSSLog.enableLog();
		return new OSSClient(context, endPoint, credentialProvider, conf);
	}

	private void fireProgress(String eventName, double fileSzie, double percent) {
		DoInvokeResult _invokeResult = new DoInvokeResult(getUniqueKey());
		JSONObject jsonNode = new JSONObject();
		DecimalFormat df = new DecimalFormat("######0.00");
		try {
			jsonNode.put("fileSize", fileSzie / 1024f);
			jsonNode.put("percent", df.format(percent * 100));// 保留两位小数
			_invokeResult.setResultNode(jsonNode);
		} catch (Exception e) {
			e.printStackTrace();
		}
		fireEvent(eventName, _invokeResult);
	}

	private void fireEvent(String eventName, DoInvokeResult _invokeResult) {
		DoEventCenter eventCenter = getEventCenter();
		if (eventCenter != null) {
			eventCenter.fireEvent(eventName, _invokeResult);
		}
	}

	private Exception getException(ServiceException arg2) throws JSONException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("ErrorCode", arg2.getErrorCode());
		jsonObject.put("RequestId", arg2.getRequestId());
		jsonObject.put("HostId", arg2.getHostId());
		jsonObject.put("RawMessage", arg2.getRawMessage());
		return new Exception(jsonObject.toString());
	}
}