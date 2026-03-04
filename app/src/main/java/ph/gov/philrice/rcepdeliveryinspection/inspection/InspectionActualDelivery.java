package ph.gov.philrice.rcepdeliveryinspection.inspection;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

//import com.amitshekhar.DebugDB;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.zxing.Result;
import com.google.zxing.client.android.Intents;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.kunzisoft.switchdatetime.SwitchDateTimeDialogFragment;
import com.ortiz.touchview.TouchImageView;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.CountDownLatch;

import in.galaxyofandroid.spinerdialog.OnSpinerItemClick;
import in.galaxyofandroid.spinerdialog.SpinnerDialog;
import ph.gov.philrice.rcepdeliveryinspection.BuildConfig;
import ph.gov.philrice.rcepdeliveryinspection.Fun;
import ph.gov.philrice.rcepdeliveryinspection.R;
import ph.gov.philrice.rcepdeliveryinspection.dbase.database.RCEPDatabase;
import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TblActualDelivery;
import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TblDeliveryInspection;
import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TblDeliveryStatusAppLocal;
import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TblInspection;
import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TblSampling;
import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TblTempSampling;
import ph.gov.philrice.rcepdeliveryinspection.inspection.dataview.ActualDeliveryAdapter;
import ph.gov.philrice.rcepdeliveryinspection.inspection.dataview.ActualDeliveryData;

