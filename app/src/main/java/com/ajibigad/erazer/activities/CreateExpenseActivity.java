package com.ajibigad.erazer.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.ajibigad.erazer.R;
import com.ajibigad.erazer.data.Expense;
import com.ajibigad.erazer.service.ErazerIntentService;
import com.ajibigad.erazer.utils.ImageUtils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CreateExpenseActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_PICK_FROM_GALLERY = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String TAG = CreateExpenseActivity.class.getSimpleName();
    @BindViews({R.id.input_title, R.id.input_description, R.id.input_cost})
    public List<TextInputEditText> editTexts;

    @BindViews({R.id.input_layout_title, R.id.input_layout_description, R.id.input_layout_cost})
    public List<TextInputLayout> textInputLayouts;

    @BindView(R.id.btn_submit)
    public Button btnSubmit;

    @BindView(R.id.proof_type_spinner)
    public Spinner proofTypeSpinner;

    @BindView(R.id.input_proof_description)
    public TextInputEditText etProofDescription;

    @BindView(R.id.input_layout_proof_description)
    public TextInputLayout tlProofDescription;

    @BindView(R.id.btn_select_proof_image)
    public Button btnSelectProofImage;

    @BindView(R.id.iv_proof_image)
    ImageView ivProofImage;

    @BindView(R.id.proof_image_layout)
    View proofImageLayout;

    private final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;

    private ArrayAdapter<CharSequence> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_expense);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);

        adapter = ArrayAdapter.createFromResource(this,
                R.array.proof_type_array, R.layout.support_simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

        proofTypeSpinner.setAdapter(adapter);
        proofTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String proofType = adapter.getItem(position).toString();
                handleSelectedProofType(Expense.PROOF_TYPE.valueOf(proofType));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private Expense composeNewExpense() {
        Expense expense = new Expense();
        expense.setTitle(editTexts.get(0).getText().toString());
        expense.setDescription(editTexts.get(1).getText().toString());
        expense.setCost(Double.valueOf(editTexts.get(2).getText().toString()));
        expense.setState(Expense.STATE.PENDING);
        expense.setProofType(Expense.PROOF_TYPE.valueOf((String) proofTypeSpinner.getSelectedItem()));
        switch (expense.getProofType()) {
            case EMAIL:
            case TEXT:
                expense.setProof(etProofDescription.getText().toString());
                break;
            case IMAGE:
                expense.setProof((String) ivProofImage.getTag(R.id.iv_proof_image));//base64 encoding of the image
                break;
        }
        return expense;
    }

    @OnClick(R.id.btn_submit)
    public void submitExpense() {
        if (!validateTextFields()) {
            return;
        }

        Expense expense = composeNewExpense();

        Toast.makeText(this, "Saving expense", Toast.LENGTH_SHORT).show();
        ErazerIntentService.startActionCreateExpense(this, expense);
        finish();
    }

    @OnClick(R.id.btn_select_proof_image)
    public void selectProofImage() {
        //open dialog to select from gallery or camera
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final String[] options = {"Take from gallery", "Take with Camera"};
        builder.setTitle(R.string.select_picture)
                .setSingleChoiceItems(options, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handleImageSelectionOption(which);
                        dialog.dismiss();
                    }
                }).create().show();
        //get image and display in the image view
        //display full image when image view is clicked
        //base54 encode the image and add it to the expense as proof description
        //this would be converted to a url when it gets to the backend
    }

    private Expense.PROOF_TYPE getSelectedProofType() {
        return Expense.PROOF_TYPE.valueOf(adapter.getItem(proofTypeSpinner.getSelectedItemPosition()).toString());
    }

    private void handleSelectedProofType(Expense.PROOF_TYPE proofType) {
        switch (proofType) {
            case EMAIL:
                etProofDescription.setText(proofType.name());
                proofImageLayout.setVisibility(View.INVISIBLE);
                tlProofDescription.setVisibility(View.INVISIBLE);
                break;
            case IMAGE:
                proofImageLayout.setVisibility(View.VISIBLE);
                tlProofDescription.setVisibility(View.INVISIBLE);
                break;
            case TEXT:
                etProofDescription.setText("");
                tlProofDescription.setVisibility(View.VISIBLE);
                proofImageLayout.setVisibility(View.INVISIBLE);
        }
    }

    private void handleImageSelectionOption(int selectedOption) {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermission();
        } else {
            switch (selectedOption) {
                case REQUEST_IMAGE_PICK_FROM_GALLERY:
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto, REQUEST_IMAGE_PICK_FROM_GALLERY);
                    break;
                case REQUEST_IMAGE_CAPTURE:
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        try {
                            selectedImageFile = createTempImageFile();
                        } catch (IOException ex) {
                            // Error occurred while creating the File
                            Log.e(TAG, "IO Exception while creating temp Image File directory");
                            ex.printStackTrace();
                        }
                        // Continue only if the File was successfully created
                        if (selectedImageFile != null) {
                            Uri photoURI = FileProvider.getUriForFile(this,
                                    getString(R.string.file_provider_authority),
                                    selectedImageFile);
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                        }
                    }
                    break;
            }
        }
    }

    @OnClick(R.id.iv_proof_image)
    public void displayProofImageFullScreen(ImageView proofImage) {
        //best to past the uri to another activity that would show it in full screen
    }

    private boolean validateTextFields() {
        int count = 0;
        for (TextInputLayout textInputLayout : textInputLayouts) {
            if (editTexts.get(count).getText().toString().trim().isEmpty()) {
                textInputLayout.setError(getString(R.string.blank_field_msg));
                requestFocus(editTexts.get(count));
                return false;
            } else {
                textInputLayout.setErrorEnabled(false);
            }
            count++;
        }

        if (etProofDescription.getText().toString().trim().isEmpty() && getSelectedProofType().compareTo(Expense.PROOF_TYPE.IMAGE) != 0) {
            tlProofDescription.setError(getString(R.string.blank_field_msg));
            requestFocus(etProofDescription);
            return false;
        } else {
            tlProofDescription.setErrorEnabled(false);
        }

        return true;
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private void requestPermission() {
        // Should we show an explanation?
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {

            showMessageOKCancel("You need to allow access to sd card",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(CreateExpenseActivity.this,
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                    PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                        }
                    });
            return;

        } else {

            // No explanation needed, we can request the permission.

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_IMAGE_PICK_FROM_GALLERY:
            case REQUEST_IMAGE_CAPTURE:
                if (resultCode == RESULT_OK) {
                    if (selectedImageFile == null) {
                        Log.w(TAG, "Could not get selected image");
                        Toast.makeText(this, "Could not get selected image\n Pls try again", Toast.LENGTH_LONG).show();
                        return;
                    }

                    try {
                        Uri selectedImageFileUri = Uri.fromFile(selectedImageFile);
                        Picasso.with(this).load(selectedImageFileUri).into(ivProofImage);
                        ivProofImage.setTag(R.id.iv_proof_image, ImageUtils.convertImageToBase64(this, selectedImageFileUri));
                        selectedImageFile.delete();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Could not get selected image\n Pls try again", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    File selectedImageFile;

    private File createTempImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(getFilesDir(), "images");
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        return image;
    }
}
