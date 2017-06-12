package rx_playground.com.jablonski.cameracomponentlib;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.view.TextureView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by yabol on 10.06.2017.
 */

public class CameraComponent extends RelativeLayout implements CameraAPI {
    private Activity activity;
    private CameraManager manager;

    @BindView(R2.id.cameraPreview)
    TextureView cameraPreview;
    @BindView(R2.id.photoButton)
    ImageButton photoButotn;
    @BindView(R2.id.switchButton)
    ImageButton switchCameraButton;

    public static CameraComponent getInstance(Activity activity){
        CameraComponent component = new CameraComponent(activity);
        component.setActivity(activity);

        return component;
    }


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

    public void setActivity(Activity activity){
        this.activity = activity;
        this.manager = new CameraManager(activity);
    }


    private void init(){
        inflate(getContext(), R.layout.camera_component, this);

        ButterKnife.bind(this);

    }

    @Override
    public void takePhoto() {
        if(this.manager != null){
            this.manager.takePhoto();
        }
    }

    public void startCamera(){
        if(this.manager != null){
            this.manager.startCamera();
        }
    }

    @Override
    public void stopCamera() {
        if(this.manager != null){
            this.manager.stopCamera();
        }
    }

    @Override
    public void changeCamera() {
        if(this.manager != null){
            this.manager.changeCamera();
        }
    }

}
