package com.nfrancoi.delivery.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.nfrancoi.delivery.R;
import com.nfrancoi.delivery.room.entities.DeliveryProductsJoin;

import java.util.List;


public class NoteProductDetailsListAdapter extends RecyclerView.Adapter<NoteProductDetailsListAdapter.NoteDeliveryProductDetailViewHolder> {

    class NoteDeliveryProductDetailViewHolder extends RecyclerView.ViewHolder {

        private final TextView type;
        private final TextView productName;
        private final TextView quantity;
        private final TextView priceHtUnit;
        private final TextView discount;
        private final TextView priceHtTot;
        private final TextView vat;


        private NoteDeliveryProductDetailViewHolder(View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.pdf_note_product_detail_item_name);
            priceHtUnit = itemView.findViewById(R.id.pdf_note_product_detail_item_priceht_unit);
            discount = itemView.findViewById(R.id.pdf_note_product_detail_item_discount);
            priceHtTot = itemView.findViewById(R.id.pdf_note_product_detail_item_priceht_total);
            vat = itemView.findViewById(R.id.pdf_note_product_detail_item_vat);
            quantity = itemView.findViewById(R.id.pdf_note_product_detail_item_quantity);
            type = itemView.findViewById(R.id.pdf_note_product_detail_item_type);
        }
    }



    private final LayoutInflater mInflater;
    private List<DeliveryProductsJoin> noteDeliveryProductDetails;

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
            DeliveryProductsJoin  current = noteDeliveryProductDetails.get(position);

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
            holder.priceHtUnit.setText(""+current.priceUnitVatIncl);
            holder.discount.setText(""+current.discount);

            holder.priceHtTot.setText(""+current.priceTotVatDiscounted);
            holder.quantity.setText(""+current.quantity); //negative for return
            holder.vat.setText(""+current.vat);

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


    public void setNoteDeliveryProductDetails(List<DeliveryProductsJoin> noteDeliveryProductDetails) {
        this.noteDeliveryProductDetails = noteDeliveryProductDetails;
        notifyDataSetChanged();
    }



}
