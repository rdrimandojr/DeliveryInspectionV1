package ph.gov.philrice.rcepdeliveryinspection;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Scroller;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ph.gov.philrice.rcepdeliveryinspection.dbase.database.RCEPDatabase;
import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TblActualDelivery;
import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TblDeliveryStatusAppLocal;
import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TblInspection;
import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TblSampling;
import ph.gov.philrice.rcepdeliveryinspection.delivery.dataview.DeliveryBatchData;
import ph.gov.philrice.rcepdeliveryinspection.inspection.dataview.ActualDeliveryData;
import ph.gov.philrice.rcepdeliveryinspection.inspection.dataview.SamplingData;
import ph.gov.philrice.rcepdeliveryinspection.inspection.dataview.SamplingNewData;

public class Fun {
    public static AlertDialog showDialog(Context ctx, String title, String msg,
                                         String positiveLabel,
                                         DialogInterface.OnClickListener positiveOnClick,
                                         String negativeLabel,
                                         DialogInterface.OnClickListener negativeOnClick,
                                         boolean isCancelable) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle(title);
        builder.setCancelable(isCancelable);
        builder.setMessage(msg);
        builder.setPositiveButton(positiveLabel, positiveOnClick);
        builder.setNegativeButton(negativeLabel, negativeOnClick);

