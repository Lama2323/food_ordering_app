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

import java.io.IOException;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView profileImage;
    private TextView usernameTextView;
    private EditText editNameInput;
    private Button changeImageButton, saveButton, backButton;
    private String imageSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileImage = findViewById(R.id.profile_image);
        changeImageButton = findViewById(R.id.change_image_button);
        saveButton = findViewById(R.id.save_button);
        backButton = findViewById(R.id.back_button);
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
    }

    private void backToCartActivity()
    {
        startActivity(new Intent(ProfileActivity.this, CartActivity.class));
        finish();
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
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                profileImage.setImageBitmap(bitmap);
                imageSource = imageUri.toString();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Tải ảnh thất bại", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveChanges() {
        String newName = editNameInput.getText().toString().trim();
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
                if (!newName.isEmpty())
                    user.setProperty("name", newName);
                if (imageSource != null) {
                    user.setProperty("image_source", imageSource);
                }

                Backendless.UserService.update(user, new AsyncCallback<BackendlessUser>() {
                    @Override
                    public void handleResponse(BackendlessUser updatedUser) {
                        if (imageSource != null)
                        {
                            Glide.with(ProfileActivity.this)
                                    .load(imageSource)
                                    .into(profileImage);
                        }
                        usernameTextView.setText((!newName.isEmpty()) ? newName : usernameTextView.getText());

                        Log.v("UpdateUser", "User updated successfully");
                        Toast.makeText(ProfileActivity.this, "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Log.e("UpdateUser", "Failed to update user: " + fault.getMessage());
                        Toast.makeText(ProfileActivity.this, "Lỗi khi lưu: " + fault.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e("FindUser", "Error finding user: " + fault.getMessage());
                Toast.makeText(ProfileActivity.this, "Lỗi khi cập nhật thông tin: " + fault.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
