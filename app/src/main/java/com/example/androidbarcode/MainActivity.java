package com.example.androidbarcode;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE=101;
    private static final int FILE_SHARE_PERMISSION = 102;
    private TextView textView;
    private ImageView barcode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

         barcode=findViewById(R.id.bar_code);
        textView=findViewById(R.id.data_text);

        String data_in_code="Hello Bar Code Data";
        MultiFormatWriter multiFormatWriter=new MultiFormatWriter();
        try{
            BitMatrix bitMatrix=multiFormatWriter.encode(data_in_code, BarcodeFormat.QR_CODE,200,200);
            BarcodeEncoder barcodeEncoder=new BarcodeEncoder();
            Bitmap bitmap=barcodeEncoder.createBitmap(bitMatrix);
            barcode.setImageBitmap(bitmap);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        //now let's create barcode scanner

        Button scan_code=findViewById(R.id.button_scan);
        scan_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT>=23){
                    if(checkPermission(Manifest.permission.CAMERA)){
                        openScanner();
                    }
                    else{
                        requestPermission(Manifest.permission.CAMERA,CAMERA_PERMISSION_CODE);
                    }
                }
                else{
                    openScanner();
                }
            }
        });
        
        //now let's share qr code
        Button share_code=findViewById(R.id.share_code);
        share_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT>=23){
                    if(checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                        shareQrCode();
                    }
                    else{
                        requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,FILE_SHARE_PERMISSION);
                    }
                }
                else{
                    shareQrCode();
                }
            }
        });
    }

    private void shareQrCode() {
        //create a file provider
        barcode.setDrawingCacheEnabled(true);
        Bitmap bitmap=barcode.getDrawingCache();
        File file=new File(Environment.getExternalStorageDirectory(),"bar_code.jpg");
        try {
            file.createNewFile();
            FileOutputStream fileOutputStream=new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,fileOutputStream);
            fileOutputStream.close();
            //shareingFile

            Intent intent=new Intent(Intent.ACTION_SEND);
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N){
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(MainActivity.this,"com.example.androidbarcode",file));
            }
            else{
                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            }
            intent.setType("image/*");
            startActivity(intent);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        //file sharing also workin

    }

    private void openScanner() {
        new IntentIntegrator(MainActivity.this).initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result=IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if(result!=null){
            if(result.getContents()==null){
                Toast.makeText(this, "Blank", Toast.LENGTH_SHORT).show();
            }
            else{
                textView.setText("Data : "+result.getContents());
            }
        }
        else{
            Toast.makeText(this, "Blank", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkPermission(String permission){
        int result= ContextCompat.checkSelfPermission(MainActivity.this,permission);
        if(result== PackageManager.PERMISSION_GRANTED){
            return true;
        }
        else{
            return false;
        }
    }

    private void requestPermission(String permision,int code){
        if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,permision)){

        }
        else{
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{permision},code);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case CAMERA_PERMISSION_CODE:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    openScanner();
                }
        }
    }
}
