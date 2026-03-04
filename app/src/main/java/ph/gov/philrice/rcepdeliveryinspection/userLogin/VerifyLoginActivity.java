package ph.gov.philrice.rcepdeliveryinspection.userLogin;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.zxing.client.android.Intents;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import at.favre.lib.crypto.bcrypt.BCrypt;
import ph.gov.philrice.rcepdeliveryinspection.Fun;
import ph.gov.philrice.rcepdeliveryinspection.dbase.database.RCEPDatabase;
import ph.gov.philrice.rcepdeliveryinspection.delivery.DeliveryMainActivity;
import ph.gov.philrice.rcepdeliveryinspection.inspection.InspectionMainActivity;
import ph.gov.philrice.rcepdeliveryinspection.R;

public class VerifyLoginActivity extends AppCompatActivity {
    private static final String TAG = "VerifyLoginActivity";
    //Views
    TextInputLayout til_password;
    private EditText password;
    private TextView tv_name, tv_login, tv_coopPassHint, tv_welcome, tv_season, tv_version,
            tv_station;
    private ProgressDialog progressDialog;
    //Variables
    SharedPreferences prefUserAccount;
    RCEPDatabase rcepDatabase;
    String glbl_season;
    ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //initialize screen
        Fun.fullScreen(this);//fullScreen
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
//        getSupportActionBar().hide(); // hide the title bar
        //setScreenActivityContents
        setContentView(R.layout.activity_verify_login);

        mProgressDialog = new ProgressDialog(this);
        rcepDatabase = RCEPDatabase.getAppDatabase(this);
        prefUserAccount = getApplicationContext()
                .getSharedPreferences(Fun.userAccountPreference(), Context.MODE_PRIVATE);
        progressDialog = new ProgressDialog(this);
        password = findViewById(R.id.password);
        tv_login = findViewById(R.id.tv_login);
        tv_version = findViewById(R.id.tv_version);
        tv_station = findViewById(R.id.tv_station);
        tv_name = findViewById(R.id.tv_name);
        tv_welcome = findViewById(R.id.tv_welcome);
        tv_season = findViewById(R.id.tv_season);
        tv_coopPassHint = findViewById(R.id.tv_coopPassHint);
        til_password = findViewById(R.id.til_password);
        glbl_season = prefUserAccount.getString(Fun.uaSeason(), "");

        String mrole = prefUserAccount.getString(Fun.uaUserRoles(), "");
        String welcomeMsg = "";

        switch (mrole) {
            case "seed-grower":
                welcomeMsg = getString(R.string.welcome);
                break;
            case "delivery-manager":
                welcomeMsg = getString(R.string.welcome) + " delivery manager,";
                break;
            case "seed-inspector":
                welcomeMsg = getString(R.string.welcome) + "  inspector,";
                break;

        }


        tv_welcome.setText(welcomeMsg);

        String name = prefUserAccount.getString(Fun.uaFirstName(), "") + " " +
                prefUserAccount.getString(Fun.uaLastName(), "");
        tv_name.setText(name);

        String mUserRoles = prefUserAccount.getString(Fun.uaUserRoles(), "");

        if (!mUserRoles.toLowerCase().equals("seed-inspector")) {
            tv_coopPassHint.setVisibility(View.VISIBLE);
            til_password.setHint("Accreditation No");
        }

        tv_season.setText("Server : " + glbl_season);

