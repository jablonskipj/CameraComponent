package rx_playground.com.jablonski.cameracomponentlib;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.*;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created by yabol on 10.06.2017.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CameraAPI21 implements CameraAPI{
    private Activity activity;
    private CameraDevice camera;
    private CameraManager manager;
    private String cameraId;
    private HandlerThread backgroundThread;
    private Handler backgroundHandler;
    private Semaphore cameraOpenCloseLock = new Semaphore(1);
    private AutoFitTextureView cameraPreview;
    private ImageReader reader;
    private Size cameraPreviewSize;
    private boolean flashSupported;
    private int displayRotation;
    private int cameraSensorOrientaiton;
    private CameraPreviewCapturer cameraPreviewCapturerHandler;


    private static final int MAX_PREVIEW_WIDTH = 1920;
    private static final int MAX_PREVIEW_HEIGHT = 1080;

    private final CameraDevice.StateCallback cameraStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            // This method is called when the camera is opened.  We start camera preview here.
            cameraOpenCloseLock.release();
            camera = cameraDevice;
            cameraPreviewCapturerHandler = new CameraPreviewCapturer(CameraAPI21.this);
            cameraPreviewCapturerHandler.createPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraOpenCloseLock.release();
            cameraDevice.close();
            camera = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            cameraOpenCloseLock.release();
            cameraDevice.close();
            camera = null;
            if (activity != null) {
                activity.finish();
            }
        }

    };

    private final ImageReader.OnImageAvailableListener imageCapturedListener
            = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {
           // backgroundHandler.post(new ImageSaver(reader.acquireNextImage(), mFile));
        }

    };

    public CameraAPI21(Activity activity, AutoFitTextureView cameraPreview){
        this.activity = activity;
        this.manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        this.cameraPreview = cameraPreview;
    }
    @Override
    public void takePhoto() {

    }

    @Override
    public void startCamera(int width, int heigth) {
        startBackgroundThread();
        if(ContextCompat.checkSelfPermission(this.activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

            setUpCamera(width, heigth);
            new PreviewTransformer(this).transform(width, heigth, this.displayRotation);
            try {
                if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                    throw new RuntimeException("Time out waiting to lock camera opening.");
                }
                this.manager.openCamera(this.cameraId, cameraStateCallback, backgroundHandler);
            } catch (CameraAccessException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void setUpCamera(int width, int height){
        CameraManager manager = (CameraManager) this.activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            for(String cameraId : manager.getCameraIdList()){
                CameraCharacteristics characteristics
                        = manager.getCameraCharacteristics(cameraId);

                StreamConfigurationMap map = characteristics.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) {
                    continue;
                }

                Size largest = Collections.max(
                        Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                        new SizeAreaComparator());

                this.reader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(),
                        ImageFormat.JPEG, 1);
                this.reader.setOnImageAvailableListener(
                        this.imageCapturedListener, this.backgroundHandler);

                this.displayRotation = activity.getWindowManager().getDefaultDisplay().getRotation();


                this.cameraPreviewSize = getCameraPreviewSize(characteristics, map, largest, width, height);

                int orientation = this.activity.getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    this.cameraPreview.setAspectRatio(
                            this.cameraPreviewSize.getWidth(), this.cameraPreviewSize.getHeight());
                } else {
                    this.cameraPreview.setAspectRatio(
                            this.cameraPreviewSize.getHeight(), this.cameraPreviewSize.getWidth());
                }

                Boolean available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                this.flashSupported = available == null ? false : available;

                this.cameraId = cameraId;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private Size getCameraPreviewSize(CameraCharacteristics characteristics, StreamConfigurationMap map, Size largest, int width, int height){

        Point displaySize = new Point();
        this.activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
        int rotatedPreviewWidth = width;
        int rotatedPreviewHeight = height;
        int maxPreviewWidth = displaySize.x;
        int maxPreviewHeight = displaySize.y;

        if (isDimensionSwapped(characteristics)) {
            rotatedPreviewWidth = height;
            rotatedPreviewHeight = width;
            maxPreviewWidth = displaySize.y;
            maxPreviewHeight = displaySize.x;
        }

        if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
            maxPreviewWidth = MAX_PREVIEW_WIDTH;
        }

        if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
            maxPreviewHeight = MAX_PREVIEW_HEIGHT;
        }
        return chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                maxPreviewHeight, largest);
    }

    private boolean isDimensionSwapped(CameraCharacteristics characteristics){
        boolean swappedDimensions = false;
        Integer cameraSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        if(cameraSensorOrientation != null) {
            this.cameraSensorOrientaiton = cameraSensorOrientation;
            switch (displayRotation) {
                case Surface.ROTATION_0:
                case Surface.ROTATION_180:
                    if (cameraSensorOrientation == 90 || cameraSensorOrientation == 270) {
                        swappedDimensions = true;
                    }
                    break;
                case Surface.ROTATION_90:
                case Surface.ROTATION_270:
                    if (cameraSensorOrientation == 0 || cameraSensorOrientation == 180) {
                        swappedDimensions = true;
                    }
                    break;
                default:
                    Log.e("Camera Component", "Display rotation is invalid: " + displayRotation);
            }
        }
        return swappedDimensions;
    }

    private Size chooseOptimalSize(Size[] choices, int textureViewWidth,
                                          int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {

        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        // Collect the supported resolutions that are smaller than the preview Surface
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
                    option.getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth &&
                        option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }

        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new SizeAreaComparator());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new SizeAreaComparator());
        } else {
            Log.e("Camera COmponent", "Couldn't find any suitable preview size");
            return choices[0];
        }
    }
    public void setAutoFlash(CaptureRequest.Builder requestBuilder) {
        if (this.flashSupported) {
            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
        }
    }

    @Override
    public void stopCamera() {
        cameraOpenCloseLock.release();
        if (null != mCaptureSession) {
            mCaptureSession.close();
            mCaptureSession = null;
        }
        if (this.camera != null) {
            this.camera.close();
            this.camera = null;
        }
        if (this.reader != null) {
            this.reader.close();
            this.reader = null;
        }
        stopBackgroundThread();
    }
    private void startBackgroundThread() {
        this.backgroundThread = new HandlerThread("CameraBackground");
        this.backgroundThread.start();
        this.backgroundHandler = new Handler(this.backgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        if(this.backgroundThread != null) {
            this.backgroundThread.quitSafely();
            try {
                this.backgroundThread.join();
                this.backgroundThread = null;
                this.backgroundHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void changeCamera() {

    }

    public Handler getHandler(){
        return this.backgroundHandler;
    }

    public TextureView getTextureView(){
        return this.cameraPreview;
    }

    public CameraDevice getCameraDevice(){
        return this.camera;
    }

    public ImageReader getImageReader(){
        return this.reader;
    }

    public Size getPreviewSize(){
        return this.cameraPreviewSize;
    }

    public int getDisplayRotation(){
        return this.displayRotation;
    }

    public int getCameraOrientation(){
        return this.cameraSensorOrientaiton;
    }
}
