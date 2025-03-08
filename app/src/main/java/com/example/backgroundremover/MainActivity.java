package com.example.backgroundremover;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.backgroundremover.fragments.CameraFragment;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Start with the CameraFragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new CameraFragment())
                .commit();
        }
    }
}
