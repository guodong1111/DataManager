package tw.guodong.tools.datamanager;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import android.os.AsyncTask;
import android.util.Log;

class GetDataFromInternet extends AsyncTask<String, Integer, byte[]>{
	private OnGetDataFromInternetListener mGetDataListener;
	private WorkerProgressBar mProgressBar;
	private Header header;
	public GetDataFromInternet(){
		super();
	}
	public GetDataFromInternet(WorkerProgressBar progressBar){
		mProgressBar=progressBar;
	}
	public void setHeader(Header header){
		this.header=header;
	}
	protected byte[] doInBackground(String... urls) {
		byte[] data = null;
		HttpURLConnection urlConnection = null;
		try {
			URL url = new URL(urls[0]);
			urlConnection = (HttpURLConnection) url.openConnection();
			String acceptLanguage = Locale.getDefault().getLanguage() + "-" + Locale.getDefault().getCountry();
			urlConnection.setRequestProperty("Accept-Language", acceptLanguage);
			if(null != header){
				urlConnection.setRequestProperty(header.getName(),header.getValue());
			}
			InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
			data = readStream(inputStream);
			if(data.length == 0){
				data=null;
			}
		}catch (Exception e) {
			Log.e("DataManager", e.toString());
		}finally {
			if(null != urlConnection){
				urlConnection.disconnect();
			}
		}
		return data;
	}

	public static byte[] readStream(InputStream inStream) throws Exception {
		ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = -1;
		while ((len = inStream.read(buffer)) != -1) {
			outSteam.write(buffer, 0, len);
		}
		outSteam.close();
		inStream.close();
		return outSteam.toByteArray();
	}

	protected void setProgressBar(WorkerProgressBar workerProgressBar){
        mProgressBar = workerProgressBar;
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
            mProgressBar.addWork();
    	}catch(NullPointerException e){
    	}
    }
    public void setProgressBarGONE(){
    	try{
            mProgressBar.subWork();
    	}catch(NullPointerException e){
    	}
    }
    public void destroy(){
        cancel(true);
        mGetDataListener = null;
    }
}