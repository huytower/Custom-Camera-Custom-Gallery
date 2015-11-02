package surface;

/**
 * Created by trek2000 on 16/3/2015.
 */

import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

import ui.activity.custom.camera.CustomCamera;
import ui.fragment.custom.camera.CameraPreviewFragment;

/**
 * A basic Camera preview class
 */
public class CustomCameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private Camera mCamera;
    private Camera.Parameters parameters;

    private SurfaceHolder mHolder;

    private Camera.Size mPreviewSize;

    private List<Camera.Size> mSupportedPreviewSizes;

    public CustomCameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;

        // supported preview sizes
        try {
            mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);

        setMeasuredDimension(width, height);

        if (mSupportedPreviewSizes != null) {
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
        }

        float ratio;
        if (mPreviewSize.height >= mPreviewSize.width)
            ratio = (float) mPreviewSize.height / (float) mPreviewSize.width;
        else
            ratio = (float) mPreviewSize.width / (float) mPreviewSize.height;

        // One of these methods should be used, second method squishes preview slightly
        // if current_camera_id = 1- Front camera mode
        // if current_camera_id = 0 - Back camera mode
        if (CameraPreviewFragment.current_camera_id == define.Camera.CAMERA_BACK) {
            setMeasuredDimension(width, (int) (width * ratio));
        } else {
            if (((int) (height / ratio)) < width) {
                int plusWidth = width - ((int) (height / ratio));

                setMeasuredDimension(width, height + (int) (plusWidth * ratio));
            } else
                setMeasuredDimension((int) (height / ratio), height);
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // empty. surfaceChanged will take care of stuff
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
            e.printStackTrace();
        }

        // set preview size and make any resize, rotate or reformatting changes here
        // start preview with new settings
        try {
            // set preview size and make any resize, rotate or
            // reformatting changes here
            Camera.Parameters parameters = mCamera.getParameters();

//            for (Camera.Size size : mSupportedPreviewSizes) {
//                if (1600 <= size.width & size.width <= 1920) {
            parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            parameters.setPictureSize(mPreviewSize.width, mPreviewSize.height);
//                    break;
//                }
//            }

            // Set parameters for camera
            try {
                CustomCamera.mCamera.setParameters(parameters);
            } catch (Exception e) {
                if (mCamera != null) {
                    mCamera.setPreviewDisplay(mHolder);
                    mCamera.startPreview();
                }
            }

            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }
}
