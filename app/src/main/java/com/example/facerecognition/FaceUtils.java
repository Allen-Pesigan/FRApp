package com.example.facerecognition;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;

public class FaceUtils {

    /** View all stored faces */
    public static void viewDatabase(Context context, File facesDir) {
        File[] files = facesDir.listFiles();
        if (files == null || files.length == 0) {
            new AlertDialog.Builder(context)
                    .setTitle("Database Empty")
                    .setMessage("No faces stored yet.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        for (File file : files) {
            ImageView img = new ImageView(context);
            img.setImageURI(android.net.Uri.fromFile(file));
            img.setAdjustViewBounds(true);
            layout.addView(img);
        }

        new AlertDialog.Builder(context)
                .setTitle("Stored Faces")
                .setView(layout)
                .setPositiveButton("Close", null)
                .show();
    }

    /** Remove a specific face by name */
    public static void removeFace(Context context, File facesDir) {
        File[] files = facesDir.listFiles();
        if (files == null || files.length == 0) {
            new AlertDialog.Builder(context)
                    .setTitle("No Faces")
                    .setMessage("Nothing to remove.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        String[] names = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            names[i] = files[i].getName().replace(".jpg", "");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_list_item_1, names);

        new AlertDialog.Builder(context)
                .setTitle("Select Face to Remove")
                .setAdapter(adapter, (dialog, which) -> {
                    File fileToDelete = files[which];
                    if (fileToDelete.delete()) {
                        Toast.makeText(context,
                                "Deleted: " + names[which], Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context,
                                "Failed to delete.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
