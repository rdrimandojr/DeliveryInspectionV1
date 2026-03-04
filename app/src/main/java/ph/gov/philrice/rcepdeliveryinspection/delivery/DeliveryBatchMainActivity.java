package ph.gov.philrice.rcepdeliveryinspection.delivery;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.Result;
import com.google.zxing.client.android.Intents;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.kunzisoft.switchdatetime.SwitchDateTimeDialogFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import in.galaxyofandroid.spinerdialog.OnSpinerItemClick;
import in.galaxyofandroid.spinerdialog.SpinnerDialog;
import ph.gov.philrice.rcepdeliveryinspection.Fun;
import ph.gov.philrice.rcepdeliveryinspection.R;
import ph.gov.philrice.rcepdeliveryinspection.dbase.database.RCEPDatabase;
import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.LibSeeds;
import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TblCurrentDeliveryCount;
import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TmpDeliveryBatchData;
import ph.gov.philrice.rcepdeliveryinspection.delivery.dataview.DeliveryBatchAdapter;
import ph.gov.philrice.rcepdeliveryinspection.delivery.dataview.DeliveryBatchData;
import ph.gov.philrice.rcepdeliveryinspection.objects.CoopDropoffPoint;
import ph.gov.philrice.rcepdeliveryinspection.objects.RlaDetails;
import ph.gov.philrice.rcepdeliveryinspection.objects.SeedGrower;

