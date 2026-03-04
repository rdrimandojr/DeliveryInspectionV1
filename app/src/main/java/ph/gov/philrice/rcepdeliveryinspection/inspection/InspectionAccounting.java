package ph.gov.philrice.rcepdeliveryinspection.inspection;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

import ph.gov.philrice.rcepdeliveryinspection.Fun;
import ph.gov.philrice.rcepdeliveryinspection.R;
import ph.gov.philrice.rcepdeliveryinspection.dbase.database.RCEPDatabase;

public class InspectionAccounting extends AppCompatActivity {
    private static final String TAG = "InspectionAccounting";
    ProgressDialog mProgressDialog;
    ImageView iv_preview, iv_setDrDate;
    EditText et_drDate, et_drNumber;
    TextView tv_acctgInspectionImage, tv_batchTicketNumber;
    String glbl_imgName, glbl_imgName2, currentPhotoPath, glbl_batchTicketNumber;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    SharedPreferences prefUserAccount;
    RCEPDatabase rcepDatabase;
    private int mYear, mMonth, mDay;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fun.fullScreen(this);//fullScreen
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
//        getSupportActionBar().hide(); // hide the title bar
        setContentView(R.layout.activity_inspection_accounting);

        init();
    }

    private void init() {
        //vars
        rcepDatabase = RCEPDatabase.getAppDatabase(this);
        prefUserAccount = getApplicationContext()
                .getSharedPreferences(Fun.userAccountPreference(), Context.MODE_PRIVATE);

        glbl_batchTicketNumber = "";
        currentPhotoPath = "";
        glbl_imgName =
                prefUserAccount.getInt(Fun.uaUserId(), 0) + "-ACTG-" + Fun.getTimestamp();
        glbl_imgName2 = glbl_imgName + ".jpg";


        glbl_batchTicketNumber = getIntent().getStringExtra("batchTicketNumber");
        //views
        iv_setDrDate = findViewById(R.id.iv_setDrDate);
        et_drDate = findViewById(R.id.et_drDate);
        et_drNumber = findViewById(R.id.et_drNumber);
        iv_preview = findViewById(R.id.iv_preview);
        tv_acctgInspectionImage = findViewById(R.id.tv_acctgInspectionImage);
        tv_batchTicketNumber = findViewById(R.id.tv_batchTicketNumber);
        //listener
        iv_setDrDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get Current Date
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(InspectionAccounting.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                et_drDate.setText(
                                        year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });
        iv_preview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });
        //others
        tv_batchTicketNumber.setText(glbl_batchTicketNumber);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
           /* Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imgvw_preview.setImageBitmap(imageBitmap);*/
            tv_acctgInspectionImage.setText(glbl_imgName2);
            Toast.makeText(this, "Image Captured!", Toast.LENGTH_SHORT).show();
            /*File f = new File(currentPhotoPath);
            Uri contentUri = Uri.fromFile(f);*/

            setPic(iv_preview);
            //Picasso.get().load(new File(currentPhotoPath)).into(iv_preview);
        } else {
            Toast.makeText(this, "Image capture cancelled", Toast.LENGTH_SHORT).show();
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

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(InspectionAccounting.this,
                        "ph.gov.philrice.rcepdeliveryinspection",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
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

    public void save(View view) {


        String dr_date = et_drDate.getText().toString().trim();
        String dr_number = et_drNumber.getText().toString().trim();
        String img_name = tv_acctgInspectionImage.getText().toString().trim();

        if (!dr_date.isEmpty() && !dr_number.isEmpty() && !img_name.isEmpty()) {
            CountDownLatch latch = new CountDownLatch(1); // Number of tasks

            new Thread(() -> {
                rcepDatabase.tblInspectionDAO()
                        .updateAccountingAttachment(glbl_batchTicketNumber, img_name,
                                currentPhotoPath,
                                dr_date, dr_number);

                // Switch to the main thread to show the Toast
                runOnUiThread(() -> {
                    Toast.makeText(this, "Attachment saved!", Toast.LENGTH_SHORT).show();
                });
                latch.countDown(); // Mark as completed
            }).start();

            new Thread(() -> {
                try {
                    latch.await(); // Wait for both tasks to finish

                    Intent intent =
                            new Intent(InspectionAccounting.this, InspectionMainActivity.class);
                    intent.putExtra("batchTicketNumber", glbl_batchTicketNumber);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();


        } else {
            Toast.makeText(this, "Please complete required(*) fields", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        new MaterialAlertDialogBuilder(InspectionAccounting.this)
                .setTitle("RCEF DI")
                .setCancelable(false)
                .setMessage("Cancel batch " + glbl_batchTicketNumber + " inspection attachment?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(InspectionAccounting.this,
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


       /* CFAlertDialog.Builder builder =
                new CFAlertDialog.Builder(InspectionAccounting.this);
        builder.setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET);
        builder.setTitle("RCEP App");
        builder.setMessage("Cancel batch " + glbl_batchTicketNumber + " inspection attachment?");
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

                                Intent intent = new Intent(InspectionAccounting.this,
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
}