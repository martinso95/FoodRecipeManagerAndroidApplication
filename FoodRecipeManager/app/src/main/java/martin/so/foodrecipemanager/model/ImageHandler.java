package martin.so.foodrecipemanager.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Image handler class. Allows for saving and loading photos in storage.
 */
public class ImageHandler {

    private static final String TAG = ImageHandler.class.getName();

    private String directoryName = Utils.PHOTO_STORAGE_DIRECTORY;
    private Context context;
    private boolean external;
    private String imageFileFormat = ".jpg";

    public ImageHandler(Context context) {
        this.context = context;
    }

    /**
     * Creates and saves an image, which is an image from a file path.
     *
     * @param fileName The name of the image file to be saved.
     * @param filePath The file path of the image that comes from the phone's photo storage.
     */
    public boolean createImageFile(String fileName, String filePath) {
        boolean createImageFileCreated;
        File directory = getImageDirectory();

        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(new File(directory, fileName + imageFileFormat));
            Bitmap bitmap = BitmapFactory.decodeFile(filePath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            createImageFileCreated = true;
        } catch (Exception e) {
            e.printStackTrace();
            createImageFileCreated = false;
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return createImageFileCreated;
    }

    /**
     * Loads an image and returns its Bitmap.
     *
     * @param fileName The name of the image file to be loaded.
     */
    public Bitmap loadImageFile(String fileName) {
        if (imageFileExists(fileName)) {
            File directory = getImageDirectory();

            FileInputStream inputStream = null;
            try {
                inputStream = new FileInputStream(new File(directory, fileName + imageFileFormat));
                return BitmapFactory.decodeStream(inputStream);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        } else {
            return null;
        }
    }

    /**
     * Deletes an image.
     *
     * @param fileName The name of the image file to be deleted.
     */
    public void deleteImageFile(String fileName) {
        if (imageFileExists(fileName)) {
            File directory = getImageDirectory();
            new File(directory, fileName + imageFileFormat).delete();
        }
    }

    /**
     * Edits an image's name.
     *
     * @param fileName The name of the image file to be edited.
     */
    public boolean editImageFileName(String fileName, String newFileName) {
        File directory = getImageDirectory();
        File[] files = directory.listFiles();
        for (int i = 0; i < files.length; i++) {
            String currentFile = files[i].getName();
            if (currentFile.substring(0, currentFile.indexOf(".")).equals(fileName)) {
                File newFile = new File(directory, newFileName + imageFileFormat);
                files[i].renameTo(newFile);
                return true;
            }
        }
        return false;
    }

    private boolean imageFileExists(String fileName) {
        File directory = getImageDirectory();
        File[] files = directory.listFiles();
        for (int i = 0; i < files.length; i++) {
            String currentFile = files[i].getName();
            if (currentFile.substring(0, currentFile.indexOf(".")).equals(fileName)) {
                return true;
            }
        }
        return false;
    }

    private File getImageDirectory() {
        File directory;
        if (external) {
            directory = getAlbumStorageDir(directoryName);
        } else {
            directory = context.getDir(directoryName, Context.MODE_PRIVATE);
        }
        if (!directory.exists() && !directory.mkdirs()) {
            Log.e(TAG, "Error creating directory " + directory);
        }
        return directory;
    }

    public void setImageFileFormat(String format) {
        this.imageFileFormat = format;
    }

    private File getAlbumStorageDir(String albumName) {
        return new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName);
    }

    public ImageHandler setExternal(boolean external) {
        this.external = external;
        return this;
    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }
}