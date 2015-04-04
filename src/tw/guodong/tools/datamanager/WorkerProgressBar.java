package tw.guodong.tools.datamanager;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;

/**
 * Created by USER on 2015/3/6.
 */
public class WorkerProgressBar extends ProgressBar {
    private int works = 0;
    public WorkerProgressBar(Context context) {
        super(context);
    }

    public WorkerProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WorkerProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void addWork(){
        if(++works > 0){
            post(new Runnable() {
                @Override
                public void run() {
                    setVisibility(View.VISIBLE);
                }
            });
        }
    }

    public void subWork(){
        if(--works <= 0){
            works = 0;
            post(new Runnable() {
                @Override
                public void run() {
                    setVisibility(View.GONE);
                }
            });
        }
    }

    public void clearWorks(){
        works = 0;
        subWork();
    }
}
