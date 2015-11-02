package model.custom_gallery;

import android.content.Context;
import android.graphics.Bitmap;

import mirrortowers.custom_camera_gallery_library.R;

/**
 * Created by trek2000 on 12/2/2015.
 */
public class Album {

    /**
     * String section
     */
    private String ALBUM_NAME = null;
    private String DATE_TAKEN = null;
    private String FOLDER_PATH = null;

    private Bitmap mBitmapFolderThumbnail;

    private boolean is_photo_or_video = true;
    private boolean media_type_of_latest_bitmap_folder_thumbnail = true;

    private int number_of_photos = 0;
    private int number_of_videos = 0;

    /**
     * The others section
     */
    private Context mContext;

    /**
     * @param mContext
     * @param ALBUM_NAME
     * @param FOLDER_PATH
     * @param is_photo_or_video
     */
    public Album(
            Context mContext,
            String ALBUM_NAME,
            String DATE_TAKEN,
            String FOLDER_PATH,
            boolean is_photo_or_video) {
        this.mContext = mContext;

        this.ALBUM_NAME = ALBUM_NAME;
        this.DATE_TAKEN = DATE_TAKEN;
        this.FOLDER_PATH = FOLDER_PATH;

        this.is_photo_or_video = is_photo_or_video;
    }

    public String getAlbumName() {
        return ALBUM_NAME;
    }

    public Bitmap getBitmapFolderThumbnail() {
        return mBitmapFolderThumbnail;
    }

    public void setBitmapFolderThumbnail(Bitmap mBitmapFolderThumbnail) {
        this.mBitmapFolderThumbnail = mBitmapFolderThumbnail;
    }

    public String getDateTaken() {
        return DATE_TAKEN;
    }

    public String getFolderPath() {
        return FOLDER_PATH;
    }

    public boolean getMediaTypeOfLatestBitmapFolderThumbnail() {
        return media_type_of_latest_bitmap_folder_thumbnail;
    }

    public void setMediaTypeOfLatestBitmapFolderThumbnail(boolean media_type_of_latest_bitmap_folder_thumbnail) {
        this.media_type_of_latest_bitmap_folder_thumbnail = media_type_of_latest_bitmap_folder_thumbnail;
    }

    public String getNumberOfPhotos() {
        if (number_of_photos == 0 | number_of_photos == 1)
            return number_of_photos + " " + mContext.getString(R.string.photo);
        else
            return number_of_photos + " " + mContext.getString(R.string.photos);
    }

//    public void setAlbumFilesCount(String ALBUM_FILES_COUNT) {
//        this.ALBUM_FILES_COUNT = ALBUM_FILES_COUNT;
//    }

    public void setNumberOfPhotos(int number_of_photos) {
        this.number_of_photos = number_of_photos;
    }

    public String getNumberOfVideos() {
        if (number_of_videos == 0 | number_of_videos == 1)
            return number_of_videos + " " + mContext.getString(R.string.video);
        else
            return number_of_videos + " " + mContext.getString(R.string.videos);
    }

    public void setNumberOfVideos(int number_of_videos) {
        this.number_of_videos = number_of_videos;
    }
}