        AlertDialog alert = builder.create();
        alert.show();
        return alert;
    }

    public static String getDynamicIP(SharedPreferences preferences, Context ctx) {
        preferences = ctx.getSharedPreferences(Fun.userAccountPreference(), Context.MODE_PRIVATE);
        return preferences.getString(Fun.uaDynamicIP(), "");
    }

    public static double roundAvoid(double value, int places) {
        double scale = Math.pow(10, places);
        return Math.round(value * scale) / scale;
    }

    public static double toTwoDecimal(double d) {
        /*return ((long)(d * 1e2)) / 1e2;
        //Long typecast will remove the decimals*/
        DecimalFormat df = new DecimalFormat("#.000");
        String num = df.format(d);
        num = num.substring(0, num.length() - 1);    // 1.47

      /*  String s = "" + 234.12413;
        String[] result = s.split("\\.");*/


        return Double.parseDouble(num);
    }

    public static String appVersion() {
//        return "v2.8a";
        return BuildConfig.VERSION_NAME;
    }

    public static int isCoopVersion() {
        //1 = coop mode ; 0 = philrice mode

        //1 date is fixed
        //0 date can be inputted
        return 1;
    }

    public static String season() {
        return "rcef_ds2021";
    }

    public static String scriptPath() {
        return "rcep_delivery_inspection";
    }

    public static String server() {
//        return "http://192.168.106.58/";
        return "https://rcef-seed.philrice.gov.ph/";
    }

    public static String getAddress(String season) {
        //central server address
        return server() + Fun.scriptPath() + "/" + season + "/" +
                Fun.appVersion();
//http://dbmp2.philrice.gov.ph/
//        return "https://rcef-seed.philrice.gov.ph/";
    }

   /* public static String apiLinkSeasonStatic() {
        return Fun.ipAddress() + Fun.scriptPath() + "/season_request.php";
    }*/

    //for image uploading inspection url
    public static String uploadURL(String dynamicIP, int uploadMode) {
        String mUploadURL;
        //dynamicIP = "192.168.102.82";
        //server
        dynamicIP = "192.168.33.10";
        if (uploadMode == 0) {
            //local ip address
            mUploadURL = "http://" + dynamicIP + "/" + Fun.scriptPath() + "/" + "uploadVolley.php";
        } else {
            //image upload sending central server
            String centralServer = "rcef-seed.philrice.gov.ph";
            mUploadURL =
                    "https://" + centralServer + "/" + Fun.scriptPath() + "/" + "uploadVolley.php";

            //img upload sending testing
           /* String testServer = "192.168.137.1";
            mUploadURL = "http://" + testServer + "/" + Fun.scriptPath() + "/" + "uploadVolley.php";*/
        }
        return mUploadURL;
    }

    public static String localDeploymentAddress(String dynamicIP) {
        //bahay
//        dynamicIP = "192.168.102.82";
        //server
        dynamicIP = "192.168.33.10";
        return "http://" + dynamicIP + "/" + Fun.scriptPath() + "/" + Fun.season() + "/" +
                Fun.appVersion();
        //return "http://192.168.102.82/" + Fun.scriptPath() + "/" + Fun.appVersion();
    }

    public static String apiLinkSeasonStatic() {
        //local testing
//        return "http://192.168.137.1/" + Fun.scriptPath() + "/season_request.php";
        //deployment
        return server() + Fun.scriptPath() + "/season_request_new.php";
        //return "http://dbmp2.philrice.gov.ph/" + Fun.scriptPath() + "/season_request.php";


    }

    public static String getDir() {
        String dir = "RCEP Inspection";

        return dir;
    }

    public static int isQRSeriesCorrect(String qrCode) {
        int res = 0;
//        Pattern a = Pattern.compile("^S\\d{3}-\\d{2}-\\d{5}-\\d{3}");
        Pattern a = Pattern.compile("^S\\d{3}-\\d{2}-\\d{6}"); //s221-03-000001
        Matcher aa = a.matcher(qrCode);
        if (aa.matches()) {
            res = 1;
        }

        return res;
    }

    public static void showLongMsgDialog(Context ctx, String title, String message) {
        AlertDialog dialog = new AlertDialog.Builder(ctx)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("DONE", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
        TextView textView = (TextView) dialog.findViewById(android.R.id.message);
        textView.setScroller(new Scroller(ctx));
        textView.setVerticalScrollBarEnabled(true);
        textView.setMovementMethod(new ScrollingMovementMethod());
        textView.setTextSize(14);
    }


    public static double reqSamplingAverage() {
        return 20.0;
    }

    public static void progressStart(ProgressDialog mProgressDialog, String title, String message) {
        mProgressDialog.setTitle(title);
        mProgressDialog.setMessage(message);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    public static void progressMessage(ProgressDialog mProgressDialog, String message) {
        mProgressDialog.setMessage(message);
    }

    public static void progressMessageAndTitle(ProgressDialog mProgressDialog, String title,
                                               String message) {
        mProgressDialog.setTitle(title);
        mProgressDialog.setMessage(message);
    }

    public static void progressStop(ProgressDialog mProgressDialog) {
        mProgressDialog.dismiss();
    }

    public static String getCurrentYear() {
        return String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
    }

    //Account preference
    public static String userAccountPreference() {
        return "user_account";
    }

    public static String uaUserId() {
        return "userId";
    }

    public static String uaFirstName() {
        return "firstName";
    }

    public static String uaMidName() {
        return "middleName";
    }

    public static String uaLastName() {
        return "lastName";
    }

    public static String uaPassword() {
        return "password";
    }

    public static String uaUserRoles() {
        return "userRoles";
    }

    public static String uaCoopAccreditation() {
        return "coopAccreditation";
    }

    public static String uaCoopCurrentMOA() {
        return "current_moa";
    }

    public static String uaCoopAcronym() {
        return "coop_acronym";
    }

    public static String uaDynamicIP() {
        return "dynamic_ip";
    }

    public static String uaSeason() {
        return "season";
    }

    public static String uaStationName() {
        return "station_name";
    }

    public static String uaStationExternalIP() {
        return "station_external_ip";
    }

    public static String uaStationDomain() {
        return "station_domain";
    }

    public static String uaStationPort() {
        return "station_port";
    }


    //Delivery preference
    public static String deliveryPreference() {
        return "delivery_details";
    }

    public static String delTicketNumber() {
        return "ticketNumber";
    }

    public static String delSeedTag() {
        return "seedTag";
    }

    public static String delVariety() {
        return "seedVariety";
    }

    public static String delTotalWeight() {
        return "totalWeight";
    }

    public static String delWeightPerBag() {
        return "weightPerBag";
    }

    public static String delActualNumBagsDelivered() {
        return "actualNumbBagsDelivered";
    }

    //tempInspectionData
    public static String tempInspectionPreference() {
        return "temp_inspection_data";
    }

    public static String tiBatchTicketNumber() {
        return "batchTicketNumber";
    }

    public static String tiScreeningPassed() {
        return "screeningPassed";
    }

    public static String tiScreeningRemarks() {
        return "screeningRemarks";
    }

    public static String tiVisualPassed() {
        return "visualPassed";
    }

    public static String tiVisualFindings() {
        return "visualFindings";
    }

    public static String tiVisualRemarks() {
        return "visualRemarks";
    }

    public static String tiVisualInspectionImage() {
        return "visualInspectionImage";
    }

    public static String tiSamplingPassed() {
        return "samplingPassed";
    }

    public static String tiSamplingImage() {
        return "samplingImage";
    }

    public static String tiSamplingImagePath() {
        return "samplingImagePath";
    }

    public static String tiBatchDeliveryImage() {
        return "batchDeliveryImage";
    }

    public static String tiBatchDeliveryImagePath() {
        return "batchDeliveryImagePath";
    }

    public static String tiDateInspected() {
        return "dateInspected";
    }

    public static String tiDateCreated() {
        return "dateCreated";
    }

    public static String tiSend() {
        return "send";
    }

    public static String tiTempSampling() {
        return "tempSampling";
    }

    public static String tiTempActualDelivery() {
        return "tempActualDelivery";
    }

    public static String tiTempDeliveryStatus() {
        return "tempDeliveryStatus";
    }

    public static String tiPrvDropoffId() {
        return "tempPrvDropoffId";
    }

    public static String tiPrv() {
        return "tempPrv";
    }

    public static String tiMoaNumber() {
        return "tempMoaNumber";
    }

    public static String tiBatchSeries() {
        return "tempBatchSeries";
    }

    public static String tiIsBuffer() {
        return "isBuffer";
    }

    public static String tiForEbinhi() {
        //yes = 1 || no = 2
        return "isEbinhi";
    }

    public static String tiAccountingImage() {
        return "tempAccountingImage";
    }

    public static String tiAccountingImagePath() {
        return "tempAccountingImagePath";
    }

    public static String tiDrDate() {
        return "tempDrDate";
    }

    public static String tiDrNumber() {
        return "tempDrNumber";
    }

    //Threshold preference
    public static String thresholdPreference() {
        return "threshold";
    }

    public static String thresId() {
        return "thresholdId";
    }

    public static String thresName() {
        return "thresholdName";
    }

    public static String thresVal() {
        return "thresholdVal";
    }

    public static String thresSamplingPercentage() {
        return "samplingPercentage";
    }

    //temporary samplingData holder
    public static String tempSamplingData() {
        return "temp_sampling_data";
    }

    public static String tmpSamplingData() {
        return "tmpSamplingData";
    }

    public static String updateStationDB(String address, String port, String table_name) {
        //http://192.168.11.29:8080/rdmui/api/artisan/db/rcep_delivery_inspection
        return "http://" + address + ":" + port + "/rdmui/api/artisan/db/" + table_name;
    }

    //for updating stations db
/*
    private void updateStationDB(final Context ctx, String address, String table_name) {
        final String request_url = Fun.updateStationDB(address,table_name);
        Log.e(TAG, "updateStationDB: " + request_url);
        //prepare the Request
        StringRequest stringRequest = new StringRequest(Request.Method.GET, request_url, response -> {
            response = response.trim();
            Log.e(TAG, "request_url: " + response);

        }, error -> {
            //srl_refreshList.setRefreshing(false);
            //Toast.makeText(ctx, "Server unreachable. Please check network connectivity and try again.", Toast.LENGTH_SHORT).show();
        })*/
/* {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("prv_dropoff_id", mprv_dropoff_id);
                params.put("accreditation_no", maccreditation_no);
                params.put("moa_number", mmoa_number);
                return params;
            }
        }*//*
;
        RequestQueue requestQueue = Volley.newRequestQueue(ctx);
        requestQueue.add(stringRequest);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                -1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }
*/


    public static boolean isTimeAutomatic(Context c) /*returns true or false value*/ {
        return Settings.Global.getInt(c.getContentResolver(), Settings.Global.AUTO_TIME, 0) == 1;
    }

    public static String getCurrentDate() {
        //creating Calendar instance
        Calendar cal = Calendar.getInstance();
        //Returns current date and time
        String mDate = DateFormat.format("yyyy-MM-dd HH:mm:ss", cal).toString();
        return mDate;
    }

    public static String getCurrentDateYMD() {
        //creating Calendar instance
        Calendar cal = Calendar.getInstance();
        //Returns current date and time
        String mDate = DateFormat.format("yyyy-MM-dd", cal).toString();
        return mDate;
    }

    public static void fullScreen(Activity activity) {
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //enable full screen
    }

    public static int isEqualOrBeyond(String fromServer, String fromMobile) {
        int res = 0;

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date dfromServer = null;
        Date dfromMobile = null;
        try {
            dfromServer = format.parse(fromServer);
            dfromMobile = format.parse(fromMobile);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (dfromServer.equals(dfromMobile) || dfromServer.after(dfromMobile)) {
            res = 1;
        }

        return res;
    }

    public static boolean isLocationEnabled(Context ctx) {
        LocationManager locationManager =
                (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static void hideKeyboard(Activity activity) {
        View view = activity.findViewById(android.R.id.content);
        if (view != null) {
            InputMethodManager imm =
                    (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static String apiLinkStationServers() {
        return "https://isd.philrice.gov.ph/rcef_api/api/lib_servers";
    }

    public static String getTimestamp() {
        //creating Calendar instance
        Calendar cal = Calendar.getInstance();
        //Returns current time in millis
        int dateInSeconds = (int) ((cal.getTimeInMillis() +
                cal.getTimeZone().getOffset(cal.getTimeInMillis())) / 1000);
        return String.valueOf(dateInSeconds);
    }

    public static int checkVariety(Context ctx, String variety) {
        int res = 0;
        RCEPDatabase rcepDatabase = RCEPDatabase.getAppDatabase(ctx);
        if (rcepDatabase.libSeedsDAO().checkVariety(variety) > 0) {
            res = 1;
        }
        return res;
    }

    public static int checkDropoffPoint(Context ctx, int dropoffPointId) {
        int res = 0;
        RCEPDatabase rcepDatabase = RCEPDatabase.getAppDatabase(ctx);
        if (rcepDatabase.libDropoffPointDAO().checkDropoffPoint(dropoffPointId) > 0) {
            res = 1;
        }
        return res;
    }

    public static int checkDeliveryStatus(Context ctx, int deliveryStatusId) {
        int res = 0;
        RCEPDatabase rcepDatabase = RCEPDatabase.getAppDatabase(ctx);
        if (rcepDatabase.tblDeliveryStatusDAO().checkDeliveryStatus(deliveryStatusId) > 0) {
            res = 1;
        }
        return res;
    }


    public static int checkDelivery(Context ctx, int deliveryId) {
        int res = 0;
        RCEPDatabase rcepDatabase = RCEPDatabase.getAppDatabase(ctx);
        if (rcepDatabase.tblDeliveryDAO().checkDelivery(deliveryId) > 0) {
            res = 1;
        }
        return res;
    }

    public static int checkDeliveryInspection(Context ctx, int deliveryId) {
        int res = 0;
        RCEPDatabase rcepDatabase = RCEPDatabase.getAppDatabase(ctx);
        if (rcepDatabase.tblDeliveryInspectionDAO().isExisting(deliveryId) > 0) {
            res = 1;
        }
        return res;
    }

    /*public static int hasInpsection(Context ctx, String ticketNumber) {
        int res = 0;
        RCEPDatabase rcepDatabase = RCEPDatabase.getAppDatabase(ctx);
        if (rcepDatabase.tblInspectionDAO().checkInspection(ticketNumber) > 0) {
            res = 1;
        }
        return res;
    }*/


    public static int completedBatchLocal(Context ctx, String batchTicketNumber) {
        //returns 1 if batch data were completely sent in local server
        int res = 1;
        RCEPDatabase rcepDatabase = RCEPDatabase.getAppDatabase(ctx);

        if (rcepDatabase.tblInspectionDAO().sendLocal(batchTicketNumber) > 0 ||
                rcepDatabase.tblSamplingDAO().sendLocal(batchTicketNumber) > 0 ||
                rcepDatabase.tblActualDeliveryDAO().sendLocal(batchTicketNumber) > 0) {
            res = 0;
        }
        return res;
    }

    public static int completedBatchCentral(Context ctx, String batchTicketNumber) {
        //returns 1 if batch data were completely sent in central server
        int res = 1;
        RCEPDatabase rcepDatabase = RCEPDatabase.getAppDatabase(ctx);

        if (rcepDatabase.tblInspectionDAO().sendCentral(batchTicketNumber) > 0 ||
                rcepDatabase.tblSamplingDAO().sendCentral(batchTicketNumber) > 0 ||
                rcepDatabase.tblActualDeliveryDAO().sendCentral(batchTicketNumber) > 0) {
            res = 0;
        }
        return res;
    }

    public static int isCompletedLocalDataSending(Context ctx) {
        //returns 1 if all data were completely sent in local server
        int res = 1;
        RCEPDatabase rcepDatabase = RCEPDatabase.getAppDatabase(ctx);

        if (rcepDatabase.tblInspectionDAO().sendLocalAll() > 0 ||
                rcepDatabase.tblSamplingDAO().sendLocalAll() > 0 ||
                rcepDatabase.tblActualDeliveryDAO().sendLocalAll() > 0) {
            res = 0;
        }
        return res;
    }

    public static int isCompletedCentralDataSending(Context ctx) {
        //returns 1 if all data were completely sent in central server
        int res = 1;
        RCEPDatabase rcepDatabase = RCEPDatabase.getAppDatabase(ctx);

        if (rcepDatabase.tblInspectionDAO().sendCentralAll() > 0 ||
                rcepDatabase.tblSamplingDAO().sendCentralAll() > 0 ||
                rcepDatabase.tblActualDeliveryDAO().sendCentralAll() > 0) {
            res = 0;
        }
        return res;
    }

    public static int getBalance(int limit, int currentTotal) {
        return limit - currentTotal;
    }


    public static int isDownloaded(Context ctx, String batchTicketNumber) {
        RCEPDatabase rcepDatabase = RCEPDatabase.getAppDatabase(ctx);
        return rcepDatabase.tblActualDeliveryDAO().isDownloaded(batchTicketNumber);
    }

    public static int hasLocalStatus(Context ctx, String batchTicketNumber) {
        RCEPDatabase rcepDatabase = RCEPDatabase.getAppDatabase(ctx);
        return rcepDatabase.tblDeliveryStatusAppLocalDAO().hasLocalStatus(batchTicketNumber);
    }


    public static int getLocalStatus(Context ctx, String batchTicketNumber) {
        RCEPDatabase rcepDatabase = RCEPDatabase.getAppDatabase(ctx);
        return rcepDatabase.tblDeliveryStatusAppLocalDAO().getDeliveryStatus(batchTicketNumber);
    }


    public static int hasLocalInspection(Context ctx, String batchTicketNumber) {
        RCEPDatabase rcepDatabase = RCEPDatabase.getAppDatabase(ctx);
        return rcepDatabase.tblDeliveryStatusAppLocalDAO().getDeliveryStatus(batchTicketNumber);
    }

    public static int localSendingStatus(Context ctx, String batchTicketNumber) {
        RCEPDatabase rcepDatabase = RCEPDatabase.getAppDatabase(ctx);
        return rcepDatabase.tblSendingStatusDAO().getSendingStatus(batchTicketNumber);
    }


    public static String currentYr() {
        Calendar now = Calendar.getInstance();
        int yr = now.get(Calendar.YEAR);
        return String.valueOf(yr);
    }

    public static String formatDate(String dateToParse) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date newDate = null;
        try {
            newDate = format.parse(dateToParse);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        format = new SimpleDateFormat("E, MMM dd yyyy hh a", Locale.getDefault());
        String date = format.format(newDate);

        return date;
    }

    public static String allBatchTickets(List<String> source) {
        String data = "";

        if (source.size() > 0) {
            for (String s : source) {
                data += "'" + s + "',";
            }
            data = removeLastChar(data);
        }
        return data;
    }

    public static String csvArray(List<String> source) {
        String data = "";

        if (source.size() > 0) {
            for (String s : source) {
                data += s + ",";
            }
            data = removeLastChar(data);
            data = data.trim();
        }
        return data;
    }


    public static String removeLastChar(String s) {
        if (s == null || s.length() == 0) {
            return s;
        }
        return s.substring(0, s.length() - 1);
    }

    public static String jsonSamplingData(List<SamplingData> samplingData) {
        JSONArray jsonArray = new JSONArray();
        for (SamplingData s : samplingData) {
            JSONObject object = new JSONObject();
            try {
                object.put("ticketNumber", s.getTicketNumber());
                object.put("bagWeight", s.getBagWeight());
                object.put("bagNumber", s.getBagNumber());
                object.put("bagSequenceNumber", s.getBagSequenceNumber());
                object.put("timeStamp", s.getTimeStamp());

            } catch (JSONException e) {
                e.printStackTrace();
            }
            jsonArray.put(object);
        }
        return jsonArray.toString();
    }

    public static String jsonDeliveryBatchData(List<DeliveryBatchData> deliveryBatchData) {
        JSONArray jsonArray = new JSONArray();
        for (DeliveryBatchData d : deliveryBatchData) {
            JSONObject object = new JSONObject();
            try {
                object.put("ticketNumber", d.getTicketNumber());
                object.put("batchTicketNumber", d.getBatchTicketNumber());
                object.put("deliveryDate", d.getDeliveryDate());
                object.put("coopAccreditation", d.getCoopAccreditation());
                object.put("seedTag", d.getSeedTag());
                object.put("seedVariety", d.getSeedVariety());
                object.put("seedClass", d.getSeedClass());
                object.put("totalBagCount", d.getTotalBagCount());
                object.put("userId", d.getUserId());
                object.put("dateCreated", d.getDateCreated());
                object.put("dropOffPoint", d.getDropOffPoint());
                object.put("region", d.getRegion());
                object.put("province", d.getProvince());
                object.put("municipality", d.getMunicipality());
                object.put("prv_dropoff_id", d.getPrv_dropoff_id());
                object.put("prv", d.getPrv());
                object.put("moa_number", d.getMoa_number());
                object.put("app_version", d.getApp_version());
                object.put("batchSeries", d.getBatchSeries());
                object.put("sg_id", d.getSg_id());
                object.put("isBuffer", d.getMisBuffer());
                object.put("transpo_cost_per_bag", d.getTranspo_cost_per_bag());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jsonArray.put(object);
        }
        return jsonArray.toString();
    }

    public static String jsonTempActualDelivery(List<ActualDeliveryData> actualDeliveryData) {
        JSONArray jsonArray = new JSONArray();
        for (ActualDeliveryData a : actualDeliveryData) {
            JSONObject object = new JSONObject();
            try {
                object.put("batchTicketNumber", a.getBatchTicketNumber());
                object.put("seedVariety", a.getSeedVariety());
                object.put("totalBagCount", a.getTotalBagCount());
                object.put("dateCreated", a.getDateCreated());
                object.put("send", a.getSend());

            } catch (JSONException e) {
                e.printStackTrace();
            }
            jsonArray.put(object);
        }
        return jsonArray.toString();
    }

    public static String jsonTempSamplingNewData(List<SamplingNewData> samplingNewData) {
        JSONArray jsonArray = new JSONArray();
        for (SamplingNewData s : samplingNewData) {
            JSONObject object = new JSONObject();
            try {
                object.put("batchTicketNumber", s.getBatchTicketNumber());
                object.put("seedTag", s.getSeedTag());
                object.put("bagWeight", s.getBagWeight());
                object.put("dateSampled", s.getDateSampled());
                object.put("send", s.getSend());

            } catch (JSONException e) {
                e.printStackTrace();
            }
            jsonArray.put(object);
        }
        return jsonArray.toString();
    }

    public static String jsonTblSampling(List<TblSampling> data) {
        String glbl_result = "";
        if (data.size() > 0) {
            JSONArray jsonArray = new JSONArray();
            for (TblSampling s : data) {
                JSONObject object = new JSONObject();
                try {
                    object.put("batchTicketNumber", s.getBatchTicketNumber());
                    object.put("seedTag", s.getSeedTag());
                    object.put("bagWeight", s.getBagWeight());
                    object.put("dateSampled", s.getDateSampled());
                    object.put("send", s.getSend());
                    object.put("prv", s.getPrv());
                    object.put("moa_number", s.getMoa_number());
                    object.put("app_version", s.getApp_version());
                    object.put("batchSeries", s.getBatchSeries());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                jsonArray.put(object);
            }
            glbl_result = jsonArray.toString();
        }
        return glbl_result;

    }

    public static String jsonTblInspection(List<TblInspection> data) {

        String glbl_result = "";
        if (data.size() > 0) {
            JSONArray jsonArray = new JSONArray();
            for (TblInspection s : data) {
                JSONObject object = new JSONObject();
                try {
                    object.put("batchTicketNumber", s.getBatchTicketNumber());
                    object.put("screeningPassed", s.getScreeningPassed());
                    object.put("screeningRemarks", s.getScreeningRemarks());
                    object.put("visualPassed", s.getVisualPassed());
                    object.put("visualFindings", s.getVisualFindings());
                    object.put("visualRemarks", s.getVisualRemarks());
                    object.put("visualInspectionImage", s.getVisualInspectionImage());
                    object.put("samplingPassed", s.getSamplingPassed());
                    object.put("samplingImage", s.getSamplingImage());
                    object.put("batchDeliveryImage", s.getBatchDeliveryImage());
                    object.put("dateInspected", s.getDateInspected());
                    object.put("dateCreated", s.getDateCreated());
                    object.put("send", s.getSend());
                    object.put("prv", s.getPrv());
                    object.put("moa_number", s.getMoa_number());
                    object.put("app_version", s.getApp_version());
                    object.put("batchSeries", s.getBatchSeries());

                    object.put("accountingImage", s.getAccountingImage());
                    object.put("dr_date", s.getDr_date());
                    object.put("dr_number", s.getDr_number());
                    object.put("actual_inspection_date", s.getActual_inspection_date());
                    object.put("actual_delivery_date", s.getActual_delivery_date());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                jsonArray.put(object);
            }
            glbl_result = jsonArray.toString();
        }

        return glbl_result;


    }

    /*public static String prepareInspectionData(String batchTicketNumber, RCEPDatabase db) {
        String glbl_result = "";

        List<TblDeliveryStatusAppLocal> dataStatus = new ArrayList<>();
        List<TblInspection> dataInspection = new ArrayList<>();
        List<TblSampling> dataSampling = new ArrayList<>();
        List<TblActualDelivery> dataActualDelivery = new ArrayList<>();

        *//*dataStatus =
                db.tblDeliveryStatusAppLocalDAO().getLocalDeliveryStatusByBatch(batchTicketNumber);
        dataInspection = db.tblInspectionDAO()
        dataSampling = db.tblSamplingDAO().getSamplingByBatch(batchTicketNumber);*//*


        //prepare status


        return glbl_result;

    }*/


    public static String prepareInspectionData(String batchTicketNumber, RCEPDatabase db) {
        //List<TblDeliveryStatusAppLocal> dataStatus = new ArrayList<>();
        TblDeliveryStatusAppLocal dataStatus;
        //List<TblInspection> dataInspection = new ArrayList<>();
        TblInspection dataInspection;
        List<TblSampling> dataSampling = new ArrayList<>();
        List<TblActualDelivery> dataActualDelivery = new ArrayList<>();

        // Fetch and populate the lists from the database (db)
        dataStatus =
                db.tblDeliveryStatusAppLocalDAO().getLocalDeliveryStatusByBatch2(batchTicketNumber);
        dataInspection = db.tblInspectionDAO().getInspectionByBatch2(batchTicketNumber);
        dataSampling = db.tblSamplingDAO().getSamplingByBatch(batchTicketNumber);
        dataActualDelivery = db.tblActualDeliveryDAO().getActualDeliveryByBatch(batchTicketNumber);

       /* Bitmap bitmap;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();*/


        // Initialize Gson
        Gson gson = new Gson();

        // Serialize lists to JSON
        String jsonStatus = gson.toJson(dataStatus);
        String jsonInspection = gson.toJson(dataInspection);
        String jsonSampling = gson.toJson(dataSampling);
        String jsonActualDelivery = gson.toJson(dataActualDelivery);

        // Parse the JSON strings into JsonElement
        JsonElement jsonElementStatus = JsonParser.parseString(jsonStatus);
        JsonElement jsonElementInspection = JsonParser.parseString(jsonInspection);
        JsonElement jsonElementSampling = JsonParser.parseString(jsonSampling);
        JsonElement jsonElementActualDelivery = JsonParser.parseString(jsonActualDelivery);

        // Create a JSON object and add named arrays
        JsonObject resultJson = new JsonObject();
        resultJson.add("deliveryStatus", jsonElementStatus);
        resultJson.add("inspection", jsonElementInspection);
        resultJson.add("sampling", jsonElementSampling);
        resultJson.add("actualDelivery", jsonElementActualDelivery);

       /* bitmap = BitmapFactory.decodeFile(dataInspection.getSamplingImage_path());
        bitmap.compress(Bitmap.CompressFormat.JPEG, 35, byteArrayOutputStream);
        String imgSampling =
                Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);

        resultJson.addProperty("imgSamplingName", dataInspection.getSamplingImage());
        resultJson.addProperty("imgSampling", imgSampling);

        bitmap = BitmapFactory.decodeFile(dataInspection.getBatchDeliveryImage_path());
        bitmap.compress(Bitmap.CompressFormat.JPEG, 35, byteArrayOutputStream);
        String imgActual =
                Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);

        resultJson.addProperty("imgActualName", dataInspection.getBatchDeliveryImage());
        resultJson.addProperty("imgActual", imgActual);

        bitmap = BitmapFactory.decodeFile(dataInspection.getAccountingImage_path());
        bitmap.compress(Bitmap.CompressFormat.JPEG, 35, byteArrayOutputStream);
        String imgAttachment =
                Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);

        resultJson.addProperty("imgAttachmentName", dataInspection.getAccountingImage());
        resultJson.addProperty("imgAttachment", imgAttachment);*/

        return gson.toJson(resultJson);
    }


    public static String jsonTblDeliveryStatusAppLocalData(List<TblDeliveryStatusAppLocal> data) {
        String glbl_result = "";
        if (data.size() > 0) {
            JSONArray jsonArray = new JSONArray();
            for (TblDeliveryStatusAppLocal s : data) {
                JSONObject object = new JSONObject();
                try {
                    object.put("batchTicketNumber", s.getBatchTicketNumber());
                    object.put("status", s.getStatus());
                    object.put("dateCreated", s.getDateCreated());
                    object.put("send", 1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                jsonArray.put(object);
            }

            glbl_result = jsonArray.toString();
        }
        return glbl_result;
    }

    public static String jsonTblActualDelivery(List<TblActualDelivery> data) {
        String glbl_result = "";
        if (data.size() > 0) {
            JSONArray jsonArray = new JSONArray();
            for (TblActualDelivery s : data) {
                JSONObject object = new JSONObject();
                try {
                    object.put("batchTicketNumber", s.getBatchTicketNumber());
                    object.put("region", s.getRegion());
                    object.put("province", s.getProvince());
                    object.put("municipality", s.getMunicipality());
                    object.put("dropOffPoint", s.getDropOffPoint());
                    object.put("seedVariety", s.getSeedVariety());
                    object.put("totalBagCount", s.getTotalBagCount());
                    object.put("dateCreated", s.getDateCreated());
                    object.put("send", 1);
                    object.put("seedTag", s.getSeedTag());
                    object.put("prv_dropoff_id", s.getPrv_dropoff_id());
                    object.put("prv", s.getPrv());
                    object.put("moa_number", s.getMoa_number());
                    object.put("app_version", s.getApp_version());
                    object.put("batchSeries", s.getBatchSeries());
                    object.put("remarks", s.getRemarks());
                    object.put("isRejected", s.getIsRejected());
                    object.put("isDownloaded", s.getIsDownloaded());
                    object.put("hasRla", s.getHasRLA());
                    object.put("sack_code", s.getSack_code());
                    object.put("isBuffer", s.getMisBuffer());

                    object.put("qrValStart", s.getQRValStart());
                    object.put("qrValEnd", s.getQRValEnd());
                    object.put("qrStart", s.getQRStart());
                    object.put("qrEnd", s.getQREnd());

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                jsonArray.put(object);
            }
            glbl_result = jsonArray.toString();
        }
        return glbl_result;
    }

    public static void displayPopUpImage(Context ctx, Bitmap imageBitmap) {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(ctx);
        ImageView imageView = new ImageView(ctx);
        imageView.setImageBitmap(imageBitmap);
        builder.setView(imageView);
//        builder.setCancelable(true);

        builder.setPositiveButton("DONE", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // continue with delete
                //function that deletes image then clear text of button here!
                dialog.cancel();
            }
        });
        builder.create();
        builder.show();
    }

    public static void removeBatchDownload(Context ctx, String batchTicketNumber) {
        RCEPDatabase rcepDatabase = RCEPDatabase.getAppDatabase(ctx);
        //clear inspection
        rcepDatabase.tblInspectionDAO().removeBatchData(batchTicketNumber);
        //clear sampling
        rcepDatabase.tblSamplingDAO().removeBatchData(batchTicketNumber);
        //clear actual delivery
        rcepDatabase.tblActualDeliveryDAO().removeBatchData(batchTicketNumber);
        //clear status
        rcepDatabase.tblDeliveryStatusAppLocalDAO().removeBatchData(batchTicketNumber);
    }

    public static int isCostValid(String cost) {
        int res = 0;

        if (!cost.equals("") || !cost.equals(".")) {
            Pattern a = Pattern.compile("\\d+(\\.\\d{1,2})?");
            Matcher aa = a.matcher(cost);
            if (aa.matches()) {
                res = 1;
            }
        }

        return res;
    }


    //NOTES
    //accessing json without array name
    /*
    JSONArray json;
        try {
        json = new JSONArray(prefTempSampling.getString(Fun.tmpSamplingData(), ""));
        for (int i = 0; i < json.length(); i++) {
            JSONObject temp = json.getJSONObject(i);
            String mTicketNumber = temp.getString("ticketNumber");
            float mBagWeight = Float.parseFloat(temp.getString("bagWeight"));
            int mBagNumber = temp.getInt("bagNumber");
            String mBagSequenceNumber = temp.getString("bagSequenceNumber");
            String mTimeStamp = temp.getString("timeStamp");
        }
    } catch (JSONException e) {
        e.printStackTrace();
    }
    */

    //logging multiple lines
    /*
    int maxLogSize = 1000;
    for(int i = 0; i <=response.length()/maxLogSize;i++){
        int start = i * maxLogSize;
        int end = (i + 1) * maxLogSize;
        end = end > response.length() ? response.length() : end;
        Log.e(TAG, response.substring(start, end));
    }
    */

    public static void copyToClipboard(Context context, String text) {
        // Get the system clipboard
        ClipboardManager clipboard =
                (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

        // Create a new ClipData
        ClipData clip = ClipData.newPlainText("Copied Text", text);

        // Set the clipboard's primary clip
        clipboard.setPrimaryClip(clip);
    }
}
