package tw.guodong.tools.datamanager;

import java.util.Arrays;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataBaseHelper extends SQLiteOpenHelper{
	private final static int  	DBVersion = 1;
	private final static String DBName = "DataManagerDataBase.db";
	private final static String TableName = "DataManagerDataBase";
	private final static String DataLocation="DataLocation";
	private final static String Data="Data";
	private final static String CreateDate="CreateDate";
	private Context context;
	public DataBaseHelper(Context context) {
		super(context, DBName, null, DBVersion);
		this.context=context;
	}
	public void onCreate(SQLiteDatabase db) {  
		String CREATE_PRODUCTS_TABLE = "CREATE TABLE IF NOT EXISTS " +  
				TableName + "("  + 
          DataLocation  + " TEXT," + 
          Data  + " REAL," + 
          CreateDate  + " INTEGER" + ")";  
		db.execSQL(CREATE_PRODUCTS_TABLE);
	}  
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {  
		db.execSQL("DROP TABLE IF EXISTS " + TableName);         
		onCreate(db);  
	}  
	@SuppressLint("SimpleDateFormat")
	public synchronized boolean addData(byte[] data,String... dataLocation) {
		byte[] data_=findData(dataLocation); 
		if(context==null)return false;
	    SQLiteDatabase db = this.getWritableDatabase();
		boolean isNewData=false;;
	    try{
			if(data_==null){
				ContentValues values = new ContentValues();
			    values.put(DataLocation, dataLocation[0]);
			    values.put(Data, data);
			    values.put(CreateDate, (new Date().getTime())/(24*60*60*1000));
			    db.insert(TableName, null, values);
			    isNewData=true;
			}else if((data_.length!=data.length)||!Arrays.equals(data_, data)){
				updateData(data,dataLocation);
			    isNewData=true;
			}
	    }catch(Exception e){
		    db.close();  
	    }
		return isNewData;
	}  
	public void updateData(byte[] data,String... dataLocation) { 
		if(context==null)return ;
		SQLiteDatabase db = this.getWritableDatabase();
	    try{
			ContentValues values = new ContentValues();
			values.put(DataLocation, dataLocation[0]);
			values.put(Data, data);
			values.put(CreateDate, (new Date().getTime())/(24*60*60*1000));
			db.update(TableName, values, DataLocation+" = '"+dataLocation[0]+"'",null);
	    }catch(Exception e){
	    }finally{
	    	db.close();
	    }
	}  
	public byte[] findData(String... dataLocation) {  
		if(context==null)return null;
		byte[] date = null; 
		SQLiteDatabase db = null;
		Cursor cursor = null;
	    try{
		    db = this.getWritableDatabase();
		    String query = "Select "+Data+" FROM " + TableName + " WHERE " + DataLocation + " = \"" + dataLocation[0] + "\"";   
		    cursor = db.rawQuery(query, null);  
		    if(cursor.moveToFirst()){  
		    	date = cursor.getBlob(0);
		    }
	    }catch(Exception e){
	    }finally{
	    	try{
		    	cursor.close(); 
			    db.close();  
	    	}catch(NullPointerException e){
	    	}
	    }
	    return date;  
	}
    public int deleteData(int dataLiveCycle) {  
		if(context==null)return 0;
        int deleteDataCount=0;
        SQLiteDatabase db = this.getWritableDatabase();
	    try{  
	        deleteDataCount=db.delete(TableName, CreateDate +" <= ?", new String[]{String.valueOf(((new Date().getTime())/(24*60*60*1000))-dataLiveCycle)});
		    Log.i("DataManager",deleteDataCount+"");
	    }catch(Exception e){
	    }finally{
	        db.close();  
	    }
        return deleteDataCount;
    }  
}