        tv_station.setText(prefUserAccount.getString(Fun.uaStationName(), ""));
        tv_version.setText(Fun.appVersion());


    }

    private void enableViews() {
        password.setEnabled(true);
        tv_login.setEnabled(true);
    }

    private void disableViews() {
        password.setEnabled(false);
        tv_login.setEnabled(false);
    }


    public void change_account(View view) {

        new MaterialAlertDialogBuilder(VerifyLoginActivity.this)
                .setTitle("RCEF DI")
                .setCancelable(false)
                .setMessage("Do you really want to change account?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (Fun.isCompletedCentralDataSending(VerifyLoginActivity.this) ==
                                0 /*|| Fun.isCompletedLocalDataSending(VerifyLoginActivity.this) == 0*/) {
                            dialogInterface.dismiss();
                            Toast.makeText(VerifyLoginActivity.this,
                                            "Unable to change account. Please complete sending data to central server",
                                            Toast.LENGTH_LONG)
                                    .show();
                        } else {
                            //all data were sent
                            dialogInterface.dismiss();
                            Toast.makeText(VerifyLoginActivity.this, "Changing account",
                                    Toast.LENGTH_SHORT).show();
                            //clear user preference data
                            prefUserAccount.edit().clear().apply();
                            //clear table delivery
                            clearData();

                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(VerifyLoginActivity.this,
                                            LoginActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    finish();
                                }
                            }, 2000);
                        }
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .show();


        /*CFAlertDialog.Builder builder = new CFAlertDialog.Builder(VerifyLoginActivity.this);
        builder.setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET);
        builder.setTitle("RCEP App");
        builder.setMessage("Do you really want to change account?");
        builder.addButton("CHANGE ACCOUNT", Color.parseColor("#FFFFFF"),
                Color.parseColor("#429ef4"), CFAlertDialog.CFAlertActionStyle.POSITIVE,
                CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (Fun.isCompletedCentralDataSending(VerifyLoginActivity.this) ==
                                0 *//*|| Fun.isCompletedLocalDataSending(VerifyLoginActivity.this) == 0*//*) {
                            dialog.dismiss();
                            //have existing data in local sending and central sending
                            CFAlertDialog.Builder builder =
                                    new CFAlertDialog.Builder(VerifyLoginActivity.this);
                            builder.setDialogStyle(CFAlertDialog.CFAlertStyle.ALERT);
                            builder.setMessage(
                                    "Please complete sending data to local server and central server before changing account");
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
                            builder.show();
                        } else {
                            //all data were sent
                            dialog.dismiss();
                            Toast.makeText(VerifyLoginActivity.this, "Changing account",
                                    Toast.LENGTH_SHORT).show();
                            //clear user preference data
                            prefUserAccount.edit().clear().apply();
                            //clear table delivery
                            clearData();

                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(VerifyLoginActivity.this,
                                            LoginActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    finish();
                                }
                            }, 2000);
                        }
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

    void clearData() {
        //clearing libraries
        rcepDatabase.libDropoffPointDAO().nukeTable();
        rcepDatabase.libSeedsDAO().nukeTable();
        //clearing delivery data
        rcepDatabase.tmpSgDeliveryTotalDAO().nukeTable();
        rcepDatabase.tblDeliveryDAO().nukeTable();
        rcepDatabase.tblCurrentDeliveryCountDAO().nukeTable();
        rcepDatabase.tblCommitmentDAO().nukeTable();
        rcepDatabase.tblTotalCommitmentDAO().nukeTable();
        //clearing delivery status
        rcepDatabase.tblDeliveryStatusDAO().nukeTable();
        //clearing inspection data
        rcepDatabase.tblInspectionDAO().nukeTable();
        rcepDatabase.tblActualDeliveryDAO().nukeTable();
        rcepDatabase.tblDeliveryInspectionDAO().nukeTable();
        rcepDatabase.tblDeliveryStatusAppLocalDAO().nukeTable();
        rcepDatabase.tmpDeliveryBatchDataDAO().nukeTable();
        rcepDatabase.tblTempSamplingDAO().nukeTable();
        rcepDatabase.tblSamplingDAO().nukeTable();


    }

    public void login(View view) {
        if (password.getText().toString().length() > 0) {
            if (!Fun.isTimeAutomatic(this)) {
                //automatic date and time is disabled
                //do function here
                new MaterialAlertDialogBuilder(VerifyLoginActivity.this)
                        .setTitle("RCEF DI")
                        .setCancelable(false)
                        .setMessage(
                                "Automatic date and time is not set. Please \"Go to settings\" to enable and proceed.")
                        .setPositiveButton("Go to settings", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                startActivity(
                                        new Intent(android.provider.Settings.ACTION_DATE_SETTINGS));
                            }
                        })
                        .show();

                /*CFAlertDialog.Builder builder = new CFAlertDialog.Builder(VerifyLoginActivity.this);
                builder.setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET);
                builder.setTitle("RCEP App");
                builder.setCancelable(false);
                builder.setMessage(
                        "Automatic date and time is not set. Please \"Go to settings\" to enable and proceed.");
                builder.addButton("Go to settings", Color.parseColor("#FFFFFF"),
                        Color.parseColor("#429ef4"), CFAlertDialog.CFAlertActionStyle.POSITIVE,
                        CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                startActivity(
                                        new Intent(android.provider.Settings.ACTION_DATE_SETTINGS));
                            }
                        });
                builder.show();*/
            } else {
                //set preference
                SharedPreferences.Editor editor = prefUserAccount.edit();
                editor.putString(Fun.uaDynamicIP(), "192.168.137.1");
                editor.apply();

                disableViews();
                String userInputPass = password.getText().toString();
                String mPassword = prefUserAccount.getString(Fun.uaPassword(), "");
                String mCoopAccreditation =
                        prefUserAccount.getString(Fun.uaCoopAccreditation(), "");
                //decoding accreditation
                final String[] accreditation = mCoopAccreditation.split("-");
                Log.e(TAG, "login: " + mCoopAccreditation);
                /* for (int x = 0; x < accreditation.length; x++) {
                    Log.e(TAG, "accreditation[" + x + "]->" +accreditation[x]);
                }*/
                //decoding user roles
                String mUserRoles = prefUserAccount.getString(Fun.uaUserRoles(), "");

                switch (mUserRoles) {
                    case "seed-inspector":
                        BCrypt.Result result =
                                BCrypt.verifyer().verify(userInputPass.toCharArray(), mPassword);
                        if (result.verified) {
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    //proceed to inspection
                                    enableViews();
                                    Intent intent = new Intent(VerifyLoginActivity.this,
                                            InspectionMainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    finish();
                                    Toast.makeText(VerifyLoginActivity.this,
                                            "Welcome Delivery Inspector", Toast.LENGTH_LONG).show();
                                }
                            }, 3000);
                        } else {
                            enableViews();
                            Toast.makeText(this, "Invalid password. Please try again",
                                    Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case "seed-grower":
                    case "delivery-manager":
                        //seed grower || cooperatives login
                        if (userInputPass.equals(accreditation[4])) {
                            //proceed to delivery
                            enableViews();
                            Intent intent = new Intent(VerifyLoginActivity.this,
                                    DeliveryMainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                            Toast.makeText(VerifyLoginActivity.this, "Welcome Seed Grower",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            enableViews();
                            Toast.makeText(this, "Invalid accreditation number. Please try again",
                                    Toast.LENGTH_SHORT).show();
                        }
                        break;
                }

               /* final String[] roles = mUserRoles.split(">>");
                //verify individual roles
                if (Arrays.asList(roles).contains("seed-inspector")) {
                    //enableViews();
                    BCrypt.Result result = BCrypt.verifyer().verify(userInputPass.toCharArray(), mPassword);
                    if (result.verified) {
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //proceed to inspection
                                enableViews();
                                Intent intent = new Intent(VerifyLoginActivity.this, InspectionMainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                                Toast.makeText(VerifyLoginActivity.this, "Welcome Delivery Inspector", Toast.LENGTH_LONG).show();
                            }
                        }, 3000);
                    } else {
                        enableViews();
                        Toast.makeText(this, "Invalid password. Please try again", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    //seed grower || cooperatives login
                    if (userInputPass.equals(accreditation[4])) {
                        //proceed to delivery
                        enableViews();
                        Intent intent = new Intent(VerifyLoginActivity.this, DeliveryMainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                        Toast.makeText(VerifyLoginActivity.this, "Welcome Seed Grower", Toast.LENGTH_SHORT).show();
                    } else {
                        enableViews();
                        Toast.makeText(this, "Invalid accreditation number. Please try again", Toast.LENGTH_SHORT).show();
                    }
                }*/
            }
        } else {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onBackPressed() {

        new MaterialAlertDialogBuilder(VerifyLoginActivity.this)
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

       /* CFAlertDialog.Builder builder = new CFAlertDialog.Builder(VerifyLoginActivity.this);
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
        Fun.hideKeyboard(this);
        password.findViewById(R.id.password).clearFocus();
    }


}
