package com.example.klaudia.whiteboard;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    int reqCode = 1;

    String UPLOAD_URL = "http://34.226.143.170:5000/base64upload";

    OkHttpClient client = new OkHttpClient();

    Call send(String img, Callback callback) {
        // Create a multipart request body. Add metadata and files as 'data parts'.
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", img)
                .build();

        // Create a POST request to send the data to UPLOAD_URL
        Request request = new Request.Builder()
                .url(UPLOAD_URL)
                .post(requestBody)
                .build();

        System.out.print("JUST BEFORE");
        Call call = client.newCall(request);
        call.enqueue(callback);
        return call;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == reqCode && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap pic = (Bitmap) extras.get("data");


            ByteArrayOutputStream outstream = new ByteArrayOutputStream();
            pic.compress(Bitmap.CompressFormat.JPEG, 100, outstream);
            byte[] imgBytes = outstream.toByteArray();
            String encodedImage = Base64.encodeToString(imgBytes, Base64.DEFAULT);

            send(encodedImage, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    System.out.print("SOMETHING IS WRONG");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseStr = response.body().string();
                        System.out.print("RESPONSE: " + responseStr);
                        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "PICTURE UPLOADED", Snackbar.LENGTH_LONG);

                        snackbar.show();
                    } else {
                        System.out.print("REQUEST NOT SO SUCCESSFUL");
                    }
                }
            });

        }
    }


    protected void takePicture(View view){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            Intent capture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (capture.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(capture, reqCode);
            }

        } else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0);
            takePicture(view);
        }
    }
}
