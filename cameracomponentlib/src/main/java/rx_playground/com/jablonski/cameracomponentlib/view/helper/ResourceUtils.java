package rx_playground.com.jablonski.cameracomponentlib.view.helper;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;

/**
 * Created by yabol on 17.06.2017.
 */

public class ResourceUtils {

    public static Drawable getDrawable(Context context, int drawableId){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return context.getDrawable(drawableId);
        }else{
            return context.getResources().getDrawable(drawableId);
        }
    }
}
