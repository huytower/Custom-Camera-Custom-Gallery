package model;

import android.content.Context;

/**
 * Created by trek2000 on 12/2/2015.
 */
public class File {

    /**
     * String section
     */
    private String DURATION_OF_VIDEO;

    private String FILE_NAME;
    private String FILE_PATH;

    private boolean isChecked = false;
    private boolean isVideo = false;

    /**
     * The others section
     */
    private Context mContext;

    public File(Context mContext, String FILE_NAME, String FILE_PATH, boolean isChecked) {
        this.mContext = mContext;

        this.FILE_NAME = FILE_NAME;
        this.FILE_PATH = FILE_PATH;

        this.isChecked = isChecked;
    }

    public String getDurationOfVideo() {
        return DURATION_OF_VIDEO;
    }

    public void setDurationOfVideo(String DURATION_OF_VIDEO) {
        this.DURATION_OF_VIDEO = DURATION_OF_VIDEO;
    }

    public String getFileName() {
        return FILE_NAME;
    }

    public String getFilePath() {
        return FILE_PATH;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    public boolean isVideo() {
        return isVideo;
    }

    public void setVideo(boolean isVideo) {
        this.isVideo = isVideo;
    }
}
