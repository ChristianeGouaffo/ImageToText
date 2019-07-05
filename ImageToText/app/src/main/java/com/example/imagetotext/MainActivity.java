package com.example.imagetotext;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;



import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class MainActivity extends AppCompatActivity {
    EditText mResultEt;
    ImageView mPreviewIv;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 400;
    private static final int IMAGE_PICK_GALLERY_CODE = 1000;
    private static final int IMAGE_PICK_CAMERA_CODE = 1001;
    String cameraPermission[];
    String storagePermission[];
    Uri image_uri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        ActionBar actionBar = getSupportActionBar();
        mResultEt = findViewById( R.id.resultEt );
        mPreviewIv = findViewById( R.id.imageIv );
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate( R.menu.menu_main, menu );
        return super.onCreateOptionsMenu( menu );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.addImage) {
            showImageImportDialog();

        }
        if (id == R.id.settings) {
            Toast.makeText( this, "Paramèttre", Toast.LENGTH_SHORT ).show();
        }
        return super.onOptionsItemSelected( item );
    }

    private void showImageImportDialog() {
        String[] items = {"Camera", "Gallery"};
        AlertDialog.Builder dialog = new AlertDialog.Builder( this );
        dialog.setTitle( "Choisir une photo" );
        dialog.setItems( items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    if (!checkCameraPermission()) {
                        requestCameraPermission();
                    } else {
                        pickCamera();
                    }
                }

                if (which == 1) {
                    if (!checkStoragePermission()) {
                        requestStotragePermission();
                    } else {
                        pickGallery();
                    }

                }
            }
        } );
        dialog.create().show();

    }

    private void pickCamera() {
        ContentValues values = new ContentValues();
        values.put( MediaStore.Images.Media.TITLE, "NewPic" );
        values.put( MediaStore.Images.Media.DESCRIPTION, "Image to text" );
        image_uri = getContentResolver().insert( MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values );
        Intent camreraIntent = new Intent( MediaStore.ACTION_IMAGE_CAPTURE );
        camreraIntent.putExtra( MediaStore.EXTRA_OUTPUT, image_uri );
        startActivityForResult( camreraIntent, IMAGE_PICK_CAMERA_CODE );
    }

    private void pickGallery() {
        Intent intent = new Intent( Intent.ACTION_PICK );
        intent.setType( "image/*" );
        startActivityForResult( intent, IMAGE_PICK_GALLERY_CODE );


    }

    private void requestStotragePermission() {
        ActivityCompat.requestPermissions( this, storagePermission, STORAGE_REQUEST_CODE );
    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission( this, Manifest.permission.WRITE_EXTERNAL_STORAGE ) == (PackageManager.PERMISSION_GRANTED);
        return result;

    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions( this, cameraPermission, CAMERA_REQUEST_CODE );
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission( this, Manifest.permission.CAMERA ) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission( this, Manifest.permission.WRITE_EXTERNAL_STORAGE ) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted) {
                        pickCamera();
                    } else {
                        Toast.makeText( this, "permission refusé", Toast.LENGTH_SHORT ).show();
                    }
                }
                break;
            case STORAGE_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted) {
                        pickGallery();
                    } else {
                        Toast.makeText( this, "permission refusé", Toast.LENGTH_SHORT ).show();
                    }
                }
                break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                CropImage.activity( data.getData() )
                        .setGuidelines( CropImageView.Guidelines.ON )
                        .start( this );

            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                CropImage.activity( image_uri )
                        .setGuidelines( CropImageView.Guidelines.ON )
                        .start( this );


            }

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult( data );
            if(resultCode  == RESULT_OK) {
                Uri resultUri = result.getUri();
                mPreviewIv.setImageURI( resultUri );
                BitmapDrawable bitmapDrawable = (BitmapDrawable)mPreviewIv.getDrawable();
                Bitmap bitmap = bitmapDrawable.getBitmap();
                TextRecognizer  recognizer = new  TextRecognizer.Builder(getApplicationContext()).build();
                if (!recognizer.isOperational()) {
                    Toast.makeText( this, "Erreur", Toast.LENGTH_SHORT ).show();
                }
                else {
                    Frame frame = new  Frame.Builder().setBitmap(bitmap).build();
                    SparseArray<TextBlock> items = recognizer.detect( frame );
                    StringBuilder stringBuilder = new StringBuilder(  );
                    for (int i=0; i<items.size(); i++){
                        TextBlock myItem = items.valueAt( i );
                        stringBuilder.append( myItem.getValue());
                        stringBuilder.append( "\n" );
                    }
                    mResultEt.setText( stringBuilder.toString() );
                }
            }
            else  if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                Exception error = result.getError();
                Toast.makeText( this, ""+error, Toast.LENGTH_SHORT ).show();
            }
        }
    }
}

