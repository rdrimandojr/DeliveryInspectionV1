package ph.gov.philrice.rcepdeliveryinspection.inspection;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import android.text.Html;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.permissionx.guolindev.PermissionX;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import ph.gov.philrice.rcepdeliveryinspection.Fun;
import ph.gov.philrice.rcepdeliveryinspection.R;
import ph.gov.philrice.rcepdeliveryinspection.dbase.database.RCEPDatabase;
import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TblActualDelivery;
import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TblDeliveryInspection;
import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TblDeliveryStatus;
import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TblDeliveryStatusAppLocal;
import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TblInspection;
import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TblSampling;
import ph.gov.philrice.rcepdeliveryinspection.inspection.dataview.InspectionSIViewAdapter;
import ph.gov.philrice.rcepdeliveryinspection.inspection.dataview.InspectionSIViewData;
import ph.gov.philrice.rcepdeliveryinspection.inspection.dataview.SGDeliveryData;
import ph.gov.philrice.rcepdeliveryinspection.userLogin.VerifyLoginActivity;

public class InspectionMainActivity extends AppCompatActivity
        implements InspectionSIViewAdapter.ItemClicked {
    private static final String TAG = "InspectionMainActivity";
    //Views
    private SwipeRefreshLayout srl_refreshList;
    SearchView sv_inspection;
    RecyclerView rv_inspection;
    //Variables
    ProgressDialog mProgressDialog;
    SharedPreferences prefUserAccount;
    SharedPreferences prefDelivery;
    SharedPreferences prefTempInspectionData;
    RCEPDatabase rcepDatabase;
    InspectionSIViewAdapter inspectionSIViewAdapter;
    /* ArrayList mInspection;*/
    String glbl_actualBagDelivered;

    ArrayList<InspectionSIViewData> glbl_mainViewData;
    JSONObject jsonObject;


    private String glbl_imgSampling;
    private String glbl_imgSamplingPath;
    private String glbl_imgActualDelivery;
    private String glbl_imgActualDeliveryPath;
    private String glbl_imgAccounting;
    private String glbl_imgAccountingPath;
    private String glbl_onSendBatchTicketNumber;
    private int glbl_sendingMode; //0 for local || 1 for central

    private String glbl_season, glbl_domain, glbl_port;


    String[] permissions = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //initialize screen
        Fun.fullScreen(this);//fullScreen
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
//        getSupportActionBar().hide(); // hide the title bar
        //setScreenActivityContents
        setContentView(R.layout.activity_inspection_main);

        if (Build.VERSION.SDK_INT >= 33) {
            permissions = new String[]{Manifest.permission.INTERNET,
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_MEDIA_IMAGES};
        } else {
            permissions = new String[]{Manifest.permission.INTERNET,
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE};
        }

        initInspection();

    }

    private void initInspection() {
        prefDelivery = getApplicationContext()
                .getSharedPreferences(Fun.deliveryPreference(), Context.MODE_PRIVATE);
        prefUserAccount = getApplicationContext()
                .getSharedPreferences(Fun.userAccountPreference(), Context.MODE_PRIVATE);
        prefTempInspectionData = getApplicationContext()
                .getSharedPreferences(Fun.tempInspectionPreference(), Context.MODE_PRIVATE);
        //Log.e(TAG, "getAddressLog: " + DebugDB.getAddressLog());

        glbl_domain = prefUserAccount.getString(Fun.uaStationDomain(), "");
        glbl_port = prefUserAccount.getString(Fun.uaStationPort(), "");

        glbl_season = "";
        glbl_mainViewData = new ArrayList<>();
        rcepDatabase = RCEPDatabase.getAppDatabase(this);

        srl_refreshList = findViewById(R.id.srl_refreshList);
        sv_inspection = findViewById(R.id.sv_inspection);
        rv_inspection = findViewById(R.id.rv_inspection);
        mProgressDialog = new ProgressDialog(this);
        glbl_season = prefUserAccount.getString(Fun.uaSeason(), "");
      /*  mInspection = new ArrayList();
        mInspection = getInspectorList();*/

        glbl_mainViewData = getDeliveryData();
        //set properties
        rv_inspection.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rv_inspection.setItemAnimator(new DefaultItemAnimator());
        inspectionSIViewAdapter = new InspectionSIViewAdapter(this, glbl_mainViewData);
        inspectionSIViewAdapter.setitemClickedListener(this);
        rv_inspection.setAdapter(inspectionSIViewAdapter);

        sv_inspection.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                inspectionSIViewAdapter.getFilter().filter(newText);
                return false;
            }
        });

        srl_refreshList.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //srl_refreshList.setRefreshing(false);
                if (Fun.isCompletedCentralDataSending(InspectionMainActivity.this) ==
                        0/* || Fun.isCompletedLocalDataSending(InspectionMainActivity.this) == 0*/) {
                    srl_refreshList.setRefreshing(false);
                    Toast.makeText(InspectionMainActivity.this,
                            "Please retry to send all incomplete data before updating list",
                            Toast.LENGTH_LONG).show();

                } else {
                    requestDeliveryList(InspectionMainActivity.this,
                            String.valueOf(prefUserAccount.getInt(Fun.uaUserId(), 0)),
                            Fun.getCurrentYear());
                }

