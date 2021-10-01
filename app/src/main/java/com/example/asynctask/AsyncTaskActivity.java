package com.example.asynctask;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class AsyncTaskActivity extends Activity {

    private static final String TAG = "AsyncTaskActivity";
    private static final int P_BAR_MAX = 100;
    Button bStart;
    Button bStop;
    TextView textViewMessage;
    ProgressBar pBar;

    private UpdateTask1 myUpdateTask=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_async_task);

        bStart = (Button) findViewById(R.id.buttonStart);
        bStop = (Button) findViewById(R.id.buttonStop);
        textViewMessage = (TextView) findViewById(R.id.textView2);
        pBar = (ProgressBar) findViewById(R.id.progressBar1);

        //what is the max value
        pBar.setMax(P_BAR_MAX);
    }


    //see https://developer.android.com/guide/components/activities/activity-lifecycle
    //must detach and attach thread in onStop and onStart
    @Override
    protected void onStart() {
        super.onStart();
        //lets see if the device rotated and we need to regrab thread
        //create or get ref to existing singleton, and then get the thread it's holding
        myUpdateTask = (UpdateTask1)Singleton.getInstance().get_thread();

        //if a thread was retained then grab it
        if (myUpdateTask != null) {
            myUpdateTask.attach(this);
            pBar.setProgress(myUpdateTask.progress);
        }

        //set the buttonstate accordingly
        bStart.setEnabled(Singleton.getInstance().get_startState());
        bStop.setEnabled(!Singleton.getInstance().get_startState());
    }

    @Override
    protected void onStop() {
        super.onStop();
        //oh no, we rotated, save the thread
        if (myUpdateTask != null) {
            Log.d(TAG, "onStop");
            myUpdateTask.detach();
            Singleton.getInstance().set_thread(myUpdateTask);
            Singleton.getInstance().set_startState(bStart.isEnabled());
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_async_task, menu);
        return true;
    }

    // make sure only one is enabled at a time===========
    private void setButtonState(boolean state) {
        bStart.setEnabled(!state);
        bStop.setEnabled(state);
    }

    // personal asynctask============

    // start thread
    public void doStart(View v) {
        setButtonState(true);
        textViewMessage.setText("Working...");

        //create and start ansync task
//        myUpdateTask = new UpdateTask(this);
//        myUpdateTask.execute();

        myUpdateTask = new UpdateTask1(this);
        myUpdateTask.start();
    }

    //try to cancel thread
    public void doStop(View v) {
        setButtonState(false);
        textViewMessage.setText("Stopping...");
        pBar.setProgress(0);

        myUpdateTask.cancel(true);
    }

    public void doButton(View view) {
        try {
            Thread.sleep(25000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static class UpdateTask1 extends Thread{
        private static final String TAG = "UpdateTask1";
        int progress = 1;
        private AsyncTaskActivity activity = null;
        private boolean isCancelled=false;

        public UpdateTask1(AsyncTaskActivity activity) {
            attach(activity);
        }

        public void cancel(boolean b){
            isCancelled=b;
        }

        // ===========================================
        // important do not hold a reference so garbage collector can grab old
        // defunct dying activity
        void detach() {
            activity = null;
            Log.d(TAG, "DETACHING");
        }


        // grab a reference to this activity, mindful of leaks
        void attach(AsyncTaskActivity activity) {
            if (activity == null)
                throw new IllegalArgumentException("Activity cannot be null");

            this.activity = activity;
        }

        public void run() {
            for (int i = 1; i <= 10; i++) {
                //simulate some work sleep for .5 seconds
                SystemClock.sleep(500);

                //let main thread know we are busy
                if (activity != null)
                    activity.runOnUiThread(new Runnable() {
                    public void run() {
                        //indicate how far we have gone
                        progress+=10;
                        activity.pBar.setProgress(progress);
                    }
                });

                //periodically check if the user canceled
                if (isCancelled){
                    if (activity != null)
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            //indicate how far we have gone
                            progress=0;
                            activity.pBar.setProgress(0);
                        }
                    });
                    return;
                }

            }

            //let main thread know we are busy
            if (activity != null)
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        //indicate how far we have gone
                        progress+=10;
                        activity.doStop(null);
                    }
                });
        }
    }
    //****************************************
    //notice I only define what will be returned to
    //onCanceled or onPostExecute
    //notice also that this is static, so it does not hold an implicit reference to enclosing
    //activity, rotate the phone and activity is GCed
//    private static class UpdateTask extends AsyncTask<Void, Integer, String> {
//        private static final String TAG = "UpdateTask";
//        int progress = 1;
//        private AsyncTaskActivity activity = null;
//
//        // ===========================================
//        // important do not hold a reference so garbage collector can grab old
//        // defunct dying activity
//        void detach() {
//            activity = null;
//            Log.d(TAG, "DETACHING");
//        }
//
//        public UpdateTask(AsyncTaskActivity activity) {
//            attach(activity);
//            Log.d(TAG, "          ATACHING");
//        }
//
//        // grab a reference to this activity, mindful of leaks
//        void attach(AsyncTaskActivity activity) {
//            if (activity == null)
//                throw new IllegalArgumentException("Activity cannot be null");
//
//            this.activity = activity;
//        }
//
//        @Override
//        protected void onPreExecute() {
//            // TODO Auto-generated method stub
//            super.onPreExecute();
//            //do whatever setup you need here
//        }
//
//        /**
//         * this is done in a new thread
//         */
//        @Override
//        protected String doInBackground(Void... params) {
//            for (int i = 1; i <= 10; i++) {
//                //simulate some work sleep for .5 seconds
//                SystemClock.sleep(500);
//
//                //let main thread know we are busy
//                //notice that we are autoboxing int to Integer
//                publishProgress(i);
//
//                //periodically check if the user canceled
//                if (isCancelled())
//                    return ("Canceled");
//            }
//            return "Finished";
//        }
//
//        /**
//         * the following run on UI thread
//         */
//        @Override
//        protected void onProgressUpdate(Integer... value) {
//            super.onProgressUpdate(value);
//
//            //indicate how far we have gone
//            progress = value[0] * 10;
//            activity.pBar.setProgress(progress);
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            super.onPostExecute(result);
//
//            try {
//                activity.textViewMessage.setText(result);
//                activity.setButtonState(false);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//        @Override
//        protected void onCancelled(String result) {
//            super.onCancelled(result);
//
//            try {
//                activity.textViewMessage.setText(result);
//                activity.setButtonState(false);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }

}
