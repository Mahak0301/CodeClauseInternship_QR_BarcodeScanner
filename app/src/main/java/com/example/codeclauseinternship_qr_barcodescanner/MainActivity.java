package com.example.codeclauseinternship_qr_barcodescanner;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PackageManagerCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.MediaParser;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import org.w3c.dom.Text;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private MaterialButton cameraBtn;
    private MaterialButton galleryBtn;
    private ImageView image;
    private MaterialButton scanBtn;
    private TextView result;
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 101;

    private String[] cameraPermissions;
    private String[] storagePermissions;
    private Uri imageUri = null;
    private static final String TAG = "MAIN_TAG";
    private BarcodeScannerOptions barcodeScannerOptions;
    private BarcodeScanner barcodeScanner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraBtn = findViewById(R.id.cameraBtn);
        galleryBtn = findViewById(R.id.galleryBtn);
        image = findViewById(R.id.image);
        scanBtn = findViewById(R.id.scanBtn);
        result = findViewById(R.id.result);
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        barcodeScannerOptions=new BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS).build();
        barcodeScanner= BarcodeScanning.getClient(barcodeScannerOptions);

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkCameraPermission())
                pickImageCamera();
                else
                    requestCameraPermission();
            }
        });
        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkStoragePermisssion()){
                    pickImageGallery();
                }
                else{
                    requestStoragePermission();
                }
            }
        });
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(imageUri==null){
                    Toast.makeText(MainActivity.this, "Pick an Image..", Toast.LENGTH_SHORT).show();
                }
                else{
                    detectResult();
                }
            }
        });
    }

    private void detectResult() {
        try{
            InputImage inputImage=InputImage.fromFilePath(this,imageUri);
            Task<List<Barcode>> barcodeResult= barcodeScanner.process(inputImage).addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                @Override
                public void onSuccess(List<Barcode> barcodes) {
                    extractInfo(barcodes);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, "Failed to scan", Toast.LENGTH_SHORT).show();
                }
            });
        }
        catch (Exception e){
            Toast.makeText(this, "Failed to scan", Toast.LENGTH_SHORT).show();
        }
    }

    private void extractInfo(List<Barcode> barcodes) {
        for(Barcode barcode: barcodes){
            Rect bounds=barcode.getBoundingBox();
            Point[] corners=barcode.getCornerPoints();
            String rawValue=barcode.getRawValue();
            Log.d(TAG,"extractBarCodeQRCodeInfo: rawValue: "+rawValue);
            int valueType = barcode.getValueType();
            switch(valueType){
                case Barcode.TYPE_WIFI:{
                    Barcode.WiFi typeWifi=barcode.getWifi();
                    String ssid=typeWifi.getSsid();
                    String password=typeWifi.getPassword();
                    String encryptionType=  ""+typeWifi.getEncryptionType();
                    Log.d(TAG,"extractBarCodeQRCodeInfo: TYPE_WIFI");
                    Log.d(TAG,"extractBarCodeQRCodeInfo: ssid" + ssid);
                    Log.d(TAG,"extractBarCodeQRCodeInfo: password" + password);
                    Log.d(TAG,"extractBarCodeQRCodeInfo: encryption type" + encryptionType);
                    result.setText("TYPE:TYPE_WIFI \nssid:"+ssid+"\n[password:"+password+"\nencryption Type"+encryptionType+"\nraw value:"+rawValue);
                }
                break;
                case Barcode.TYPE_URL:{
                    Barcode.UrlBookmark typeUrl= barcode.getUrl();
                    String title=typeUrl.getTitle();
                    String url=""+typeUrl.getUrl();
                    Log.d(TAG,"extractBarCodeQRCodeInfo: TYPE_URL");
                    Log.d(TAG,"extractBarCodeQRCodeInfo: title" + title);
                    Log.d(TAG,"extractBarCodeQRCodeInfo: URL" + url);
                    result.setText("TYPE: TYPE_URl \ntitle: "+title+"\nURL:"+url+"\nRaw value: "+rawValue);
                }
                break;
                case Barcode.TYPE_EMAIL:{
                    Barcode.Email typeEmail= barcode.getEmail();
                    String address=typeEmail.getAddress();
                    String body= ""+typeEmail.getBody();
                    String subject= ""+typeEmail.getSubject();
                    Log.d(TAG,"extractBarCodeQRCodeInfo: TYPE_EMAIL");
                    Log.d(TAG,"extractBarCodeQRCodeInfo: address : "+address );
                    Log.d(TAG,"extractBarCodeQRCodeInfo: body: " +body);
                    Log.d(TAG,"extractBarCodeQRCodeInfo: subject: "+subject);
                    result.setText("TYPE: TYPE_EMAIL \n Address: "+address+"\nBody: "+body+"\nSubject:"+subject+"\nRawValue: "+rawValue);
                }
                break;
                case Barcode.TYPE_CONTACT_INFO:{
                    Barcode.ContactInfo typeContact= barcode.getContactInfo();
                    String title= ""+typeContact.getTitle();
                    String organization= ""+typeContact.getOrganization();
                    String name= ""+typeContact.getName().getFirst()+" "+typeContact.getName().getLast();
                    String phone= ""+typeContact.getPhones().get(0).getNumber();
                    Log.d(TAG,"extractBarCodeQRCodeInfo: TYPE_CONTACT_INFO");
                    Log.d(TAG,"extractBarCodeQRCodeInfo: title" + title);
                    Log.d(TAG,"extractBarCodeQRCodeInfo: organization" + organization);
                    Log.d(TAG,"extractBarCodeQRCodeInfo: name" + name);
                    Log.d(TAG,"extractBarCodeQRCodeInfo: Phone" + phone);
                    result.setText("TYPE: TYPE_CONTACT_INFO \n Title: "+title+"\nOrganization: "+organization+"\nName:"+name+"\nPhone:"+phone+"\nRawValue: "+rawValue);
                }
                break;
                default:{
                    result.setText("raw value: "+rawValue);
                }
            }
        }
    }

    private void pickImageGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        imageUri = data.getData();
                        Log.d(TAG, "onActivityResult: imageUri:" + imageUri);
                        image.setImageURI(imageUri);
                    } else {
                        Toast.makeText(MainActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private void pickImageCamera() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Sample Image Description");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        imageUri = data.getData();
                        Log.d(TAG, "onActivityResult: imageUri:" + imageUri);
                        image.setImageURI(imageUri);
                    } else {
                        Toast.makeText(MainActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private boolean checkStoragePermisssion() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        return result;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean resultStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        return result && resultStorage;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted) {
                        pickImageCamera();
                    } else {
                        Toast.makeText(this, "Camera And Storage permissions are required", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        pickImageGallery();
                    } else {
                        Toast.makeText(this, "Storage permissions are required", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            }
        }
    }
}