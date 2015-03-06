package tw.guodong.tools.datamanager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.Executors;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class DataManager {
	private static int mDataLiveCycle=1;
	private Context mContext;
	private String[] mLocation;
	private String[] mLocationId;
	private boolean mOnlyInternet=false,mLocalFirst=true,mOnlyLocationIfNotNull=false,mDataUpToDate=false;
	private GetDataFromDatabase getDataFromDatabase;
	private OnGetDataListener mOnGetDataListener;
	private GetDataFromInternet mGetDataFromInternet;
	private WorkerProgressBar mProgressBar;
	private Header header;
	private int timeOut = -1;
	private boolean loading=false,useLocationId=false;
	public DataManager(Context context){
		mContext=context;
	}
	public DataManager dataOnlyUseInternet(boolean onlyInternet){
		mOnlyInternet=onlyInternet;
		return this;
	}
	public DataManager dataUseLocalFirst(boolean localFirst){
		mLocalFirst=localFirst;
		return this;
	}
	public DataManager dataOnlyUseLocationIfNotNull(boolean onlyLocationIfNotNull){
		mOnlyLocationIfNotNull=onlyLocationIfNotNull;
		return this;
	}
	public DataManager dataUpToDate(boolean dataUpToDate){
		mDataUpToDate=dataUpToDate;
		return this;
	}
	public DataManager setTimeOut(int timeOut){
		this.timeOut=timeOut;
		return this;
	}
	public boolean isLoading(){
		return loading;
	}
	public DataManager setProgressBar(int progressBar){
		if(mContext instanceof Activity){
			mProgressBar=(WorkerProgressBar)((Activity)mContext).findViewById(progressBar);
		}
		return this;
	}
	public DataManager setHeader(String name,String value){
		header = new BasicHeader(name,value);
		return this;
	}
	public DataManager setLocationId(String... locationId){
		mLocationId=locationId;
		useLocationId=true;
		return this;
	}
	public void startConnect(String... location){
		loading=true;
		mLocation=location;
		if(!useLocationId){
			mLocationId=location;
		}
		if(mProgressBar==null){
			getDataFromDatabase=new GetDataFromDatabase(mContext);
			mGetDataFromInternet=new GetDataFromInternet();
		}else{
			getDataFromDatabase=new GetDataFromDatabase(mContext,mProgressBar);
			mGetDataFromInternet=new GetDataFromInternet(mProgressBar);
		}
		if(header!=null){
			mGetDataFromInternet.setHeader(header);
		}
        ThreadManager.getInstance(mContext).postToBackgroungThread(new Runnable() {
            @Override
            public void run() {
                getDateFromDataBase();
            }
        });
	}
	static public int deleteData(Context context,int dataLiveCycle){
        int deleteDataCount=0;
		mDataLiveCycle=dataLiveCycle;
		DataBaseHelper dataBaseHelper=new DataBaseHelper(context);
		deleteDataCount=dataBaseHelper.deleteData(mDataLiveCycle);
		dataBaseHelper.close();
		return deleteDataCount;
	}
	public void destroy(Context context){
		mGetDataFromInternet.cancel(true);
		getDataFromDatabase.cancel(true);
		mGetDataFromInternet=null;
		getDataFromDatabase=null;
		mContext=null;
	}
	private void getDataFromInternet(final boolean dataBaseNoData){
		mGetDataFromInternet.execute(mLocation);
		if(timeOut>=0){
			new Thread(new Runnable() {
				public void run() {
					try {
						Thread.sleep(timeOut);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					mGetDataFromInternet.cancel(true);
				}
			}).start();
		}
		mGetDataFromInternet.setOnGetDataFromInternetListener(new GetDataFromInternet.OnGetDataFromInternetListener(){
			public void onGetData(byte[] data) {
				if(mOnlyInternet){
					returnData(false,data);
					loading=false;
				}else{
					DataBaseHelper dataBaseHelper=new DataBaseHelper(mContext);
					if((dataBaseHelper.addData(data,mLocationId)&&mDataUpToDate)){
						returnData(!dataBaseNoData,data);
						loading=false;
					}else if(!mLocalFirst||dataBaseNoData){
						returnData(false,data);
						loading=false;
					}
					dataBaseHelper.close();
				}
			}
			public void onMissConnect() {
				if(mOnlyInternet||dataBaseNoData){
					returnMissConnect();
					loading=false;
				}else if(mLocalFirst){
					loading=false;
				}else{
					getDataFromDatabase.executeOnExecutor(Executors.newFixedThreadPool(2), mLocationId);
					getDataFromDatabase.setOnGetDataFromDatabaseListener(new GetDataFromDatabase.OnGetDataFromDatabaseListener() {
						public void onGetData(byte[] data) {
							returnData(false,data);
							loading=false;
						}
						public void onMissConnect() {
							returnMissConnect();
							loading=false;
						}
					});
				}
			}
		});
	}
	private void getDateFromDataBase(){
		if(mOnlyInternet||!mLocalFirst){
			getDataFromInternet(false);
		}else{
			loading=true;
			getDataFromDatabase.executeOnExecutor(Executors.newFixedThreadPool(2), mLocationId);
			getDataFromDatabase.setOnGetDataFromDatabaseListener(new GetDataFromDatabase.OnGetDataFromDatabaseListener() {
				public void onGetData(byte[] data) {
					returnData(false,data);
					loading=false;
					if(!mOnlyLocationIfNotNull){
						getDataFromInternet(false);
					}
				}
				public void onMissConnect() {
					getDataFromInternet(true);
				}
			});
		}
	}
	private void returnData(final boolean newData,final byte[] data){
        ThreadManager.getInstance(mContext).postToUIThread(new Runnable() {
            @Override
            public void run() {
                if(mOnGetDataListener!=null){
                    mOnGetDataListener.onGetData(newData,data);
                }
            }
        });
	}
	private void returnMissConnect(){
        ThreadManager.getInstance(mContext).postToUIThread(new Runnable() {
            @Override
            public void run() {
                if(mOnGetDataListener!=null){
                    mOnGetDataListener.onMissConnect();
                }
            }
        });
	}
	public void setOnGetDataListener(OnGetDataListener onGetDataListener){
		mOnGetDataListener=onGetDataListener;
	}
	public interface OnGetDataListener{
        public void onGetData(boolean isNewData,Object data);
        public void onMissConnect();
    }
	public static JSONArray getJSONArray(Object data){
		try {
			return new JSONArray(new String((byte[])data));
		} catch (Exception e) {
			return null;
		}
	}
	public static JSONObject getJSONObject(Object data){
		try {
			return new JSONObject(new String((byte[])data));
		} catch (Exception e) {
			return null;
		}
	}
	public static byte[] getByteArray(Object data){
		try {
			return (byte[])data;
		} catch (Exception e) {
			return null;
		}
	}
	public static Bitmap getImage(Object data,int size){
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize=size;
			return BitmapFactory.decodeByteArray((byte[])data, 0, ((byte[])data).length,options);
		} catch (Exception e) {
			return null;
		}
	}
	public static Bitmap compressImage(Bitmap image,int x) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, x, baos);
        image.recycle();
		return BitmapFactory.decodeStream(new ByteArrayInputStream(baos.toByteArray()), null, null);
	}
	public static String getString(Object data){
		try {
			return new String((byte[])data);
		} catch (Exception e) {
			return null;
		}
	}
}
