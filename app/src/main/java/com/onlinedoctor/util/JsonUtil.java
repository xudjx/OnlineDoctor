package com.onlinedoctor.util;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.onlinedoctor.pojo.chats.PatientMessageDTO;

/**
 * Gson类库的封装工具类，专门负责解析json数据</br> 内部实现了Gson对象的单例
 */
public class JsonUtil {

	private static Gson gson = null;

	static {
		if (gson == null) {
			gson = new Gson();
		}
	}
	
	private JsonUtil() {

	}

	public static Gson getInstance() {
		return gson==null?gson=new Gson():gson;
	}
	
	/**
	 * 将对象转换成json格式
	 * 
	 * @param ts
	 * @return
	 */
	public static String objectToJson(Object ts) {
		String jsonStr = null;
		if (gson != null) {
			jsonStr = gson.toJson(ts);
		}
		return jsonStr;
	}

	/**
	 * 将对象转换成json格式(并自定义日期格式)
	 * 
	 * @param ts
	 * @return
	 */
	public static String objectToJsonDateSerializer(Object ts, final String dateformat) {
		String jsonStr = null;
		gson = new GsonBuilder().registerTypeHierarchyAdapter(Date.class, new JsonSerializer<Date>() {
			@Override
			public JsonElement serialize(Date date, Type typeOfDate, JsonSerializationContext arg2) {
				SimpleDateFormat format = new SimpleDateFormat(dateformat);
				return new JsonPrimitive(format.format(date));
			}
		}).setDateFormat(dateformat).create();
		if (gson != null) {
			jsonStr = gson.toJson(ts);
		}
		return jsonStr;
	}

	/**
	 * 将json格式转换成list对象
	 * 
	 * @param jsonStr
	 * @return
	 */
	public static List<?> jsonToList(String jsonStr) {
		List<?> objList = null;
		if (gson != null) {
			java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<List<?>>() {
			}.getType();
			objList = gson.fromJson(jsonStr, type);
		}
		return objList;
	}

	/**
	 * 将json格式转换成list对象，并准确指定类型
	 * 
	 * @param jsonStr
	 * @param type
	 * @return
	 */
	public static List<?> jsonToList(String jsonStr, java.lang.reflect.Type type) {
		List<?> objList = null;
		if (gson != null) {
			objList = gson.fromJson(jsonStr, type);
		}
		return objList;
	}

	/**
	 * 将json格式转换成map对象
	 * 
	 * @param jsonStr
	 * @return
	 */
	public static Map<?, ?> jsonToMap(String jsonStr) {
		Map<?, ?> objMap = null;
		if (gson != null) {
			java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<Map<?, ?>>() {
			}.getType();
			objMap = gson.fromJson(jsonStr, type);
		}
		return objMap;
	}

	public static Map<String, String> jsonToMapString(String jsonStr) {
		Map<String, String> objMap = null;
		if (gson != null) {
			java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<Map<String, String>>() {
			}.getType();
			objMap = gson.fromJson(jsonStr, type);
		}
		return objMap;
	}

	/**
	 * 将json转换成bean对象
	 * 
	 * @param jsonStr
	 * @return
	 */
	public static Object jsonToBean(String jsonStr, Class<?> cl) {
		Object obj = null;
		if (gson != null) {
			obj = gson.fromJson(jsonStr, cl);
		}
		return obj;
	}

	/**
	 * 根据key 提取数据返回的可能是String，也可能是Map<?,?>，使用第一层数据
	 * 
	 * @param jsonStr
	 * @param key
	 * @return
	 */
	public static Object getJsonValue(String jsonStr, String key) {
		Object rulsObj = null;
		Map<?, ?> rulsMap = jsonToMap(jsonStr);
		if (rulsMap != null && rulsMap.size() > 0) {
			rulsObj = rulsMap.get(key);
		}
		return rulsObj;
	}

	/**
	 * 根据key 提取数据，使用第一层数据
	 * 
	 * @param jsonStr
	 * @param key
	 * @return String
	 */
	public static Object getJsonStringByKey(String jsonStr, String key) {
		Object rulsObj = null;
		Map<?, ?> rulsMap = jsonToMap(jsonStr);
		if (rulsMap != null && rulsMap.size() > 0) {
			rulsObj = rulsMap.get(key);
		}
		if (rulsObj instanceof Map<?, ?>) {
			return objectToJson(rulsObj);
		}
		return rulsObj;
	}

	/**
	 * 用JsonObject方法提取字符串
	 */
	public static String getJsonStrByKey(String jsonStr, String key) {
		String jsonResult = null;
		try {
			JSONObject jObject = new JSONObject(jsonStr);
			jsonResult = jObject.getString(key);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonResult;
	}

	public static List<Map<String, Object>> jsonToListMap(String jsonString) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		try {
			Gson gson = new Gson();
			list = gson.fromJson(jsonString, new TypeToken<List<Map<String, Object>>>() {
			}.getType());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	public static List<PatientMessageDTO> jsonToPackMessageList(String jsonStr) {
		Gson gson = new Gson();
		List<PatientMessageDTO> objList = null;
		if (gson != null) {
			java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<PatientMessageDTO>() {
			}.getType();
			objList = gson.fromJson(jsonStr, type);
		}
		return objList;
	}
	
	  public static Map<String, String> toMap(Object javaBean) 
	    { 
	        Map<String, String> result = new HashMap<String, String>(); 
	        Method[] methods = javaBean.getClass().getDeclaredMethods(); 

	        for (Method method : methods) 
	        { 
	            try 
	            { 
	                if (method.getName().startsWith("get")) 
	                { 
	                    String field = method.getName(); 
	                    field = field.substring(field.indexOf("get") + 3); 
	                    field = field.toLowerCase().charAt(0) + field.substring(1); 

	                    Object value = method.invoke(javaBean, (Object[])null); 
	                    result.put(field, null == value ? "" : value.toString()); 
	                } 
	            } 
	            catch (Exception e) 
	            { 
	            } 
	        }
	        return result;
	    }
	  
}