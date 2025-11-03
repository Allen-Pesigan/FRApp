package com.example.facerecognition;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.facerecognition.databinding.ActivityMainBinding;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 1;

    private ActivityMainBinding mainBinding;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Use View Binding to access layout elements
        mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mainBinding.getRoot());

        // Create the image file URI and register the camera launcher
        imageUri = createUri();
        registerPictureLauncher();

        // Button click event
        mainBinding.btnTakePicture.setOnClickListener(view -> checkCameraPermissionAndOpenCamera());

        // Adjust window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /** Create a file URI to store the captured image */
    private Uri createUri() {
        File imageFile = new File(getApplicationContext().getFilesDir(), "camera_photo.jpg");
        return FileProvider.getUriForFile(
                getApplicationContext(),
                "com.example.facerecognition.fileProvider", // Must match your manifest provider authority
                imageFile
        );
    }

    /** Register launcher for taking a picture */
    private void registerPictureLauncher() {
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                new ActivityResultCallback<Boolean>() {
                    @Override
                    public void onActivityResult(Boolean result) {
                        if (result != null && result) {
                            // Refresh ImageView
                            mainBinding.ivUser.setImageURI(null);
                            mainBinding.ivUser.setImageURI(imageUri);
                        } else {
                            Toast.makeText(MainActivity.this, "Failed to capture image.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    /** Check camera permission before launching camera */
    private void checkCameraPermissionAndOpenCamera() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_CODE
            );

        } else {
            takePictureLauncher.launch(imageUri);
        }
    }

    /** Handle permission result */
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePictureLauncher.launch(imageUri);
            } else {
                Toast.makeText(this,
                        "Camera permission denied. Please allow permission to take a picture.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
