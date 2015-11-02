package singleton.custom_camera;

import android.graphics.Bitmap;

/**
 * Created by trek2000 on 18/8/2014.
 */
public class Camera {

    /**
     * Single Ton method
     *
     * @return
     */
    private static Camera item = null;
    /**
     * String section
     */
    private boolean IS_CROP_MODE_OR_FULL_MODE = false;
    private String FILE_PATH = null;
    /**
     * View section
     */
    private Bitmap mBitmapImage;

    public static Camera getInstance() {
        if (item == null) {
            item = new Camera();
        }
        return item;
    }

    public String getFilePath() {
        return FILE_PATH;
    }

    public void setFilePath(String FILE_PATH) {
        this.FILE_PATH = FILE_PATH;
    }

    public Bitmap getImage() {
        return mBitmapImage;
    }

    public void setImage(Bitmap mBitmapImage) {
        this.mBitmapImage = mBitmapImage;
    }

    public boolean isCropModeOrFullMode() {
        return IS_CROP_MODE_OR_FULL_MODE;
    }

    public void setCropModeOrFullMode(boolean IS_CROP_MODE_OR_FULL_MODE) {
        this.IS_CROP_MODE_OR_FULL_MODE = IS_CROP_MODE_OR_FULL_MODE;
    }
}
