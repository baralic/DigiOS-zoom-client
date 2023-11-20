package us.zoom.sdksample.initsdk;

import android.content.Context;

public interface JwtCallback {
    void onPostExecution(String signature);

    Context context();
}
