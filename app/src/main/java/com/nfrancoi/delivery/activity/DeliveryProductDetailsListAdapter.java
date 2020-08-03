package com.nfrancoi.delivery.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.nfrancoi.delivery.R;
import com.nfrancoi.delivery.activity.widget.HorizontalNumberPicker;
import com.nfrancoi.delivery.room.dao.DeliveryProductsJoinDao;

import java.util.List;


public class DeliveryProductDetailsListAdapter extends RecyclerView.Adapter<DeliveryProductDetailsListAdapter.DeliveryProductDetailsViewHolder> {

    class DeliveryProductDetailsViewHolder extends RecyclerView.ViewHolder {
        private final TextView productName;
        private final TextView price;
        private final HorizontalNumberPicker quantity;

        private DeliveryProductDetailsViewHolder(View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.activity_delivery_product_name);
            price = itemView.findViewById(R.id.activity_delivery_product_price);
            quantity = itemView.findViewById(R.id.activity_delivery_product_quantity);

        }
    }

    //
    // NumberPicker management
    //
    public interface QuantityValueChangeListener {
        void quantityValueChange(DeliveryProductsJoinDao.DeliveryProductDetail deliveryProductDetail);
    }

    private QuantityValueChangeListener quantityValueChangeListener;

    public void setQuantityValueChangeListener(QuantityValueChangeListener quantityValueChangeListener) {
        this.quantityValueChangeListener = quantityValueChangeListener;
    }


    private final LayoutInflater mInflater;
    private List<DeliveryProductsJoinDao.DeliveryProductDetail> deliveryProductDetails;

    DeliveryProductDetailsListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public DeliveryProductDetailsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.fragment_select_products_recyclerview_item, parent, false);
        return new DeliveryProductDetailsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(DeliveryProductDetailsViewHolder holder, int position) {
        if (deliveryProductDetails != null) {
            DeliveryProductsJoinDao.DeliveryProductDetail current = deliveryProductDetails.get(position);

            holder.productName.setText(current.productName);
            holder.price.setText(current.priceHtUnit.toString());
            holder.quantity.setMin(0);
            holder.quantity.setValue(Math.abs(current.quantity)); //abs to manage negative quantity with returns
            holder.quantity.setOnChangeValueListener(
                    (newVal) -> {
                        current.quantity = newVal;
                        if (quantityValueChangeListener != null) {
                            quantityValueChangeListener.quantityValueChange(current);
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


    public void setDeliveryProductDetails(List<DeliveryProductsJoinDao.DeliveryProductDetail> deliveryProductDetails) {
        this.deliveryProductDetails = deliveryProductDetails;
        notifyDataSetChanged();
    }
}
