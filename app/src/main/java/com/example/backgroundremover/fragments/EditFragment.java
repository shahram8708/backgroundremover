package com.example.backgroundremover.fragments;

import android.Manifest;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
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
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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

        // Process the original image and remove background using remove.bg API
        Bitmap original = viewModel.getOriginalImage().getValue();
        if (original != null) {
            progressDialog.show();
            // Start async background removal using remove.bg API
            new RemoveBgTask().execute(original);
        }

        btnSave.setOnClickListener(v -> saveImage());
    }

    /**
     * AsyncTask to call the remove.bg API to remove the background from the image.
     */
    private class RemoveBgTask extends AsyncTask<Bitmap, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(Bitmap... bitmaps) {
            Bitmap inputBitmap = bitmaps[0];
            try {
                // Convert bitmap to JPEG byte array
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                inputBitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
                byte[] imageBytes = baos.toByteArray();

                OkHttpClient client = new OkHttpClient();

                // Build the multipart request body
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("image_file", "image.jpg",
                                RequestBody.create(MediaType.parse("image/jpeg"), imageBytes))
                        .addFormDataPart("size", "auto")
                        .build();

                // Create the request using the provided remove.bg API key
                Request request = new Request.Builder()
                        .url("https://api.remove.bg/v1.0/removebg")
                        .addHeader("X-Api-Key", "JKaN44MGWpnVgimc9M61qBum")
                        .post(requestBody)
                        .build();

                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    return null;
                }
                byte[] resultBytes = response.body().bytes();
                return BitmapFactory.decodeByteArray(resultBytes, 0, resultBytes.length);

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap resultBitmap) {
            progressDialog.dismiss();
            if (resultBitmap != null) {
                viewModel.setProcessedImage(resultBitmap);
                imagePreview.setImageBitmap(resultBitmap);
            } else {
                Toast.makeText(getContext(), "Background removal failed", Toast.LENGTH_SHORT).show();
            }
        }
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
