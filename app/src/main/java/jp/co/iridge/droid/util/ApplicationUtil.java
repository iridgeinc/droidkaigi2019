package jp.co.iridge.droid.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.viro.core.AnimationTimingFunction;
import com.viro.core.AnimationTransaction;
import com.viro.core.Node;
import com.viro.core.Vector;

import java.io.IOException;
import java.io.InputStream;

public final class ApplicationUtil {

    public static Bitmap getBitmapFromAssets(@NonNull Context context, @NonNull String assetName) {
        AssetManager assetManager = context.getResources().getAssets();
        InputStream imageStream;
        try {
            imageStream = assetManager.open(assetName);
        } catch (IOException exception) {
            Log.w("ApplicationUtil", "Unable to find image [" + assetName + "] in assets! Error: " + exception.getMessage());
            return null;
        }
        return BitmapFactory.decodeStream(imageStream);
    }


    public static void showToast(@NonNull Context context, @NonNull String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void animateScale(@NonNull Node node, long duration, @NonNull Vector targetScale,
                              @NonNull AnimationTimingFunction fcn, @Nullable final Runnable runnable) {
        AnimationTransaction.begin();
        AnimationTransaction.setAnimationDuration(duration);
        AnimationTransaction.setTimingFunction(fcn);
        node.setScale(targetScale);
        if (runnable != null){
            AnimationTransaction.setListener(new AnimationTransaction.Listener() {
                @Override
                public void onFinish(AnimationTransaction animationTransaction) {
                    runnable.run();
                }
            });
        }
        AnimationTransaction.commit();
    }

}
