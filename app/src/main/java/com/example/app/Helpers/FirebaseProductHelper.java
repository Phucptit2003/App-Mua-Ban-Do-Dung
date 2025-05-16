package com.example.app.Helpers;

import android.util.Log;

import com.example.app.Model.Product;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseProductHelper {

    private DatabaseReference databaseReference;

    public FirebaseProductHelper() {
        databaseReference = FirebaseDatabase.getInstance().getReference("Products");
    }

    public void addProduct(String productName, String image1, String image2, String image3, String image4,
                           int price, String productType, int remainAmount, int sold, String description,
                           double ratingStar, int ratingAmount, String publisherId, String state) {

        String productId = databaseReference.push().getKey();

        Product product = new Product(productId, productName, image1, image2, image3, image4,
                price, productType, remainAmount, sold, description,
                ratingStar, ratingAmount, publisherId, state);

        databaseReference.child(productId).setValue(product)
                .addOnSuccessListener(aVoid -> Log.d("Firebase", "✔ Sản phẩm đã thêm thành công!"))
                .addOnFailureListener(e -> Log.e("Firebase", "❌ Lỗi khi thêm sản phẩm: " + e.getMessage()));
    }
}
