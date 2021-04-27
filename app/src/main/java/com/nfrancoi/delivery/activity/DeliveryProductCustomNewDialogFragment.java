package com.nfrancoi.delivery.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.nfrancoi.delivery.R;
import com.nfrancoi.delivery.room.entities.DeliveryProductsJoin;
import com.nfrancoi.delivery.tools.StringTools;
import com.nfrancoi.delivery.viewmodel.DeliveryViewModel;
import com.nfrancoi.delivery.viewmodel.DeliveryViewModelFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DeliveryProductCustomNewDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DeliveryProductCustomNewDialogFragment extends DialogFragment {


    private TextInputEditText nameEditText;
    private TextInputLayout nameInputLayout;

    private TextInputEditText priceEditText;
    private TextInputLayout priceInputLayout;

    private TextInputEditText vatEditText;
    private TextInputLayout vatInputLayout;

    private TextInputEditText quantityEditText;
    private TextInputLayout quantityInputLayout;

    private TextInputEditText discountEditText;
    private TextInputLayout discountInputLayout;


    private Button saveButton;

    public DeliveryProductCustomNewDialogFragment() {
        // Required empty public constructor
    }


    public static DeliveryProductCustomNewDialogFragment newInstance(Long deliveryId) {
        DeliveryProductCustomNewDialogFragment fragment = new DeliveryProductCustomNewDialogFragment();
        Bundle args = new Bundle();
        args.putLong("deliveryId", deliveryId);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setStyle(STYLE_NORMAL, R.style.DialogWithTitle);

    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().setTitle(R.string.fragment_delivery_products_custom_new_title);


    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_delivery_products_custom_new, container, false);
        nameEditText = view.findViewById(R.id.fragment_delivery_products_custom_new_name);
        nameInputLayout = view.findViewById(R.id.fragment_delivery_products_custom_new_layout_name);

        priceEditText = view.findViewById(R.id.fragment_delivery_products_custom_new_price);
        priceInputLayout = view.findViewById(R.id.fragment_delivery_products_custom_new_layout_price);

        vatEditText = view.findViewById(R.id.fragment_delivery_products_custom_new_vat);
        vatInputLayout = view.findViewById(R.id.fragment_delivery_products_custom_new_layout_vat);

        quantityEditText = view.findViewById(R.id.fragment_delivery_products_custom_new_quantity);
        quantityInputLayout = view.findViewById(R.id.fragment_delivery_products_custom_new_layout_quantity);

        discountEditText = view.findViewById(R.id.fragment_delivery_products_custom_new_discount);
        discountInputLayout = view.findViewById(R.id.fragment_delivery_products_custom_new_layout_discount);

        saveButton = view.findViewById(R.id.fragment_delivery_products_custom_new_save_button);
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        Long deliveryId = args.getLong("deliveryId");

        if (deliveryId == null) {
            throw new IllegalStateException("DeliveryProductsCustomSelectFragment must be called with deliveryId mandatory params");
        }


        DeliveryViewModelFactory dvmFactory = new DeliveryViewModelFactory(getActivity().getApplication(), deliveryId);
        //scope fragment
        String key = deliveryId.toString();
        DeliveryViewModel deliveryViewModel = ViewModelProviders.of(requireActivity(), dvmFactory).get(key, DeliveryViewModel.class);

        DeliveryProductsJoin editDeliveryProdcutsJoin = deliveryViewModel.getEditDeliveryProductsJoin();

        if (editDeliveryProdcutsJoin != null) {
            //update case
            nameEditText.setText(editDeliveryProdcutsJoin.productName);
            priceEditText.setText(StringTools.PriceFormat.format(editDeliveryProdcutsJoin.priceUnitVatIncl));
            vatEditText.setText(StringTools.PercentageFormat.format(editDeliveryProdcutsJoin.vat));
            quantityEditText.setText(editDeliveryProdcutsJoin.quantity + "");
            discountEditText.setText(StringTools.PercentageFormat.format(editDeliveryProdcutsJoin.discount));
        }


        saveButton.setOnClickListener(button -> {

            //validate
            boolean validated = true;

            //validate name
            String name = nameEditText.getText().toString();
            if (name == null || name.length() == 0) {
                nameInputLayout.setError(getString(R.string.fragment_delivery_products_custom_error_cannot_be_empty));
                validated = false;
            }

            //validate price
            BigDecimal price = null;
            if (priceEditText.getText() == null || priceEditText.getText().toString().length() == 0) {
                priceInputLayout.setError(getString(R.string.fragment_delivery_products_custom_error_cannot_be_empty));
                validated = false;
            }

            try {
                price = BigDecimal.valueOf(StringTools.PriceFormat.parse(priceEditText.getText().toString()).doubleValue()).setScale(2, RoundingMode.HALF_UP);
            } catch (ParseException e) {
                priceInputLayout.setError(getString(R.string.fragment_delivery_products_custom_error_bad_format));
                validated = false;
            }

            //validate vat
            BigDecimal  vat = null;
            if (vatEditText.getText() == null || vatEditText.getText().toString().length() == 0) {
                priceInputLayout.setError(getString(R.string.fragment_delivery_products_custom_error_cannot_be_empty));
                validated = false;
            }
            try {
                vat = BigDecimal.valueOf(StringTools.PercentageFormat.parse(vatEditText.getText().toString()).longValue());
            } catch (ParseException e) {
                vatInputLayout.setError(getString(R.string.fragment_delivery_products_custom_error_bad_format));
                validated = false;
            }


            //validate quantity
            int quantity = 0;
            if (quantityEditText.getText() == null || quantityEditText.getText().toString().length() == 0) {
                quantityInputLayout.setError(getString(R.string.fragment_delivery_products_custom_error_cannot_be_empty));
                validated = false;
            }
            try {
               quantity = Integer.parseInt(quantityEditText.getText().toString());
            } catch (NumberFormatException e) {
                quantityInputLayout.setError(getString(R.string.fragment_delivery_products_custom_error_bad_format));
                validated = false;
            }


            //validate discount
            BigDecimal discount = null;
            if (discountEditText.getText() == null || discountEditText.getText().toString().length() == 0) {
                discountInputLayout.setError(getString(R.string.fragment_delivery_products_custom_error_cannot_be_empty));
                validated = false;
            }
            try {
                discount = BigDecimal.valueOf(StringTools.PercentageFormat.parse(discountEditText.getText().toString()).longValue());
            } catch (ParseException e) {
                discountInputLayout.setError(getString(R.string.fragment_delivery_products_custom_error_bad_format));
                validated = false;
            }


            if(!validated)
                return;





            DeliveryProductsJoin deliveryProduct;
            if (editDeliveryProdcutsJoin != null) {
                deliveryProduct = editDeliveryProdcutsJoin;
            } else {
                deliveryProduct = new DeliveryProductsJoin();
            }

            deliveryProduct.deliveryId = deliveryId;
            deliveryProduct.type = "S";
            deliveryProduct.productName = name;
            deliveryProduct.priceUnitVatIncl = price;
            deliveryProduct.vat = vat;
            deliveryProduct.quantity = quantity;
            deliveryProduct.discount = discount;


            deliveryViewModel.saveProductDetail(deliveryProduct);

            getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
        });


    }
}