package tw.guodong.tools;

import java.io.IOException;
import java.util.Locale;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

class GetDataFromInternet extends AsyncTask<String, Integer, byte[]>{
	private OnGetDataFromInternetListener mGetDataListener;
	private ProgressBar mProgressBar;
	private Header header;
	//private int size;
	public GetDataFromInternet(){
		super();
	}
	public GetDataFromInternet(ProgressBar progressBar){
		mProgressBar=progressBar;
	}
	public void setHeader(Header header){
		this.header=header;
	}
	protected byte[] doInBackground(String... url) {
		byte[] data =null;
		HttpEntity entity = null;
		try {
			entity = requestInputStream(url[0]);
			if(entity !=null){
				data = fileWrite(entity); 
			}
		}catch (Exception e) {
			Log.e("DataManager", e.toString());
		}
		return data;
	}
	
	private HttpEntity requestInputStream(String url) throws ClientProtocolException, IOException {  
		HttpEntity httpEntity = null;  
        HttpGet httpGet = new HttpGet(url);
	    if(header!=null){
	    	httpGet.setHeader(header);
	    }
	    String acceptLanguage = Locale.getDefault().getLanguage() + "-" + Locale.getDefault().getCountry();
	    httpGet.setHeader(new BasicHeader("Accept-Language",acceptLanguage));
        HttpClient httpClient = new DefaultHttpClient();  
        HttpResponse httpResponse = httpClient.execute(httpGet);  
        int httpStatusCode = httpResponse.getStatusLine().getStatusCode(); 
        if(httpStatusCode == HttpStatus.SC_OK) {  
            httpEntity = httpResponse.getEntity();  
            //size = (int) httpEntity.getContentLength();  
        }  
        return httpEntity;  
    }  
	
	private byte[] fileWrite(HttpEntity httpEntity) throws IOException {  
        byte[] data = null;
	    /*InputStream in = httpEntity.getContent();
	    try{
	    	byte[] buffer = new byte[1024];  
	    	int progress = 0;  
	        int readBytes = 0;  
	        ByteArrayBuffer byteArray = new ByteArrayBuffer(size);
		    while ((readBytes = in.read(buffer)) != -1) {  
		    	byteArray.append(buffer, progress, readBytes);
		        progress += readBytes;   
		        publishProgress(progress);  
		    }  
			data = byteArray.toByteArray();
	    }catch(Exception e){
		}finally{
			in.close();
		}*/
    	data = EntityUtils.toByteArray(httpEntity);
    	if(data.length==0){
    		data=null;
    	}
        return data;
    }  
	
	protected void onPostExecute(byte[] data) {
		setProgressBarGONE();
		if(mGetDataListener!=null){
			if(data==null){
				mGetDataListener.onMissConnect();
			}else{
				mGetDataListener.onGetData(data);
			}
		}
		super.onPostExecute(data);
	}
	protected void onCancelled() {
		if(mGetDataListener!=null){
			mGetDataListener.onMissConnect();
		}
		setProgressBarGONE();
	}
	protected void onPreExecute() {
		setProgressBarVISIBLE();
	}
	protected void setOnGetDataFromInternetListener(OnGetDataFromInternetListener onGetDataFromInternetListener){
		mGetDataListener=onGetDataFromInternetListener;
	}
	protected interface OnGetDataFromInternetListener {
        public void onGetData(byte[] data);
        public void onMissConnect();
    }
    public void setProgressBarVISIBLE(){
    	try{
    		mProgressBar.post(new Runnable() {
				public void run() {
					if(mProgressBar!=null){
						mProgressBar.setVisibility(View.VISIBLE);
					}
				}
			});
    	}catch(NullPointerException e){
    	}
    }
    public void setProgressBarGONE(){
    	try{
    		mProgressBar.post(new Runnable() {
				public void run() {
					if(mProgressBar!=null){
						mProgressBar.setVisibility(View.GONE);
					}
				}
			});
    	}catch(NullPointerException e){
    	}
    }
}