package com.example.facerecognition;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.example.facerecognition.databinding.ActivityMainBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 1;
    private ActivityMainBinding binding;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private Uri imageUri;
    private File facesDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Create public directory for storing face images
        facesDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "FaceRecognition");
        if (!facesDir.exists()) facesDir.mkdirs();

        registerPictureLauncher();

        // Button actions
        binding.btnAddFace.setOnClickListener(v -> checkCameraPermissionAndCapture());
        binding.btnViewDatabase.setOnClickListener(v -> FaceUtils.viewDatabase(this, facesDir));
        binding.btnRemoveFace.setOnClickListener(v -> FaceUtils.removeFace(this, facesDir));
    }

    private void registerPictureLauncher() {
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                new ActivityResultCallback<Boolean>() {
                    @Override
                    public void onActivityResult(Boolean result) {
                        if (result != null && result) {
                            askForNameAndSave();
                        } else {
                            Toast.makeText(MainActivity.this, "Capture cancelled.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void checkCameraPermissionAndCapture() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_CODE);
        } else {
            File imageFile = new File(getFilesDir(), "temp_photo.jpg");
            imageUri = FileProvider.getUriForFile(this,
                    "com.example.facerecognition.fileProvider",
                    imageFile);
            takePictureLauncher.launch(imageUri);
        }
    }

    private void askForNameAndSave() {
        EditText input = new EditText(this);
        input.setHint("Enter name");

        new AlertDialog.Builder(this)
                .setTitle("Save Face")
                .setMessage("Enter name for this person:")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> saveFaceImage(input.getText().toString().trim()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveFaceImage(String name) {
        if (name.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File savedFile = new File(facesDir, name + ".jpg");
            try (InputStream in = getContentResolver().openInputStream(imageUri);
                 OutputStream out = new FileOutputStream(savedFile)) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = in.read(buffer)) > 0) out.write(buffer, 0, len);
            }
            Toast.makeText(this, "Saved: " + savedFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkCameraPermissionAndCapture();
            } else {
                Toast.makeText(this, "Camera permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
