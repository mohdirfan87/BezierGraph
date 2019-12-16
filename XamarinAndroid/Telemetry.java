package org.spektrum.dx2e_programmer;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.spektrum.dx2e_programmer.UI.CustomSeekBar;
import org.spektrum.dx2e_programmer.UI.OnSwipeListener;
import org.spektrum.dx2e_programmer.UI.TelemetryProgress;
import org.spektrum.dx2e_programmer.capture_runs.RunsCache;
import org.spektrum.dx2e_programmer.capture_runs.RunsModel;
import org.spektrum.dx2e_programmer.comm_ble.BlueLayer;
import org.spektrum.dx2e_programmer.comm_ble.DataMode;
import org.spektrum.dx2e_programmer.comm_ble.Dx2eActions;
import org.spektrum.dx2e_programmer.customodel.ElemetryRangeModel;
import org.spektrum.dx2e_programmer.customodel.QOSModel;
import org.spektrum.dx2e_programmer.customodel.RPMModel;
import org.spektrum.dx2e_programmer.customodel.TelemetrySettinsModel;
import org.spektrum.dx2e_programmer.dx2eutils.Alarms;
import org.spektrum.dx2e_programmer.dx2eutils.QuickstartPreferences;
import org.spektrum.dx2e_programmer.dx2eutils.TelemetryPack;
import org.spektrum.dx2e_programmer.dx2eutils.TelemetryRestore;
import org.spektrum.dx2e_programmer.fragments.SharePopUpFragmentDialog;
import org.spektrum.dx2e_programmer.models.Model;
import org.spektrum.dx2e_programmer.models.Structs_Surface;

