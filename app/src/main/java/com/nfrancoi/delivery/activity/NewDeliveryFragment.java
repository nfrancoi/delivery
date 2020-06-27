package com.nfrancoi.delivery.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;

import com.nfrancoi.delivery.R;
import com.nfrancoi.delivery.room.dao.DeliveryProductsJoinDao;
import com.nfrancoi.delivery.room.entities.Company;
import com.nfrancoi.delivery.room.entities.Delivery;
import com.nfrancoi.delivery.room.entities.PointOfDelivery;
import com.nfrancoi.delivery.tools.BitmapTools;
import com.nfrancoi.delivery.tools.CalendarTools;
import com.nfrancoi.delivery.tools.FilterWithSpaceAdapter;
import com.nfrancoi.delivery.tools.NotePDFCreator;
import com.nfrancoi.delivery.viewmodel.DeliveryViewModel;
import com.nfrancoi.delivery.viewmodel.DeliveryViewModelFactory;
import com.nfrancoi.delivery.viewmodel.NoteViewModel;
import com.nfrancoi.delivery.viewmodel.NoteViewModelFactory;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.List;

public class NewDeliveryFragment extends Fragment {

    public static final String TAG = NewDeliveryFragment.class.toString();

    public static final String INTENT_DELIVERY_ID = "deliveryId";
    public static final int ACTIVITY_RESULT_DEPOSIT = 1;

    public static final String ACTIVITY_SELECT_PRODUCT_TITLE = "ACTIVITY_SELECT_PRODUCT_TITLE";


    private Long deliveryId;

    //ViewModels
    private DeliveryViewModel deliveryViewModel;

    //Delivery
    private TextView receiverNameTextView;
    private TextView deliveryCommentTextView;
    private TextView receiverCommentTextView;


    //Select PointOfDelivery
    private AutoCompleteTextView podTextView;
    private FilterWithSpaceAdapter<PointOfDelivery> podAdapter;


    //Select Deposit
    private Button depositButton;
    private TextView countDepositTextView;
    private TextView priceDepositTextView;

    //Select take
    private Button takeButton;
    private TextView countTakeTextView;
    private TextView priceTakeTextView;

    //total
    private TextView priceTotalTextView;

    //documents
    private ImageButton noteImageButton;

    //Signature
    private ImageView signatureImageView;


    private Button finishButton;