/*
                if (rcepDatabase.tblSendingStatusDAO().hasPendingLocal() > 0) {

                    srl_refreshList.setRefreshing(false);
                } else {

                }*/


                //requestInspectorList(InspectionMainActivity.this, String.valueOf(prefUserAccount.getInt(Fun.uaUserId(), 0)), Fun.getCurrentYear());
            }
        });

        //TEst

        createFolder(Fun.getDir());

    }

    private void pingLocalServer(final Context ctx, String dynamicIP) {
        Fun.progressStart(mProgressDialog, "", "Checking local network connectivity to server");
        final SharedPreferences.Editor editor = prefUserAccount.edit();
        final AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        //url
        final String url_request = "http://" + dynamicIP + "/" + Fun.scriptPath() + "/ping.php";
        Log.e(TAG, "login: " + url_request);
        //prepare the Request
        StringRequest stringRequest =
                new StringRequest(Request.Method.GET, url_request, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Log.e(TAG, "onResponse: " + response);
                        Fun.progressStop(mProgressDialog);
                        if (response.equals("1")) {
                            new MaterialAlertDialogBuilder(InspectionMainActivity.this)
                                    .setTitle("RCEF DI")
                                    .setCancelable(false)
                                    .setMessage("Already connected to local server : " +
                                            Fun.getDynamicIP(prefUserAccount, ctx))
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface,
                                                            int i) {
                                            dialogInterface.dismiss();
                                        }
                                    })
                                    .show();

                            /*CFAlertDialog.Builder builder =
                                    new CFAlertDialog.Builder(InspectionMainActivity.this);
                            builder.setDialogStyle(CFAlertDialog.CFAlertStyle.ALERT);
                            builder.setMessage("Already connected to local server : " +
                                    Fun.getDynamicIP(prefUserAccount, ctx));
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
                            builder.setTitle("Connection not found.");
                            builder.setMessage(
                                    "Check network connection and enter local server IP address");
                            builder.setCancelable(false);
                            // Set up the input
                            final EditText input = new EditText(ctx);
                            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                            input.setInputType(InputType.TYPE_CLASS_PHONE);
                            builder.setView(input);
                            // Set up the buttons
                            builder.setPositiveButton("SET", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String dynamicIP = input.getText().toString().trim();
                                    editor.putString(Fun.uaDynamicIP(), dynamicIP);
                                    editor.apply();
                                }
                            });
                            builder.setNegativeButton("CANCEL",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    });
                            builder.show();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Fun.progressStop(mProgressDialog);
                        builder.setTitle("Connection not found.");
                        builder.setMessage(
                                "Check network connection and enter local server IP address");
                        builder.setCancelable(false);
                        // Set up the input
                        final EditText input = new EditText(ctx);
                        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                        input.setInputType(InputType.TYPE_CLASS_PHONE);
                        builder.setView(input);
                        // Set up the buttons
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String dynamicIP = input.getText().toString().trim();
                                editor.putString(Fun.uaDynamicIP(), dynamicIP);
                                editor.apply();
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder.show();
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(ctx);
        requestQueue.add(stringRequest);
    }

    public void createFolder(String fname) {
        String myfolder = Environment.getExternalStorageDirectory() + "/" + fname;
        File f = new File(myfolder);
        if (!f.exists()) {
            if (!f.mkdir()) {
                //Toast.makeText(this, myfolder+" can't be created.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onCreate: " + myfolder + " can't be created.");
            } else {
                //  Toast.makeText(this, myfolder+" can be created.", Toast.LENGTH_SHORT).show();
                Log.v(TAG, "onCreate: " + myfolder + " can be created.");
            }
        } else {
            Log.e(TAG, "onCreate: " + myfolder + " already exist.");
        }
    }

    private ArrayList<InspectionSIViewData> getDeliveryData() {
        ArrayList<InspectionSIViewData> data = new ArrayList<>();

        for (SGDeliveryData s : rcepDatabase.joinsDAO().getDeliveryInspectionDetails()) {
            String mBatchTicketNumber = s.getBatchTicketNumber();
            String mVariety = s.getVariety();
            int mTotalBags = s.getTotalBags();
            String mDeliveryDate = s.getDeliveryDate();
            String mDropoffPoint = s.getDropoffPoint();
            int mStatus =
                    rcepDatabase.tblDeliveryStatusDAO().getDeliveryStatus(s.getBatchTicketNumber());
            String mAsOf =
                    rcepDatabase.tblDeliveryStatusDAO().getDeliveryAsOf(s.getBatchTicketNumber()) ==
                            null ? "x" : rcepDatabase.tblDeliveryStatusDAO()
                            .getDeliveryAsOf(s.getBatchTicketNumber());
            String mProvince = s.getProvince();
            String mMunicipality = s.getMunicipality();
            String seed_distribution_mode = rcepDatabase.tblDeliveryInspectionDAO()
                    .getSeedDistributionMode(mBatchTicketNumber);

            InspectionSIViewData details =
                    new InspectionSIViewData(mBatchTicketNumber, mVariety, mTotalBags,
                            mDeliveryDate, mDropoffPoint, mStatus, mAsOf, mProvince, mMunicipality,
                            seed_distribution_mode);
            data.add(details);
        }


        return data;
    }

    /*private ArrayList<InspectionData> getInspectorList() {
        ArrayList<InspectionData> inspectionData = new ArrayList<>();
        *//*List<InspectionData> source = rcepDatabase.inspectionDataDAO().getInspectionData();
        for (InspectionData d : source) {
            InspectionData data = new InspectionData(d.getDeliveryId(), d.getTicketNumber(), d.getCoopAccreditation(), d.getSgAccreditation(), d.getSeedTag(), d.getSeedVariety(), d.getSeedClass(), d.getTotalWeight(), d.getWeightPerBag(), d.getDeliveryDate(), d.getDeliverTo(), d.getCoordinates(), d.getStatus(), d.getUserId(), d.getDateCreated());
            inspectionData.add(data);
        }*//*
        return inspectionData;
    }*/

    public void showMenu(View view) {
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select action");
        // choice list
        String[] animals = {"View History", "Cancel"};
        builder.setItems(animals, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: // View History
                        Toast.makeText(InspectionMainActivity.this, "Function under construction.",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case 1: // Cancel
                        dialog.dismiss();
                        break;
                }
            }
        });
        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
    }

    private void uploadImageSampling(Bitmap bitmap, String imageName) {
        Fun.progressMessage(mProgressDialog, "Uploading image " + imageName);
        String upload_URL =
                Fun.uploadURL(Fun.getDynamicIP(prefUserAccount, this), glbl_sendingMode);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 35, byteArrayOutputStream);
        String encodedImage =
                Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
        try {
            jsonObject = new JSONObject();
            jsonObject.put("name", imageName);
            jsonObject.put("image", encodedImage);
        } catch (JSONException e) {
            Log.e("JSONObject Here", e.toString());
        }
        JsonObjectRequest jsonObjectRequest =
                new JsonObjectRequest(Request.Method.POST, upload_URL, jsonObject,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject jsonObject) {
                                //Log.e("resultImgSampling", jsonObject.toString());
                                try {
                                    JSONObject mJsonObject = new JSONObject(jsonObject.toString());
                                    int success = mJsonObject.getInt("success");
                                    String message = mJsonObject.getString("message");
                                    if (success == 1) {
                                        //proceed uploading second image(actual delivery image)
                                        /*String fileLocation =
                                                Environment.getExternalStorageDirectory() + "/" +
                                                        Fun.getDir() + File.separator +
                                                        glbl_imgActualDelivery;*/
                                        String fileLocation = glbl_imgActualDeliveryPath;
                                        Bitmap preview = BitmapFactory.decodeFile(fileLocation);
                                        uploadImageActualDelivery(preview, glbl_imgActualDelivery);
                                    } else {
                                        Fun.progressStop(mProgressDialog);
                                        Toast.makeText(InspectionMainActivity.this,
                                                "Fail to upload image. Please try again",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(InspectionMainActivity.this,
                                "Something went wrong. Please check network connectivity and try again",
                                Toast.LENGTH_LONG).show();
                        Fun.progressStop(mProgressDialog);
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(InspectionMainActivity.this);
        requestQueue.add(jsonObjectRequest);
    }

    private void uploadImageActualDelivery(Bitmap bitmap, String imageName) {
        Fun.progressMessage(mProgressDialog, "Uploading image " + imageName);
        String upload_URL =
                Fun.uploadURL(Fun.getDynamicIP(prefUserAccount, this), glbl_sendingMode);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 35, byteArrayOutputStream);
        String encodedImage =
                Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
        try {
            jsonObject = new JSONObject();
            jsonObject.put("name", imageName);
            jsonObject.put("image", encodedImage);
        } catch (JSONException e) {
            Log.e("JSONObject Here", e.toString());
        }
        JsonObjectRequest jsonObjectRequest =
                new JsonObjectRequest(Request.Method.POST, upload_URL, jsonObject,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject jsonObject) {
                                try {
                                    JSONObject mJsonObject = new JSONObject(jsonObject.toString());
                                    int success = mJsonObject.getInt("success");
                                    String message = mJsonObject.getString("message");
                                    if (success == 1) {
                                        if (glbl_imgAccounting.length() > 0) {
                                            String fileLocation = glbl_imgAccountingPath;
                                            Bitmap preview = BitmapFactory.decodeFile(fileLocation);
                                            uploadImageAccounting(preview, glbl_imgAccounting);
                                        } else {
                                            rcepDatabase.tblSendingStatusDAO()
                                                    .updateExistingLocal(1, 2,
                                                            glbl_onSendBatchTicketNumber);
                                            if (glbl_sendingMode == 0 || glbl_sendingMode == 3) {
                                                //local
                                                String prv_dropoff_id = "";
                                                String coop_accreditation = "";
                                                String region = "";
                                                String province = "";
                                                String municipality = "";
                                                String dropoffpoint = "";
                                                String prv = "";
                                                //assign values
                                                for (TblDeliveryInspection data : rcepDatabase
                                                        .tblDeliveryInspectionDAO()
                                                        .getSingleDelivery(
                                                                glbl_onSendBatchTicketNumber)) {
                                                    prv_dropoff_id = data.getPrv_dropoff_id();
                                                    coop_accreditation =
                                                            data.getCoopAccreditation();
                                                    region = data.getRegion();
                                                    province = data.getProvince();
                                                    municipality = data.getMunicipality();
                                                    dropoffpoint = data.getDropOffPoint();
                                                    prv = data.getPrv();
                                                }
                                                //proceed on updating local server dropoffpoint
                                                updateLocalServerDropoffPoint(
                                                        InspectionMainActivity.this, prv_dropoff_id,
                                                        coop_accreditation, region, province,
                                                        municipality, dropoffpoint, prv);
                                            } else {
                                                //central
                                                //flag status of sending central
                                                rcepDatabase.tblInspectionDAO()
                                                        .successSendingCentral(
                                                                glbl_onSendBatchTicketNumber);
                                                rcepDatabase.tblSamplingDAO().successSendingCentral(
                                                        glbl_onSendBatchTicketNumber);
                                                rcepDatabase.tblActualDeliveryDAO()
                                                        .successSendingCentral(
                                                                glbl_onSendBatchTicketNumber);
                                                //stop dialog
                                                Fun.progressStop(mProgressDialog);

                                                File sample = new File(glbl_imgSamplingPath);
                                                File actual = new File(glbl_imgActualDeliveryPath);
                                                if (sample.exists()) {
                                                    sample.delete();
                                                }

                                                if (actual.exists()) {
                                                    actual.delete();
                                                }


                                                new MaterialAlertDialogBuilder(
                                                        InspectionMainActivity.this)
                                                        .setTitle("RCEF DI")
                                                        .setCancelable(false)
                                                        .setMessage("Batch delivery " +
                                                                glbl_onSendBatchTicketNumber +
                                                                " data successfully sent!")
                                                        .setPositiveButton("DONE",
                                                                new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(
                                                                            DialogInterface dialogInterface,
                                                                            int i) {
                                                                        final Handler handler =
                                                                                new Handler();
                                                                        handler.postDelayed(
                                                                                new Runnable() {
                                                                                    @Override
                                                                                    public void run() {
                                                                                        dialogInterface.dismiss();
                                                                                        glbl_mainViewData.clear();
                                                                                        glbl_mainViewData
                                                                                                .addAll(getDeliveryData());
                                                                                        inspectionSIViewAdapter
                                                                                                .notifyDataSetChanged();
                                                                                    }
                                                                                }, 500);
                                                                    }
                                                                })
                                                        .show();

                                                /*CFAlertDialog.Builder builder =
                                                        new CFAlertDialog.Builder(
                                                                InspectionMainActivity.this);
                                                builder.setDialogStyle(
                                                        CFAlertDialog.CFAlertStyle.ALERT);
                                                builder.setTitle("RCEP App");
                                                builder.setMessage("Batch delivery " +
                                                        glbl_onSendBatchTicketNumber +
                                                        " data successfully sent!");
                                                builder.addButton("DONE",
                                                        Color.parseColor("#FFFFFF"),
                                                        Color.parseColor("#429ef4"),
                                                        CFAlertDialog.CFAlertActionStyle.POSITIVE,
                                                        CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                                                        new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(
                                                                    final DialogInterface dialog,
                                                                    int which) {
                                                                final Handler handler =
                                                                        new Handler();
                                                                handler.postDelayed(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        dialog.dismiss();
                                                                        glbl_mainViewData.clear();
                                                                        glbl_mainViewData
                                                                                .addAll(getDeliveryData());
                                                                        inspectionSIViewAdapter
                                                                                .notifyDataSetChanged();
                                                                    }
                                                                }, 500);
                                                            }
                                                        });
                                                builder.show();*/
                                            }
                                        }

                                    } else {
                                        Fun.progressStop(mProgressDialog);
                                        Toast.makeText(InspectionMainActivity.this,
                                                "Fail to upload image. Please try again",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                               /* try {
                                    JSONObject mJsonObject = new JSONObject(jsonObject.toString());
                                    int success = mJsonObject.getInt("success");
                                    String message = mJsonObject.getString("message");
                                    if (success == 1) {
                                        rcepDatabase.tblSendingStatusDAO().updateExistingLocal(1, 2,
                                                glbl_onSendBatchTicketNumber);
                                        if (glbl_sendingMode == 0 || glbl_sendingMode == 3) {
                                            //local
                                            String prv_dropoff_id = "";
                                            String coop_accreditation = "";
                                            String region = "";
                                            String province = "";
                                            String municipality = "";
                                            String dropoffpoint = "";
                                            String prv = "";
                                            //assign values
                                            for (TblDeliveryInspection data : rcepDatabase
                                                    .tblDeliveryInspectionDAO().getSingleDelivery(
                                                            glbl_onSendBatchTicketNumber)) {
                                                prv_dropoff_id = data.getPrv_dropoff_id();
                                                coop_accreditation = data.getCoopAccreditation();
                                                region = data.getRegion();
                                                province = data.getProvince();
                                                municipality = data.getMunicipality();
                                                dropoffpoint = data.getDropOffPoint();
                                                prv = data.getPrv();
                                            }
                                            //proceed on updating local server dropoffpoint
                                            updateLocalServerDropoffPoint(
                                                    InspectionMainActivity.this, prv_dropoff_id,
                                                    coop_accreditation, region, province,
                                                    municipality, dropoffpoint, prv);
                                        } else {
                                            //central
                                            //flag status of sending central
                                            rcepDatabase.tblInspectionDAO().successSendingCentral(
                                                    glbl_onSendBatchTicketNumber);
                                            rcepDatabase.tblSamplingDAO().successSendingCentral(
                                                    glbl_onSendBatchTicketNumber);
                                            rcepDatabase.tblActualDeliveryDAO()
                                                    .successSendingCentral(
                                                            glbl_onSendBatchTicketNumber);
                                            //stop dialog
                                            Fun.progressStop(mProgressDialog);

                                            File sample = new File(glbl_imgSamplingPath);
                                            File actual = new File(glbl_imgActualDeliveryPath);
                                            if (sample.exists()) {
                                                sample.delete();
                                            }

                                            if (actual.exists()) {
                                                actual.delete();
                                            }


                                            CFAlertDialog.Builder builder =
                                                    new CFAlertDialog.Builder(
                                                            InspectionMainActivity.this);
                                            builder.setDialogStyle(
                                                    CFAlertDialog.CFAlertStyle.ALERT);
                                            builder.setTitle("RCEP App");
                                            builder.setMessage("Batch delivery " +
                                                    glbl_onSendBatchTicketNumber +
                                                    " data successfully sent!");
                                            builder.addButton("DONE", Color.parseColor("#FFFFFF"),
                                                    Color.parseColor("#429ef4"),
                                                    CFAlertDialog.CFAlertActionStyle.POSITIVE,
                                                    CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                                                    new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(
                                                                final DialogInterface dialog,
                                                                int which) {
                                                            final Handler handler = new Handler();
                                                            handler.postDelayed(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    dialog.dismiss();
                                                                    glbl_mainViewData.clear();
                                                                    glbl_mainViewData
                                                                            .addAll(getDeliveryData());
                                                                    inspectionSIViewAdapter
                                                                            .notifyDataSetChanged();
                                                                }
                                                            }, 500);
                                                        }
                                                    });
                                            builder.show();
                                        }
                                    } else {
                                        Fun.progressStop(mProgressDialog);
                                        Toast.makeText(InspectionMainActivity.this,
                                                "Fail to upload image. Please try again",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }*/

                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(InspectionMainActivity.this,
                                "Something went wrong. Please check network connectivity and try again",
                                Toast.LENGTH_LONG).show();
                        Fun.progressStop(mProgressDialog);
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(InspectionMainActivity.this);
        requestQueue.add(jsonObjectRequest);
    }

    private void uploadImageAccounting(Bitmap bitmap, String imageName) {
        Fun.progressMessage(mProgressDialog, "Uploading image " + imageName);
        String upload_URL =
                Fun.uploadURL(Fun.getDynamicIP(prefUserAccount, this), glbl_sendingMode);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 35, byteArrayOutputStream);
        String encodedImage =
                Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
        try {
            jsonObject = new JSONObject();
            jsonObject.put("name", imageName);
            jsonObject.put("image", encodedImage);
        } catch (JSONException e) {
            Log.e("JSONObject Here", e.toString());
        }
        JsonObjectRequest jsonObjectRequest =
                new JsonObjectRequest(Request.Method.POST, upload_URL, jsonObject,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject jsonObject) {
                                Log.e("uploadImageAccounting", jsonObject.toString());
                                try {
                                    JSONObject mJsonObject = new JSONObject(jsonObject.toString());
                                    int success = mJsonObject.getInt("success");
                                    String message = mJsonObject.getString("message");
                                    if (success == 1) {
                                        rcepDatabase.tblSendingStatusDAO().updateExistingLocal(1, 2,
                                                glbl_onSendBatchTicketNumber);
                                        if (glbl_sendingMode == 0 || glbl_sendingMode == 3) {
                                            //local
                                            String prv_dropoff_id = "";
                                            String coop_accreditation = "";
                                            String region = "";
                                            String province = "";
                                            String municipality = "";
                                            String dropoffpoint = "";
                                            String prv = "";
                                            //assign values
                                            for (TblDeliveryInspection data : rcepDatabase
                                                    .tblDeliveryInspectionDAO().getSingleDelivery(
                                                            glbl_onSendBatchTicketNumber)) {
                                                prv_dropoff_id = data.getPrv_dropoff_id();
                                                coop_accreditation = data.getCoopAccreditation();
                                                region = data.getRegion();
                                                province = data.getProvince();
                                                municipality = data.getMunicipality();
                                                dropoffpoint = data.getDropOffPoint();
                                                prv = data.getPrv();
                                            }
                                            //proceed on updating local server dropoffpoint
                                            updateLocalServerDropoffPoint(
                                                    InspectionMainActivity.this, prv_dropoff_id,
                                                    coop_accreditation, region, province,
                                                    municipality, dropoffpoint, prv);
                                        } else {
                                            //central
                                            //flag status of sending central
                                            rcepDatabase.tblInspectionDAO().successSendingCentral(
                                                    glbl_onSendBatchTicketNumber);
                                            rcepDatabase.tblSamplingDAO().successSendingCentral(
                                                    glbl_onSendBatchTicketNumber);
                                            rcepDatabase.tblActualDeliveryDAO()
                                                    .successSendingCentral(
                                                            glbl_onSendBatchTicketNumber);
                                            //stop dialog
                                            Fun.progressStop(mProgressDialog);

                                            File sample = new File(glbl_imgSamplingPath);
                                            File actual = new File(glbl_imgActualDeliveryPath);
                                            File acctg = new File(glbl_imgAccountingPath);


                                            if (sample.exists()) {
                                                sample.delete();
                                            }

                                            if (actual.exists()) {
                                                actual.delete();
                                            }

                                            if (acctg.exists()) {
                                                acctg.delete();
                                            }

                                            new MaterialAlertDialogBuilder(
                                                    InspectionMainActivity.this)
                                                    .setTitle("RCEF DI")
                                                    .setCancelable(false)
                                                    .setMessage("Batch delivery " +
                                                            glbl_onSendBatchTicketNumber +
                                                            " data successfully sent!")
                                                    .setPositiveButton("DONE",
                                                            new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(
                                                                        DialogInterface dialogInterface,
                                                                        int i) {
                                                                    final Handler handler =
                                                                            new Handler();
                                                                    handler.postDelayed(
                                                                            new Runnable() {
                                                                                @Override
                                                                                public void run() {
                                                                                    dialogInterface.dismiss();
                                                                                    glbl_mainViewData.clear();
                                                                                    glbl_mainViewData
                                                                                            .addAll(getDeliveryData());
                                                                                    inspectionSIViewAdapter
                                                                                            .notifyDataSetChanged();
                                                                                }
                                                                            }, 500);
                                                                }
                                                            })
                                                    .show();


                                            /*CFAlertDialog.Builder builder =
                                                    new CFAlertDialog.Builder(
                                                            InspectionMainActivity.this);
                                            builder.setDialogStyle(
                                                    CFAlertDialog.CFAlertStyle.ALERT);
                                            builder.setTitle("RCEP App");
                                            builder.setMessage("Batch delivery " +
                                                    glbl_onSendBatchTicketNumber +
                                                    " data successfully sent!");
                                            builder.addButton("DONE", Color.parseColor("#FFFFFF"),
                                                    Color.parseColor("#429ef4"),
                                                    CFAlertDialog.CFAlertActionStyle.POSITIVE,
                                                    CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                                                    new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(
                                                                final DialogInterface dialog,
                                                                int which) {
                                                            final Handler handler = new Handler();
                                                            handler.postDelayed(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    dialog.dismiss();
                                                                    glbl_mainViewData.clear();
                                                                    glbl_mainViewData
                                                                            .addAll(getDeliveryData());
                                                                    inspectionSIViewAdapter
                                                                            .notifyDataSetChanged();
                                                                }
                                                            }, 500);
                                                        }
                                                    });
                                            builder.show();*/
                                        }
                                /* //success indicator flagging
                                rcepDatabase.tblSendingStatusDAO().updateExistingLocal(1, 2, glbl_onSendBatchTicketNumber);

                                if (glbl_sendingMode == 0) {
                                    //local
                                    rcepDatabase.tblInspectionDAO().successSendingLocal(glbl_onSendBatchTicketNumber);
                                    rcepDatabase.tblSamplingDAO().successSendingLocal(glbl_onSendBatchTicketNumber);
                                    rcepDatabase.tblActualDeliveryDAO().successSendingLocal(glbl_onSendBatchTicketNumber);
                                } else {
                                    //central
                                    rcepDatabase.tblInspectionDAO().successSendingCentral(glbl_onSendBatchTicketNumber);
                                    rcepDatabase.tblSamplingDAO().successSendingCentral(glbl_onSendBatchTicketNumber);
                                    rcepDatabase.tblActualDeliveryDAO().successSendingCentral(glbl_onSendBatchTicketNumber);
                                }


                                //success dialog
                                Fun.progressStop(mProgressDialog);
                                CFAlertDialog.Builder builder = new CFAlertDialog.Builder(InspectionMainActivity.this);
                                builder.setDialogStyle(CFAlertDialog.CFAlertStyle.ALERT);
                                builder.setTitle("RCEP App");
                                builder.setMessage("Batch delivery " + glbl_onSendBatchTicketNumber + " data successfully sent!");
                                builder.addButton("YES", Color.parseColor("#FFFFFF"), Color.parseColor("#429ef4"), CFAlertDialog.CFAlertActionStyle.POSITIVE, CFAlertDialog.CFAlertActionAlignment.JUSTIFIED, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(final DialogInterface dialog, int which) {
                                        final Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                dialog.dismiss();
                                                glbl_mainViewData.clear();
                                                glbl_mainViewData.addAll(getDeliveryData());
                                                inspectionSIViewAdapter.notifyDataSetChanged();
                                            }
                                        }, 500);
                                    }
                                });
                                builder.show();*/
                                    } else {
                                        Fun.progressStop(mProgressDialog);
                                        Toast.makeText(InspectionMainActivity.this,
                                                "Fail to upload image. Please try again",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(InspectionMainActivity.this,
                                "Something went wrong. Please check network connectivity and try again",
                                Toast.LENGTH_LONG).show();
                        Fun.progressStop(mProgressDialog);
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(InspectionMainActivity.this);
        requestQueue.add(jsonObjectRequest);
    }

    private void sendInspectionDataNew(final Context ctx, final String data,
                                       TblInspection inspection) {
        Fun.progressStart(mProgressDialog, "", "Sending inspection data to server");
        String request_url;
        /*if (glbl_sendingMode == 0 || glbl_sendingMode == 3) {
            Log.e(TAG, "azzzz: ");
            request_url = Fun.localDeploymentAddress(Fun.getDynamicIP(prefUserAccount, ctx)) +
                    "/script/si_receive_inspection.php";
        } else {
            Log.e(TAG, "bzzzz: ");
            request_url =
                    Fun.getAddress(glbl_season) + "/script/si_receive_inspection_data_new.php";
        }*/
        request_url =
                Fun.getAddress(glbl_season) + "/script/si_receive_inspection_data_new.php";
        Log.e(TAG, "sendTblInspectionData: " + request_url);
        //request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, request_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e(TAG, "onResponse: " + response);

                        try {
                            // Parse the JSON string
                            JSONObject jsonObject = new JSONObject(response);
                            // Retrieve values
                            int code = jsonObject.getInt("code");
                            String message = jsonObject.getString("message");

                            switch (code) {
                                case 1:
                                    String fileLocation = inspection.getSamplingImage_path();
                                    Bitmap preview = BitmapFactory.decodeFile(fileLocation);
                                    uploadImageSampling(preview, inspection.getSamplingImage());
                                    break;
                                case 0:
                                    Toast.makeText(ctx, "Err: " + message,
                                            Toast.LENGTH_LONG).show();
                                    Fun.progressStop(mProgressDialog);
                                    glbl_mainViewData.clear();
                                    glbl_mainViewData.addAll(getDeliveryData());
                                    inspectionSIViewAdapter.notifyDataSetChanged();
                                    break;
                                default:
                                    Toast.makeText(ctx, "Unable to identify response",
                                            Toast.LENGTH_SHORT).show();
                                    Fun.progressStop(mProgressDialog);
                                    glbl_mainViewData.clear();
                                    glbl_mainViewData.addAll(getDeliveryData());
                                    inspectionSIViewAdapter.notifyDataSetChanged();
                                    break;
                            }

                        } catch (JSONException e) {
                            Toast.makeText(ctx, "Err: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                            Fun.progressStop(mProgressDialog);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "volleyError: " + error.toString());
                Log.e(TAG, "getLocalizedMessage: " + error.getLocalizedMessage());
                Log.e(TAG, "getCause: " + Objects.requireNonNull(error.getCause()).toString());
                Fun.progressStop(mProgressDialog);
                Toast.makeText(ctx,
                        "Server unreachable. Please check network connectivity and try again.",
                        Toast.LENGTH_SHORT).show();
                glbl_mainViewData.clear();
                glbl_mainViewData.addAll(getDeliveryData());
                inspectionSIViewAdapter.notifyDataSetChanged();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("data", data);
                //Log.e(TAG, "inspectionData: " + data);

                return params;
            }
        };
       /* RequestQueue requestQueue = Volley.newRequestQueue(ctx);
        requestQueue.add(stringRequest);*/

       /* stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        Volley.newRequestQueue(ctx).add(stringRequest);*/

        RequestQueue requestQueue = Volley.newRequestQueue(ctx);
        requestQueue.add(stringRequest);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                -1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    private void sendTblInspectionData(final Context ctx, final String data,
                                       final String batchTicketNumber) {
        //status flagging for local sending
        /*if (rcepDatabase.tblSendingStatusDAO().isExistingLocal(batchTicketNumber) > 0) {
            rcepDatabase.tblSendingStatusDAO().updateExistingLocal(1, 1, batchTicketNumber);
        } else {
            TblSendingStatus flag = new TblSendingStatus(batchTicketNumber, 1, 1);
            rcepDatabase.tblSendingStatusDAO().insertSendingStatus(flag);
        }*/
        Fun.progressStart(mProgressDialog, "", "Sending inspection data to server");
//        Log.e(TAG, "sendTblInspectionData: " + data);
        //url
        String request_url;
        if (glbl_sendingMode == 0 || glbl_sendingMode == 3) {
            Log.e(TAG, "azzzz: ");
            request_url = Fun.localDeploymentAddress(Fun.getDynamicIP(prefUserAccount, ctx)) +
                    "/script/si_receive_inspection.php";
        } else {
            Log.e(TAG, "bzzzz: ");
            request_url = Fun.getAddress(glbl_season) + "/script/si_receive_inspection.php";
        }
        Log.e(TAG, "sendTblInspectionData: " + request_url);

        //request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, request_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e(TAG, "onResponse: " + response);
                        switch (response.trim()) {
                            case "1":
                                //proceed to next request
                                sendLocalStatusData(InspectionMainActivity.this,
                                        Fun.jsonTblDeliveryStatusAppLocalData(
                                                rcepDatabase.tblDeliveryStatusAppLocalDAO()
                                                        .getLocalDeliveryStatusByBatch(
                                                                batchTicketNumber)),
                                        batchTicketNumber);
                                break;
                            case "0":
                                Toast.makeText(ctx,
                                        "Failed to receive inspection data. Please retry sending batch delivery data again",
                                        Toast.LENGTH_SHORT).show();
                                Fun.progressStop(mProgressDialog);
                                glbl_mainViewData.clear();
                                glbl_mainViewData.addAll(getDeliveryData());
                                inspectionSIViewAdapter.notifyDataSetChanged();
                                break;
                            case "404":
                                Toast.makeText(ctx, "Error : Unable to connect in database server",
                                        Toast.LENGTH_LONG).show();
                                Fun.progressStop(mProgressDialog);
                                glbl_mainViewData.clear();
                                glbl_mainViewData.addAll(getDeliveryData());
                                inspectionSIViewAdapter.notifyDataSetChanged();
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
                glbl_mainViewData.clear();
                glbl_mainViewData.addAll(getDeliveryData());
                inspectionSIViewAdapter.notifyDataSetChanged();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("data", data);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(ctx);
        requestQueue.add(stringRequest);
    }

    private void sendLocalStatusData(final Context ctx, final String data,
                                     final String batchTicketNumber) {
        Fun.progressMessage(mProgressDialog, "Updating batch delivery status to server");

        String request_url;
        if (glbl_sendingMode == 0 || glbl_sendingMode == 3) {
            Log.e(TAG, "a: ");
            request_url = Fun.localDeploymentAddress(Fun.getDynamicIP(prefUserAccount, ctx)) +
                    "/script/si_receive_status_app_local.php";
        } else {
            Log.e(TAG, "b: ");
            request_url = Fun.getAddress(glbl_season) + "/script/si_receive_status_app_local.php";
        }
        Log.e(TAG, "sendLocalStatusData: " + request_url);

        //request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, request_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        switch (response.trim()) {
                            case "1":
                                //success
                                //next request
                                String mjsonData = Fun.jsonTblSampling(rcepDatabase.tblSamplingDAO()
                                        .getSamplingByBatch(batchTicketNumber));
                                if (mjsonData.trim().length() > 0) {
                                    sendTblSamplingData(InspectionMainActivity.this, mjsonData,
                                            batchTicketNumber);
                                } else {
                                    rcepDatabase.tblSendingStatusDAO()
                                            .updateExistingLocal(1, 2, batchTicketNumber);
                                    Fun.progressStop(mProgressDialog);

                                    if (glbl_sendingMode == 0 || glbl_sendingMode == 3) {
                                        rcepDatabase.tblInspectionDAO()
                                                .successSendingLocal(glbl_onSendBatchTicketNumber);
                                        rcepDatabase.tblSamplingDAO()
                                                .successSendingLocal(glbl_onSendBatchTicketNumber);
                                        rcepDatabase.tblActualDeliveryDAO()
                                                .successSendingLocal(glbl_onSendBatchTicketNumber);
                                    } else {
                                        rcepDatabase.tblInspectionDAO().successSendingCentral(
                                                glbl_onSendBatchTicketNumber);
                                        rcepDatabase.tblSamplingDAO().successSendingCentral(
                                                glbl_onSendBatchTicketNumber);
                                        rcepDatabase.tblActualDeliveryDAO().successSendingCentral(
                                                glbl_onSendBatchTicketNumber);
                                    }

                                    new MaterialAlertDialogBuilder(InspectionMainActivity.this)
                                            .setTitle("RCEF DI")
                                            .setCancelable(false)
                                            .setMessage("Batch delivery " + batchTicketNumber +
                                                    " data successfully sent!")
                                            .setPositiveButton("DONE",
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
                                                                    glbl_mainViewData.clear();
                                                                    glbl_mainViewData
                                                                            .addAll(getDeliveryData());
                                                                    inspectionSIViewAdapter
                                                                            .notifyDataSetChanged();

                                                                }
                                                            }, 500);
                                                        }
                                                    })
                                            .show();

                                    /*CFAlertDialog.Builder builder =
                                            new CFAlertDialog.Builder(InspectionMainActivity.this);
                                    builder.setDialogStyle(CFAlertDialog.CFAlertStyle.ALERT);
                                    builder.setTitle("RCEP App");
                                    builder.setMessage("Batch delivery " + batchTicketNumber +
                                            " data successfully sent!");
                                    builder.addButton("DONE", Color.parseColor("#FFFFFF"),
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
                                                            glbl_mainViewData.clear();
                                                            glbl_mainViewData
                                                                    .addAll(getDeliveryData());
                                                            inspectionSIViewAdapter
                                                                    .notifyDataSetChanged();

                                                        }
                                                    }, 500);
                                                }
                                            });
                                    builder.show();*/
                                }
                                break;
                            case "0":
                                Toast.makeText(ctx,
                                        "Failed to receive delivery. Please retry sending batch delivery again",
                                        Toast.LENGTH_SHORT).show();
                                Fun.progressStop(mProgressDialog);
                                glbl_mainViewData.clear();
                                glbl_mainViewData.addAll(getDeliveryData());
                                inspectionSIViewAdapter.notifyDataSetChanged();
                                break;
                            case "404":
                                Toast.makeText(ctx, "Error : Unable to connect in database server",
                                        Toast.LENGTH_LONG).show();
                                Fun.progressStop(mProgressDialog);
                                glbl_mainViewData.clear();
                                glbl_mainViewData.addAll(getDeliveryData());
                                inspectionSIViewAdapter.notifyDataSetChanged();
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
                glbl_mainViewData.clear();
                glbl_mainViewData.addAll(getDeliveryData());
                inspectionSIViewAdapter.notifyDataSetChanged();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("data", data);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(ctx);
        requestQueue.add(stringRequest);
    }

    private void sendTblSamplingData(final Context ctx, final String data,
                                     final String batchTicketNumber) {

        //        Fun.progressStart(mProgressDialog, "", " Updating batch delivery sampling data to server");
        Fun.progressMessage(mProgressDialog, "Updating batch delivery sampling data to server");

        String request_url;
        if (glbl_sendingMode == 0 || glbl_sendingMode == 3) {
            Log.e(TAG, "a: ");
            request_url = Fun.localDeploymentAddress(Fun.getDynamicIP(prefUserAccount, ctx)) +
                    "/script/si_receive_sampling.php";
        } else {
            Log.e(TAG, "b: ");
            request_url = Fun.getAddress(glbl_season) + "/script/si_receive_sampling.php";
        }
        Log.e(TAG, "sendTblSamplingData: " + request_url);

        //request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, request_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        switch (response.trim()) {
                            case "1":
                                //success
                                String mjsonData = Fun.jsonTblActualDelivery(
                                        rcepDatabase.tblActualDeliveryDAO()
                                                .getActualDeliveryByBatch(batchTicketNumber));
                                if (mjsonData.trim().length() > 0) {
                                    sendActualDeliveryData(InspectionMainActivity.this, mjsonData,
                                            batchTicketNumber);
                                } else {
                                    rcepDatabase.tblSendingStatusDAO()
                                            .updateExistingLocal(1, 2, batchTicketNumber);

                                    if (glbl_sendingMode == 0 || glbl_sendingMode == 3) {
                                        rcepDatabase.tblInspectionDAO()
                                                .successSendingLocal(glbl_onSendBatchTicketNumber);
                                        rcepDatabase.tblSamplingDAO()
                                                .successSendingLocal(glbl_onSendBatchTicketNumber);
                                        rcepDatabase.tblActualDeliveryDAO()
                                                .successSendingLocal(glbl_onSendBatchTicketNumber);
                                    } else {
                                        rcepDatabase.tblInspectionDAO().successSendingCentral(
                                                glbl_onSendBatchTicketNumber);
                                        rcepDatabase.tblSamplingDAO().successSendingCentral(
                                                glbl_onSendBatchTicketNumber);
                                        rcepDatabase.tblActualDeliveryDAO().successSendingCentral(
                                                glbl_onSendBatchTicketNumber);
                                    }

                                    Fun.progressStop(mProgressDialog);

                                    new MaterialAlertDialogBuilder(InspectionMainActivity.this)
                                            .setTitle("RCEF DI")
                                            .setCancelable(false)
                                            .setMessage("Batch delivery " + batchTicketNumber +
                                                    " data successfully sent!")
                                            .setPositiveButton("DONE",
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
                                                                    glbl_mainViewData.clear();
                                                                    glbl_mainViewData
                                                                            .addAll(getDeliveryData());
                                                                    inspectionSIViewAdapter
                                                                            .notifyDataSetChanged();

                                                                }
                                                            }, 500);
                                                        }
                                                    })
                                            .show();

                                    /*CFAlertDialog.Builder builder =
                                            new CFAlertDialog.Builder(InspectionMainActivity.this);
                                    builder.setDialogStyle(CFAlertDialog.CFAlertStyle.ALERT);
                                    builder.setTitle("RCEP App");
                                    builder.setMessage("Batch delivery " + batchTicketNumber +
                                            " data successfully sent!");
                                    builder.addButton("DONE", Color.parseColor("#FFFFFF"),
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
                                                            glbl_mainViewData.clear();
                                                            glbl_mainViewData
                                                                    .addAll(getDeliveryData());
                                                            inspectionSIViewAdapter
                                                                    .notifyDataSetChanged();

                                                        }
                                                    }, 500);
                                                }
                                            });
                                    builder.show();*/
                                }
                                break;
                            case "0":
                                Toast.makeText(ctx,
                                        "Failed to receive delivery. Please retry sending batch delivery again",
                                        Toast.LENGTH_SHORT).show();
                                Fun.progressStop(mProgressDialog);
                                glbl_mainViewData.clear();
                                glbl_mainViewData.addAll(getDeliveryData());
                                inspectionSIViewAdapter.notifyDataSetChanged();
                                break;
                            case "404":
                                Toast.makeText(ctx, "Error : Unable to connect in database server",
                                        Toast.LENGTH_LONG).show();
                                Fun.progressStop(mProgressDialog);
                                glbl_mainViewData.clear();
                                glbl_mainViewData.addAll(getDeliveryData());
                                inspectionSIViewAdapter.notifyDataSetChanged();
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
                glbl_mainViewData.clear();
                glbl_mainViewData.addAll(getDeliveryData());
                inspectionSIViewAdapter.notifyDataSetChanged();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("data", data);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(ctx);
        requestQueue.add(stringRequest);
    }

    private void sendActualDeliveryData(final Context ctx, final String data,
                                        final String batchTicketNumber) {
        //Log.e(TAG, "sendActualDeliveryData: " + data);
        Fun.progressMessage(mProgressDialog, "Updating batch delivery stocks to server");
        //url
        String request_url;

        if (glbl_sendingMode == 0 || glbl_sendingMode == 3) {
            Log.e(TAG, "a: ");
            request_url = Fun.localDeploymentAddress(Fun.getDynamicIP(prefUserAccount, ctx)) +
                    "/script/si_receive_actual_delivery.php";
        } else {
            Log.e(TAG, "b: ");
            request_url = Fun.getAddress(glbl_season) + "/script/si_receive_actual_delivery.php";
        }
        Log.e(TAG, "sendActualDeliveryData: " + request_url);

        //request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, request_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e(TAG, "onResponse: " + response);
                        switch (response.trim()) {
                            case "1":
                                if (glbl_sendingMode == 3) {
                                    //local
                                    String prv_dropoff_id = "";
                                    String coop_accreditation = "";
                                    String region = "";
                                    String province = "";
                                    String municipality = "";
                                    String dropoffpoint = "";
                                    String prv = "";
                                    //assign values
                                    for (TblDeliveryInspection data : rcepDatabase
                                            .tblDeliveryInspectionDAO()
                                            .getSingleDelivery(glbl_onSendBatchTicketNumber)) {
                                        prv_dropoff_id = data.getPrv_dropoff_id();
                                        coop_accreditation = data.getCoopAccreditation();
                                        region = data.getRegion();
                                        province = data.getProvince();
                                        municipality = data.getMunicipality();
                                        dropoffpoint = data.getDropOffPoint();
                                        prv = data.getPrv();
                                    }
                                    //proceed on updating local server dropoffpoint
                                    updateLocalServerDropoffPoint(InspectionMainActivity.this,
                                            prv_dropoff_id, coop_accreditation, region, province,
                                            municipality, dropoffpoint, prv);
                                } else {
                                    //upload sampling image
                                    /*String fileLocation =
                                            Environment.getExternalStorageDirectory() + "/" +
                                                    Fun.getDir() + File.separator +
                                                    glbl_imgSampling;*/

                                    if (!Objects.equals(
                                            prefUserAccount.getString(Fun.uaStationName(), ""),
                                            "PhilRice Central Experiment Station") ||
                                            !Objects.equals(
                                                    prefUserAccount.getString(Fun.uaStationName(),
                                                            ""), "")) {
                                        updateStationDB(ctx, glbl_domain, glbl_port,
                                                "rcep_delivery_inspection");
                                    }


                                    String fileLocation = glbl_imgSamplingPath;
                                    Bitmap preview = BitmapFactory.decodeFile(fileLocation);
                                    uploadImageSampling(preview, glbl_imgSampling);
                                }


                                break;
                            case "0":
                                Toast.makeText(ctx,
                                        "Failed to receive delivery. Please retry sending batch delivery again",
                                        Toast.LENGTH_SHORT).show();
                                Fun.progressStop(mProgressDialog);
                                glbl_mainViewData.clear();
                                glbl_mainViewData.addAll(getDeliveryData());
                                inspectionSIViewAdapter.notifyDataSetChanged();
                                break;
                            case "404":
                                Toast.makeText(ctx, "Error : Unable to connect in database server",
                                        Toast.LENGTH_LONG).show();
                                Fun.progressStop(mProgressDialog);
                                glbl_mainViewData.clear();
                                glbl_mainViewData.addAll(getDeliveryData());
                                inspectionSIViewAdapter.notifyDataSetChanged();
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
                glbl_mainViewData.clear();
                glbl_mainViewData.addAll(getDeliveryData());
                inspectionSIViewAdapter.notifyDataSetChanged();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("data", data);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(ctx);
        requestQueue.add(stringRequest);
    }

    private void updateLocalServerDropoffPoint(final Context ctx, final String prvDropoffId,
                                               final String coopAccreditation, final String region,
                                               final String province, final String municipality,
                                               final String dropoffpoint, final String prv) {

        Fun.progressMessage(mProgressDialog, "updating local server dropoffpoint");
        //url
        String request_url = Fun.localDeploymentAddress(Fun.getDynamicIP(prefUserAccount, ctx)) +
                "/script/si_update_local_dropoffpoint_request.php";
        //request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, request_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e(TAG, "onResponse: " + response);
                        response = response.toLowerCase().trim();

                        if (response.equals("success") || response.equals("existing")) {
                            //local
                            //flag status of sending local
                            rcepDatabase.tblInspectionDAO()
                                    .successSendingLocal(glbl_onSendBatchTicketNumber);
                            rcepDatabase.tblSamplingDAO()
                                    .successSendingLocal(glbl_onSendBatchTicketNumber);
                            rcepDatabase.tblActualDeliveryDAO()
                                    .successSendingLocal(glbl_onSendBatchTicketNumber);
                            //stop dialog
                            Fun.progressStop(mProgressDialog);
                            new MaterialAlertDialogBuilder(InspectionMainActivity.this)
                                    .setTitle("RCEF DI")
                                    .setCancelable(false)
                                    .setMessage("Batch delivery " + glbl_onSendBatchTicketNumber +
                                            " data successfully sent!")
                                    .setPositiveButton("DONE",
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface,
                                                                    int i) {
                                                    final Handler handler = new Handler();
                                                    handler.postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            dialogInterface.dismiss();
                                                            glbl_mainViewData.clear();
                                                            glbl_mainViewData.addAll(
                                                                    getDeliveryData());
                                                            inspectionSIViewAdapter.notifyDataSetChanged();
                                                        }
                                                    }, 500);
                                                }
                                            })
                                    .show();

                            /*CFAlertDialog.Builder builder =
                                    new CFAlertDialog.Builder(InspectionMainActivity.this);
                            builder.setDialogStyle(CFAlertDialog.CFAlertStyle.ALERT);
                            builder.setTitle("RCEP App");
                            builder.setMessage("Batch delivery " + glbl_onSendBatchTicketNumber +
                                    " data successfully sent!");
                            builder.addButton("DONE", Color.parseColor("#FFFFFF"),
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
                                                    glbl_mainViewData.clear();
                                                    glbl_mainViewData.addAll(getDeliveryData());
                                                    inspectionSIViewAdapter.notifyDataSetChanged();
                                                }
                                            }, 500);
                                        }
                                    });
                            builder.show();*/
                        } else {
                            Fun.progressStop(mProgressDialog);
                            Toast.makeText(ctx,
                                    "Unable to write local dropoffpoint error[" + response + "]",
                                    Toast.LENGTH_SHORT).show();
                            glbl_mainViewData.clear();
                            glbl_mainViewData.addAll(getDeliveryData());
                            inspectionSIViewAdapter.notifyDataSetChanged();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Fun.progressStop(mProgressDialog);
                Toast.makeText(ctx,
                        "Server unreachable. Please check network connectivity and try again.",
                        Toast.LENGTH_SHORT).show();
                glbl_mainViewData.clear();
                glbl_mainViewData.addAll(getDeliveryData());
                inspectionSIViewAdapter.notifyDataSetChanged();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                /*prvDropoffId, coopAccreditation, region, province, municipality, dropoffpoint, prv*/
                Map<String, String> params = new HashMap<>();
                params.put("prvDropoffId", prvDropoffId);
                params.put("coopAccreditation", coopAccreditation);
                params.put("region", region);
                params.put("province", province);
                params.put("municipality", municipality);
                params.put("dropoffpoint", dropoffpoint);
                params.put("prv", prv);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(ctx);
        requestQueue.add(stringRequest);
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

    @Override
    public void onBackPressed() {
        new MaterialAlertDialogBuilder(InspectionMainActivity.this)
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

                                Intent intent = new Intent(InspectionMainActivity.this,
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

       /* CFAlertDialog.Builder builder = new CFAlertDialog.Builder(InspectionMainActivity.this);
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

                                Intent intent = new Intent(InspectionMainActivity.this,
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

    @Override
    public void onPassed(final String batchTicketNumber) {

        PermissionX.init(this)
                .permissions(permissions)
                .onExplainRequestReason((scope, deniedList) ->
                        scope.showRequestReasonDialog(deniedList,
                                "Delivery & Inspection app requires all permission to work properly",
                                "Ok",
                                "Cancel"))
                .onForwardToSettings((scope, deniedList) ->
                        scope.showForwardToSettingsDialog(deniedList,
                                "Delivery & Inspection app requires all permission to work properly",
                                "Go to Settings",
                                "Cancel"))
                .request((allGranted, grantedList, deniedList) -> {
                    if (allGranted) {
                        //passed in initial screening
                        //clear temp data
                        prefTempInspectionData.edit().clear().apply();
                        rcepDatabase.tblTempSamplingDAO().nukeTable();
                        //reset flagging of seedtag within selected batch
                        rcepDatabase.tblDeliveryInspectionDAO().resetSeedTagFlag(batchTicketNumber);

                        final String mPrvDropoffId =
                                rcepDatabase.tblDeliveryInspectionDAO()
                                        .getPrvDropoffId(batchTicketNumber);
                        final String mPrv =
                                rcepDatabase.tblDeliveryInspectionDAO().getPrv(batchTicketNumber);
                        final String mMoaNumber =
                                rcepDatabase.tblDeliveryInspectionDAO()
                                        .getMoaNumber(batchTicketNumber);
                        final String mBatchSeries =
                                rcepDatabase.tblDeliveryInspectionDAO()
                                        .getBatchSeries(batchTicketNumber);
                        final int mIsBuffer =
                                rcepDatabase.tblDeliveryInspectionDAO()
                                        .getIsBuffer(batchTicketNumber);
                        final String mSeedDistributionMode =
                                rcepDatabase.tblDeliveryInspectionDAO()
                                        .getSeedDistributionMode(batchTicketNumber);


                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                SharedPreferences.Editor editor = prefTempInspectionData.edit();
                                //update preference values
                                editor.putString(Fun.tiBatchTicketNumber(), batchTicketNumber);
                                editor.putInt(Fun.tiScreeningPassed(), 1);
                                editor.putString(Fun.tiScreeningRemarks(), "");
                                //visual inspetion parameters were updated due to hiding of visual inspection
                                editor.putInt(Fun.tiVisualPassed(), 1);
                                editor.putString(Fun.tiVisualFindings(), "");
                                editor.putString(Fun.tiVisualRemarks(), "");
                                editor.putString(Fun.tiVisualInspectionImage(), "");
                                editor.putInt(Fun.tiSamplingPassed(), 0);
                                editor.putString(Fun.tiSamplingImage(), "");
                                editor.putString(Fun.tiBatchDeliveryImage(), "");
                                editor.putString(Fun.tiDateInspected(), Fun.getCurrentDate());
                                editor.putString(Fun.tiDateCreated(), "");
                                editor.putInt(Fun.tiSend(), 1);
                                editor.putInt(Fun.tiTempDeliveryStatus(), 1);
                                editor.putString(Fun.tiPrvDropoffId(), mPrvDropoffId);
                                editor.putString(Fun.tiPrv(), mPrv);
                                editor.putString(Fun.tiMoaNumber(), mMoaNumber);
                                editor.putString(Fun.tiBatchSeries(), mBatchSeries);
                                editor.putString(Fun.tiIsBuffer(), String.valueOf(mIsBuffer));
                                if (mSeedDistributionMode.equalsIgnoreCase("Binhi e-Padala")) {
                                    editor.putInt(Fun.tiForEbinhi(), 1);
                                } else {
                                    editor.putInt(Fun.tiForEbinhi(), 0);
                                }

                                //apply values
                                editor.apply();
                                //go to next activity
                                Intent intent = new Intent(InspectionMainActivity.this,
                                        InspectionNewSamplingActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }
                        }, 1500);
                    }
                });


        //on passed function start
      /*  CFAlertDialog.Builder builder = new CFAlertDialog.Builder(InspectionMainActivity.this);
        builder.setDialogStyle(CFAlertDialog.CFAlertStyle.ALERT);
        builder.setTitle(batchTicketNumber);
        builder.setMessage("Select inspection category");
        builder.addButton("REGULAR INSPECTION", Color.parseColor("#FFFFFF"),
                Color.parseColor("#429ef4"),
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
                                SharedPreferences.Editor editor = prefTempInspectionData.edit();
                                //update preference values
                                editor.putString(Fun.tiBatchTicketNumber(), batchTicketNumber);
                                editor.putInt(Fun.tiScreeningPassed(), 1);
                                editor.putString(Fun.tiScreeningRemarks(), "");
                                //visual inspetion parameters were updated due to hiding of visual inspection
                                editor.putInt(Fun.tiVisualPassed(), 1);
                                editor.putString(Fun.tiVisualFindings(), "");
                                editor.putString(Fun.tiVisualRemarks(), "");
                                editor.putString(Fun.tiVisualInspectionImage(), "");
                                editor.putInt(Fun.tiSamplingPassed(), 0);
                                editor.putString(Fun.tiSamplingImage(), "");
                                editor.putString(Fun.tiBatchDeliveryImage(), "");
                                editor.putString(Fun.tiDateInspected(), Fun.getCurrentDate());
                                editor.putString(Fun.tiDateCreated(), "");
                                editor.putInt(Fun.tiSend(), 1);
                                editor.putInt(Fun.tiTempDeliveryStatus(), 1);
                                editor.putString(Fun.tiPrvDropoffId(), mPrvDropoffId);
                                editor.putString(Fun.tiPrv(), mPrv);
                                editor.putString(Fun.tiMoaNumber(), mMoaNumber);
                                editor.putString(Fun.tiBatchSeries(), mBatchSeries);
                                editor.putString(Fun.tiIsBuffer(), String.valueOf(mIsBuffer));
                                editor.putInt(Fun.tiForEbinhi(), 0);
                                //apply values
                                editor.apply();
                                //go to next activity
                                Intent intent = new Intent(InspectionMainActivity.this,
                                        InspectionNewSamplingActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }
                        }, 1500);
                    }
                });

        builder.addButton("e-BINHI INSPECTION", Color.parseColor("#FFFFFF"),
                Color.parseColor("#429ef4"),
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
                                SharedPreferences.Editor editor = prefTempInspectionData.edit();
                                //update preference values
                                editor.putString(Fun.tiBatchTicketNumber(), batchTicketNumber);
                                editor.putInt(Fun.tiScreeningPassed(), 1);
                                editor.putString(Fun.tiScreeningRemarks(), "");
                                //visual inspetion parameters were updated due to hiding of visual inspection
                                editor.putInt(Fun.tiVisualPassed(), 1);
                                editor.putString(Fun.tiVisualFindings(), "");
                                editor.putString(Fun.tiVisualRemarks(), "");
                                editor.putString(Fun.tiVisualInspectionImage(), "");
                                editor.putInt(Fun.tiSamplingPassed(), 0);
                                editor.putString(Fun.tiSamplingImage(), "");
                                editor.putString(Fun.tiBatchDeliveryImage(), "");
                                editor.putString(Fun.tiDateInspected(), Fun.getCurrentDate());
                                editor.putString(Fun.tiDateCreated(), "");
                                editor.putInt(Fun.tiSend(), 1);
                                editor.putInt(Fun.tiTempDeliveryStatus(), 1);
                                editor.putString(Fun.tiPrvDropoffId(), mPrvDropoffId);
                                editor.putString(Fun.tiPrv(), mPrv);
                                editor.putString(Fun.tiMoaNumber(), mMoaNumber);
                                editor.putString(Fun.tiBatchSeries(), mBatchSeries);
                                editor.putString(Fun.tiIsBuffer(), String.valueOf(mIsBuffer));
                                editor.putInt(Fun.tiForEbinhi(), 1);
                                //apply values
                                editor.apply();
                                //go to next activity
                                Intent intent = new Intent(InspectionMainActivity.this,
                                        InspectionNewSamplingActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }
                        }, 1500);
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
    public void onSendLocal(final String batchTicketNumber) {

        /*String prv_dropoff_id = "";
        String coop_accreditation = "";
        String region = "";
        String province = "";
        String municipality = "";
        String dropoffpoint = "";
        String prv = "";

        for (TblDeliveryInspection data : rcepDatabase.tblDeliveryInspectionDAO().getSingleDelivery(batchTicketNumber)) {
            prv_dropoff_id = data.getPrv_dropoff_id();
            coop_accreditation = data.getCoopAccreditation();
            region = data.getRegion();
            province = data.getProvince();
            municipality = data.getMunicipality();
            dropoffpoint = data.getDropOffPoint();
            prv = data.getPrv();
        }

        updateLocalServerDropoffPoint(this, prv_dropoff_id, coop_accreditation, region, province, municipality, dropoffpoint, prv);

        Log.e(TAG, "prv_dropoff_id->" + prv_dropoff_id +
                "\ncoop_accreditation->" + coop_accreditation +
                "\nregion->" + region +
                "\nprovince->" + province +
                "\nmunicipality->" + municipality +
                "\ndropoffpoint->" + dropoffpoint +
                "\nprv->" + prv);*/


        glbl_imgSampling = "";
        glbl_imgSamplingPath = "";

        glbl_imgActualDelivery = "";
        glbl_imgActualDeliveryPath = "";

        glbl_imgAccounting = "";
        glbl_imgAccountingPath = "";

        glbl_onSendBatchTicketNumber = batchTicketNumber;
        glbl_sendingMode = 0;//inspected and send to local
        //assigning images
        for (TblInspection i : rcepDatabase.tblInspectionDAO()
                .getInspectionByBatch(batchTicketNumber)) {
            glbl_imgSampling = i.getSamplingImage();
            glbl_imgSamplingPath = i.getSamplingImage_path();
            glbl_imgActualDelivery = i.getBatchDeliveryImage();
            glbl_imgActualDeliveryPath = i.getBatchDeliveryImage_path();
            glbl_imgAccounting = i.getAccountingImage();
            glbl_imgAccountingPath = i.getAccountingImage_path();
        }
        // Log.e(TAG, "onSendCentral: " + glbl_onSendBatchTicketNumber + ":" + glbl_sendingMode + ":" + glbl_imgSampling + ":" + glbl_imgActualDelivery);
        // Confirmation Dialog

        new MaterialAlertDialogBuilder(InspectionMainActivity.this)
                .setTitle("RCEF DI")
                .setCancelable(false)
                .setMessage("Send batch delivery " + batchTicketNumber + " to local server?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //send data
                                //updating flag send local attempt
                                rcepDatabase.tblDeliveryStatusAppLocalDAO()
                                        .setAttemptSendLocal1(batchTicketNumber);
                                sendTblInspectionData(InspectionMainActivity.this,
                                        Fun.jsonTblInspection(rcepDatabase.tblInspectionDAO()
                                                .getInspectionByBatch(batchTicketNumber)),
                                        batchTicketNumber);
                            }
                        }, 250);
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .show();


        /*CFAlertDialog.Builder builder = new CFAlertDialog.Builder(InspectionMainActivity.this);
        builder.setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET);
        builder.setTitle("RCEP App");
        builder.setMessage("Send batch delivery " + batchTicketNumber + " to local server?");
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
                                //send data
                                //updating flag send local attempt
                                rcepDatabase.tblDeliveryStatusAppLocalDAO()
                                        .setAttemptSendLocal1(batchTicketNumber);
                                sendTblInspectionData(InspectionMainActivity.this,
                                        Fun.jsonTblInspection(rcepDatabase.tblInspectionDAO()
                                                .getInspectionByBatch(batchTicketNumber)),
                                        batchTicketNumber);
                            }
                        }, 250);
                    }
                });
        builder.addButton("CANCEL", Color.parseColor("#FFFFFF"), Color.parseColor("#429ef4"),
                CFAlertDialog.CFAlertActionStyle.NEGATIVE,
                CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.show();*/
    }

    @Override
    public void onSendCentral(final String batchTicketNumber) {
        glbl_imgSampling = "";
        glbl_imgSamplingPath = "";
        glbl_imgActualDelivery = "";
        glbl_imgActualDeliveryPath = "";
        glbl_imgAccounting = "";
        glbl_imgAccountingPath = "";


        glbl_onSendBatchTicketNumber = batchTicketNumber;
        glbl_sendingMode = 1; //direct send to central database
        //assigning images
        for (TblInspection i : rcepDatabase.tblInspectionDAO()
                .getInspectionByBatch(batchTicketNumber)) {
            glbl_imgSampling = i.getSamplingImage();
            glbl_imgSamplingPath = i.getSamplingImage_path();
            glbl_imgActualDelivery = i.getBatchDeliveryImage();
            glbl_imgActualDeliveryPath = i.getBatchDeliveryImage_path();
            glbl_imgAccounting = i.getAccountingImage();
            glbl_imgAccountingPath = i.getAccountingImage_path();
        }
        Log.e(TAG, "onSendCentral: " + glbl_onSendBatchTicketNumber + ":" + glbl_sendingMode + ":" +
                glbl_imgSampling + ":" + glbl_imgActualDelivery);
        // Confirmation Dialog
        new MaterialAlertDialogBuilder(InspectionMainActivity.this)
                .setTitle("RCEF DI")
                .setCancelable(false)
                .setMessage("Send batch delivery " + batchTicketNumber + " to central server?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                TblInspection dataInspection = rcepDatabase.tblInspectionDAO()
                                        .getInspectionByBatch2(batchTicketNumber);
                                String data =
                                        Fun.prepareInspectionData(batchTicketNumber, rcepDatabase);

                                //Fun.copyToClipboard(InspectionMainActivity.this, data);

                                sendInspectionDataNew(InspectionMainActivity.this, data,
                                        dataInspection);
                            }
                        }, 250);
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .show();

        /*CFAlertDialog.Builder builder = new CFAlertDialog.Builder(InspectionMainActivity.this);
        builder.setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET);
        builder.setTitle("RCEP App");
        builder.setMessage("Send batch delivery " + batchTicketNumber + " to central server?");
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
                                //send data
                                *//*Fun.copyToClipboard(InspectionMainActivity.this,
                                        Fun.prepareInspectionData(batchTicketNumber, rcepDatabase));
                                Log.e(TAG, "run: " +
                                        Fun.prepareInspectionData(batchTicketNumber, rcepDatabase));*//*

                                TblInspection dataInspection = rcepDatabase.tblInspectionDAO()
                                        .getInspectionByBatch2(batchTicketNumber);
                                String data =
                                        Fun.prepareInspectionData(batchTicketNumber, rcepDatabase);

                                //Fun.copyToClipboard(InspectionMainActivity.this,data);

                                sendInspectionDataNew(InspectionMainActivity.this, data,
                                        dataInspection);

                               *//* rcepDatabase.tblDeliveryStatusAppLocalDAO()
                                        .setAttemptSendCentral1(batchTicketNumber);
                                Log.e(TAG, "run: sending central");
                                sendTblInspectionData(InspectionMainActivity.this,
                                        Fun.jsonTblInspection(rcepDatabase.tblInspectionDAO()
                                                .getInspectionByBatch(batchTicketNumber)),
                                        batchTicketNumber);*//*
                            }
                        }, 250);
                    }
                });
        builder.addButton("CANCEL", Color.parseColor("#FFFFFF"), Color.parseColor("#429ef4"),
                CFAlertDialog.CFAlertActionStyle.NEGATIVE,
                CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.show();*/
    }

    @Override
    public void onDownloadData(final String batchTicketNumber) {
        Log.e(TAG, "onDownloadData: " + batchTicketNumber);

        new MaterialAlertDialogBuilder(InspectionMainActivity.this)
                .setTitle("RCEF DI")
                .setCancelable(false)
                .setMessage("")
                .setPositiveButton("DOWNLOAD", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                        /*        downloadInspectionData(InspectionMainActivity.this,
                                        batchTicketNumber);*/
                            }
                        }, 250);
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .show();


        /*CFAlertDialog.Builder builder = new CFAlertDialog.Builder(InspectionMainActivity.this);
        builder.setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET);
        builder.setTitle("RCEP App");
        builder.setCancelable(false);
        builder.setMessage("Download batch " + batchTicketNumber + " data?");
        builder.addButton("DOWNLOAD", Color.parseColor("#FFFFFF"), Color.parseColor("#429ef4"),
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
                        *//*        downloadInspectionData(InspectionMainActivity.this,
                                        batchTicketNumber);*//*
                            }
                        }, 250);
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
    public void onReset(String batchTicketNumber, int mHasLocalStatus, int mAppLocalStatus,
                        int mstatus) {
        if (mHasLocalStatus > 0) {
            //reset
            new MaterialAlertDialogBuilder(InspectionMainActivity.this)
                    .setTitle("RCEF DI")
                    .setCancelable(false)
                    .setMessage("Reset inspection data for batch " + batchTicketNumber + "?")
                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            rcepDatabase.tblActualDeliveryDAO().reset(batchTicketNumber);
                            rcepDatabase.tblDeliveryStatusDAO().reset(batchTicketNumber);
                            rcepDatabase.tblDeliveryStatusAppLocalDAO().reset(batchTicketNumber);
                            rcepDatabase.tblInspectionDAO().reset(batchTicketNumber);
                            rcepDatabase.tblSamplingDAO().reset(batchTicketNumber);

                            glbl_mainViewData.clear();
                            glbl_mainViewData.addAll(getDeliveryData());
                            inspectionSIViewAdapter.notifyDataSetChanged();
                            Toast.makeText(InspectionMainActivity.this,
                                            "Inspection for batch " + batchTicketNumber + " cleared!",
                                            Toast.LENGTH_SHORT)
                                    .show();

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

            /*CFAlertDialog.Builder builder =
                    new CFAlertDialog.Builder(InspectionMainActivity.this);
            builder.setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET);
            builder.setCancelable(false);
            builder.setMessage("Reset inspection data for batch " + batchTicketNumber + "?");
            builder.addButton("YES", Color.parseColor("#FFFFFF"), Color.parseColor("#429ef4"),
                    CFAlertDialog.CFAlertActionStyle.POSITIVE,
                    CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, int which) {

                            rcepDatabase.tblActualDeliveryDAO().reset(batchTicketNumber);
                            rcepDatabase.tblDeliveryStatusDAO().reset(batchTicketNumber);
                            rcepDatabase.tblDeliveryStatusAppLocalDAO().reset(batchTicketNumber);
                            rcepDatabase.tblInspectionDAO().reset(batchTicketNumber);
                            rcepDatabase.tblSamplingDAO().reset(batchTicketNumber);

                            dialog.dismiss();
                            glbl_mainViewData.clear();
                            glbl_mainViewData.addAll(getDeliveryData());
                            inspectionSIViewAdapter.notifyDataSetChanged();
                            Toast.makeText(InspectionMainActivity.this,
                                            "Inspection for batch " + batchTicketNumber + " cleared!",
                                            Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });
            builder.addButton("CANCEL", Color.parseColor("#FFFFFF"),
                    Color.parseColor("#429ef4"),
                    CFAlertDialog.CFAlertActionStyle.NEGATIVE,
                    CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            builder.show();*/
        } else {
            if (mstatus == 1) {
                Toast.makeText(this, "Already Inspected",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Not yet inspected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onSendLocal2(final String batchTicketNumber) {
        Log.e(TAG, "onSendLocal2: " + batchTicketNumber);

        glbl_onSendBatchTicketNumber = batchTicketNumber;
        glbl_sendingMode = 3; //downloaded and send to local case
        // Confirmation Dialog
        new MaterialAlertDialogBuilder(InspectionMainActivity.this)
                .setTitle("RCEF DI")
                .setCancelable(false)
                .setMessage("Send batch delivery " + batchTicketNumber + " to local server?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //send data
                                //updating flag send local attempt
                                rcepDatabase.tblDeliveryStatusAppLocalDAO()
                                        .setAttemptSendLocal1(batchTicketNumber);
                                sendTblInspectionData(InspectionMainActivity.this,
                                        Fun.jsonTblInspection(rcepDatabase.tblInspectionDAO()
                                                .getInspectionByBatch(batchTicketNumber)),
                                        batchTicketNumber);
                            }
                        }, 250);
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .show();

        /*CFAlertDialog.Builder builder = new CFAlertDialog.Builder(InspectionMainActivity.this);
        builder.setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET);
        builder.setTitle("RCEP App");
        builder.setMessage("Send batch delivery " + batchTicketNumber + " to local server?");
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
                                //send data
                                //updating flag send local attempt
                                rcepDatabase.tblDeliveryStatusAppLocalDAO()
                                        .setAttemptSendLocal1(batchTicketNumber);
                                sendTblInspectionData(InspectionMainActivity.this,
                                        Fun.jsonTblInspection(rcepDatabase.tblInspectionDAO()
                                                .getInspectionByBatch(batchTicketNumber)),
                                        batchTicketNumber);
                            }
                        }, 250);
                    }
                });
        builder.addButton("CANCEL", Color.parseColor("#FFFFFF"), Color.parseColor("#429ef4"),
                CFAlertDialog.CFAlertActionStyle.NEGATIVE,
                CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.show();*/

    }

    private void requestDeliveryList(final Context ctx, final String userId, final String year) {
        //Fun.progressStart(mProgressDialog, "", "Requesting delivery data to server");
        //url
        String request_url = Fun.getAddress(glbl_season) + "/script/si_delivery_list_request.php";
        Log.e(TAG, "requestDeliveryList: " + request_url);
        //request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, request_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e(TAG, "onResponse: " + response);

                        Thread thread = new Thread() {
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    public void run() {

                                        try {
                                            JSONObject jsonObject = new JSONObject(response);
                                            String status = jsonObject.getString("status");
                                            String message = jsonObject.getString("message");
                                            switch (status) {
                                                case "1":
                                                    rcepDatabase.tblDeliveryInspectionDAO()
                                                            .nukeTable();
                                                    //do other functions here
                                                    //parsing json
                                                    JSONObject json = new JSONObject(message);
                                                    //name of array is result
                                                    JSONArray jArray = json.getJSONArray("result");
                                                    JSONObject jData = null;
                                                    for (int i = 0; i < jArray.length(); i++) {
                                                        jData = jArray.getJSONObject(i);
                                                        //decode html_encode value from server
                                                        int mDeliveryId =
                                                                jData.getInt("deliveryId");
                                                        String mTicketNumber =
                                                                Html.fromHtml(jData.getString(
                                                                                "ticketNumber"))
                                                                        .toString();
                                                        String mBatchTicketNumber =
                                                                Html.fromHtml(jData.getString(
                                                                                "batchTicketNumber"))
                                                                        .toString();
                                                        String mCoopAccreditation =
                                                                Html.fromHtml(jData.getString(
                                                                                "coopAccreditation"))
                                                                        .toString();
                                                        String mSgAccreditation =
                                                                Html.fromHtml(jData.getString(
                                                                                "sgAccreditation"))
                                                                        .toString();
                                                        String mSeedTag = Html.fromHtml(
                                                                        jData.getString("seedTag"))
                                                                .toString();
                                                        String mSeedVariety =
                                                                Html.fromHtml(jData.getString(
                                                                                "seedVariety"))
                                                                        .toString();
                                                        int mTotalBagCount =
                                                                jData.getInt("totalBagCount");
                                                        String mDeliveryDate =
                                                                Html.fromHtml(jData.getString(
                                                                                "deliveryDate"))
                                                                        .toString();
                                                        int mUserId = jData.getInt("userId");
                                                        String mDateCreated =
                                                                Html.fromHtml(jData.getString(
                                                                                "dateCreated"))
                                                                        .toString();
                                                        String mRegion =
                                                                Html.fromHtml(
                                                                                jData.getString("region"))
                                                                        .toString();
                                                        String mProvince =
                                                                Html.fromHtml(
                                                                                jData.getString("province"))
                                                                        .toString();
                                                        String mMunicipality =
                                                                Html.fromHtml(jData.getString(
                                                                                "municipality"))
                                                                        .toString();
                                                        String mDropOffPoint =
                                                                Html.fromHtml(jData.getString(
                                                                                "dropOffPoint"))
                                                                        .toString();

                                                        String mPrvDropoffId =
                                                                Html.fromHtml(jData.getString(
                                                                                "prv_dropoff_id"))
                                                                        .toString();
                                                        String mPrv =
                                                                Html.fromHtml(
                                                                                jData.getString("prv"))
                                                                        .toString();
                                                        String mMoaNumber =
                                                                Html.fromHtml(jData.getString(
                                                                                "moa_number"))
                                                                        .toString();
                                                        String mBatchSeries =
                                                                Html.fromHtml(jData.getString(
                                                                                "batchSeries"))
                                                                        .toString();
                                                        int misBuffer = jData.getInt("isBuffer");
                                                        int mHasActualDelivery = 0;
                                                        int mActualTotal = 0;
                                                        String mseed_distribution_mode =
                                                                Html.fromHtml(jData.getString(
                                                                                "seed_distribution_mode"))
                                                                        .toString();

                                                        //checking and inserting not existing value
                                                        if (Fun.checkDeliveryInspection(
                                                                InspectionMainActivity.this,
                                                                mDeliveryId) == 0) {
                                                            TblDeliveryInspection newData =
                                                                    new TblDeliveryInspection(
                                                                            mDeliveryId,
                                                                            mTicketNumber,
                                                                            mBatchTicketNumber,
                                                                            mCoopAccreditation,
                                                                            mSgAccreditation,
                                                                            mSeedTag, mSeedVariety,
                                                                            mTotalBagCount,
                                                                            mDeliveryDate, mUserId,
                                                                            mDateCreated,
                                                                            mRegion, mProvince,
                                                                            mMunicipality,
                                                                            mDropOffPoint,
                                                                            mPrvDropoffId, mPrv,
                                                                            mMoaNumber,
                                                                            mBatchSeries,
                                                                            mHasActualDelivery,
                                                                            mActualTotal,
                                                                            misBuffer,
                                                                            mseed_distribution_mode);
                                                            rcepDatabase.tblDeliveryInspectionDAO()
                                                                    .insert(newData);
                                    /* TblDelivery tblDelivery = new TblDelivery(mDeliveryId, mTicketNumber, mBatchTicketNumber, mCoopAccreditation, mSgAccreditation, mSeedTag, mSeedVariety, mTotalBagCount, mDeliveryDate, mUserId, mDateCreated, mRegion, mProvince, mMunicipality, mDropOffPoint);
                                    rcepDatabase.tblDeliveryDAO().insertDelivery(tblDelivery);*/
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
                                                            requestDeliveryStatusList(
                                                                    InspectionMainActivity.this,
                                                                    Fun.allBatchTickets(
                                                                            rcepDatabase.tblDeliveryInspectionDAO()
                                                                                    .getAllbatchTicket()));
                                                        }
                                                    }, 1000);


                                                    break;
                                                case "0":
                                                    //rcepDatabase.tblDeliveryDAO().nukeTable();
                          /*  mDelivery.clear();
                            mDelivery.addAll(getDeliveries());
                            deliveryAdapter.notifyDataSetChanged();*/
                                                    srl_refreshList.setRefreshing(false);
                                                    Toast.makeText(InspectionMainActivity.this,
                                                            message,
                                                            Toast.LENGTH_LONG).show();
                                                    break;
                                                case "3":
                                                    srl_refreshList.setRefreshing(false);
                                                    Toast.makeText(InspectionMainActivity.this,
                                                            "Error : Unable to connect in database server.",
                                                            Toast.LENGTH_LONG).show();
                                                    break;
                                            }
                                            //Log.e(TAG, "onResponse: " + message);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                        };
                        thread.start();
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
                        Toast.makeText(InspectionMainActivity.this,
                                "Server unreachable. Please check network connectivity and try again.",
                                Toast.LENGTH_SHORT).show();
                    }
                }, 1000);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("userId", userId);
                params.put("year", year);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(ctx);
        requestQueue.add(stringRequest);
    }

    private void requestDeliveryStatusList(final Context ctx, final String allBatchTicket) {
        // Fun.progressMessage(mProgressDialog, "updating delivery batch status");
        //url
        String request_url =
                Fun.getAddress(glbl_season) + "/script/si_delivery_status_list_request.php";
        //request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, request_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Thread thread = new Thread() {
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    public void run() {

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
                                                        int mDeliveryStatusId =
                                                                jData.getInt("deliveryStatusId");
                                                        String mBatchTicketNumber =
                                                                Html.fromHtml(jData.getString(
                                                                                "batchTicketNumber"))
                                                                        .toString();
                                                        int mStatus = jData.getInt("status");
                                                        String mDateCreated =
                                                                Html.fromHtml(jData.getString(
                                                                                "dateCreated"))
                                                                        .toString();
                                                        //checking and inserting not existing value
                                                        if (Fun.checkDeliveryStatus(
                                                                InspectionMainActivity.this,
                                                                mDeliveryStatusId) == 0) {
                                                            TblDeliveryStatus tblDeliveryStatus =
                                                                    new TblDeliveryStatus(
                                                                            mDeliveryStatusId,
                                                                            mBatchTicketNumber,
                                                                            mStatus,
                                                                            mDateCreated);
                                                            rcepDatabase.tblDeliveryStatusDAO()
                                                                    .insertDeliveryStatus(
                                                                            tblDeliveryStatus);
                                                        }
                                                    }
                                                    final Handler handler = new Handler();
                                                    handler.postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            //proceed verification screen
                                                            glbl_mainViewData.clear();
                                                            glbl_mainViewData.addAll(
                                                                    getDeliveryData());
                                                            inspectionSIViewAdapter.notifyDataSetChanged();
                                                            srl_refreshList.setRefreshing(false);
                                                        }
                                                    }, 1000);
                                                    break;
                                                case "0":
                                                    //Fun.progressStop(mProgressDialog);
                                                    glbl_mainViewData.clear();
                                                    rcepDatabase.tblDeliveryStatusDAO().nukeTable();
                                                    glbl_mainViewData.addAll(getDeliveryData());
                                                    inspectionSIViewAdapter.notifyDataSetChanged();
                                                   /*  mDelivery.clear();
                            mDelivery.addAll(getDeliveries());
                            deliveryAdapter.notifyDataSetChanged();*/
                                                    srl_refreshList.setRefreshing(false);
                                                    //Toast.makeText(ctx, message, Toast.LENGTH_LONG).show();
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
                                });
                            }
                        };
                        thread.start();
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


    private void downloadSamplingData(final Context ctx, final String batchTicketNumber) {
        Fun.progressMessage(mProgressDialog, "Requesting sampling data from central server");
        //url
        String request_url = Fun.getAddress(glbl_season) + "/script/si_dload_sampling_data.php";
        //request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, request_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Log.e(TAG, "downloadSamplingData: " + response);
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
                                        String mBatchTicketNumber =
                                                jData.getString("batchTicketNumber");
                                        String mSeedTag = jData.getString("seedTag");
                                        float mBagWeight =
                                                Float.valueOf(jData.getString("bagWeight"));
                                        String mDateSampled = jData.getString("dateSampled");
                                        int mSend = jData.getInt("send");
                                        String mPrv = jData.getString("prv");
                                        String mMoaNumber = jData.getString("moa_number");
                                        String mAppVersion = jData.getString("app_version");
                                        String mBatchSeries = jData.getString("batchSeries");

                                        if (rcepDatabase.tblSamplingDAO()
                                                .hasSamplingData(mBatchTicketNumber, mSeedTag,
                                                        mDateSampled) == 0) {
                                            TblSampling mData =
                                                    new TblSampling(mBatchTicketNumber, mSeedTag,
                                                            mBagWeight, mDateSampled, mSend, mPrv,
                                                            mMoaNumber, mAppVersion, mBatchSeries,
                                                            1, 0);

                                            //insert new data if not exist
                                            rcepDatabase.tblSamplingDAO().insertSampling(mData);
                                        }
                                    }
                                    /*proceed next process*/
                                    downloadActualDeliveryData(ctx, batchTicketNumber);
                                    break;
                                case "0":
                                    Fun.progressStop(mProgressDialog);
                                    /*clear data if failed*/
                                    Fun.removeBatchDownload(ctx, batchTicketNumber);
                                    Toast.makeText(ctx, message, Toast.LENGTH_LONG).show();
                                    break;
                                case "3":
                                    Fun.progressStop(mProgressDialog);
                                    /*clear data if failed*/
                                    Fun.removeBatchDownload(ctx, batchTicketNumber);
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
                        /*clear data if failed*/
                        Fun.removeBatchDownload(ctx, batchTicketNumber);
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
                params.put("batchTicketNumber", batchTicketNumber);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(ctx);
        requestQueue.add(stringRequest);
    }

    private void downloadActualDeliveryData(final Context ctx, final String batchTicketNumber) {
        Fun.progressMessage(mProgressDialog, "Requesting actual delivery data from central server");
        //url
        String request_url =
                Fun.getAddress(glbl_season) + "/script/si_dload_actual_delivery_data.php";
        //request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, request_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Log.e(TAG, "downloadActualDeliveryData: " + response);
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
                                        String mBatchTicketNumber =
                                                jData.getString("batchTicketNumber");
                                        String mRegion = jData.getString("region");
                                        String mProvince = jData.getString("province");
                                        String mMunicipality = jData.getString("municipality");
                                        String mDropOffPoint = jData.getString("dropOffPoint");
                                        String mSeedVariety = jData.getString("seedVariety");
                                        int mTotalBagCount = jData.getInt("totalBagCount");
                                        String mDateCreated = jData.getString("dateCreated");
                                        int mSend = jData.getInt("send");
                                        String mSeedTag = jData.getString("seedTag");
                                        String mPrvDropoffId = jData.getString("prv_dropoff_id");
                                        String mPrv = jData.getString("prv");
                                        String mMoaNumber = jData.getString("moa_number");
                                        String mAppVersion = jData.getString("app_version");
                                        String mBatchSeries = jData.getString("batchSeries");
                                        String mRemarks = jData.getString("remarks");
                                        int mIsRejected = jData.getInt("isRejected");
                                        String mSackCode = jData.getString("sack_code");
                                        int misbuffer = jData.getInt("isBuffer");

                                        if (rcepDatabase.tblActualDeliveryDAO()
                                                .hasActualDeliveryData(mBatchTicketNumber,
                                                        mSeedTag) == 0) {
                                            TblActualDelivery mData =
                                                    new TblActualDelivery(mBatchTicketNumber,
                                                            mRegion, mProvince, mMunicipality,
                                                            mDropOffPoint, mSeedVariety,
                                                            mTotalBagCount, mDateCreated, mSend,
                                                            mSeedTag, mPrvDropoffId, mPrv,
                                                            mMoaNumber, mAppVersion, mBatchSeries,
                                                            1, 0, mRemarks, mIsRejected,
                                                            1/*downloaded data from central server*/,
                                                            0, mSackCode, misbuffer, "", "", 0, 0);

                                            //insert new data if not exist
                                            rcepDatabase.tblActualDeliveryDAO()
                                                    .insertActualDelivery(mData);
                                        }
                                    }
                                    /* proceed next process*/
                                    downloadStatusData(ctx, batchTicketNumber);
                                    break;
                                case "0":
                                    Fun.progressStop(mProgressDialog);
                                    /*clear data if failed*/
                                    Fun.removeBatchDownload(ctx, batchTicketNumber);
                                    Toast.makeText(ctx, message, Toast.LENGTH_LONG).show();
                                    break;
                                case "3":
                                    Fun.progressStop(mProgressDialog);
                                    /*clear data if failed*/
                                    Fun.removeBatchDownload(ctx, batchTicketNumber);
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
                        /*clear data if failed*/
                        Fun.removeBatchDownload(ctx, batchTicketNumber);
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
                params.put("batchTicketNumber", batchTicketNumber);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(ctx);
        requestQueue.add(stringRequest);
    }

    private void downloadStatusData(final Context ctx, final String batchTicketNumber) {
        Fun.progressMessage(mProgressDialog, "Updating batch status");
        //url
        String request_url = Fun.getAddress(glbl_season) + "/script/si_dload_status_data.php";
        //request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, request_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Log.e(TAG, "downloadStatusData: " + response);
                        //Fun.progressStop(mProgressDialog);
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
                                        int mDeliveryStatusId = jData.getInt("deliveryStatusId");
                                        String mBatchTicketNumber =
                                                jData.getString("batchTicketNumber");
                                        int mStatus = jData.getInt("status");
                                        String mDateCreated = jData.getString("dateCreated");
                                        int mSend = jData.getInt("send");

                                        if (rcepDatabase.tblDeliveryStatusAppLocalDAO()
                                                .hasDeliveryStatusAppLocal(mBatchTicketNumber) ==
                                                0) {
                                            TblDeliveryStatusAppLocal mData =
                                                    new TblDeliveryStatusAppLocal(
                                                            mBatchTicketNumber, mStatus,
                                                            mDateCreated, 0, 1);
                                            //insert new data if not exist
                                            rcepDatabase.tblDeliveryStatusAppLocalDAO()
                                                    .insertDeliveryStatusAppLocal(mData);
                                        }
                                    }
                                    //success dialog
                                    final Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Fun.progressStop(mProgressDialog);
                                            new MaterialAlertDialogBuilder(
                                                    InspectionMainActivity.this)
                                                    .setTitle("RCEF DI")
                                                    .setCancelable(false)
                                                    .setMessage("Batch " + batchTicketNumber +
                                                            " data has been downloaded. You can now send it to your local server.")
                                                    .setPositiveButton("OK",
                                                            new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(
                                                                        DialogInterface dialogInterface,
                                                                        int i) {
                                                                    dialogInterface.dismiss();
                                                                    glbl_mainViewData.clear();
                                                                    glbl_mainViewData
                                                                            .addAll(getDeliveryData());
                                                                    inspectionSIViewAdapter
                                                                            .notifyDataSetChanged();
                                                                }
                                                            })
                                                    .show();

                                            /*CFAlertDialog.Builder builder =
                                                    new CFAlertDialog.Builder(
                                                            InspectionMainActivity.this);
                                            builder.setDialogStyle(
                                                    CFAlertDialog.CFAlertStyle.ALERT);
                                            builder.setMessage("Batch " + batchTicketNumber +
                                                    " data has been downloaded. You can now send it to your local server.");
                                            builder.setCancelable(false);
                                            builder.addButton("OK", Color.parseColor("#FFFFFF"),
                                                    Color.parseColor("#429ef4"),
                                                    CFAlertDialog.CFAlertActionStyle.POSITIVE,
                                                    CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                                                    new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(
                                                                final DialogInterface dialog,
                                                                int which) {
                                                            dialog.dismiss();
                                                            glbl_mainViewData.clear();
                                                            glbl_mainViewData
                                                                    .addAll(getDeliveryData());
                                                            inspectionSIViewAdapter
                                                                    .notifyDataSetChanged();
                                                        }
                                                    });
                                            builder.show();*/
                                        }
                                    }, 3000);
                                    break;
                                case "0":
                                    Fun.progressStop(mProgressDialog);
                                    /*clear data if failed*/
                                    Fun.removeBatchDownload(ctx, batchTicketNumber);
                                    Toast.makeText(ctx, message, Toast.LENGTH_LONG).show();
                                    break;
                                case "3":
                                    Fun.progressStop(mProgressDialog);
                                    /*clear data if failed*/
                                    Fun.removeBatchDownload(ctx, batchTicketNumber);
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
                        /*clear data if failed*/
                        Fun.removeBatchDownload(ctx, batchTicketNumber);
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
                params.put("batchTicketNumber", batchTicketNumber);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(ctx);
        requestQueue.add(stringRequest);
    }

    public void pingServer(View view) {
        pingLocalServer(this, Fun.getDynamicIP(prefUserAccount, this));
    }
}
