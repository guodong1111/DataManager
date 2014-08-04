package tw.guodong.tools;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

class GetDataFromDatabase extends AsyncTask<String, String, byte[]>{
	private Context mContext;
	private OnGetDataFromDatabaseListener mGetDataListener;
	private ProgressBar mProgressBar;
	public GetDataFromDatabase(Context context){
		super();
		this.mContext=context;
	}
	public GetDataFromDatabase(Context context,ProgressBar progressBar){
		this.mContext=context;
		mProgressBar=progressBar;
	}
	protected byte[] doInBackground(String... mLocation) {
		DataBaseHelper dataBaseHelper=new DataBaseHelper(mContext);
		byte[] data=dataBaseHelper.findData(mLocation);
		dataBaseHelper.close();
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
		setProgressBarGONE();
	}
	protected void onPreExecute() {
		setProgressBarVISIBLE();
	}
	protected void setOnGetDataFromDatabaseListener(OnGetDataFromDatabaseListener onGetDataFromDatabaseListener){
		mGetDataListener=onGetDataFromDatabaseListener;
	}
	protected interface OnGetDataFromDatabaseListener {
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