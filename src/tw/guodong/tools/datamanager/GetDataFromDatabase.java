package tw.guodong.tools.datamanager;

import android.content.Context;
import android.os.AsyncTask;

class GetDataFromDatabase extends AsyncTask<String, String, byte[]>{
	private Context mContext;
	private OnGetDataFromDatabaseListener mGetDataListener;
	private WorkerProgressBar mProgressBar;
	public GetDataFromDatabase(Context context){
		super();
		this.mContext=context;
	}
	public GetDataFromDatabase(Context context,WorkerProgressBar progressBar){
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
        mContext = null;
        mGetDataListener = null;
    }
}