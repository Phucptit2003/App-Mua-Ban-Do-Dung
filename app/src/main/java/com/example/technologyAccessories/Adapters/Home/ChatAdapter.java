package com.example.technologyAccessories.Adapters.Home;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.technologyAccessories.R;
import com.example.technologyAccessories.databinding.ItemChatBinding;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    private Context context;
    private ArrayList<ItemChatRoom> bunchOfItemChatRooms;
    private ArrayList<ItemChatRoom> currentBunchOfItemChatRooms;

    public ChatAdapter(Context context, ArrayList<ItemChatRoom> itemChatRooms) {
        this.context = context;
        this.bunchOfItemChatRooms = itemChatRooms;
        this.currentBunchOfItemChatRooms = bunchOfItemChatRooms;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemChatBinding.inflate(LayoutInflater.from(context), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemChatRoom itemChatRoom = currentBunchOfItemChatRooms.get(position);

        // Set user name
        holder.binding.txtNameUser.setText(itemChatRoom.getReceiver().getUserName());

        // Set last message and its color
        holder.binding.txtLastMessage.setText(itemChatRoom.getLastMessage().getContent());
        if (itemChatRoom.getLastMessage().getSenderId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            holder.binding.imgNewMessage.setVisibility(View.INVISIBLE);
            holder.binding.txtLastMessage.setTextColor(context.getColor(R.color.gray));
        } else {
            if (itemChatRoom.getLastMessage().isSeen()) {
                holder.binding.imgNewMessage.setVisibility(View.INVISIBLE);
                holder.binding.txtLastMessage.setTextColor(context.getColor(R.color.gray));
            } else {
                holder.binding.imgNewMessage.setVisibility(View.VISIBLE);
            }
        }

        // Load user avatar
        Glide.with(context)
                .load(itemChatRoom.getReceiver().getAvatarURL())
                .placeholder(R.drawable.default_avatar)
                .error(R.drawable.image_default)
                .into(holder.binding.lnItemChat.imgUser);

        // Handle item click
        holder.binding.layout.setOnClickListener(view -> {
            Intent intent = new Intent(context, ChatDetailActivity.class);
            intent.setAction("chatActivity");
            intent.putExtra("publisher", itemChatRoom.getReceiver());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return currentBunchOfItemChatRooms != null ? currentBunchOfItemChatRooms.size() : 0;
    }

    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String key = charSequence.toString().toLowerCase();
                ArrayList<ItemChatRoom> filteredList = new ArrayList<>();

                if (key.trim().isEmpty()) {
                    filteredList = bunchOfItemChatRooms;
                } else {
                    for (ItemChatRoom item : bunchOfItemChatRooms) {
                        if (item.getReceiver().getUserName().toLowerCase().contains(key)) {
                            filteredList.add(item);
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.values = filteredList;
                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                currentBunchOfItemChatRooms = (ArrayList<ItemChatRoom>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemChatBinding binding;

        public ViewHolder(@NonNull ItemChatBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}