public class InspectionActualDelivery extends AppCompatActivity
        implements ActualDeliveryAdapter.ItemClicked {
    private static final String TAG = "InspectionActualDeliver";
    //Views
    ProgressDialog mProgressDialog;
    RecyclerView rv_actualDelivery;
    TextView tv_batchTicketNumber;
    private EditText et_seedVariety, et_bagCount, et_actualDeliveryImage, et_lotNo, et_labNo,
            et_seedTag,
            et_remarks, et_qrSeries, et_sackCode, et_qrStart, et_qrEnd, et_actualDateofInspection,
            et_actualDateOfDelivery;
    LinearLayout ll_seedTagContainer, ll_otherTagContainer, ll_imgContainer, ll_qrSeriesContainer,
            ll_qrSeriesContainer2;
    ImageView iv_preview;
    CheckBox chkbox_reject;
    //private CodeScanner mCodeScanner;

    private TextInputLayout til_qstart, til_qend;

    //Variables
    LinearLayout actual_delivery_form;
    FrameLayout content_frame;
    SharedPreferences prefUserAccount;
    SharedPreferences prefTempInspectionData;
    ArrayList<ActualDeliveryData> actualDeliveryData;
    ActualDeliveryAdapter actualDeliveryAdapter;
    int glbl_start_value, glbl_end_value;

    RCEPDatabase rcepDatabase;
    String glbl_batchTicketNumber;
    String glbl_region;
    String glbl_province;
    String glbl_municipality;
    String glbl_dropOffPoint;

    String glbl_imgName;
    private File file;

    private int glbl_scan_mode;

    String glbl_imgName2;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    String currentPhotoPath;

    ImageView imgvw_capture;

    SwitchDateTimeDialogFragment dateTimeDialogFragment;

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher =
            registerForActivityResult(new ScanContract(),
                    result -> {
                        if (result.getContents() == null) {
                            Intent originalIntent = result.getOriginalIntent();
                            if (originalIntent == null) {
                                Toast.makeText(InspectionActualDelivery.this, "Cancelled",
                                        Toast.LENGTH_LONG).show();
                            } else if (originalIntent.hasExtra(
                                    Intents.Scan.MISSING_CAMERA_PERMISSION)) {
                                Toast.makeText(InspectionActualDelivery.this,
                                        "Cancelled due to missing camera permission, Please enable in settings.",
                                        Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Log.e(TAG, "data: " + result.getContents());
                            InspectionActualDelivery.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.e(TAG, "run: " + result.getContents());
                                    //et_qrSeries.setText("");
                                    Log.e(TAG, "glbl_scan_mode: " + glbl_scan_mode);
                                    switch (glbl_scan_mode) {
                                        case 3://start

                                            if (Fun.isQRSeriesCorrect(result.getContents()) == 1) {
                                                boolean exists = actualDeliveryData.stream()
                                                        .anyMatch(data -> result.getContents()
                                                                .equals(data.getQRValStart()));

                                                et_qrStart.setText("");
                                                et_qrEnd.setText("");

                                                if (!exists) {
                                                    et_qrStart.setText(result.getContents());
                                                    et_qrEnd.setText(result.getContents());
                                                } else {
                                                    Toast.makeText(InspectionActualDelivery.this,
                                                            "Already used QR code",
                                                            Toast.LENGTH_SHORT).show();
                                                }

                                            } else {
                                                Toast.makeText(InspectionActualDelivery.this,
                                                        "Invalid QR Start format",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                            break;
                                        case 4://end
                                            et_qrEnd.setText("");
                                            if (Fun.isQRSeriesCorrect(result.getContents()) == 1) {
                                                et_qrEnd.setText(result.getContents());
                                                //computation here ...qrstart must be less than qrend

                                  /*  String mQRStart = et_qrStart.getText().toString().trim();
                                    String mQREnd = result.toString();

                                    int start = Integer.parseInt(mQRStart.substring(8));
                                    int end = Integer.parseInt(mQREnd.substring(8));


                                    if (end >= start) {
                                        et_qrEnd.setText(result.toString());
                                    } else {
                                        Toast.makeText(InspectionActualDelivery.this,
                                                "QR End must be greater than QR Start",
                                                Toast.LENGTH_LONG).show();
                                    }
*/
                                            } else {
                                                Toast.makeText(InspectionActualDelivery.this,
                                                                "Invalid QR End format", Toast.LENGTH_SHORT)
                                                        .show();
                                            }
                                            break;
                                        default:
                                            //sack code
                                            et_sackCode.setText("");
                                            et_sackCode.setText(result.getContents());

                                    }
                                    /*   if (glbl_scan_mode == 1*//*qr series*//*) {
                            et_qrSeries.setText("");
                            if (Fun.isQRSeriesCorrect(result.toString()) == 1) {
                                et_qrSeries.setText(result.toString());
                            } else {
                                Toast.makeText(InspectionActualDelivery.this,
                                        "Invalid QR series format", Toast.LENGTH_SHORT).show();
                            }
                        } else *//*sack code*//* {
                            et_sackCode.setText("");
                            et_sackCode.setText(result.toString());
                        }*/

                                    //after scan function
                                    /*content_frame.setVisibility(View.GONE);
                                    actual_delivery_form.setVisibility(View.VISIBLE);
                                    mCodeScanner.stopPreview();
                                    mCodeScanner.releaseResources();*/
                                }
                            });
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //initialize screen
        Fun.fullScreen(this);//fullScreen


        /*String x = "S221-03-023001";
        Log.e(TAG, "x_int: " + Integer.parseInt(x.substring(8)));*/


        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        //getSupportActionBar().hide(); // hide the title bar
        setContentView(R.layout.activity_inspection_actual_delivery);

        initActualDelivery();

//        Log.e(TAG, "onCreate: " + DebugDB.getAddressLog());
    }


    void initActualDelivery() {
        glbl_scan_mode = 0;
        mProgressDialog = new ProgressDialog(this);
        rcepDatabase = RCEPDatabase.getAppDatabase(this);
        prefUserAccount = getApplicationContext()
                .getSharedPreferences(Fun.userAccountPreference(), Context.MODE_PRIVATE);
        prefTempInspectionData = getApplicationContext()
                .getSharedPreferences(Fun.tempInspectionPreference(), Context.MODE_PRIVATE);


        currentPhotoPath = "";
        glbl_imgName =
                prefUserAccount.getInt(Fun.uaUserId(), 0) + "-ACD-" + Fun.getTimestamp();
        glbl_imgName2 = glbl_imgName + ".jpg";

     /*   // TODO: 18/03/2021 testing
        SharedPreferences.Editor editor = prefTempInspectionData.edit();
        editor.putInt(Fun.tiForEbinhi(), 1);
        editor.apply();*/
        et_actualDateofInspection = findViewById(R.id.et_actualDateofInspection);
        et_actualDateOfDelivery = findViewById(R.id.et_actualDateOfDelivery);
        imgvw_capture = findViewById(R.id.imgvw_capture);
        til_qstart = findViewById(R.id.til_qstart);
        til_qend = findViewById(R.id.til_qend);
        et_qrStart = findViewById(R.id.et_qrStart);
        et_qrEnd = findViewById(R.id.et_qrEnd);
        iv_preview = findViewById(R.id.iv_preview);
        et_lotNo = findViewById(R.id.et_lotNo);
        et_labNo = findViewById(R.id.et_labNo);
        et_seedTag = findViewById(R.id.et_seedTag);
        et_sackCode = findViewById(R.id.et_sackCode);
        actual_delivery_form = findViewById(R.id.actual_delivery_form);
        content_frame = findViewById(R.id.content_frame);
        ll_qrSeriesContainer = findViewById(R.id.ll_qrSeriesContainer);
        ll_qrSeriesContainer2 = findViewById(R.id.ll_qrSeriesContainer2);
        ll_imgContainer = findViewById(R.id.ll_imgContainer);
        ll_otherTagContainer = findViewById(R.id.ll_otherTagContainer);
        ll_seedTagContainer = findViewById(R.id.ll_seedTagContainer);
        tv_batchTicketNumber = findViewById(R.id.tv_batchTicketNumber);
        et_seedVariety = findViewById(R.id.et_seedVariety);
        et_bagCount = findViewById(R.id.et_bagCount);
        et_remarks = findViewById(R.id.et_remarks);
        et_qrSeries = findViewById(R.id.et_qrSeries);
        et_actualDeliveryImage = findViewById(R.id.et_actualDeliveryImage);
        rv_actualDelivery = findViewById(R.id.rv_actualDelivery);
        chkbox_reject = findViewById(R.id.chkbox_reject);
        actualDeliveryData = new ArrayList<>();
        glbl_batchTicketNumber = prefTempInspectionData.getString(Fun.tiBatchTicketNumber(), "");
        tv_batchTicketNumber.setText(glbl_batchTicketNumber);

        //set properties
        rv_actualDelivery.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rv_actualDelivery.setItemAnimator(new DefaultItemAnimator());
        actualDeliveryAdapter = new ActualDeliveryAdapter(this, actualDeliveryData);
        actualDeliveryAdapter.setitemClickedListener(this);
        rv_actualDelivery.setAdapter(actualDeliveryAdapter);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                for (TblDeliveryInspection d : rcepDatabase.tblDeliveryInspectionDAO()
                        .getSingleDelivery(glbl_batchTicketNumber)) {
                    glbl_region = d.getRegion();
                    glbl_province = d.getProvince();
                    glbl_municipality = d.getMunicipality();
                    glbl_dropOffPoint = d.getDropOffPoint();
                }
            }
        }, 1000);

        et_remarks.setFilters(new InputFilter[]{
                new InputFilter() {
                    @Override
                    public CharSequence filter(CharSequence cs, int start, int end, Spanned spanned,
                                               int dStart, int dEnd) {
                        if (cs.equals("")) { // for backspace
                            return cs;
                        }

                        if (cs.toString().matches("[a-zA-Z0-9 ]+")) {
                            return cs;
                        } else {
                            Toast.makeText(InspectionActualDelivery.this, "Character not allowed",
                                    Toast.LENGTH_SHORT).show();
                        }
                        return "";
                    }
                }
        });

        imgvw_capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

        iv_preview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentPhotoPath.equals("")) {
                    Toast.makeText(InspectionActualDelivery.this, "No image to display",
                                    Toast.LENGTH_SHORT)
                            .show();
                } else {
                    final AlertDialog dialogBuilder =
                            new AlertDialog.Builder(InspectionActualDelivery.this).create();
                    dialogBuilder.setCancelable(true);
                    LayoutInflater inflater =
                            LayoutInflater.from(InspectionActualDelivery.this);
                    final View mView = inflater.inflate(R.layout.custom_idview_dialog, null);
                    //dialogViews
                    final TouchImageView img_receipt = mView.findViewById(R.id.img_receipt);
                    final Button btn_dissmiss = mView.findViewById(R.id.btn_dissmiss);
                    //others
                    Picasso.get().load(new File(currentPhotoPath)).into(img_receipt);
                    //dialogListeners
                    btn_dissmiss.setOnClickListener(view12 -> {

                        dialogBuilder.dismiss();
                    });
                    dialogBuilder.setView(mView);
                    dialogBuilder.show();
                }
            }
        });


        chkbox_reject.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String seedTag = et_seedTag.getText().toString().trim();

                if (isChecked) {
                    //Toast.makeText(InspectionActualDelivery.this, "rejected", Toast.LENGTH_SHORT).show();
                    et_bagCount.setText("0");
                    et_bagCount.setEnabled(false);
                    et_qrSeries.setText("");
                    et_qrStart.setText("");
                    et_qrEnd.setText("");
                    et_qrStart.setEnabled(false);
                    et_qrEnd.setEnabled(false);
                    glbl_start_value = 0;
                    glbl_end_value = 0;
                } else {
                    //Toast.makeText(InspectionActualDelivery.this, "passed", Toast.LENGTH_SHORT).show();
                    et_bagCount.setText(String.valueOf(rcepDatabase.tblDeliveryInspectionDAO()
                            .getBagCountPerSeedTag(glbl_batchTicketNumber,
                                    et_seedTag.getText().toString().trim())));
                    et_bagCount.setEnabled(true);

                    et_qrStart.setEnabled(true);
                    et_qrEnd.setEnabled(true);
                    glbl_start_value = 0;
                    glbl_end_value = 0;
                }
            }
        });


        //Initialize CodeScannerView
       /* CodeScannerView scannerView = findViewById(R.id.scanner_view);
        mCodeScanner = new CodeScanner(this, scannerView);
        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                InspectionActualDelivery.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, "run: " + result.toString());
                        //et_qrSeries.setText("");
                        Log.e(TAG, "glbl_scan_mode: " + glbl_scan_mode);
                        switch (glbl_scan_mode) {
                            case 3://start
                                et_qrStart.setText("");

                                if (Fun.isQRSeriesCorrect(result.toString()) == 1) {
                                    et_qrStart.setText(result.toString());
                                   *//* if (et_qrEnd.getText().toString().trim().equals("")) {
                                        et_qrStart.setText(result.toString());
                                    } else {
                                        String mQRStart = result.toString();
                                        String mQREnd = et_qrEnd.getText().toString().trim();

                                        int start = Integer.parseInt(mQRStart.substring(8));
                                        int end = Integer.parseInt(mQREnd.substring(8));

                                        if (start <= end) {
                                            et_qrStart.setText(result.toString());
                                        } else {
                                            Toast.makeText(InspectionActualDelivery.this,
                                                    "QR Start must be less than QR End",
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    }*//*
                                } else {
                                    Toast.makeText(InspectionActualDelivery.this,
                                            "Invalid QR Start format", Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case 4://end
                                et_qrEnd.setText("");


                                if (Fun.isQRSeriesCorrect(result.toString()) == 1) {

                                    et_qrEnd.setText(result.toString());
                                    //computation here ...qrstart must be less than qrend

                                  *//*  String mQRStart = et_qrStart.getText().toString().trim();
                                    String mQREnd = result.toString();

                                    int start = Integer.parseInt(mQRStart.substring(8));
                                    int end = Integer.parseInt(mQREnd.substring(8));


                                    if (end >= start) {
                                        et_qrEnd.setText(result.toString());
                                    } else {
                                        Toast.makeText(InspectionActualDelivery.this,
                                                "QR End must be greater than QR Start",
                                                Toast.LENGTH_LONG).show();
                                    }
*//*

                                } else {
                                    Toast.makeText(InspectionActualDelivery.this,
                                            "Invalid QR End format", Toast.LENGTH_SHORT).show();
                                }
                                break;
                            default:
                                //sack code
                                et_sackCode.setText("");
                                et_sackCode.setText(result.toString());

                        }


                        *//*   if (glbl_scan_mode == 1*//**//*qr series*//**//*) {
                            et_qrSeries.setText("");
                            if (Fun.isQRSeriesCorrect(result.toString()) == 1) {
                                et_qrSeries.setText(result.toString());
                            } else {
                                Toast.makeText(InspectionActualDelivery.this,
                                        "Invalid QR series format", Toast.LENGTH_SHORT).show();
                            }
                        } else *//**//*sack code*//**//* {
                            et_sackCode.setText("");
                            et_sackCode.setText(result.toString());
                        }*//*

                        //after scan function
                        content_frame.setVisibility(View.GONE);
                        actual_delivery_form.setVisibility(View.VISIBLE);
                        mCodeScanner.stopPreview();
                        mCodeScanner.releaseResources();
                    }
                });
            }
        });*/

        /*others*/
        if (prefTempInspectionData.getInt(Fun.tiForEbinhi(), 0) == 1) {
            //ll_qrSeriesContainer.setVisibility(View.VISIBLE);
            ll_qrSeriesContainer2.setVisibility(View.VISIBLE);
        } else {
            //ll_qrSeriesContainer.setVisibility(View.GONE);
            ll_qrSeriesContainer2.setVisibility(View.GONE);
        }
    }

