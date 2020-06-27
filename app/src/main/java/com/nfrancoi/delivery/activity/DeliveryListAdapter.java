package com.nfrancoi.delivery.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.nfrancoi.delivery.R;
import com.nfrancoi.delivery.room.entities.Delivery;
import com.nfrancoi.delivery.tools.CalendarTools;

import java.util.List;


public class DeliveryListAdapter extends RecyclerView.Adapter<DeliveryListAdapter.DeliveryViewHolder> {



    class DeliveryViewHolder extends RecyclerView.ViewHolder {
        private final TextView dateTextView;
        private final TextView statusTextView;
        private final TextView pointOfDeliveryNameTextView;

        private DeliveryViewHolder(View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.fragment_delivery_recycleview_item_date);
            statusTextView = itemView.findViewById(R.id.note_product_detail_type);
            pointOfDeliveryNameTextView = itemView.findViewById(R.id.fragment_delivery_recycleview_item_pointOfDelivery_name);
        }
    }

    //
    // ClickItem management
    //
    public interface OnItemClickListener {
        void onItemClick(Delivery item);
    }
    private  OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    private final LayoutInflater mInflater;
    private Context context;

    private List<Delivery> deliveries; // Cached copy of words

    DeliveryListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public DeliveryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.fragment_delivery_list_item, parent, false);
        return new DeliveryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(DeliveryViewHolder holder, int position) {
        if (deliveries != null) {
            final Delivery current = deliveries.get(position);
            holder.dateTextView.setText(current.deliveryId + " "+ CalendarTools.YYYYMMDD.format(current.date.getTime()));
            holder.statusTextView.setText(current.status);
            holder.pointOfDeliveryNameTextView.setText(current.pointOfDelivery == null? "": current.pointOfDelivery.name);
            holder.itemView.setOnClickListener(view -> {
                if(onItemClickListener != null){
                    onItemClickListener.onItemClick(current);
                }
            });


        } else {
            // Covers the case of data not being ready yet.
            holder.dateTextView.setText("No Word");
        }
    }

    void setDeliveries(List<Delivery> deliveries) {
        this.deliveries = deliveries;
        notifyDataSetChanged();
    }

    // getItemCount() is called many times, and when it is first called,
    // mWords has not been updated (means initially, it's null, and we can't return null).
    @Override
    public int getItemCount() {
        if (deliveries != null)
            return deliveries.size();
        else return 0;
    }
}
