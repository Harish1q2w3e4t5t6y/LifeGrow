// BlockOverlayService.java
package com.example.lifegrow.ui.others;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.*;
import android.widget.Button;
import android.widget.TextView;
import com.example.lifegrow.R;

public class BlockOverlayService extends Service {
    private WindowManager windowManager;
    private View overlayView;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        LayoutInflater inflater = LayoutInflater.from(this);
        overlayView = inflater.inflate(R.layout.overlay_block_layout, null);

        int layoutFlag = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_FULLSCREEN,
                PixelFormat.TRANSLUCENT
        );

        windowManager.addView(overlayView, params);

        Button dismissBtn = overlayView.findViewById(R.id.btnDismiss);
        dismissBtn.setOnClickListener(v -> stopSelf());
    }

    @Override
    public void onDestroy() {
        if (overlayView != null) {
            windowManager.removeView(overlayView);
        }
        super.onDestroy();
    }
}
