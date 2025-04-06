package com.example.fooddeliveryapplication.Helpers;

import android.util.Log;

import com.example.fooddeliveryapplication.Model.Product;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseProductHelper {

    private DatabaseReference databaseReference;

    public FirebaseProductHelper() {
        // Tham chiếu đến node "Products" trong Firebase Realtime Database
        databaseReference = FirebaseDatabase.getInstance().getReference("Products");
    }

    // Hàm thêm sản phẩm vào Firebase
    public void addProduct(String productName, String image1, String image2, String image3, String image4,
                           int price, String productType, int remainAmount, int sold, String description,
                           double ratingStar, int ratingAmount, String publisherId, String state) {
        // Tạo ID duy nhất cho sản phẩm
        String productId = databaseReference.push().getKey();

        // Tạo đối tượng sản phẩm
        Product product = new Product(productId, productName, image1, image2, image3, image4,
                price, productType, remainAmount, sold, description,
                ratingStar, ratingAmount, publisherId, state);

        // Lưu sản phẩm vào Firebase
        databaseReference.child(productId).setValue(product)
                .addOnSuccessListener(aVoid -> Log.d("Firebase", "✔ Sản phẩm đã thêm thành công!"))
                .addOnFailureListener(e -> Log.e("Firebase", "❌ Lỗi khi thêm sản phẩm: " + e.getMessage()));
    }
}