//    private void dispatchTakePictureIntent() {
//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        // Ensure that there's a camera activity to handle the intent
//        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//            // Create the File where the photo should go
//            File photoFile = null;
//            try {
//                photoFile = createImageFile();
//            } catch (IOException ex) {
//                // Error occurred while creating the File
//
//            }
//            // Continue only if the File was successfully created
//            if (photoFile != null) {
//                Uri photoURI = FileProvider.getUriForFile(InspectionActualDelivery.this,
//                        "ph.gov.philrice.rcepdeliveryinspection",
//                        photoFile);
//                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
//                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
//            }
//        }
//    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e(TAG, "Error creating file: ", ex);
                Toast.makeText(this, "Could not create image file", Toast.LENGTH_SHORT).show();
                return;
            }

            Uri photoURI = FileProvider.getUriForFile(
                    this,
                    BuildConfig.APPLICATION_ID + ".provider", // safer than hardcoded
                    photoFile
            );
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

            // ✅ Grant URI permission explicitly
            takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void setPic(ImageView imageView) {
        // Get the dimensions of the View
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(currentPhotoPath, bmOptions);

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.max(1, Math.min(photoW / targetW, photoH / targetH));

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        imageView.setImageBitmap(bitmap);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
      /*  File image = File.createTempFile(
                imageFileName,  *//* prefix *//*
                ".jpg",         *//* suffix *//*
                storageDir      *//* directory *//*
        );*/

        File image = new File(storageDir, glbl_imgName + ".jpg");

        if (image.exists()) {
            image.delete();
            Log.e(TAG, "imageExists");
        }

        image.createNewFile();

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();

        Log.e(TAG, "currentPhotoPath: " + currentPhotoPath);
        return image;

   /*     // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
   *//*     File image = File.createTempFile(
                imageFileName,  *//**//* prefix *//**//*
                ".jpg",         *//**//* suffix *//**//*
                storageDir      *//**//* directory *//**//*
        );*//*

        File file = new File(storageDir, glbl_imgName + ".jpg");

        if (file.exists()) {
            file.delete();
            Log.e(TAG, "imageExists");
        }

        file.createNewFile();


        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = file.getAbsolutePath();

        Log.e(TAG, "currentPhotoPath: " + currentPhotoPath);
        return file;*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
           /* Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imgvw_preview.setImageBitmap(imageBitmap);*/
            et_actualDeliveryImage.setText(glbl_imgName2);
            Toast.makeText(this, "Image Captured!", Toast.LENGTH_SHORT).show();
            /*File f = new File(currentPhotoPath);
            Uri contentUri = Uri.fromFile(f);*/

            setPic(iv_preview);
        } /*else {
            Toast.makeText(this, "Image capture cancelled", Toast.LENGTH_SHORT).show();
        }*/
    }

    private ArrayList getSeedTags(String batchTicketNumber) {
        ArrayList<String> arrayList = new ArrayList<>();
        //first value in array
        arrayList.add("Clear Selected");
        //arrayList.add("Others");
        //assigning list values from local dbase
        List<String> source = rcepDatabase.tblDeliveryInspectionDAO()
                .getSeedTagsByBatch(batchTicketNumber);
        arrayList.addAll(source);
        return arrayList;
    }


    public void doneInspection(View view) {

        if (/*actualDeliveryData.size() > 0*/rcepDatabase.tblDeliveryInspectionDAO()
                .isActualDeliveryCompleted(glbl_batchTicketNumber) == 0 && !et_actualDeliveryImage
                .getText().toString().trim().isEmpty() &&
                !et_actualDateofInspection.getText().toString().trim().isEmpty() &&
                !et_actualDateOfDelivery.getText().toString().trim().isEmpty()) {

            final String mPrvDropoffId = prefTempInspectionData.getString(Fun.tiPrvDropoffId(), "");
            final String mPrv = prefTempInspectionData.getString(Fun.tiPrv(), "");
            final String mMoaNumber = prefTempInspectionData.getString(Fun.tiMoaNumber(), "");
            //final String mBatchSeries = prefTempInspectionData.getString(Fun.tiBatchSeries(), "");
//     .setMessage("Do you have available Delivery receipt on-hand for batch " +
//                            glbl_batchTicketNumber + "?")
            new MaterialAlertDialogBuilder(InspectionActualDelivery.this)
                    .setTitle("RCEF DI")
                    .setCancelable(false)
                    .setMessage("Do you wish to complete inspection data for batch  " +
                            glbl_batchTicketNumber + "?")
                    .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i){
                         dialogInterface.dismiss();
                        }
                    })
                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            CountDownLatch latch = new CountDownLatch(2); // Number of tasks

                            new Thread(() -> {
                                SharedPreferences.Editor editor = prefTempInspectionData.edit();
                                editor.putString(Fun.tiBatchDeliveryImage(),
                                        et_actualDeliveryImage.getText().toString().trim());
                                editor.putString(Fun.tiBatchDeliveryImagePath(),
                                        currentPhotoPath);
                                editor.putInt(Fun.tiTempDeliveryStatus(), 1);
                                editor.apply();

                                latch.countDown();
                            }).start();

                            new Thread(() -> {
                                //set preferences variables
                                String mBatchTicketNumber = prefTempInspectionData
                                        .getString(Fun.tiBatchTicketNumber(), "");
                                int mScreeningPassed = prefTempInspectionData
                                        .getInt(Fun.tiScreeningPassed(), 0);
                                String mScreeningRemarks = prefTempInspectionData
                                        .getString(Fun.tiScreeningRemarks(), "");
                                int mVisualPassed = prefTempInspectionData
                                        .getInt(Fun.tiVisualPassed(), 0);
                                String mVisualFindings = prefTempInspectionData
                                        .getString(Fun.tiVisualFindings(), "");
                                String mVisualRemarks = prefTempInspectionData
                                        .getString(Fun.tiVisualRemarks(), "");
                                String mVisualInspectionImage = prefTempInspectionData
                                        .getString(Fun.tiVisualInspectionImage(), "");
                                int mSamplingPassed = prefTempInspectionData
                                        .getInt(Fun.tiVisualPassed(), 0);
                                String mSamplingImage = prefTempInspectionData
                                        .getString(Fun.tiSamplingImage(), "");
                                String mSamplingImagePath = prefTempInspectionData
                                        .getString(Fun.tiSamplingImagePath(), "");
                                String mBatchDeliveryImage = prefTempInspectionData
                                        .getString(Fun.tiBatchDeliveryImage(), "");
                                String mBatchDeliveryImagePath = prefTempInspectionData
                                        .getString(Fun.tiBatchDeliveryImagePath(), "");
                                String mDateInspected = prefTempInspectionData
                                        .getString(Fun.tiDateInspected(), "");
                                String mDateCreated = prefTempInspectionData
                                        .getString(Fun.tiDateCreated(), "");
                                int mSend = prefTempInspectionData.getInt(Fun.tiSend(), 0);
                                int mTempStatus = prefTempInspectionData
                                        .getInt(Fun.tiTempDeliveryStatus(), 0);
                                String mAppVersion = Fun.appVersion();
                                int mSendLocal = 1;
                                int mSendCentral = 1;
                                String misBuffer =
                                        prefTempInspectionData.getString(Fun.tiIsBuffer(), "");
                                String actualDateOfInspection =
                                        et_actualDateofInspection.getText().toString().trim();
                                String actualDateOfDelivery =
                                        et_actualDateOfDelivery.getText().toString().trim();
                                //inserting data inspection
                                TblInspection data = new TblInspection(mBatchTicketNumber,
                                        mScreeningPassed, mScreeningRemarks, mVisualPassed,
                                        mVisualFindings, mVisualRemarks, mVisualInspectionImage,
                                        mSamplingPassed, mSamplingImage, mSamplingImagePath,
                                        mBatchDeliveryImage, mBatchDeliveryImagePath,
                                        mDateInspected, mDateCreated, mSend, mPrv, mMoaNumber,
                                        mAppVersion, "", mSendLocal, mSendCentral,
                                        Integer.parseInt(misBuffer), "", "", "", "",
                                        actualDateOfInspection, actualDateOfDelivery);
                                rcepDatabase.tblInspectionDAO().insertInspection(data);
                                //insert local status data
                                TblDeliveryStatusAppLocal dsData =
                                        new TblDeliveryStatusAppLocal(
                                                mBatchTicketNumber, mTempStatus, mDateInspected,
                                                0, 0);
                                rcepDatabase.tblDeliveryStatusAppLocalDAO()
                                        .insertDeliveryStatusAppLocal(dsData);
                                //insert all values to tbl_sampling
                                for (TblTempSampling b : rcepDatabase.tblTempSamplingDAO()
                                        .getTempSamplingData()) {
                                    TblSampling tsData = new TblSampling(
                                            b.getBatchTicketNumber(), b.getSeedTag(),
                                            b.getBagWeight(), b.getDateSampled(), b.getSend(),
                                            mPrv, mMoaNumber, mAppVersion, "", mSendLocal,
                                            mSendCentral);
                                    rcepDatabase.tblSamplingDAO().insertSampling(tsData);
                                }
                                //insert all values to tbl_actual_delivery
                                if (actualDeliveryData.size() > 0) {
                                    for (ActualDeliveryData a : actualDeliveryData) {
                                        TblActualDelivery adData = new TblActualDelivery(
                                                a.getBatchTicketNumber(), glbl_region,
                                                glbl_province, glbl_municipality,
                                                glbl_dropOffPoint, a.getSeedVariety(),
                                                a.getTotalBagCount(), a.getDateCreated(),
                                                a.getSend(), a.getSeedTag(), mPrvDropoffId,
                                                mPrv, mMoaNumber, mAppVersion,
                                                a.getBatchSeries(), mSendLocal, mSendCentral,
                                                a.getRemarks(), a.getIsRejected(),
                                                a.getIsDownloaded(), a.getHasRLA(),
                                                a.getSack_code(), a.getMisBuffer(),
                                                a.getQRValStart(), a.getQRValEnd(),
                                                a.getQRStart(), a.getQREnd());
                                        rcepDatabase.tblActualDeliveryDAO()
                                                .insertActualDelivery(adData);
                                    }
                                }

                                latch.countDown();
                            }).start();

                            new Thread(() -> {
                                try {
                                    latch.await(); // Wait for both tasks to finish
                                    dialogInterface.dismiss();
                                    //done sampling proceed to inspection main page
                                    Intent intent = new Intent(
                                            InspectionActualDelivery.this,
                                            InspectionMainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    finish();
                                } catch (InterruptedException e) {
                                    Toast.makeText(InspectionActualDelivery.this, e.toString(),
                                            Toast.LENGTH_SHORT).show();
                                }


                            }).start();

                            //update preference
                          /*  SharedPreferences.Editor editor = prefTempInspectionData.edit();
                            editor.putString(Fun.tiBatchDeliveryImage(),
                                    et_actualDeliveryImage.getText().toString().trim());
                            editor.putString(Fun.tiBatchDeliveryImagePath(),
                                    currentPhotoPath);
                            editor.putInt(Fun.tiTempDeliveryStatus(), 1);
                            editor.apply();*/

                            /*final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                     //set preferences variables
                                    String mBatchTicketNumber = prefTempInspectionData
                                            .getString(Fun.tiBatchTicketNumber(), "");
                                    int mScreeningPassed = prefTempInspectionData
                                            .getInt(Fun.tiScreeningPassed(), 0);
                                    String mScreeningRemarks = prefTempInspectionData
                                            .getString(Fun.tiScreeningRemarks(), "");
                                    int mVisualPassed = prefTempInspectionData
                                            .getInt(Fun.tiVisualPassed(), 0);
                                    String mVisualFindings = prefTempInspectionData
                                            .getString(Fun.tiVisualFindings(), "");
                                    String mVisualRemarks = prefTempInspectionData
                                            .getString(Fun.tiVisualRemarks(), "");
                                    String mVisualInspectionImage = prefTempInspectionData
                                            .getString(Fun.tiVisualInspectionImage(), "");
                                    int mSamplingPassed = prefTempInspectionData
                                            .getInt(Fun.tiVisualPassed(), 0);
                                    String mSamplingImage = prefTempInspectionData
                                            .getString(Fun.tiSamplingImage(), "");
                                    String mSamplingImagePath = prefTempInspectionData
                                            .getString(Fun.tiSamplingImagePath(), "");
                                    String mBatchDeliveryImage = prefTempInspectionData
                                            .getString(Fun.tiBatchDeliveryImage(), "");
                                    String mBatchDeliveryImagePath = prefTempInspectionData
                                            .getString(Fun.tiBatchDeliveryImagePath(), "");
                                    String mDateInspected = prefTempInspectionData
                                            .getString(Fun.tiDateInspected(), "");
                                    String mDateCreated = prefTempInspectionData
                                            .getString(Fun.tiDateCreated(), "");
                                    int mSend = prefTempInspectionData.getInt(Fun.tiSend(), 0);
                                    int mTempStatus = prefTempInspectionData
                                            .getInt(Fun.tiTempDeliveryStatus(), 0);
                                    String mAppVersion = Fun.appVersion();
                                    int mSendLocal = 1;
                                    int mSendCentral = 1;
                                    String misBuffer =
                                            prefTempInspectionData.getString(Fun.tiIsBuffer(), "");
                                    String actualDateOfInspection =
                                            et_actualDateofInspection.getText().toString().trim();
                                    String actualDateOfDelivery =
                                            et_actualDateOfDelivery.getText().toString().trim();
                                    //inserting data inspection
                                    TblInspection data = new TblInspection(mBatchTicketNumber,
                                            mScreeningPassed, mScreeningRemarks, mVisualPassed,
                                            mVisualFindings, mVisualRemarks, mVisualInspectionImage,
                                            mSamplingPassed, mSamplingImage, mSamplingImagePath,
                                            mBatchDeliveryImage, mBatchDeliveryImagePath,
                                            mDateInspected, mDateCreated, mSend, mPrv, mMoaNumber,
                                            mAppVersion, "", mSendLocal, mSendCentral,
                                            Integer.parseInt(misBuffer), "", "", "", "",
                                            actualDateOfInspection, actualDateOfDelivery);
                                    rcepDatabase.tblInspectionDAO().insertInspection(data);
                                    //insert local status data
                                    TblDeliveryStatusAppLocal dsData =
                                            new TblDeliveryStatusAppLocal(
                                                    mBatchTicketNumber, mTempStatus, mDateInspected,
                                                    0, 0);
                                    rcepDatabase.tblDeliveryStatusAppLocalDAO()
                                            .insertDeliveryStatusAppLocal(dsData);
                                    //insert all values to tbl_sampling
                                    for (TblTempSampling b : rcepDatabase.tblTempSamplingDAO()
                                            .getTempSamplingData()) {
                                        TblSampling tsData = new TblSampling(
                                                b.getBatchTicketNumber(), b.getSeedTag(),
                                                b.getBagWeight(), b.getDateSampled(), b.getSend(),
                                                mPrv, mMoaNumber, mAppVersion, "", mSendLocal,
                                                mSendCentral);
                                        rcepDatabase.tblSamplingDAO().insertSampling(tsData);
                                    }
                                    //insert all values to tbl_actual_delivery
                                    if (actualDeliveryData.size() > 0) {
                                        for (ActualDeliveryData a : actualDeliveryData) {
                                            TblActualDelivery adData = new TblActualDelivery(
                                                    a.getBatchTicketNumber(), glbl_region,
                                                    glbl_province, glbl_municipality,
                                                    glbl_dropOffPoint, a.getSeedVariety(),
                                                    a.getTotalBagCount(), a.getDateCreated(),
                                                    a.getSend(), a.getSeedTag(), mPrvDropoffId,
                                                    mPrv, mMoaNumber, mAppVersion,
                                                    a.getBatchSeries(), mSendLocal, mSendCentral,
                                                    a.getRemarks(), a.getIsRejected(),
                                                    a.getIsDownloaded(), a.getHasRLA(),
                                                    a.getSack_code(), a.getMisBuffer(),
                                                    a.getQRValStart(), a.getQRValEnd(),
                                                    a.getQRStart(), a.getQREnd());
                                            rcepDatabase.tblActualDeliveryDAO()
                                                    .insertActualDelivery(adData);
                                        }
                                    }


                                    dialogInterface.dismiss();
                                    //done sampling proceed to inspection main page
                                    Intent intent = new Intent(
                                            InspectionActualDelivery.this,
                                            InspectionMainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    finish();

                                }
                            }, 1500);*/
                        }
                    })
                    .show();

            /*CFAlertDialog.Builder builder = new CFAlertDialog.Builder(
                    InspectionActualDelivery.this);
            builder.setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET);
            builder.setTitle("RCEP App");
            builder.setMessage("Do you have available Delivery receipt on-hand for batch " +
                    glbl_batchTicketNumber + "?");
            builder.addButton("YES", Color.parseColor("#FFFFFF"), Color.parseColor("#429ef4"),
                    CFAlertDialog.CFAlertActionStyle.NEGATIVE,
                    CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            // TODO: 10/10/2022
                            //go to inspection accounting delivery
                            //update preference
                            SharedPreferences.Editor editor = prefTempInspectionData.edit();
                            editor.putString(Fun.tiBatchDeliveryImage(),
                                    et_actualDeliveryImage.getText().toString().trim());
                            editor.putString(Fun.tiBatchDeliveryImagePath(),
                                    currentPhotoPath);
                            editor.putInt(Fun.tiTempDeliveryStatus(), 1);
                            editor.apply();

                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    //set preferences variables
                                    String mBatchTicketNumber = prefTempInspectionData
                                            .getString(Fun.tiBatchTicketNumber(), "");
                                    int mScreeningPassed = prefTempInspectionData
                                            .getInt(Fun.tiScreeningPassed(), 0);
                                    String mScreeningRemarks = prefTempInspectionData
                                            .getString(Fun.tiScreeningRemarks(), "");
                                    int mVisualPassed = prefTempInspectionData
                                            .getInt(Fun.tiVisualPassed(), 0);
                                    String mVisualFindings = prefTempInspectionData
                                            .getString(Fun.tiVisualFindings(), "");
                                    String mVisualRemarks = prefTempInspectionData
                                            .getString(Fun.tiVisualRemarks(), "");
                                    String mVisualInspectionImage = prefTempInspectionData
                                            .getString(Fun.tiVisualInspectionImage(), "");
                                    int mSamplingPassed = prefTempInspectionData
                                            .getInt(Fun.tiVisualPassed(), 0);
                                    String mSamplingImage = prefTempInspectionData
                                            .getString(Fun.tiSamplingImage(), "");
                                    String mSamplingImagePath = prefTempInspectionData
                                            .getString(Fun.tiSamplingImagePath(), "");
                                    String mBatchDeliveryImage = prefTempInspectionData
                                            .getString(Fun.tiBatchDeliveryImage(), "");
                                    String mBatchDeliveryImagePath = prefTempInspectionData
                                            .getString(Fun.tiBatchDeliveryImagePath(), "");
                                    String mDateInspected = prefTempInspectionData
                                            .getString(Fun.tiDateInspected(), "");
                                    String mDateCreated = prefTempInspectionData
                                            .getString(Fun.tiDateCreated(), "");
                                    int mSend = prefTempInspectionData.getInt(Fun.tiSend(), 0);
                                    int mTempStatus = prefTempInspectionData
                                            .getInt(Fun.tiTempDeliveryStatus(), 0);
                                    String mAppVersion = Fun.appVersion();
                                    int mSendLocal = 1;
                                    int mSendCentral = 1;
                                    String misBuffer =
                                            prefTempInspectionData.getString(Fun.tiIsBuffer(), "");
                                    String actualDateOfInspection =
                                            et_actualDateofInspection.getText().toString().trim();
                                    //inserting data inspection
                                    TblInspection data = new TblInspection(mBatchTicketNumber,
                                            mScreeningPassed, mScreeningRemarks, mVisualPassed,
                                            mVisualFindings, mVisualRemarks, mVisualInspectionImage,
                                            mSamplingPassed, mSamplingImage, mSamplingImagePath,
                                            mBatchDeliveryImage, mBatchDeliveryImagePath,
                                            mDateInspected, mDateCreated, mSend, mPrv, mMoaNumber,
                                            mAppVersion, "", mSendLocal, mSendCentral,
                                            Integer.parseInt(misBuffer), "", "", "", "",
                                            actualDateOfInspection);
                                    rcepDatabase.tblInspectionDAO().insertInspection(data);
                                    //insert local status data
                                    TblDeliveryStatusAppLocal dsData =
                                            new TblDeliveryStatusAppLocal(
                                                    mBatchTicketNumber, mTempStatus, mDateInspected,
                                                    0, 0);
                                    rcepDatabase.tblDeliveryStatusAppLocalDAO()
                                            .insertDeliveryStatusAppLocal(dsData);
                                    //insert all values to tbl_sampling
                                    for (TblTempSampling b : rcepDatabase.tblTempSamplingDAO()
                                            .getTempSamplingData()) {
                                        TblSampling tsData = new TblSampling(
                                                b.getBatchTicketNumber(), b.getSeedTag(),
                                                b.getBagWeight(), b.getDateSampled(), b.getSend(),
                                                mPrv, mMoaNumber, mAppVersion, "", mSendLocal,
                                                mSendCentral);
                                        rcepDatabase.tblSamplingDAO().insertSampling(tsData);
                                    }
                                    //insert all values to tbl_actual_delivery
                                    if (actualDeliveryData.size() > 0) {
                                        for (ActualDeliveryData a : actualDeliveryData) {
                                            TblActualDelivery adData = new TblActualDelivery(
                                                    a.getBatchTicketNumber(), glbl_region,
                                                    glbl_province, glbl_municipality,
                                                    glbl_dropOffPoint, a.getSeedVariety(),
                                                    a.getTotalBagCount(), a.getDateCreated(),
                                                    a.getSend(), a.getSeedTag(), mPrvDropoffId,
                                                    mPrv, mMoaNumber, mAppVersion,
                                                    a.getBatchSeries(), mSendLocal, mSendCentral,
                                                    a.getRemarks(), a.getIsRejected(),
                                                    a.getIsDownloaded(), a.getHasRLA(),
                                                    a.getSack_code(), a.getMisBuffer(),
                                                    a.getQRValStart(), a.getQRValEnd(),
                                                    a.getQRStart(), a.getQREnd());
                                            rcepDatabase.tblActualDeliveryDAO()
                                                    .insertActualDelivery(adData);
                                        }
                                    }

                                }
                            }, 1500);


                            dialog.dismiss();
                            //done sampling proceed to inspection main page
                            Intent intent = new Intent(
                                    InspectionActualDelivery.this,
                                    InspectionAccounting.class);
                            intent.putExtra("batchTicketNumber", glbl_batchTicketNumber);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        }
                    });

            builder.addButton("NO", Color.parseColor("#FFFFFF"), Color.parseColor("#429ef4"),
                    CFAlertDialog.CFAlertActionStyle.POSITIVE,
                    CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, int which) {
                            //update preference
                            SharedPreferences.Editor editor = prefTempInspectionData.edit();
                            editor.putString(Fun.tiBatchDeliveryImage(),
                                    et_actualDeliveryImage.getText().toString().trim());
                            editor.putString(Fun.tiBatchDeliveryImagePath(),
                                    currentPhotoPath);
                            editor.putInt(Fun.tiTempDeliveryStatus(), 1);
                            editor.apply();

                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    //set preferences variables
                                    String mBatchTicketNumber = prefTempInspectionData
                                            .getString(Fun.tiBatchTicketNumber(), "");
                                    int mScreeningPassed = prefTempInspectionData
                                            .getInt(Fun.tiScreeningPassed(), 0);
                                    String mScreeningRemarks = prefTempInspectionData
                                            .getString(Fun.tiScreeningRemarks(), "");
                                    int mVisualPassed = prefTempInspectionData
                                            .getInt(Fun.tiVisualPassed(), 0);
                                    String mVisualFindings = prefTempInspectionData
                                            .getString(Fun.tiVisualFindings(), "");
                                    String mVisualRemarks = prefTempInspectionData
                                            .getString(Fun.tiVisualRemarks(), "");
                                    String mVisualInspectionImage = prefTempInspectionData
                                            .getString(Fun.tiVisualInspectionImage(), "");
                                    int mSamplingPassed = prefTempInspectionData
                                            .getInt(Fun.tiVisualPassed(), 0);
                                    String mSamplingImage = prefTempInspectionData
                                            .getString(Fun.tiSamplingImage(), "");
                                    String mSamplingImagePath = prefTempInspectionData
                                            .getString(Fun.tiSamplingImagePath(), "");
                                    String mBatchDeliveryImage = prefTempInspectionData
                                            .getString(Fun.tiBatchDeliveryImage(), "");
                                    String mBatchDeliveryImagePath = prefTempInspectionData
                                            .getString(Fun.tiBatchDeliveryImagePath(), "");
                                    String mDateInspected = prefTempInspectionData
                                            .getString(Fun.tiDateInspected(), "");
                                    String mDateCreated = prefTempInspectionData
                                            .getString(Fun.tiDateCreated(), "");
                                    int mSend = prefTempInspectionData.getInt(Fun.tiSend(), 0);
                                    int mTempStatus = prefTempInspectionData
                                            .getInt(Fun.tiTempDeliveryStatus(), 0);
                                    String mAppVersion = Fun.appVersion();
                                    int mSendLocal = 1;
                                    int mSendCentral = 1;
                                    String misBuffer =
                                            prefTempInspectionData.getString(Fun.tiIsBuffer(), "");
                                    String actualDateOfInspection =
                                            et_actualDateofInspection.getText().toString().trim();
                                    //inserting data inspection
                                    TblInspection data = new TblInspection(mBatchTicketNumber,
                                            mScreeningPassed, mScreeningRemarks, mVisualPassed,
                                            mVisualFindings, mVisualRemarks, mVisualInspectionImage,
                                            mSamplingPassed, mSamplingImage, mSamplingImagePath,
                                            mBatchDeliveryImage, mBatchDeliveryImagePath,
                                            mDateInspected, mDateCreated, mSend, mPrv, mMoaNumber,
                                            mAppVersion, "", mSendLocal, mSendCentral,
                                            Integer.parseInt(misBuffer), "", "", "", "",
                                            actualDateOfInspection);
                                    rcepDatabase.tblInspectionDAO().insertInspection(data);
                                    //insert local status data
                                    TblDeliveryStatusAppLocal dsData =
                                            new TblDeliveryStatusAppLocal(
                                                    mBatchTicketNumber, mTempStatus, mDateInspected,
                                                    0, 0);
                                    rcepDatabase.tblDeliveryStatusAppLocalDAO()
                                            .insertDeliveryStatusAppLocal(dsData);
                                    //insert all values to tbl_sampling
                                    for (TblTempSampling b : rcepDatabase.tblTempSamplingDAO()
                                            .getTempSamplingData()) {
                                        TblSampling tsData = new TblSampling(
                                                b.getBatchTicketNumber(), b.getSeedTag(),
                                                b.getBagWeight(), b.getDateSampled(), b.getSend(),
                                                mPrv, mMoaNumber, mAppVersion, "", mSendLocal,
                                                mSendCentral);
                                        rcepDatabase.tblSamplingDAO().insertSampling(tsData);
                                    }
                                    //insert all values to tbl_actual_delivery
                                    if (actualDeliveryData.size() > 0) {
                                        for (ActualDeliveryData a : actualDeliveryData) {
                                            TblActualDelivery adData = new TblActualDelivery(
                                                    a.getBatchTicketNumber(), glbl_region,
                                                    glbl_province, glbl_municipality,
                                                    glbl_dropOffPoint, a.getSeedVariety(),
                                                    a.getTotalBagCount(), a.getDateCreated(),
                                                    a.getSend(), a.getSeedTag(), mPrvDropoffId,
                                                    mPrv, mMoaNumber, mAppVersion,
                                                    a.getBatchSeries(), mSendLocal, mSendCentral,
                                                    a.getRemarks(), a.getIsRejected(),
                                                    a.getIsDownloaded(), a.getHasRLA(),
                                                    a.getSack_code(), a.getMisBuffer(),
                                                    a.getQRValStart(), a.getQRValEnd(),
                                                    a.getQRStart(), a.getQREnd());
                                            rcepDatabase.tblActualDeliveryDAO()
                                                    .insertActualDelivery(adData);
                                        }
                                    }


                                    dialog.dismiss();
                                    //done sampling proceed to inspection main page
                                    Intent intent = new Intent(
                                            InspectionActualDelivery.this,
                                            InspectionMainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    finish();

                                }
                            }, 1500);
                        }
                    });
            builder.show();*/
        } else {
            Toast.makeText(this,
                    "Please complete actual delivery inputs(*) and take image to done inspection",
                    Toast.LENGTH_LONG).show();
        }
        //Log.e(TAG, "doneSampling: " + Fun.jsonTempActualDelivery(actualDeliveryData));
    }

    public void setSeedVariety(View view) {
        final Handler handler = new Handler();
        SpinnerDialog ss = new SpinnerDialog(InspectionActualDelivery.this, getSeeds(),
                "Select Variety");
        ss.setCancellable(false);
        ss.setShowKeyboard(false);
        ss.bindOnSpinerListener(new OnSpinerItemClick() {
            @Override
            public void onClick(final String item, int position) {
                switch (item.toLowerCase()) {
                    case "clear selected":
                        //Log.e(TAG, "onClick: " + position);
                        et_seedVariety.setText("");
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
//                                clearData();
//                                clearFocus();
                                et_seedVariety.setText("");
                            }
                        }, 250);
                        break;
                    default:
                        //Log.e(TAG, "onClick: " + position);
                        et_seedVariety.setText(item);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                et_seedVariety.setText(item);
//                                clearData();
//                                clearFocus();
                            }
                        }, 250);
                }
            }
        });
        ss.showSpinerDialog();
    }

    private ArrayList getSeeds() {
        ArrayList<String> arrayList = new ArrayList<>();
        //first value in array
        arrayList.add("Clear Selected");
        arrayList.add("NSIC Rc 160");
        arrayList.add("NSIC Rc 216");
        arrayList.add("NSIC Rc 222");
        //assigning list values from local dbase
        List<String> source = rcepDatabase.tblDeliveryDAO()
                .getVarietiesByBatch(glbl_batchTicketNumber);
        arrayList.addAll(source);
        return arrayList;
    }

    public void nextVariety(View view) {
        //Log.e(TAG, "nextVariety: " + verify());

        boolean exists = actualDeliveryData.stream()
                .anyMatch(data -> et_qrStart.getText().toString().trim()
                        .equals(data.getQRValStart()));

        if (prefTempInspectionData.getInt(Fun.tiForEbinhi(), 0) == 1) {
            if (exists) {
                Toast.makeText(this, "Already used QR code", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (verify() > 0) {
            Toast.makeText(this, "Please complete and verify data before moving to next lot",
                    Toast.LENGTH_LONG).show();
        } else {
            final String mPrvDropoffId = prefTempInspectionData.getString(Fun.tiPrvDropoffId(), "");
            final String mPrv = prefTempInspectionData.getString(Fun.tiPrv(), "");
            final String mMoaNumber = prefTempInspectionData.getString(Fun.tiMoaNumber(), "");
            final String mBatchSeries = "";
            final String mQRStart = et_qrStart.getText().toString().trim();
            final String mQREnd = et_qrEnd.getText().toString().trim();
            if (prefTempInspectionData.getInt(Fun.tiForEbinhi(), 0) == 1) {
                if (!chkbox_reject.isChecked()) {
                    glbl_start_value = Integer.parseInt(mQRStart.substring(8));
                    glbl_end_value = Integer.parseInt(mQREnd.substring(8));
                } else {
                    glbl_start_value = 0;
                    glbl_end_value = 0;
                }
            } else {
                glbl_start_value = 0;
                glbl_end_value = 0;
            }

            final String mSackCode = et_sackCode.getText().toString().trim();

            final String mBatchTicketNumber = glbl_batchTicketNumber;
            final String mVariety = et_seedVariety.getText().toString().trim();
            /*  String mSeedTag = et_labNo.getText().toString().trim() + "/" + et_lotNo.getText().toString().trim();*/
            final String seedTag;
            String mSeedTag = et_seedTag.getText().toString().trim().toLowerCase();
            String labNo = et_labNo.getText().toString().trim().toUpperCase();
            String lotNo = et_lotNo.getText().toString().trim().toUpperCase();
            final String appVersion = Fun.appVersion();
            final int mSendLocal = 1;
            final int mSendCentral = 1;

            final String mRemarks = et_remarks.getText().toString().trim()
                    .equals("") ? "no remarks" : et_remarks.getText().toString().trim();

            if (mSeedTag.equals("others")) {
                if (!labNo.equals("") && !lotNo.equals("")) {
                    seedTag = labNo + "/" + lotNo;
                } else {
                    seedTag = "";
                }
            } else {
                if (!et_seedTag.getText().toString().trim().equals("")) {
                    seedTag = et_seedTag.getText().toString();
                } else {
                    seedTag = "";
                }
            }

            int mLimit = rcepDatabase.tblDeliveryInspectionDAO()
                    .getBagCountPerSeedTag(glbl_batchTicketNumber, seedTag);
            final int mBagCount = Integer.parseInt(et_bagCount.getText().toString());
            final String mDateCreated = Fun.getCurrentDate();
            final int mSend = 1;
            final int mIsRejected = chkbox_reject.isChecked() ? 1 : 0;
            final int mIsDownloaded = 0;
            final String mIsBuffer = prefTempInspectionData.getString(Fun.tiIsBuffer(), "");


            if (mBagCount >= 0) {
                if (mBagCount > mLimit) {
                    Toast.makeText(this, "Cannot exceed more than confirmed " + mLimit + " bags",
                            Toast.LENGTH_SHORT).show();
                } else {
                    if ((mBagCount != mLimit) && et_remarks.getText().toString().isEmpty()) {
                        Toast.makeText(this,
                                "You have initiated partial rejection. Please enter remarks",
                                Toast.LENGTH_LONG).show();
                    } else {
                        //removing seedTag from selection
                        rcepDatabase.tblDeliveryInspectionDAO()
                                .setActualDelivery1(glbl_batchTicketNumber, seedTag);

                        Log.e(TAG, "nextVariety: " + mBatchSeries);

                        new MaterialAlertDialogBuilder(InspectionActualDelivery.this)
                                .setTitle("RCEF DI")
                                .setCancelable(false)
                                .setMessage("Do you have on-hand RLA document for seed tag \"" +
                                        seedTag +
                                        "\"?")
                                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        ActualDeliveryData data = new ActualDeliveryData(
                                                mBatchTicketNumber, mVariety, mBagCount,
                                                mDateCreated,
                                                mSend, seedTag, mPrvDropoffId, mPrv, mMoaNumber,
                                                appVersion, mBatchSeries, mSendLocal, mSendCentral,
                                                mRemarks, mIsRejected, mIsDownloaded, 1, mSackCode,
                                                Integer.parseInt(mIsBuffer), mQRStart, mQREnd,
                                                glbl_start_value, glbl_end_value);
                                        actualDeliveryData.add(0, data);
                                        actualDeliveryAdapter.notifyDataSetChanged();

                                        final Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                //run code here
                                                dialogInterface.dismiss();
                                                clearVal();
                                            }
                                        }, 250);
                                    }
                                })
                                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        ActualDeliveryData data = new ActualDeliveryData(
                                                mBatchTicketNumber, mVariety, mBagCount,
                                                mDateCreated,
                                                mSend, seedTag, mPrvDropoffId, mPrv, mMoaNumber,
                                                appVersion, mBatchSeries, mSendLocal, mSendCentral,
                                                mRemarks, mIsRejected, mIsDownloaded, 0, mSackCode,
                                                Integer.parseInt(mIsBuffer), mQRStart, mQREnd,
                                                glbl_start_value, glbl_end_value);
                                        actualDeliveryData.add(0, data);
                                        actualDeliveryAdapter.notifyDataSetChanged();

                                        final Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                //run code here
                                                dialogInterface.dismiss();
                                                clearVal();
                                            }
                                        }, 250);
                                    }
                                })
                                .show();
                    }
                }
            }
        }
    }


    void clearVal() {
        et_remarks.setText("");
        et_remarks.clearFocus();
        et_labNo.setText("");
        et_lotNo.setText("");
        et_seedTag.setText("");
        et_bagCount.setText("");
        et_qrSeries.setText("");
        et_qrEnd.setText("");
        et_qrEnd.clearFocus();
        et_qrStart.setText("");
        et_qrStart.clearFocus();
        et_sackCode.setText("");
        et_sackCode.clearFocus();
        et_bagCount.clearFocus();
        et_seedVariety.setText("");
        ll_otherTagContainer.setVisibility(View.GONE);
        ll_seedTagContainer.setVisibility(View.VISIBLE);

        et_bagCount.setEnabled(false);
        chkbox_reject.setEnabled(false);
        chkbox_reject.setChecked(false);

        til_qend.setError(null);
        til_qstart.setError(null);

        clearFocus();
    }

    void clearFocus() {
        /*et_seedVariety.clearFocus();
        et_bagCount.clearFocus();
        et_lotNo.clearFocus();
        et_seedTag.clearFocus();
        et_labNo.clearFocus();*/
        et_labNo.clearFocus();
        et_lotNo.clearFocus();
        et_seedTag.clearFocus();
        et_bagCount.clearFocus();
        et_seedVariety.clearFocus();

        et_qrStart.clearFocus();
        et_qrEnd.clearFocus();
    }


    int verify() {
        int verify = 0;
        String seedTag;

        if (et_bagCount.getText().toString().trim().isEmpty()) {
            verify++;
        }

        if (et_seedVariety.getText().toString().trim().isEmpty()) {
            verify++;
        }

        String mSeedTag = et_seedTag.getText().toString().trim().toLowerCase();
        String labNo = et_labNo.getText().toString().trim().toUpperCase();
        String lotNo = et_lotNo.getText().toString().trim().toUpperCase();

        if (mSeedTag.equals("others")) {
            if (!labNo.isEmpty() && !lotNo.isEmpty()) {
                seedTag = labNo + "/" + lotNo;
            } else {
                seedTag = "";
            }
        } else {
            if (!et_seedTag.getText().toString().trim().isEmpty()) {
                seedTag = et_seedTag.getText().toString();
            } else {
                seedTag = "";
            }
        }

        if (seedTag.isEmpty()) {
            verify++;
        }

        //for ebinhi process
        if (prefTempInspectionData.getInt(Fun.tiForEbinhi(), 0) == 1) {
            glbl_start_value = 0;
            glbl_end_value = 0;

            String mQrStart = et_qrStart.getText().toString().trim();
            String mQrEnd = et_qrEnd.getText().toString().trim();

            if (!chkbox_reject.isChecked()) {
               /* if (!(et_qrSeries.getText().toString().trim().length() > 0)) {
                    verify++;
                }*/

                if (Fun.isQRSeriesCorrect(mQrStart) == 1 && Fun.isQRSeriesCorrect(mQrEnd) == 1) {
                    int start = Integer.parseInt(mQrStart.substring(8));
                    int end = Integer.parseInt(mQrEnd.substring(8));
                    glbl_start_value = start;
                    glbl_end_value = end;
                    til_qstart.setError(null);
                    til_qend.setError(null);

                    if (glbl_start_value > glbl_end_value) {
                        verify++;
                        //Log.e(TAG, "verify: ", );


                        til_qstart.setError("should be less than end");
                    }

                    if (glbl_end_value < glbl_start_value) {
                        verify++;
                        Toast.makeText(InspectionActualDelivery.this,
                                "QR end must be greater than QR start",
                                Toast.LENGTH_SHORT).show();
                        til_qend.setError("should be greater than start");
                    }


                } else {
                    til_qstart.setError(null);
                    til_qend.setError(null);
                    verify++;

                    if (Fun.isQRSeriesCorrect(mQrStart) == 0) {
                        til_qstart.setError("Invalid format");
                    }

                    if (Fun.isQRSeriesCorrect(mQrEnd) == 0) {
                        til_qend.setError("Invalid format");
                    }
/*
                    Fun.isQRSeriesCorrect(mQrStart) == 1 && Fun.isQRSeriesCorrect(mQrEnd) == 1)

                    Toast.makeText(this, "Please check QR start and QR end format",
                            Toast.LENGTH_SHORT).show();*/

                }


               /* if (!(mQrStart.length() > 0)) {
                    //verify++;
                    if (Fun.isQRSeriesCorrect(mQrStart) == 1) {
                        asdfasdf
                        int start = Integer.parseInt(mQrStart.substring(8));
                        int end = Integer.parseInt(mQrEnd.substring(8));

                        if (start <= end) {
                            // et_qrStart.setText(result.toString());
                        } else {
                            Toast.makeText(InspectionActualDelivery.this,
                                    "QR Start must be less than QR End",
                                    Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(this, "Invalid QR Start format", Toast.LENGTH_SHORT).show();
                        verify++;
                    }
                }*/
                /*if (!(mQrEnd.length() > 0)) {
                    if (Fun.isQRSeriesCorrect(mQrEnd) == 1) {
                        asdfasdf
                        String mQRStart = et_qrStart.getText().toString().trim();
                        String mQREnd = result.toString();

                        int start = Integer.parseInt(mQRStart.substring(8));
                        int end = Integer.parseInt(mQREnd.substring(8));


                        if (end >= start) {
                            et_qrEnd.setText(result.toString());
                        } else {
                            Toast.makeText(InspectionActualDelivery.this,
                                    "QR End must be greater than QR Start",
                                    Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(this, "Invalid QR End format", Toast.LENGTH_SHORT).show();
                        verify++;
                    }
                }*/
            }
        }



        /*if (!chkbox_reject.isChecked()) {
            if (!(et_qrSeries.getText().toString().trim().length() > 0)) {
                verify++;
            }
        }*/


        return verify;
    }


    @Override
    public void onDelete(final int position) {
        Fun.progressStart(mProgressDialog, "", "Removing selected variety from list");
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Fun.progressStop(mProgressDialog);
                //re-adding seedTag from selection
                rcepDatabase.tblDeliveryInspectionDAO()
                        .setActualDelivery0(glbl_batchTicketNumber,
                                actualDeliveryData.get(position).getSeedTag());
                //proceed verification screen
                //remove selected sample
                Fun.hideKeyboard(InspectionActualDelivery.this);
                clearFocus();
                actualDeliveryData.remove(position);
                //updates sampling list
                actualDeliveryAdapter.notifyDataSetChanged();


            }
        }, 1250);
    }

    @Override
    public void onBackPressed() {
        new MaterialAlertDialogBuilder(InspectionActualDelivery.this)
                .setTitle("RCEF DI")
                .setCancelable(false)
                .setMessage("Cancel batch " + glbl_batchTicketNumber + " inspection?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dialogInterface.dismiss();
                                Intent intent = new Intent(InspectionActualDelivery.this,
                                        InspectionMainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }
                        }, 500);
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .show();


        /*CFAlertDialog.Builder builder =
                new CFAlertDialog.Builder(InspectionActualDelivery.this);
        builder.setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET);
        builder.setTitle("RCEP App");
        builder.setMessage("Cancel batch " + glbl_batchTicketNumber + " inspection?");
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

                                Intent intent = new Intent(InspectionActualDelivery.this,
                                        InspectionMainActivity.class);
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

    public void setSeedTag(View view) {
        final Handler handler = new Handler();
        SpinnerDialog ss = new SpinnerDialog(InspectionActualDelivery.this,
                getSeedTags(glbl_batchTicketNumber), "Select Seed Tag");
        ss.setCancellable(false);
        ss.setShowKeyboard(false);
        ss.bindOnSpinerListener(new OnSpinerItemClick() {
            @Override
            public void onClick(final String item, int position) {
                switch (item.toLowerCase()) {
                    case "clear selected":
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                et_seedTag.setText("");
                                et_seedVariety.setText("");
                                et_bagCount.setText("");
                                et_bagCount.setEnabled(false);
                                chkbox_reject.setEnabled(false);
                                chkbox_reject.setChecked(false);

                            }
                        }, 200);
                        break;
                    /*case "others":
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                et_seedTag.setText(item);
                                ll_otherTagContainer.setVisibility(View.VISIBLE);
                                ll_seedTagContainer.setVisibility(View.GONE);
                            }
                        }, 200);
                        break;*/
                    default:
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                et_seedTag.setText(item);
                                et_bagCount.setEnabled(true);
                                chkbox_reject.setEnabled(true);
                                chkbox_reject.setChecked(false);
                                et_seedVariety.setText(rcepDatabase.tblDeliveryInspectionDAO()
                                        .getSeedVariety(glbl_batchTicketNumber, item));
                                et_bagCount.setText(String.valueOf(
                                        rcepDatabase.tblDeliveryInspectionDAO()
                                                .getBagCountPerSeedTag(glbl_batchTicketNumber,
                                                        item)));
                            }
                        }, 200);
                }
            }
        });
        ss.showSpinerDialog();
    }

    public void cancelOthers(View view) {
        Fun.hideKeyboard(this);
        et_labNo.setText("");
        et_lotNo.setText("");
        et_seedTag.setText("");
        et_labNo.clearFocus();
        et_lotNo.clearFocus();
        et_seedTag.clearFocus();
        ll_otherTagContainer.setVisibility(View.GONE);
        ll_seedTagContainer.setVisibility(View.VISIBLE);
    }

    public void closeImagePreview(View view) {
        ll_imgContainer.setVisibility(View.GONE);
    }

    public void setQRSeries(View view) {
        Fun.hideKeyboard(this);
        glbl_scan_mode = 1;
        /*content_frame.setVisibility(View.VISIBLE);
        actual_delivery_form.setVisibility(View.GONE);
        mCodeScanner.startPreview();*/
        barcodeLauncher.launch(new ScanOptions());
    }

    public void cancelScan(View view) {
       /* content_frame.setVisibility(View.GONE);
        mCodeScanner.stopPreview();
        mCodeScanner.releaseResources();
        actual_delivery_form.setVisibility(View.VISIBLE);*/
    }

    @Override
    protected void onPause() {
        super.onPause();
       /* content_frame.setVisibility(View.GONE);
        mCodeScanner.releaseResources();*/
    }

    public void setSackCode(View view) {
        Fun.hideKeyboard(this);
        glbl_scan_mode = 2;
        /*content_frame.setVisibility(View.VISIBLE);
        actual_delivery_form.setVisibility(View.GONE);
        mCodeScanner.startPreview();*/
        barcodeLauncher.launch(new ScanOptions());

    }

    public void setQRStart(View view) {

        Fun.hideKeyboard(this);
        glbl_scan_mode = 3;
        /*content_frame.setVisibility(View.VISIBLE);
        actual_delivery_form.setVisibility(View.GONE);
        mCodeScanner.startPreview();*/
        barcodeLauncher.launch(new ScanOptions());
    }

    public void setQREnd(View view) {
        if (et_qrStart.getText().toString().trim().isEmpty()) {
            Toast.makeText(InspectionActualDelivery.this,
                    "QR Start required",
                    Toast.LENGTH_SHORT).show();
        } else {
            Fun.hideKeyboard(this);
            glbl_scan_mode = 4;
           /* content_frame.setVisibility(View.VISIBLE);
            actual_delivery_form.setVisibility(View.GONE);
            mCodeScanner.startPreview();*/
            barcodeLauncher.launch(new ScanOptions());
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

    public void reject_rule(View view) {
        final AlertDialog dialogBuilder =
                new AlertDialog.Builder(InspectionActualDelivery.this).create();
        dialogBuilder.setCancelable(false);
        LayoutInflater inflater =
                LayoutInflater.from(InspectionActualDelivery.this);
        final View mView = inflater.inflate(R.layout.custom_reject_rule_dialog, null);
        //dialogViews
        final TextView mtv_dismiss = mView.findViewById(R.id.tv_dismiss);
        //others
        //dialogListeners
        mtv_dismiss.setOnClickListener(view12 -> {
            dialogBuilder.dismiss();
        });
        dialogBuilder.setView(mView);
        dialogBuilder.show();
    }


    public void setActualDateOfInspection(View view) {
        Fun.hideKeyboard(this);
        final SimpleDateFormat myDateFormat =
                new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        dateTimeDialogFragment = SwitchDateTimeDialogFragment.newInstance(
                "Actual Inspection Date",
                "Set",
                "Cancel",
                "Clear"
        );
        dateTimeDialogFragment.setTimeZone(TimeZone.getDefault());
        //dateTimeDialogFragment.set24HoursMode(true);
        //dateTimeDialogFragment.setMinimumDateTime(Calendar.getInstance().getTime());
        dateTimeDialogFragment.startAtCalendarView();
        dateTimeDialogFragment.setHighlightAMPMSelection(true);
        dateTimeDialogFragment.setOnButtonClickListener(
                new SwitchDateTimeDialogFragment.OnButtonWithNeutralClickListener() {

                    @Override
                    public void onPositiveButtonClick(Date date) {
                        et_actualDateofInspection.setText(myDateFormat.format(date));
                    }

                    @Override
                    public void onNegativeButtonClick(Date date) {
                        dateTimeDialogFragment.dismiss();
                    }

                    @Override
                    public void onNeutralButtonClick(Date date) {
                        et_actualDateofInspection.setText("");

                    }
                });
        dateTimeDialogFragment.show(getSupportFragmentManager(), "dialog_time");
    }

    public void setActualDateOfDelivery(View view) {
        Fun.hideKeyboard(this);
        final SimpleDateFormat myDateFormat =
                new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        dateTimeDialogFragment = SwitchDateTimeDialogFragment.newInstance(
                "Actual Delivery Date",
                "Set",
                "Cancel",
                "Clear"
        );
        dateTimeDialogFragment.setTimeZone(TimeZone.getDefault());
        //dateTimeDialogFragment.set24HoursMode(true);
        //dateTimeDialogFragment.setMinimumDateTime(Calendar.getInstance().getTime());
        dateTimeDialogFragment.startAtCalendarView();
        dateTimeDialogFragment.setHighlightAMPMSelection(true);
        dateTimeDialogFragment.setOnButtonClickListener(
                new SwitchDateTimeDialogFragment.OnButtonWithNeutralClickListener() {

                    @Override
                    public void onPositiveButtonClick(Date date) {
                        et_actualDateOfDelivery.setText(myDateFormat.format(date));
                    }

                    @Override
                    public void onNegativeButtonClick(Date date) {
                        dateTimeDialogFragment.dismiss();
                    }

                    @Override
                    public void onNeutralButtonClick(Date date) {
                        et_actualDateOfDelivery.setText("");

                    }
                });
        dateTimeDialogFragment.show(getSupportFragmentManager(), "dialog_time");
    }
}
