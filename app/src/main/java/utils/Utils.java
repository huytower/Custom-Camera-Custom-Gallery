package utils;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import define.Extension;
import define.MediaType;
import ui.activity.custom.camera.CustomCamera;

public class Utils {

    // type definition for rotate
    public static final int FLIP_VERTICAL = 1;
    public static final int FLIP_HORIZONTAL = 2;
    /**
     * String section
     */
    public static boolean IS_SOFT_KEYBOARD_SHOWING = false;
    /**
     * View section
     */
    public static Dialog mDialogWaiting = null;
    public static Dialog mDialogWarning = null;

    private static ProgressDialog mPdWaiting = null;

    /**
     * invoke the system's media scanner to add your photo
     * to the Media Provider's database, making it available in the Android Gallery application and to other apps.
     */
    public static void addPictureToGallery(Context mContext, String realFilePathFromMedia) {
        Intent mIntentMediaScan = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File mFile = new File(realFilePathFromMedia);
        Uri mUriContent = Uri.fromFile(mFile);
        mIntentMediaScan.setData(mUriContent);
        ((Activity) mContext).sendBroadcast(mIntentMediaScan);
    }

    /**
     * Clear Cache
     *
     * @param mContext
     * @return
     */
    public static boolean clearCache(Context mContext) {
        PackageManager mPm = mContext.getPackageManager();
        // Get all methods on the PackageManager
        Method[] mMethods = mPm.getClass().getDeclaredMethods();
        for (Method mMethod : mMethods) {
            if (mMethod.getName().equals("freeStorage")) {
                // Found the method I want to use
                try {
                    long desired_free_storage = 8 * 1024 * 1024 * 1024; // Request for 8GB of free space
                    mMethod.invoke(mPm, desired_free_storage, null);

                    return true;
                } catch (Exception e) {
                    // Method invocation failed. Could be a permission problem
                    e.printStackTrace();
                }
                break;
            }
        }

        return false;
    }

    /**
     * Clear Data application
     *
     * @param mContext
     * @return
     */
    public static boolean clearData(Context mContext) {
        File dir = mContext.getCacheDir();

        // The directory is now empty so album it
        return dir.delete();
    }

    /**
     * Clear Data application
     *
     * @param mContext
     * @return
     */
    public static void clearOldBackStack(Context mContext) {
        // Try to clear old fragments stored in Back stack, avoid case
        // clicked Back hardware button, it will show overlay UI
        FragmentManager fm = ((FragmentActivity) mContext).getSupportFragmentManager();

        try {
            for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
                fm.popBackStack();
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * compress image
     */
    public static Bitmap compressImage(Bitmap bitmap) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        Bitmap decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
        return decoded;
    }

    public static int[] convertDateFromString(String[] SPLIT_PATH_DATE) {
        Date mDate = new Date();
        // 11 - 1 -> NOV
        // 1 - 1 -> Jan
        // 0 - 1 -> Dec
        int month = Integer.valueOf(SPLIT_PATH_DATE[1]) - 1;
        if (month == -1) month = Calendar.DECEMBER;
        mDate.setMonth(month);

        Calendar mCalendar = Calendar.getInstance();
        mCalendar.setTime(mDate);
        int year = Integer.valueOf(SPLIT_PATH_DATE[0]);
        // Dec -> 11
        int month_of_year = mCalendar.get(Calendar.MONTH);
        int day_of_month = Integer.valueOf(SPLIT_PATH_DATE[2]);

        return new int[]{year, month_of_year, day_of_month};
    }

    /**
     * @param dpValue
     * @param context
     * @return
     */
    public static float convertFromDpIntoInt(float dpValue, Context context) {
        final float scale = context.getResources().getDisplayMetrics().density;
        float intValue = (int) (dpValue * scale + 0.5f);
        return intValue;
    }

    /**
     *
     */
    public static String convertImageToEncodeBase64String(Bitmap mBitmap) {
        ByteArrayOutputStream mBaos = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.PNG, 0, mBaos); //bm is the bitmap object
        byte[] byteArrayImage = mBaos.toByteArray();

        return Base64.encodeToString(byteArrayImage, Base64.DEFAULT);
    }

