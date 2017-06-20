package rx_playground.com.jablonski.cameracomponent;

import android.Manifest;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import rx_playground.com.jablonski.cameracomponentlib.view.CameraComponent;

public class MainActivity extends AppCompatActivity {
    CameraComponent component;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        component = (CameraComponent) findViewById(R.id.camera);
        component.setActivity(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        component.startCamera();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
