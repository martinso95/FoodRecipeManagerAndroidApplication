package martin.so.foodrecipemanager.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.content.Context.MODE_PRIVATE;

/**
 * Class for handling added or deleted files in Firebase storage when the user is offline.
 * Updates Firebase Storage with the correct additions/deletions when the user comes online.
 */
public class FirebaseStorageOfflineHandler {

    private static FirebaseStorageOfflineHandler firebaseStorageOfflineHandlerInstance = new FirebaseStorageOfflineHandler();

    private List<String> filesForDeletionInFirebaseStorage = new ArrayList<>();
    private HashMap<String, Bitmap> temporaryPhotoBitmaps = new HashMap<>();

    private SharedPreferences.Editor sharedPreferencesEditor;
    private SharedPreferences sharedPreferences;

    private File localDirectoryFileCache;

    public static FirebaseStorageOfflineHandler getInstance() {
        return firebaseStorageOfflineHandlerInstance;
    }

    private FirebaseStorageOfflineHandler() {
    }

    /**
     * Initialize FirebaseStorageOfflineHandler. Update Firebase Storage with additions or deletions of files, based on local cache.
     */
    public void initializeFirebaseStorageOfflineHandler(Context context) {
        sharedPreferences = context.getSharedPreferences("FoodRecipeManager", MODE_PRIVATE);
        sharedPreferencesEditor = context.getSharedPreferences("FoodRecipeManager", MODE_PRIVATE).edit();
        Gson gson = new Gson();

        String string = sharedPreferences.getString("recipePhotosToDelete", null);

        Type type = new TypeToken<List<String>>() {
        }.getType();
        List<String> recipePhotosToDelete = gson.fromJson(string, type);
        if (recipePhotosToDelete != null)
            filesForDeletionInFirebaseStorage.addAll(recipePhotosToDelete);

        localDirectoryFileCache = getLocalDirectoryFileCache(context);

        File[] files = localDirectoryFileCache.listFiles();
        for (File file : files) {
            String fileName = file.getName();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Bitmap bitmap = loadFile(file.getName());

            // Save bitmap temporarily for this session, so that recipes can load local photos,
            // in order to avoid the recipes not having any picture, due to delay in Firebase loading.
            temporaryPhotoBitmaps.put(fileName, bitmap);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            UploadTask uploadTask = FirebaseStorage.getInstance().getReference().child(Utils.FIREBASE_IMAGES_PATH).child(FirebaseAuth.getInstance().getUid()).child(fileName).putBytes(data);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    deleteFile(fileName);
                }
            });
        }

        final int[] filesForDeletionInFirebaseStorageCount = {filesForDeletionInFirebaseStorage.size()};
        for (String recipePhotoToDelete : filesForDeletionInFirebaseStorage) {
            StorageReference photoRef = FirebaseStorage.getInstance().getReference().child(Utils.FIREBASE_IMAGES_PATH).child(FirebaseAuth.getInstance().getUid()).child(recipePhotoToDelete);
            photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    filesForDeletionInFirebaseStorage.remove(recipePhotoToDelete);
                    filesForDeletionInFirebaseStorageCount[0]--;

                    // Save/Update the deletion list after last item has been processed.
                    if (filesForDeletionInFirebaseStorageCount[0] == 0) {
                        Gson gson = new Gson();
                        String json = gson.toJson(filesForDeletionInFirebaseStorage);

                        sharedPreferencesEditor.putString("recipePhotosToDelete", json);
                        sharedPreferencesEditor.apply();
                    }
                }
            });
        }
    }

    /**
     * Temporarily add file in internal storage to be added to Firebase Storage once online.
     *
     * @param fileName Name of the file.
     * @param bitmap   The photo bitmap to be stored.
     */
    public void addFileForUploadInFirebaseStorage(String fileName, Bitmap bitmap) {
        createFile(fileName, bitmap);
    }

    /**
     * Remove file in internal storage that was to be added to Firebase Storage once online.
     *
     * @param fileName Name of the file.
     */
    public void removeFileForUploadInFirebaseStorage(String fileName) {
        deleteFile(fileName);
    }

    /**
     * Adds a recipe's photo name to the list that tracks what is going to be deleted,
     * once there is connection to Firebase Storage.
     * This list contains recipe photo's UID name, and the list is stored using SharedPreferences.
     *
     * @param fileName The name of the recipe's photo. Which is a UID.
     */
    public void addFileForDeletionInFirebaseStorage(String fileName) {
        filesForDeletionInFirebaseStorage.add(fileName);

        Gson gson = new Gson();
        String json = gson.toJson(filesForDeletionInFirebaseStorage);

        sharedPreferencesEditor.putString("recipePhotosToDelete", json);
        sharedPreferencesEditor.apply();
    }

    /**
     * Removes a recipe's photo name from the list that tracks what is going to be deleted,
     * once there is connection to Firebase Storage.
     * This list contains recipe photo's UID name, and the list is stored using SharedPreferences.
     *
     * @param fileName The name of the recipe's photo. Which is a UID.
     */
    public void removeFileForDeletionInFirebaseStorage(String fileName) {
        filesForDeletionInFirebaseStorage.remove(fileName);

        Gson gson = new Gson();
        String json = gson.toJson(filesForDeletionInFirebaseStorage);

        sharedPreferencesEditor.putString("recipePhotosToDelete", json);
        sharedPreferencesEditor.apply();
    }

    /**
     * Creates and saves a photo, which is a photo from a bitmap.
     *  @param fileName The name of the photo file to be saved.
     * @param bitmap   The photo bitmap to be stored.
     */
    private void createFile(String fileName, Bitmap bitmap) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(new File(localDirectoryFileCache, fileName));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Loads a photo and returns its Bitmap.
     *
     * @param fileName The name of the photo file to be loaded.
     */
    private Bitmap loadFile(String fileName) {
        if (fileExists(fileName)) {
            FileInputStream inputStream = null;
            try {
                inputStream = new FileInputStream(new File(localDirectoryFileCache, fileName));
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
     * Deletes a file.
     *
     * @param fileName The name of the file to be deleted.
     */
    private void deleteFile(String fileName) {
        if (fileExists(fileName)) {
            new File(localDirectoryFileCache, fileName).delete();
        }
    }

    boolean fileExists(String fileName) {
        File[] files = localDirectoryFileCache.listFiles();
        for (File file : files) {
            String currentFile = file.getName();
            if (currentFile.equals(fileName)) {
                return true;
            }
        }
        return false;
    }

    private File getLocalDirectoryFileCache(Context context) {
        String localFileDirectoryName = "fileCache";
        File directory = context.getDir(localFileDirectoryName, Context.MODE_PRIVATE);
        if (!directory.exists() && !directory.mkdirs()) {
            Log.e("Test", "Error creating directory " + directory);
        }
        return directory;
    }

    public Bitmap getLocalRecipeBitmap(String name) {
        if (temporaryPhotoBitmaps.containsKey(name)) {
            return temporaryPhotoBitmaps.get(name);
        }
        return null;
    }

}
