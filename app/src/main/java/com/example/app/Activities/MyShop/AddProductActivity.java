package com.example.app.Activities.MyShop;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.example.app.BuildConfig;
import com.example.app.CustomMessageBox.FailToast;
import com.example.app.CustomMessageBox.SuccessfulToast;
import com.example.app.Dialog.UploadDialog;
import com.example.app.Model.Product;
import com.example.app.R;
import com.example.app.databinding.ActivityAddProductBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.uploadcare.android.library.api.UploadcareClient;
import com.uploadcare.android.library.api.UploadcareFile;
import com.uploadcare.android.library.upload.FileUploader;
import com.uploadcare.android.library.callbacks.UploadFileCallback;
import com.uploadcare.android.library.exceptions.UploadcareApiException;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class AddProductActivity extends AppCompatActivity {
    private ActivityAddProductBinding binding;
    private int position;
    private int PERMISSION_REQUEST_CODE = 10001;
    private UploadDialog uploadDialog;
    private Uri uri1, uri2, uri3, uri4;
    private String img1 = "", img2 = "", img3 = "", img4 = "";
    private String imgOld1 = "", imgOld2 = "", imgOld3 = "", imgOld4 = "";
    private Product productUpdate = null;
    private boolean checkUpdate = false;
    private String userId;
    private static final int FIRST_IMAGE = 1;
    private static final int SECOND_IMAGE = 2;
    private static final int THIRD_IMAGE = 3;
    private static final int FOURTH_IMAGE = 4;
    private UploadcareClient uploadcareClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getWindow().setStatusBarColor(Color.parseColor("#E8584D"));
        getWindow().setNavigationBarColor(Color.parseColor("#E8584D"));

        // Khởi tạo UploadcareClient
        uploadcareClient = new UploadcareClient(BuildConfig.UPLOADCARE_PUBLIC_KEY, BuildConfig.UPLOADCARE_SECRET_KEY);

        // Nhận intent từ edit
        Intent intentUpdate = getIntent();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (intentUpdate != null && intentUpdate.hasExtra("Product updating")) {
            productUpdate = (Product) intentUpdate.getSerializableExtra("Product updating");
            checkUpdate = true;
            binding.lnAddProduct.btnAddProduct.setText("Update");
            binding.lnAddProduct.edtNameOfProduct.setText(productUpdate.getProductName());
            binding.lnAddProduct.edtAmount.setText(productUpdate.getRemainAmount() + "");
            binding.lnAddProduct.edtDescp.setText(productUpdate.getDescription());
            binding.lnAddProduct.edtPrice.setText(productUpdate.getProductPrice() + "");
            if (productUpdate.getProductType().equals("Balo")) {
                binding.lnAddProduct.rbBalo.setChecked(true);
            } else {
                binding.lnAddProduct.rbProduct.setChecked(true);
            }
            imgOld1 = productUpdate.getProductImage1();
            imgOld2 = productUpdate.getProductImage2();
            imgOld3 = productUpdate.getProductImage3();
            imgOld4 = productUpdate.getProductImage4();

            if (!imgOld1.isEmpty()) {
                binding.layout1.setVisibility(View.GONE);
                Glide.with(this)
                        .asBitmap()
                        .load(imgOld1)
                        .placeholder(R.drawable.background_loading_layout)
                        .into(binding.imgProduct1);
            }
            if (!imgOld2.isEmpty()) {
                binding.layout2.setVisibility(View.GONE);
                Glide.with(this)
                        .asBitmap()
                        .load(imgOld2)
                        .placeholder(R.drawable.background_loading_layout)
                        .into(binding.imgProduct2);
            }
            if (!imgOld3.isEmpty()) {
                binding.layout3.setVisibility(View.GONE);
                Glide.with(this)
                        .asBitmap()
                        .load(imgOld3)
                        .placeholder(R.drawable.background_loading_layout)
                        .into(binding.imgProduct3);
            }
            if (!imgOld4.isEmpty()) {
                binding.layout4.setVisibility(View.GONE);
                Glide.with(this)
                        .asBitmap()
                        .load(imgOld4)
                        .placeholder(R.drawable.background_loading_layout)
                        .into(binding.imgProduct4);
            }
        }

        position = -1;
        binding.addImage1.setOnClickListener(view -> {
            position = 1;
            checkRuntimePermission();
        });
        binding.addImage2.setOnClickListener(view -> {
            position = 2;
            checkRuntimePermission();
        });
        binding.addImage3.setOnClickListener(view -> {
            position = 3;
            checkRuntimePermission();
        });
        binding.addImage4.setOnClickListener(view -> {
            position = 4;
            checkRuntimePermission();
        });
        binding.lnAddProduct.btnAddProduct.setOnClickListener(view -> {
            if (checkLoi()) {
                uploadDialog = new UploadDialog(AddProductActivity.this);
                uploadDialog.show();
                uploadImage(FIRST_IMAGE);
            }
        });
        binding.imgBack.setOnClickListener(view -> finish());
    }

    private void deleteOldImage(int position) {
        StringBuilder imageURL = new StringBuilder();
        handleImagePosition(imageURL, position);
        String imageUrlStr = imageURL.toString();
        if (!imageUrlStr.isEmpty()) {
            if (imageUrlStr.startsWith("https://ucarecdn.com")) {
                // Xóa ảnh Uploadcare
                new Thread(() -> {
                    try {
                        String uuid = imageUrlStr.replace("https://ucarecdn.com/", "").split("/")[0];
                        uploadcareClient.deleteFile(uuid);
                        runOnUiThread(() -> {
                            if (position == FOURTH_IMAGE) {
                                uploadDialog.dismiss();
                                new SuccessfulToast(AddProductActivity.this, "Update successfully!").showToast();
                                finish();
                            } else {
                                deleteOldImage(position + 1);
                            }
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            if (position == FOURTH_IMAGE) {
                                uploadDialog.dismiss();
                                new SuccessfulToast(AddProductActivity.this, "Update successfully!").showToast();
                                finish();
                            } else {
                                deleteOldImage(position + 1);
                            }
                        });
                    }
                }).start();
            } else if (imageUrlStr.startsWith("https://firebasestorage.googleapis.com")) {
                // Xóa ảnh Firebase Storage
                new Thread(() -> {
                    try {
                        FirebaseStorage.getInstance().getReferenceFromUrl(imageUrlStr).delete().addOnCompleteListener(task -> {
                            runOnUiThread(() -> {
                                if (position == FOURTH_IMAGE) {
                                    uploadDialog.dismiss();
                                    new SuccessfulToast(AddProductActivity.this, "Update successfully!").showToast();
                                    finish();
                                } else {
                                    deleteOldImage(position + 1);
                                }
                            });
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            if (position == FOURTH_IMAGE) {
                                uploadDialog.dismiss();
                                new SuccessfulToast(AddProductActivity.this, "Update successfully!").showToast();
                                finish();
                            } else {
                                deleteOldImage(position + 1);
                            }
                        });
                    }
                }).start();
            } else {
                // Bỏ qua nếu URL không hợp lệ
                if (position != FOURTH_IMAGE) {
                    deleteOldImage(position + 1);
                } else {
                    uploadDialog.dismiss();
                    new SuccessfulToast(AddProductActivity.this, "Update successfully!").showToast();
                    finish();
                }
            }
        } else {
            if (position != FOURTH_IMAGE) {
                deleteOldImage(position + 1);
            } else {
                uploadDialog.dismiss();
                new SuccessfulToast(AddProductActivity.this, "Update successfully!").showToast();
                finish();
            }
        }
    }

    private void handleImagePosition(StringBuilder imageURL, int position) {
        if (position == FIRST_IMAGE) {
            if (!img1.equals(imgOld1)) {
                imageURL.append(imgOld1);
            }
        } else if (position == SECOND_IMAGE) {
            if (!img2.equals(imgOld2)) {
                imageURL.append(imgOld2);
            }
        } else if (position == THIRD_IMAGE) {
            if (!img3.equals(imgOld3)) {
                imageURL.append(imgOld3);
            }
        } else {
            if (!img4.equals(imgOld4)) {
                imageURL.append(imgOld4);
            }
        }
    }

    public void pickImg() {
        Dexter.withContext(this)
                .withPermission(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                        ? Manifest.permission.READ_MEDIA_IMAGES
                        : Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        pickImageLauncher.launch(intent);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        new FailToast(AddProductActivity.this, "Permission denied!").showToast();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                        new FailToast(AddProductActivity.this, "Permission denied!").showToast();
                    }
                }).check();
    }

    public boolean checkLoi() {
        try {
            String name = binding.lnAddProduct.edtNameOfProduct.getText().toString();
            double price = Double.parseDouble(binding.lnAddProduct.edtPrice.getText().toString() + ".0");
            int amount = Integer.parseInt(binding.lnAddProduct.edtAmount.getText().toString());
            String description = binding.lnAddProduct.edtDescp.getText().toString();
            if (!checkUpdate) {
                if (img1.isEmpty()) {
                    createDialog("Vui lòng chọn ít nhất 1 hình").create().show();
                    return false;
                } else if (name.isEmpty() || name.length() < 8) {
                    createDialog("Tên ít nhất phải từ 8 kí tự và không được bỏ trống").create().show();
                    return false;
                } else if (price < 5000.0) {
                    createDialog("Giá phải từ 5000 trở lên").create().show();
                    return false;
                } else if (amount <= 0) {
                    createDialog("Số lượng phải lớn hơn 0").create().show();
                    return false;
                } else if (description.isEmpty() || description.length() < 10) {
                    createDialog("Phần mô tả phải từ 10 ký tự trở lên và không được bỏ trống").create().show();
                    return false;
                }
            } else if (name.isEmpty() || name.length() < 8) {
                createDialog("Tên ít nhất phải từ 8 kí tự và không được bỏ trống").create().show();
                return false;
            } else if (price < 5000.0) {
                createDialog("Giá phải từ 5000 trở lên").create().show();
                return false;
            } else if (amount <= 0) {
                createDialog("Số lượng phải lớn hơn 0").create().show();
                return false;
            } else if (description.isEmpty() || description.length() < 10) {
                createDialog("Phần mô tả phải từ 10 ký tự trở lên và không được bỏ trống").create().show();
                return false;
            }
            return true;
        } catch (Exception e) {
            createDialog("Price và Amount chỉ được nhập ký tự là số và không được bỏ trống").create().show();
            return false;
        }
    }

    public AlertDialog.Builder createDialog(String content) {
        AlertDialog.Builder builder = new AlertDialog.Builder(AddProductActivity.this);
        builder.setTitle("Thông báo");
        builder.setMessage(content);
        builder.setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.cancel());
        builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());
        builder.setIcon(R.drawable.icon_dialog_alert_addproduct);
        return builder;
    }

    public void uploadProduct(Product tmp) {
        if (checkUpdate) {
            tmp.setProductId(productUpdate.getProductId());
            FirebaseDatabase.getInstance().getReference().child("Products").child(productUpdate.getProductId()).setValue(tmp).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        deleteOldImage(FIRST_IMAGE);
                    } else {
                        uploadDialog.dismiss();
                        new FailToast(AddProductActivity.this, "Some errors occurred!").showToast();
                        finish();
                    }
                }
            });
        } else {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Products").push();
            tmp.setProductId(reference.getKey() + "");
            reference.setValue(tmp).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        uploadDialog.dismiss();
                        finish();
                        new SuccessfulToast(AddProductActivity.this, "Add product successfully!").showToast();
                    } else {
                        uploadDialog.dismiss();
                        new FailToast(AddProductActivity.this, "Some error occurred!").showToast();
                    }
                }
            });
        }
    }

    public void uploadImage(int position) {
        Uri uri;
        if (position == SECOND_IMAGE) {
            uri = uri2;
        } else if (position == THIRD_IMAGE) {
            uri = uri3;
        } else if (position == FOURTH_IMAGE) {
            uri = uri4;
        } else {
            uri = uri1;
        }
        if (uri != null) {
            uploadDialog.show();
            new Thread(() -> {
                try {
                    if (!isNetworkAvailable()) {
                        runOnUiThread(() -> {
                            uploadDialog.dismiss();
                            new FailToast(AddProductActivity.this, "No internet connection").showToast();
                        });
                        return;
                    }

                    FileUploader uploader = new FileUploader(uploadcareClient, uri, AddProductActivity.this).store(true);
                    uploader.uploadAsync(new UploadFileCallback() {
                        @Override
                        public void onFailure(UploadcareApiException e) {
                            Log.e("UploadcareUpload", "Upload failed: " + e.getMessage());
                            runOnUiThread(() -> {
                                uploadDialog.dismiss();
                                new FailToast(AddProductActivity.this, "Uploadcare upload failed: " + e.getMessage()).showToast();
                            });
                        }

                        @Override
                        public void onProgressUpdate(long bytesWritten, long contentLength, double progress) {}

                        @Override
                        public void onSuccess(UploadcareFile result) {
                            String imageUrl = result.getOriginalFileUrl().toString();
                            Log.d("UploadcareUpload", "Image uploaded: " + imageUrl);
                            runOnUiThread(() -> {
                                switch (position) {
                                    case FIRST_IMAGE: img1 = imageUrl; break;
                                    case SECOND_IMAGE: img2 = imageUrl; break;
                                    case THIRD_IMAGE: img3 = imageUrl; break;
                                    case FOURTH_IMAGE: img4 = imageUrl; break;
                                }
                                if (position == FOURTH_IMAGE) {
                                    Product tmp = new Product(
                                            "null",
                                            binding.lnAddProduct.edtNameOfProduct.getText().toString(),
                                            img1, img2, img3, img4,
                                            Integer.parseInt(binding.lnAddProduct.edtPrice.getText().toString()),
                                            binding.lnAddProduct.rbProduct.isChecked() ? "TechAccessory" : "Balo",
                                            Integer.parseInt(binding.lnAddProduct.edtAmount.getText().toString()),
                                            0,
                                            binding.lnAddProduct.edtDescp.getText().toString(),
                                            0.0, 0, userId, ""
                                    );
                                    uploadProduct(tmp);
                                } else {
                                    uploadImage(position + 1);
                                }
                            });
                        }
                    });
                } catch (Exception e) {
                    Log.e("UploadcareUpload", "Upload error: " + e.getMessage());
                    runOnUiThread(() -> {
                        uploadDialog.dismiss();
                        new FailToast(AddProductActivity.this, "Uploadcare upload failed: " + e.getMessage()).showToast();
                    });
                }
            }).start();
        } else {
            if (position != FOURTH_IMAGE) {
                switch (position) {
                    case FIRST_IMAGE: img1 = imgOld1; break;
                    case SECOND_IMAGE: img2 = imgOld2; break;
                    case THIRD_IMAGE: img3 = imgOld3; break;
                }
                uploadImage(position + 1);
            } else {
                img4 = imgOld4;
                Product tmp = new Product(
                        "null",
                        binding.lnAddProduct.edtNameOfProduct.getText().toString(),
                        img1, img2, img3, img4,
                        Integer.parseInt(binding.lnAddProduct.edtPrice.getText().toString()),
                        binding.lnAddProduct.rbProduct.isChecked() ? "TechAccessory" : "Balo",
                        Integer.parseInt(binding.lnAddProduct.edtAmount.getText().toString()),
                        0,
                        binding.lnAddProduct.edtDescp.getText().toString(),
                        0.0, 0, userId, ""
                );
                uploadProduct(tmp);
            }
        }
    }

    private File getFileFromUri(Uri uri) {
        try {
            String fileName = "temp_image_" + System.currentTimeMillis() + ".jpg";
            File cacheDir = getCacheDir();
            File file = new File(cacheDir, fileName);

            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                FileOutputStream outputStream = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                inputStream.close();
                outputStream.close();
                return file;
            }
        } catch (Exception e) {
            Log.e("FileFromUri", "Error accessing file: " + e.getMessage());
        }
        return null;
    }

    ActivityResultLauncher pickImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK) {
            Intent intent = result.getData();
            if (intent != null) {
                switch (position) {
                    case 1:
                        uri1 = intent.getData();
                        img1 = uri1.toString();
                        binding.layout1.setVisibility(View.GONE);
                        binding.imgProduct1.setImageURI(uri1);
                        break;
                    case 2:
                        uri2 = intent.getData();
                        img2 = uri2.toString();
                        binding.layout2.setVisibility(View.GONE);
                        binding.imgProduct2.setImageURI(uri2);
                        break;
                    case 3:
                        uri3 = intent.getData();
                        img3 = uri3.toString();
                        binding.layout3.setVisibility(View.GONE);
                        binding.imgProduct3.setImageURI(uri3);
                        break;
                    case 4:
                        uri4 = intent.getData();
                        img4 = uri4.toString();
                        binding.layout4.setVisibility(View.GONE);
                        binding.imgProduct4.setImageURI(uri4);
                        break;
                }
            }
        }
    });

    private void checkRuntimePermission() {
        if (isPermissionGranted()) {
            pickImg();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_IMAGES)
                : ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            buildAlertPermissionDialog().create().show();
        } else {
            requestRuntimePermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImg();
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                    ? !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_IMAGES)
                    : !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                buildAlertDeniedPermissionDialog().create().show();
            } else {
                checkRuntimePermission();
            }
        }
    }

    private AlertDialog.Builder buildAlertPermissionDialog() {
        AlertDialog.Builder builderDialog = new AlertDialog.Builder(this);
        builderDialog.setTitle("Notice")
                .setMessage("Bạn cần cấp quyền để thực hiện tính năng này")
                .setPositiveButton("Ok", (dialogInterface, i) -> {
                    requestRuntimePermission();
                    dialogInterface.dismiss();
                })
                .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());
        return builderDialog;
    }

    private AlertDialog.Builder buildAlertDeniedPermissionDialog() {
        AlertDialog.Builder builderDialog = new AlertDialog.Builder(this);
        builderDialog.setTitle("Notice")
                .setMessage("Bạn cần vào cài đặt để cài đặt cho tính năng này")
                .setPositiveButton("Setting", (dialogInterface, i) -> {
                    startActivity(createIntentToAppSetting());
                    dialogInterface.dismiss();
                })
                .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());
        return builderDialog;
    }

    private Intent createIntentToAppSetting() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        return intent;
    }

    private void requestRuntimePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    private boolean isPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            return checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
