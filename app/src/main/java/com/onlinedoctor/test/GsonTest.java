package com.onlinedoctor.test;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.onlinedoctor.util.JsonUtil;

public class GsonTest {

	public static void main(String[] args) {
		Timestamp sTimestamp = new Timestamp(System.currentTimeMillis());
		OBJ obj1 = new OBJ(1, 2, null, System.currentTimeMillis(), sTimestamp);
		OBJ obj2 = new OBJ(1, 2, obj1, System.currentTimeMillis(), sTimestamp);
		String json = JsonUtil.objectToJson(obj2);
		System.out.println("json: " + json);

		json = JsonUtil.objectToJsonDateSerializer(obj2,
				"yyyy年MM月dd日 HH时mm分ss秒");
		System.out.println("json: " + json);

		String a = String.valueOf(JsonUtil.getJsonValue(json, "a"));
		System.out.println(a);

		String timestamp1 = (String) JsonUtil.getJsonValue(json, "timestamp2");
		System.out.println(timestamp1);

		// GSON 默认将int long转换成double类型
		Double timestamp2 = (Double) JsonUtil.getJsonValue(json, "timeStamp");
		System.out.println(timestamp2);

		// 先将GSON转换成 对象，则使用对象的原类型
		OBJ obj = (OBJ) JsonUtil.jsonToBean(json, OBJ.class);
		System.out.println(obj.getA());
		System.out.println(obj.getTimeStamp());
		System.out.println(obj.getTimestamp2());

		// List to Json, Json to List
		List<OBJ> objList = new ArrayList<GsonTest.OBJ>();
		objList.add(obj2);
		objList.add(obj1);
		String listJson = JsonUtil.objectToJson(objList);
		System.out.println("listJson: " + listJson);
		// 该方法有点问题
		// List<OBJ> list = (List<OBJ>) JsonUtil.jsonToList(listJson);
		// System.out.println(list.get(0).getTimeStamp());

		List<Map<String, Object>> map = JsonUtil.jsonToListMap(listJson);
		System.out.println(map.toString());
	}

	public static class OBJ {
		private int a;
		private int b;
		private OBJ obj;
		private long timeStamp;
		private Timestamp timestamp2;

		public OBJ(int a, int b, OBJ obj, long timeStamp, Timestamp timestamp2) {
			this.a = a;
			this.b = b;
			this.obj = obj;
			this.timeStamp = timeStamp;
			this.timestamp2 = timestamp2;
		}

		public int getA() {
			return a;
		}

		public void setA(int a) {
			this.a = a;
		}

		public int getB() {
			return b;
		}

		public void setB(int b) {
			this.b = b;
		}

		public OBJ getObj() {
			return obj;
		}

		public void setObj(OBJ obj) {
			this.obj = obj;
		}

		public long getTimeStamp() {
			return timeStamp;
		}

		public void setTimeStamp(long timeStamp) {
			this.timeStamp = timeStamp;
		}

		public Timestamp getTimestamp2() {
			return timestamp2;
		}

		public void setTimestamp2(Timestamp timestamp2) {
			this.timestamp2 = timestamp2;
		}
	}
}
