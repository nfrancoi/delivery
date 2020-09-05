package com.nfrancoi.delivery.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.nfrancoi.delivery.R;
import com.nfrancoi.delivery.room.entities.Delivery;
import com.nfrancoi.delivery.tools.CalendarTools;

import java.util.List;


public class DeliveryListAdapter extends RecyclerView.Adapter<DeliveryListAdapter.DeliveryViewHolder> {


    class DeliveryViewHolder extends RecyclerView.ViewHolder {
        private final TextView deliveryIdTextView;
        private final TextView dateBeginTextView;
        private final TextView dateEndTextView;
        private final CheckBox statusMailCheckBox;
        private final CheckBox statusSaveCheckBox;

        private final TextView pointOfDeliveryNameTextView;

        private DeliveryViewHolder(View itemView) {
            super(itemView);
            deliveryIdTextView = itemView.findViewById(R.id.fragment_delivery_recycleview_item_id);
            dateBeginTextView = itemView.findViewById(R.id.fragment_delivery_recycleview_item_date_begin);
            dateEndTextView = itemView.findViewById(R.id.fragment_delivery_recycleview_item_date_end);
            statusMailCheckBox = itemView.findViewById(R.id.fragment_delivery_recycleview_item_status_mail);
            statusSaveCheckBox = itemView.findViewById(R.id.fragment_delivery_recycleview_item_status_save);
            pointOfDeliveryNameTextView = itemView.findViewById(R.id.fragment_delivery_recycleview_item_pointOfDelivery_name);


        }
    }

    //
    // ClickItem management
    //
    public interface OnItemClickListener {
        void onItemClick(Delivery item);
    }

    private OnItemClickListener onItemClickListener;

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
            holder.dateBeginTextView.setText(CalendarTools.HHmm.format(current.startDate.getTime()));

            if (current.sentDate == null) {
                holder.dateEndTextView.setVisibility(View.INVISIBLE);
            } else {
                holder.dateEndTextView.setVisibility(View.VISIBLE);
                holder.dateEndTextView.setText(CalendarTools.HHmm.format(current.sentDate.getTime()));
            }


            holder.deliveryIdTextView.setText(current.noteId == null?"": current.noteId);


            holder.statusMailCheckBox.setChecked(current.isMailSent);
            holder.statusSaveCheckBox.setChecked(current.isNoteSaved);


            holder.pointOfDeliveryNameTextView.setText(current.pointOfDelivery == null ? "" : current.pointOfDelivery.name);
            holder.itemView.setOnClickListener(view -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(current);
                }
            });


        } else {
            // Covers the case of data not being ready yet.
            holder.dateBeginTextView.setText("No Word");
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
