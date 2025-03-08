package com.example.backgroundremover.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.backgroundremover.R;
import com.example.backgroundremover.viewmodel.MainViewModel;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.ExecutionException;

public class CameraFragment extends Fragment {
    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final int GALLERY_REQUEST_CODE = 1002;

    private PreviewView previewView;
    private ImageCapture imageCapture;
    private MainViewModel viewModel;

    public CameraFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        previewView = view.findViewById(R.id.previewView);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        
        view.findViewById(R.id.btnCapture).setOnClickListener(v -> captureImage());
        view.findViewById(R.id.btnGallery).setOnClickListener(v -> openGallery());

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
        }
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }
    
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        previewView.setImplementationMode(PreviewView.ImplementationMode.COMPATIBLE);
        androidx.camera.core.Preview preview = new androidx.camera.core.Preview.Builder().build();
        imageCapture = new ImageCapture.Builder().build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
    }
    
    private void captureImage() {
        if (imageCapture == null) return;
        imageCapture.takePicture(ContextCompat.getMainExecutor(requireContext()), new ImageCapture.OnImageCapturedCallback(){
            @Override
            public void onCaptureSuccess(@NonNull androidx.camera.core.ImageProxy image) {
                // Convert image to Bitmap (dummy implementation – replace with proper conversion)
                Bitmap bitmap = imageProxyToBitmap(image);
                image.close();
                if (bitmap != null) {
                    viewModel.setOriginalImage(bitmap);
                    // Navigate to EditFragment
                    requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new EditFragment())
                        .addToBackStack(null)
                        .commit();
                } else {
                    Toast.makeText(getContext(), "Image conversion failed", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Toast.makeText(getContext(), "Error capturing image", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Dummy conversion method – in production, convert YUV image data to Bitmap
    private Bitmap imageProxyToBitmap(androidx.camera.core.ImageProxy image) {
        // For a proper conversion, use a YuvToRgbConverter or similar utility.
        return null;
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults){
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(getContext(), "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_REQUEST_CODE && data != null && data.getData() != null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), data.getData());
                viewModel.setOriginalImage(bitmap);
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new EditFragment())
                        .addToBackStack(null)
                        .commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
