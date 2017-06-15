package rx_playground.com.jablonski.cameracomponentlib.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx_playground.com.jablonski.cameracomponentlib.R2;
import rx_playground.com.jablonski.cameracomponentlib.view.interfaces.ImageResultCallback;
import rx_playground.com.jablonski.cameracomponentlib.view.managers.CameraManager;
import rx_playground.com.jablonski.cameracomponentlib.R;

/**
 * Created by yabol on 10.06.2017.
 */

public class CameraComponent extends RelativeLayout {
    private Activity activity;
    private CameraManager manager;

    @BindView(R2.id.cameraPreview)
    AutoFitTextureView cameraPreview;
    @BindView(R2.id.photoButton)
    ImageButton photoButotn;
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

    }

    public void takePhoto() {
        if (this.manager != null) {
            this.manager.takePhoto();
        }
    }

    public void setPhotoButton(ImageButton button){
        ViewGroup.LayoutParams params = this.photoButotn.getLayoutParams();
        removeView(this.photoButotn);
        this.photoButotn = button;
        this.photoButotn.setLayoutParams(params);
        addView(button);
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
