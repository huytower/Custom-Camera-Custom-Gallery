package model.custom_gallery;

import android.content.Context;
import android.graphics.Bitmap;


/**
 * Created by trek2000 on 12/2/2015.
 */
public class AlbumFileInside {

    /**
     * String section
     */
    private String ALBUM_NAME = null;
    private String FILE_NAME = null;
    private String FILE_PATH = null;

    private Bitmap mBitmapFile;

    /**
     * The others section
     */
    private Context mContext;

    /**
     * @param mContext
     * @param ALBUM_NAME
     * @param FILE_NAME
     * @param FILE_PATH
     * @param mBitmapFile
     */
    public AlbumFileInside(
            Context mContext, String ALBUM_NAME,
            String FILE_NAME, String FILE_PATH, Bitmap mBitmapFile) {
        this.mContext = mContext;

        this.ALBUM_NAME = ALBUM_NAME;
        this.FILE_NAME = FILE_NAME;

        this.FILE_PATH = FILE_PATH;

        this.mBitmapFile = mBitmapFile;
    }

    public String getAlbumName() {
        return ALBUM_NAME;
    }

    public String getFileName() {
        return FILE_NAME;
    }

    public String getFilePath() {
        return FILE_PATH;
    }

    public Bitmap getBitmapFile() {
        return mBitmapFile;
    }
}
