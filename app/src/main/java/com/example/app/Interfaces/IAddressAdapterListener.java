package com.example.app.Interfaces;

import com.example.app.Model.Address;

public interface IAddressAdapterListener {
    void onCheckedChanged(Address selectedAddress);
    void onDeleteAddress();
}
