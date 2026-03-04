package ph.gov.philrice.rcepdeliveryinspection.userLogin;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Build;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
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
import com.permissionx.guolindev.PermissionX;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import at.favre.lib.crypto.bcrypt.BCrypt;
import in.galaxyofandroid.spinerdialog.OnSpinerItemClick;
import in.galaxyofandroid.spinerdialog.SpinnerDialog;
import ph.gov.philrice.rcepdeliveryinspection.Fun;
import ph.gov.philrice.rcepdeliveryinspection.MyCustomDialog;
import ph.gov.philrice.rcepdeliveryinspection.R;
import ph.gov.philrice.rcepdeliveryinspection.Station;
import ph.gov.philrice.rcepdeliveryinspection.dbase.database.RCEPDatabase;
import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.LibSeeds;
import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TblCommitment;
import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TblTotalCommitment;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    //Variables
    String[] appPermissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.INTERNET,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    String[] permissions;
    private static final int PERMISSIONS_REQUEST_CODE = 143;
    SharedPreferences prefUserAccount;
    SharedPreferences prefThreshold;
    //Views
    private EditText username, password;
    private ProgressDialog mProgressDialog;
    ArrayList<String> season_select;
    //Variables
    RCEPDatabase rcepDatabase;

    String glbl_season;

    private TextView tv_season, tv_station, tv_version;

    ArrayList<Station> source_station;
    ArrayList<String> station_select;
    Station current_station;
    MyCustomDialog myCustomDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //initialize screen
        Fun.fullScreen(this);//fullScreen
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        //getSupportActionBar().hide(); // hide the title bar
        //setScreenActivityContents
        setContentView(R.layout.activity_login);

        if (Build.VERSION.SDK_INT >= 33) {
            permissions = new String[]{Manifest.permission.INTERNET,
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_MEDIA_IMAGES};
        } else {
            permissions = new String[]{Manifest.permission.INTERNET,
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    //Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
        }

        initApp();

    }

    /*public void login(View view) {
        Intent intent = new Intent(LoginActivity.this, VerifyLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }*/

