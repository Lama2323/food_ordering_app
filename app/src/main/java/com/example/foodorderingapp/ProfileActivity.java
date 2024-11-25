package com.example.foodorderingapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.persistence.BackendlessDataQuery;
import com.bumptech.glide.Glide;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.backendless.persistence.local.UserIdStorageFactory;
import com.backendless.persistence.local.UserTokenStorageFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView profileImage;
    private TextView usernameTextView;
    private EditText editNameInput;
    private Button changeImageButton, saveButton;
    private String imageSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            profileImage = findViewById(R.id.profile_image);
            changeImageButton = findViewById(R.id.change_image_button);
            saveButton = findViewById(R.id.save_button);
            editNameInput = findViewById(R.id.edit_name_input);
            usernameTextView = findViewById(R.id.username_text);

            SharedPreferences prefs = getSharedPreferences("UserInfo", MODE_PRIVATE);
            String email = prefs.getString("email", null);

            if (email != null)
            {
                String whereClause = "email = '" + email + "'";
                DataQueryBuilder queryBuilder = DataQueryBuilder.create();
                queryBuilder.setWhereClause(whereClause);

                Backendless.Data.of("user").find(queryBuilder, new AsyncCallback<List<Map>>() {
                    @Override
                    public void handleResponse(List<Map> foundUsers) {
                        if (!foundUsers.isEmpty())
                        {
                            Map<String, Object> user = foundUsers.get(0);

                            String name = (String) user.get("name");
                            String imageSource = (String) user.get("image_source");
                            usernameTextView.setText((name != null) ? name : "name null");

                            Glide.with(ProfileActivity.this).load(imageSource).into(profileImage);

                            Log.v("User", "Name: " + name);
                            Log.v("User", "Image Source: " + imageSource);
                        }
                        else
                        {
                            Log.e("User", "User with this email not found: " + email);
                        }
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Log.e("Backendless", "err: " + fault.getMessage());
                    }
                });
            }
            else
            {
                Toast.makeText(ProfileActivity.this, "Email Pref ở Pro5 null", Toast.LENGTH_SHORT).show();
            }


            changeImageButton.setOnClickListener(f -> openImageChooser());

            saveButton.setOnClickListener(f -> saveChanges());

            return insets;
        });
    }

    private void openImageChooser()
    {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            String imagePath = getRealPathFromURI(imageUri);
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                profileImage.setImageBitmap(bitmap);
                imageSource = imagePath;
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Tải ảnh thất bại", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getRealPathFromURI(Uri contentUri)
    {
        String result = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            result = cursor.getString(columnIndex);
            cursor.close();
        }
        return result;
    }

    private void saveChanges() {
        String newName = editNameInput.getText().toString().trim();
        if (newName.isEmpty()) {
            Toast.makeText(this, "Hãy nhập tên mới!", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", MODE_PRIVATE);
        String email = sharedPreferences.getString("email", null);

        if (email == null) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng!", Toast.LENGTH_SHORT).show();
            return;
        }

        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setWhereClause("email = '" + email + "'");

        Backendless.Data.of("user").find(queryBuilder, new AsyncCallback<List<Map>>() {
            @Override
            public void handleResponse(List<Map> response) {
                if (response != null && !response.isEmpty()) {
                    Map<String, Object> userMap = response.get(0);

                    userMap.put("name", newName);
                    userMap.put("image_source", imageSource);

                    Backendless.Data.of("user").save(userMap, new AsyncCallback<Map>() {
                        @Override
                        public void handleResponse(Map response) {
                            Glide.with(ProfileActivity.this)
                                    .load(imageSource)
                                    .into(profileImage);

                            Log.v("UpdateUser", "Update user success");
                            Toast.makeText(getApplicationContext(), "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {
                            Log.v("UpdateUser", "Update user failed");
                            Toast.makeText(getApplicationContext(), "Lỗi khi lưu thông tin: " + fault.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "Không tìm thấy người dùng với email: " + email, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(getApplicationContext(), "Lỗi khi tìm kiếm người dùng: " + fault.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}