package rx_playground.com.jablonski.cameracomponentlib.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx_playground.com.jablonski.cameracomponentlib.R2;
import rx_playground.com.jablonski.cameracomponentlib.view.helper.ResourceUtils;
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
    private OnClickListener photoTakenListener;
    private ImageResultCallback imageResultCallback;
    private String imagePath;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            if(data != null) {
                setPhotoTaken(data.getString("imagePath"));
            }
        }
    };

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
        this.manager.setImageCapturedListener(new ImageResultCallback() {
            @Override
            public void onImageCaptured(String imagePath) {
                Bundle data = new Bundle();
                data.putString("imagePath", imagePath);
                Message message = new Message();
                message.setData(data);
                handler.sendMessage(message);
            }
        });
    }


    private void init() {
        inflate(getContext(), R.layout.camera_component, this);

        ButterKnife.bind(this);

        this.placement = new DefaultButtonsPlacement();
        this.photoButton.setLayoutParams(this.placement.photoButtonPosition());
        this.switchCameraButton.setLayoutParams(this.placement.changeCameraButtonPosition());
        initiateListeners();

        this.photoButton.setOnClickListener(this.buttonClickListener);
        this.switchCameraButton.setOnClickListener(this.buttonClickListener);
    }

    private void initiateListeners(){
        this.buttonClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = v.getId();
                if (i == R.id.switchButton) {
                    changeCamera();
                }else if (i == R.id.photoButton){
                    takePhoto();
                }
            }
        };
        this.photoTakenListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = v.getId();
                if (i == R.id.switchButton) {
                    cancelPhotoTaken();
                }else if (i == R.id.photoButton){
                    if(imageResultCallback != null && !TextUtils.isEmpty(imagePath)){
                        imageResultCallback.onImageCaptured(imagePath);
                    }
                }
            }
        };
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
        this.imageResultCallback = listener;
    }

    public void setPhotoTaken(String imagePath){
        this.imagePath = imagePath;
        this.photoButton.setOnClickListener(this.photoTakenListener);
        this.switchCameraButton.setOnClickListener(this.photoTakenListener);
        this.photoButton.setImageDrawable(ResourceUtils.getDrawable(getContext(), R.drawable.ic_submit));
        this.switchCameraButton.setImageDrawable(ResourceUtils.getDrawable(getContext(), R.drawable.ic_cancel));
        //todo change resources
    }

    private void cancelPhotoTaken(){
        stopCamera();
        startCamera();
        this.imagePath = "";
        this.photoButton.setOnClickListener(this.buttonClickListener);
        this.switchCameraButton.setOnClickListener(this.buttonClickListener);
        this.photoButton.setImageDrawable(ResourceUtils.getDrawable(getContext(), R.drawable.ic_take_photo));
        this.switchCameraButton.setImageDrawable(ResourceUtils.getDrawable(getContext(), R.drawable.ic_switch_camera));
        //todo chang eresources
    }
}