/*    public boolean checkAndRequestPermissions() {
        //Check which permissions are granted
        List<String> mPermissionsNeeded = new ArrayList<>();
        for (String perm : appPermissions) {
            if (ContextCompat.checkSelfPermission(this, perm) !=
                    PackageManager.PERMISSION_GRANTED) {
                mPermissionsNeeded.add(perm);
            }
        }
        //Ask for non-granted permissions
        if (!mPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    mPermissionsNeeded.toArray(new String[mPermissionsNeeded.size()]),
                    PERMISSIONS_REQUEST_CODE
            );
            return false;
        }
        //App has all permissions. Proceed ahead
        return true;
    }*/


    private void initApp() {
        prefUserAccount = getApplicationContext()
                .getSharedPreferences(Fun.userAccountPreference(), Context.MODE_PRIVATE);
        prefThreshold = getApplicationContext()
                .getSharedPreferences(Fun.thresholdPreference(), Context.MODE_PRIVATE);
        rcepDatabase = RCEPDatabase.getAppDatabase(this);//initializing dbase

        glbl_season = "";

        source_station = new ArrayList<>();
        station_select = new ArrayList<>();

        season_select = new ArrayList<>();
        tv_version = findViewById(R.id.tv_version);
        tv_station = findViewById(R.id.tv_station);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        tv_season = findViewById(R.id.tv_season);
        mProgressDialog = new ProgressDialog(this);

        tv_version.setText(Fun.appVersion());

        String mPassword = prefUserAccount.getString(Fun.uaPassword(), "");

        if (mPassword.length() > 0) {
            Intent intent = new Intent(LoginActivity.this, VerifyLoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }

        /*Dexter.withContext(LoginActivity.this)
                .withPermissions(permissions)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(
                            MultiplePermissionsReport multiplePermissionsReport) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list,
                                                                   PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();*/

        // Request permissions
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
                                "Forward",
                                "Discard"))
                .request((allGranted, grantedList, deniedList) -> {
                    if (allGranted) {
                        //do nothing
                    }
                });


    }


    public void login(View view) {
        //glbl_season = prefUserAccount.getString(Fun.uaSeason(), "");

        if (glbl_season.equals("") || tv_season.getText().toString().trim().equals("") /*||
                tv_station.getText().toString().trim().equals("")*/) {
            Toast.makeText(this, "RCEF Season required", Toast.LENGTH_SHORT).show();
        } else {
            // Request permissions
            PermissionX.init(this)
                    .permissions(permissions)
                    .onExplainRequestReason((scope, deniedList) ->
                            scope.showRequestReasonDialog(deniedList,
                                    "Binhi e-Padala requires all permission to work properly",
                                    "Ok",
                                    "Cancel"))
                    .onForwardToSettings((scope, deniedList) ->
                            scope.showForwardToSettingsDialog(deniedList,
                                    "Binhi e-Padala requires all permission to work properly",
                                    "Forward",
                                    "Discard"))
                    .request((allGranted, grantedList, deniedList) -> {
                        if (allGranted) {
                            //assign username and password
                            String mUsername = username.getText().toString();
                            String mPassword = password.getText().toString();

                            //user and pass validation
                            if (!mUsername.isEmpty() && !mPassword.isEmpty()) {
                                if (!Fun.isTimeAutomatic(LoginActivity.this)) {
                                    //automatic date and time is disabled
                                    //do function here
                                    new MaterialAlertDialogBuilder(LoginActivity.this)
                                            .setTitle("RCEF DI")
                                            .setCancelable(false)
                                            .setMessage(
                                                    "Automatic date and time is not set. Please \"Go to settings\" to proceed.")
                                            .setPositiveButton("Go to settings",
                                                    new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(
                                                                DialogInterface dialogInterface,
                                                                int i) {
                                                            dialogInterface.dismiss();
                                                            startActivity(new Intent(
                                                                    android.provider.Settings.ACTION_DATE_SETTINGS));
                                                        }
                                                    })
                                            .show();

                                    /*CFAlertDialog.Builder builder =
                                            new CFAlertDialog.Builder(LoginActivity.this);
                                    builder.setDialogStyle(
                                            CFAlertDialog.CFAlertStyle.BOTTOM_SHEET);
                                    builder.setTitle("RCEP App");
                                    builder.setCancelable(false);
                                    builder.setMessage(
                                            "Automatic date and time is not set. Please \"Go to settings\" to proceed.");
                                    builder.addButton("Go to settings",
                                            Color.parseColor("#FFFFFF"),
                                            Color.parseColor("#429ef4"),
                                            CFAlertDialog.CFAlertActionStyle.POSITIVE,
                                            CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                                    int which) {
                                                    dialog.dismiss();
                                                    startActivity(new Intent(
                                                            android.provider.Settings.ACTION_DATE_SETTINGS));
                                                }
                                            });
                                    builder.show();*/
                                } else {
                                    Fun.hideKeyboard(LoginActivity.this);
                                    glbl_season =
                                            prefUserAccount.getString(Fun.uaSeason(), "");
                                    Log.e(TAG,
                                            "glbl_season||prefUserAccount: " + glbl_season);
                                    login(LoginActivity.this, mUsername, mPassword);
                                }
                            } else {
                                Toast.makeText(LoginActivity.this,
                                                "Please input username and password",
                                                Toast.LENGTH_LONG)
                                        .show();
                            }
                        }
                    });
        }
    }
