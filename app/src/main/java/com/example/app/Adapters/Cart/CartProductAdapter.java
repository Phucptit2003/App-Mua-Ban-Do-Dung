package com.example.app.Adapters.Cart;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.app.Activities.ProductInformation.ProductInfoActivity;
import com.example.app.CustomMessageBox.CustomAlertDialog;
import com.example.app.CustomMessageBox.FailToast;
import com.example.app.CustomMessageBox.SuccessfulToast;
import com.example.app.Helpers.FirebaseNotificationHelper;
import com.example.app.Helpers.FirebaseProductInfoHelper;
import com.example.app.Helpers.FirebaseUserInfoHelper;
import com.example.app.Interfaces.IAdapterItemListener;
import com.example.app.Model.Cart;
import com.example.app.Model.Product;
import com.example.app.Model.User;
import com.example.app.R;
import com.example.app.databinding.ItemCartProductBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartProductAdapter extends RecyclerView.Adapter<CartProductAdapter.ViewHolder> {
    private Context mContext;
    private List<CartInfo> mCartInfos;
    private String cartId;
    private int checkedItemCount = 0;
    private long checkedItemPrice = 0;
    private IAdapterItemListener adapterItemListener;
    private boolean isCheckAll;
    private String userId;
    private String userName;
    private ArrayList<CartInfo> selectedItems = new ArrayList<>();
    private HashMap<String, Boolean> itemStates = new HashMap<>();

    public CartProductAdapter(Context mContext, List<CartInfo> mCartInfos, String cartId, boolean isCheckAll, String id) {
        this.mContext = mContext;
        this.mCartInfos = mCartInfos;
        this.cartId = cartId;
        this.isCheckAll = isCheckAll;
        this.userId = id;

        new FirebaseUserInfoHelper(mContext).readUserInfo(userId, new FirebaseUserInfoHelper.DataStatus() {
            @Override
            public void DataIsLoaded(User user) {
                userName = user.getUserName();
            }

            @Override
            public void DataIsInserted() {
            }

            @Override
            public void DataIsUpdated() {
            }

            @Override
            public void DataIsDeleted() {
            }
        });
    }

    @NonNull
    @Override
    public CartProductAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemCartProductBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CartProductAdapter.ViewHolder holder, int position) {
        CartInfo cartInfo = mCartInfos.get(position);
        String itemId = cartInfo.getCartInfoId();

        // Khôi phục trạng thái nếu có
        if (itemStates.containsKey(itemId)) {
            boolean isChecked = itemStates.get(itemId);
            holder.binding.checkBox.setChecked(isChecked);
        }

        // Lưu trạng thái khi thay đổi
        holder.binding.checkBox.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            itemStates.put(itemId, isChecked);
        });


        holder.binding.checkBox.setChecked(isCheckAll);

        FirebaseDatabase.getInstance().getReference().child("Products").child(cartInfo.getProductId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Product product = snapshot.getValue(Product.class);
                holder.binding.productName.setText(product.getProductName());
                holder.binding.productPrice.setText(convertToMoney(product.getProductPrice()) + "đ");
                Glide.with(mContext).load(product.getProductImage1()).placeholder(R.mipmap.ic_launcher).into(holder.binding.productImage);
                holder.binding.productAmount.setText(String.valueOf(cartInfo.getAmount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        isLiked(holder.binding.like, cartInfo.getProductId());

        holder.binding.add.setOnClickListener(view -> {
            FirebaseDatabase.getInstance().getReference().child("Products").child(cartInfo.getProductId()).child("remainAmount").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int amount = Integer.parseInt(holder.binding.productAmount.getText().toString());
                    int remainAmount = snapshot.getValue(int.class);
                    if (amount >= remainAmount) {
                        new FailToast(mContext, "Can't add anymore!").showToast();
                    } else {
                        amount++;
                        holder.binding.productAmount.setText(String.valueOf(amount));
                        holder.binding.checkBox.setChecked(false);
                        isCheckAll = false;

                        if (adapterItemListener != null) {
                            adapterItemListener.onCheckedItemCountChanged(0, 0, new ArrayList<>());
                            adapterItemListener.onAddClicked();
                        }

                        FirebaseDatabase.getInstance().getReference().child("CartInfos").child(cartId).child(cartInfo.getCartInfoId()).child("amount").setValue(amount);

                        FirebaseDatabase.getInstance().getReference().child("Carts").child(cartId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Cart cart = snapshot.getValue(Cart.class);
                                FirebaseDatabase.getInstance().getReference().child("Products").child(cartInfo.getProductId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot1) {
                                        Product product = snapshot1.getValue(Product.class);
                                        int totalAmount = cart.getTotalAmount() + 1;
                                        long totalPrice = cart.getTotalPrice() + product.getProductPrice();

                                        HashMap<String, Object> map = new HashMap<>();
                                        map.put("totalAmount", totalAmount);
                                        map.put("totalPrice", totalPrice);
                                        FirebaseDatabase.getInstance().getReference().child("Carts").child(cartId).updateChildren(map);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });

                        FirebaseDatabase.getInstance().getReference().child("Products").child(cartInfo.getProductId()).child("remainAmount").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                int remainAmount = snapshot.getValue(int.class) - 1;
                                FirebaseDatabase.getInstance().getReference().child("Products").child(cartInfo.getProductId()).child("remainAmount").setValue(remainAmount);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        });

        holder.binding.subtract.setOnClickListener(view -> {
            if (!holder.binding.productAmount.getText().toString().equals("1")) {
                int amount = Integer.parseInt(holder.binding.productAmount.getText().toString());
                amount--;
                holder.binding.productAmount.setText(String.valueOf(amount));
                isCheckAll = false;

                if (adapterItemListener != null) {
                    adapterItemListener.onCheckedItemCountChanged(0, 0, new ArrayList<>());
                    adapterItemListener.onSubtractClicked();
                }

                FirebaseDatabase.getInstance().getReference().child("CartInfos").child(cartId).child(cartInfo.getCartInfoId()).child("amount").setValue(amount);

                FirebaseDatabase.getInstance().getReference().child("Carts").child(cartId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Cart cart = snapshot.getValue(Cart.class);
                        FirebaseDatabase.getInstance().getReference().child("Products").child(cartInfo.getProductId()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot1) {
                                Product product = snapshot1.getValue(Product.class);
                                int totalAmount = cart.getTotalAmount() - 1;
                                long totalPrice = cart.getTotalPrice() - product.getProductPrice();

                                HashMap<String, Object> map = new HashMap<>();
                                map.put("totalAmount", totalAmount);
                                map.put("totalPrice", totalPrice);
                                FirebaseDatabase.getInstance().getReference().child("Carts").child(cartId).updateChildren(map);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

                FirebaseDatabase.getInstance().getReference().child("Products").child(cartInfo.getProductId()).child("remainAmount").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int remainAmount = snapshot.getValue(int.class) + 1;
                        FirebaseDatabase.getInstance().getReference().child("Products").child(cartInfo.getProductId()).child("remainAmount").setValue(remainAmount);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            } else {
                new FailToast(mContext, "Can't reduce anymore!").showToast();
            }
        });

        holder.binding.like.setOnClickListener(view -> {
            if (holder.binding.like.getTag().equals("like")) {
                FirebaseDatabase.getInstance().getReference().child("Favorites").child(userId).child(cartInfo.getProductId()).setValue(true);
                pushNotificationFavourite(cartInfo);
                new SuccessfulToast(mContext, "Added to your favourite list").showToast();
            } else if (holder.binding.like.getTag().equals("liked")) {
                FirebaseDatabase.getInstance().getReference().child("Favorites").child(userId).child(cartInfo.getProductId()).removeValue();
                new SuccessfulToast(mContext, "Removed from your favourite list").showToast();
            }
        });

        holder.binding.delete.setOnClickListener(view -> {
            new CustomAlertDialog(mContext, "Delete this product?");
            CustomAlertDialog.binding.btnYes.setOnClickListener(view1 -> {
                CustomAlertDialog.alertDialog.dismiss();

                FirebaseDatabase.getInstance().getReference().child("CartInfos").child(cartId).child(cartInfo.getCartInfoId()).removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        new SuccessfulToast(mContext, "Delete product successfully!").showToast();
                        if (adapterItemListener != null) {
                            adapterItemListener.onDeleteProduct();
                        }
                    }
                });

                FirebaseDatabase.getInstance().getReference().child("Carts").child(cartId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Cart cart = snapshot.getValue(Cart.class);
                        FirebaseDatabase.getInstance().getReference().child("Products").child(cartInfo.getProductId()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Product product = snapshot.getValue(Product.class);
                                int totalAmount = cart.getTotalAmount() - cartInfo.getAmount();
                                long totalPrice = cart.getTotalPrice() - (long) (product.getProductPrice() * cartInfo.getAmount());

                                HashMap<String, Object> map = new HashMap<>();
                                map.put("totalAmount", totalAmount);
                                map.put("totalPrice", totalPrice);
                                FirebaseDatabase.getInstance().getReference().child("Carts").child(cartId).updateChildren(map);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

                FirebaseDatabase.getInstance().getReference().child("Products").child(cartInfo.getProductId()).child("remainAmount").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int remainAmount = snapshot.getValue(int.class) + cartInfo.getAmount();
                        FirebaseDatabase.getInstance().getReference().child("Products").child(cartInfo.getProductId()).child("remainAmount").setValue(remainAmount);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            });
            CustomAlertDialog.binding.btnNo.setOnClickListener(view12 -> CustomAlertDialog.alertDialog.dismiss());
            CustomAlertDialog.showAlertDialog();
        });

        holder.binding.checkBox.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            FirebaseDatabase.getInstance().getReference().child("Products").child(cartInfo.getProductId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Product product = snapshot.getValue(Product.class);
                    if (isChecked) {
                        checkedItemCount += cartInfo.getAmount();
                        checkedItemPrice += cartInfo.getAmount() * product.getProductPrice();
                        selectedItems.add(cartInfo);
                    } else {
                        checkedItemCount -= cartInfo.getAmount();
                        checkedItemPrice -= cartInfo.getAmount() * product.getProductPrice();
                        selectedItems.removeIf(c -> (c.getCartInfoId().equals(cartInfo.getCartInfoId())));
                    }

                    if (adapterItemListener != null) {
                        adapterItemListener.onCheckedItemCountChanged(checkedItemCount, checkedItemPrice, selectedItems);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        });

        holder.binding.itemContainer.setOnClickListener(view -> {
            FirebaseDatabase.getInstance().getReference().child("Products").child(cartInfo.getProductId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Product product = snapshot.getValue(Product.class);
                    if (product != null) {
                        Intent intent = new Intent(mContext, ProductInfoActivity.class);
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
                        intent.putExtra("userName", userName);
                        mContext.startActivity(intent);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        });
    }

    private void isLiked(ImageButton imageButton, String productId) {
        FirebaseDatabase.getInstance().getReference().child("Favorites").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(productId).exists()) {
                    imageButton.setImageResource(R.drawable.ic_liked);
                    imageButton.setTag("liked");
                } else {
                    imageButton.setImageResource(R.drawable.ic_like);
                    imageButton.setTag("like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
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

    @Override
    public int getItemCount() {
        return mCartInfos == null ? 0 : mCartInfos.size();
    }

    public void setAdapterItemListener(IAdapterItemListener adapterItemListener) {
        this.adapterItemListener = adapterItemListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemCartProductBinding binding;

        public ViewHolder(ItemCartProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
    public void saveStates(Bundle outState) {
        for (Map.Entry<String, Boolean> entry : itemStates.entrySet()) {
            outState.putBoolean(entry.getKey(), entry.getValue());
        }
    }

    // Phương thức khôi phục trạng thái
    public void restoreStates(Bundle savedInstanceState) {
        for (String key : savedInstanceState.keySet()) {
            itemStates.put(key, savedInstanceState.getBoolean(key));
        }
    }

    public void pushNotificationFavourite(CartInfo cartInfo) {
        new FirebaseProductInfoHelper(cartInfo.getProductId()).readInformationById(new FirebaseProductInfoHelper.DataStatusInformationOfProduct() {
            @Override
            public void DataIsLoaded(Product product) {
                String title = "Favourite product";
                String content = userName + " liked your product: " + product.getProductName() + ". Go to Product Information to check it.";
                Notification notification = FirebaseNotificationHelper.createNotification(title, content, product.getProductImage1(), product.getProductId(), "None", "None", null);
                new FirebaseNotificationHelper(mContext).addNotification(product.getPublisherId(), notification, new FirebaseNotificationHelper.DataStatus() {
                    @Override
                    public void DataIsLoaded(List<Notification> notificationList, List<Notification> notificationListToNotify) {
                    }

                    @Override
                    public void DataIsInserted() {
                    }

                    @Override
                    public void DataIsUpdated() {
                    }

                    @Override
                    public void DataIsDeleted() {
                    }
                });
            }

            @Override
            public void DataIsInserted() {
            }

            @Override
            public void DataIsUpdated() {
            }

            @Override
            public void DataIsDeleted() {
            }
        });
    }
}