public class DeliveryBatchMainActivity extends AppCompatActivity
        implements DeliveryBatchAdapter.ItemClicked {
    private static final String TAG = "DeliveryBatchMainActivi";

    //Views
    TextInputLayout til_varietyBalance, til_transpo_cost;
    LinearLayout delivery_form;
    FrameLayout content_frame;
    RecyclerView rv_batchDelivery;
    TextView tv_remainingVolume;
    //private CodeScanner mCodeScanner;
    ImageView imgvw_deliveryDate, imgv_coopId, imgv_delCoopDropoffpoint, imgv_coopDropoffpoint,
            imgv_removeSeries;
    EditText /*et_deliverySched,*/ et_coopId, et_seedVariety, et_labNo, et_lotNo, et_bagsDelivered,
            et_coopDropoffpoint, et_batchSeries, et_seedTag, et_seedGrower, et_transpo_cost_per_bag;
    SwitchDateTimeDialogFragment dateTimeDialogFragment;
    ProgressDialog mProgressDialog;

    //Variables
    ArrayList<DeliveryBatchData> deliveryBatchData;
    DeliveryBatchAdapter deliveryBatchAdapter;
    SharedPreferences prefUserAccount;
    int glbl_dropoffpointId, glbl_rla_bag_limit, glbl_instructeDeliveryVolume, glbl_sgid,
            glbl_varietyBalance, glbl_misbuffer;
    String glbl_batchTicketNumber, glbl_coopAccreditation, glbl_moaNumber, glbl_prvdropoffId,
            glbl_region, glbl_province, glbl_municipality, glbl_coopdropoffpoint, glbl_prv,
            glbl_variety, glbl_seedTag, glbl_deliveryDate, glbl_userRole, glbl_domain, glbl_port;
    RCEPDatabase rcepDatabase;
    ArrayList<String> region;
    ArrayList<String> province;
    ArrayList<String> municipality;
    ArrayList<String> dropoffPoint;
    List<DropoffPoint> mDropoffPoint;
    List<CoopDropoffPoint> coopdropoff_source;
    ArrayList<String> coopdropoff_selection;
    ArrayList<String> coopdropoff_selection_grower;
    ArrayList<String> coopdropoff_selection_dmanager;
    List<RlaDetails> rlaDetails_source;
    ArrayList<String> rlaDetails_selection;
    List<SeedGrower> seedGrower_source;
    ArrayList<String> seedGrower_selection;

    String glbl_season;

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher =
            registerForActivityResult(new ScanContract(),
                    result -> {
                        if (result.getContents() == null) {
                            Intent originalIntent = result.getOriginalIntent();
                            if (originalIntent == null) {
                                Toast.makeText(DeliveryBatchMainActivity.this, "Cancelled",
                                        Toast.LENGTH_LONG).show();
                            } else if (originalIntent.hasExtra(
                                    Intents.Scan.MISSING_CAMERA_PERMISSION)) {
                                Toast.makeText(DeliveryBatchMainActivity.this,
                                        "Cancelled due to missing camera permission, Please enable in settings.",
                                        Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Log.e(TAG, "data: " + result.getContents());
                            DeliveryBatchMainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    et_batchSeries.setText("");
                                    final String[] qrDecode = result.getContents().split("-");
                                    if (qrDecode.length == 2) {
                                        if (qrDecode[0].length() == 10 &&
                                                qrDecode[1].length() == 3) {
                                            //check if series has WS or DS
                                            //et_batchSeries.setText(qrDecode[0]);
                                            String mSeriesSeason =
                                                    qrDecode[0].substring(0, 2).toLowerCase();
                                            if (mSeriesSeason.equals("ws") ||
                                                    mSeriesSeason.equals("ds")) {
                                                //setting visibility of removing series
                                                imgv_removeSeries.setVisibility(View.VISIBLE);
                                                et_batchSeries.setText(qrDecode[0]);
                                            } else {
                                                imgv_removeSeries.setVisibility(View.GONE);
                                                et_batchSeries.setText("");
                                                Toast.makeText(DeliveryBatchMainActivity.this,
                                                                "Invalid series format", Toast.LENGTH_SHORT)
                                                        .show();
                                            }
                                        } else {
                                            imgv_removeSeries.setVisibility(View.GONE);
                                            et_batchSeries.setText("");
                                            Toast.makeText(DeliveryBatchMainActivity.this,
                                                            "Invalid series format", Toast.LENGTH_SHORT)
                                                    .show();
                                        }
                                    } else {
                                        imgv_removeSeries.setVisibility(View.GONE);
                                        et_batchSeries.setText("");
                                        Toast.makeText(DeliveryBatchMainActivity.this,
                                                "Invalid series format",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //initialize screen
        Fun.fullScreen(this);//fullScreen
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        //getSupportActionBar().hide(); // hide the title bar
        //setScreenActivityContents
        setContentView(R.layout.activity_delivery_batch_main);

        initDeliveryBatch();

        //refresh droppoint library
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //requestDropoffPointLib(DeliveryBatchMainActivity.this);

                requestCoopDropoffPointLib(DeliveryBatchMainActivity.this, glbl_coopAccreditation,
                        glbl_moaNumber);
            }
        }, 100);


        /*String input = "1.3";
        if (input.equals("") || input.equals(".")) {
            input = "0";
        } else {
            input = String.format("%.2f", input);
        }
        Log.e(TAG, "input: " + input);*/


    }

    void initDeliveryBatch() {
        prefUserAccount = getApplicationContext()
                .getSharedPreferences(Fun.userAccountPreference(), Context.MODE_PRIVATE);
        //initialize dropoffPointId
        glbl_season = "";
        glbl_varietyBalance = 0;
        mProgressDialog = new ProgressDialog(this);
        deliveryBatchData = new ArrayList<>();
        rcepDatabase = RCEPDatabase.getAppDatabase(this);

        glbl_domain = prefUserAccount.getString(Fun.uaStationDomain(), "");
        glbl_port = prefUserAccount.getString(Fun.uaStationPort(), "");

        glbl_userRole = prefUserAccount.getString(Fun.uaUserRoles(), "");
        glbl_coopAccreditation = prefUserAccount.getString(Fun.uaCoopAccreditation(), "");
        glbl_moaNumber = prefUserAccount.getString(Fun.uaCoopCurrentMOA(), "");
        glbl_season = prefUserAccount.getString(Fun.uaSeason(), "");
        glbl_instructeDeliveryVolume = 0;
        glbl_rla_bag_limit = 0;
        til_transpo_cost = findViewById(R.id.til_transpo_cost);
        til_varietyBalance = findViewById(R.id.til_varietyBalance);
        et_coopDropoffpoint = findViewById(R.id.et_coopDropoffpoint);
        delivery_form = findViewById(R.id.delivery_form);
        content_frame = findViewById(R.id.content_frame);
        imgv_coopDropoffpoint = findViewById(R.id.imgv_coopDropoffpoint);
//        et_deliverySched = findViewById(R.id.et_deliverySched);
        imgv_delCoopDropoffpoint = findViewById(R.id.imgv_delCoopDropoffpoint);
        imgv_coopId = findViewById(R.id.imgv_coopId);
        imgv_removeSeries = findViewById(R.id.imgv_removeSeries);
        et_transpo_cost_per_bag = findViewById(R.id.et_transpo_cost_per_bag);
        et_coopId = findViewById(R.id.et_coopId);
        et_seedVariety = findViewById(R.id.et_seedVariety);
        et_seedGrower = findViewById(R.id.et_seedGrower);
        et_seedTag = findViewById(R.id.et_seedTag);
        tv_remainingVolume = findViewById(R.id.tv_remainingVolume);
        et_labNo = findViewById(R.id.et_labNo);
        et_lotNo = findViewById(R.id.et_lotNo);
        et_bagsDelivered = findViewById(R.id.et_bagsDelivered);
        rv_batchDelivery = findViewById(R.id.rv_batchDelivery);
        //imgvw_deliveryDate = findViewById(R.id.imgvw_deliveryDate);
        et_batchSeries = findViewById(R.id.et_batchSeries);
        region = new ArrayList<>();
        province = new ArrayList<>();
        municipality = new ArrayList<>();
        dropoffPoint = new ArrayList<>();
        mDropoffPoint = new ArrayList<>();

        coopdropoff_source = new ArrayList<>();
        coopdropoff_selection = new ArrayList<>();

        rlaDetails_source = new ArrayList<>();
        rlaDetails_selection = new ArrayList<>();

        seedGrower_source = new ArrayList<>();
        seedGrower_selection = new ArrayList<>();

        //region = getRegions();
        //creating batchTicketNumber
        //glbl_batchTicketNumber = prefUserAccount.getInt(Fun.uaUserId(), 0) + "-" + "BCH" + "-" + Fun.getTimestamp();

        //Initialize CodeScannerView
        /*CodeScannerView scannerView = findViewById(R.id.scanner_view);
        mCodeScanner = new CodeScanner(this, scannerView);
        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                DeliveryBatchMainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Log.e(TAG, "run: " + result.toString());

                        //setText
                      *//*  et_coopId.setText("");
                        et_coopId.setText(result.toString());*//*
                        et_batchSeries.setText("");
                        //et_batchSeries.setText(result.toString());
                        final String[] qrDecode = result.toString().split("-");
                        //Log.e(TAG, "run: " + qrDecode.length);
                        if (qrDecode.length == 2) {
                            if (qrDecode[0].length() == 10 && qrDecode[1].length() == 3) {
                                //check if series has WS or DS
                                //et_batchSeries.setText(qrDecode[0]);
                                String mSeriesSeason = qrDecode[0].substring(0, 2).toLowerCase();
                                if (mSeriesSeason.equals("ws") || mSeriesSeason.equals("ds")) {
                                    //setting visibility of removing series
                                    imgv_removeSeries.setVisibility(View.VISIBLE);
                                    et_batchSeries.setText(qrDecode[0]);
                                } else {
                                    imgv_removeSeries.setVisibility(View.GONE);
                                    et_batchSeries.setText("");
                                    Toast.makeText(DeliveryBatchMainActivity.this,
                                            "Invalid series format", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                imgv_removeSeries.setVisibility(View.GONE);
                                et_batchSeries.setText("");
                                Toast.makeText(DeliveryBatchMainActivity.this,
                                        "Invalid series format", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            imgv_removeSeries.setVisibility(View.GONE);
                            et_batchSeries.setText("");
                            Toast.makeText(DeliveryBatchMainActivity.this, "Invalid series format",
                                    Toast.LENGTH_SHORT).show();
                        }


                        //after scan function
                        *//*content_frame.setVisibility(View.GONE);
                        delivery_form.setVisibility(View.VISIBLE);
                        mCodeScanner.stopPreview();
                        mCodeScanner.releaseResources();*//*
                    }
                });
            }
        });*/
        //set properties
        rv_batchDelivery.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rv_batchDelivery.setItemAnimator(new DefaultItemAnimator());
        deliveryBatchAdapter = new DeliveryBatchAdapter(this, deliveryBatchData);
        deliveryBatchAdapter.setitemClickedListener(this);
        rv_batchDelivery.setAdapter(deliveryBatchAdapter);


        //clear values of tmpTable
        rcepDatabase.tmpDeliveryBatchDataDAO().nukeTable();
    }

    int deliveryBatchCount() {
        return deliveryBatchData.size();
    }

    int verifyForm() {
        int ctr = 0;
        if (/*et_deliverySched.getText().toString().equals("") ||*/
                et_coopDropoffpoint.getText().toString().trim().equals("") ||
                        et_seedVariety.getText().toString().trim().equals("") ||
                        et_seedTag.getText().toString().trim().equals("") ||
                        et_transpo_cost_per_bag.getText().toString().trim().equals("") ||
             /*   et_labNo.getText().toString().trim().equals("") ||
                et_lotNo.getText().toString().trim().equals("") ||*/
                        et_bagsDelivered.getText().toString().trim().equals("")) {
            ctr++;
        }

        if (et_transpo_cost_per_bag.equals("") || et_transpo_cost_per_bag.equals(".")) {
            ctr++;
            til_transpo_cost.setError(
                    "Invalid cost. Must have at least a(1) number and max of two(2) decimal places");
        } else {
            if (Fun.isCostValid(et_transpo_cost_per_bag.getText().toString().trim()) == 1) {
                til_transpo_cost.setError(null);
            } else {
                ctr++;
                til_transpo_cost.setError(
                        "Invalid cost. Must have at least a(1) number and max of two(2) decimal places");
            }
        }
        //optional for now
        /*if (et_batchSeries.getText().toString().trim().equals("")) {
            ctr++;
        }*/
        return ctr;
    }

   /* public void setDeliveryDate(View view) {
        Fun.hideKeyboard(this);
        final SimpleDateFormat myDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
        dateTimeDialogFragment = SwitchDateTimeDialogFragment.newInstance(
                "Set delivery date",
                "Set",
                "Cancel",
                "Clear"
        );
        dateTimeDialogFragment.setTimeZone(TimeZone.getDefault());
        //dateTimeDialogFragment.set24HoursMode(true);
        //dateTimeDialogFragment.setMinimumDateTime(Calendar.getInstance().getTime());
        dateTimeDialogFragment.startAtCalendarView();
        dateTimeDialogFragment.setHighlightAMPMSelection(true);
        dateTimeDialogFragment.setOnButtonClickListener(new SwitchDateTimeDialogFragment.OnButtonWithNeutralClickListener() {

            @Override
            public void onPositiveButtonClick(Date date) {
                et_deliverySched.setText(String.valueOf(myDateFormat.format(date)));
            }

            @Override
            public void onNegativeButtonClick(Date date) {
                dateTimeDialogFragment.dismiss();
            }

            @Override
            public void onNeutralButtonClick(Date date) {
                et_deliverySched.setText("");

            }
        });
        dateTimeDialogFragment.show(getSupportFragmentManager(), "dialog_time");
    }
*/

    public void scanIdCoop(View view) {
        Fun.hideKeyboard(this);
        /*content_frame.setVisibility(View.VISIBLE);
        delivery_form.setVisibility(View.GONE);
        mCodeScanner.startPreview();*/
        barcodeLauncher.launch(new ScanOptions());
    }


    public void setSeedVariety(View view) {
        ArrayList<String> mseeds = getSeeds();

        if (et_coopDropoffpoint.getText().toString().trim().length() > 0) {
            if (mseeds.size() > 0) {
                clearFocus();
                final Handler handler = new Handler();
                SpinnerDialog ss = new SpinnerDialog(DeliveryBatchMainActivity.this, mseeds,
                        "Select Variety");
                ss.setCancellable(false);
                ss.setShowKeyboard(false);
                ss.bindOnSpinerListener(new OnSpinerItemClick() {
                    @Override
                    public void onClick(String item, int position) {
                        /*set values here*/
                        til_varietyBalance.setHelperText("Remaining balance : fetching result");

                        glbl_variety = item;
                        //logging data
                   /* Log.e(TAG, "variety : " + et_seedVariety.getText().toString().trim() +
                            "\n coop_accred : " + glbl_coopAccreditation +
                            "\n moa_number : " + glbl_moaNumber);*/
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                et_seedVariety.setText(glbl_variety);
                                et_seedGrower.setText("");
                                et_seedTag.setText("");
                                et_bagsDelivered.setText("");
                                /*clear arrays*/
                                seedGrower_selection.clear();
                                seedGrower_source.clear();
                                rlaDetails_selection.clear();
                                rlaDetails_source.clear();
                                /*request seed growers*/
                                requestCoopSeedGrowers(DeliveryBatchMainActivity.this,
                                        glbl_coopAccreditation, glbl_moaNumber, glbl_variety);
                            }
                        }, 250);
                    }
                });
                ss.showSpinerDialog();
            } else {
                Toast.makeText(this, "No committed variety yet.", Toast.LENGTH_LONG).show();
            }
        } else {

            Toast.makeText(this, "Please select batch ticket first", Toast.LENGTH_LONG).show();
        }


    }


    void disableViews() {

        //  imgvw_deliveryDate.setEnabled(false);
//        imgv_delCoopDropoffpoint.setEnabled(false);
        imgv_coopDropoffpoint.setEnabled(false);
        et_coopDropoffpoint.setTextColor(getResources().getColor(R.color.pending));

        et_transpo_cost_per_bag.setEnabled(false);
//        et_deliverySched.setTextColor(getResources().getColor(R.color.pending));


        //deliveryDate
      /*  imgvw_deliveryDate.setEnabled(false);
        et_deliverySched.setTextColor(getResources().getColor(R.color.pending));

        imgv_region.setEnabled(false);
        imgv_province.setEnabled(false);
        imgv_municipality.setEnabled(false);
        imgv_dropoffPoint.setEnabled(false);
        et_otherDropoffPoint.setEnabled(false);
        et_coopId.setEnabled(false);
        imgv_coopId.setEnabled(false);


        et_otherDropoffPoint.setTextColor(getResources().getColor(R.color.pending));
        et_coopId.setTextColor(getResources().getColor(R.color.pending));*/
    }

    void enableViews() {

        // imgvw_deliveryDate.setEnabled(true);
//        imgv_delCoopDropoffpoint.setEnabled(true);
        imgv_coopDropoffpoint.setEnabled(true);
//        et_deliverySched.setTextColor(getResources().getColor(R.color.black));
        et_coopDropoffpoint.setTextColor(getResources().getColor(R.color.black));
        et_transpo_cost_per_bag.setEnabled(true);
        //deliveryDate
      /*  imgvw_deliveryDate.setEnabled(true);
        et_deliverySched.setTextColor(getResources().getColor(R.color.black));

        imgv_region.setEnabled(true);
        imgv_province.setEnabled(true);
        imgv_municipality.setEnabled(true);
        imgv_dropoffPoint.setEnabled(true);
        et_coopId.setEnabled(true);
        imgv_coopId.setEnabled(true);

        et_otherDropoffPoint.setTextColor(getResources().getColor(R.color.black));
        et_coopId.setTextColor(getResources().getColor(R.color.black));*/
    }

    void clearFocus() {
        et_labNo.clearFocus();
        et_lotNo.clearFocus();
        et_bagsDelivered.clearFocus();
        et_batchSeries.clearFocus();
        //et_transpo_cost_per_bag.clearFocus();
        //et_coopId.clearFocus();
    }

    public void nextLot(View view) {

        if (verifyForm() > 0) {
            Toast.makeText(this, "Please complete form before moving to next lot",
                    Toast.LENGTH_LONG).show();
        } else {

            final String mTicketNumber = prefUserAccount
                    .getInt(Fun.uaUserId(), 0) + "-" + "LT" + "-" + Fun.getTimestamp();
            final String mBatchTicketNumber = glbl_batchTicketNumber;
            final String mDeliveryDate = glbl_deliveryDate;
            // String mDeliveryDate = et_deliverySched.getText().toString().trim();
            /* String mDeliveryDate = et_deliverySched.getText().toString().trim();*/
            /*  String mDeliverTo = et_deliveryTo.getText().toString().trim();*/
            final String mCoopAccreditation = glbl_coopAccreditation;
            //String mSeedVariety = et_seedVariety.getText().toString().trim();
            final String mSeedVariety = glbl_variety;
            final String mSeedClass = "Certified";
            //String mSeedTag = (et_labNo.getText().toString().trim() + "/" + et_lotNo.getText().toString().trim()).toUpperCase();
            final String mSeedTag = glbl_seedTag;
            final int mTotalBagCount = Integer
                    .parseInt(et_bagsDelivered.getText().toString().trim());
            final String mDateCreated = Fun.getCurrentDate();
            final int mUserId = prefUserAccount.getInt(Fun.uaUserId(), 0);
            final String mRegion = glbl_region;
            final String mProvince = glbl_province;
            final String mMunicipality = glbl_municipality;
            final String mCoopdropoffpoint = glbl_coopdropoffpoint;
            final String mPrvDropoffId = glbl_prvdropoffId;
            final String mPrv = glbl_prv;

            final String mSeries = et_batchSeries.getText().toString().trim();
            final String mMoaNumber = prefUserAccount.getString(Fun.uaCoopCurrentMOA(), "");
            final String mAppVersion = Fun.appVersion();
            final String mSgId = String.valueOf(glbl_sgid);
            final int misBuffer = glbl_misbuffer;

            final double transpo_cost_per_bag =
                    Double.parseDouble(et_transpo_cost_per_bag.getText().toString());

            Log.e(TAG, "transpo_cost_per_bag: " + transpo_cost_per_bag);

            if (rcepDatabase.tmpDeliveryBatchDataDAO().seedTagCount(mSeedTag) > 0) {
                Toast.makeText(this, "Seed tag already exist on the list.", Toast.LENGTH_SHORT)
                        .show();
            } else {
                new MaterialAlertDialogBuilder(DeliveryBatchMainActivity.this)
                        .setTitle("RCEF DI")
                        .setCancelable(false)
                        .setMessage("Before you proceed please check if seed tag " + "\"" +
                                glbl_seedTag +
                                "\" is correct. If seed tag has typographical error, immediately contact assigned RC/PC in your area. \n\n Click \"Proceed\" if no correction to be made.")
                        .setPositiveButton("PROCEED", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                if (mTotalBagCount > 0) {

                                    //updated computation here for considering isBuffer status 9(replacement)
                                    int mglbl_varietyBalance;
                                    if (glbl_misbuffer == 9) {
                                        mglbl_varietyBalance = 999;
                                    } else {
                                        mglbl_varietyBalance = glbl_varietyBalance - rcepDatabase
                                                .tmpDeliveryBatchDataDAO()
                                                .getCurrentBatchVarietyTotalInput(glbl_variety,
                                                        glbl_batchTicketNumber);
                                    }

                                    if (mTotalBagCount > mglbl_varietyBalance) {
                                        Toast.makeText(DeliveryBatchMainActivity.this,
                                                "Cannot exceed more than the available " +
                                                        glbl_varietyBalance +
                                                        " bag(s) of remaining balance",
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        if (mTotalBagCount > glbl_rla_bag_limit) {
                                            Toast.makeText(DeliveryBatchMainActivity.this,
                                                    "Cannot exceed more than " +
                                                            glbl_rla_bag_limit + " bags",
                                                    Toast.LENGTH_SHORT).show();
                                        } else {

                                            if (glbl_instructeDeliveryVolume >= rcepDatabase
                                                    .tmpDeliveryBatchDataDAO()
                                                    .getTempDeliveryBatchTotal() + mTotalBagCount) {
                                                checkSeedlotStatus(DeliveryBatchMainActivity.this,
                                                        mTicketNumber,
                                                        mBatchTicketNumber, mDeliveryDate,
                                                        mCoopAccreditation, mSeedTag, mSeedVariety,
                                                        mSeedClass, mTotalBagCount, mUserId,
                                                        mDateCreated, mCoopdropoffpoint, mRegion,
                                                        mProvince, mMunicipality, mPrvDropoffId,
                                                        mPrv, mMoaNumber, mAppVersion, mSeries,
                                                        mSgId, transpo_cost_per_bag);
                                            } else {
                                                Toast.makeText(DeliveryBatchMainActivity.this,
                                                        "Cannot exceed more than the remaining volume.",
                                                        Toast.LENGTH_SHORT).show();
                                            }

                                            Log.e(TAG, "ticketNumber->" + mTicketNumber
                                                    + "\nbatchTicketNumber->" + mBatchTicketNumber
                                                    + "\ndeliveryDate->" + mDeliveryDate
                                                    + "\ncoopAccreditation->" + mCoopAccreditation
                                                    + "\nseedTag->" + mSeedTag
                                                    + "\nseedVariety->" + mSeedVariety
                                                    + "\nbatchTicketNumber->" + mBatchTicketNumber
                                                    + "\nseedClass->" + mSeedClass
                                                    + "\ntotalBagCount->" + mTotalBagCount
                                                    + "\nuserId->" + mUserId
                                                    + "\ndateCreated->" + mDateCreated
                                                    + "\ndropOffPoint->" + mCoopdropoffpoint
                                                    + "\nregion->" + mRegion
                                                    + "\nprovince->" + mProvince
                                                    + "\nmunicipality->" + mMunicipality
                                                    + "\nbatchSeries->" + mSeries
                                                    + "\nsgId>" + mSgId);

                                        }
                                    }
                                } else {
                                    Toast.makeText(DeliveryBatchMainActivity.this,
                                            "Bags to deliver must be greater than 0",
                                            Toast.LENGTH_SHORT).show();
                                }

                                dialogInterface.dismiss();
                            }
                        })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .show();


                /*CFAlertDialog.Builder builder = new CFAlertDialog.Builder(
                        DeliveryBatchMainActivity.this);
                builder.setDialogStyle(CFAlertDialog.CFAlertStyle.ALERT);
                builder.setTitle("Warning!");
                builder.setMessage(
                        "Before you proceed please check if seed tag " + "\"" + glbl_seedTag +
                                "\" is " +
                                "correct. If seed tag has typographical error, immediately contact assigned RC/PC in your area. \n\n Click \"Proceed\" if no correction to be made.");
                builder.addButton("PROCEED", Color.parseColor("#FFFFFF"),
                        Color.parseColor("#429ef4"),
                        CFAlertDialog.CFAlertActionStyle.POSITIVE,
                        CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, int which) {
                                dialog.dismiss();
                                if (mTotalBagCount > 0) {

                                    //updated computation here for considering isBuffer status 9(replacement)
                                    int mglbl_varietyBalance;
                                    if (glbl_misbuffer == 9) {
                                        mglbl_varietyBalance = 999;
                                    } else {
                                        mglbl_varietyBalance = glbl_varietyBalance - rcepDatabase
                                                .tmpDeliveryBatchDataDAO()
                                                .getCurrentBatchVarietyTotalInput(glbl_variety,
                                                        glbl_batchTicketNumber);
                                    }

                                    if (mTotalBagCount > mglbl_varietyBalance) {
                                        Toast.makeText(DeliveryBatchMainActivity.this,
                                                "Cannot exceed more than the available " +
                                                        glbl_varietyBalance +
                                                        " bag(s) of remaining balance",
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        if (mTotalBagCount > glbl_rla_bag_limit) {
                                            Toast.makeText(DeliveryBatchMainActivity.this,
                                                    "Cannot exceed more than " +
                                                            glbl_rla_bag_limit + " bags",
                                                    Toast.LENGTH_SHORT).show();
                                        } else {

                                            if (glbl_instructeDeliveryVolume >= rcepDatabase
                                                    .tmpDeliveryBatchDataDAO()
                                                    .getTempDeliveryBatchTotal() + mTotalBagCount) {
                                                checkSeedlotStatus(DeliveryBatchMainActivity.this,
                                                        mTicketNumber,
                                                        mBatchTicketNumber, mDeliveryDate,
                                                        mCoopAccreditation, mSeedTag, mSeedVariety,
                                                        mSeedClass, mTotalBagCount, mUserId,
                                                        mDateCreated, mCoopdropoffpoint, mRegion,
                                                        mProvince, mMunicipality, mPrvDropoffId,
                                                        mPrv, mMoaNumber, mAppVersion, mSeries,
                                                        mSgId, transpo_cost_per_bag);
                                            } else {
                                                Toast.makeText(DeliveryBatchMainActivity.this,
                                                        "Cannot exceed more than the remaining volume.",
                                                        Toast.LENGTH_SHORT).show();
                                            }

                                            Log.e(TAG, "ticketNumber->" + mTicketNumber
                                                    + "\nbatchTicketNumber->" + mBatchTicketNumber
                                                    + "\ndeliveryDate->" + mDeliveryDate
                                                    + "\ncoopAccreditation->" + mCoopAccreditation
                                                    + "\nseedTag->" + mSeedTag
                                                    + "\nseedVariety->" + mSeedVariety
                                                    + "\nbatchTicketNumber->" + mBatchTicketNumber
                                                    + "\nseedClass->" + mSeedClass
                                                    + "\ntotalBagCount->" + mTotalBagCount
                                                    + "\nuserId->" + mUserId
                                                    + "\ndateCreated->" + mDateCreated
                                                    + "\ndropOffPoint->" + mCoopdropoffpoint
                                                    + "\nregion->" + mRegion
                                                    + "\nprovince->" + mProvince
                                                    + "\nmunicipality->" + mMunicipality
                                                    + "\nbatchSeries->" + mSeries
                                                    + "\nsgId>" + mSgId);

                                        }
                                    }
                                } else {
                                    Toast.makeText(DeliveryBatchMainActivity.this,
                                            "Bags to deliver must be greater than 0",
                                            Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
                builder.addButton("NO", Color.parseColor("#FFFFFF"), Color.parseColor("#429ef4"),
                        CFAlertDialog.CFAlertActionStyle.NEGATIVE,
                        CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                builder.show();*/


            }


        }

    }

    private void verifyLot(final Context ctx, final String mTicketNumber,
                           final String mBatchTicketNumber, final String mDeliveryDate,
                           final String mCoopAccreditation, final String mSeedTag,
                           final String mSeedVariety, final String mSeedClass,
                           final int mTotalBagCount, final int mUserId, final String mDateCreated,
                           final String mCoopdropoffpoint, final String mRegion,
                           final String mProvince, final String mMunicipality,
                           final String mPrvDropoffId, final String mPrv, final String mMoaNumber,
                           final String mAppVersion, final String mSeries, final String sg_id,
                           final double transpo_cost_per_bag) {
        Fun.progressMessage(mProgressDialog, "verifying lot balance");
        //url
        String request_url = Fun.getAddress(glbl_season) + "/script/sg_verify_lot_balance.php";
        Log.e(TAG, "verifyCurrentDelivery: " + request_url);
        //request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, request_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                   /* int deliveryTotal = jsonObject.getInt("deliveryTotal");
                    int actualHasMatch = jsonObject.getInt("actualHasMatch");
                    int actualTotal = jsonObject.getInt("actualTotal");*/
                            int totalCoopBalance = jsonObject.getInt("totalBalance");

                            Log.e(TAG, "onResponse: " + totalCoopBalance);
                            //current confirmed and inspected bags count
                            // int current_total = actualHasMatch > 0 ? actualTotal : deliveryTotal;
                            //current lot balance to deliver
                            final int mBalance = glbl_rla_bag_limit - totalCoopBalance;

                            //checking data from server side
                            if (totalCoopBalance > glbl_rla_bag_limit) {
                                Fun.progressStop(mProgressDialog);
                                //already completed allocation
                                String msg =
                                        "Transaction cannot proceed. Already completed allocation";
                                new MaterialAlertDialogBuilder(DeliveryBatchMainActivity.this)
                                        .setTitle("RCEF DI")
                                        .setCancelable(false)
                                        .setMessage(msg)
                                        .setPositiveButton("DONE",
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(
                                                            DialogInterface dialogInterface,
                                                            int i) {
                                                        dialogInterface.dismiss();
                                                        updateVarietyBalance();
                                                    }
                                                })
                                        .show();

                                /*CFAlertDialog.Builder builder = new CFAlertDialog.Builder(
                                        DeliveryBatchMainActivity.this);
                                builder.setDialogStyle(CFAlertDialog.CFAlertStyle.ALERT);
                                builder.setTitle("RCEP App");
                                builder.setCancelable(false);
                                builder.setMessage(msg);
                                builder.addButton("DONE", Color.parseColor("#FFFFFF"),
                                        Color.parseColor("#429ef4"),
                                        CFAlertDialog.CFAlertActionStyle.POSITIVE,
                                        CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(final DialogInterface dialog,
                                                                int which) {
                                                dialog.dismiss();
                                                updateVarietyBalance();
                                            }
                                        });
                                builder.show();*/
                            } else {
                                //checking local data balance to be sent central server
                                if (mBalance >= mTotalBagCount) {
                                    Fun.progressStop(mProgressDialog);
                                    //verified, proceed inserting next lot
                                    TmpDeliveryBatchData data = new TmpDeliveryBatchData(
                                            mTicketNumber, mBatchTicketNumber, mDeliveryDate,
                                            mCoopAccreditation, mSeedTag, mSeedVariety, mSeedClass,
                                            mTotalBagCount, mUserId, mDateCreated,
                                            mCoopdropoffpoint, mRegion, mProvince, mMunicipality,
                                            mPrvDropoffId, mPrv, mMoaNumber, mAppVersion, mSeries,
                                            sg_id, glbl_misbuffer, transpo_cost_per_bag);
                                    //adding data to tmpBatchData
                                    rcepDatabase.tmpDeliveryBatchDataDAO().insert(data);
                                    //refresh data
                                    refreshTmpBatchData();
                                    if (deliveryBatchCount() > 0) {
                                        disableViews();
                                    }
                                    //hide keyboard
                                    Fun.hideKeyboard(DeliveryBatchMainActivity.this);
                                    final Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            clearData();
                                            clearFocus();
                                            updateRemainingBalance();
                                            glbl_varietyBalance = 0;
                                            til_varietyBalance.setHelperText(
                                                    "Remaining balance : no variety selected");
                                        }
                                    }, 500);
                                } else {
                                    Fun.progressStop(mProgressDialog);
                                    final int mmBalance = mBalance;

                                    if (mmBalance == 0) {
                                        String msg =
                                                "Transaction cannot proceed. Already completed allocation";
                                        new MaterialAlertDialogBuilder(
                                                DeliveryBatchMainActivity.this)
                                                .setTitle("RCEF DI")
                                                .setCancelable(false)
                                                .setMessage(msg)
                                                .setPositiveButton("DONE",
                                                        new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(
                                                                    DialogInterface dialogInterface,
                                                                    int i) {
                                                                dialogInterface.dismiss();
                                                            }
                                                        })
                                                .show();

                                        /*CFAlertDialog.Builder builder = new CFAlertDialog.Builder(
                                                DeliveryBatchMainActivity.this);
                                        builder.setDialogStyle(CFAlertDialog.CFAlertStyle.ALERT);
                                        builder.setTitle("RCEP App");
                                        builder.setCancelable(false);
                                        builder.setMessage(msg);
                                        builder.addButton("DONE", Color.parseColor("#FFFFFF"),
                                                Color.parseColor("#429ef4"),
                                                CFAlertDialog.CFAlertActionStyle.POSITIVE,
                                                CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(
                                                            final DialogInterface dialog,
                                                            int which) {
                                                        dialog.dismiss();
                                                    }
                                                });
                                        builder.show();*/
                                    } else {
                                        //dialog confirmation here
                                        String dialogMessage = "Your input " + mTotalBagCount +
                                                " bag(s) will exceed the current limit of " +
                                                glbl_rla_bag_limit + " bags on lot " + mSeedTag +
                                                "." +
                                                " Do you wish to fill up the remaining balance(" +
                                                mmBalance + " bags) on lot " + mSeedTag + ".";
                                        new MaterialAlertDialogBuilder(
                                                DeliveryBatchMainActivity.this)
                                                .setTitle("RCEF DI")
                                                .setCancelable(false)
                                                .setMessage(dialogMessage)
                                                .setPositiveButton("PROCEED",
                                                        new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(
                                                                    DialogInterface dialogInterface,
                                                                    int i) {
                                                                TmpDeliveryBatchData data =
                                                                        new TmpDeliveryBatchData(
                                                                                mTicketNumber,
                                                                                mBatchTicketNumber,
                                                                                mDeliveryDate,
                                                                                mCoopAccreditation,
                                                                                mSeedTag,
                                                                                mSeedVariety,
                                                                                mSeedClass,
                                                                                mmBalance, mUserId,
                                                                                mDateCreated,
                                                                                mCoopdropoffpoint,
                                                                                mRegion,
                                                                                mProvince,
                                                                                mMunicipality,
                                                                                mPrvDropoffId, mPrv,
                                                                                mMoaNumber,
                                                                                mAppVersion,
                                                                                mSeries,
                                                                                sg_id,
                                                                                glbl_misbuffer,
                                                                                transpo_cost_per_bag);
                                                                //adding data to tmpBatchData
                                                                rcepDatabase.tmpDeliveryBatchDataDAO()
                                                                        .insert(data);
                                                                //refresh data
                                                                refreshTmpBatchData();
                                                                if (deliveryBatchCount() > 0) {
                                                                    disableViews();
                                                                }
                                                                //hide keyboard
                                                                Fun.hideKeyboard(
                                                                        DeliveryBatchMainActivity.this);
                                                                final Handler handler =
                                                                        new Handler();
                                                                handler.postDelayed(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        clearData();
                                                                        clearFocus();
                                                                        updateRemainingBalance();
                                                                        glbl_varietyBalance = 0;
                                                                        til_varietyBalance.setHelperText(
                                                                                "Remaining balance : no variety selected");
                                                                    }
                                                                }, 500);

                                                                dialogInterface.dismiss();
                                                            }
                                                        })
                                                .setNegativeButton("CANCEL",
                                                        new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(
                                                                    DialogInterface dialogInterface,
                                                                    int i) {
                                                                dialogInterface.dismiss();
                                                            }
                                                        })
                                                .show();


                                       /* CFAlertDialog.Builder builder = new CFAlertDialog.Builder(
                                                DeliveryBatchMainActivity.this);
                                        builder.setDialogStyle(CFAlertDialog.CFAlertStyle.ALERT);
                                        builder.setTitle("RCEP App");
                                        builder.setCancelable(false);
                                        builder.setMessage(dialogMessage);
                                        builder.addButton("PROCEED", Color.parseColor("#FFFFFF"),
                                                Color.parseColor("#429ef4"),
                                                CFAlertDialog.CFAlertActionStyle.POSITIVE,
                                                CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(
                                                            final DialogInterface dialog,
                                                            int which) {
                                                        dialog.dismiss();
                                                        TmpDeliveryBatchData data =
                                                                new TmpDeliveryBatchData(
                                                                        mTicketNumber,
                                                                        mBatchTicketNumber,
                                                                        mDeliveryDate,
                                                                        mCoopAccreditation,
                                                                        mSeedTag, mSeedVariety,
                                                                        mSeedClass,
                                                                        mmBalance, mUserId,
                                                                        mDateCreated,
                                                                        mCoopdropoffpoint, mRegion,
                                                                        mProvince, mMunicipality,
                                                                        mPrvDropoffId, mPrv,
                                                                        mMoaNumber,
                                                                        mAppVersion, mSeries,
                                                                        sg_id, glbl_misbuffer,
                                                                        transpo_cost_per_bag);
                                                        //adding data to tmpBatchData
                                                        rcepDatabase.tmpDeliveryBatchDataDAO()
                                                                .insert(data);
                                                        //refresh data
                                                        refreshTmpBatchData();
                                                        if (deliveryBatchCount() > 0) {
                                                            disableViews();
                                                        }
                                                        //hide keyboard
                                                        Fun.hideKeyboard(
                                                                DeliveryBatchMainActivity.this);
                                                        final Handler handler = new Handler();
                                                        handler.postDelayed(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                clearData();
                                                                clearFocus();
                                                                updateRemainingBalance();
                                                                glbl_varietyBalance = 0;
                                                                til_varietyBalance.setHelperText(
                                                                        "Remaining balance : no variety selected");
                                                            }
                                                        }, 500);
                                                    }
                                                });
                                        builder.addButton("NO", Color.parseColor("#FFFFFF"),
                                                Color.parseColor("#429ef4"),
                                                CFAlertDialog.CFAlertActionStyle.NEGATIVE,
                                                CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog,
                                                                        int which) {
                                                        dialog.dismiss();
                                                    }
                                                });
                                        builder.show();*/
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Fun.progressStop(mProgressDialog);
                Toast.makeText(ctx,
                        "Server unreachable. Please check network connectivity and try again.",
                        Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("seedTag", mSeedTag);
                params.put("moaNumber", mMoaNumber);

                Log.e(TAG, "getParams: " + mSeedTag + "::" + mMoaNumber);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(ctx);
        requestQueue.add(stringRequest);
    }

    void refreshTmpBatchData() {
        deliveryBatchData.clear();
        List<TmpDeliveryBatchData> src = rcepDatabase.tmpDeliveryBatchDataDAO().getAll();
        for (TmpDeliveryBatchData val : src) {
            DeliveryBatchData data = new DeliveryBatchData(val.getTmpDeliveryBatchDataId(),
                    val.getTicketNumber(), val.getBatchTicketNumber(), val.getDeliveryDate(),
                    val.getCoopAccreditation(), val.getSeedTag(), val.getSeedVariety(),
                    val.getSeedClass(), val.getTotalBagCount(), val.getUserId(),
                    val.getDateCreated(), val.getDropOffPoint(), val.getRegion(), val.getProvince(),
                    val.getMunicipality(), val.getPrv_dropoff_id(), val.getPrv(),
                    val.getMoa_number(), val.getApp_version(), val.getBatchSeries(),
                    val.getSg_id(), val.getMisBuffer(), val.getTranspo_cost_per_bag());
            deliveryBatchData.add(data);
        }
        deliveryBatchAdapter.notifyDataSetChanged();
    }

    public void confirmDelivery(View view) {
        //checking if delivery batch is empty
        if (deliveryBatchData.size() > 0) {
            new MaterialAlertDialogBuilder(DeliveryBatchMainActivity.this)
                    .setTitle("RCEF DI")
                    .setCancelable(false)
                    .setMessage(
                            "Please review list before confirming delivery. Do you wish to proceed?")
                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            sendBatchDelivery(DeliveryBatchMainActivity.this,
                                    Fun.jsonDeliveryBatchData(deliveryBatchData));
                        }
                    })
                    .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .show();

            /*CFAlertDialog.Builder builder = new CFAlertDialog.Builder(
                    DeliveryBatchMainActivity.this);
            builder.setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET);
            builder.setTitle("RCEP App");
            builder.setMessage(
                    "Please review list before confirming delivery. Do you wish to proceed?");
            builder.addButton("YES", Color.parseColor("#FFFFFF"), Color.parseColor("#429ef4"),
                    CFAlertDialog.CFAlertActionStyle.POSITIVE,
                    CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, int which) {
                            dialog.dismiss();
                            //send batch delivery data to server
                    *//*    Log.e(TAG, "onClick: sending delivery");
                        String accredNo = prefUserAccount.getString(Fun.uaCoopAccreditation(), "");
                        String moaNo = prefUserAccount.getString(Fun.uaCoopCurrentMOA(), "");*//*
                            sendBatchDelivery(DeliveryBatchMainActivity.this,
                                    Fun.jsonDeliveryBatchData(deliveryBatchData));
                            // verifyCurrentDelivery(DeliveryBatchMainActivity.this, accredNo, moaNo);
                            //sendBatchDelivery(DeliveryBatchMainActivity.this, Fun.jsonDeliveryBatchData(deliveryBatchData));
                        }
                    });
            builder.addButton("NO", Color.parseColor("#FFFFFF"), Color.parseColor("#429ef4"),
                    CFAlertDialog.CFAlertActionStyle.NEGATIVE,
                    CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            builder.show();*/
           /* //verify if delivery limit is exhausted
            int tempTotal = rcepDatabase.tmpDeliveryBatchDataDAO().getTempDeliveryBatchTotal();
            int limit = rcepDatabase.tblTotalCommitmentDAO().getTotalCommitment() - rcepDatabase.tblCurrentDeliveryCountDAO().getCurrentDeliveryTotal();
            if (limit >= tempTotal) {
                // Log.e(TAG, "confirmDelivery: " + Fun.jsonDeliveryBatchData(deliveryBatchData));

            } else {
                int excess = Math.abs(limit - tempTotal);
                CFAlertDialog.Builder builder = new CFAlertDialog.Builder(DeliveryBatchMainActivity.this);
                builder.setDialogStyle(CFAlertDialog.CFAlertStyle.ALERT);
                builder.setMessage("Delivery batch exceeds " + excess + " bag/s. Please see cooperative delivery summary for details and try again.");
                builder.addButton("OK", Color.parseColor("#FFFFFF"), Color.parseColor("#429ef4"), CFAlertDialog.CFAlertActionStyle.POSITIVE, CFAlertDialog.CFAlertActionAlignment.JUSTIFIED, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
            }*/
        } else {
            Toast.makeText(DeliveryBatchMainActivity.this, "Unable to send empty batch delivery",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void sendBatchDelivery(final Context ctx, final String deliveryBatchData) {
        Fun.progressStart(mProgressDialog, "", "Sending batch delivery to server");

        //Fun.progressStart(mProgressDialog, "", "Sending batch delivery to server");
        //url
        String request_url = Fun.getAddress(glbl_season) + "/script/sg_delivery_batch_request.php";

        Log.e(TAG, "sendBatchDelivery: " + request_url);
        //request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, request_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e(TAG, "sendBatchDelivery => onResponse : " + response);
                        switch (response.trim()) {
                            case "1":
                                //success
                                //next request
                                updateBatchStatus(DeliveryBatchMainActivity.this,
                                        glbl_batchTicketNumber, "0", Fun.getCurrentDate());
                                break;
                            case "0":
                                Toast.makeText(ctx,
                                        "Failed to receive delivery. Please retry sending batch delivery again",
                                        Toast.LENGTH_SHORT).show();
                                Fun.progressStop(mProgressDialog);
                                break;
                            case "404":
                                Toast.makeText(ctx, "Error : Unable to connect in database server",
                                        Toast.LENGTH_LONG).show();
                                Fun.progressStop(mProgressDialog);
                                break;
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Fun.progressStop(mProgressDialog);
                Toast.makeText(ctx,
                        "Server unreachable. Please check network connectivity and try again.",
                        Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("deliveryBatchData", deliveryBatchData);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(ctx);
        requestQueue.add(stringRequest);
    }

    private void updateBatchStatus(final Context ctx, final String batchTicketNumber,
                                   final String status, final String dateCreated) {
        Fun.progressMessage(mProgressDialog, "updating batch delivery status");
        //url
        String request_url = Fun
                .getAddress(glbl_season) + "/script/sg_delivery_batch_updatestatus_request.php";
        //request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, request_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        switch (response.trim()) {
                            case "1":
                                updateBatchTransactionStatus(ctx, glbl_batchTicketNumber);
                                break;
                            case "0":
                                Toast.makeText(ctx,
                                        "Failed to update dropoff point. Please retry sending batch delivery again",
                                        Toast.LENGTH_SHORT).show();
                                Fun.progressStop(mProgressDialog);
                                break;
                            case "404":
                                Toast.makeText(ctx, "Error : Unable to connect in database server",
                                        Toast.LENGTH_LONG).show();
                                Fun.progressStop(mProgressDialog);
                                break;
                /*    default:
                        Toast.makeText(ctx, "Unknown error occured", Toast.LENGTH_SHORT).show();
                        Fun.progressStop(mProgressDialog);*/
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ctx,
                        "Server unreachable. Please check network connectivity and try again.",
                        Toast.LENGTH_SHORT).show();
                Fun.progressStop(mProgressDialog);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("batchTicketNumber", batchTicketNumber);
                params.put("status", status);
                params.put("dateCreated", dateCreated);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(ctx);
        requestQueue.add(stringRequest);
    }

    private void updateBatchTransactionStatus(final Context ctx, final String batchTicketNumber) {
        Fun.progressMessage(mProgressDialog, "updating batch transaction status");
        //url
        String request_url =
                Fun.getAddress(glbl_season) + "/script/sg_delivery_batch_update_transaction.php";
        //request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, request_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        switch (response.trim()) {
                            case "1":

                                if (!Objects.equals(
                                        prefUserAccount.getString(Fun.uaStationName(), ""),
                                        "PhilRice Central Experiment Station") || !Objects.equals(
                                        prefUserAccount.getString(Fun.uaStationName(), ""), "")) {
                                    updateStationDB(ctx, glbl_domain, glbl_port,
                                            "rcep_delivery_inspection");
                                }

                                Fun.progressStop(mProgressDialog);
                                new MaterialAlertDialogBuilder(DeliveryBatchMainActivity.this)
                                        .setTitle("RCEF DI")
                                        .setCancelable(false)
                                        .setMessage("Batch delivery " + glbl_batchTicketNumber +
                                                " successfully confirmed! Pull down to update delivery list.")
                                        .setPositiveButton("DONE",
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(
                                                            DialogInterface dialogInterface,
                                                            int i) {
                                                        dialogInterface.dismiss();
                                                        final Handler handler = new Handler();
                                                        handler.postDelayed(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                Intent intent = new Intent(
                                                                        DeliveryBatchMainActivity.this,
                                                                        DeliveryMainActivity.class);
                                                                intent.setFlags(
                                                                        Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                                startActivity(intent);
                                                                finish();
                                                            }
                                                        }, 250);
                                                    }
                                                })
                                        .show();

                                /*CFAlertDialog.Builder builder = new CFAlertDialog.Builder(
                                        DeliveryBatchMainActivity.this);
                                builder.setDialogStyle(CFAlertDialog.CFAlertStyle.ALERT);
                                builder.setTitle("RCEP App");
                                builder.setCancelable(false);
                                builder.setMessage(
                                        "Batch delivery " + glbl_batchTicketNumber +
                                                " successfully confirmed! Pull down to update delivery list.");
                                builder.addButton("OK", Color.parseColor("#FFFFFF"),
                                        Color.parseColor("#429ef4"),
                                        CFAlertDialog.CFAlertActionStyle.POSITIVE,
                                        CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(final DialogInterface dialog,
                                                                int which) {
                                                dialog.dismiss();
                                                final Handler handler = new Handler();
                                                handler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Intent intent = new Intent(
                                                                DeliveryBatchMainActivity.this,
                                                                DeliveryMainActivity.class);
                                                        intent.setFlags(
                                                                Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                }, 250);
                                            }
                                        });
                                builder.show();*/
                                break;
                            case "0":
                                Toast.makeText(ctx,
                                        "Failed to update dropoff point. Please retry sending batch delivery again",
                                        Toast.LENGTH_SHORT).show();
                                Fun.progressStop(mProgressDialog);
                                break;
                            case "404":
                                Toast.makeText(ctx, "Error : Unable to connect in database server",
                                        Toast.LENGTH_LONG).show();
                                Fun.progressStop(mProgressDialog);
                                break;
                /*    default:
                        Toast.makeText(ctx, "Unknown error occured", Toast.LENGTH_SHORT).show();
                        Fun.progressStop(mProgressDialog);*/
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ctx,
                        "Server unreachable. Please check network connectivity and try again.",
                        Toast.LENGTH_SHORT).show();
                Fun.progressStop(mProgressDialog);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("batchTicketNumber", batchTicketNumber);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(ctx);
        requestQueue.add(stringRequest);
    }

    private void checkSeedlotStatus(final Context ctx, final String mTicketNumber,
                                    final String mBatchTicketNumber, final String mDeliveryDate,
                                    final String mCoopAccreditation, final String mSeedTag,
                                    final String mSeedVariety, final String mSeedClass,
                                    final int mTotalBagCount, final int mUserId,
                                    final String mDateCreated, final String mCoopdropoffpoint,
                                    final String mRegion, final String mProvince,
                                    final String mMunicipality, final String mPrvDropoffId,
                                    final String mPrv, final String mMoaNumber,
                                    final String mAppVersion, final String mSeries,
                                    final String sg_id, final double transpo_cost_per_bag) {
        Fun.progressStart(mProgressDialog, "", "verifying seedtag status");
        //url
        String request_url = Fun.getAddress(glbl_season) + "/script/sg_verify_lot_status.php";
        Log.e(TAG, "verifyCurrentDelivery: " + request_url);
        //request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, request_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String status = jsonObject.getString("status");
                            String message = jsonObject.getString("message");
                            int isRejected = jsonObject.getInt("isRejected");
                            if (isRejected == 1) {
                                Fun.progressStop(mProgressDialog);
                                new MaterialAlertDialogBuilder(DeliveryBatchMainActivity.this)
                                        .setTitle("RCEF DI")
                                        .setCancelable(false)
                                        .setMessage("Invalid entry. Seed tag " + mSeedTag +
                                                " has been rejected during inspection.")
                                        .setPositiveButton("DONE",
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(
                                                            DialogInterface dialogInterface,
                                                            int i) {
                                                        dialogInterface.dismiss();
                                                    }
                                                })
                                        .show();

                                /*CFAlertDialog.Builder builder = new CFAlertDialog.Builder(
                                        DeliveryBatchMainActivity.this);
                                builder.setDialogStyle(CFAlertDialog.CFAlertStyle.ALERT);
                                builder.setMessage(
                                        "Invalid entry. Seed tag " + mSeedTag +
                                                " has been rejected during inspection.");
                                builder.addButton("OK", Color.parseColor("#FFFFFF"),
                                        Color.parseColor("#429ef4"),
                                        CFAlertDialog.CFAlertActionStyle.POSITIVE,
                                        CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(final DialogInterface dialog,
                                                                int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                builder.show();*/
                            } else {
                       /* Fun.progressStop(mProgressDialog);
                        //continue process here
                        Toast.makeText(ctx, "Passed!", Toast.LENGTH_LONG).show();*/
                                verifyLot(ctx, mTicketNumber, mBatchTicketNumber, mDeliveryDate,
                                        mCoopAccreditation, mSeedTag, mSeedVariety, mSeedClass,
                                        mTotalBagCount, mUserId, mDateCreated, mCoopdropoffpoint,
                                        mRegion, mProvince, mMunicipality, mPrvDropoffId, mPrv,
                                        mMoaNumber, mAppVersion, mSeries, sg_id,
                                        transpo_cost_per_bag);
                            }
                            //Log.e(TAG, "onResponse: " + message);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Fun.progressStop(mProgressDialog);
                Toast.makeText(ctx,
                        "Server unreachable. Please check network connectivity and try again.",
                        Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("seedTag", mSeedTag);
                params.put("moaNumber", mMoaNumber);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(ctx);
        requestQueue.add(stringRequest);
    }

    public void cancelScan(View view) {
        /*content_frame.setVisibility(View.GONE);
        mCodeScanner.stopPreview();
        mCodeScanner.releaseResources();
        delivery_form.setVisibility(View.VISIBLE);*/
    }

    @Override
    protected void onPause() {
        super.onPause();
        /*content_frame.setVisibility(View.GONE);
        mCodeScanner.releaseResources();*/
    }


    private ArrayList getRegions() {
        ArrayList<String> arrayList = new ArrayList<>();
        //first value in array
        arrayList.add("Clear Selected");
        //assigning list values from local dbase
        List<String> source = rcepDatabase.libDropoffPointDAO().getDropoffRegions();
        for (String s : source) {
            arrayList.add(s);
        }
        return arrayList;
    }

    private ArrayList getProvinces(String region) {
        ArrayList<String> arrayList = new ArrayList<>();
        //first value in array
        arrayList.add("Clear Selected");
        //assigning list values from local dbase
        List<String> source = rcepDatabase.libDropoffPointDAO().getDropoffProvinces(region);
        for (String s : source) {
            arrayList.add(s);
        }
        return arrayList;
    }

    private ArrayList getMunicipalities(String region, String province) {
        ArrayList<String> arrayList = new ArrayList<>();
        //first value in array
        arrayList.add("Clear Selected");
        //assigning list values from local dbase
        List<String> source = rcepDatabase.libDropoffPointDAO()
                .getDropoffMunicipalities(region, province);
        for (String s : source) {
            arrayList.add(s);
        }
        return arrayList;
    }

    private ArrayList getDropoffPoints_list(String region, String province, String municipality) {
        ArrayList<String> arrayList = new ArrayList<>();
        //first value in array
        arrayList.add("Clear Selected");
        arrayList.add("Others");
        //assigning list values from local dbase
        List<String> source = rcepDatabase.libDropoffPointDAO()
                .getDropoffPoints(region, province, municipality);
        for (String s : source) {
            arrayList.add(s);
        }
        return arrayList;
    }

   /* void getDropoffPoints_main(String region, String province, String municipality) {
        mDropoffPoint = rcepDatabase.libDropoffPointDAO().getDropoffPoints(region, province, municipality);
    }*/


    private ArrayList getSeeds() {
        ArrayList<String> arrayList = new ArrayList<>();
        //first value in array
        //arrayList.add("Clear Selected");
        //assigning list values from local dbase
        List<LibSeeds> source = rcepDatabase.libSeedsDAO().getSeeds();
        for (LibSeeds s : source) {
            arrayList.add(s.getVariety());
        }
        return arrayList;
    }

    @Override
    public void onBackPressed() {

        new MaterialAlertDialogBuilder(DeliveryBatchMainActivity.this)
                .setTitle("RCEF DI")
                .setCancelable(false)
                .setMessage("Back to home page? Unconfirmed delivery will be discarded")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(DeliveryBatchMainActivity.this,
                                        DeliveryMainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }
                        }, 500);
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .show();


        /*CFAlertDialog.Builder builder = new CFAlertDialog.Builder(DeliveryBatchMainActivity.this);
        builder.setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET);
        builder.setTitle("RCEP App");
        builder.setMessage("Back to home page? Unconfirmed delivery will be discarded");
        builder.addButton("YES", Color.parseColor("#FFFFFF"), Color.parseColor("#429ef4"),
                CFAlertDialog.CFAlertActionStyle.POSITIVE,
                CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        dialog.dismiss();
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(DeliveryBatchMainActivity.this,
                                        DeliveryMainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }
                        }, 500);
                    }
                });
        builder.addButton("NO", Color.parseColor("#FFFFFF"), Color.parseColor("#429ef4"),
                CFAlertDialog.CFAlertActionStyle.NEGATIVE,
                CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.show();*/
    }

    void clearData() {
        et_labNo.setText("");
        et_lotNo.setText("");
        et_bagsDelivered.setText("");
        et_batchSeries.setText("");
        et_seedGrower.setText("");
        et_seedTag.setText("");
        et_seedVariety.setText("");
        //et_transpo_cost_per_bag.setText("");
    }


    @Override
    public void onDelete(final int tmpDeliveryBatchDataId) {
        //removing from list
        Fun.progressStart(mProgressDialog, "", "Removing selected data from list");
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Fun.progressStop(mProgressDialog);
                //proceed verification screen
                //remove selected data
                rcepDatabase.tmpDeliveryBatchDataDAO().removeSelected(tmpDeliveryBatchDataId);
                //refresh data
                refreshTmpBatchData();
                //deliveryBatchData.remove(position);
                //updates sampling list
                //deliveryBatchAdapter.notifyDataSetChanged();
                // update views
                if (deliveryBatchCount() > 0) {
                    disableViews();
                } else {
                    enableViews();
                }
                updateRemainingBalance();

                clearData();
                clearFocus();
                glbl_varietyBalance = 0;
                til_varietyBalance.setHelperText("Remaining balance : no variety selected");
            }
        }, 1250);
    }