//end of activity

    private void login(final Context ctx, final String user, final String pass) {
        Fun.progressStart(mProgressDialog, "", "Verifying user account");
        Log.e(TAG, "glbl_season: " + glbl_season);
        //url
        final String login_request_url = Fun.getAddress(glbl_season) + "/script/login_request.php";
        Log.e(TAG, "login_request_url: " + login_request_url);
        //prepare the Request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, login_request_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Log.e(TAG, "onResponse: " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String status = jsonObject.getString("status");
                            String message = jsonObject.getString("message");
                            switch (status) {
                                case "1":
                                    Fun.progressMessage(mProgressDialog, "Checking user details");
                                    //declaring preferece
                                    SharedPreferences.Editor editor = prefUserAccount.edit();
                                    //parsing json
                                    JSONObject json = new JSONObject(message);
                                    //name of array is result
                                    JSONArray jArray = json.getJSONArray("result");
                                    JSONObject jData;
                                    for (int i = 0; i < jArray.length(); i++) {
                                        jData = jArray.getJSONObject(i);
                                        int mUserId = jData.getInt("userId");
                                        String mFirstName = jData.getString("firstName");
                                        String mMidName = jData.getString("middleName");
                                        String mLastName = jData.getString("lastName");
                                        String mPassword = jData.getString("password");
                                        String mUserRoles = jData.getString("userRoles").trim();
                                        Log.e(TAG, "role : " + mUserRoles);
                                        String mCoopAccreditation =
                                                jData.getString("coopAccreditation");
                                        //Log.e(TAG, "mCoopAccreditation: " + mCoopAccreditation);
                                        //Password verification
                                        BCrypt.Result result = BCrypt.verifyer()
                                                .verify(pass.toCharArray(), mPassword);
                                        if (result.verified) {

                                            editor.putString(Fun.uaStationName(),
                                                    "PhilRice Central Experiment Station");
                                            editor.putString(Fun.uaStationExternalIP(), "--");
                                            editor.putString(Fun.uaStationDomain(), "--");
                                            editor.putString(Fun.uaStationPort(), "--");
                                            editor.apply();


                                            //splitting array role
                                            //final String[] roles = mUserRoles.split(">>");
                                            //check if multiple role as inspector and seed grower
                                            switch (mUserRoles) {
                                                case "seed-grower":
                                                case "delivery-manager":
                                                    //seed grower role only
                                                    if (mCoopAccreditation.equals("null")) {
                                                        Fun.progressStop(mProgressDialog);

                                                        new MaterialAlertDialogBuilder(
                                                                LoginActivity.this)
                                                                .setTitle("RCEF DI")
                                                                .setCancelable(false)
                                                                .setMessage(
                                                                        "Please contact your RC/PC to assign account on your respective cooperative")
                                                                .setPositiveButton("GOT IT",
                                                                        new DialogInterface.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(
                                                                                    DialogInterface dialogInterface,
                                                                                    int i) {
                                                                                dialogInterface.dismiss();
                                                                            }
                                                                        })
                                                                .setNegativeButton("CANCEL",
                                                                        new DialogInterface.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(
                                                                                    DialogInterface dialogInterface,
                                                                                    int i) {

                                                                            }
                                                                        })
                                                                .show();

                                                        /*CFAlertDialog.Builder builder =
                                                                new CFAlertDialog.Builder(
                                                                        LoginActivity.this);
                                                        builder.setDialogStyle(
                                                                CFAlertDialog.CFAlertStyle.ALERT);
                                                        builder.setMessage(
                                                                "Please contact your RC/PC to assign account on your respective cooperative");
                                                        builder.addButton("OK",
                                                                Color.parseColor("#FFFFFF"),
                                                                Color.parseColor("#429ef4"),
                                                                CFAlertDialog.CFAlertActionStyle.POSITIVE,
                                                                CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                                                                new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(
                                                                            DialogInterface dialog,
                                                                            int which) {
                                                                        dialog.dismiss();
                                                                    }
                                                                });
                                                        builder.show();*/
                                                    } else {
                                                        //inputting result to preference
                                                        editor.putInt(Fun.uaUserId(), mUserId);
                                                        editor.putString(Fun.uaFirstName(),
                                                                mFirstName);
                                                        editor.putString(Fun.uaMidName(), mMidName);
                                                        editor.putString(Fun.uaLastName(),
                                                                mLastName);
                                                        editor.putString(Fun.uaPassword(),
                                                                mPassword);
                                                        editor.putString(Fun.uaUserRoles(),
                                                                mUserRoles);
                                                        editor.putString(Fun.uaCoopAccreditation(),
                                                                mCoopAccreditation);
                                                        editor.apply();
                                                   /* //request seed library
                                                    requestSeedsLib(LoginActivity.this);*/
                                                        //request current moa of seed cooperative
                                                        currentMoaRequest(ctx, mCoopAccreditation);
                                                    }
                                                    break;

                                                case "seed-inspector":
                                                    //inputting result to preference
                                                    editor.putInt(Fun.uaUserId(), mUserId);
                                                    editor.putString(Fun.uaFirstName(), mFirstName);
                                                    editor.putString(Fun.uaMidName(), mMidName);
                                                    editor.putString(Fun.uaLastName(), mLastName);
                                                    editor.putString(Fun.uaPassword(), mPassword);
                                                    editor.putString(Fun.uaUserRoles(), mUserRoles);
                                                    editor.putString(Fun.uaCoopAccreditation(),
                                                            mCoopAccreditation);
                                                    editor.apply();
                                                    //request seed library
                                                    //requestSeedsLib(LoginActivity.this);

                                                    //end of request proceed to verify activity
                                                    final Handler handler = new Handler();
                                                    handler.postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Fun.progressStop(mProgressDialog);
                                                            Intent intent =
                                                                    new Intent(LoginActivity.this,
                                                                            VerifyLoginActivity.class);
                                                            intent.setFlags(
                                                                    Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    }, 1500);
                                                    break;

                                                default:
                                                    Toast.makeText(LoginActivity.this,
                                                            "Invalid user role. Please contact administrator.",
                                                            Toast.LENGTH_LONG).show();
                                                    Fun.progressStop(mProgressDialog);
                                            }

/*
                                    if (Arrays.asList(roles).contains("seed-inspector") && Arrays.asList(roles).contains("seed-grower")) {
                                        //seed inspector and seed grower
                                        Toast.makeText(LoginActivity.this, "Multiple roles detected cannot proceed. Please contact administrator.", Toast.LENGTH_LONG).show();
                                        Fun.progressStop(mProgressDialog);
                                    } else {
                                        //verify individual roles
                                        if (Arrays.asList(roles).contains("seed-inspector")) {
                                            //seed inspector role
                                            //inputting result to preference
                                            editor.putInt(Fun.uaUserId(), mUserId);
                                            editor.putString(Fun.uaFirstName(), mFirstName);
                                            editor.putString(Fun.uaMidName(), mMidName);
                                            editor.putString(Fun.uaLastName(), mLastName);
                                            editor.putString(Fun.uaPassword(), mPassword);
                                            editor.putString(Fun.uaUserRoles(), mUserRoles);
                                            editor.putString(Fun.uaCoopAccreditation(), mCoopAccreditation);
                                            editor.apply();
                                            //request seed library
                                            //requestSeedsLib(LoginActivity.this);

                                            //end of request proceed to verify activity
                                            final Handler handler = new Handler();
                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Fun.progressStop(mProgressDialog);
                                                    Intent intent = new Intent(LoginActivity.this, VerifyLoginActivity.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            }, 1500);
                                        } else {
                                            if (Arrays.asList(roles).contains("seed-grower")) {
                                                //seed grower role only
                                                if (mCoopAccreditation.equals("null")) {
                                                    Fun.progressStop(mProgressDialog);
                                                    CFAlertDialog.Builder builder = new CFAlertDialog.Builder(LoginActivity.this);
                                                    builder.setDialogStyle(CFAlertDialog.CFAlertStyle.ALERT);
                                                    builder.setMessage("Please contact your RC/PC to assign account on your respective cooperative");
                                                    builder.addButton("OK", Color.parseColor("#FFFFFF"), Color.parseColor("#429ef4"), CFAlertDialog.CFAlertActionStyle.POSITIVE, CFAlertDialog.CFAlertActionAlignment.JUSTIFIED, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            dialog.dismiss();
                                                        }
                                                    });
                                                    builder.show();
                                                } else {
                                                    //inputting result to preference
                                                    editor.putInt(Fun.uaUserId(), mUserId);
                                                    editor.putString(Fun.uaFirstName(), mFirstName);
                                                    editor.putString(Fun.uaMidName(), mMidName);
                                                    editor.putString(Fun.uaLastName(), mLastName);
                                                    editor.putString(Fun.uaPassword(), mPassword);
                                                    editor.putString(Fun.uaUserRoles(), mUserRoles);
                                                    editor.putString(Fun.uaCoopAccreditation(), mCoopAccreditation);
                                                    editor.apply();
                                                   *//* //request seed library
                                                    requestSeedsLib(LoginActivity.this);*//*
                                                    //request current moa of seed cooperative
                                                    currentMoaRequest(ctx, mCoopAccreditation);
                                                }
                                            } else {

                                                //default case
                                                Toast.makeText(LoginActivity.this, "Invalid user role. Please contact administrator.", Toast.LENGTH_LONG).show();
                                                Fun.progressStop(mProgressDialog);
                                            }
                                        }
                                    }*/

                                        } else {
                                            //Log.e(TAG, "onCreate: Not Matched!");
                                            Toast.makeText(ctx, "Invalid credentials",
                                                    Toast.LENGTH_SHORT).show();
                                            Fun.progressStop(mProgressDialog);
                                        }
                                    }
                                    break;
                                case "0":
                                    Fun.progressStop(mProgressDialog);
                                    Toast.makeText(ctx, message, Toast.LENGTH_LONG).show();
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
                params.put("user", user);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(ctx);
        requestQueue.add(stringRequest);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                -1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    private void currentMoaRequest(final Context ctx, final String accreditation_no) {
        Log.e(TAG, "currentMoaRequest: " + accreditation_no);
        Fun.progressMessage(mProgressDialog, "requesting current current moa");
        //url
        final String mRequest = Fun.getAddress(glbl_season) + "/script/sg_current_moa_request.php";
        //prepare the Request
        StringRequest stringRequest =
                new StringRequest(Request.Method.POST, mRequest, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String status = jsonObject.getString("status");
                            String message = jsonObject.getString("message");
                            switch (status) {
                                case "1":
                                    SharedPreferences.Editor editor = prefUserAccount.edit();
                                    //parsing json
                                    JSONObject json = new JSONObject(message);
                                    //name of array is result
                                    JSONArray jArray = json.getJSONArray("result");
                                    JSONObject jData = null;
                                    for (int i = 0; i < jArray.length(); i++) {
                                        jData = jArray.getJSONObject(i);
                                        //decode html_encode value from server
                                        String coopName = Html.fromHtml(jData.getString("coopName"))
                                                .toString();
                                        String acronym = Html.fromHtml(jData.getString("acronym"))
                                                .toString();
                                        String current_moa =
                                                Html.fromHtml(jData.getString("current_moa"))
                                                        .toString();
                                        //update moa
                                        editor.putString(Fun.uaCoopCurrentMOA(), current_moa);
                                        editor.putString(Fun.uaCoopAcronym(), acronym);
                                        editor.apply();
                                    }
                                    final Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            String current_moa = prefUserAccount
                                                    .getString(Fun.uaCoopCurrentMOA(), "");
                                            if (current_moa.equals("") ||
                                                    current_moa.equals("null")) {
                                                //do nothing stop requests here
                                                Fun.progressStop(mProgressDialog);
                                                prefUserAccount.edit().clear().apply();
                                            } else {
                                                // proceed total commitment request
                                                totalCommitmentRequest(ctx, current_moa);
                                            }
                                        }
                                    }, 250);
                                    break;
                                case "0":
                                    Fun.progressStop(mProgressDialog);
                                    //Fun.nukeTables(appDatabase);
                                    prefUserAccount.edit().clear().apply();
                                    Toast.makeText(ctx, message, Toast.LENGTH_LONG).show();
                                    break;
                                case "3":
                                    Fun.progressStop(mProgressDialog);
                                    //Fun.nukeTables(appDatabase);
                                    prefUserAccount.edit().clear().apply();
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
                        prefUserAccount.edit().clear().apply();
                        Toast.makeText(ctx,
                                "Server unreachable. Please check network connectivity and try again.",
                                Toast.LENGTH_SHORT).show();
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("accreditation_no", accreditation_no);
                        return params;
                    }
                };
        RequestQueue requestQueue = Volley.newRequestQueue(ctx);
        requestQueue.add(stringRequest);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                -1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    private void totalCommitmentRequest(final Context ctx, final String moa_number) {
        Log.e(TAG, "totalCommitmentRequest: " + moa_number);
        Fun.progressMessage(mProgressDialog, "requesting cooperative total commitment");
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
                                        if (rcepDatabase.tblTotalCommitmentDAO()
                                                .isExisting(totalCommitmentId) == 0) {
                                            //inserting
                                            TblTotalCommitment totalCommitment =
                                                    new TblTotalCommitment(totalCommitmentId,
                                                            total_value, moa_number);
                                            rcepDatabase.tblTotalCommitmentDAO()
                                                    .insert(totalCommitment);
                                        }
                                    }
                                    //proceed commitment request
                                    commitmentRequest(ctx, moa_number);
                                    break;
                                case "0":
                                    Fun.progressStop(mProgressDialog);
                                    prefUserAccount.edit().clear().apply();
                                    Toast.makeText(ctx, message, Toast.LENGTH_LONG).show();
                                    break;
                                case "3":
                                    Fun.progressStop(mProgressDialog);
                                    prefUserAccount.edit().clear().apply();
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
                        prefUserAccount.edit().clear().apply();
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
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                -1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    private void commitmentRequest(final Context ctx, final String moa_number) {
        Log.e(TAG, "commitmentRequest: " + moa_number);
        Fun.progressMessage(mProgressDialog, "requesting cooperative commitment");
        //url
        final String request_article_url =
                Fun.getAddress(glbl_season) + "/script/sg_commitment_request.php";
        //prepare the Request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, request_article_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
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
                                        if (rcepDatabase.tblCommitmentDAO()
                                                .isExisting(commitmentId) == 0) {
                                            //inserting
                                            TblCommitment commitment =
                                                    new TblCommitment(commitmentId,
                                                            commitment_value, commitment_variety,
                                                            moa_number);
                                            rcepDatabase.tblCommitmentDAO().insert(commitment);
                                        }
                                        //verifying of variety exists
                                        if (rcepDatabase.libSeedsDAO()
                                                .checkVariety(commitment_variety) == 0) {
                                            LibSeeds seeds = new LibSeeds(commitment_variety);
                                            rcepDatabase.libSeedsDAO().insertSeed(seeds);
                                        }
                                    }
                                    //next request
                                    //requestThreshold(ctx);
                                    //end of request proceed to verify activity
                                    final Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Fun.progressStop(mProgressDialog);
                                            Intent intent = new Intent(LoginActivity.this,
                                                    VerifyLoginActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }, 1500);

                                    break;
                                case "0":
                                    Fun.progressStop(mProgressDialog);
                                    prefUserAccount.edit().clear().apply();
                                    Toast.makeText(ctx, message, Toast.LENGTH_LONG).show();
                                    break;
                                case "3":
                                    Fun.progressStop(mProgressDialog);
                                    prefUserAccount.edit().clear().apply();
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
                prefUserAccount.edit().clear().apply();
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
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                -1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    private void seasonRequest(final Context ctx, String version) {
        season_select.clear();
        Fun.progressStart(mProgressDialog, "", "Requesting available seasons...");
        //url
        final String request_url = Fun.apiLinkSeasonStatic();
        Log.e(TAG, "seasonRequest: " + request_url);
        //prepare the Request
        StringRequest stringRequest =
                new StringRequest(Request.Method.POST, request_url, response -> {
                    Log.e(TAG, "prvRequest: " + response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");
                        String message = jsonObject.getString("message");
                        switch (status) {
                            case "1":
                                season_select.clear();
                                //parsing json
                                JSONObject json = new JSONObject(message);
                                //name of array is result
                                JSONArray jArray = json.getJSONArray("result");
                                JSONObject jData = null;
                                for (int i = 0; i < jArray.length(); i++) {
                                    jData = jArray.getJSONObject(i);
                                    //decode html_encode value from server
                                    int mId = jData.getInt("id");
                                    String mSeason = jData.getString("season");
                                    season_select.add(mSeason);
                                }

                                SpinnerDialog ss =
                                        new SpinnerDialog(LoginActivity.this, season_select,
                                                "Select Season");
                                ss.setCancellable(false);
                                ss.setShowKeyboard(false);
                                ss.bindOnSpinerListener(new OnSpinerItemClick() {
                                    @Override
                                    public void onClick(final String item, int position) {
                                        glbl_season = item;
                                        SharedPreferences.Editor editor = prefUserAccount.edit();
                                        editor.putString(Fun.uaSeason(), item);
                                        editor.apply();

//                                        Log.e(TAG, "glbl_season: " + DebugDB.getAddressLog());

                                        tv_season.setText(item);
                                    }
                                });
                                ss.showSpinerDialog();

                                Fun.progressStop(mProgressDialog);
                                break;
                            case "0":
                                Fun.progressStop(mProgressDialog);
                                Toast.makeText(ctx, message, Toast.LENGTH_LONG).show();
                                break;
                            case "3":
                                Fun.progressStop(mProgressDialog);
                                Toast.makeText(ctx, "Error : Unable to connect in database server.",
                                        Toast.LENGTH_LONG).show();
                                break;
                        }
                    } catch (JSONException e) {
                        Fun.progressStop(mProgressDialog);
                        e.printStackTrace();
                        Toast.makeText(ctx, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }, error -> {
                    Fun.progressStop(mProgressDialog);


                    new MaterialAlertDialogBuilder(LoginActivity.this)
                            .setTitle("RCEF DI")
                            .setCancelable(false)
                            .setMessage(
                                    "Failed to request season. Please check network connectivity. Do you want to retry?")
                            .setPositiveButton("RETRY", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                    seasonRequest(ctx, version);
                                }
                            })
                            .setNegativeButton("EXIT APPLICATION",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface,
                                                            int i) {
                                            dialogInterface.dismiss();
                                            finishAffinity();
                                            System.exit(0);
                                        }
                                    })
                            .show();


                    /*CFAlertDialog.Builder builder = new CFAlertDialog.Builder(LoginActivity.this);
                    builder.setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET);
                    builder.setMessage("Failed to request season. Do you want to retry?");
                    builder.addButton("RETRY", Color.parseColor("#FFFFFF"),
                            Color.parseColor("#429ef4"), CFAlertDialog.CFAlertActionStyle.POSITIVE,
                            CFAlertDialog.CFAlertActionAlignment.JUSTIFIED, (dialog, which) -> {
                                dialog.dismiss();
                                seasonRequest(ctx, version);
                            });

                    builder.addButton("EXIT APPLICATION", Color.parseColor("#FFFFFF"),
                            Color.parseColor("#429ef4"), CFAlertDialog.CFAlertActionStyle.NEGATIVE,
                            CFAlertDialog.CFAlertActionAlignment.JUSTIFIED, (dialog, which) -> {
                                dialog.dismiss();
                                finishAffinity();
                                System.exit(0);
                            });
                    builder.show();*/
                }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("version", version);
                        return params;
                    }
                };
        RequestQueue requestQueue = Volley.newRequestQueue(ctx);
        // Set a more robust retry policy
        int timeout = 5000; // 5 seconds timeout
        int maxRetries = 3; // Retry up to 3 times
        float backoffMultiplier = 1.5f; // Increase wait time between retries
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                timeout,
                maxRetries,
                backoffMultiplier
        ));
        requestQueue.add(stringRequest);
    }

    private void stationServerRequest(final Context ctx) {
        station_select.clear();
        source_station.clear();
        Fun.progressStart(mProgressDialog, "", "requesting available station servers...");
        //url
        final String request_url = Fun.apiLinkStationServers();
        Log.e(TAG, "stationServerRequest: " + request_url);
        //prepare the Request
        StringRequest stringRequest =
                new StringRequest(Request.Method.GET, request_url, response -> {
                    Log.e(TAG, "stationServerRequest: " + response);
                    try {
                        JSONObject main = new JSONObject(response);
                        String servers = main.optString("servers");

                        JSONArray arr = new JSONArray(servers);
                        if (arr.length() > 0) {
                            JSONObject obj;
                            for (int i = 0; i < arr.length(); i++) {
                                obj = arr.getJSONObject(i);
                                //assign to variable
                                int station_id = obj.getInt("station_id");
                                String station_name = obj.optString("station_name");
                                String external_ip = obj.optString("external_ip");
                                String subdomain_name = obj.optString("subdomain_name");
                                String port = obj.optString("port");
                                int is_active = obj.getInt("is_active");
                                //feed source and selection
                                source_station.add(
                                        new Station(station_id, station_name, external_ip,
                                                subdomain_name, port, is_active));
                                station_select.add(station_name);
                            }

                            Fun.progressStop(mProgressDialog);
                            SpinnerDialog ss = new SpinnerDialog(LoginActivity.this, station_select,
                                    "Select station");
                            ss.setCancellable(false);
                            ss.setShowKeyboard(false);
                            ss.bindOnSpinerListener(new OnSpinerItemClick() {
                                @Override
                                public void onClick(final String item, int position) {

                                    current_station = source_station.get(position);

                                    SharedPreferences.Editor editor = prefUserAccount.edit();
                                    editor.putString(Fun.uaStationName(),
                                            current_station.getStation_name());
                                    editor.putString(Fun.uaStationExternalIP(),
                                            current_station.getExternal_ip());
                                    editor.putString(Fun.uaStationDomain(),
                                            current_station.getSubdomain_name());
                                    editor.putString(Fun.uaStationPort(),
                                            current_station.getPort());
                                    editor.apply();

                                    tv_station.setText(current_station.getStation_name());
                                }
                            });
                            ss.showSpinerDialog();
                        } else {
                            Log.e(TAG, "No active station server found");
                        }
                    } catch (JSONException e) {
                        Toast.makeText(ctx, "", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }, error -> {
                    Log.e(TAG, "stationServerRequest: " + error.toString());
                    Fun.progressStop(mProgressDialog);

                    new MaterialAlertDialogBuilder(LoginActivity.this)
                            .setTitle("RCEF DI")
                            .setCancelable(false)
                            .setMessage(
                                    "Failed to request station. Please check network connectivity. Do you want to retry?")
                            .setPositiveButton("RETRY", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                    stationServerRequest(ctx);
                                }
                            })
                            .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                    finishAffinity();
                                    System.exit(0);
                                }
                            })
                            .show();

                   /* CFAlertDialog.Builder builder = new CFAlertDialog.Builder(LoginActivity.this);
                    builder.setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET);
                    builder.setMessage("Failed to request station. Do you want to retry?");
                    builder.addButton("RETRY", Color.parseColor("#FFFFFF"),
                            Color.parseColor("#429ef4"), CFAlertDialog.CFAlertActionStyle.POSITIVE,
                            CFAlertDialog.CFAlertActionAlignment.JUSTIFIED, (dialog, which) -> {
                                dialog.dismiss();
                                seasonRequest(ctx, Fun.appVersion());
                            });

                    builder.addButton("EXIT APPLICATION", Color.parseColor("#FFFFFF"),
                            Color.parseColor("#429ef4"), CFAlertDialog.CFAlertActionStyle.NEGATIVE,
                            CFAlertDialog.CFAlertActionAlignment.JUSTIFIED, (dialog, which) -> {
                                dialog.dismiss();
                                finishAffinity();
                                System.exit(0);
                            });
                    builder.show();*/
                });
        RequestQueue requestQueue = Volley.newRequestQueue(ctx);
        // Set a more robust retry policy
        int timeout = 5000; // 5 seconds timeout
        int maxRetries = 3; // Retry up to 3 times
        float backoffMultiplier = 1.5f; // Increase wait time between retries
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                timeout,
                maxRetries,
                backoffMultiplier
        ));
        requestQueue.add(stringRequest);
    }

    @Override
    public void onBackPressed() {
        new MaterialAlertDialogBuilder(LoginActivity.this)
                .setTitle("RCEF DI")
                .setCancelable(false)
                .setMessage("Do you really want to exit application?")
                .setPositiveButton("EXIT", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        finishAffinity();
                        System.exit(0);
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .show();

/*
        CFAlertDialog.Builder builder = new CFAlertDialog.Builder(LoginActivity.this);
        builder.setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET);
        builder.setTitle("RCEP App");
        builder.setMessage("Do you really want to exit application?");
        builder.addButton("EXIT", Color.parseColor("#FFFFFF"), Color.parseColor("#429ef4"),
                CFAlertDialog.CFAlertActionStyle.POSITIVE,
                CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finishAffinity();
                        System.exit(0);
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

    public void removefocus(View view) {
        Fun.hideKeyboard(LoginActivity.this);
        username.findViewById(R.id.username).clearFocus();
        password.findViewById(R.id.password).clearFocus();
    }

    public void setSeason(View view) {
        if (season_select.size() > 0) {
            SpinnerDialog ss =
                    new SpinnerDialog(LoginActivity.this, season_select,
                            "Select Season");
            ss.setCancellable(false);
            ss.setShowKeyboard(false);
            ss.bindOnSpinerListener(new OnSpinerItemClick() {
                @Override
                public void onClick(final String item, int position) {
                    glbl_season = item;
                    SharedPreferences.Editor editor = prefUserAccount.edit();
                    editor.putString(Fun.uaSeason(), item);
                    editor.apply();
                    tv_season.setText(item);
                }
            });
            ss.showSpinerDialog();
        } else {
            seasonRequest(getBaseContext(), Fun.appVersion());
        }
    }

    public void setStation(View view) {
        if (!source_station.isEmpty()) {
            SpinnerDialog ss =
                    new SpinnerDialog(LoginActivity.this, station_select, "Select station");
            ss.setCancellable(false);
            ss.setShowKeyboard(false);
            ss.bindOnSpinerListener(new OnSpinerItemClick() {
                @Override
                public void onClick(final String item, int position) {
                    /*SharedPreferences.Editor editor = prefAccount.edit();
                    editor.putString(Fun.paSeason(), item);
                    editor.apply();*/
                    current_station = source_station.get(position);

                    SharedPreferences.Editor editor = prefUserAccount.edit();
                    editor.putString(Fun.uaStationName(), current_station.getStation_name());
                    editor.putString(Fun.uaStationExternalIP(), current_station.getExternal_ip());
                    editor.putString(Fun.uaStationDomain(), current_station.getSubdomain_name());
                    editor.putString(Fun.uaStationPort(), current_station.getPort());
                    editor.apply();

                    tv_station.setText(current_station.getStation_name());
                }
            });
            ss.showSpinerDialog();
        } else {
            stationServerRequest(LoginActivity.this);
        }

    }
}
