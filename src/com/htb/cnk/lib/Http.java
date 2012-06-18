package com.htb.cnk.lib;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.util.Log;

import com.htb.constant.ErrorNum;
import com.htb.constant.Server;

/**
 * @author josh
 * 
 */
public class Http {
	private static int mErrno;
	
	private static String httpRequestPost(String page, String msg) {
		HttpParams httpParameters1 = new BasicHttpParams();

		// 超时设置
		HttpConnectionParams.setConnectionTimeout(httpParameters1, 20 * 1000);
		HttpConnectionParams.setSoTimeout(httpParameters1, 20 * 1000);

		List<NameValuePair> ls = constructHttpPkg(msg);

		String url = Server.SERVER_DOMIN + "/" + page;

		DefaultHttpClient client;
		client = new DefaultHttpClient(httpParameters1);
		
		if ("".equals(url)) {
			mErrno = ErrorNum.HTTP_NO_CONECTION;
			return null;
		}

		HttpPost post = new HttpPost(url);
		post.setHeader("User-Agent", "Mozilla/4.5");
		post.setHeader("connection", "Keep-Alive");
		post.setHeader("Accept-Language", "zh-cn,zh;q=0.5");

		try {
			post.setEntity(new UrlEncodedFormEntity(ls, HTTP.UTF_8));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
			mErrno = ErrorNum.UTF8_NOT_SUPPORTED;
			return null;
		}

		HttpResponse httpResponse = null;
		try {
			httpResponse = client.execute(post);
		} catch (IOException e) {
			e.printStackTrace();
			mErrno = ErrorNum.HTTP_NO_CONECTION;
			return null;
		}

		int statusCode = httpResponse.getStatusLine().getStatusCode();
		if (statusCode == 200) {
			try {
				return EntityUtils.toString(httpResponse.getEntity());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		} else {
		    	mErrno = -statusCode;
				return null;
		}
	}

	private static String handleHttpResponse(HttpResponse httpResponse) {
		HttpEntity httpEntity = httpResponse.getEntity();
		
		int length = (int) httpEntity.getContentLength();
		if (length <= 0) {
			length = 50;
		}
		
		StringBuffer stringBuffer = new StringBuffer(length);
		try {
			InputStreamReader inputStreamReader = new InputStreamReader(
					httpEntity.getContent(), HTTP.UTF_8);
			char buffer[] = new char[length];
			int count;
			while ((count = inputStreamReader.read(buffer, 0, length)) > 0) {
				stringBuffer.append(buffer, 0, count);
			}
			inputStreamReader.close();
		} catch (UnsupportedEncodingException e) {
			Log.e("inputseerteerewrdfa==", e.getMessage());
			mErrno = ErrorNum.UTF8_NOT_SUPPORTED;
			return null;
		} catch (IllegalStateException e) {
			Log.e("inputseerteerewrdfa==", e.getMessage());
		} catch (IOException e) {
			Log.e("inputseerteerewrdfa==", e.getMessage());
			mErrno = ErrorNum.HTTP_NO_CONECTION;
			return null;
		}
		Log.d("respond pkg", stringBuffer.toString());
		return stringBuffer.toString();
	}
		
	private static String httpRequestGet(String page, String msg) {
		/*声明网址字符串*/
		String uriAPI = Server.SERVER_DOMIN + "/" + page;
		/*建立HTTP Get联机*/
		HttpGet httpRequest = new HttpGet(uriAPI); 
		
		try 
		{
			HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest); 
		
			if(httpResponse.getStatusLine().getStatusCode() == 200) { 
				return EntityUtils.toString(httpResponse.getEntity());
			} else {
				return null;
			} 
		} 
		catch (ClientProtocolException e) {
			e.printStackTrace();
			return null;
		} 
		catch (IOException e) {
			e.printStackTrace();
			return null;
		} 
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}  
	}


	private static List<NameValuePair> constructHttpPkg(String msg) {
		Map<String, String> params = new HashMap<String, String>();
		String param = "";
		
		try {
			param = new String(msg.getBytes(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		params.put("value", param);

		List<NameValuePair> ls = new ArrayList<NameValuePair>();
		Set<String> keys = params.keySet();
		for (String key : keys) {
			ls.add(new BasicNameValuePair(key, params.get(key).toString()));
		}
		return ls;
	}
}
