package rx_playground.com.jablonski.cameracomponentlib.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx_playground.com.jablonski.cameracomponentlib.R2;
import rx_playground.com.jablonski.cameracomponentlib.view.interfaces.ImageResultCallback;
import rx_playground.com.jablonski.cameracomponentlib.view.managers.CameraManager;
import rx_playground.com.jablonski.cameracomponentlib.R;
import rx_playground.com.jablonski.cameracomponentlib.view.positioning.ButtonPlacement;
import rx_playground.com.jablonski.cameracomponentlib.view.positioning.DefaultButtonsPlacement;

/**
 * Created by yabol on 10.06.2017.
 */

public class CameraComponent extends RelativeLayout {
    private Activity activity;
    private CameraManager manager;
    private ButtonPlacement placement;
    private OnClickListener buttonClickListener;

    @BindView(R2.id.cameraPreview)
    AutoFitTextureView cameraPreview;
    @BindView(R2.id.photoButton)
    ImageButton photoButton;
    @BindView(R2.id.switchButton)
    ImageButton switchCameraButton;


    public CameraComponent(Context context) {
        super(context);
        init();
    }

    public CameraComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CameraComponent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
        this.manager = new CameraManager(activity, this.cameraPreview);
    }


    private void init() {
        inflate(getContext(), R.layout.camera_component, this);

        ButterKnife.bind(this);

        this.placement = new DefaultButtonsPlacement();
        this.photoButton.setLayoutParams(this.placement.photoButtonPosition());
        this.switchCameraButton.setLayoutParams(this.placement.changeCameraButtonPosition());
        this.buttonClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = v.getId();
                if (i == R.id.switchButton) {
                    manager.changeCamera();
                }else{
                    manager.takePhoto();
                }
            }
        };
        this.photoButton.setOnClickListener(this.buttonClickListener);
        this.switchCameraButton.setOnClickListener(this.buttonClickListener);
    }

    public void takePhoto() {
        if (this.manager != null) {
            this.manager.takePhoto();
        }
    }


    public void setPhotoButton(ImageButton button){
        removeView(this.photoButton);
        this.photoButton = button;
        this.photoButton.setId(R.id.photoButton);
        this.photoButton.setLayoutParams(this.placement.photoButtonPosition());
        this.photoButton.setOnClickListener(this.buttonClickListener);
        addView(button);
    }

    public void setChangeCameraButton(ImageButton button){
        removeView(this.switchCameraButton);
        this.switchCameraButton = button;
        this.switchCameraButton.setId(R.id.switchButton);
        this.switchCameraButton.setLayoutParams(this.placement.changeCameraButtonPosition());
        this.switchCameraButton.setOnClickListener(this.buttonClickListener);
        addView(button);
    }

    public void setPlacement(ButtonPlacement placement){
        this.placement = placement;
    }


    public void startCamera() {
        if (this.manager != null) {
            this.manager.startCamera();
        }
    }

    public void stopCamera() {
        if (this.manager != null) {
            this.manager.stopCamera();
        }
    }

    public void changeCamera() {
        if (this.manager != null) {
            this.manager.changeCamera();
        }
    }

    public void setImageCapturedListener(ImageResultCallback listener){
        if(manager != null) {
            this.manager.setImageCapturedListener(listener);
        }
    }

}
