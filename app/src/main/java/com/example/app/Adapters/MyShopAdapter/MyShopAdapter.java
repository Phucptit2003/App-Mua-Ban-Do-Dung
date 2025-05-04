package com.example.app.Adapters.MyShopAdapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.app.Activities.MyShop.AddFoodActivity;
import com.example.app.Activities.ProductInformation.ProductInfoActivity;
import com.example.app.CustomMessageBox.CustomAlertDialog;
import com.example.app.CustomMessageBox.FailToast;
import com.example.app.CustomMessageBox.SuccessfulToast;
import com.example.app.Model.Product;
import com.example.app.R;
import com.example.app.databinding.LayoutFoodItemBinding;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class MyShopAdapter extends RecyclerView.Adapter<MyShopAdapter.ViewHolder> {
    private ArrayList<Product> ds;
    private Context context;
    private String userId;

    public MyShopAdapter(ArrayList<Product> ds, Context context, String id) {
        this.ds = ds;
        this.context = context;
        this.userId = id;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutFoodItemBinding binding = LayoutFoodItemBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = ds.get(position);
        holder.binding.txtNameProdiuct.setText(product.getProductName());
        holder.binding.txtPrice.setText(convertToMoney(product.getProductPrice()) + "đ");
        Glide.with(context)
                .load(product.getProductImage1())
                .placeholder(R.drawable.baseline_image_search_24)
                .into(holder.binding.imgFood);

        // Xử lý sự kiện xóa sản phẩm
        holder.binding.imgDelete.setOnClickListener(view -> {
            new CustomAlertDialog(context, "Delete this product?");
            CustomAlertDialog.binding.btnYes.setOnClickListener(view1 -> {
                CustomAlertDialog.alertDialog.dismiss();

                FirebaseDatabase.getInstance().getReference("Products")
                        .child(product.getProductId())
                        .child("state")
                        .setValue("deleted")
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                ds.remove(product);
                                notifyItemRemoved(position);
                                new SuccessfulToast(context, "Delete product successfully!").showToast();
                            } else {
                                new FailToast(context, "Delete product failed!").showToast();
                                Log.e("My Shop", "Error remove");
                            }
                        });
            });
            CustomAlertDialog.binding.btnNo.setOnClickListener(view12 -> CustomAlertDialog.alertDialog.dismiss());
            CustomAlertDialog.showAlertDialog();
        });

        // Xử lý sự kiện chỉnh sửa sản phẩm
        holder.binding.imgEdit.setOnClickListener(view -> {
            Intent intent = new Intent(context, AddProductActivity.class);
            intent.putExtra("Product updating", product);
            context.startActivity(intent);
        });

        // Xử lý sự kiện click vào sản phẩm
        holder.binding.productContainer.setOnClickListener(view -> {
            Intent intent = new Intent(context, ProductInfoActivity.class);
            intent.putExtra("productId", product.getProductId());
            intent.putExtra("productName", product.getProductName());
            intent.putExtra("productPrice", product.getProductPrice());
            intent.putExtra("productImage1", product.getProductImage1());
            intent.putExtra("productImage2", product.getProductImage2());
            intent.putExtra("productImage3", product.getProductImage3());
            intent.putExtra("productImage4", product.getProductImage4());
            intent.putExtra("ratingStar", product.getRatingStar());
            intent.putExtra("productDescription", product.getDescription());
            intent.putExtra("publisherId", product.getPublisherId());
            intent.putExtra("sold", product.getSold());
            intent.putExtra("productType", product.getProductType());
            intent.putExtra("remainAmount", product.getRemainAmount());
            intent.putExtra("ratingAmount", product.getRatingAmount());
            intent.putExtra("state", product.getState());
            intent.putExtra("userId", userId);
            intent.putExtra("userName", product);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return ds == null ? 0 : ds.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final LayoutFoodItemBinding binding;

        public ViewHolder(@NonNull LayoutFoodItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private String convertToMoney(long price) {
        String temp = String.valueOf(price);
        String output = "";
        int count = 3;
        for (int i = temp.length() - 1; i >= 0; i--) {
            count--;
            if (count == 0) {
                count = 3;
                output = "," + temp.charAt(i) + output;
            } else {
                output = temp.charAt(i) + output;
            }
        }

        if (output.charAt(0) == ',')
            return output.substring(1);

        return output;
    }
}