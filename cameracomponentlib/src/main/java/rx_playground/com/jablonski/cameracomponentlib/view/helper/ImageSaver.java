package rx_playground.com.jablonski.cameracomponentlib.view.helper;

import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import rx_playground.com.jablonski.cameracomponentlib.view.interfaces.ImageResultCallback;

/**
 * Created by yabol on 15.06.2017.
 */

public class ImageSaver implements Runnable {
    private ImageReader imageReader;
    private File file;
    private ImageResultCallback callback;
    private String imagePath;
    private byte[] data;
    private Image image;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public ImageSaver(ImageReader image, String imagePath) {
        this.imageReader = image;
        this.imagePath = imagePath;
        this.image = this.imageReader.acquireNextImage();
        ByteBuffer buffer = this.image.getPlanes()[0].getBuffer();
        this.data = new byte[buffer.remaining()];
        buffer.get(this.data);
        image.close();
    }

    public ImageSaver(byte[] data, String imagePath) {
        this.data = data;
        this.imagePath = imagePath;
    }


    @Override
    public void run() {
        this.file = createImageFile();
        if (this.file != null) {

            saveImage();

        }
    }

    private File createImageFile() {
        File mediaStorageDir = new File(this.imagePath);
        if (createFolder(mediaStorageDir)) {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            return new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");

        }
        return null;
    }

    private boolean createFolder(File file) {
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return false;
            }
        }
        return true;
    }

    private void saveImage() {
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(this.file);
            output.write(this.data);
            if (this.callback != null) {
                this.callback.onImageCaptured(this.file.getAbsolutePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(this.image != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    image.close();
                }
            }
            if (null != output) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public void setCallback(ImageResultCallback callback) {
        this.callback = callback;
    }
}