    public static NewDeliveryFragment newInstance(Long deliveryId) {
        NewDeliveryFragment fragment = new NewDeliveryFragment();
        Bundle bundle = new Bundle();
        bundle.putLong("deliveryId", deliveryId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            this.deliveryId = Long.valueOf(getArguments().getLong("deliveryId", 0));
        } else {
            new IllegalStateException("Fragment must be created with deliveryId in bundle");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_new_delivery, container, false);

        requireActivity().setTitle(R.string.fragment_select_products_title);

        //
        // Fetch delivery
        //
        receiverNameTextView = view.findViewById(R.id.fragment_new_delivery_text_receiver_name);
        deliveryCommentTextView = view.findViewById(R.id.fragment_new_delivery_text_delivery_comment);
        receiverCommentTextView = view.findViewById(R.id.fragment_new_delivery_text_receiver_comment);

        //
        //Select PointofDelvery
        //
        podTextView = view.findViewById(R.id.fragment_new_delivery_text_select_pod);


        //
        // Deposit products
        //
        depositButton = view.findViewById(R.id.fragment_new_delivery_button_deposit);
        countDepositTextView = view.findViewById(R.id.fragment_new_delivery_text_deposit);
        priceDepositTextView = view.findViewById(R.id.fragment_new_delivery_text_deposit_price);

        depositButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewDeliveryFragment.this.showSelectProductsFragment("D");
            }
        });

        //
        // Take products
        //
        takeButton = view.findViewById(R.id.fragment_new_delivery_button_take);
        countTakeTextView = view.findViewById(R.id.fragment_new_delivery_text_take);
        priceTakeTextView = view.findViewById(R.id.fragment_new_delivery_text_take_price);


        takeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewDeliveryFragment.this.showSelectProductsFragment("T");
            }
        });

        //
        // Total price
        //
        priceTotalTextView = view.findViewById(R.id.fragment_new_delivery_text_total_amount);

        //
        // Signature
        //
        signatureImageView = view.findViewById(R.id.fragment_new_delivery_signature);

        //
        // Documents
        //

        noteImageButton = view.findViewById(R.id.fragment_new_delivery_button_note);

        //
        //Finished finishButton
        //
        finishButton = view.findViewById(R.id.fragment_new_delivery_button_finished);
        finishButton.setEnabled(false);

        return view;
    }


    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        DeliveryViewModelFactory dvmFactory = new DeliveryViewModelFactory(getActivity().getApplication(), this.deliveryId);
        //scope fragment
        String key = this.deliveryId.toString();
        deliveryViewModel = ViewModelProviders.of(requireActivity(), dvmFactory).get(key, DeliveryViewModel.class);

        //
        // Delivery
        //
        deliveryViewModel.getSelectedDelivery().observe(getViewLifecycleOwner(), selectedDelivery -> {
            if (selectedDelivery.receiverName != null)
                receiverNameTextView.setText(selectedDelivery.receiverName);
            if (selectedDelivery.commentDelivery != null)
                deliveryCommentTextView.setText(selectedDelivery.commentDelivery);
            if (selectedDelivery.commentReceiver != null)
                receiverCommentTextView.setText(selectedDelivery.commentReceiver);
            if (selectedDelivery.pointOfDelivery != null) {
                podTextView.setText(selectedDelivery.pointOfDelivery.name);
                podTextView.dismissDropDown();
            }
            if (selectedDelivery.signatureBytes != null) {
                Bitmap signatureBitmap = BitmapTools.byteArrayToBitmap(selectedDelivery.signatureBytes);
                signatureImageView.setImageBitmap(signatureBitmap);
            }


            signatureImageView.setOnClickListener(imageView -> {
                FragmentManager fm = requireActivity().getSupportFragmentManager();
                SignatureDialogFragment signatureDialog = SignatureDialogFragment.newInstance(selectedDelivery.deliveryId);
                signatureDialog.show(fm, "fragment_signature");
            });

            receiverNameTextView.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    selectedDelivery.receiverName = receiverNameTextView.getText().toString();
                    deliveryViewModel.updateSelectedDelivery();
                }
            });

            deliveryCommentTextView.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    selectedDelivery.commentDelivery = deliveryCommentTextView.getText().toString();
                    deliveryViewModel.updateSelectedDelivery();
                }
            });
            receiverCommentTextView.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    selectedDelivery.commentReceiver = receiverCommentTextView.getText().toString();
                    deliveryViewModel.updateSelectedDelivery();
                }
            });

            finishButton.setEnabled(true);
        });


        //
        // Point of delivery selection
        //

        podAdapter = new FilterWithSpaceAdapter<PointOfDelivery>(requireActivity(), android.R.layout.simple_dropdown_item_1line, android.R.id.text1);
        podTextView.setAdapter(podAdapter);
        deliveryViewModel.getPointOfDeliveries().observe(getViewLifecycleOwner(), pods -> {
            // Update the cached copy of the words in the adapter.
            podAdapter.addAll(pods);
        });

        podTextView.setOnItemClickListener((parent, arg1, position, arg3) -> {
            Object item = parent.getItemAtPosition(position);
            if (item instanceof PointOfDelivery) {
                PointOfDelivery pod = (PointOfDelivery) item;
                deliveryViewModel.onSelectedPointOfDelivery(pod);


            }
        });


        //
        //Deposit
        //
        deliveryViewModel.getSelectedDepositDeliveryProductDetails().observe(getViewLifecycleOwner(), deliveryProductDetails -> {
            int countDeposit = deliveryProductDetails.stream().mapToInt(value -> value.quantity).sum();
            countDepositTextView.setText("" + countDeposit);
            BigDecimal sumPriceDeposit = deliveryProductDetails.stream().map(value -> value.price.multiply(BigDecimal.valueOf(value.quantity))).reduce(BigDecimal.ZERO, BigDecimal::add);

            priceDepositTextView.setText(NumberFormat.getCurrencyInstance().format(sumPriceDeposit));

        });

        //
        //Take
        //
        deliveryViewModel.getSelectedTakeDeliveryProductDetails().observe(getViewLifecycleOwner(), deliveryProductDetails -> {
            int sumQuantity = deliveryProductDetails.stream().mapToInt(value -> value.quantity).sum();
            countTakeTextView.setText("" + sumQuantity);
            BigDecimal sumPriceTake = deliveryProductDetails.stream().map(value -> value.price.multiply(BigDecimal.valueOf(value.quantity))).reduce(BigDecimal.ZERO, BigDecimal::add);

            priceTakeTextView.setText(NumberFormat.getCurrencyInstance().format(sumPriceTake));
        });

        //
        // Total
        //
        deliveryViewModel.getSelectedDeliveryTotalAmount().observe(getViewLifecycleOwner(), totalBigDecimal -> {
            if (totalBigDecimal != null)
                this.priceTotalTextView.setText(NumberFormat.getCurrencyInstance().format(totalBigDecimal));
        });

        //
        // Documents
        //
        noteImageButton.setOnClickListener(view1 -> {
            NewDeliveryFragment.this.generateNotePDF();
        });

        finishButton.setOnClickListener(view1 -> {
            NewDeliveryFragment.this.generateNotePDF();
        });


    }


    private void showSelectProductsFragment(String type) {

        SelectProductsFragment fragment = SelectProductsFragment.newInstance(this.deliveryId, type);
        fragment.setRetainInstance(true);
        getFragmentManager().beginTransaction()
                .replace(R.id.activity_main_layout, fragment, "SelectProductFragment")
                .addToBackStack(null)
                .commit();
    }


    private void generateNotePDF() {
        Delivery selectedDelivery = deliveryViewModel.getSelectedDelivery().getValue();
        //Calculate noteId
        selectedDelivery.noteId = CalendarTools.YYYYMM.format(Calendar.getInstance().getTime()) + "_" + selectedDelivery.deliveryId;
        deliveryViewModel.updateDelivery(selectedDelivery);

        NoteViewModel noteViewModel = ViewModelProviders.of(this, new NoteViewModelFactory(getActivity().getApplication(), selectedDelivery.deliveryId)).get(NoteViewModel.class);
        noteViewModel.getNoteLiveData().observe(getViewLifecycleOwner(), pair -> {
            Company company = pair.first;
            List<DeliveryProductsJoinDao.NoteDeliveryProductDetail> deliveryProductNoteDetails = pair.second;
            NotePDFCreator notePdfCreator = new NotePDFCreator(getActivity(), company, selectedDelivery, deliveryProductNoteDetails);
            notePdfCreator.createClientNotePdf();

        });


    }
}