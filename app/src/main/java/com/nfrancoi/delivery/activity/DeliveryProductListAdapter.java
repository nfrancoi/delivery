package com.nfrancoi.delivery.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.nfrancoi.delivery.R;
import com.nfrancoi.delivery.activity.widget.HorizontalNumberPicker;
import com.nfrancoi.delivery.room.entities.DeliveryProductsJoin;

import java.util.List;


public class DeliveryProductListAdapter extends RecyclerView.Adapter<DeliveryProductListAdapter.DeliveryProductViewHolder> {

    class DeliveryProductViewHolder extends RecyclerView.ViewHolder {
        private final TextView productName;
        private final TextView price;
        private final HorizontalNumberPicker quantity;

        private DeliveryProductViewHolder(View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.fragment_delivery_products_rv_item_name);
            price = itemView.findViewById(R.id.fragment_delivery_products_rv_item_price);
            quantity = itemView.findViewById(R.id.fragment_delivery_products_rv_item_quantity);

        }
    }

    //
    // NumberPicker management
    //
    public interface QuantityValueChangeListener {
        void quantityValueChange(DeliveryProductsJoin deliveryProductDetail);
    }

    private QuantityValueChangeListener quantityValueChangeListener;

    public void setQuantityValueChangeListener(QuantityValueChangeListener quantityValueChangeListener) {
        this.quantityValueChangeListener = quantityValueChangeListener;
    }


    private final LayoutInflater mInflater;
    private List<DeliveryProductsJoin> deliveryProductDetails;

    DeliveryProductListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public DeliveryProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.fragment_delivery_products_rv_item, parent, false);
        return new DeliveryProductViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(DeliveryProductViewHolder holder, int position) {
        if (deliveryProductDetails != null) {
            DeliveryProductsJoin current = deliveryProductDetails.get(position);

            holder.productName.setText(current.productName);
            holder.price.setText(current.priceUnitVatIncl.toString());
            holder.quantity.setMin(0);
            holder.quantity.setValue(Math.abs(current.quantity)); //abs to manage negative quantity with returns
            holder.quantity.setOnChangeValueListener(
                    (newVal) -> {

                        if (current.quantity != newVal) {
                            current.quantity = newVal;
                            if (quantityValueChangeListener != null) {
                                quantityValueChangeListener.quantityValueChange(current);

                            }
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
        if (deliveryProductDetails != null)
            return deliveryProductDetails.size();
        else return 0;
    }


    public void setDeliveryProductDetails(List<DeliveryProductsJoin> deliveryProductDetails) {
        this.deliveryProductDetails = deliveryProductDetails;
        notifyDataSetChanged();
    }
}
