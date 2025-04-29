package com.example.app.Interfaces;

import com.example.app.Model;
import java.util.ArrayList;

public interface IAdapterItemListener {
    void onCheckedItemCountChanged(int count, long price, ArrayList<CartInfo> selectedItems);
    void onAddClicked();
    void onSubtractClicked();
    void onDeleteProduct();
}