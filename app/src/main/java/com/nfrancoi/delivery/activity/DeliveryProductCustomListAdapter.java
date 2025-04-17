package com.nfrancoi.delivery.activity;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.nfrancoi.delivery.R;
import com.nfrancoi.delivery.room.entities.DeliveryProductsJoin;
import com.nfrancoi.delivery.tools.StringTools;

import java.util.List;


public class DeliveryProductCustomListAdapter extends RecyclerView.Adapter<DeliveryProductCustomListAdapter.DeliveryProductCustomViewHolder> {

    class DeliveryProductCustomViewHolder extends RecyclerView.ViewHolder {
        private final TextView productName;
        private final TextInputEditText priceHvat;

        private final TextInputEditText vat;
        private final TextInputEditText quantity;
        private final TextInputEditText discount;

        private final ImageButton editButton;
        private final ImageButton deleteButton;

        private DeliveryProductCustomViewHolder(View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.fragment_delivery_products_custom_rv_item_name);

            priceHvat = itemView.findViewById(R.id.fragment_delivery_products_custom_rv_item_price_hvat);
            priceHvat.setEnabled(false);
            priceHvat.setTextColor(Color.BLACK);

            vat = itemView.findViewById(R.id.fragment_delivery_products_custom_rv_item_vat);
            vat.setEnabled(false);
            vat.setTextColor(Color.BLACK);

            quantity = itemView.findViewById(R.id.fragment_delivery_products_custom_rv_item_quantity);
            quantity.setEnabled(false);
            quantity.setTextColor(Color.BLACK);

            discount = itemView.findViewById(R.id.fragment_delivery_products_custom_rv_item_discount);
            discount.setEnabled(false);
            discount.setTextColor(Color.BLACK);

            editButton = itemView.findViewById(R.id.fragment_delivery_products_custom_rv_item_button_edit);
            deleteButton = itemView.findViewById(R.id.fragment_delivery_products_custom_rv_item_button_delete);

        }
    }


    //
    // ClickItem management
    //
    public interface OnItemClickListener {
        void onEditClick(DeliveryProductsJoin item);
        void onDeleteClick(DeliveryProductsJoin item);
    }

    private DeliveryProductCustomListAdapter.OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(DeliveryProductCustomListAdapter.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }


    private final LayoutInflater mInflater;
    private List<DeliveryProductsJoin> deliveryProducts;

    DeliveryProductCustomListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public DeliveryProductCustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.fragment_delivery_products_custom_rv_item, parent, false);
        return new DeliveryProductCustomViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(DeliveryProductCustomViewHolder holder, int position) {
        if (deliveryProducts != null) {
            DeliveryProductsJoin current = deliveryProducts.get(position);

            holder.productName.setText(current.productName);
            holder.priceHvat.setText(StringTools.PriceFormat.format(current.priceUnitVatExcl));
            holder.vat.setText(current.vat.longValue() + " %");
            holder.quantity.setText(current.quantity+"");
            holder.discount.setText(current.discount.longValue() +" %");


            holder.editButton.setOnClickListener(v -> {
                if( onItemClickListener != null) {
                    onItemClickListener.onEditClick(current);
                }
            });

            holder.deleteButton.setOnClickListener(v -> {
                if( onItemClickListener != null) {
                    onItemClickListener.onDeleteClick(current);
                }
            });



        } else {
            // Covers the case of data not being ready yet.
            holder.productName.setText(". . .");
        }
    }


    // getItemCount() is called many times, and when it is first called,
    // mWords has not been updated (means initially, it's null, and we can't return null).
    @Override
    public int getItemCount() {
        if (deliveryProducts != null)
            return deliveryProducts.size();
        else return 0;
    }


    public void setDeliveryProducts(List<DeliveryProductsJoin> deliveryProducts) {
        this.deliveryProducts = deliveryProducts;
        notifyDataSetChanged();
    }
}
