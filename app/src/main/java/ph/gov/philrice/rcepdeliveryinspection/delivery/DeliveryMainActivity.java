package ph.gov.philrice.rcepdeliveryinspection.delivery;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.Window;
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ph.gov.philrice.rcepdeliveryinspection.Fun;
import ph.gov.philrice.rcepdeliveryinspection.R;
import ph.gov.philrice.rcepdeliveryinspection.dbase.database.RCEPDatabase;
import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.LibSeeds;
import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TblCommitment;
import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TblCurrentDeliveryCount;
import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TblDelivery;
import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TblDeliveryStatus;
import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TblTotalCommitment;
import ph.gov.philrice.rcepdeliveryinspection.delivery.dataview.DeliverySGViewAdapter;
import ph.gov.philrice.rcepdeliveryinspection.delivery.dataview.DeliverySGViewData;
import ph.gov.philrice.rcepdeliveryinspection.inspection.dataview.SGDeliveryData;
import ph.gov.philrice.rcepdeliveryinspection.userLogin.VerifyLoginActivity;

public class DeliveryMainActivity extends AppCompatActivity
        implements DeliverySGViewAdapter.ItemClicked {
    private static final String TAG = "DeliveryMainActivity";
    //Views
    ProgressDialog mProgressDialog;
    private SwipeRefreshLayout srl_refreshList;
    SearchView sv_delivery;
    RecyclerView rv_delivery;
    TextView tv_deliveryTitle;
    //Variables
    SharedPreferences prefUserAccount;
    RCEPDatabase rcepDatabase;
    DeliverySGViewAdapter deliverySGViewAdapter;
    int glbl_checkDataAction;
    String glbl_acronym;


    ArrayList<SGDeliveryData> glbl_sourceData;
    ArrayList<DeliverySGViewData> glbl_mainViewData;

    String glbl_season;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //initialize screen
        Fun.fullScreen(this);//fullScreen
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
//        getSupportActionBar().hide(); // hide the title bar
        //setScreenActivityContents
        setContentView(R.layout.activity_delivery_main);

        initDelivery();

        Log.e(TAG, "cost: " + Fun.isCostValid("2."));
    }

    @Override
    protected void onResume() {
        super.onResume();

        requestDeliveryList(DeliveryMainActivity.this,
                prefUserAccount.getString(Fun.uaCoopCurrentMOA(), ""),
                prefUserAccount.getString(Fun.uaCoopAccreditation(), ""));

    }

    private void initDelivery() {
        glbl_season = "";
        glbl_mainViewData = new ArrayList<>();
        rcepDatabase = RCEPDatabase.getAppDatabase(this);
        prefUserAccount = getApplicationContext()
                .getSharedPreferences(Fun.userAccountPreference(), Context.MODE_PRIVATE);
        srl_refreshList = findViewById(R.id.srl_refreshList);
        tv_deliveryTitle = findViewById(R.id.tv_deliveryTitle);
        glbl_season = prefUserAccount.getString(Fun.uaSeason(), "");
        glbl_acronym = prefUserAccount.getString(Fun.uaCoopAcronym(), "");
        if (glbl_acronym.equals("")) {
            tv_deliveryTitle.setText("Delivery List");
        } else {
            tv_deliveryTitle.setText("Delivery List(" + glbl_acronym + ")");
        }

        sv_delivery = findViewById(R.id.sv_delivery);
        mProgressDialog = new ProgressDialog(this);
        rv_delivery = findViewById(R.id.rv_delivery);
        glbl_mainViewData = getDeliveryData();
        //set properties
        rv_delivery.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rv_delivery.setItemAnimator(new DefaultItemAnimator());
        deliverySGViewAdapter = new DeliverySGViewAdapter(this, glbl_mainViewData);
        deliverySGViewAdapter.setitemClickedListener(this);
        rv_delivery.setAdapter(deliverySGViewAdapter);

        sv_delivery.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                deliverySGViewAdapter.getFilter().filter(newText);
                return false;
            }
        });

        srl_refreshList.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                sv_delivery.setQuery("", false);
                sv_delivery.setIconified(true);
                requestDeliveryList(DeliveryMainActivity.this,
                        prefUserAccount.getString(Fun.uaCoopCurrentMOA(), ""),
                        prefUserAccount.getString(Fun.uaCoopAccreditation(), ""));

            }
        });


    }

    private void requestCurrentDelivery(final Context ctx, final String moaNo,
                                        final String accredNo) {
        Fun.progressStart(mProgressDialog, "", "verifying cooperative current deliveries");
        //url
        String request_url =
                Fun.getAddress(glbl_season) + "/script/sg_fetch_sent_delivery_request.php";
        //request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, request_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Log.e(TAG, "requestCurrentDelivery: " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String status = jsonObject.getString("status");
                            String message = jsonObject.getString("message");
                            Log.e(TAG, "status: " + status);
                            Log.e(TAG, "message: " + message);
                            switch (status) {
                                case "1":
                                    //do other functions here
                                    //parsing json
                                    JSONObject json = new JSONObject(message);
                                    //name of array is result
                                    JSONArray jArray = json.getJSONArray("result");
                                    JSONObject jData = null;
                                    Log.e(TAG, "qwerwqerwqer: ");
                                    for (int i = 0; i < jArray.length(); i++) {
                                        Log.e(TAG, "asdfasdfadsf");

                                        jData = jArray.getJSONObject(i);
                                        //decode html_encode value from server
                                        String mCoopAccreditation =
                                                jData.getString("coopAccreditation");
                                        Log.e(TAG, "mCoopAccreditation: " + mCoopAccreditation);
                                        String mMoaNumber = jData.getString("moa_number");
                                        Log.e(TAG, "mMoaNumber: " + mMoaNumber);
                                        String mRegion = jData.getString("region");
                                        Log.e(TAG, "mRegion: " + mRegion);
                                        String mProvince = jData.getString("province");
                                        Log.e(TAG, "mProvince: " + mProvince);
                                        String mMunicipality = jData.getString("municipality");
                                        Log.e(TAG, "mMunicipality: " + mMunicipality);
                                        String mSeedVariety = jData.getString("seedVariety");
                                        Log.e(TAG, "mMoaNumber: " + mSeedVariety);
                                        int mCurrentCommitted = jData.getInt("current_committed");
                                        Log.e(TAG, "mCurrentCommitted: " + mCurrentCommitted);


                                        //populating current commited
                                        TblCurrentDeliveryCount data =
                                                new TblCurrentDeliveryCount(mCoopAccreditation,
                                                        mMoaNumber, mRegion, mProvince,
                                                        mMunicipality, "", mCurrentCommitted);
                                        rcepDatabase.tblCurrentDeliveryCountDAO().insert(data);
                                    }
                                    //proceed updating total commitment
                                    totalCommitmentRequest(ctx, moaNo);
                                    break;
                                case "0":
                                    //proceed updating total commitment
                                    totalCommitmentRequest(ctx, moaNo);

                            /*Fun.progressStop(mProgressDialog);
                            Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show();*/
                                    /*     //rcepDatabase.tblDeliveryDAO().nukeTable();
                                     *//*  mDelivery.clear();
                            mDelivery.addAll(getDeliveries());
                            deliveryAdapter.notifyDataSetChanged();*//*
                            srl_refreshList.setRefreshing(false);
                            Toast.makeText(ctx, message, Toast.LENGTH_LONG).show();*/
                                    break;
                                case "3":
                                    Fun.progressStop(mProgressDialog);
                                    Toast.makeText(ctx,
                                            "Error : Unable to connect in database server.",
                                            Toast.LENGTH_LONG).show();
                                    break;
                            }
                            //Log.e(TAG, "onResponse: " + message);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Fun.progressStop(mProgressDialog);
                        Toast.makeText(ctx,
                                "Server unreachable. Please check network connectivity and try again.",
                                Toast.LENGTH_SHORT).show();
                    }
                }, 1000);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("moa_number", moaNo);
                params.put("accreditation_no", accredNo);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(ctx);
        requestQueue.add(stringRequest);
    }

    private void totalCommitmentRequest(final Context ctx, final String moa_number) {
        Fun.progressMessage(mProgressDialog, "verifying cooperative total commitment");
        //url
        final String mRequest =
                Fun.getAddress(glbl_season) + "/script/sg_total_commitment_request.php";
        //prepare the Request
        StringRequest stringRequest =
                new StringRequest(Request.Method.POST, mRequest, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e(TAG, "totalcommitment: " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String status = jsonObject.getString("status");
                            String message = jsonObject.getString("message");
                            switch (status) {
                                case "1":
                                    //do other functions here
                                    //parsing json
                                    JSONObject json = new JSONObject(message);
                                    //name of array is result
                                    JSONArray jArray = json.getJSONArray("result");
                                    JSONObject jData = null;
                                    for (int i = 0; i < jArray.length(); i++) {
                                        jData = jArray.getJSONObject(i);
                                        //decode html_encode value from server
                                        int totalCommitmentId = jData.getInt("totalCommitmentId");
                                        int total_value = jData.getInt("total_value");
                                        String moa_number = jData.getString("moa_number");
                                        //inserting values
                                        TblTotalCommitment totalCommitment =
                                                new TblTotalCommitment(totalCommitmentId,
                                                        total_value, moa_number);
                                        rcepDatabase.tblTotalCommitmentDAO()
                                                .insert(totalCommitment);
                                    }
                                    //proceed commitment request
                                    commitmentRequest(ctx, moa_number);
                                    break;
                                case "0":
                                    //proceed commitment request
                                    commitmentRequest(ctx, moa_number);
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
                        params.put("moa_number", moa_number);
                        return params;
                    }
                };
        RequestQueue requestQueue = Volley.newRequestQueue(ctx);
        requestQueue.add(stringRequest);
    }

    private void commitmentRequest(final Context ctx, final String moa_number) {
        Fun.progressMessage(mProgressDialog, "verifying cooperative commitment");
        //url
        final String request_article_url =
                Fun.getAddress(glbl_season) + "/script/sg_commitment_request.php";
        //prepare the Request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, request_article_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Handler handler = new Handler();
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String status = jsonObject.getString("status");
                            String message = jsonObject.getString("message");
                            switch (status) {
                                case "1":
                                    //parsing json
                                    JSONObject json = new JSONObject(message);
                                    //name of array is result
                                    JSONArray jArray = json.getJSONArray("result");
                                    JSONObject jData = null;
                                    for (int i = 0; i < jArray.length(); i++) {
                                        jData = jArray.getJSONObject(i);
                                        //decode html_encode value from server
                                        int commitmentId = jData.getInt("commitmentId");
                                        int commitment_value = jData.getInt("commitment_value");
                                        String commitment_variety =
                                                jData.getString("commitment_variety");
                                        String moa_number = jData.getString("moa_number");
                                        //verifying commitment if exists
                                        TblCommitment commitment =
                                                new TblCommitment(commitmentId, commitment_value,
                                                        commitment_variety, moa_number);
                                        rcepDatabase.tblCommitmentDAO().insert(commitment);
                                        //verifying of variety exists
                                        if (rcepDatabase.libSeedsDAO()
                                                .checkVariety(commitment_variety) == 0) {
                                            LibSeeds seeds = new LibSeeds(commitment_variety);
                                            rcepDatabase.libSeedsDAO().insertSeed(seeds);
                                        }
                                    }
                                    //done request
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (glbl_checkDataAction == 1) {
                                                //view dialog
                                                Fun.progressStop(mProgressDialog);
                                                String coop = prefUserAccount
                                                        .getString(Fun.uaCoopAcronym(), "");
                                                final StringBuilder concatVarities =
                                                        new StringBuilder();
                                                int currentDelivery =
                                                        rcepDatabase.tblCurrentDeliveryCountDAO()
                                                                .getCurrentDeliveryTotal();
                                                int limit = rcepDatabase.tblTotalCommitmentDAO()
                                                        .getTotalCommitment();
                                                List<TblCurrentDeliveryCount> src =
                                                        rcepDatabase.tblCurrentDeliveryCountDAO()
                                                                .getAll();
                                                String msg1, msg2;
                                                msg1 = "\nAlready confirmed " + currentDelivery +
                                                        " out of " + limit + "\n\n\n";
                                                if (src.size() > 0) {
                                                    for (TblCurrentDeliveryCount data : src) {
                                                        concatVarities
                                                                .append(/*data.getRegion() + "-" +*/
                                                                        data.getProvince() + "-" +
                                                                                data.getMunicipality() +
                                                                                " = " +
                                                                                data.getCurrent_committed() +
                                                                                " bags" + "\n\n");
                                                    }
                                                    msg2 = concatVarities.toString();
                                                } else {
                                                    msg2 = "No deliveries found yet";
                                                }

                                                Fun.showLongMsgDialog(ctx, coop, msg1 + msg2);

                                       /* CFAlertDialog.Builder builder = new CFAlertDialog.Builder(DeliveryMainActivity.this);
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
                                            } else {
                                                //add batchdelivery update case
                                                Fun.progressStop(mProgressDialog);
                                                Intent intent =
                                                        new Intent(DeliveryMainActivity.this,
                                                                DeliveryBatchMainActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                startActivity(intent);
                                                finish();
                                            }


                                        }
                                    }, 500);
                                    break;
                                case "0":
                                    //done request
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Fun.progressStop(mProgressDialog);
                                            Intent intent = new Intent(DeliveryMainActivity.this,
                                                    DeliveryBatchMainActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }, 500);
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
                params.put("moa_number", moa_number);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(ctx);
        requestQueue.add(stringRequest);
    }

    private void requestDeliveryList(final Context ctx, final String moaNo, final String accredNo) {
        //Fun.progressStart(mProgressDialog, "", "Requesting delivery data to server");

        Log.e(TAG, "requestDeliveryList: " + moaNo + "||" + accredNo);
        //url
        String request_url = Fun.getAddress(glbl_season) + "/script/sg_delivery_list_request.php";
        Log.e(TAG, "requestDeliveryList: " + request_url);
        //request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, request_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e(TAG, "onResponse: " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String status = jsonObject.getString("status");
                            String message = jsonObject.getString("message");
                            switch (status) {
                                case "1":
                                    rcepDatabase.tblDeliveryDAO().nukeTable();
                                    //do other functions here
                                    //parsing json
                                    JSONObject json = new JSONObject(message);
                                    //name of array is result
                                    JSONArray jArray = json.getJSONArray("result");
                                    JSONObject jData = null;
                                    for (int i = 0; i < jArray.length(); i++) {
                                        jData = jArray.getJSONObject(i);
                                        //decode html_encode value from server
                                        int mDeliveryId = jData.getInt("deliveryId");
                                        String mTicketNumber =
                                                Html.fromHtml(jData.getString("ticketNumber"))
                                                        .toString();
                                        String mBatchTicketNumber =
                                                Html.fromHtml(jData.getString("batchTicketNumber"))
                                                        .toString();
                                        String mCoopAccreditation =
                                                Html.fromHtml(jData.getString("coopAccreditation"))
                                                        .toString();
                                        String mSgAccreditation =
                                                Html.fromHtml(jData.getString("sgAccreditation"))
                                                        .toString();
                                        String mSeedTag = Html.fromHtml(jData.getString("seedTag"))
                                                .toString();
                                        String mSeedVariety =
                                                Html.fromHtml(jData.getString("seedVariety"))
                                                        .toString();
                                        int mTotalBagCount = jData.getInt("totalBagCount");
                                        String mDeliveryDate =
                                                Html.fromHtml(jData.getString("deliveryDate"))
                                                        .toString();
                                        int mUserId = jData.getInt("userId");
                                        String mDateCreated =
                                                Html.fromHtml(jData.getString("dateCreated"))
                                                        .toString();
                                        String mRegion =
                                                Html.fromHtml(jData.getString("region")).toString();
                                        String mProvince =
                                                Html.fromHtml(jData.getString("province"))
                                                        .toString();
                                        String mMunicipality =
                                                Html.fromHtml(jData.getString("municipality"))
                                                        .toString();
                                        String mDropOffPoint =
                                                Html.fromHtml(jData.getString("dropOffPoint"))
                                                        .toString();
                                        int actualTotal = jData.getInt("actualTotal");
                                        //checking and inserting not existing value
                                        if (Fun.checkDelivery(DeliveryMainActivity.this,
                                                mDeliveryId) == 0) {
                                            TblDelivery tblDelivery =
                                                    new TblDelivery(mDeliveryId, mTicketNumber,
                                                            mBatchTicketNumber, mCoopAccreditation,
                                                            mSgAccreditation, mSeedTag,
                                                            mSeedVariety, mTotalBagCount,
                                                            mDeliveryDate, mUserId, mDateCreated,
                                                            mRegion, mProvince, mMunicipality,
                                                            mDropOffPoint, actualTotal);
                                            rcepDatabase.tblDeliveryDAO()
                                                    .insertDelivery(tblDelivery);
                                        }
                                    }
                                    final Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            //proceed verification screen
                                  /*  mDelivery.clear();
                                    mDelivery.addAll(getDeliveries());
                                    deliveryAdapter.notifyDataSetChanged();*/
                                            requestDeliveryStatusList(DeliveryMainActivity.this,
                                                    Fun.allBatchTickets(
                                                            rcepDatabase.tblDeliveryDAO()
                                                                    .getAllbatchTicket()));
                                        }
                                    }, 1000);
                                    break;
                                case "0":
                                    rcepDatabase.tblDeliveryDAO().nukeTable();
                                    rcepDatabase.tblDeliveryStatusDAO().nukeTable();
                                    glbl_mainViewData.clear();
                                    glbl_mainViewData.addAll(getDeliveryData());
                                    deliverySGViewAdapter.notifyDataSetChanged();
                                    //rcepDatabase.tblDeliveryDAO().nukeTable();
                          /*  mDelivery.clear();
                            mDelivery.addAll(getDeliveries());
                            deliveryAdapter.notifyDataSetChanged();*/
                                    srl_refreshList.setRefreshing(false);
                                    Toast.makeText(ctx, message, Toast.LENGTH_LONG).show();
                                    break;
                                case "3":
                                    srl_refreshList.setRefreshing(false);
                                    Toast.makeText(ctx,
                                            "Error : Unable to connect in database server.",
                                            Toast.LENGTH_LONG).show();
                                    break;
                            }
                            //Log.e(TAG, "onResponse: " + message);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //proceed verification screen
                        srl_refreshList.setRefreshing(false);
                        //Fun.progressStop(mProgressDialog);
                        Toast.makeText(ctx,
                                "Server unreachable. Please check network connectivity and try again.",
                                Toast.LENGTH_SHORT).show();
                    }
                }, 1000);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("moa_number", moaNo);
                params.put("accreditation_no", accredNo);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(ctx);
        requestQueue.add(stringRequest);
    }

    private void requestDeliveryStatusList(final Context ctx, final String allBatchTicket) {
        // Fun.progressMessage(mProgressDialog,  "updating delivery batch status");
        //url
        String request_url =
                Fun.getAddress(glbl_season) + "/script/sg_delivery_status_list_request.php";
        //request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, request_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String status = jsonObject.getString("status");
                            String message = jsonObject.getString("message");
                            switch (status) {
                                case "1":
                                    //clear data before inserting values
                                    rcepDatabase.tblDeliveryStatusDAO().nukeTable();
                                    //do other functions here
                                    //parsing json
                                    JSONObject json = new JSONObject(message);
                                    //name of array is result
                                    JSONArray jArray = json.getJSONArray("result");
                                    JSONObject jData = null;
                                    for (int i = 0; i < jArray.length(); i++) {
                                        jData = jArray.getJSONObject(i);
                                        //decode html_encode value from server
                                        int mDeliveryStatusId = jData.getInt("deliveryStatusId");
                                        String mBatchTicketNumber =
                                                Html.fromHtml(jData.getString("batchTicketNumber"))
                                                        .toString();
                                        int mStatus = jData.getInt("status");
                                        String mDateCreated =
                                                Html.fromHtml(jData.getString("dateCreated"))
                                                        .toString();
                                        //checking and inserting not existing value
                                        if (Fun.checkDeliveryStatus(DeliveryMainActivity.this,
                                                mDeliveryStatusId) == 0) {
                                            TblDeliveryStatus tblDeliveryStatus =
                                                    new TblDeliveryStatus(mDeliveryStatusId,
                                                            mBatchTicketNumber, mStatus,
                                                            mDateCreated);
                                            rcepDatabase.tblDeliveryStatusDAO()
                                                    .insertDeliveryStatus(tblDeliveryStatus);
                                        }
                                    }

                                    final Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            //proceed verification screen
                                            glbl_mainViewData.clear();
                                            glbl_mainViewData.addAll(getDeliveryData());
                                            deliverySGViewAdapter.notifyDataSetChanged();
                                            srl_refreshList.setRefreshing(false);
                                        }
                                    }, 1000);
                                    break;
                                case "0":

                                    rcepDatabase.tblDeliveryStatusDAO().nukeTable();
                                    glbl_mainViewData.clear();
                                    glbl_mainViewData.addAll(getDeliveryData());
                                    deliverySGViewAdapter.notifyDataSetChanged();
                                    srl_refreshList.setRefreshing(false);
                                                   /*  mDelivery.clear();
                            mDelivery.addAll(getDeliveries());
                            deliveryAdapter.notifyDataSetChanged();*/
                          /*  srl_refreshList.setRefreshing(false);
                            Toast.makeText(ctx, message, Toast.LENGTH_LONG).show();*/
                                    break;
                                case "3":
                                    srl_refreshList.setRefreshing(false);
                                    Toast.makeText(ctx,
                                            "Error : Unable to connect in database server.",
                                            Toast.LENGTH_LONG).show();
                                    break;
                            }
                            //Log.e(TAG, "onResponse: " + message);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //proceed verification screen
                        srl_refreshList.setRefreshing(false);
                        //Fun.progressStop(mProgressDialog);
                        Toast.makeText(ctx,
                                "Server unreachable. Please check network connectivity and try again.",
                                Toast.LENGTH_SHORT).show();
                    }
                }, 1000);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("allBatchTicket", allBatchTicket);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(ctx);
        requestQueue.add(stringRequest);
    }

    @Override
    public void onBackPressed() {
        new MaterialAlertDialogBuilder(DeliveryMainActivity.this)
                .setTitle("RCEF DI")
                .setCancelable(false)
                .setMessage("Logout application?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(DeliveryMainActivity.this,
                                        VerifyLoginActivity.class);
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

        /*CFAlertDialog.Builder builder = new CFAlertDialog.Builder(DeliveryMainActivity.this);
        builder.setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET);
        builder.setTitle("RCEP App");
        builder.setMessage("Logout application?");
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

                                Intent intent = new Intent(DeliveryMainActivity.this,
                                        VerifyLoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }
                        }, 500);

                    }
                });

        builder.addButton("CANCEL", Color.parseColor("#FFFFFF"), Color.parseColor("#429ef4"),
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


    private ArrayList<DeliverySGViewData> getDeliveryData() {
        ArrayList<DeliverySGViewData> data = new ArrayList<>();

        for (SGDeliveryData s : rcepDatabase.joinsDAO().getDeliveryDetails()) {
            String mBatchTicketNumber = s.getBatchTicketNumber();
            String mVariety = s.getVariety();
            int mTotalBags = s.getTotalBags();
            String mDeliveryDate = s.getDeliveryDate();
            String mDropoffPoint = s.getDropoffPoint();
            int mStatus =
                    rcepDatabase.tblDeliveryStatusDAO().getDeliveryStatus(s.getBatchTicketNumber());
            //Log.e(TAG, "mStatus: " + mStatus);
            String mAsOf =
                    rcepDatabase.tblDeliveryStatusDAO().getDeliveryAsOf(s.getBatchTicketNumber()) ==
                            null ? "x" : rcepDatabase.tblDeliveryStatusDAO()
                            .getDeliveryAsOf(s.getBatchTicketNumber());
            //Log.e(TAG, "mAsOf: " + mAsOf);
            String mProvince = s.getProvince();
            String mMunicipality = s.getMunicipality();
            int actualTotal = s.getActualTotal();


            DeliverySGViewData details =
                    new DeliverySGViewData(mBatchTicketNumber, mVariety, mTotalBags, mDeliveryDate,
                            mDropoffPoint, mStatus, mAsOf, mProvince, mMunicipality,
                            s.getActualTotal());
            data.add(details);

         /*   Log.e(TAG, "batcTicket: " + s.getBatchTicketNumber()
                    + "\nvariety" + s.getVariety()
                    + "\ntotalBags" + s.getTotalBags()
                    + "\ndeliveryDate" + s.getDeliveryDate()
                    + "\ndropoffPoint" + s.getDropoffPoint()
                    + "\nstatus" + rcepDatabase.tblDeliveryStatusDAO().getDeliveryStatus(s.getBatchTicketNumber())
                    + "\nasOf" + rcepDatabase.tblDeliveryStatusDAO().getDeliveryAsOf(s.getBatchTicketNumber())
            );*/
        }


        return data;
    }


    public void addDeliveryBatch(View view) {
        glbl_checkDataAction = 2;

        new MaterialAlertDialogBuilder(DeliveryMainActivity.this)
                .setTitle("RCEF DI")
                .setCancelable(false)
                .setMessage("Add new batch delivery?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //clear values of tmpTable
                                rcepDatabase.tmpDeliveryBatchDataDAO().nukeTable();
                                //clear values of tblCurrentDelivery
                                rcepDatabase.tblCurrentDeliveryCountDAO().nukeTable();
                                rcepDatabase.tblCommitmentDAO().nukeTable();
                                rcepDatabase.tblTotalCommitmentDAO().nukeTable();
                                rcepDatabase.libSeedsDAO().nukeTable();

                                //do preparation of form here
                                /*perform requests for already committed seeds vs limit*/
                                String moa = prefUserAccount.getString(Fun.uaCoopCurrentMOA(), "");
                                String accred =
                                        prefUserAccount.getString(Fun.uaCoopAccreditation(), "");

                                requestCurrentDelivery(DeliveryMainActivity.this, moa, accred);

                       /* Intent intent = new Intent(DeliveryMainActivity.this, DeliveryBatchMainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();*/
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


        /*CFAlertDialog.Builder builder = new CFAlertDialog.Builder(DeliveryMainActivity.this);
        builder.setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET);
        builder.setTitle("RCEP App");
        builder.setMessage("Add new batch delivery?");
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
                                //clear values of tmpTable
                                rcepDatabase.tmpDeliveryBatchDataDAO().nukeTable();
                                //clear values of tblCurrentDelivery
                                rcepDatabase.tblCurrentDeliveryCountDAO().nukeTable();
                                rcepDatabase.tblCommitmentDAO().nukeTable();
                                rcepDatabase.tblTotalCommitmentDAO().nukeTable();
                                rcepDatabase.libSeedsDAO().nukeTable();

                                //do preparation of form here
                                *//*perform requests for already committed seeds vs limit*//*
                                String moa = prefUserAccount.getString(Fun.uaCoopCurrentMOA(), "");
                                String accred =
                                        prefUserAccount.getString(Fun.uaCoopAccreditation(), "");

                                requestCurrentDelivery(DeliveryMainActivity.this, moa, accred);

                       *//* Intent intent = new Intent(DeliveryMainActivity.this, DeliveryBatchMainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();*//*
                            }
                        }, 500);
                    }
                });

        builder.addButton("CANCEL", Color.parseColor("#FFFFFF"), Color.parseColor("#429ef4"),
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


    @Override
    public void onDispatched(final String batchTicketNumber) {
        Log.e(TAG, "onDispatched: " + batchTicketNumber);
        new MaterialAlertDialogBuilder(DeliveryMainActivity.this)
                .setTitle("RCEF DI")
                .setCancelable(false)
                .setMessage("Do you want to dispatch batch delivery " + batchTicketNumber + "?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dialogInterface.dismiss();
                                updateBatchStatus(DeliveryMainActivity.this, batchTicketNumber, "3",
                                        Fun.getCurrentDate());
                            }
                        }, 250);
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dialogInterface.dismiss();
                            }
                        }, 250);
                    }
                })
                .show();


        /*CFAlertDialog.Builder builder = new CFAlertDialog.Builder(DeliveryMainActivity.this);
        builder.setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET);
        builder.setTitle("RCEP App");
        builder.setMessage("Do you want to dispatch batch delivery " + batchTicketNumber + "?");
        builder.addButton("YES", Color.parseColor("#FFFFFF"), Color.parseColor("#429ef4"),
                CFAlertDialog.CFAlertActionStyle.POSITIVE,
                CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                                updateBatchStatus(DeliveryMainActivity.this, batchTicketNumber, "3",
                                        Fun.getCurrentDate());
                            }
                        }, 250);
                    }
                });
        builder.addButton("NO", Color.parseColor("#FFFFFF"), Color.parseColor("#429ef4"),
                CFAlertDialog.CFAlertActionStyle.POSITIVE,
                CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                            }
                        }, 250);
                    }
                });
        builder.show();*/
    }

    @Override
    public void onCancel(final String batchTicketNumber) {
        Log.e(TAG, "onCancel: " + batchTicketNumber);
        new MaterialAlertDialogBuilder(DeliveryMainActivity.this)
                .setTitle("RCEF DI")
                .setCancelable(false)
                .setMessage("Do you want to cancel batch delivery " + batchTicketNumber + "?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dialogInterface.dismiss();
                                updateBatchStatus2(DeliveryMainActivity.this, batchTicketNumber,
                                        "4",
                                        Fun.getCurrentDate());
                            }
                        }, 250);
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dialogInterface.dismiss();
                            }
                        }, 250);
                    }
                })
                .show();

        /*CFAlertDialog.Builder builder = new CFAlertDialog.Builder(DeliveryMainActivity.this);
        builder.setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET);
        builder.setTitle("RCEP App");
        builder.setMessage("Do you want to cancel batch delivery " + batchTicketNumber + "?");
        builder.addButton("YES", Color.parseColor("#FFFFFF"), Color.parseColor("#429ef4"),
                CFAlertDialog.CFAlertActionStyle.POSITIVE,
                CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                                updateBatchStatus2(DeliveryMainActivity.this, batchTicketNumber,
                                        "4",
                                        Fun.getCurrentDate());
                            }
                        }, 250);
                    }
                });
        builder.addButton("NO", Color.parseColor("#FFFFFF"), Color.parseColor("#429ef4"),
                CFAlertDialog.CFAlertActionStyle.POSITIVE,
                CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                            }
                        }, 250);
                    }
                });
        builder.show();*/
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


    private void updateBatchStatus(final Context ctx, final String batchTicketNumber,
                                   final String status, final String dateCreated) {
        Fun.progressStart(mProgressDialog, "", "Updating batch delivery status");
        //url
        String request_url =
                Fun.getAddress(glbl_season) + "/script/sg_delivery_batch_updatestatus_request.php";
        //request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, request_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        switch (response.trim()) {
                            case "1":
                                //success
                                String status_case = status == "3" ? "dispatched" : "cancelled";

                                new MaterialAlertDialogBuilder(DeliveryMainActivity.this)
                                        .setTitle("RCEF DI")
                                        .setCancelable(false)
                                        .setMessage("Batch delivery " + batchTicketNumber +
                                                " successfully " +
                                                status_case + "! Pull down to update list.")
                                        .setPositiveButton("OK",
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(
                                                            DialogInterface dialogInterface,
                                                            int i) {

                                                        final Handler handler = new Handler();
                                                        handler.postDelayed(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                dialogInterface.dismiss();
                                                                requestDeliveryList(
                                                                        DeliveryMainActivity.this,
                                                                        prefUserAccount.getString(
                                                                                Fun.uaCoopCurrentMOA(),
                                                                                ""),
                                                                        prefUserAccount.getString(
                                                                                Fun.uaCoopAccreditation(),
                                                                                ""));
                                                            }
                                                        }, 250);
                                                    }
                                                })
                                        .show();


                                /*CFAlertDialog.Builder builder =
                                        new CFAlertDialog.Builder(DeliveryMainActivity.this);
                                builder.setDialogStyle(CFAlertDialog.CFAlertStyle.ALERT);
                                builder.setTitle("RCEP App");
                                builder.setCancelable(false);
                                builder.setMessage(
                                        "Batch delivery " + batchTicketNumber + " successfully " +
                                                status_case + "! Pull down to update list.");
                                builder.addButton("OK", Color.parseColor("#FFFFFF"),
                                        Color.parseColor("#429ef4"),
                                        CFAlertDialog.CFAlertActionStyle.POSITIVE,
                                        CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(final DialogInterface dialog,
                                                                int which) {

                                                final Handler handler = new Handler();
                                                handler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        dialog.dismiss();
                                                        requestDeliveryList(
                                                                DeliveryMainActivity.this,
                                                                prefUserAccount.getString(
                                                                        Fun.uaCoopCurrentMOA(), ""),
                                                                prefUserAccount.getString(
                                                                        Fun.uaCoopAccreditation(),
                                                                        ""));
                                                    }
                                                }, 250);
                                            }
                                        });
                                builder.show();*/
                                Fun.progressStop(mProgressDialog);
                                break;
                            case "0":
                                Toast.makeText(ctx,
                                        "Failed to update batch status. Please retry sending batch delivery again",
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

    private void updateBatchStatus2(final Context ctx, final String batchTicketNumber,
                                    final String status, final String dateCreated) {
        Fun.progressStart(mProgressDialog, "", "Updating batch delivery status");
        //url
        String request_url =
                Fun.getAddress(glbl_season) + "/script/sg_delivery_batch_updatestatus_request2.php";
        //request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, request_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        switch (response.trim()) {
                            case "1":
                                //success
                                String status_case = status == "3" ? "dispatched" : "cancelled";
                                new MaterialAlertDialogBuilder(DeliveryMainActivity.this)
                                        .setTitle("RCEF DI")
                                        .setCancelable(false)
                                        .setMessage("Batch delivery " + batchTicketNumber +
                                                " successfully " +
                                                status_case + "! Pull down to update list.")
                                        .setPositiveButton("OK",
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(
                                                            DialogInterface dialogInterface,
                                                            int i) {
                                                        final Handler handler = new Handler();
                                                        handler.postDelayed(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                dialogInterface.dismiss();
                                                                requestDeliveryList(
                                                                        DeliveryMainActivity.this,
                                                                        prefUserAccount.getString(
                                                                                Fun.uaCoopCurrentMOA(),
                                                                                ""),
                                                                        prefUserAccount.getString(
                                                                                Fun.uaCoopAccreditation(),
                                                                                ""));
                                                            }
                                                        }, 250);
                                                    }
                                                })
                                        .show();


                                /*CFAlertDialog.Builder builder =
                                        new CFAlertDialog.Builder(DeliveryMainActivity.this);
                                builder.setDialogStyle(CFAlertDialog.CFAlertStyle.ALERT);
                                builder.setTitle("RCEP App");
                                builder.setCancelable(false);
                                builder.setMessage(
                                        "Batch delivery " + batchTicketNumber + " successfully " +
                                                status_case + "! Pull down to update list.");
                                builder.addButton("OK", Color.parseColor("#FFFFFF"),
                                        Color.parseColor("#429ef4"),
                                        CFAlertDialog.CFAlertActionStyle.POSITIVE,
                                        CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(final DialogInterface dialog,
                                                                int which) {

                                                final Handler handler = new Handler();
                                                handler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        dialog.dismiss();
                                                        requestDeliveryList(
                                                                DeliveryMainActivity.this,
                                                                prefUserAccount.getString(
                                                                        Fun.uaCoopCurrentMOA(), ""),
                                                                prefUserAccount.getString(
                                                                        Fun.uaCoopAccreditation(),
                                                                        ""));
                                                    }
                                                }, 250);
                                            }
                                        });
                                builder.show();*/
                                Fun.progressStop(mProgressDialog);
                                break;
                            case "0":
                                Toast.makeText(ctx,
                                        "Failed to update batch status. Please retry sending batch delivery again",
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

    public void viewCoopDeliverySummary(View view) {
        glbl_checkDataAction = 1;
        String moa = prefUserAccount.getString(Fun.uaCoopCurrentMOA(), "");
        String accred = prefUserAccount.getString(Fun.uaCoopAccreditation(), "");

        //clear values of tmpTable
        rcepDatabase.tmpDeliveryBatchDataDAO().nukeTable();
        //clear values of tblCurrentDelivery
        rcepDatabase.tblCurrentDeliveryCountDAO().nukeTable();
        rcepDatabase.tblCommitmentDAO().nukeTable();
        rcepDatabase.tblTotalCommitmentDAO().nukeTable();

        requestCurrentDelivery(DeliveryMainActivity.this, moa, accred);
    }
}
