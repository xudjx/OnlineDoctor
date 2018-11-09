package com.onlinedoctor.net;


import android.content.Context;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;


/**
 * HttpClient for HTTP Request, this is single instance
 */
public class HttpClientUtil {

	private static final int RequestTimeout = 10000;
	private static final int ConnectTimeout = 15000;

	private HttpClientUtil() {
		super();
	}

	public static HttpClient getHttpClient() {
		HttpClient chttpClient = new DefaultHttpClient();
		HttpParams params = chttpClient.getParams();
		HttpConnectionParams.setConnectionTimeout(params, RequestTimeout);
		HttpConnectionParams.setSoTimeout(params, ConnectTimeout);

		return chttpClient;
	}

	public static HttpClient getHttpsClient(Context context){
		BasicHttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, RequestTimeout);
		HttpConnectionParams.setSoTimeout(params, ConnectTimeout);
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, HTTP.DEFAULT_CONTENT_CHARSET);
		HttpProtocolParams.setUseExpectContinue(params, true);

		SchemeRegistry schReg = new SchemeRegistry();
		schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		schReg.register(new Scheme("https", CustomerSocketFactory.getSocketFactory(context), 443));

		ClientConnectionManager connMgr = new ThreadSafeClientConnManager(params, schReg);

		return new DefaultHttpClient(connMgr, params);
	}
}