    /**
     * Flip Bitmap
     *
     * @param src
     * @param type
     * @return
     */
    public static Bitmap flipBitmap(Bitmap src, int type) {
        // create new matrix for transformation
        Matrix matrix = new Matrix();

        if (type == 1) {
            // if vertical
            // top_bar = top_bar * -1
            matrix.preScale(1.0f, -1.0f);
        } else if (type == 2) {
            // if horizontal
            // x = x * -1
            matrix.preScale(-1.0f, 1.0f);
            // unknown type
        } else {
            return null;
        }

        // return transformed image
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }

    /**
     * @param bitmap
     * @return
     */
    public static Bitmap flipFrontImage(Bitmap bitmap) {
        Bitmap scale = null;

        switch (CustomCamera.current_orientation) {
            case 0:
                // Portrait mode
                scale = flipBitmap(bitmap, FLIP_VERTICAL);
                break;
            case 90:
                // Landscape mode
                scale = flipBitmap(bitmap, FLIP_HORIZONTAL);
                break;
            case 180:
                // Portrait mode
                scale = flipBitmap(bitmap, FLIP_VERTICAL);
                break;
            case 270:
                // Landscape mode
                scale = flipBitmap(bitmap, FLIP_HORIZONTAL);
                break;
        }

        return scale;
    }

    /**
     * @return
     */
    public static BitmapFactory.Options getBitmapOptions() {
        BitmapFactory.Options options = new BitmapFactory.Options();

        options.inPreferQualityOverSpeed = true;
        options.inJustDecodeBounds = false;
        options.inDither = false;
        options.inSampleSize = 4;
        options.inScaled = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        return options;
    }

    /**
     * Get dimensions of screen
     *
     * @param context
     * @return
     */
    public static float[] getDimension(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();

        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        Log.i("", "dpWidth " + dpWidth);

        final float scale = context.getResources().getDisplayMetrics().density;
        int pixels_height = (int) (dpHeight * scale + 0.5f);
        int pixels_width = (int) (dpWidth * scale + 0.5f);
        Log.i("", "pixels_width " + pixels_width);

        float[] values = new float[2];
        values[0] = pixels_width;
        values[1] = pixels_height;

        return values;
    }