/*

    private void requestDropoffPointLib2(final Context ctx) {
        Fun.progressStart(mProgressDialog, "", "Requesting dropoff points library");
        //clear drop off points
        rcepDatabase.libDropoffPointDAO().nukeTable();
        //initializing dialog
        final CFAlertDialog.Builder[] builder = {new CFAlertDialog.Builder(DeliveryBatchMainActivity.this)};
        //request_url
        final String request_url = Fun.getAddress() + "/script/lib_dropoffpoint_request.php";
        //prepare the Request
        StringRequest stringRequest = new StringRequest(Request.Method.GET, request_url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Log.e(TAG, "onResponse request item: " + response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    String message = jsonObject.getString("message");
                    switch (status) {
                        case "1":
                            //do other functions here
                            Fun.progressMessage(mProgressDialog, "Writing dropoff points library to disk");
                            //parsing json
                            JSONObject json = new JSONObject(message);
                            //name of array is result
                            JSONArray jArray = json.getJSONArray("result");
                            JSONObject jData = null;
                            for (int i = 0; i < jArray.length(); i++) {
                                jData = jArray.getJSONObject(i);
                                int dropoffPointId = jData.getInt("dropoffPointId");
                                //decode html_encode value from server
                                String region = Html.fromHtml(jData.getString("region")).toString();
                                String province = Html.fromHtml(jData.getString("province")).toString();
                                String municipality = Html.fromHtml(jData.getString("municipality")).toString();
                                String dropOffPoint = Html.fromHtml(jData.getString("dropOffPoint")).toString();
                                //logging results
                                //Log.e(TAG, "onResponse: " + item_id + "--" + item);
                                //check and insert if not existing in drop point library
                                if (Fun.checkDropoffPoint(DeliveryBatchMainActivity.this, dropoffPointId) == 0) {
                                    LibDropoffPoint libDropoffPointData = new LibDropoffPoint(dropoffPointId, region, province, municipality, dropOffPoint);
                                    rcepDatabase.libDropoffPointDAO().insertDropoffPoint(libDropoffPointData);
                                }
                            }
                            //end of request proceed to next activity

                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    //proceed verification screen
                                    //initialize region choices
                                    region = getRegions();
                                    Fun.progressStop(mProgressDialog);
                                    Toast.makeText(ctx, "Drop off points successfully updated", Toast.LENGTH_SHORT).show();
                                }
                            }, 1500);
                            break;
                        case "0":
                            Fun.progressStop(mProgressDialog);
                            //Toast.makeText(ctx, message, Toast.LENGTH_LONG).show();
                            builder[0] = new CFAlertDialog.Builder(DeliveryBatchMainActivity.this);
                            builder[0].setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET);
                            builder[0].setCancelable(false);
                            builder[0].setTitle("RCEP App");
                            builder[0].setMessage(message);
                            builder[0].addButton("RETRY", Color.parseColor("#FFFFFF"), Color.parseColor("#429ef4"), CFAlertDialog.CFAlertActionStyle.POSITIVE, CFAlertDialog.CFAlertActionAlignment.JUSTIFIED, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    final Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            requestDropoffPointLib(ctx);
                                        }
                                    }, 250);
                                }
                            });
                            builder[0].addButton("CANCEL", Color.parseColor("#FFFFFF"), Color.parseColor("#429ef4"), CFAlertDialog.CFAlertActionStyle.POSITIVE, CFAlertDialog.CFAlertActionAlignment.JUSTIFIED, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    final Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent intent = new Intent(DeliveryBatchMainActivity.this, DeliveryMainActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }, 250);
                                }
                            });
                            builder[0].show();
                            break;
                        case "3":
                            Fun.progressStop(mProgressDialog);
                            //Toast.makeText(ctx, "Error : Unable to connect in database server.", Toast.LENGTH_LONG).show();
                            builder[0] = new CFAlertDialog.Builder(DeliveryBatchMainActivity.this);
                            builder[0].setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET);
                            builder[0].setCancelable(false);
                            builder[0].setTitle("RCEP App");
                            builder[0].setMessage("Error : Unable to connect in database server.");
                            builder[0].addButton("RETRY", Color.parseColor("#FFFFFF"), Color.parseColor("#429ef4"), CFAlertDialog.CFAlertActionStyle.POSITIVE, CFAlertDialog.CFAlertActionAlignment.JUSTIFIED, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    final Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            requestDropoffPointLib(ctx);
                                        }
                                    }, 250);
                                }
                            });
                            builder[0].addButton("CANCEL", Color.parseColor("#FFFFFF"), Color.parseColor("#429ef4"), CFAlertDialog.CFAlertActionStyle.POSITIVE, CFAlertDialog.CFAlertActionAlignment.JUSTIFIED, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    final Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent intent = new Intent(DeliveryBatchMainActivity.this, DeliveryMainActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }, 250);
                                }
                            });
                            builder[0].show();
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Fun.progressStop(mProgressDialog);
                builder[0] = new CFAlertDialog.Builder(DeliveryBatchMainActivity.this);
                builder[0].setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET);
                builder[0].setCancelable(false);
                builder[0].setTitle("RCEP App");
                builder[0].setMessage("Server unreachable. Please check network connectivity and try again.");
                builder[0].addButton("RETRY", Color.parseColor("#FFFFFF"), Color.parseColor("#429ef4"), CFAlertDialog.CFAlertActionStyle.POSITIVE, CFAlertDialog.CFAlertActionAlignment.JUSTIFIED, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        dialog.dismiss();
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                requestDropoffPointLib(ctx);
                            }
                        }, 250);
                    }
                });
                builder[0].addButton("CANCEL", Color.parseColor("#FFFFFF"), Color.parseColor("#429ef4"), CFAlertDialog.CFAlertActionStyle.POSITIVE, CFAlertDialog.CFAlertActionAlignment.JUSTIFIED, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        dialog.dismiss();
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(DeliveryBatchMainActivity.this, DeliveryMainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }
                        }, 250);
                    }
                });
                builder[0].show();
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(ctx);
        requestQueue.add(stringRequest);
    }

*/


    private void requestCoopDropoffPointLib(final Context ctx, final String coop_accreditation,
                                            final String moa_number) {
        Log.e(TAG, "requestCoopDropoffPointLib: " + coop_accreditation + " " + moa_number);
        Fun.progressStart(mProgressDialog, "", "Updating delivery transaction details");
        //Log.e(TAG, "requestCoopDropoffPointLib: A");
        //request_url
        final String request_url =
                Fun.getAddress(glbl_season) + "/script/sg_fetch_transactions.php";
        Log.e(TAG, "requestCoopDropoffPointLib: " + request_url);
        //prepare the Request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, request_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e(TAG, "dropoffpoints: " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String status = jsonObject.getString("status");
                            String message = jsonObject.getString("message");
                            switch (status) {
                                case "1":
                                    Fun.progressStop(mProgressDialog);
                                    //coopdropoff_selection.add("Clear selected");
                                    //parsing json
                                    JSONObject json = new JSONObject(message);
                                    //name of array is result
                                    JSONArray jArray = json.getJSONArray("result");
                                    JSONObject jData;
                                    for (int i = 0; i < jArray.length(); i++) {
                                        jData = jArray.getJSONObject(i);

                                        int mdropoffPointId = jData.getInt("dropoffPointId");
                                        String mprv_dropoff_id = jData.getString("prv_dropoff_id");
                                        String mcoop_accreditation = jData
                                                .getString("accreditation_no");
                                        String mregion = jData.getString("region");
                                        String mprovince = jData.getString("province");
                                        String mmunicipality = jData.getString("municipality");
                                        String mdropOffPoint = jData.getString("dropOffPoint");
                                        String mprv = jData.getString("prv");
                                        String mmoa_number = jData.getString("moa_number");
                                        int mstatus = jData.getInt("status");
                                        int misBuffer = jData.getInt("isBuffer");
                                        int minstructed_delivery_volume = jData
                                                .getInt("instructed_delivery_volume");
                                        String mdelivery_date = jData.getString("delivery_date");
                                        String mbatchTicketNumber = jData
                                                .getString("batchTicketNumber");
                                        String mseed_distribution_mode = jData
                                                .getString("seed_distribution_mode");
                                        //assigning selection view
                                        String batch_category;
                                        switch (misBuffer) {
                                            case 0:
                                                /*if (mseed_distribution_mode.equalsIgnoreCase(
                                                        "regular")) {
                                                    batch_category = "Regular";
                                                } else {
                                                    batch_category = "Binhi e-Padala";
                                                }*/
                                                batch_category = mseed_distribution_mode;
                                                break;
                                            case 1:
                                                batch_category = "Buffer";
                                                break;
                                            case 9:
                                                batch_category = "Replacement";
                                                break;
                                            default:
                                                batch_category = "Unknown";
                                        }

                                        String mselection =
                                                mprovince + " > " + mmunicipality + " > " +
                                                        mdropOffPoint +
                                                        "\nTicket : " + mbatchTicketNumber +
                                                        "\nVolume : " +
                                                        minstructed_delivery_volume +
                                                        "\nDelivery : " +
                                                        mdelivery_date.substring(0, 10) +
                                                        "\nCategory : " + batch_category;
                                        //populate

                                        if (glbl_userRole.equals("seed-grower")) {
                                            Log.e(TAG, "seed grower");
                                            if (Fun.isEqualOrBeyond(mdelivery_date.substring(0, 10),
                                                    Fun.getCurrentDateYMD()) == 1) {
                                                Log.e(TAG, "seed grower plus");
                                                coopdropoff_source
                                                        .add(new CoopDropoffPoint(mdropoffPointId,
                                                                mprv_dropoff_id,
                                                                mcoop_accreditation, mregion,
                                                                mprovince, mmunicipality,
                                                                mdropOffPoint, mprv, mmoa_number,
                                                                mstatus,
                                                                minstructed_delivery_volume,
                                                                mdelivery_date,
                                                                mbatchTicketNumber, misBuffer,
                                                                mseed_distribution_mode));
                                                //adding data to selection
                                                coopdropoff_selection.add(mselection);
                                            }
                                        } else {
                                            //for delivery manager
                                            Log.e(TAG, "delivery manager");
                                            coopdropoff_source
                                                    .add(new CoopDropoffPoint(mdropoffPointId,
                                                            mprv_dropoff_id, mcoop_accreditation,
                                                            mregion, mprovince, mmunicipality,
                                                            mdropOffPoint, mprv, mmoa_number,
                                                            mstatus, minstructed_delivery_volume,
                                                            mdelivery_date, mbatchTicketNumber,
                                                            misBuffer, mseed_distribution_mode));
                                            //adding data to selection

                                            Log.e(TAG, "onResponse: " + mbatchTicketNumber);
                                            coopdropoff_selection.add(mselection);
                                        }


                                    }

                                    //proceed next request
                                    //requestCoopRlaDetails(ctx, coop_accreditation, moa_number);

                                    break;
                                case "0":
                                    Fun.progressStop(mProgressDialog);
                                    new MaterialAlertDialogBuilder(DeliveryBatchMainActivity.this)
                                            .setTitle("RCEF DI")
                                            .setCancelable(false)
                                            .setMessage(
                                                    "No current transactions available. Please contact assigned RC/PC in your area for details")
                                            .setPositiveButton("DONE",
                                                    new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(
                                                                DialogInterface dialogInterface,
                                                                int i) {
                                                            dialogInterface.dismiss();
                                                            Intent intent = new Intent(
                                                                    DeliveryBatchMainActivity.this,
                                                                    DeliveryMainActivity.class);
                                                            intent.setFlags(
                                                                    Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    })
                                            .show();

                                    /*CFAlertDialog.Builder builder = new CFAlertDialog.Builder(
                                            DeliveryBatchMainActivity.this);
                                    builder.setDialogStyle(CFAlertDialog.CFAlertStyle.ALERT);
                                    builder.setCancelable(false);
                                    builder.setMessage(
                                            "No current transactions available. Please contact assigned RC/PC in your area for details");
                                    builder.addButton("DONE", Color.parseColor("#FFFFFF"),
                                            Color.parseColor("#429ef4"),
                                            CFAlertDialog.CFAlertActionStyle.POSITIVE,
                                            CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(final DialogInterface dialog,
                                                                    int which) {
                                                    dialog.dismiss();
                                                    Intent intent = new Intent(
                                                            DeliveryBatchMainActivity.this,
                                                            DeliveryMainActivity.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            });
                                    builder.show();*/
                                    break;
                                case "3":
                                    Fun.progressStop(mProgressDialog);
                                    Toast.makeText(ctx,
                                            "Error : Unable to connect in database server.",
                                            Toast.LENGTH_LONG).show();
                                    break;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Fun.progressStop(mProgressDialog);
                Toast.makeText(ctx,
                        "Server unreachable. Please check network connectivity and try again.",
                        Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("accreditation_no", coop_accreditation);
                params.put("moa_number", moa_number);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(ctx);
        requestQueue.add(stringRequest);
    }

    private void requestCoopRlaDetails(final Context ctx, final String coopAccreditation,
                                       final String moaNumber, final String sg_id,
                                       final String seedVariety) {

        Fun.progressStart(mProgressDialog, "", "checking for available laboratory results");
        //Log.e(TAG, "requestCoopDropoffPointLib: A");
        //request_url
        final String request_url =
                Fun.getAddress(glbl_season) + "/script/sg_rla_details_request.php";
        //Log.e(TAG, "requestCoopDropoffPointLib: " + request_url);
        //prepare the Request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, request_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e(TAG, "RLAs: " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String status = jsonObject.getString("status");
                            String message = jsonObject.getString("message");
                            switch (status) {
                                case "1":
                                    //coopdropoff_selection.add("Clear selected");
                                    //parsing json
                                    JSONObject json = new JSONObject(message);
                                    //name of array is result
                                    JSONArray jArray = json.getJSONArray("result");
                                    JSONObject jData;
                                    for (int i = 0; i < jArray.length(); i++) {
                                        jData = jArray.getJSONObject(i);
                                        int mrlaId = jData.getInt("rlaId");
                                        String mcoopAccreditation = jData
                                                .getString("coopAccreditation");
                                        String mlabNo = jData.getString("labNo");
                                        String mlotNo = jData.getString("lotNo").trim();
                                        int mnoOfBags = jData.getInt("noOfBags");
                                        String mseedVariety = jData.getString("seedVariety");
                                        String mmoaNumber = jData.getString("moaNumber");
                                        String mseedTag = mlotNo
                                                .equals("") ? mlabNo : mlabNo + " /  " + mlotNo;
                                        //populate
                                        rlaDetails_source
                                                .add(new RlaDetails(mrlaId, mcoopAccreditation,
                                                        mlabNo, mlotNo, mnoOfBags, mseedVariety,
                                                        mmoaNumber));
                                        rlaDetails_selection.add(mseedTag);
                                    }
                                    Fun.progressStop(mProgressDialog);
                                    break;
                                case "0":
                                    Fun.progressStop(mProgressDialog);
                                    new MaterialAlertDialogBuilder(DeliveryBatchMainActivity.this)
                                            .setTitle("RCEF DI")
                                            .setCancelable(false)
                                            .setMessage(
                                                    "No Laboratory result found. Please contact RCEF PMO for more details.")
                                            .setPositiveButton("DONE",
                                                    new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(
                                                                DialogInterface dialogInterface,
                                                                int i) {
                                                            dialogInterface.dismiss();
                                                        }
                                                    })
                                            .show();

                                    /*CFAlertDialog.Builder builder = new CFAlertDialog.Builder(
                                            DeliveryBatchMainActivity.this);
                                    builder.setDialogStyle(CFAlertDialog.CFAlertStyle.ALERT);
                                    builder.setCancelable(false);
                                    builder.setMessage(
                                            "No Laboratory result found. Please contact RCEF PMO for more details.");
                                    builder.addButton("OK", Color.parseColor("#FFFFFF"),
                                            Color.parseColor("#429ef4"),
                                            CFAlertDialog.CFAlertActionStyle.POSITIVE,
                                            CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(final DialogInterface dialog,
                                                                    int which) {
                                                    dialog.dismiss();
                                                }
                                            });
                                    builder.show();*/
                                    break;
                                case "3":
                                    Fun.progressStop(mProgressDialog);
                                    Toast.makeText(ctx,
                                            "Error : Unable to connect in database server.",
                                            Toast.LENGTH_LONG).show();
                                    break;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Fun.progressStop(mProgressDialog);
                Toast.makeText(ctx,
                        "Server unreachable. Please check network connectivity and try again.",
                        Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("coopAccreditation", coopAccreditation);
                params.put("moaNumber", moaNumber);
                params.put("sg_id", sg_id);
                params.put("seedVariety", seedVariety);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(ctx);
        requestQueue.add(stringRequest);
    }

    private void requestCoopSeedGrowers(final Context ctx, final String coop_accred,
                                        final String moaNumber, final String seedVariety) {

        Log.e(TAG,
                "requestCoopSeedGrowers: " + coop_accred + "||" + moaNumber + "||" + seedVariety);

        Fun.progressStart(mProgressDialog, "", "verifying seed growers list");
        //Log.e(TAG, "requestCoopDropoffPointLib: A");
        //request_url
        final String request_url = Fun.getAddress(glbl_season) + "/script/sg_fetch_seed_grower.php";
        Log.e(TAG, "requestCoopSeedGrowers: " + request_url);
        //prepare the Request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, request_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Log.e(TAG, "seedgrowers: " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String status = jsonObject.getString("status");
                            String message = jsonObject.getString("message");
                            switch (status) {
                                case "1":
                                    //clear all data before populating new data from server
                                    seedGrower_source.clear();
                                    seedGrower_selection.clear();
                                    //parsing json
                                    JSONObject json = new JSONObject(message);
                                    //name of array is result
                                    JSONArray jArray = json.getJSONArray("result");
                                    JSONObject jData;
                                    for (int i = 0; i < jArray.length(); i++) {
                                        jData = jArray.getJSONObject(i);
                                        int msg_id = jData.getInt("sg_id");
                                        String mcoop_accred = jData.getString("coop_accred");
                                        String mname = jData.getString("name");
                                        String m_fullname = jData.getString("full_name");
                                        //populate
                                        seedGrower_source
                                                .add(new SeedGrower(msg_id, mcoop_accred, mname));
                                        seedGrower_selection.add(m_fullname);
                                    }
                                    //Fun.progressStop(mProgressDialog);
                                    //check variety balance here with 10% value
                                    if (glbl_misbuffer == 9) {
                                        Fun.progressStop(mProgressDialog);
                                        til_varietyBalance.setHelperText("Remaining balance : 999");
                                    } else {
                                        requestVarietyBalance2(ctx, coop_accred, glbl_region,
                                                seedVariety, moaNumber);
                                    }

                                    break;
                                case "0":
                                    Fun.progressStop(mProgressDialog);
                                    glbl_varietyBalance = 0;
                                    til_varietyBalance.setHelperText(
                                            "Remaining balance : no variety selected");

                                    new MaterialAlertDialogBuilder(DeliveryBatchMainActivity.this)
                                            .setTitle("RCEF DI")
                                            .setCancelable(false)
                                            .setMessage("No seed grower found for " + seedVariety +
                                                    ". Please contact RCEF PMO for more details.")
                                            .setPositiveButton("DONE",
                                                    new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(
                                                                DialogInterface dialogInterface,
                                                                int i) {
                                                            dialogInterface.dismiss();
                                                        }
                                                    })
                                            .show();

                                    /*CFAlertDialog.Builder builder = new CFAlertDialog.Builder(
                                            DeliveryBatchMainActivity.this);
                                    builder.setDialogStyle(CFAlertDialog.CFAlertStyle.ALERT);
                                    builder.setCancelable(false);
                                    builder.setMessage(
                                            "No seed grower found for " + seedVariety +
                                                    ". Please contact RCEF PMO for more details.");
                                    builder.addButton("OK", Color.parseColor("#FFFFFF"),
                                            Color.parseColor("#429ef4"),
                                            CFAlertDialog.CFAlertActionStyle.POSITIVE,
                                            CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(final DialogInterface dialog,
                                                                    int which) {
                                                    dialog.dismiss();
                                                }
                                            });
                                    builder.show();*/
                                    break;
                                case "3":
                                    Fun.progressStop(mProgressDialog);
                                    glbl_varietyBalance = 0;
                                    til_varietyBalance
                                            .setHelperText("Remaining balance : please retry");
                                    Toast.makeText(ctx,
                                            "Error : Unable to connect in database server.",
                                            Toast.LENGTH_LONG).show();
                                    break;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "onErrorResponse: " + error.toString());
                Fun.progressStop(mProgressDialog);
                glbl_varietyBalance = 0;
                til_varietyBalance.setHelperText("Remaining balance : please retry");
                Toast.makeText(ctx,
                        "Server unreachable. Please check network connectivity and try again.",
                        Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("coop_accred", coop_accred);
                params.put("moaNumber", moaNumber);
                params.put("seedVariety", seedVariety);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(ctx);
        stringRequest.setRetryPolicy(
                new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(stringRequest);
    }

    //updating balance after confirming delivery
    private void updateVarietyBalance() {
        //updated computation here for considering isBuffer status 9(replacement)
        int mglbl_varietyBalance;
        if (glbl_misbuffer == 9) {
            mglbl_varietyBalance = 999;
        } else {
            mglbl_varietyBalance = glbl_varietyBalance - rcepDatabase.tmpDeliveryBatchDataDAO()
                    .getCurrentBatchVarietyTotalInput(glbl_variety, glbl_batchTicketNumber);
        }

        til_varietyBalance.setHelperText("Remaining balance : " + mglbl_varietyBalance);
    }


    /*private void requestVarietyBalance(final Context ctx, final String moaNumber,
                                       final String seedVariety) {
        Fun.progressMessage(mProgressDialog, "updating variety balance");
        //Log.e(TAG, "requestCoopDropoffPointLib: A");
        //request_url
        final String request_url =
                Fun.getAddress(glbl_season) + "/script/sg_variety_ten_percent_balance.php";
        //Log.e(TAG, "requestCoopDropoffPointLib: " + request_url);
        //prepare the Request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, request_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.e(TAG, "requestVarietyBalance: " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String conn = jsonObject.getString("conn");
                            String variety_balance = jsonObject.getString("variety_balance");

                            if (conn.equals("success")) {
                                Fun.progressStop(mProgressDialog);
                                glbl_varietyBalance = Integer.parseInt(variety_balance);
                                updateVarietyBalance();
                            } else {
                                Fun.progressStop(mProgressDialog);
                                glbl_varietyBalance = 0;
                                til_varietyBalance
                                        .setHelperText("Remaining balance : please retry");
                                Toast.makeText(ctx, "Error : Unable to connect in database server.",
                                        Toast.LENGTH_LONG).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Fun.progressStop(mProgressDialog);
                glbl_varietyBalance = 0;
                til_varietyBalance.setHelperText("Remaining balance : please retry");
                Toast.makeText(ctx,
                        "Server unreachable. Please check network connectivity and try again.",
                        Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("moa_number", moaNumber);
                params.put("commitment_variety", seedVariety);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(ctx);
        requestQueue.add(stringRequest);
    }*/

    private void requestVarietyBalance2(final Context ctx, final String accreditation_no,
                                        final String region_name, final String seed_variety,
                                        final String moa_number) {

        Log.e(TAG, "requestVarietyBalance2 :: " + accreditation_no + region_name + seed_variety +
                moa_number);
        Fun.progressMessage(mProgressDialog, "updating variety balance");
        Log.e(TAG, "requestCoopDropoffPointLib: A");
        //request_url
        final String request_url =
                Fun.getAddress(glbl_season) + "/script/sg_variety_regional_ten_percent_balance.php";
        //Log.e(TAG, "requestCoopDropoffPointLib: " + request_url);
        //prepare the Request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, request_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Fun.progressStop(mProgressDialog);
                        Log.e(TAG, "requestVarietyBalance2: " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            String variety_balance = jsonObject.getString("variety_balance");
                            Fun.progressStop(mProgressDialog);
                            glbl_varietyBalance = Integer.parseInt(variety_balance);
                            updateVarietyBalance();


                          /*  if (conn.equals("success")) {
                                Fun.progressStop(mProgressDialog);
                                glbl_varietyBalance = Integer.parseInt(variety_balance);
                                updateVarietyBalance();
                            } else {
                                Fun.progressStop(mProgressDialog);
                                glbl_varietyBalance = 0;
                                til_varietyBalance
                                        .setHelperText("Remaining balance : please retry");
                                Toast.makeText(ctx, "Error : Unable to connect in database server.",
                                        Toast.LENGTH_LONG).show();
                            }*/

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Fun.progressStop(mProgressDialog);
                            glbl_varietyBalance = 0;
                            til_varietyBalance
                                    .setHelperText("Remaining balance : please retry");
                            Toast.makeText(ctx, e.toString(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Fun.progressStop(mProgressDialog);
                glbl_varietyBalance = 0;
                til_varietyBalance.setHelperText("Remaining balance : please retry");
                Toast.makeText(ctx,
                        "Server unreachable. Please check network connectivity and try again.",
                        Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("accreditation_no", accreditation_no);
                params.put("region_name", region_name);
                params.put("seed_variety", seed_variety);
                params.put("moa_number", moa_number);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(ctx);
        stringRequest.setRetryPolicy(
                new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(stringRequest);
    }


    public void setCoopDropoffpoint(View view) {
        Log.e(TAG, "setCoopDropoffpoint: " + coopdropoff_selection.size());

        if (coopdropoff_selection.size() > 0) {
            final Handler handler = new Handler();
            SpinnerDialog ss = new SpinnerDialog(DeliveryBatchMainActivity.this,
                    coopdropoff_selection, "Select batch ticket");
            ss.setCancellable(false);
            ss.setShowKeyboard(false);
            ss.bindOnSpinerListener(new OnSpinerItemClick() {
                @Override
                public void onClick(final String item, final int position) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //set selected value
                            //imgv_delCoopDropoffpoint.setVisibility(View.VISIBLE);
                            et_coopDropoffpoint.setText(item);
                            glbl_deliveryDate = coopdropoff_source.get(position).getDelivery_date();
                            glbl_batchTicketNumber = coopdropoff_source.get(position)
                                    .getBatchTicketNumber();
                            glbl_instructeDeliveryVolume = coopdropoff_source.get(position)
                                    .getInstructed_delivery_volume();
                            updateRemainingBalance();
                            glbl_dropoffpointId = coopdropoff_source.get(position)
                                    .getDropoffPointId();
                            glbl_prvdropoffId = coopdropoff_source.get(position)
                                    .getPrv_dropoff_id();
                            glbl_region = coopdropoff_source.get(position).getRegion();
                            glbl_province = coopdropoff_source.get(position).getProvince();
                            glbl_municipality = coopdropoff_source.get(position).getMunicipality();
                            glbl_coopdropoffpoint = coopdropoff_source.get(position)
                                    .getDropOffPoint();
                            glbl_prv = coopdropoff_source.get(position).getPrv();
                            glbl_misbuffer = coopdropoff_source.get(position).getMisBuffer();

                            Log.e(TAG, "dropoffPointId->" + glbl_dropoffpointId +
                                    "\nprv_dropoff_id->" + glbl_prvdropoffId +
                                    "\nbatch->" + glbl_batchTicketNumber +
                                    "\ndate->" + glbl_deliveryDate +
                                    "\nvolume->" + glbl_instructeDeliveryVolume +
                                    "\ncoop_accreditation->" + glbl_coopAccreditation +
                                    "\nregion->" + glbl_region +
                                    "\nprovince->" + glbl_province +
                                    "\nmunicipality->" + glbl_municipality +
                                    "\ndropoffpoint->" + glbl_coopdropoffpoint +
                                    "\nprv->" + glbl_prv
                            );

                            /*clear values*/
                            et_seedVariety.setText("");
                            et_seedGrower.setText("");
                            et_seedTag.setText("");
                            et_bagsDelivered.setText("");
                        }
                    }, 250);
                }
            });
            ss.showSpinerDialog();
        } else {
            Toast.makeText(this,
                    "No available drop off points in your cooperative. Please contact your RC/PC to add new dropoff point",
                    Toast.LENGTH_LONG).show();
        }
    }


    void updateRemainingBalance() {
        tv_remainingVolume.setText(String.valueOf(
                glbl_instructeDeliveryVolume - rcepDatabase.tmpDeliveryBatchDataDAO()
                        .getTempDeliveryBatchTotal()));
    }

    public void delCoopDropoffpoint(View view) {
//        imgv_delCoopDropoffpoint.setVisibility(View.GONE);
        et_coopDropoffpoint.setText("");
        glbl_prvdropoffId = "";
        glbl_region = "";
        glbl_province = "";
        glbl_municipality = "";
        glbl_coopdropoffpoint = "";
        glbl_prv = "";
    }

    public void scanSeries(View view) {
        Fun.hideKeyboard(this);
        /*content_frame.setVisibility(View.VISIBLE);
        delivery_form.setVisibility(View.GONE);
        mCodeScanner.startPreview();*/
        barcodeLauncher.launch(new ScanOptions());
    }

    public void removeSeries(View view) {
        imgv_removeSeries.setVisibility(View.GONE);
        et_batchSeries.setText("");
    }

    public void checkBalance(View view) {
        String coop = prefUserAccount.getString(Fun.uaCoopAcronym(), "");
        final StringBuilder concatVarities = new StringBuilder();
        int currentDelivery = rcepDatabase.tblCurrentDeliveryCountDAO().getCurrentDeliveryTotal();
        int limit = rcepDatabase.tblTotalCommitmentDAO().getTotalCommitment();
        List<TblCurrentDeliveryCount> src = rcepDatabase.tblCurrentDeliveryCountDAO().getAll();
        String msg1, msg2;
        msg1 = "\nAlready confirmed " + currentDelivery + " out of " + limit + "\n\n\n";
        if (src.size() > 0) {
            for (TblCurrentDeliveryCount data : src) {
                concatVarities.append(/*data.getRegion() + "-" +*/
                        data.getProvince() + "-" + data.getMunicipality() + " = " + data
                                .getCurrent_committed() + " bags" + "\n\n");
            }
            msg2 = concatVarities.toString();
        } else {
            msg2 = "No deliveries found yet";
        }

        Fun.showLongMsgDialog(DeliveryBatchMainActivity.this, coop, msg1 + msg2);
      /*  final StringBuilder concatVarities = new StringBuilder();
        int currentDelivery = rcepDatabase.tblCurrentDeliveryCountDAO().getCurrentDeliveryTotal();
        int limit = rcepDatabase.tblTotalCommitmentDAO().getTotalCommitment();
        List<TblCurrentDeliveryCount> src = rcepDatabase.tblCurrentDeliveryCountDAO().getAll();
        String msg1, msg2;
        msg1 = "Already confirmed " + currentDelivery + " out of " + limit + " allocation, with remaining " + (limit - currentDelivery) + " bags to deliver.\n\n";
        if (src.size() > 0) {
            msg1 = msg1 + "Already confirmed,\n";
            for (TblCurrentDeliveryCount data : src) {
                concatVarities.append(data.getCurrent_committed() + " bags of " + data.getSeedVariety() + "\n");
            }
            msg2 = concatVarities.toString();
        } else {
            msg2 = "No deliveries found yet";
        }
        CFAlertDialog.Builder builder = new CFAlertDialog.Builder(DeliveryBatchMainActivity.this);
        builder.setDialogStyle(CFAlertDialog.CFAlertStyle.ALERT);
        builder.setTitle("Cooperative delivery summary");
        builder.setMessage(msg1 + msg2);
        builder.addButton("DONE", Color.parseColor("#FFFFFF"), Color.parseColor("#429ef4"), CFAlertDialog.CFAlertActionStyle.POSITIVE, CFAlertDialog.CFAlertActionAlignment.JUSTIFIED, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();*/
    }

    public void setSeedTag(View view) {
        if (rlaDetails_selection.size() > 0) {
            final Handler handler = new Handler();
            SpinnerDialog ss = new SpinnerDialog(DeliveryBatchMainActivity.this,
                    rlaDetails_selection, "Select seed tag");
            ss.setCancellable(false);
            ss.setShowKeyboard(false);
            ss.bindOnSpinerListener(new OnSpinerItemClick() {
                @Override
                public void onClick(final String item, final int position) {
                    glbl_seedTag = item.replace(" ", "").trim();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            et_seedTag.setText(glbl_seedTag);
                            glbl_rla_bag_limit = rlaDetails_source.get(position).getNoOfBags();
                            et_bagsDelivered.setText(String.valueOf(glbl_rla_bag_limit));
                        }
                    }, 250);
                }
            });
            ss.showSpinerDialog();
        } else {
            if (et_seedGrower.getText().toString().trim().equals("")) {
                Toast.makeText(this, "Please select seed grower first", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this,
                        "No laboratory results found. Please contact your RC/PC for more details",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    public void setSeedGrower(View view) {
        if (seedGrower_selection.size() > 0) {
            final Handler handler = new Handler();
            SpinnerDialog ss = new SpinnerDialog(DeliveryBatchMainActivity.this,
                    seedGrower_selection, "Select seed grower");
            ss.setCancellable(false);
            ss.setShowKeyboard(false);
            ss.bindOnSpinerListener(new OnSpinerItemClick() {
                @Override
                public void onClick(final String item, final int position) {
                    glbl_sgid = seedGrower_source.get(position).getSg_id();
                    Log.e(TAG, "sg_id: " + glbl_sgid);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            et_seedGrower.setText(item);
                            /*clear values*/
                            et_seedTag.setText("");
                            et_bagsDelivered.setText("");
                            /*clear arrays*/
                            rlaDetails_source.clear();
                            rlaDetails_selection.clear();
                            /*request rla details*/
                            requestCoopRlaDetails(DeliveryBatchMainActivity.this,
                                    glbl_coopAccreditation, glbl_moaNumber,
                                    String.valueOf(glbl_sgid), glbl_variety);
                        }
                    }, 250);
                }
            });
            ss.showSpinerDialog();
        } else {
            if (et_seedVariety.getText().toString().trim().equals("")) {
                Toast.makeText(this, "Please select seed variety first", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No seed grower found. Please select variety first",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void updateStationDB(final Context ctx, String address, String port,
                                 String table_name) {
        final String request_url = Fun.updateStationDB(address, port, table_name);
        Log.e(TAG, "updateStationDB: " + request_url);
        //prepare the Request
        StringRequest stringRequest =
                new StringRequest(Request.Method.GET, request_url, response -> {
                    //Fun.progressStop(mProgressDialog);
                    response = response.trim();
                    Log.e(TAG, "updateStationDB_response: " + response);
                }, error -> {
                    //Fun.progressStop(mProgressDialog);
                });
        RequestQueue requestQueue = Volley.newRequestQueue(ctx);
        requestQueue.add(stringRequest);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                -1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

}
