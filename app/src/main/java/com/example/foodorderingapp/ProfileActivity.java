package com.example.foodorderingapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.BackendlessUser;
import com.bumptech.glide.Glide;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.local.UserIdStorageFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView profileImage;
    private TextView usernameTextView;
    private EditText editNameInput;
    private Button changeImageButton, saveButton, backButton, logoutButton;
    private String imageSource = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileImage = findViewById(R.id.profile_image);
        changeImageButton = findViewById(R.id.change_image_button);
        saveButton = findViewById(R.id.save_button);
        backButton = findViewById(R.id.back_button);
        logoutButton = findViewById(R.id.logout_button);
        editNameInput = findViewById(R.id.edit_name_input);
        usernameTextView = findViewById(R.id.username_text);

        //Lấy người dùng hiện tại từ backendless
        String userId = UserIdStorageFactory.instance().getStorage().get();
        if (userId == null) {
            Toast.makeText(this, "Người dùng chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        //load ảnh và tên từ db
        Backendless.UserService.findById(userId, new AsyncCallback<BackendlessUser>() {
            @Override
            public void handleResponse(BackendlessUser user) {
                String name = (String) user.getProperty("name");
                String image = (String) user.getProperty("image_source");

                usernameTextView.setText(name != null ? name : "No Name");
                if (image != null) {
                    Glide.with(ProfileActivity.this).load(image).into(profileImage);
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e("Backendless", "Error loading user: " + fault.getMessage());
                Toast.makeText(ProfileActivity.this, "Lỗi: " + fault.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        changeImageButton.setOnClickListener(f -> openImageChooser());
        saveButton.setOnClickListener(f -> saveChanges());
        backButton.setOnClickListener(f -> backToCartActivity());
        logoutButton.setOnClickListener(f -> logOut());
    }

    private byte[] readInputStreamAsBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;

        while ((length = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, length);
        }

        return byteArrayOutputStream.toByteArray();
    }

    private void uploadImageToBackendless(Uri imageUri) {
        String directoryPath = "profile_image/";
        String userId = UserIdStorageFactory.instance().getStorage().get();

        if (userId == null) {
            Toast.makeText(this, "Người dùng chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        String imageName = "profile_avatar_" + userId + ".jpg";

        byte[] imageBytes = null;

        try (InputStream inputStream = getContentResolver().openInputStream(imageUri)) {
            if (inputStream != null) {
                imageBytes = readInputStreamAsBytes(inputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi đọc hình ảnh", Toast.LENGTH_SHORT).show();
        }

            Backendless.Files.saveFile(directoryPath, imageName, imageBytes, true, new AsyncCallback<String>() {
                @Override
                public void handleResponse(String fileURL) {
                    imageSource = fileURL;
                    Log.v("UploadImage", "File uploaded: " + fileURL);

                    Toast.makeText(ProfileActivity.this, "Tải ảnh thành công!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void handleFault(BackendlessFault fault) {
                    Log.e("UploadImage", "Failed to upload file: " + fault.getMessage());
                    Toast.makeText(ProfileActivity.this, "Lỗi khi tải ảnh: " + fault.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }


    private void backToCartActivity()
    {
        startActivity(new Intent(ProfileActivity.this, CartActivity.class));
        finish();
    }

    private void logOut()
    {
        Backendless.UserService.logout(new AsyncCallback<Void>() {
            @Override
            public void handleResponse(Void response) {

                //có thể cần xoá data từ phiên làm việc cũ ở đây
                //deleteDataSession();

                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

                Toast.makeText(ProfileActivity.this, "Đăng xuất thành công!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e("LogoutError", "Lỗi đăng xuất: " + fault.getMessage());
                Toast.makeText(ProfileActivity.this, "Lỗi khi đăng xuất: " + fault.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                //hiển thị ảnh lên
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                profileImage.setImageBitmap(bitmap);

                uploadImageToBackendless(imageUri);

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Tải ảnh thât bại", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveChanges() {
        String newName = editNameInput.getText().toString().trim();

        Log.d("Pro5Activity", "save change b4 check");
        if (newName.isEmpty() && imageSource == null) {
            Toast.makeText(this, "Bạn chưa thực hiện thay đổi nào!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = UserIdStorageFactory.instance().getStorage().get();
        if (userId == null) {
            Toast.makeText(this, "Người dùng chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        Backendless.UserService.findById(userId, new AsyncCallback<BackendlessUser>() {
            @Override
            public void handleResponse(BackendlessUser user) {
                user.setProperty("name", newName);

                if (imageSource != null) {
                    user.setProperty("image_source", imageSource);
                }

                Backendless.UserService.update(user, new AsyncCallback<BackendlessUser>() {
                    @Override
                    public void handleResponse(BackendlessUser updatedUser) {
                        usernameTextView.setText(newName);
                        Log.v("UpdateUser", "User updated successfully");
                        Toast.makeText(ProfileActivity.this, "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Log.e("UpdateUser", "Failed to update user: " + fault.getMessage());
                        Toast.makeText(ProfileActivity.this, "Lỗi khi cập nhật: " + fault.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e("FindUser", "Error finding user: " + fault.getMessage());
                Toast.makeText(ProfileActivity.this, "Lỗi khi cập nhật: " + fault.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        }
}