import java.io.IOException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.content.Context.BIND_AUTO_CREATE;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Telemetry.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Telemetry#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Telemetry extends Fragment implements View.OnLongClickListener, View.OnClickListener, SharePopUpFragmentDialog.SharePopHandler, View.OnTouchListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    private static final String TAG = Telemetry.class.getSimpleName();
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private TelemetryProgress tachoMeter;
    private TelemetryProgress speedoMeter;
    private CustomSeekBar customSeekbar_temp;
    private CustomSeekBar customSeekbarVoltage;
    private CustomSeekBar customSeekbarDrivePack;
    Handler telemetry_Handler;
    private Model model;
    private Structs_Surface.TelemetrySettings[] telemetrySettings;

    private RelativeLayout top_speed_layout;
    private LinearLayout customSeekbar_temp_item1;
    private LinearLayout customSeekbarVoltage_item1;
    private LinearLayout customSeekbarDrivePack_item1;
    private LinearLayout buttonReset;
    private ImageButton share_button;
    private ImageButton button_settings;
    private ImageButton recording_button;
    private TextView textView_topSpeed;
    private TextView textView_MPH;


    public Alarms alarms;
    private int poleCount = 4;

    /*Recording Runs*/
    private long startMills;
    private RunsModel runsModel;
    private List<RunsModel> runsModels;
    private boolean isStoringRuns;

    private LinearLayout swipeLayout;
    private float initialX, initialY;

    private void alertSwipe() {

    }

    public Telemetry() {
        // Required empty public constructor
    }

    private void broadcastUpdate(final String action) {
        Intent intent = new Intent(action);
        getActivity().sendBroadcast(intent);
    }

    public static Telemetry newInstance(String param1, String param2) {
        Telemetry fragment = new Telemetry();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {

          /*  MainActivity.isFirstSwitch = false;
            if (BlueLayer.mConnectionState == BlueLayer.STATE_CONNECTED) {
                Dx2e_Programmer.getInstance().dataMode = DataMode.TELEMETRY;
                broadcastUpdate(Dx2eActions.ACTION_GATT_SERVICES_DISCOVERED);
            } else if (Dx2e_Programmer.getInstance().dataMode != DataMode.COMMUNICATION) {
                Dx2e_Programmer.getInstance().dataMode = DataMode.COMMUNICATION;
                broadcastUpdate(Dx2eActions.ACTION_GATT_SERVICES_DISCOVERED);
            }

            if (mListener != null) {
                mListener.OnTelemetryVisible();
            }
        } else {
            if (mAlarmServicer != null) {
                mAlarmServicer.silentAlarm();
            }
            /*else {
            saveModel();
        }*/
        }
    }

    public void initTelemetry() {
        if (Dx2e_Programmer.getInstance().modelCache != null) {
            Log.v(TAG, "initTelemetry");
            model = Dx2e_Programmer.getInstance().modelCache.getCurrentModel();
            if (model != null) {
                Structs_Surface registerStruct = model.registerStruct;
                if (registerStruct != null) {
                    Structs_Surface.TelemetryData telemetryData = registerStruct.telemetryData;
                    if (telemetryData != null) {
                        telemetrySettings = telemetryData.telemetrySettings;
                        if (telemetrySettings != null && telemetrySettings[3] != null) {
                            poleCount = telemetrySettings[3].pollCount;
                        }
                    }
                }
            }
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("Telemetry", "onCreate");
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        //alarms = new Alarms();
        alarms = new Alarms(getActivity());
        initTelemetry();
        setIsDashboard(true);
    }

    boolean mBounded;
    AlarmService mAlarmServicer;
    ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceDisconnected(ComponentName name) {
            Toast.makeText(getActivity(), "Service is disconnected", Toast.LENGTH_LONG).show();
            mBounded = false;
            mAlarmServicer = null;
            Log.v("Telemetry", "onServiceDisconnected");
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            // Toast.makeText(getActivity(), "Service is connected", Toast.LENGTH_LONG).show();
            mBounded = true;
            AlarmService.LocalBinder mLocalBinder = (AlarmService.LocalBinder) service;
            mAlarmServicer = mLocalBinder.getAlarmServiceInstance();
            Log.v("Telemetry", "onServiceConnected");
        }
    };

    private void setDisplayed() {
        if (telemetrySettings != null && telemetrySettings[0] != null && telemetrySettings[0].display == 0) {
            customSeekbarDrivePack.setVisibility(View.INVISIBLE);
            customSeekbarDrivePack_item1.setVisibility(View.INVISIBLE);
        }
        if (telemetrySettings != null && telemetrySettings[1] != null && telemetrySettings[1].display == 0) {
            customSeekbarVoltage.setVisibility(View.INVISIBLE);
            customSeekbarVoltage_item1.setVisibility(View.INVISIBLE);
        }
        //top_speed_layout
        if (telemetrySettings != null && telemetrySettings[2] != null && telemetrySettings[2].display == 0) {
            speedoMeter.setVisibility(View.INVISIBLE);
            top_speed_layout.setVisibility(View.INVISIBLE);
        }
        if (telemetrySettings != null && telemetrySettings[3] != null && telemetrySettings[3].display == 0) {
            tachoMeter.setVisibility(View.INVISIBLE);
        }
        if (telemetrySettings != null && telemetrySettings[4] != null && telemetrySettings[4].display == 0) {
            customSeekbar_temp.setVisibility(View.INVISIBLE);
            customSeekbar_temp_item1.setVisibility(View.INVISIBLE);
        }
        if (telemetrySettings != null) {
            Log.v("setDisplayed ", "telemetrySettings[0] " + telemetrySettings[0].display);
            Log.v("setDisplayed ", "telemetrySettings[1] " + telemetrySettings[1].display);
            Log.v("setDisplayed ", "telemetrySettings[2] " + telemetrySettings[2].display);
            Log.v("setDisplayed ", "telemetrySettings[3] " + telemetrySettings[3].display);
            Log.v("setDisplayed ", "telemetrySettings[4] " + telemetrySettings[4].display);
        }

    }

    private CustomSeekBar customSeekbarCurrent;
    private LinearLayout customSeekbarCurrent_item1;

    private void initwidgets(View view) {
        textView_MPH = (TextView) view.findViewById(R.id.textView_MPH);
        textView_topSpeed = (TextView) view.findViewById(R.id.textView_topSpeed);
        speedoMeter = (TelemetryProgress) view.findViewById(R.id.speedoMeter);
        tachoMeter = (TelemetryProgress) view.findViewById(R.id.tachoMeter);
        customSeekbar_temp = (CustomSeekBar) view.findViewById(R.id.customSeekbar_temp);
        customSeekbarVoltage = (CustomSeekBar) view.findViewById(R.id.customSeekbarVoltage);
        customSeekbarCurrent = (CustomSeekBar) view.findViewById(R.id.customSeekbarCurrent);
        customSeekbarDrivePack = (CustomSeekBar) view.findViewById(R.id.customSeekbarDrivePack);


        top_speed_layout = (RelativeLayout) view.findViewById(R.id.top_speed_layout);
        customSeekbar_temp_item1 = (LinearLayout) view.findViewById(R.id.customSeekbar_temp_item1);
        customSeekbarVoltage_item1 = (LinearLayout) view.findViewById(R.id.customSeekbarVoltage_item1);
        customSeekbarCurrent_item1 = (LinearLayout) view.findViewById(R.id.customSeekbarCurrent_item1);
        customSeekbarDrivePack_item1 = (LinearLayout) view.findViewById(R.id.customSeekbarDrivePack_item1);


        buttonReset = (LinearLayout) view.findViewById(R.id.buttonReset);
        share_button = view.findViewById(R.id.share_button);
        recording_button = view.findViewById(R.id.recording_button);
        buttonReset.setOnClickListener(this);
        share_button.setOnClickListener(this);
        recording_button.setOnClickListener(this);
        setDisplayed();
        initTelemetryData();


    }

    private void socialAreaVisibility() {
        if (!QuickstartPreferences.isFacebookSelected(getActivity()) && !QuickstartPreferences.isTwitterSelected(getActivity()) &&
                !QuickstartPreferences.isInstagramSelected(getActivity())) {
            share_button.setVisibility(View.INVISIBLE);
            recording_button.setVisibility(View.INVISIBLE);
        } else {
            share_button.setVisibility(View.VISIBLE);
            recording_button.setVisibility(View.VISIBLE);
            recording_button.setActivated(false);
        }
        /*share_button.setVisibility(View.VISIBLE);
        recording_button.setVisibility(View.VISIBLE);*/
    }


    public List<RunsModel> getRunsModel() {
        return runsModels;
    }


    public void recordRuns() {
        if (!recording_button.isActivated()) {
            startRecording();
        } else {
            stopRecording();
        }
        if (mListener != null) {
            mListener.runsRecording(recording_button.isActivated());
        }
    }

    private void startRecording() {
        if (!isRecordingValid()) {
            showRunRecordingErrorDialog("No widgets are available", "All widgets are disables, so runs can not be recorded");
            return;
        }
        share_button.setImageResource(R.drawable.ic_share_grayed_icon);
        button_settings.setImageResource(R.drawable.ic_settings_grayed_icon);
        runsModel = new RunsModel();
        resetTopSpeed();
        getRunsSettings();
        runsModel.setCaptureStartTime(captureStartTime());
        startMills = System.currentTimeMillis();
        recording_button.setActivated(true);
        recording_button.setImageResource(R.drawable.ic_stop_recording);
//            disabledWidgets();
        swipeLayout.setVisibility(View.VISIBLE);
    }

    public void stopRecording() {
        swipeLayout.setVisibility(View.GONE);
        share_button.setImageResource(R.drawable.ic_share);
        button_settings.setImageResource(R.drawable.ic_settings_black_24dp);
        runsModel.setDuration(getDuration(System.currentTimeMillis() - startMills));
        recording_button.setImageResource(R.drawable.ic_start_recording);
        Log.v(TAG, "recordRuns " + runsModel.getRpmModel());
        if (runsModel.getRpmModel() != null && runsModel.getRpmModel().getRpm() > 0) {
            runsModels.add(0, runsModel);
        } else {
            showRunRecordingErrorDialog("", "No telemetry data was received");
        }
        recording_button.setActivated(false);
        if (mListener != null) {
            mListener.runsRecording(recording_button.isActivated());
        }
    }


    private boolean isRecordingValid() {
        if (telemetrySettings != null) {
            if (telemetrySettings[0] != null && telemetrySettings[0].display == 1) {
                return true;
            } else if (telemetrySettings[1] != null && telemetrySettings[1].display == 1) {
                return true;
            } else if (telemetrySettings[2] != null && telemetrySettings[2].display == 1) {
                return true;
            } else if (telemetrySettings[3] != null && telemetrySettings[3].display == 1) {
                return true;
            } else if (telemetrySettings[4] != null && telemetrySettings[4].display == 1) {
                return true;
            }
        }

        return false;
    }

    private void getRunsSettings() {
        runsModel.setModelName(model.name);
        runsModel.setModelType(model.modelType);
        runsModel.setMotorType(model.motorType);
        runsModel.setESCType(model.ESCType);
        runsModel.setBatteryType(model.batteryType);
        runsModel.setGearRatio(model.gearRatioFirst + "/" + model.gearRatioSecond);
        runsModel.setUnitType(model.telemetryUnit);

        runsModel.setDpVoltageMax(telemetrySettings[0].maxRange);
        runsModel.setRxVoltageMax(telemetrySettings[1].maxRange);
        runsModel.setSpeedMax(telemetrySettings[2].maxRange);
        runsModel.setTachoMax(telemetrySettings[3].maxRange);
        runsModel.setTempMax(telemetrySettings[4].maxRange);
        runsModel.setRollOut(telemetrySettings[2].impRollOut);


        Log.v(TAG, "getSpeedMax " + runsModel.getSpeedMax());
    }


    private void storeRPMModelOnHighSpeed(RPMModel rpmModel) {
        if (recording_button.isActivated()) {
            Log.v(TAG, "temp== " + rpmModel.getTemperature());
            isStoringRuns = true;
            runsModel.setRpmModel(rpmModel);
        }

    }

    private void storeQOSModelOnHighSpeed(QOSModel qosModel) {
        if (recording_button.isActivated() && isStoringRuns) {
            isStoringRuns = false;
            runsModel.setQosModel(qosModel);
        }
    }

    private String getDuration(long duration) {
        long minutes = (duration / 1000) / 60;
        long seconds = (duration / 1000) % 60;
        Log.v("Run Record", "" + minutes + ":" + seconds);
        return minutes + ":" + seconds;
    }

    private String captureStartTime() {
        return new SimpleDateFormat("MM/dd/yyyy HH:mm").format(new Date());
    }

    public void showRunRecordingErrorDialog(String title, String msg) {
        new AlertDialog.Builder(getActivity(), R.style.Theme_AppCompat_Light_Dialog_Alert)
                .setMessage(msg)
                .setCancelable(false)
                .setTitle(title)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    /*TODO Alarms values are to be set*/
    private void setMaxLimits() {
        if (telemetrySettings != null && telemetrySettings[0] != null) {
            customSeekbarDrivePack.setMaxValue(telemetrySettings[0].maxRange);
        }
        if (telemetrySettings != null && telemetrySettings[1] != null) {
            customSeekbarVoltage.setMaxValue(telemetrySettings[1].maxRange);
        }
        if (telemetrySettings != null && telemetrySettings[2] != null) {
            speedoMeter.setMaxValue(telemetrySettings[2].maxRange);
        }
        if (telemetrySettings != null && telemetrySettings[3] != null) {
            tachoMeter.setMaxValue(telemetrySettings[3].maxRange);
        }
        if (telemetrySettings != null && telemetrySettings[4] != null) {
            customSeekbar_temp.setMaxValue(telemetrySettings[4].maxRange);
        }
    }

    private void initSpeedoMeter(int width, int height) {
        speedoMeter.getLayoutParams().width = width > 1500 ? width * 45 / 100 : width * 55 / 100;
        speedoMeter.getLayoutParams().height = height > 720 ? height * 45 / 100 : height * 50 / 100;
        speedoMeter.setStrokeWidth(width * 7 / 100);
        Log.d(TAG, "onCreateView: getStrokeWidth " + speedoMeter.getStrokeWidth());
        speedoMeter.setOnLongClickListener(this);
        speedoMeter.setOnClickListener(this);
    }

    private void initTachMeter(int width, int height) {
        tachoMeter.getLayoutParams().width = width > 1500 ? width * 33 / 100 : width * 33 / 100;
        tachoMeter.getLayoutParams().height = height > 720 ? height * 33 / 100 : height * 30 / 100;

        tachoMeter.setStrokeWidth(width * 4 / 100);
        tachoMeter.setOnLongClickListener(this);
        tachoMeter.setOnClickListener(this);
    }

    private void initTemp(int width, int height) {
        customSeekbar_temp.getLayoutParams().height = height * 10 / 100;
        customSeekbar_temp.setHeightCanvas(width * 3 / 100);
        customSeekbar_temp.setOnLongClickListener(this);
        customSeekbar_temp.setOnClickListener(this);
    }

    private void initVoltage(int width, int height) {
        customSeekbarVoltage.setMinValue(0.0f);
        customSeekbarVoltage.getLayoutParams().height = height * 10 / 100;
        customSeekbarVoltage.setHeightCanvas(width * 3 / 100);
        customSeekbarVoltage.setOnLongClickListener(this);
        customSeekbarVoltage.setOnClickListener(this);
    }

    private void initCurrent(int width, int height) {
        customSeekbarCurrent.setMinValue(0.0f);
        customSeekbarCurrent.getLayoutParams().height = height * 10 / 100;
        customSeekbarCurrent.setHeightCanvas(width * 3 / 100);
        customSeekbarCurrent.setOnLongClickListener(this);
        customSeekbarCurrent.setOnClickListener(this);
    }

    private void initDrivePack(int width, int height) {
        customSeekbarDrivePack.setMinValue(3.0f);
        customSeekbarDrivePack.getLayoutParams().height = height * 10 / 100;
        customSeekbarDrivePack.setHeightCanvas(width * 3 / 100);
        customSeekbarDrivePack.setOnLongClickListener(this);
        customSeekbarDrivePack.setOnClickListener(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v("Telemetry", "onCreateView");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_telemetry, container, false);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        Log.d(TAG, "onCreateView: width " + width + "  height " + height);
        initwidgets(view);
        socialAreaVisibility();
        initHeaderFont(view, width);
        initSpeedoMeter(width, height);
        initTachMeter(width, height);
        initTemp(width, height);
        initVoltage(width, height);
        initCurrent(width, height);
        initDrivePack(width, height);
        if (model != null) {
            initRestored();
        }

        telemetry_Handler = new Handler();
        setBlinked();
        //AlarmService.startAlarmService(getActivity());
        //setRPM();
        startService();
        getRuns();

        swipeLayout = view.findViewById(R.id.swipeLayout);
        swipeLayout.setOnTouchListener(this);

        if (isESC) {
            displayCurrent();
        } else {
            displayRxVolt();
        }
        return view;
    }

    private void displayCurrent() {
        customSeekbarCurrent.setVisibility(View.VISIBLE);
        customSeekbarCurrent_item1.setVisibility(View.VISIBLE);

        customSeekbarVoltage.setVisibility(View.GONE);
        customSeekbarVoltage_item1.setVisibility(View.GONE);

    }

    private void displayRxVolt() {
        customSeekbarVoltage.setVisibility(View.VISIBLE);
        customSeekbarVoltage_item1.setVisibility(View.VISIBLE);

        customSeekbarCurrent.setVisibility(View.GONE);
        customSeekbarCurrent_item1.setVisibility(View.GONE);

    }

    private void getRuns() {
        RunsCache runsCache = new RunsCache();
        runsModels = runsCache.readCaptureRuns();


        if (runsModels == null) {
            runsModels = new ArrayList<>();
        }
        /*if (tab1 != null && tab1 instanceof Telemetry) {
            tab1.setRunsModel(runsModels);
        }*/
    }

    /* private TelemetryProgress tachoMeter;
     private TelemetryProgress speedoMeter;
     private CustomSeekBar customSeekbar_temp;
     private CustomSeekBar customSeekbarVoltage;
     private CustomSeekBar customSeekbarDrivePack;*/
    public void initOnDisconnect() {
        TelemetryRestore.packVolts = 3.0f;
        TelemetryRestore.rxvoltage = 0.0f;
        TelemetryRestore.current = 0.0f;
        TelemetryRestore.temperature = 0;
        TelemetryRestore.rpm = 0;
        TelemetryRestore.speed = 0;
        //TelemetryRestore.topSpeed = 0;
    }

    private void initRestored() {
        if (model != null && model.telemetryUnit == Model.METRIC) {
            tmpUnit = degree + "C";
            customSeekbar_temp.setProgress(TelemetryRestore.temperature, tmpUnit);
        } else {
            tmpUnit = degree + "F";
            customSeekbar_temp.setProgress(TelemetryRestore.temperature, tmpUnit);
        }

        customSeekbarCurrent.setProgress(TelemetryRestore.current, "A");
        customSeekbarVoltage.setProgress(TelemetryRestore.rxvoltage, "V");
        customSeekbarDrivePack.setProgress(TelemetryRestore.packVolts, "V");

//        customSeekbarVoltage.setProgress(TelemetryRestore.rxvoltage, "V");

        if (model.telemetryUnit == Model.METRIC) {
            unitText = " KPH";
            textView_MPH.setText("KPH");
            //speedoMeter.setProgress(getProgress(TelemetryRestore.speed, telemetrySettings[2].maxRange), (int) TelemetryRestore.speed, unitText, -1);
            textView_topSpeed.setText(String.valueOf(TelemetryRestore.topSpeed) + unitText);
            if (TelemetryRestore.speed > telemetrySettings[2].maxRange) {
                speedoMeter.setProgress(getProgress(telemetrySettings[2].maxRange, telemetrySettings[2].maxRange), TelemetryRestore.speed, unitText, -1);
            } else {
                speedoMeter.setProgress(getProgress(TelemetryRestore.speed, telemetrySettings[2].maxRange), TelemetryRestore.speed, unitText, -1);
//                    speedoMeter.setAlarmType(-1);
            }
        } else {
            unitText = " MPH";
            textView_MPH.setText("MPH");
            // speedoMeter.setProgress(getProgress(TelemetryRestore.speed, telemetrySettings[2].maxRange), (int) TelemetryRestore.speed, unitText, -1);
            textView_topSpeed.setText(String.valueOf(TelemetryRestore.topSpeed) + unitText);
            if (TelemetryRestore.speed > telemetrySettings[2].maxRange) {
                speedoMeter.setProgress(getProgress(telemetrySettings[2].maxRange, telemetrySettings[2].maxRange), TelemetryRestore.speed, unitText, -1);
            } else {
                speedoMeter.setProgress(getProgress(TelemetryRestore.speed, telemetrySettings[2].maxRange), TelemetryRestore.speed, unitText, -1);
//                    speedoMeter.setAlarmType(-1);
            }
        }
        if (TelemetryRestore.rpm > telemetrySettings[3].maxRange) {
            tachoMeter.setProgress(getProgress(telemetrySettings[3].maxRange, telemetrySettings[3].maxRange), TelemetryRestore.rpm, "RPM", -1);
        } else {
            tachoMeter.setProgress(getProgress(TelemetryRestore.rpm, telemetrySettings[3].maxRange), (int) TelemetryRestore.rpm, "RPM", -1);
        }

        if (TelemetryRestore.topRPM > 0) {
            RPMModel rpmModel = new RPMModel();
            rpmModel.setRpm(TelemetryRestore.topRPM);
            if (model.telemetryUnit == Model.METRIC) {
                textView_topSpeed.setText(String.valueOf(Math.round(rpmModel.calMetricSpeed((float) rpmModel.getRpm(), telemetrySettings[2].impRollOut))) + " " + unitText);
                //TelemetryRestore.topSpeed = previousTopSpeed;
            } else {
                textView_topSpeed.setText(String.valueOf(Math.round(rpmModel.calImpSpeed((float) rpmModel.getRpm(), telemetrySettings[2].impRollOut))) + " " + unitText);
                //TelemetryRestore.topSpeed = previousTopSpeed;
            }
        }
    }

    public void stopService() {
        try {
            if (mConnection != null) {
                //AlarmService.stopAlarmService(getActivity());ss
                getActivity().getApplicationContext().unbindService(mConnection);
                if (mAlarmServicer != null) {
                    mAlarmServicer.stopService();
                }
                initAlarmColor();
            }
        } catch (IllegalArgumentException ex) {

        }
    }

    public void initAlarmColor() {
//        Log.v("initAlarmColor", "initAlarmColor");
     /*   if (telemetrySettings != null && telemetrySettings[0] != null) {
            telemetrySettings[0].alarm = 1;
            telemetrySettings[1].alarm = 1;
            telemetrySettings[2].alarm = 1;
            telemetrySettings[3].alarm = 1;
            telemetrySettings[4].alarm = 1;
            saveModel();
        }*/
        if (customSeekbarDrivePack != null) {
            customSeekbarDrivePack.setAlarmType(-1);
        }
        if (customSeekbarVoltage != null) {
            customSeekbarVoltage.setAlarmType(-1);
        }
        if (customSeekbarCurrent != null) {
            customSeekbarCurrent.setAlarmType(-1);
        }
        if (tachoMeter != null) {
            tachoMeter.setAlarmType(-1);
        }
        if (speedoMeter != null) {
            speedoMeter.setAlarmType(-1);
        }
        if (customSeekbar_temp != null) {
            customSeekbar_temp.setAlarmType(-1);
        }
    }

    public void startService() {
        Intent mIntent = new Intent(getActivity(), AlarmService.class);
        Telemetry.this.getActivity().getApplicationContext().bindService(mIntent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v("Telemetry", "onDestroy");
        //AlarmService.stopAlarmService(getActivity());
        //stopService();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v("Telemetry", "onResume");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.v("Telemetry", "onStart");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v("Telemetry", "onPause");
        /*try {
            if (mConnection != null) {
                getActivity().unbindService(mConnection);
            }
        } catch (IllegalArgumentException ex) {

        }*/
        /*if (mAlarmServicer != null) {
            mAlarmServicer.silentAlarm();
        }*/
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.v("Telemetry", "onStop");
    }


    List<RPMModel> rpmModelList = new ArrayList<>();

    private RPMModel meanRPMTelemetry(List<RPMModel> rpmModelList) {
        RPMModel sumRPM = new RPMModel();
        if (rpmModelList != null && rpmModelList.size() > 0) {
//            Log.v("meanRPMTelemetry ", "rpmModelList size " + rpmModelList.size());
            for (int i = 0; i < rpmModelList.size(); i++) {
                sumRPM.setTemperature(sumRPM.getTemperature() + rpmModelList.get(i).getTemperature());
                sumRPM.setPackVolts(sumRPM.getPackVolts() + rpmModelList.get(i).getPackVolts());
                sumRPM.setMicroseconds(sumRPM.getMicroseconds() + rpmModelList.get(i).getMicroseconds());
            }

            RPMModel meanRPM = new RPMModel();
            meanRPM.setTemperature(sumRPM.getTemperature() / rpmModelList.size());
            meanRPM.setMicroseconds(sumRPM.getMicroseconds() / rpmModelList.size());
            meanRPM.setPackVolts(sumRPM.getPackVolts() / rpmModelList.size());
            return meanRPM;
        }
        return null;
    }

    char degree = '\u00B0';//0x0056
    char voltage = '\u0056';
    String tmpUnit = "";

    private void updateRPMTelemetryData(RPMModel meanRPM) {
        displayRPMTemp(meanRPM);
        displayPackVolt(meanRPM);
        displayTachoMeter(meanRPM);
        if (!isGPS) {
            displaySpeed(meanRPM);
        }
    }

    private void displayRPMTemp(RPMModel meanRPM) {
        if (model != null && model.telemetryUnit == Model.METRIC) {
//            Log.v("Telemetry", "telemetrySettings " + telemetrySettings);
            tmpUnit = degree + "C";
//            customSeekbar_temp.setAlarmType(-1);
            if (telemetrySettings != null && telemetrySettings[4] != null && meanRPM.getTemperature() > 0 && tempInCelecius(meanRPM.getTemperature()) > telemetrySettings[4].maxRange) {
                customSeekbar_temp.setProgress(telemetrySettings[4].maxRange, tmpUnit);
            } else if (meanRPM.getTemperature() <= 0.0f) {
                customSeekbar_temp.setProgress(0, tmpUnit);
            } else {
                customSeekbar_temp.setProgress(tempInCelecius(meanRPM.getTemperature()), tmpUnit);
                TelemetryRestore.temperature = tempInCelecius(meanRPM.getTemperature());
                meanRPM.setTemperature(tempInCelecius(meanRPM.getTemperature()));

            }

            playTempAlarm(meanRPM);
        } else if (telemetrySettings != null && telemetrySettings[4] != null) {
            tmpUnit = degree + "F";
//            customSeekbar_temp.setAlarmType(-1);
            if (meanRPM.getTemperature() > telemetrySettings[4].maxRange) {
                customSeekbar_temp.setProgress(telemetrySettings[4].maxRange, tmpUnit);
            } else if (meanRPM.getTemperature() <= 0.0f) {
                customSeekbar_temp.setProgress(0, tmpUnit);
            } else {
                customSeekbar_temp.setProgress(meanRPM.getTemperature(), tmpUnit);
                TelemetryRestore.temperature = meanRPM.getTemperature();
//                customSeekbar_temp.setAlarmType(-1);
            }
            if (BlueLayer.mConnectionState == BlueLayer.STATE_CONNECTED)
                playTempAlarm(meanRPM);
        }
    }

    private void playSpeedAlarm(float topSpeed) {
        if (telemetrySettings != null && telemetrySettings[2] != null && telemetrySettings[2].alarm == 0) {
            if (topSpeed > 0 && topSpeed < telemetrySettings[2].alarmLow) {
                setOnAlarm(2);
                speedoMeter.setAlarmType(0);
                if (MainActivity.telemetry_Tap_Alarm[2] == 0) {
                    playAlarm();
                }
                Log.v("playAlarm", "low speed");
            } else if (topSpeed > telemetrySettings[2].alarmHigh) {
                setOnAlarm(2);
                speedoMeter.setAlarmType(1);
                if (MainActivity.telemetry_Tap_Alarm[2] == 0) {
                    playAlarm();
                }
                Log.v("playAlarm", "high speed");
            } else if (MainActivity.is_High_Alarm[2] == 1) {
                MainActivity.is_High_Alarm[2] = 0;
                speedoMeter.setAlarmType(-1);
                silentAlarm();
                Log.v("playAlarm", "silent speed");
            }
        }
    }

    private void playPackVoltAlarm(RPMModel meanRPM) {
        if (telemetrySettings != null && telemetrySettings[0] != null && telemetrySettings[0].alarm == 0) {
            if (meanRPM.getPackVolts() > 0 && meanRPM.getPackVolts() < telemetrySettings[0].alarmLow) {
                setOnAlarm(0);
                customSeekbarDrivePack.setAlarmType(0);
                if (MainActivity.telemetry_Tap_Alarm[0] == 0) {
                    playAlarm();
                }
                Log.v("playAlarm", "low pack volt");
            } else if (meanRPM.getPackVolts() > telemetrySettings[0].alarmHigh) {
                setOnAlarm(0);
                customSeekbarDrivePack.setAlarmType(1);
                if (MainActivity.telemetry_Tap_Alarm[0] == 0) {
                    playAlarm();
                }
                Log.v("playAlarm", "high pack volt");
            } else if (MainActivity.is_High_Alarm[0] == 1) {
                MainActivity.is_High_Alarm[0] = 0;
                customSeekbarDrivePack.setAlarmType(-1);
                silentAlarm();
                Log.v("playAlarm", "silent pack volt");
            }
        }
    }

    private void playTempAlarm(RPMModel meanRPM) {
        if (telemetrySettings != null && telemetrySettings[4] != null && telemetrySettings[4].alarm == 0) {
            if (meanRPM.getTemperature() > 0 && meanRPM.getTemperature() < telemetrySettings[4].alarmLow) {
                setOnAlarm(4);
                customSeekbar_temp.setAlarmType(0);
                if (MainActivity.telemetry_Tap_Alarm[4] == 0) {
                    playAlarm();
                }
                Log.v("playAlarm", "low temp");
            } else if (meanRPM.getTemperature() > telemetrySettings[4].alarmHigh) {
                setOnAlarm(4);
                customSeekbar_temp.setAlarmType(1);
                if (MainActivity.telemetry_Tap_Alarm[4] == 0) {
                    playAlarm();
                }
                Log.v("playAlarm", "high temp");
            } else if (MainActivity.is_High_Alarm[4] == 1) {
                MainActivity.is_High_Alarm[4] = 0;
                customSeekbar_temp.setAlarmType(-1);
                silentAlarm();
                Log.v("playAlarm", "silent temp");
            }
        }
    }

    private void playTachoAlarm(RPMModel meanRPM) {
        if (telemetrySettings != null && telemetrySettings[3] != null && telemetrySettings[3].alarm == 0) {
            if (meanRPM.getRpm() > 0 && meanRPM.getRpm() < telemetrySettings[3].alarmLow) {
                setOnAlarm(3);
                tachoMeter.setAlarmType(0);
                if (MainActivity.telemetry_Tap_Alarm[3] == 0) {
                    playAlarm();
                }
                Log.v("playAlarm", "low rpm");
            } else if (meanRPM.getRpm() > telemetrySettings[3].alarmHigh) {
                setOnAlarm(3);
                tachoMeter.setAlarmType(1);
                if (MainActivity.telemetry_Tap_Alarm[3] == 0) {
                    playAlarm();
                }
                Log.v("playAlarm", "high rpm");
            } else if (MainActivity.is_High_Alarm[3] == 1) {
                MainActivity.is_High_Alarm[3] = 0;
                tachoMeter.setAlarmType(-1);
                silentAlarm();
                Log.v("playAlarm", "silent rpm");
            }
        }
    }

    private void playCurrentAlarm(RPMModel escModel) {
        if (telemetrySettings != null && telemetrySettings[5] != null && telemetrySettings[5].alarm == 0) {
            Log.v(TAG,"getCurrentMotor "+escModel.getCurrentMotor()+" alarmLow "+telemetrySettings[5].alarmLow);
            if (escModel.getCurrentMotor() > 0.0f && escModel.getCurrentMotor() < telemetrySettings[5].alarmLow) {
                setOnAlarm(5);
                customSeekbarCurrent.setAlarmType(0);
                if (MainActivity.telemetry_Tap_Alarm[5] == 0) {
                    playAlarm();
                }
                Log.v("playAlarm", "low currentMotor");
            } else if (escModel.getCurrentMotor() > telemetrySettings[5].alarmHigh) {
                setOnAlarm(5);
                customSeekbarCurrent.setAlarmType(1);
                if (MainActivity.telemetry_Tap_Alarm[5] == 0) {
                    playAlarm();
                }
                Log.v("playAlarm", "high currentMotor");
            } else if (MainActivity.is_High_Alarm[5] == 1) {
                MainActivity.is_High_Alarm[5] = 0;
                customSeekbarCurrent.setAlarmType(-1);
                silentAlarm();
                Log.v("playAlarm", "silent  currentMotor");
            }
        }
    }

    private void playRxVoltageAlarm(QOSModel qosModel) {
        if (telemetrySettings != null && telemetrySettings[1] != null && telemetrySettings[1].alarm == 0) {
            if (qosModel.getRxvoltage() > 0 && qosModel.getRxvoltage() < telemetrySettings[1].alarmLow) {
                setOnAlarm(1);
                customSeekbarVoltage.setAlarmType(0);
                if (MainActivity.telemetry_Tap_Alarm[1] == 0) {
                    playAlarm();
                }
                Log.v("playAlarm", "low rx volt");
            } else if (qosModel.getRxvoltage() > telemetrySettings[1].alarmHigh) {
                setOnAlarm(1);
                customSeekbarVoltage.setAlarmType(1);
                if (MainActivity.telemetry_Tap_Alarm[1] == 0) {
                    playAlarm();
                }
                Log.v("playAlarm", "high rx volt");
            } else if (MainActivity.is_High_Alarm[1] == 1) {
                MainActivity.is_High_Alarm[1] = 0;
                customSeekbarVoltage.setAlarmType(-1);
                silentAlarm();
                Log.v("playAlarm", "silent rx volt");
            }
        }
    }

    private void setOnAlarm(int index) {
        MainActivity.firstTime_Alarm[index] = 2;
        MainActivity.is_High_Alarm[index] = 1;
    }

    private void playAlarm() {
        if (isDashboard) {
            if (telemetrySettings != null && telemetrySettings[0] != null && telemetrySettings[0].alarm == 0) {
                playAlarmSerivce();
            }
            if (telemetrySettings != null && telemetrySettings[1] != null && telemetrySettings[1].alarm == 0) {
                playAlarmSerivce();
            }
            if (telemetrySettings != null && telemetrySettings[2] != null && telemetrySettings[2].alarm == 0) {
                playAlarmSerivce();
            }
            if (telemetrySettings != null && telemetrySettings[3] != null && telemetrySettings[3].alarm == 0) {
                playAlarmSerivce();
            }
            if (telemetrySettings != null && telemetrySettings[4] != null && telemetrySettings[4].alarm == 0) {
                playAlarmSerivce();
            }
            if (telemetrySettings != null && telemetrySettings[5] != null && telemetrySettings[5].alarm == 0) {
                playAlarmSerivce();
            }
        }
    }


    private int alarm_Timer = 0;

    private void playAlarmSerivce() {
//        if (alarm_Timer++ == 20) {
        //Log.v("playAlarmSerivce", "alarm_Timer " + alarm_Timer);
        //alarm_Timer = 0;
        if (mAlarmServicer != null && mBounded) {
            mAlarmServicer.playAlarm();
        }

        //    }

    }

    private int previousTopSpeed;

    private int getProgress(float value, float limit) {
//        Log.v("getProgress", "value " + value);
        if (value > 0.0f) {
            int percentage = (int) ((value / limit) * 100);
            int disp = (percentage * 50) / 100;
//            Log.v("getProgress", "disp " + disp);
            return disp;
        }
        return 0;
    }

    private void initTelemetryData() {
        if (telemetrySettings != null) {
            for (int i = 0; i < telemetrySettings.length; i++) {
                setModelTelemetry(telemetrySettings[i], i);
            }
        }
    }


    String unitText = "KPH";

    private void setModelTelemetry(Structs_Surface.TelemetrySettings telemetry, int telmetry_Index) {
        switch (telmetry_Index) {
            case 0:
                if (customSeekbarDrivePack.getProgress() > telemetry.maxRange) {
                    customSeekbarDrivePack.setProgress(telemetry.maxRange, tmpUnit);
                }
                customSeekbarDrivePack.setMaxValue(telemetry.maxRange);
                break;
            case 1:
                //changeUnits();
                if (customSeekbarVoltage.getProgress() > telemetry.maxRange) {
                    customSeekbarVoltage.setProgress(telemetry.maxRange, tmpUnit);
                }
                customSeekbarVoltage.setMaxValue(telemetry.maxRange);
                break;
            case 2:
                if (speedoMeter.getProgress() > telemetry.maxRange) {
                    if (model != null && model.telemetryUnit == Model.METRIC) {
                        unitText = "KPH";
                    } else {
                        unitText = "MPH";
                    }
                    speedoMeter.setProgress(getProgress(telemetry.maxRange, telemetry.maxRange), telemetry.maxRange, unitText, -1);
                }
                speedoMeter.setMaxValue(telemetry.maxRange);
                break;
            case 3:
                if (tachoMeter.getProgress() > telemetry.maxRange) {
                    tachoMeter.setProgress(getProgress(telemetry.maxRange, telemetry.maxRange), telemetry.maxRange, "RPM", -1);
                }
                tachoMeter.setMaxValue(telemetry.maxRange);

                break;
            case 4:
                if (customSeekbar_temp.getProgress() > telemetry.maxRange) {
                    customSeekbar_temp.setProgress(telemetry.maxRange, tmpUnit);
                }
                customSeekbar_temp.setMaxValue(telemetry.maxRange);
            case 5:
                if (customSeekbarCurrent.getProgress() > telemetry.maxRange) {
                    customSeekbarCurrent.setProgress(telemetry.maxRange, tmpUnit);
                }
                customSeekbarCurrent.setMaxValue(telemetry.maxRange);
                break;
        }
    }

    private void resetTopSpeed() {
        TelemetryRestore.topRPM = 0;
        if (model.telemetryUnit == Model.METRIC) {
            textView_topSpeed.setText(String.valueOf(TelemetryRestore.topRPM) + " KPH");
        } else {
            textView_topSpeed.setText(String.valueOf(TelemetryRestore.topRPM) + " MPH");
        }
    }


    private void saveModel() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Dx2e_Programmer.getInstance().modelCache.saveModelManager();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private float tempInCelecius(float Tf) {
        float Tc = (Tf - 32.0f) * 5.0f / 9.0f;
        return Tc;
    }

    private float tempInFr1(float Tc) {
        float Tf = (Tc * 9.0f / 5.0f) + 320.0f;
        return Tf;
    }

    private float tempInFr(float Tc) {
        float retval;
        retval = Tc;
        retval *= 1.8;
        retval += 32;
        return (float) (retval);
    }

    float guit_makeESCTempF(float celsiusFETx10) {
        float retval;
        retval = celsiusFETx10;
        retval *= 1.8;
        retval += 320;
        return (retval);

    }

    private long qosTimeOut;
    private long escWaitingTime;
    private boolean firstTimeQOS;

    public void getQOSTelemetryData(byte[] data) {
        if (isESC) {
            return;
        }
       /* if (!firstTimeQOS) {
            firstTimeQOS = !firstTimeQOS;
            escWaitingTime = System.currentTimeMillis();
            return;
        }
        if (System.currentTimeMillis() - escWaitingTime < 2000) {
            return;
        }*/

        if ((data[0] & 0xff) == 0x7f) {
            qosTimeOut = System.currentTimeMillis();
        }
        QOSModel qosModel = pack.setQOSByteToViewMembers(data);
        updateQosUI(qosModel);
    }

    private void updateRxUI(QOSModel qosModel) {
        if (telemetrySettings != null && telemetrySettings[1] != null && qosModel.getRxvoltage() > telemetrySettings[1].maxRange) {
            customSeekbarVoltage.setProgress(telemetrySettings[1].maxRange, tmpUnit);
        } else if (qosModel.getRxvoltage() <= 0.0f) {
            customSeekbarVoltage.setProgress(0.0f, tmpUnit);
        } else {
            tmpUnit = voltage + "";
            customSeekbarVoltage.setProgress(qosModel.getRxvoltage(), tmpUnit);
            TelemetryRestore.rxvoltage = qosModel.getRxvoltage();
        }
        /*Edited to store the high speed runs*/
        storeQOSModelOnHighSpeed(qosModel);
    }

    private void updateQosUI(QOSModel qosModel) {
        if (telemetrySettings != null) {
            updateRxUI(qosModel);
            if (BlueLayer.mConnectionState == BlueLayer.STATE_CONNECTED)
                playRxVoltageAlarm(qosModel);
        }
        collectFrameLost(qosModel.getSignals());
    }

    private List<Integer> frameLostLits = new ArrayList<>();
    private int signalStrength;
    private boolean isGetSignal;

    private void collectFrameLost(int frame_lost) {
        if (frameLostLits != null && frameLostLits.size() < 23) {
            frameLostLits.add(0, frame_lost);
        } else {
            frameLostLits.add(0, frame_lost);
            int fLAvg = getFLAvg(frameLostLits);
            signalStrength = (int) (1 + ((25 - (frameLostLits.get(0).intValue() - fLAvg) / 2.0f) / 5.0f));
            frameLostLits.remove(frameLostLits.size() - 1);
            if (mListener != null) {
                isGetSignal = true;
                mListener.OnGetSignal(signalStrength);
            }
        }
    }

    private int getFLAvg(List<Integer> frameLostLits) {
        if (frameLostLits.size() > 0) {
            int sum = 0;
            for (int i = 1; i < frameLostLits.size(); i++) {
                sum += frameLostLits.get(i).intValue();
            }
            return sum / (frameLostLits.size() - 1);
        }
        return 0;
    }

    public void resetSignal() {
        if (mListener != null) {
            signalStrength = 1;
            mListener.OnGetSignal(signalStrength);
        }
        RPMModel rpmModel = new RPMModel();
        rpmModel.setRpm(0);
        //rpmModel.setSpeed(0);
        resetRPM(rpmModel);

        QOSModel qosModel = new QOSModel();
        qosModel.setRxvoltage(0);
        updateRxUI(qosModel);

        initAlarmColor();
    }


    TelemetryPack pack = new TelemetryPack();

    //    int display_Counter = -1;
    //RPMModel meanRPM;
    public void reserRPMModel() {
        RPMModel resetRpmModel = new RPMModel();
        resetRpmModel.setRpm(0);
        resetRpmModel.setTemperature(0.0f);
        resetRpmModel.setPackVolts(0.0f);
        updateRPMTelemetryData(resetRpmModel);
    }

    public void resetQOSModel() {
        QOSModel qosModel = new QOSModel();
        qosModel.setRxvoltage(0.0f);
        qosModel.setSignals(0);
        updateQosUI(qosModel);
    }

    public void resetOnDisconnection() {
        frameLostLits.clear();
        rpmModelList.clear();
        isGetSignal = false;
        qosTimeOut = 0;
        signalStrength = 1;
        if (mListener != null) {
            mListener.OnGetSignal(signalStrength);
        }
        isFirstRPM = true;
        firstTimeQOS = false;
        escWaitingTime = 0;

    }

    private void resetRPM(RPMModel rpmModel) {
        //isFirstRPM = false;
        //frameLostLits.clear();
        rpmModelList.clear();
        rpmModel.setRpm(0);
        updateRPMTelemetryData(rpmModel);
        //displayGpsSpeed(rpmModel);
//        isESC = false;
       /* if (isESC)
            setEscOff();*/
        if (mListener != null) {
            mListener.setEscIndicatorOff();
        }
    }


    private long rpmTimeOut = 0;
    //private long escTimeOut = 0;
    private boolean isFirstRPM = true;

    public void getRPMTelemetryData(byte[] data) {
        if (isESC == true) {
            return;
        }


        RPMModel rpmModel = pack.setRPMByteToViewMembers(data);
        if ((data[0] & 0xff) == 0x7e) {//RPM Time Out
            if (System.currentTimeMillis() - rpmTimeOut >= 1000 && !isFirstRPM) {
                isFirstRPM = false;
                rpmTimeOut = System.currentTimeMillis();
                resetRPM(rpmModel);
                Log.v("flicker", "rpmTimeOut");
                return;
            }
            isFirstRPM = false;
            rpmTimeOut = System.currentTimeMillis();
        }
       /* if (isGPS && System.currentTimeMillis() - gpsTimeOut > 500) {
            isGPS = false;
            if (mListener != null) {
                mListener.isGPSOn(false);
            }
        }*/
        if (System.currentTimeMillis() - qosTimeOut > 300 && signalStrength != 0) {
            signalStrength = 1;
            isGetSignal = false;
            if (mListener != null) {
                mListener.OnGetSignal(signalStrength);
            }
            Log.v("flicker", "QOSTimeOut");
            frameLostLits.clear();
            resetRPM(rpmModel);
            return;
        }

        if (isValidRPM(rpmModel) && (isGetSignal && signalStrength > 1)) {
            if (rpmModelList.size() < 15) {
                rpmModelList.add(0, rpmModel);
            } else {
                rpmModelList.add(0, rpmModel);
                RPMModel meanRPM = meanRPMTelemetry(rpmModelList);
                rpmModelList.remove(rpmModelList.size() - 1);
                if (meanRPM != null) {
                    meanRPM.setPoleCount(poleCount);
                    meanRPM.setRpm(meanRPM.calculateRPM());
                    Log.v("meanRPM", "" + meanRPM.getRpm());
                    updateRPMTelemetryData(meanRPM);
                }
            }
        } else {
            Log.v("flicker", "isValidRPM");
            resetRPM(rpmModel);
        }

    }

    private boolean isValidRPM(RPMModel rpmModel) {
        if (rpmModel.getMicroseconds() == 0 || rpmModel.getMicroseconds() == 65535) {
            return false;
        }
        return true;
    }


    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public boolean onLongClick(View v) {
        if (recording_button.isActivated()) return false;
        Intent intent = new Intent();
        intent.putExtra(Constants.IS_ESC, isESC);
        intent.setClass(getActivity(), TelemetrySetupActivity.class);
        switch (v.getId()) {
            case R.id.button_settings:
            case R.id.customSeekbarDrivePack:
                intent.putExtra(Constants.INDEX, 0);
                startActivity(intent);
                if (mListener != null) {
                    mListener.OnGaugeClick();
                }
                //silentAlarm();
                break;
            case R.id.customSeekbarVoltage:
                intent.putExtra(Constants.INDEX, 1);
                startActivity(intent);
                if (mListener != null) {
                    mListener.OnGaugeClick();
                }
                //silentAlarm();
                break;
            case R.id.speedoMeter:
                intent.putExtra(Constants.INDEX, 2);
                startActivity(intent);
                if (mListener != null) {
                    mListener.OnGaugeClick();
                }
                //silentAlarm();
                break;

            case R.id.tachoMeter:
                intent.putExtra(Constants.INDEX, 3);
                startActivity(intent);
                if (mListener != null) {
                    mListener.OnGaugeClick();
                }
                //silentAlarm();
                break;

            case R.id.customSeekbar_temp:
                intent.putExtra(Constants.INDEX, 4);
                startActivity(intent);
                if (mListener != null) {
                    mListener.OnGaugeClick();
                }
                //silentAlarm();
                break;
            case R.id.customSeekbarCurrent:
                intent.putExtra(Constants.INDEX, 1);
                startActivity(intent);
                if (mListener != null) {
                    mListener.OnGaugeClick();
                }
                //silentAlarm();
                break;
        }
        return true;
    }

    public void silentAlarm() {
        if (mAlarmServicer != null) {
            mAlarmServicer.silentAlarm();
        }
    }

    private void showSharePopUpDialog() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        SharePopUpFragmentDialog sharePopUpFragmentDialog = SharePopUpFragmentDialog.newInstance();
        sharePopUpFragmentDialog.setSharePopHandler(this);
//        sharePopUpFragmentDialog.setRuns(runsModels);
        sharePopUpFragmentDialog.show(fm, "SharePopUpFragmentDialog");
    }

    private boolean isDashboard;

    public void setIsDashboard(boolean isDashboard) {
        this.isDashboard = isDashboard;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.recording_button && (MainActivity.comms_ble == null || !MainActivity.isTelemetrySwitch)) {
//            Toast.makeText(getActivity(), "App is not connected to telemetry, please try again!", Toast.LENGTH_SHORT).show();
            showRunRecordingErrorDialog("", "Telemetry is not connected");
            return;
        }
        if (v.getId() != R.id.recording_button && recording_button.isActivated()) {
            Toast.makeText(getActivity(), "Screen changes are not allowed while capturing a run  ", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent();
        intent.setClass(getActivity(), TelemetrySetupActivity.class);
        switch (v.getId()) {
            case R.id.recording_button:
                recordRuns();
                break;
            case R.id.buttonReset:
                if (telemetrySettings != null && telemetrySettings[2] != null && telemetrySettings[2].display == 1) {
                    resetTopSpeed();
                }

                break;
            case R.id.share_button:
                showSharePopUpDialog();
                break;
            case R.id.button_settings:
                intent.putExtra(Constants.INDEX, 0);
                startActivity(intent);
                //silentAlarm();
                if (mListener != null) {
                    mListener.OnGaugeClick();
                }
                break;
            case R.id.customSeekbarDrivePack:
                Log.v("onClick", "customSeekbarDrivePack");
                if (MainActivity.firstTime_Alarm[0] == 2) {
                    if (MainActivity.telemetry_Tap_Alarm[0] == 0) {
                        MainActivity.telemetry_Tap_Alarm[0] = 1;//OFF
                        customSeekbarDrivePack.setBlinked(false);
                        silentAlarm();
                    } else {
                        MainActivity.telemetry_Tap_Alarm[0] = 0;//ON
                        customSeekbarDrivePack.setBlinked(true);
                    }
                }

                break;
            case R.id.customSeekbarVoltage:
                Log.v("onClick", "customSeekbarVoltage");
                if (MainActivity.firstTime_Alarm[1] == 2) {
                    if (MainActivity.telemetry_Tap_Alarm[1] == 0) {
                        MainActivity.telemetry_Tap_Alarm[1] = 1;//OFF
                        customSeekbarVoltage.setBlinked(false);
                        silentAlarm();
                    } else {
                        MainActivity.telemetry_Tap_Alarm[1] = 0;//ON
                        customSeekbarVoltage.setBlinked(true);
                    }
                }

                break;
            case R.id.speedoMeter:
                Log.v("onClick", "speedoMeter");
                if (MainActivity.firstTime_Alarm[2] == 2) {
                    if (MainActivity.telemetry_Tap_Alarm[2] == 0) {
                        MainActivity.telemetry_Tap_Alarm[2] = 1;//OFF
                        speedoMeter.setBlinked(false);
                        silentAlarm();
                    } else {
                        MainActivity.telemetry_Tap_Alarm[2] = 0;//ON
                        speedoMeter.setBlinked(true);
                    }
                }

                break;
            case R.id.tachoMeter:
                Log.v("onClick", "tachoMeter");
                if (MainActivity.firstTime_Alarm[3] == 2) {
                    if (MainActivity.telemetry_Tap_Alarm[3] == 0) {
                        MainActivity.telemetry_Tap_Alarm[3] = 1;//OFF
                        tachoMeter.setBlinked(false);
                        silentAlarm();
                    } else {
                        MainActivity.telemetry_Tap_Alarm[3] = 0;//ON
                        tachoMeter.setBlinked(true);
                    }
                }

                break;
            case R.id.customSeekbar_temp:
                Log.v("onClick", "customSeekbar_temp");
                if (MainActivity.firstTime_Alarm[4] == 2) {
                    if (MainActivity.telemetry_Tap_Alarm[4] == 0) {
                        MainActivity.telemetry_Tap_Alarm[4] = 1;//OFF
                        silentAlarm();
                        customSeekbar_temp.setBlinked(false);
                    } else {
                        MainActivity.telemetry_Tap_Alarm[4] = 0;//ON
                        customSeekbar_temp.setBlinked(true);
                    }
                }

                break;
            case R.id.customSeekbarCurrent:
                Log.v("onClick", "customSeekbarCurrent");
                if (MainActivity.firstTime_Alarm[5] == 2) {
                    if (MainActivity.telemetry_Tap_Alarm[5] == 0) {
                        MainActivity.telemetry_Tap_Alarm[5] = 1;//OFF
                        silentAlarm();
                        customSeekbarCurrent.setBlinked(false);
                    } else {
                        MainActivity.telemetry_Tap_Alarm[5] = 0;//ON
                        customSeekbarCurrent.setBlinked(true);
                    }
                }

                break;

        }
    }

    private void setBlinked() {
        if (MainActivity.telemetry_Tap_Alarm[5] == 1) {//OFF
            customSeekbarCurrent.setBlinked(false);
        } else {
            customSeekbarCurrent.setBlinked(true);
        }

        if (MainActivity.telemetry_Tap_Alarm[4] == 1) {//OFF
            customSeekbar_temp.setBlinked(false);
        } else {
            customSeekbar_temp.setBlinked(true);
        }

        if (MainActivity.telemetry_Tap_Alarm[3] == 1) {//OFF
            tachoMeter.setBlinked(false);
        } else {
            tachoMeter.setBlinked(true);
        }

        if (MainActivity.telemetry_Tap_Alarm[2] == 1) {//OFF
            speedoMeter.setBlinked(false);
        } else {
            speedoMeter.setBlinked(true);
        }

        if (MainActivity.telemetry_Tap_Alarm[1] == 1) {//OFF
            customSeekbarVoltage.setBlinked(false);
        } else {
            customSeekbarVoltage.setBlinked(true);
        }

        if (MainActivity.telemetry_Tap_Alarm[0] == 1) {//OFF
            customSeekbarDrivePack.setBlinked(false);
        } else {
            customSeekbarDrivePack.setBlinked(true);
        }
    }

    @Override
    public List<RunsModel> getRunList() {
        return runsModels;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getActionMasked();

        switch (action) {

            case MotionEvent.ACTION_DOWN:
                initialX = event.getX();
                initialY = event.getY();

                Log.d(TAG, "Action was DOWN");
                break;

            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "Action was MOVE");
                break;

            case MotionEvent.ACTION_UP:
                swipeLayout.performClick();
                float finalX = event.getX();
                float finalY = event.getY();

                Log.d(TAG, "Action was UP");
/*
                        if (initialX < finalX) {
                            Log.d(TAG, "Left to Right swipe performed");
                        }

                        if (initialX > finalX) {
                            Log.d(TAG, "Right to Left swipe performed");
                        }

                        if (initialY < finalY) {
                            Log.d(TAG, "Up to Down swipe performed");
                        }*/

                if (initialX > finalX || initialY > finalY || initialY < finalY) {
                    Log.d(TAG, "Down to Up swipe performed");
                    Toast.makeText(getActivity(), "Screen changes are not allowed while capturing a run  ", Toast.LENGTH_SHORT).show();
//                    showRunRecordingErrorDialog("Screen swipe not allowed", "Run capturing is recording, So user can not leave the Dashboard");
                }

                break;

            case MotionEvent.ACTION_CANCEL:
                Log.d(TAG, "Action was CANCEL");
                break;

            case MotionEvent.ACTION_OUTSIDE:
                Log.d(TAG, "Movement occurred outside bounds of current screen element");
                break;
        }

        return true;
    }

   /* private void setEscOn() {
        //changeUnits();
        tmpUnit = "A";
        textView_rx_voltage.setText("Current");
        //swithchRxVoltToCurrent();
        swithchToCurrent();
        customSeekbarVoltage.setProgress(0.0f, "A");
        customSeekbarVoltage.setMaxValue(telemetrySettings[1].maxRange);
    }*/

    private void setEscOff() {
        //changeUnits();
        //tmpUnit = voltage + "";
        //textView_rx_voltage.setText("RX Voltage");
        //swithchCurrentToRxVolt();
        // customSeekbarVoltage.setProgress(0.0f, "V");
        //customSeekbarVoltage.setMaxValue(telemetrySettings[1].maxRange);
        isESC = false;

        firstTimeQOS = false;
        escWaitingTime = 0;
    }

    public static boolean isESC;
    private boolean isGPS;
    private long gpsTimeOut;

    public void setGPS(boolean isGPS) {
        this.isGPS = isGPS;
    }

    public void getGPSTelemetryData(byte[] data) {
        if (mListener != null && !isGPS) {
            mListener.isGPSOn(true);
        }
        isGPS = true;
        gpsTimeOut = System.currentTimeMillis();
        RPMModel gpsModel = pack.setGPSByteToViewMembers(data);
        //updateSpeedByGPS(gpsModel);
        displayGpsSpeed(gpsModel);
    }

    private void displayGpsSpeed(RPMModel meanRPM) {
        if (telemetrySettings != null && telemetrySettings[2] != null) {
            //TelemetryRestore.topRPM = meanRPM.getRpm();
            String unitText = "";
            int speed;
            if (model.telemetryUnit == Model.METRIC) {
                unitText = "KPH";
                textView_MPH.setText("KPH");
                speed = Math.round(/*meanRPM.calMetricSpeed((float) meanRPM.getRpm(), telemetrySettings[2].impRollOut)*/meanRPM.getSpeed() * 1.852f);
                if (speed < 0) speed = -1 * speed;
                if (speed > telemetrySettings[2].maxRange) {
                    speedoMeter.setProgress(getProgress(telemetrySettings[2].maxRange, telemetrySettings[2].maxRange), (int) speed, unitText, -1);
                } else {
                    speedoMeter.setProgress(getProgress(speed, telemetrySettings[2].maxRange), speed, unitText, -1);
//                    TelemetryRestore.speed = speed;
//                    speedoMeter.setAlarmType(-1);
                    if (speed > TelemetryRestore.topSpeed) {
                        TelemetryRestore.speed = speed;
                        textView_topSpeed.setText(String.valueOf(/*Math.round(meanRPM.calMetricSpeed((float) meanRPM.getRpm(), telemetrySettings[2].impRollOut))*/speed) + " " + unitText);
                        //TelemetryRestore.topSpeed = previousTopSpeed;
                        storeRPMModelOnHighSpeed(meanRPM);
                    }
                }
            } else {
                unitText = "MPH";
                textView_MPH.setText("MPH");
                //topSpeed = (float) meanRPM.getRpm() * telemetrySettings[2].impRollOut / miles;
                speed = Math.round(/*meanRPM.calImpSpeed((float) meanRPM.getRpm(), telemetrySettings[2].impRollOut)*/meanRPM.getSpeed() * 1.15078f);
                if (speed < 0) speed = -1 * speed;
                if (speed > telemetrySettings[2].maxRange) {
                    speedoMeter.setProgress(getProgress(telemetrySettings[2].maxRange, telemetrySettings[2].maxRange), speed, unitText, -1);
                } else {
                    speedoMeter.setProgress(getProgress(speed, telemetrySettings[2].maxRange), speed, unitText, -1);
//                    TelemetryRestore.speed = speed;
//                    speedoMeter.setAlarmType(-1);
                    if (speed > TelemetryRestore.topSpeed) {
                        TelemetryRestore.topSpeed = speed;
                        textView_topSpeed.setText(String.valueOf(/*Math.round(meanRPM.calImpSpeed((float) meanRPM.getRpm(), telemetrySettings[2].impRollOut))*/speed) + " " + unitText);
                        //TelemetryRestore.topSpeed = previousTopSpeed;
                        /*Edited to store the runs of highest speed*/
                        storeRPMModelOnHighSpeed(meanRPM);
                    }
                }
            }


            if (BlueLayer.mConnectionState == BlueLayer.STATE_CONNECTED)
                playSpeedAlarm(speed);
        }
    }

    public void getESCTelemetryData(byte[] data) {
        //escTimeOut = System.currentTimeMillis();
        if (System.currentTimeMillis() - qosTimeOut > 300 && signalStrength != 0 && !isESC) {
            signalStrength = 1;
            isGetSignal = false;
            if (mListener != null) {
                mListener.OnGetSignal(signalStrength);
            }
            Log.v("flicker", "QOSTimeOut");
            frameLostLits.clear();
            //resetRPM(rpmModel);
            return;
        }
     /*   if (isGPS && System.currentTimeMillis() - gpsTimeOut > 500) {
            isGPS = false;
            if (mListener != null) {
                mListener.isGPSOn(false);
            }
        }*/
        if (isESC == false) {
            isESC = true;
            //customSeekbarVoltage.setProgress(0.0f, "A");
            /*switch the rx voltage to current*/
            //setEscOn();
            displayCurrent();
            if (mListener != null) {
                mListener.setEscIndicatorOn();
            }

        }

        RPMModel escModel = pack.setESCByteToViewMembers(data);
        escModel.setRpm(2*escModel.getRpm() / poleCount);
        //updateRPMTelemetryData(escModel);
        updateESCTelemetryData(escModel);
        //updateCurrentByEsc(escModel);

    }


    private void updateESCTelemetryData(RPMModel meanRPM) {
        displayEscTemp(meanRPM);
        displayPackVolt(meanRPM);
        displayTachoMeter(meanRPM);
        updateCurrent(meanRPM);
        if (!isGPS) {
            displaySpeed(meanRPM);
        }
    }

    private void displayTachoMeter(RPMModel meanRPM) {
        if (telemetrySettings != null && telemetrySettings[3] != null) {
            if (meanRPM.getRpm() > telemetrySettings[3].maxRange) {
                tachoMeter.setProgress(getProgress(telemetrySettings[3].maxRange, telemetrySettings[3].maxRange), (int) meanRPM.getRpm(), "RPM", -1);
            }/* else if (meanRPM.getRpm() <= 0.0f) {
                tachoMeter.setProgress(getProgress((float) meanRPM.getRpm(), telemetrySettings[3].maxRange), (int) meanRPM.getRpm(), "RPM", -1);
            } */ else {
                tachoMeter.setProgress(getProgress((float) meanRPM.getRpm(), telemetrySettings[3].maxRange), (int) meanRPM.getRpm(), "RPM", -1);
                TelemetryRestore.rpm = (int) meanRPM.getRpm();
                // tachoMeter.setProgress(getProgress((float) meanRPM.getRpm(), telemetrySettings[3].maxRange), (int) meanRPM.getRpm(), "RPM", -1);
            }
            if (BlueLayer.mConnectionState == BlueLayer.STATE_CONNECTED)
                playTachoAlarm(meanRPM);
        }
    }

    private void displayPackVolt(RPMModel meanRPM) {
        if (telemetrySettings != null && telemetrySettings[0] != null) {
            tmpUnit = voltage + "";
            if (meanRPM.getPackVolts() > telemetrySettings[0].maxRange) {
                customSeekbarDrivePack.setProgress(telemetrySettings[0].maxRange, tmpUnit);
            } else if (meanRPM.getPackVolts() <= 3.0f) {
                customSeekbarDrivePack.setProgress(3.0f, tmpUnit);
            } else {
                customSeekbarDrivePack.setProgress((float) (meanRPM.getPackVolts()), tmpUnit);
                TelemetryRestore.packVolts = meanRPM.getPackVolts();
//            customSeekbarDrivePack.setAlarmType(-1);
            }
            if (BlueLayer.mConnectionState == BlueLayer.STATE_CONNECTED)
                playPackVoltAlarm(meanRPM);
        }
    }

    private void displayEscTemp(RPMModel meanRPM) {
        if (model != null && model.telemetryUnit == Model.METRIC) {
            Log.v(TAG, "temperature C " + meanRPM.getTemperature());
//            Log.v("Telemetry", "telemetrySettings " + telemetrySettings);
            tmpUnit = degree + "C";
//            customSeekbar_temp.setAlarmType(-1);
            if (telemetrySettings != null && telemetrySettings[4] != null && meanRPM.getTemperature() > 0 && meanRPM.getTemperature() > telemetrySettings[4].maxRange) {
                customSeekbar_temp.setProgress(telemetrySettings[4].maxRange, tmpUnit);
            } else if (meanRPM.getTemperature() <= 0.0f) {
                customSeekbar_temp.setProgress(0, tmpUnit);
            } else {
                customSeekbar_temp.setProgress(meanRPM.getTemperature(), tmpUnit);
                TelemetryRestore.temperature = meanRPM.getTemperature();
                meanRPM.setTemperature(meanRPM.getTemperature());
            }

            playTempAlarm(meanRPM);
        } else if (telemetrySettings != null && telemetrySettings[4] != null) {
            tmpUnit = degree + "F";
            Log.v(TAG, "temperature F " + tempInFr(meanRPM.getTemperature()));
//            customSeekbar_temp.setAlarmType(-1);
            if (tempInFr(meanRPM.getTemperature()) > telemetrySettings[4].maxRange) {
                customSeekbar_temp.setProgress(telemetrySettings[4].maxRange, tmpUnit);
            } else if (meanRPM.getTemperature() <= 0.0f) {
                customSeekbar_temp.setProgress(0, tmpUnit);
            } else {
                customSeekbar_temp.setProgress(tempInFr(meanRPM.getTemperature()), tmpUnit);
                TelemetryRestore.temperature = tempInFr(meanRPM.getTemperature());
                meanRPM.setTemperature(tempInFr(meanRPM.getTemperature()));
//                customSeekbar_temp.setAlarmType(-1);
            }
            if (BlueLayer.mConnectionState == BlueLayer.STATE_CONNECTED)
                playTempAlarm(meanRPM);
        }
    }

    private void displaySpeed(RPMModel meanRPM) {
        if (telemetrySettings != null && telemetrySettings[2] != null) {
            //TelemetryRestore.topRPM = meanRPM.getRpm();
            String unitText = "";
            int speed;
            if (model.telemetryUnit == Model.METRIC) {
                unitText = "KPH";
                textView_MPH.setText("KPH");
                speed = Math.round(meanRPM.calMetricSpeed((float) meanRPM.getRpm(), telemetrySettings[2].impRollOut));
                if (speed < 0) speed = -1 * speed;
                if (speed > telemetrySettings[2].maxRange) {
                    speedoMeter.setProgress(getProgress(telemetrySettings[2].maxRange, telemetrySettings[2].maxRange), (int) speed, unitText, -1);
                } else {
                    speedoMeter.setProgress(getProgress(speed, telemetrySettings[2].maxRange), speed, unitText, -1);
                    //TelemetryRestore.speed = speed;
//                    speedoMeter.setAlarmType(-1);
                    if (meanRPM.getRpm() > TelemetryRestore.topRPM) {
                        TelemetryRestore.topRPM = meanRPM.getRpm();
                        textView_topSpeed.setText(String.valueOf(Math.round(meanRPM.calMetricSpeed((float) meanRPM.getRpm(), telemetrySettings[2].impRollOut))) + " " + unitText);
                        //TelemetryRestore.topSpeed = previousTopSpeed;
                        storeRPMModelOnHighSpeed(meanRPM);
                    }
                }
            } else {
                unitText = "MPH";
                textView_MPH.setText("MPH");
                //topSpeed = (float) meanRPM.getRpm() * telemetrySettings[2].impRollOut / miles;
                speed = Math.round(meanRPM.calImpSpeed((float) meanRPM.getRpm(), telemetrySettings[2].impRollOut));
                if (speed < 0) speed = -1 * speed;
                if (speed > telemetrySettings[2].maxRange) {
                    speedoMeter.setProgress(getProgress(telemetrySettings[2].maxRange, telemetrySettings[2].maxRange), speed, unitText, -1);
                } else {
                    speedoMeter.setProgress(getProgress(speed, telemetrySettings[2].maxRange), speed, unitText, -1);
                    //TelemetryRestore.speed = speed;
//                    speedoMeter.setAlarmType(-1);
                    if (meanRPM.getRpm() > TelemetryRestore.topRPM) {
                        TelemetryRestore.topRPM = meanRPM.getRpm();
                        textView_topSpeed.setText(String.valueOf(Math.round(meanRPM.calImpSpeed((float) meanRPM.getRpm(), telemetrySettings[2].impRollOut))) + " " + unitText);
                        //TelemetryRestore.topSpeed = previousTopSpeed;
                        /*Edited to store the runs of highest speed*/
                        storeRPMModelOnHighSpeed(meanRPM);
                    }
                }
            }


            if (BlueLayer.mConnectionState == BlueLayer.STATE_CONNECTED)
                playSpeedAlarm(speed);
        }
    }

    private void updateCurrent(RPMModel escModel) {
        tmpUnit = "A";
        if (telemetrySettings != null && telemetrySettings[5] != null && escModel.getCurrentMotor() > telemetrySettings[5].maxRange) {
            customSeekbarCurrent.setProgress(telemetrySettings[5].maxRange, tmpUnit);
        } else if (escModel.getCurrentMotor() <= 0.0f) {
            customSeekbarCurrent.setProgress(0.0f, tmpUnit);
        } else {
            customSeekbarCurrent.setProgress(escModel.getCurrentMotor(), tmpUnit);
            TelemetryRestore.current = escModel.getCurrentMotor();
        }
        if (BlueLayer.mConnectionState == BlueLayer.STATE_CONNECTED)
            playCurrentAlarm(escModel);

    }


    public void setESCOnDisconnect(boolean isAreadyESC) {
        Log.v(TAG, "setESCOnDisconnect " + this.isESC);
        if (this.isESC) {
            setEscOff();
            displayRxVolt();
        }
    }

/*    public void switchESCSensor(boolean isESC) {
        this.isESC = isESC;
        if (isESC) {
            swithchRxVoltToCurrent();//TODO esc sensor
        } else {
            swithchCurrentToRxVolt();
        }
        // changeUnits();
        customSeekbarVoltage.setProgress(TelemetryRestore.current, tmpUnit);
        customSeekbarVoltage.setMaxValue(telemetrySettings[1].maxRange);

    }*/
/*
    public void swithchToCurrent() {
        if (telemetrySettings[1] != null) {
            float tmp = telemetrySettings[1].maxRange;
            telemetrySettings[1].maxRange = QuickstartPreferences.getCurrUpper(getContext());
            QuickstartPreferences.setCurrUpper(getContext(), tmp);

            tmp = telemetrySettings[1].alarmLow;
            telemetrySettings[1].alarmLow = QuickstartPreferences.getCurrLow(getContext());
            QuickstartPreferences.setCurrLow(getContext(), tmp);


            tmp = telemetrySettings[1].alarmHigh;
            telemetrySettings[1].alarmHigh = QuickstartPreferences.getCurrHigh(getContext());
            QuickstartPreferences.setCurrHigh(getContext(), tmp);
        }
    }

    public void swithchToRx() {
        if (telemetrySettings[1] != null) {
            QuickstartPreferences.setCurrUpper(getContext(), telemetrySettings[1].maxRange);
            telemetrySettings[1].maxRange = QuickstartPreferences.getCurrUpper(getContext());

            QuickstartPreferences.setCurrLow(getContext(), telemetrySettings[1].alarmLow);
            telemetrySettings[1].alarmLow = QuickstartPreferences.getCurrLow(getContext());

            QuickstartPreferences.setCurrHigh(getContext(), telemetrySettings[1].alarmHigh);
            telemetrySettings[1].alarmHigh = QuickstartPreferences.getCurrHigh(getContext());*//*telemetrySettings[1].alarmLow =*//*
        }
    }*/



    /*private void changeUnits() {
        if (isESC) {
            tmpUnit = "A";
            textView_rx_voltage.setText("Current");
        } else {
            tmpUnit = voltage + "";
            textView_rx_voltage.setText("RX Voltage");
        }
    }*/


    public void swithchCurrentToRxVolt() {
        if (telemetrySettings[1] != null) {
            Log.v(TAG, "maxRange swithchCurrentToRxVolt before " + telemetrySettings[1].maxRange);
            Log.v(TAG, "alarmLow swithchCurrentToRxVolt before " + telemetrySettings[1].alarmLow);
            Log.v(TAG, "alarmHigh swithchCurrentToRxVolt before " + telemetrySettings[1].alarmHigh);
            telemetrySettings[1].maxRange = switchValues(telemetrySettings[1].maxRange, 0.0f, 200.0f, 4.0f, 10.0f);
            telemetrySettings[1].alarmLow = switchValues(telemetrySettings[1].alarmLow, 0.0f, 200.0f, 0.0f, 10.0f);
            telemetrySettings[1].alarmHigh = switchValues(telemetrySettings[1].alarmHigh, 0.0f, 200.0f, 0.0f, 10.0f);

            Log.v(TAG, "maxRange swithchCurrentToRxVolt after " + telemetrySettings[1].maxRange);
            Log.v(TAG, "alarmLow swithchCurrentToRxVolt after " + telemetrySettings[1].alarmLow);
            Log.v(TAG, "alarmHigh swithchCurrentToRxVolt after " + telemetrySettings[1].alarmHigh);
        }
    }

    private void swithchRxVoltToCurrent() {
        if (telemetrySettings[1] != null) {
            Log.v(TAG, "maxRange swithchRxVoltToCurrent before " + telemetrySettings[1].maxRange);
            Log.v(TAG, "alarmLow swithchRxVoltToCurrent before " + telemetrySettings[1].alarmLow);
            Log.v(TAG, "alarmHigh swithchRxVoltToCurrent before " + telemetrySettings[1].alarmHigh);

            telemetrySettings[1].maxRange = switchValues(telemetrySettings[1].maxRange, 4.0f, 10.0f, 0.0f, 200.0f);
            telemetrySettings[1].alarmLow = switchValues(telemetrySettings[1].alarmLow, 0.0f, 10.0f, 0.0f, 200.0f);
            telemetrySettings[1].alarmHigh = switchValues(telemetrySettings[1].alarmHigh, 0.0f, 10.0f, 0.0f, 200.0f);

            Log.v(TAG, "maxRange swithchRxVoltToCurrent after " + telemetrySettings[1].maxRange);
            Log.v(TAG, "alarmLow swithchRxVoltToCurrent after " + telemetrySettings[1].alarmLow);
            Log.v(TAG, "alarmHigh swithchRxVoltToCurrent after " + telemetrySettings[1].alarmHigh);
        }
    }

    private float switchValues(float value, float src_min, float src_max, float des_min, float des_max) {
        float percentage = ((value - src_min) / (src_max - src_min)) * 100;
        float newValue = (((percentage * (des_max - des_min) / 100.0f)) + des_min);
        return Float.valueOf(String.format("%.1f", newValue));
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);

        void setEscOn();

        void setEscIndicatorOff();

        void setEscIndicatorOn();

        void isGPSOn(boolean isGPS);


        public void OnTelemetryVisible();

        public void OnGetSignal(int signalStrength);

        public void OnGaugeClick();

        void runsRecording(boolean isRunsRecording);

    }

    private void initHeaderFont(View view, int width) {
        LinearLayout layout_speedometer = (LinearLayout) view.findViewById(R.id.layout_speedometer);
        Log.d(TAG, "onCreateView: width " + layout_speedometer.getLayoutParams().width + " height " + layout_speedometer.getLayoutParams().height);
        layout_speedometer.getLayoutParams().width = width > 1500 ? width * 45 / 100 : width * 55 / 100;

        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/ChoplinMedium.otf");
        LinearLayout layout = (LinearLayout) view.findViewById(R.id.activity_main);
        layout.setBackgroundColor(Color.parseColor("#00000000"));
        view.findViewById(R.id.customSeekbarDrivePack).setOnLongClickListener(this);
        button_settings = view.findViewById(R.id.button_settings);
        button_settings.setOnLongClickListener(this);
        view.findViewById(R.id.button_settings).setOnClickListener(this);

        TextView txt_top_speed = (TextView) view.findViewById(R.id.txt_top_speed);
        TextView textView_topSpeed = (TextView) view.findViewById(R.id.textView_topSpeed);
        TextView textView_MPH = (TextView) view.findViewById(R.id.textView_MPH);
        TextView txt_reset = (TextView) view.findViewById(R.id.txt_reset);
        TextView textViewTemparature = (TextView) view.findViewById(R.id.textViewTemparature);
        TextView textView_rx_voltage = (TextView) view.findViewById(R.id.textView_rx_voltage);
        TextView textView_current = (TextView) view.findViewById(R.id.textView_current);
        TextView txt_drivepack = (TextView) view.findViewById(R.id.txt_drivepack);
        txt_top_speed.setTypeface(typeface);
        textView_topSpeed.setTypeface(typeface);
        textView_MPH.setTypeface(typeface);

        textViewTemparature.setTypeface(typeface);
        textView_rx_voltage.setTypeface(typeface);
        textView_current.setTypeface(typeface);
        txt_drivepack.setTypeface(typeface);

        typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/HelveticaNeueLTStd55Roman.otf");
        txt_reset.setTypeface(typeface);


    }

    private AlertDialog showAlarmDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle("Telemetry Alarm");

        alertDialogBuilder
                .setMessage("Do you want to keep on?")
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        alarms.stopAlarm();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alarmAlertDialog = alertDialogBuilder.create();
        alarmAlertDialog.setOnDismissListener(null);
        alarmAlertDialog.setCanceledOnTouchOutside(false);
        alarmAlertDialog.show();
        return alarmAlertDialog;
    }
}
