package com.nfrancoi.delivery.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.nfrancoi.delivery.R;
import com.nfrancoi.delivery.room.dao.DeliveryProductsJoinDao;

import java.util.List;


public class NoteProductDetailsListAdapter extends RecyclerView.Adapter<NoteProductDetailsListAdapter.NoteDeliveryProductDetailViewHolder> {

    class NoteDeliveryProductDetailViewHolder extends RecyclerView.ViewHolder {

        private final TextView type;
        private final TextView productName;
        private final TextView price;
        private final TextView quantity;

        private NoteDeliveryProductDetailViewHolder(View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.pdf_note_product_detail_item_name);
            price = itemView.findViewById(R.id.pdf_note_product_detail_item_price);
            quantity = itemView.findViewById(R.id.pdf_note_product_detail_item_quantity);
            type = itemView.findViewById(R.id.pdf_note_product_detail_item_type);
        }
    }



    private final LayoutInflater mInflater;
    private List<DeliveryProductsJoinDao.NoteDeliveryProductDetail> noteDeliveryProductDetails;

    public NoteProductDetailsListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public NoteDeliveryProductDetailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.pdf_note_list_product_detail, parent, false);
        return new NoteDeliveryProductDetailViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(NoteDeliveryProductDetailViewHolder holder, int position) {
        if (noteDeliveryProductDetails != null) {
            DeliveryProductsJoinDao.NoteDeliveryProductDetail  current = noteDeliveryProductDetails.get(position);

            String typeName = "";
            switch(current.type) {
                case "T":
                    typeName = "Retour";
                    break;
                case "D":
                    typeName = "Depos";
                    break;
            }
            holder.type.setText(typeName);

            holder.productName.setText(current.productName);
            holder.price.setText(current.price.toString());
            holder.quantity.setText(""+current.quantity); //negative for return
        } else {
            // Covers the case of data not being ready yet.
            holder.productName.setText(". . .");
        }
    }


    // getItemCount() is called many times, and when it is first called,
    // mWords has not been updated (means initially, it's null, and we can't return null).
    @Override
    public int getItemCount() {
        if (noteDeliveryProductDetails != null)
            return noteDeliveryProductDetails.size();
        else return 0;
    }


    public void setNoteDeliveryProductDetails(List<DeliveryProductsJoinDao.NoteDeliveryProductDetail> noteDeliveryProductDetails) {
        this.noteDeliveryProductDetails = noteDeliveryProductDetails;
        notifyDataSetChanged();
    }



}
