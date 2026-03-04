package ph.gov.philrice.rcepdeliveryinspection.inspection;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.ortiz.touchview.TouchImageView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import in.galaxyofandroid.spinerdialog.OnSpinerItemClick;
import in.galaxyofandroid.spinerdialog.SpinnerDialog;
import ph.gov.philrice.rcepdeliveryinspection.BuildConfig;
import ph.gov.philrice.rcepdeliveryinspection.Fun;
import ph.gov.philrice.rcepdeliveryinspection.R;
import ph.gov.philrice.rcepdeliveryinspection.dbase.database.RCEPDatabase;
import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.SeedTagBag;
import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TblTempSampling;
import ph.gov.philrice.rcepdeliveryinspection.inspection.dataview.SamplingNewAdapter;
import ph.gov.philrice.rcepdeliveryinspection.inspection.dataview.SamplingNewData;

public class InspectionNewSamplingActivity extends AppCompatActivity
        implements SamplingNewAdapter.ItemClicked {
    private static final String TAG = "InspectionNewSamplingAc";
    //Views
    ProgressDialog mProgressDialog;
    TextView tv_batchTicketNumber, tv_samplingLeft, tv_average;
    EditText et_samplingInspectionImage, et_labNo, et_lotNo, et_packageWeight, et_seedTag;
    RecyclerView rv_inspection;
    LinearLayout ll_imgContainer, ll_seedTagContainer, ll_otherTagContainer, ll_samplingInfo;
    ImageView iv_preview;
    //Variables
    SharedPreferences prefUserAccount;
    SharedPreferences prefThreshold;
    SharedPreferences prefTempInspectionData;
    int glbl_limit;
    int glbl_seedtag_limit;
    String glbl_batchTicketNumber;
    /* ArrayList<String> seedTags;*/
    ArrayList<SamplingNewData> samplingNewData;
    SamplingNewAdapter samplingNewAdapter;
    RCEPDatabase rcepDatabase;
    String glbl_imgName;
    String glbl_imgName2;
    private File file;


    static final int REQUEST_IMAGE_CAPTURE = 1;
    String currentPhotoPath;

    ImageView imgvw_capture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //initialize screen
        Fun.fullScreen(this);//fullScreen
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
//        getSupportActionBar().hide(); // hide the title bar
        setContentView(R.layout.activity_inspection_new_sampling);

        initInspectionNewSampling();
    }

    private void showSeedTagCountsDialog(Context context,
                                         ArrayList<SamplingNewData> samplingNewData,
                                         List<SeedTagBag> seedTagBags) {
        // Step 1: Initialize tag counts based on seedTagBags
        HashMap<String, Integer> tagCounts = new HashMap<>();
        HashMap<String, Integer> tagLimits = new HashMap<>();
        for (SeedTagBag bag : seedTagBags) {
            tagCounts.put(bag.getSeedTag(), 0);
            tagLimits.put(bag.getSeedTag(), bag.getSamplingLimit());
        }

        // Step 2: Count per tag up to its sampling limit
        for (SamplingNewData data : samplingNewData) {
            String tag = data.getSeedTag();
            if (tagCounts.containsKey(tag)) {
                int currentCount = tagCounts.get(tag);
                int limit = tagLimits.getOrDefault(tag, 0);
                if (currentCount < limit) {
                    tagCounts.put(tag, currentCount + 1);
                }
            }
        }

        // Step 3: Build message
        StringBuilder message = new StringBuilder();
        for (SeedTagBag bag : seedTagBags) {
            String tag = bag.getSeedTag();
            int limit = bag.getSamplingLimit();
            int count = tagCounts.getOrDefault(tag, 0);

            if (count >= limit) {
                // ✅ Mark tag as completed
                message.append("✔ ")
                        .append(String.format("%-10s ... %2d/%d", tag, count, limit))
                        .append(" (Done)\n");
            } else {
                // Not yet completed
                message.append("✖ ")
                        .append(String.format("%-10s ... %2d/%d", tag, count, limit))
                        .append("\n");
            }
        }

        if (seedTagBags.isEmpty()) {
            message.append("No seed tags available.");
        }

        Log.e("SeedTagCount", "showSeedTagCountsDialog:\n" + message);

        // Step 4: Create scrollable TextView
        TextView textView = new TextView(context);
        textView.setText(message.toString());
        textView.setPadding(48, 48, 48, 48); // more spacious padding (left, top, right, bottom)
        textView.setTextSize(16);
        textView.setLineSpacing(8f, 1.2f); // adds spacing between lines
        textView.setMovementMethod(new ScrollingMovementMethod());

        ScrollView scrollView = new ScrollView(context);
        scrollView.addView(textView);

        // Step 5: Show dialog with scrollable content
        new AlertDialog.Builder(context)
                .setTitle("Sampling Details")
                .setView(scrollView)
                .setPositiveButton("OK", null)
                .show();
    }

    void initInspectionNewSampling() {
        rcepDatabase = RCEPDatabase.getAppDatabase(this);

        prefUserAccount = getApplicationContext()
                .getSharedPreferences(Fun.userAccountPreference(), Context.MODE_PRIVATE);
        prefThreshold = getApplicationContext()
                .getSharedPreferences(Fun.thresholdPreference(), Context.MODE_PRIVATE);
        prefTempInspectionData = getApplicationContext()
                .getSharedPreferences(Fun.tempInspectionPreference(), Context.MODE_PRIVATE);
        /*   seedTags = new ArrayList<>();*/
        glbl_batchTicketNumber = prefTempInspectionData.getString(Fun.tiBatchTicketNumber(), "");
        currentPhotoPath = "";
        glbl_seedtag_limit = 0;

        // Apply rule (min(total, 10))
        int sum = 0;
        Map<String, Integer> computed = new HashMap<>();

        List<SeedTagBag> results = rcepDatabase.tblDeliveryInspectionDAO()
                .getSeedTagsByBatch2(glbl_batchTicketNumber);

        Log.e(TAG, "SeedTagBag: " + results.toString());

        for (SeedTagBag row : results) {
            int value = Math.min(row.getTotalBagCount(), 10);
            computed.put(row.getSeedTag(), value);
            sum += value;
        }

        glbl_limit = sum;


       /* glbl_limit =
                rcepDatabase.tblasdDeliveryInspectionDAO()
                        .getSeedTagsByBatch(glbl_batchTicketNumber)
                        .size() * 10;*/

        glbl_imgName =
                prefUserAccount.getInt(Fun.uaUserId(), 0) + "-SMP-" + Fun.getTimestamp();
        glbl_imgName2 = glbl_imgName + ".jpg";

        Log.e(TAG, "glbl_imgName2: " + glbl_imgName2);

        samplingNewData = new ArrayList<>();

        //prefTempSampling = getApplicationContext().getSharedPreferences(Fun.tempSamplingData(), Context.MODE_PRIVATE);
        mProgressDialog = new ProgressDialog(this);
        imgvw_capture = findViewById(R.id.imgvw_capture);
        ll_samplingInfo = findViewById(R.id.ll_samplingInfo);
        ll_otherTagContainer = findViewById(R.id.ll_otherTagContainer);
        ll_seedTagContainer = findViewById(R.id.ll_seedTagContainer);
        et_seedTag = findViewById(R.id.et_seedTag);
        iv_preview = findViewById(R.id.iv_preview);
        ll_imgContainer = findViewById(R.id.ll_imgContainer);
        tv_batchTicketNumber = findViewById(R.id.tv_batchTicketNumber);
        tv_samplingLeft = findViewById(R.id.tv_samplingLeft);
        tv_average = findViewById(R.id.tv_average);
        et_samplingInspectionImage = findViewById(R.id.et_samplingInspectionImage);
        et_labNo = findViewById(R.id.et_labNo);
        et_lotNo = findViewById(R.id.et_lotNo);
        et_packageWeight = findViewById(R.id.et_packageWeight);
        rv_inspection = findViewById(R.id.rv_inspection);

        tv_batchTicketNumber.setText(glbl_batchTicketNumber);
        tv_samplingLeft.setText(String.valueOf(samplingLeft()));
        tv_average.setText(String.valueOf(0.0));

        /*tv_average.setText(String.valueOf(Math.floor(samplingAverage(samplingNewData) * 100) / 100));*/

        //set properties
        rv_inspection.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rv_inspection.setItemAnimator(new DefaultItemAnimator());
        samplingNewAdapter = new SamplingNewAdapter(this, samplingNewData);
        samplingNewAdapter.setitemClickedListener(this);

        rv_inspection.setAdapter(samplingNewAdapter);

        ll_samplingInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Toast.makeText(InspectionNewSamplingActivity.this, "yawaaa", Toast.LENGTH_SHORT).show();
                List<String> allSeedTags = getSeedTagsAll(glbl_batchTicketNumber);
                /*for (int i = 1; i <= 40; i++) {
                    allSeedTags.add("TAG-" + String.format("%03d", i)); // e.g., TAG-001 to TAG-040
                }*/

                if (samplingNewAdapter == null || samplingNewAdapter.getOriginalList() == null) {
                    Toast.makeText(InspectionNewSamplingActivity.this, "No data available",
                                    Toast.LENGTH_SHORT)
                            .show();
                } else {
                    showSeedTagCountsDialog(InspectionNewSamplingActivity.this,
                            (ArrayList<SamplingNewData>) samplingNewAdapter.getOriginalList(),
                            rcepDatabase.tblDeliveryInspectionDAO()
                                    .getSeedTagsByBatch2(glbl_batchTicketNumber));
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
                if (currentPhotoPath.isEmpty()) {
                    Toast.makeText(InspectionNewSamplingActivity.this, "No image to display",
                                    Toast.LENGTH_SHORT)
                            .show();
                } else {
                    final AlertDialog dialogBuilder =
                            new AlertDialog.Builder(InspectionNewSamplingActivity.this).create();
                    dialogBuilder.setCancelable(true);
                    LayoutInflater inflater =
                            LayoutInflater.from(InspectionNewSamplingActivity.this);
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

    }

    private ArrayList getSeedTags(String batchTicketNumber) {
        ArrayList<String> arrayList = new ArrayList<>();
        //first value in array
        //arrayList.add("Clear selected");
        //arrayList.add("Others");
        //assigning list values from local dbase
        List<String> source =
                rcepDatabase.tblDeliveryInspectionDAO().getSeedTagsByBatch(batchTicketNumber);
        arrayList.addAll(source);
        return arrayList;
    }

    private ArrayList<String> getSeedTagsAll(String batchTicketNumber) {
        ArrayList<String> seedTags = new ArrayList<>();
        if (batchTicketNumber == null || batchTicketNumber.isEmpty()) {
            return seedTags; // return empty list if input is invalid
        }
        List<String> source =
                rcepDatabase.tblDeliveryInspectionDAO().getSeedTagsByBatch(batchTicketNumber);

        if (source != null && !source.isEmpty()) {
            seedTags.addAll(source);
        }

        return seedTags;
    }


    /* int samplingLeft() {
         Log.e(TAG, "samplingLeft: " + samplingNewData.size());
         return glbl_limit - samplingNewAdapter.getOriginalListSize();
     }*/
    int samplingLeft() {
        if (samplingNewAdapter == null) {
            Log.e(TAG, "samplingNewAdapter is null in samplingLeft()");
            return glbl_limit; // or return 0, or handle as needed
        }
        return glbl_limit - samplingNewAdapter.getOriginalListSize();
    }


    double samplingAverage(ArrayList<SamplingNewData> source) {
        double totalWeight = 0;
        double mAve = 0;
        if (source.size() > 0) {
            for (SamplingNewData s : source) {
                totalWeight += s.getBagWeight();
            }
            mAve = totalWeight / source.size();
            //two decimal place truncate
            mAve = Fun.toTwoDecimal(mAve);
        }
        return mAve;
    }

    public void nextBag(View view) {

        if (samplingLeft() == 0) {
            Fun.hideKeyboard(this);
            Toast.makeText(this, "Package sampling already completed.", Toast.LENGTH_SHORT).show();

        } else {

            if (verify() > 0) {
                Toast.makeText(this, "Please complete details of package before moving to next bag",
                        Toast.LENGTH_SHORT).show();
            } else {
                //Fun.hideKeyboard(this);
                String mBatchTicketNumber = glbl_batchTicketNumber;
                String seedTag;
                String mSeedTag = et_seedTag.getText().toString().trim().toLowerCase();
                String labNo = et_labNo.getText().toString().trim().toUpperCase();
                String lotNo = et_lotNo.getText().toString().trim().toUpperCase();
                String mAppVersion = Fun.appVersion();
                int mSendLocal = 1;
                int mSendCentral = 1;


                /*long count = samplingNewData.stream()
                        .filter(data -> mSeedTag.equalsIgnoreCase(data.getSeedTag()))
                        .count();*/

                int seedTagCounter = samplingNewAdapter.getFilteredList().size();

                if (seedTagCounter >= glbl_seedtag_limit) {
                    Toast.makeText(InspectionNewSamplingActivity.this,
                            mSeedTag + " already has (" + glbl_seedtag_limit +
                                    ") entries. See sampling info for details",
                            Toast.LENGTH_LONG).show();
                    return;
                }

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
                float mBagWeight = 0;
                if (et_packageWeight.getText().length() > 0) {
                    if (et_packageWeight.getText().toString().trim().equals(".")) {
                        mBagWeight = 0;
                    } else {
                        mBagWeight = Float.parseFloat(et_packageWeight.getText().toString());
                    }
                }
                String mDateSampled = Fun.getCurrentDate();
                int mSend = 1;

                if (mBagWeight > 0) {
                    SamplingNewData data =
                            new SamplingNewData(mBatchTicketNumber, seedTag, mBagWeight,
                                    mDateSampled, mSend, mAppVersion, mSendLocal, mSendCentral);
                    //samplingNewData.add(0, data);
                    //samplingNewAdapter.notifyDataSetChanged();

                    /*if (samplingNewAdapter.getItemCount() == 10) {
                        Toast.makeText(this, "Already completed sampling in seed tag " + mSeedTag,
                                Toast.LENGTH_LONG).show();
                    } else {*/
                        samplingNewAdapter.addItem(data);
                        tv_samplingLeft.setText(String.valueOf(samplingLeft()));
                        tv_average.setText(String.valueOf(samplingAverage(
                                (ArrayList<SamplingNewData>) samplingNewAdapter.getFilteredList())));
                        reset();


                        Log.e(TAG, mSeedTag + "->" + seedTagCounter + "/" + glbl_seedtag_limit);
                        Log.e(TAG, "samplingnewData-> " +
                                samplingNewAdapter.getFilteredList().toString());

                    //}


                } else {
                    Toast.makeText(this, "Package weight must be greater than 0",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    int verify() {
        int verify = 0;
        String seedTag;

      /*  if (et_packageWeight.getText().toString().trim().equals("")) {
            verify++;
        }*/
        String mSeedTag = et_seedTag.getText().toString().trim().toLowerCase();
        String labNo = et_labNo.getText().toString().trim().toUpperCase();
        String lotNo = et_lotNo.getText().toString().trim().toUpperCase();

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

        if (seedTag.equals("")) {
            verify++;
        }


        return verify;
    }

    void reset() {
        et_labNo.setText("");
        et_lotNo.setText("");
        //et_seedTag.setText("");
        et_packageWeight.setText("");
        ll_otherTagContainer.setVisibility(View.GONE);
        ll_seedTagContainer.setVisibility(View.VISIBLE);

        clearFocus();

        //samplingNewAdapter.filterBySeedTag(et_seedTag.getText().toString());
    }

    void clearFocus() {
        et_packageWeight.clearFocus();
        et_lotNo.clearFocus();
        et_labNo.clearFocus();
        et_seedTag.clearFocus();
    }

    public void reject(View view) {
        //reject sampling
    }

    public void passed(View view) {
        //checking required requirements
        //  !samplingNewData.isEmpty() &&
        if (samplingNewAdapter == null || samplingNewAdapter.getOriginalListSize() == 0) {
            Toast.makeText(this, "Please complete sampling requirements", Toast.LENGTH_SHORT)
                    .show();
            return;
        }


        if (samplingNewAdapter.getOriginalListSize() == glbl_limit &&
                !et_samplingInspectionImage.getText().toString().trim().isEmpty()) {
            //checking if sampling have met expected sampling average
            //if (samplingAverage(samplingNewData) >= Fun.reqSamplingAverage()) {
            SharedPreferences.Editor editor = prefTempInspectionData.edit();
            editor.putString(Fun.tiTempSampling(),
                    Fun.jsonTempSamplingNewData(samplingNewData));
            editor.putInt(Fun.tiSamplingPassed(), 1);
            editor.putString(Fun.tiSamplingImage(),
                    et_samplingInspectionImage.getText().toString().trim());
            editor.putString(Fun.tiSamplingImagePath(),
                    currentPhotoPath);
            editor.putInt(Fun.tiTempDeliveryStatus(), 1);
            //apply values
            editor.apply();
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //inserting data to tbl_temp_sampling
                    //if (samplingNewData.size() > 0) {
                    for (SamplingNewData s : samplingNewAdapter.getOriginalList()) {
                        TblTempSampling tmpSamplingdata =
                                new TblTempSampling(s.getBatchTicketNumber(),
                                        s.getSeedTag(), s.getBagWeight(),
                                        s.getDateSampled(), s.getSend());
                        rcepDatabase.tblTempSamplingDAO()
                                .insertTempSampling(tmpSamplingdata);
                    }
                    //}
                    Intent intent = new Intent(InspectionNewSamplingActivity.this,
                            InspectionActualDelivery.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            }, 1500);

        } else {
            Toast.makeText(this,
                    "Please complete sampling requirements and capture image before proceeding to next step",
                    Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    public void onDelete(final int position) {
        Fun.progressStart(mProgressDialog, "", "Removing sample from list");
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Fun.progressStop(mProgressDialog);
                //proceed verification screen
                //remove selected sample
                Fun.hideKeyboard(InspectionNewSamplingActivity.this);
                clearFocus();
                //samplingNewData.remove(position);
                samplingNewAdapter.removeItem(position);
                //updates sampling list
                samplingNewAdapter.notifyDataSetChanged();
                tv_samplingLeft.setText(String.valueOf(samplingLeft()));
                /*tv_average.setText(String.valueOf(Fun.roundAvoid(samplingAverage(samplingNewData), 2)));
                 */
                tv_average.setText(String.valueOf(samplingAverage(samplingNewData)));

            }
        }, 250);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
           /* Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imgvw_preview.setImageBitmap(imageBitmap);*/
            et_samplingInspectionImage.setText(glbl_imgName2);
            Toast.makeText(this, "Image Captured!", Toast.LENGTH_SHORT).show();
            /*File f = new File(currentPhotoPath);
            Uri contentUri = Uri.fromFile(f);*/

            setPic(iv_preview);
        } else {
            Toast.makeText(this, "Image capture cancelled", Toast.LENGTH_SHORT).show();
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
//                Log.e(TAG, "Error creating file: ", ex);
//                Toast.makeText(this, "Could not create image file", Toast.LENGTH_SHORT).show();
//                return;
//            }
//            // Continue only if the File was successfully created
//            if (photoFile != null) {
//                Uri photoURI = FileProvider.getUriForFile(InspectionNewSamplingActivity.this,
//                        BuildConfig.APPLICATION_ID + ".provider",
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


    }


    @Override
    public void onBackPressed() {
        new MaterialAlertDialogBuilder(InspectionNewSamplingActivity.this)
                .setTitle("RCEF DI")
                .setCancelable(false)
                .setMessage("Cancel batch " + glbl_batchTicketNumber + " inspection?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                Intent intent = new Intent(InspectionNewSamplingActivity.this,
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

    }

    public void closeImagePreview(View view) {
        ll_imgContainer.setVisibility(View.GONE);
    }

    public void setSeedTag(View view) {
        final Handler handler = new Handler();
        SpinnerDialog ss = new SpinnerDialog(InspectionNewSamplingActivity.this,
                getSeedTags(glbl_batchTicketNumber), "Select Variety");
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
                                samplingNewAdapter.applyFilter("");
                            }
                        }, 200);
                        break;
                    default:
                        int bagCount = rcepDatabase.tblDeliveryInspectionDAO()
                                .getBagCountPerSeedTag(glbl_batchTicketNumber, item);
                        glbl_seedtag_limit = Math.min(bagCount, 10);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                et_seedTag.setText(item);
                                samplingNewAdapter.applyFilter(item);

                            }
                        }, 100);
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
}
