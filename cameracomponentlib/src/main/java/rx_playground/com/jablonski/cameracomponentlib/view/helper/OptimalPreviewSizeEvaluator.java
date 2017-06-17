package rx_playground.com.jablonski.cameracomponentlib.view.helper;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.util.Size;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by yabol on 14.06.2017.
 */

public class OptimalPreviewSizeEvaluator {
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public Size chooseOptimalSize(Size[] choices, int textureViewWidth,
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
}
