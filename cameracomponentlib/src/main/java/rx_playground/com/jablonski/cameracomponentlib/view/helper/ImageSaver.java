package rx_playground.com.jablonski.cameracomponentlib.view.helper;

import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import rx_playground.com.jablonski.cameracomponentlib.view.interfaces.ImageResultCallback;

/**
 * Created by yabol on 15.06.2017.
 */

public class ImageSaver implements Runnable {
    private ImageReader imageReader;
    private File file;
    private ImageResultCallback callback;

    public ImageSaver(ImageReader image, File file){
        this.imageReader = image;
        this.file = file;
    }

    @Override
    public void run() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            saveImageApi19();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void saveImageApi19(){
        Image image = this.imageReader.acquireNextImage();
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(this.file);
            output.write(bytes);
            if(this.callback != null){
                this.callback.onImageCaptured(this.file.getAbsolutePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            image.close();
            if (null != output) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            this.imageReader.close();
        }
    }

    public void setCallback(ImageResultCallback callback) {
        this.callback = callback;
    }
}
