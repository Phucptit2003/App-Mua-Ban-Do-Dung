package com.example.fooddeliveryapplication.Interfaces;

import com.example.fooddeliveryapplication.Model.CartInfo;

import java.util.ArrayList;

public interface IAdapterItemListener {
    void onCheckedItemCountChanged(int count, long price, ArrayList<CartInfo> selectedItems);
    void onAddClicked();
    void onSubtractClicked();
    void onDeleteProduct();
}