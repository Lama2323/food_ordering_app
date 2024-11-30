package com.example.foodorderingapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.local.UserIdStorageFactory;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import android.graphics.drawable.Drawable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ProfileActivity extends BaseNetworkActivity {
    private static final String TAG = "ProfileActivity";
    private static final int PICK_IMAGE_REQUEST = 1;

    // UI Components
    private ImageView profileImage;
    private TextView usernameTextView;
    private EditText editNameInput;
    private Button changeImageButton;
    private Button saveButton;
    private Button backButton;
    private Button logoutButton;
    private ProgressBar progressBar;
    private Button changePasswordButton;

    // Data
    private String imageSource = null;
    private boolean isImageUploading = false;
    private boolean isUpdatingProfile = false;
    private boolean hasProfileChanged = false;
    private Uri selectedImageUri = null;

    private static final RequestOptions profileImageOptions = new RequestOptions()
            .placeholder(R.drawable.ic_profile)
            .error(R.drawable.ic_profile)
            .dontAnimate();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initializeViews();
        setupClickListeners();
        loadUserProfile();
    }

    private void initializeViews() {
        profileImage = findViewById(R.id.profile_image);
        usernameTextView = findViewById(R.id.username_text);
        editNameInput = findViewById(R.id.edit_name_input);
        changeImageButton = findViewById(R.id.change_image_button);
        saveButton = findViewById(R.id.save_button);
        backButton = findViewById(R.id.back_button);
        logoutButton = findViewById(R.id.logout_button);
        progressBar = findViewById(R.id.progressBar);
        changePasswordButton = findViewById(R.id.change_password_button);
    }

    private void setupClickListeners() {
        changeImageButton.setOnClickListener(v -> openImageChooser());
        saveButton.setOnClickListener(v -> saveChanges());
        backButton.setOnClickListener(v -> onBackPressed());
        changePasswordButton.setOnClickListener(v -> openChangePassword());
        logoutButton.setOnClickListener(v -> logOut());
    }

    private void loadUserProfile() {
        String userId = getCurrentUserId();
        if (userId == null) {
            showError("Người dùng chưa đăng nhập!");
            finish();
            return;
        }

        showLoading(true);
        Backendless.UserService.findById(userId, new AsyncCallback<BackendlessUser>() {
            @Override
            public void handleResponse(BackendlessUser user) {
                runOnUiThread(() -> {
                    showLoading(false);
                    updateUIWithUserData(user);
                });
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                runOnUiThread(() -> {
                    showLoading(false);
                    showError("Lỗi tải thông tin: " + fault.getMessage());
                    Log.e(TAG, "Error loading user profile: " + fault.getMessage());
                });
            }
        });
    }

    private void updateUIWithUserData(BackendlessUser user) {
        String name = (String) user.getProperty("name");
        String image = (String) user.getProperty("image_source");

        usernameTextView.setText(name != null ? name : "No Name");
        editNameInput.setText(name != null ? name : "");

        if (image != null && !image.isEmpty()) {
            loadProfileImage(image);
        }
    }

    private void loadProfileImage(String imageUrl) {
        Glide.with(this)
                .load(imageUrl)
                .thumbnail(0.5f)
                .apply(profileImageOptions)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<Drawable> target, boolean isFirstResource) {
                        Log.e(TAG, "Image load failed: " + e);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model,
                                                   Target<Drawable> target, DataSource dataSource,
                                                   boolean isFirstResource) {
                        Log.d(TAG, "Image loaded successfully from: " + dataSource);
                        return false;
                    }
                })
                .into(profileImage);
    }

    private void openImageChooser() {
        if (isImageUploading || isUpdatingProfile) {
            showError("Đang xử lý, vui lòng đợi...");
            return;
        }

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                profileImage.setImageBitmap(bitmap);
                uploadImageToBackendless(selectedImageUri);
            } catch (IOException e) {
                Log.e(TAG, "Error loading selected image", e);
                showError("Không thể tải ảnh đã chọn");
            }
        }
    }

    private byte[] getImageBytes(Uri imageUri) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;

        InputStream inputStream = getContentResolver().openInputStream(imageUri);
        Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream, null, options);
        inputStream.close();

        int maxSize = 800;
        float ratio = Math.min(
                (float) maxSize / originalBitmap.getWidth(),
                (float) maxSize / originalBitmap.getHeight()
        );

        int width = Math.round(ratio * originalBitmap.getWidth());
        int height = Math.round(ratio * originalBitmap.getHeight());

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, true);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);

        originalBitmap.recycle();
        resizedBitmap.recycle();

        return byteArrayOutputStream.toByteArray();
    }

    private void uploadImageToBackendless(Uri imageUri) {
        if (isImageUploading) return;

        String userId = getCurrentUserId();
        if (userId == null) return;

        isImageUploading = true;
        showLoading(true);

        String directoryPath = "profile_image/";
        String imageName = "profile_avatar_" + userId + "_" + System.currentTimeMillis() + ".jpg";

        try {
            byte[] imageBytes = getImageBytes(imageUri);
            if (imageBytes == null) {
                showError("Không thể đọc dữ liệu ảnh");
                return;
            }

            Backendless.Files.saveFile(directoryPath, imageName, imageBytes, true, new AsyncCallback<String>() {
                @Override
                public void handleResponse(String fileURL) {
                    imageSource = fileURL;
                    Log.d(TAG, "Image uploaded successfully: " + fileURL);
                    runOnUiThread(() -> {
                        isImageUploading = false;
                        hasProfileChanged = true;
                        updateUserProfile(null);
                    });
                }

                @Override
                public void handleFault(BackendlessFault fault) {
                    runOnUiThread(() -> {
                        isImageUploading = false;
                        showLoading(false);
                        showError("Lỗi tải ảnh lên: " + fault.getMessage());
                        Log.e(TAG, "Failed to upload image: " + fault.getMessage());
                    });
                }
            });
        } catch (Exception e) {
            isImageUploading = false;
            showLoading(false);
            showError("Lỗi xử lý ảnh");
            Log.e(TAG, "Error processing image", e);
        }
    }

    private void updateUserProfile(@Nullable String newName) {
        if (isUpdatingProfile) return;

        String userId = getCurrentUserId();
        if (userId == null) return;

        isUpdatingProfile = true;
        showLoading(true);

        Backendless.UserService.findById(userId, new AsyncCallback<BackendlessUser>() {
            @Override
            public void handleResponse(BackendlessUser user) {
                if (newName != null && !newName.isEmpty()) {
                    user.setProperty("name", newName);
                }
                if (imageSource != null) {
                    user.setProperty("image_source", imageSource);
                }

                Backendless.UserService.update(user, new AsyncCallback<BackendlessUser>() {
                    @Override
                    public void handleResponse(BackendlessUser updatedUser) {
                        runOnUiThread(() -> {
                            isUpdatingProfile = false;
                            showLoading(false);
                            hasProfileChanged = true;

                            if (newName != null) {
                                usernameTextView.setText(newName);
                                showMessage("Cập nhật thông tin thành công!");
                            } else {
                                showMessage("Cập nhật ảnh thành công!");
                            }
                        });
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        runOnUiThread(() -> {
                            isUpdatingProfile = false;
                            showLoading(false);
                            showError("Lỗi cập nhật: " + fault.getMessage());
                            Log.e(TAG, "Failed to update user: " + fault.getMessage());
                        });
                    }
                });
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                runOnUiThread(() -> {
                    isUpdatingProfile = false;
                    showLoading(false);
                    showError("Lỗi tải thông tin người dùng: " + fault.getMessage());
                    Log.e(TAG, "Error finding user: " + fault.getMessage());
                });
            }
        });
    }

    private void saveChanges() {
        if (isImageUploading || isUpdatingProfile) {
            showError("Đang xử lý, vui lòng đợi...");
            return;
        }

        String newName = editNameInput.getText().toString().trim();
        String currentName = usernameTextView.getText().toString();

        if (newName.isEmpty()) {
            showError("Tên không được để trống!");
            return;
        }

        if (newName.equals(currentName) && imageSource == null) {
            showError("Bạn chưa thực hiện thay đổi nào!");
            return;
        }

        updateUserProfile(newName);
    }

    private void logOut() {
        if (isImageUploading || isUpdatingProfile) {
            showError("Đang xử lý, vui lòng đợi...");
            return;
        }

        showLoading(true);
        Backendless.UserService.logout(new AsyncCallback<Void>() {
            @Override
            public void handleResponse(Void response) {
                runOnUiThread(() -> {
                    showLoading(false);
                    showMessage("Đăng xuất thành công!");
                    navigateToLogin();
                });
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                runOnUiThread(() -> {
                    showLoading(false);
                    showError("Lỗi đăng xuất: " + fault.getMessage());
                    Log.e(TAG, "Logout error: " + fault.getMessage());
                });
            }
        });
    }

    private void openChangePassword() {
        if (isImageUploading || isUpdatingProfile) {
            showError("Đang xử lý, vui lòng đợi...");
            return;
        }

        Intent intent = new Intent(this, ChangePasswordActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (isImageUploading || isUpdatingProfile) {
            showError("Đang xử lý, vui lòng đợi...");
            return;
        }
        navigateBack();
    }

    private String getCurrentUserId() {
        return UserIdStorageFactory.instance().getStorage().get();
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }

        changeImageButton.setEnabled(!show);
        saveButton.setEnabled(!show);
        backButton.setEnabled(!show);
        logoutButton.setEnabled(!show);
        changePasswordButton.setEnabled(!show);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void navigateBack() {
        if (hasProfileChanged) {
            Intent intent = new Intent(this, ProductActivity.class);
            intent.putExtra("USER_UPDATED", true);
            startActivity(intent);
        }
        finish();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}