package controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import mirrortowers.custom_camera_gallery_library.R;
import ui.fragment.custom.camera.CameraReviewFragment;

/**
 * Created by trek2000 on 9/9/2014.
 */
public class SaveBitmapToFileAsync extends AsyncTask<String, Integer, Boolean> {

    /**
     * Data section
     */
    private Bitmap mBitmap = null;

    private File mFile = null;
    /**
     * String section
     */

    /**
     * View section
     */

    /**
     * The other sections
     */
    private Context mContext;
    // declare the dialog as a member field of your activity

    public SaveBitmapToFileAsync(Context mContext, Bitmap mBitmap, File mFile) {
        this.mContext = mContext;

        this.mBitmap = mBitmap;
        this.mFile = mFile;
    }

    @Override
    protected Boolean doInBackground(String... sUrl) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
        byte[] bitmapData = bos.toByteArray();

        //write the bytes in file
        try {
            FileOutputStream fos = new FileOutputStream(mFile);
            fos.write(bitmapData);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        CameraReviewFragment.mBtnUse.setEnabled(false);
        CameraReviewFragment.mBtnUse.setTextColor(
                mContext.getResources().getColor(R.color.gray));
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);

        CameraReviewFragment.mBtnUse.setEnabled(true);
        CameraReviewFragment.mBtnUse.setTextColor(
                mContext.getResources().getColor(R.color.orange));
    }
}
