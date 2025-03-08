package com.example.backgroundremover.fragments;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.backgroundremover.R;
import com.example.backgroundremover.adapters.BackgroundAdapter;
import com.example.backgroundremover.viewmodel.MainViewModel;
import java.util.ArrayList;
import java.util.List;

public class EditFragment extends Fragment implements BackgroundAdapter.OnBackgroundClickListener {

    private ImageView imagePreview;
    private ImageButton btnSave;
    private RecyclerView recyclerBackgrounds;
    private MainViewModel viewModel;
    private ProgressDialog progressDialog;
    // List of available background drawable resource IDs
    private List<Integer> backgroundList;

    public EditFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_edit, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        imagePreview = view.findViewById(R.id.imagePreview);
        btnSave = view.findViewById(R.id.btnSave);
        recyclerBackgrounds = view.findViewById(R.id.recyclerBackgrounds);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Processing...");
        progressDialog.setCancelable(false);

        // Setup RecyclerView for background selection
        backgroundList = new ArrayList<>();
        // Add some drawable resource IDs as available backgrounds (replace with your images)
        backgroundList.add(R.drawable.bg1);
        backgroundList.add(R.drawable.bg2);
        backgroundList.add(R.drawable.bg3);
        BackgroundAdapter adapter = new BackgroundAdapter(backgroundList, this);
        recyclerBackgrounds.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerBackgrounds.setAdapter(adapter);

        // Process the original image and simulate background removal
        Bitmap original = viewModel.getOriginalImage().getValue();
        if (original != null) {
            progressDialog.show();
            // Dummy background removal (replace with your AI-based code)
            Bitmap processed = removeBackground(original);
            viewModel.setProcessedImage(processed);
            imagePreview.setImageBitmap(processed);
            progressDialog.dismiss();
        }

        btnSave.setOnClickListener(v -> saveImage());
    }

    // Dummy background removal method – replace with your AI integration
    private Bitmap removeBackground(Bitmap original) {
        // In a real implementation, process the bitmap using ML Kit/TensorFlow Lite/OpenCV or an API.
        // Here we simply return the original image.
        return original;
    }

    // Combine the processed (foreground) image with a selected background
    private Bitmap combineWithBackground(Bitmap foreground, Bitmap background) {
        Bitmap scaledBackground = Bitmap.createScaledBitmap(background, foreground.getWidth(), foreground.getHeight(), true);
        Bitmap result = Bitmap.createBitmap(foreground.getWidth(), foreground.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(scaledBackground, 0, 0, null);
        // Assume the foreground image has transparency where background was removed
        canvas.drawBitmap(foreground, 0, 0, new Paint());
        return result;
    }

    @Override
    public void onBackgroundClick(int backgroundResId) {
        Bitmap processed = viewModel.getProcessedImage().getValue();
        if (processed != null) {
            try {
                // Load the background image from resources
                Uri bgUri = Uri.parse("android.resource://" + getContext().getPackageName() + "/" + backgroundResId);
                Bitmap background = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), bgUri);
                Bitmap combined = combineWithBackground(processed, background);
                viewModel.setProcessedImage(combined);
                imagePreview.setImageBitmap(combined);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void saveImage() {
        if (viewModel.getProcessedImage().getValue() == null) {
            Toast.makeText(getContext(), "No image to save", Toast.LENGTH_SHORT).show();
            return;
        }
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2001);
            return;
        }
        Bitmap bitmap = viewModel.getProcessedImage().getValue();
        // Save the image to the device gallery using MediaStore API
        String savedURL = MediaStore.Images.Media.insertImage(getContext().getContentResolver(), bitmap, "EditedImage", "Background Replaced Image");
        if (savedURL != null) {
            Toast.makeText(getContext(), "Image saved to gallery", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Failed to save image", Toast.LENGTH_SHORT).show();
        }
    }
}
