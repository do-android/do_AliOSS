package doext.define;

import org.json.JSONObject;

import core.interfaces.DoIScriptEngine;

/**
 * 声明自定义扩展组件方法
 */
public interface do_AliOSS_IMethod {
	void simpleDownload(JSONObject _dictParas,DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception ;
	void simpleUpload(JSONObject _dictParas,DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception ;
}