    /**
     * Get duration length of specified video
     *
     * @param mContext
     * @param FILE_PATH
     * @return
     */
    public static String getDurationOfVideo(Context mContext, String FILE_PATH) {
        MediaPlayer mp = MediaPlayer.create(mContext, Uri.parse(FILE_PATH));

        /**
         * Check if :
         * - Correct file : get duration of video
         * - Wrong file : set duration 0:0 for video
         */
        if (mp != null) {
            int duration = mp.getDuration();
            mp.release();
        /*convert millis to appropriate time*/
            return String.format("%d:%d",
                    TimeUnit.MILLISECONDS.toMinutes(duration),
                    TimeUnit.MILLISECONDS.toSeconds(duration) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
        } else
            return String.format("%d:%d", 0, 0);
    }

    /**
     * Get all images from Sd card to load into Custom Gallery
     */
    public static ArrayList<File> getPhotoAndVideoFromSdCard(ArrayList<File> mAlFiles, File dir) {
        File mFileList[] = dir.listFiles();
        if (mFileList != null && mFileList.length > 0) {
            for (int i = 0; i < mFileList.length; i++) {
                if (mFileList[i].isDirectory() & !mFileList[i].isHidden()) {
                    // Add folder if need
                    if (!mAlFiles.contains(mFileList[i]))
                        mAlFiles.add(mFileList[i]);

                    // Should skip folder Android before add new files
                    getPhotoAndVideoFromSdCard(mAlFiles, mFileList[i]);
                } else if (mFileList[i].isFile() & !mFileList[i].isHidden()) {

                    /**
                     * Check to put only photo, video files to show in Custom Gallery
                     */
                    if (mFileList[i].getName().toLowerCase().endsWith(Extension.JPEG)
                            | mFileList[i].getName().toLowerCase().endsWith(Extension.JPG)
                            | mFileList[i].getName().toLowerCase().endsWith(Extension.PNG)
                            | mFileList[i].getName().toLowerCase().endsWith(Extension.MP4)) {
                        mAlFiles.add(mFileList[i]);
                    }
                }
            }
        }
        return mAlFiles;
    }

    /**
     * Get all images from Sd card to load into Custom Gallery
     */
    public static ArrayList<File> getPhotoAndVideoFromSdCard(
            boolean isFolderOrFile, ArrayList<File> mAlFiles, File dir) {
        File mFileList[] = dir.listFiles();
        if (mFileList != null && mFileList.length > 0) {
            for (int i = 0; i < mFileList.length; i++) {
                if (mFileList[i].isDirectory() & !mFileList[i].isHidden()) {
                    // Add folder if need
                    if (isFolderOrFile & !mAlFiles.contains(mFileList[i]))
                        mAlFiles.add(mFileList[i]);

//                    Log.i("", "mFileList[i] " + mFileList[i].getAbsolutePath());

                    // Should skip folder Android before add new files
                    getPhotoAndVideoFromSdCard(true, mAlFiles, mFileList[i]);
                } else if (mFileList[i].isFile() & !mFileList[i].isHidden()) {

                    /**
                     * Check to put only photo, video files to show in Custom Gallery
                     */
                    if (!isFolderOrFile) {
//                        Log.i("", "getName() " + mFileList[i].getName());

                        if (mFileList[i].getName().toLowerCase().endsWith(Extension.JPEG)
                                | mFileList[i].getName().toLowerCase().endsWith(Extension.JPG)
                                | mFileList[i].getName().toLowerCase().endsWith(Extension.PNG)
                                | mFileList[i].getName().toLowerCase().endsWith(Extension.MP4)) {
                            mAlFiles.add(mFileList[i]);
                        }
                    }
                }
            }
        }
        return mAlFiles;
    }

    /**
     * @param isPhotoOrVideo
     * @param context
     * @param imageFile
     * @return
     */
    public static Uri getImagePreviewOfUri(boolean isPhotoOrVideo, Context context, File imageFile) {
        String FILE_PATH = imageFile.getAbsolutePath();

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        Cursor mCursor = null;
        if (isPhotoOrVideo)
            mCursor = context.getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Images.Media._ID},
                    MediaStore.Images.Media.DATA + "=? ",
                    new String[]{FILE_PATH}, null);
        else
            mCursor = context.getContentResolver().query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Video.Media._ID},
                    MediaStore.Video.Media.DATA + "=? ",
                    new String[]{FILE_PATH}, null);

        if (mCursor != null && mCursor.moveToFirst()) {
            int id = mCursor.getInt(mCursor.getColumnIndex(MediaColumns._ID));

            if (isPhotoOrVideo)
                return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + id);
            else
                return Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();

                if (isPhotoOrVideo) {
                    values.put(MediaStore.Images.Media.DATA, FILE_PATH);
                    return context.getContentResolver().insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                } else {
                    values.put(MediaStore.Video.Media.DATA, FILE_PATH);
                    return context.getContentResolver().insert(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
                }
            } else {
                return null;
            }
        }
    }

    /**
     * Get real path from Uri
     *
     * @param context
     * @return
     */
    public static String getRealPathFromURI(Context context, Uri contentUri) {
//        Log.i("", "getRealPathFromURI " + (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT));

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            // Will return "image:x*"
//            String wholeID = DocumentsContract.getDocumentId(contentUri);
//
//            // Split at colon, use second item in the array
//            String id = wholeID.split(":")[1];
//
//            String[] column = {MediaStore.Images.Media.DATA};
//
//            // where id is equal to
//            String sel = MediaStore.Images.Media._ID + "=?";
//
//            Cursor cursor = context.getContentResolver().
//                    query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                            column, sel, new String[]{id}, null);
//
//            String filePath = "";
//
//            int columnIndex = cursor.getColumnIndex(column[0]);
//
//            if (cursor.moveToFirst()) {
//                filePath = cursor.getString(columnIndex);
//
//                return filePath;
//            }
//
//            cursor.close();
//
//            return null;
//        } else {
        Cursor cursor = null;
        try {
            String[] proj = {MediaColumns.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null,
                    null, null);

            if (cursor != null) {
                int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
                cursor.moveToFirst();
                return cursor.getString(column_index);
            } else
                return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
//        }
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @author paulburke
     */
    public static String getPath(final Context context, final Uri uri) {
        // DocumentProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }

                }
                // DownloadsProvider
                else if (isDownloadsDocument(uri)) {

                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                    return getDataColumn(context, contentUri, null, null);
                }
                // MediaProvider
                else if (isMediaDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }

                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{
                            split[1]
                    };

                    return getDataColumn(context, contentUri, selection, selectionArgs);
                }
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param mContext
     * @return
     */
    public static int[] getSizeOfScreen(Context mContext) {
        Display display = ((Activity) mContext).getWindowManager().getDefaultDisplay();

        int[] SIZE = new int[2];
        // Width
        SIZE[0] = display.getWidth();
        // Height
        SIZE[1] = display.getHeight();

        return SIZE;
    }

    /**
     * @param mContext
     * @param media_type_of_latest_bitmap_folder_thumbnail
     * @param mBitmap
     * @param THUMBNAIL_PATH
     * @return
     */
    public static Bitmap getThumbnail(
            Context mContext,
            boolean media_type_of_latest_bitmap_folder_thumbnail,
            Bitmap mBitmap, String THUMBNAIL_PATH) {
        if (media_type_of_latest_bitmap_folder_thumbnail) {
            // Photos
            Cursor ca = mContext.getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaColumns._ID},
                    MediaColumns.DATA + "=?", new String[]{THUMBNAIL_PATH}, null);
            if (ca != null && ca.moveToFirst()) {
                int id = ca.getInt(ca.getColumnIndex(MediaColumns._ID));
                ca.close();
                return MediaStore.Images.Thumbnails.getThumbnail(
                        mContext.getContentResolver(),
                        id,
                        MediaStore.Images.Thumbnails.MICRO_KIND,
                        null);
            }

            ca.close();
        } else {
            // Videos
            mBitmap = ThumbnailUtils.createVideoThumbnail(THUMBNAIL_PATH,
                    MediaStore.Images.Thumbnails.MINI_KIND);
        }

        return mBitmap;
    }

    /**
     * Get media type
     *
     * @param FILE_PATH
     * @return
     */
    public static int isPhotoOrVideo(String FILE_PATH) {
        int is_photo_or_video = -1;

        // Media type
        if (FILE_PATH.toLowerCase().contains(Extension.ARW)
                || FILE_PATH.toLowerCase().contains(Extension.CR2)
                || FILE_PATH.toLowerCase().contains(Extension.CRW)
                || FILE_PATH.toLowerCase().contains(Extension.DCR)
                || FILE_PATH.toLowerCase().contains(Extension.DNG)
                || FILE_PATH.toLowerCase().contains(Extension.JPEG)
                || FILE_PATH.toLowerCase().contains(Extension.MRW)
                || FILE_PATH.toLowerCase().contains(Extension.NEF)
                || FILE_PATH.toLowerCase().contains(Extension.ORF)
                || FILE_PATH.toLowerCase().contains(Extension.PEF)
                || FILE_PATH.toLowerCase().contains(Extension.JPG)
                || FILE_PATH.toLowerCase().contains(Extension.PNG)
                || FILE_PATH.toLowerCase().contains(Extension.RAF)
                || FILE_PATH.toLowerCase().contains(Extension.SR2)
                ) {
            /*
            ARW = ".arw";
            CR2 = ".cr2";
            CRW = ".crw";
            DCR = ".dcr";
            DNG = ".dng";
            JPEG = ".jpeg";
            JPG = ".jpg";
            MRW = ".mrw";
            NEF = ".nef";
            ORF = ".orf";
            PEF = ".pef";
            PNG = ".png";
            RAF = ".raf";
            SR2 = ".sr2";
            */

            // Set type : Photo - true : 1
            is_photo_or_video = MediaType.PHOTO;
        } else if (FILE_PATH.toLowerCase().contains(Extension.MP4)
                || FILE_PATH.toLowerCase().contains(Extension.MPG)
                || FILE_PATH.toLowerCase().contains(Extension.MOV)
                || FILE_PATH.toLowerCase().contains(Extension.MTS)
                || FILE_PATH.toLowerCase().contains(Extension.WMV)) {
            /*
            MP4 = ".mp4";
            MPG = ".mpg";
            MOV = ".mov";
            MTS = ".mts";
            WMV = ".wmv";
            */

            // Set type : Video - false : 2
            is_photo_or_video = MediaType.VIDEO;
        }

        return is_photo_or_video;
    }

    /**
     * Remove view parent
     */
    public static void removeViewParent(View v) {
        ((ViewGroup) v.getParent()).removeView(v);
    }

    /**
     * @param mBitmap
     * @return
     */
    public static Bitmap rotateBackImage(Bitmap mBitmap) {
        // set rotate in here
        Matrix matrix = new Matrix();

        /**
         * Define Orientation of image in here,
         * if in portrait mode, use value = 90,
         * if in landscape mode, use value = 0
         */
        switch (CustomCamera.current_orientation) {
            case 0:
                matrix.postRotate(90);
                break;
            case 90:
                matrix.postRotate(180);
                break;
            case 180:
                matrix.postRotate(270);
                break;
            case 270:
                matrix.postRotate(0);
                break;
        }

        return Bitmap.createBitmap(
                mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
    }

    /**
     * @param mContext
     * @param rotated
     * @return
     */
    public static Bitmap rotateFrontImage(Context mContext, Bitmap rotated) {
        Bitmap scale = null;

        switch (CustomCamera.current_orientation) {
            case 0:
                // Portrait mode
                // Should scale image more bigger
                scale = Bitmap.createScaledBitmap(
                        rotated,
                        getSizeOfScreen(mContext)[1] * rotated.getWidth() / rotated.getHeight(),
                        getSizeOfScreen(mContext)[1],
                        true);

                scale = flipBitmap(scale, FLIP_VERTICAL);
                break;
            case 90:
                // Landscape mode
                // Should scale image more bigger
                scale = Bitmap.createScaledBitmap(
                        rotated,
                        getSizeOfScreen(mContext)[0],
                        getSizeOfScreen(mContext)[0] * rotated.getHeight() / rotated.getWidth(),
                        true);

                scale = flipBitmap(scale, FLIP_HORIZONTAL);
                break;
            case 180:
                // Portrait mode
                // Should scale image more bigger
                scale = Bitmap.createScaledBitmap(
                        rotated,
                        getSizeOfScreen(mContext)[1] * rotated.getWidth() / rotated.getHeight(),
                        getSizeOfScreen(mContext)[1],
                        true);
                scale = flipBitmap(scale, FLIP_VERTICAL);
                break;
            case 270:
                // Landscape mode
                // Should scale image more bigger
                scale = Bitmap.createScaledBitmap(
                        rotated,
                        Utils.getSizeOfScreen(mContext)[0],
                        Utils.getSizeOfScreen(mContext)[0] * rotated.getHeight() / rotated.getWidth(),
                        true);

                scale = flipBitmap(scale, FLIP_HORIZONTAL);
                break;
        }

        return scale;
    }

    /**
     * @param mMediaRecorder
     * @return
     */
    public static MediaRecorder rotateBackVideo(MediaRecorder mMediaRecorder) {
        /**
         * Define Orientation of video in here,
         * if in portrait mode, use value = 90,
         * if in landscape mode, use value = 0
         */
        switch (CustomCamera.current_orientation) {
            case 0:
                mMediaRecorder.setOrientationHint(90);
                break;
            case 90:
                mMediaRecorder.setOrientationHint(180);
                break;
            case 180:
                mMediaRecorder.setOrientationHint(270);
                break;
            case 270:
                mMediaRecorder.setOrientationHint(0);
                break;
        }

        return mMediaRecorder;
    }

    /**
     * @param mMediaRecorder
     * @return
     */
    public static MediaRecorder rotateFrontVideo(MediaRecorder mMediaRecorder) {
        /**
         * Define Orientation of video in here,
         * if in portrait mode, use value = 90,
         * if in landscape mode, use value = 0
         */
        switch (CustomCamera.current_orientation) {
            case 0:
                mMediaRecorder.setOrientationHint(270);
                break;
            case 90:
                mMediaRecorder.setOrientationHint(180);
                break;
            case 180:
                mMediaRecorder.setOrientationHint(90);
                break;
            case 270:
                mMediaRecorder.setOrientationHint(0);
                break;
        }

        return mMediaRecorder;
    }

    /**
     * Set only default language
     *
     * @param context
     * @param lang
     */
    public static void setDefaultLanguage(Context context, String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        context.getResources().updateConfiguration(config,
                context.getResources().getDisplayMetrics());
    }


}
