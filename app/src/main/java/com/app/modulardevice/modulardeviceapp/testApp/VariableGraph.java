package com.app.modulardevice.modulardeviceapp.testApp;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.androidplot.Plot;
import com.androidplot.util.PixelUtils;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.*;
import com.app.modulardevice.modulardeviceapp.AppEngine;
import com.app.modulardevice.modulardeviceapp.R;
import com.app.modulardevice.modulardeviceapp.service.BleService;
import com.app.modulardevice.modulardeviceapp.utils.Util;

import java.text.DecimalFormat;
import java.util.Observable;
import java.util.Observer;

import static com.app.modulardevice.modulardeviceapp.utils.LoggerUtil.LOGE;


/**
 * Created by igbt6 on 27.11.2015.
 */
public class VariableGraph extends AppCompatActivity{
    private final static String TAG  = VariableGraph.class.getSimpleName();
    private final static boolean LOGGER_ENABLE = true;

        // redraws a plot whenever an update is received:
        private class MyPlotUpdater implements Observer {
            Plot plot;

            public MyPlotUpdater(Plot plot) {
                this.plot = plot;
            }

            @Override
            public void update(Observable o, Object arg) {
                plot.redraw();
            }
        }
        private Integer mModId;
        private String  mModName;
        private String  mModVarName;
        private XYPlot dynamicPlot;
        private MyPlotUpdater plotUpdater;
        private Double mValueReceived=10.0;
        SampleDynamicXYDatasource data;
        private Thread myThread;

        @Override
        public void onCreate(Bundle savedInstanceState) {

            // android boilerplate stuff
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_module_test_graph);
            Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(mToolbar);
            mToolbar.setNavigationIcon(R.drawable.ic_back);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
            // get handles to our View defined in layout.xml:
            dynamicPlot = (XYPlot) findViewById(R.id.dynamicXYPlot);
            ;
            plotUpdater = new MyPlotUpdater(dynamicPlot);

            // only display whole numbers in domain labels
            dynamicPlot.getGraphWidget().setDomainValueFormat(new DecimalFormat("0"));
            final Intent intent = getIntent();
            mModId = intent.getIntExtra("MOD_ID",-1);
            mModName = intent.getStringExtra("MOD_NAME");
            mModVarName = intent.getStringExtra("MOD_VAR_NAME");
            dynamicPlot.setTitle(mModName + " - "+mModVarName);
            // getInstance and position datasets:
            data = new SampleDynamicXYDatasource();
            SampleDynamicSeries sine1Series = new SampleDynamicSeries(data, mModVarName);

            LineAndPointFormatter formatter1 = new LineAndPointFormatter(
                    Color.rgb(0, 0, 0), null, null, null);
            formatter1.getLinePaint().setStrokeJoin(Paint.Join.ROUND);
            formatter1.getLinePaint().setStrokeWidth(10);
            dynamicPlot.addSeries(sine1Series,
                    formatter1);

            LineAndPointFormatter formatter2 =
                    new LineAndPointFormatter(Color.rgb(0, 0, 200), null, null, null);
            formatter2.getLinePaint().setStrokeWidth(10);
            formatter2.getLinePaint().setStrokeJoin(Paint.Join.ROUND);


            // hook up the plotUpdater to the data model:
            data.addObserver(plotUpdater);

            // thin out domain tick labels so they dont overlap each other:
            dynamicPlot.setDomainStepMode(XYStepMode.INCREMENT_BY_VAL);
            dynamicPlot.setDomainStepValue(5);

            dynamicPlot.setRangeStepMode(XYStepMode.INCREMENT_BY_VAL);
            dynamicPlot.setRangeStepValue(10);

            dynamicPlot.setRangeValueFormat(new DecimalFormat("###.#"));

            // uncomment this line to freeze the range boundaries:
            dynamicPlot.setRangeBoundaries(-100, 100, BoundaryMode.FIXED);

            // create a dash effect for domain and range grid lines:
            DashPathEffect dashFx = new DashPathEffect(
                    new float[] {PixelUtils.dpToPix(3), PixelUtils.dpToPix(3)}, 0);
            dynamicPlot.getGraphWidget().getDomainGridLinePaint().setPathEffect(dashFx);
            dynamicPlot.getGraphWidget().getRangeGridLinePaint().setPathEffect(dashFx);


        }

        @Override
        public void onResume() {
            // kick off the data generating thread:
            registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
            myThread = new Thread(data);
            myThread.start();
            super.onResume();
        }

        @Override
        public void onPause() {
            data.stopThread();
            super.onPause();
            unregisterReceiver(mGattUpdateReceiver);
        }

        class SampleDynamicXYDatasource implements Runnable {

            // encapsulates management of the observers watching this datasource for update events:
            class MyObservable extends Observable {
                @Override
                public void notifyObservers() {
                    setChanged();
                    super.notifyObservers();
                }
            }

            private static final int SAMPLE_SIZE = 30;
            private int valAmp = 1;
            private MyObservable notifier;
            private boolean keepRunning = false;

            {
                notifier = new MyObservable();
            }

            public void stopThread() {
                keepRunning = false;
            }

            @Override
            public void run() {
                try {
                    keepRunning = true;
                    while (keepRunning) {

                        Thread.sleep(100); // decrease or remove to speed up the refresh rate.
                        valAmp = mValueReceived.intValue();//+= AMP_STEP;

                        notifier.notifyObservers();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            public int getItemCount() {
                return SAMPLE_SIZE;
            }

            public Number getX( int index) {
                if (index >= SAMPLE_SIZE) {
                    throw new IllegalArgumentException();
                }
                return index;
            }

            public Number getY( int index) {
                if (index >= SAMPLE_SIZE) {
                    throw new IllegalArgumentException();
                }
                return valAmp;
            }

            public void addObserver(Observer observer) {
                notifier.addObserver(observer);
            }

            public void removeObserver(Observer observer) {
                notifier.deleteObserver(observer);
            }

        }

        class SampleDynamicSeries implements XYSeries {
            private SampleDynamicXYDatasource datasource;
            private String title;

            public SampleDynamicSeries(SampleDynamicXYDatasource datasource, String title) {
                this.datasource = datasource;
                this.title = title;
            }

            @Override
            public String getTitle() {
                return title;
            }

            @Override
            public int size() {
                return datasource.getItemCount();
            }

            @Override
            public Number getX(int index) {
                return datasource.getX(index);
            }

            @Override
            public Number getY(int index) {
                return datasource.getY( index);
            }
        }

    // Handles the following actions sending by BleService -ACTION_DATA_AVAILABLE
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
             if (BleService.ACTION_DATA_AVAILABLE.equals(action)) {
                int moduleId= intent.getIntExtra(BleService.READ_MSG_DATA_MOD_ID, -1);
                byte[] receivedRawData = intent.getByteArrayExtra(BleService.READ_MSG_DATA_RAW_DATA);
                if(receivedRawData!=null&&moduleId!=-1&&moduleId==mModId)
                    try {
                        mValueReceived = AppEngine.getInstance().getModuleById(moduleId).getModuleVariablebyName(mModVarName).computeEquation(Util.byteArrayToObjects(receivedRawData));
                    }
                    catch (Exception e){
                        LOGE(LOGGER_ENABLE, TAG, "computation failed");
                    }
                }
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}

