package com.nfrancoi.delivery.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.nfrancoi.delivery.R;
import com.nfrancoi.delivery.viewmodel.DeliveryViewModel;
import com.nfrancoi.delivery.viewmodel.DeliveryViewModelFactory;

public class DeliveryProductsSelectFragment extends Fragment {


    private DeliveryViewModel deliveryViewModel;

    private String type; //D:Deposit, T:Take
    private Long deliveryId;

    private TextInputEditText filterEditText;
    private Switch switchSelection;


    private TextWatcher filterTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (type.equals("D")) {
                deliveryViewModel.filterDepositDeliveryProductDetails(s.toString());
            }
            if (type.equals("T")) {
                deliveryViewModel.filterTakeDeliveryProductDetails(s.toString());
            }
        }
    };


    public static DeliveryProductsSelectFragment newInstance(Long deliveryId, String type) {
        DeliveryProductsSelectFragment fragment = new DeliveryProductsSelectFragment();
        Bundle args = new Bundle();
        args.putString("type", type);
        args.putLong("deliveryId", deliveryId);
        fragment.setArguments(args);

        return fragment;
    }

    private DeliveryProductsSelectFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            this.type = args.getString("type");
            this.deliveryId = args.getLong("deliveryId");
        } else {
            throw new IllegalStateException("SelectProductsFragment must be called with deliveryId abd type params");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_delivery_product_select, container, false);
        requireActivity().setTitle(R.string.fragment_select_products_title);
        filterEditText = view.findViewById(R.id.fragment_delivery_product_select_filter);
        switchSelection = view.findViewById(R.id.fragment_delivery_product_select_switch_only);

        return view;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        DeliveryViewModelFactory dvmFactory = new DeliveryViewModelFactory(getActivity().getApplication(), this.deliveryId);
        //scope fragment
        String key = this.deliveryId.toString();
        this.deliveryViewModel = ViewModelProviders.of(requireActivity(), dvmFactory).get(key, DeliveryViewModel.class);


        RecyclerView recyclerView = view.findViewById(R.id.fragment_delivery_product_select_rv);
        final DeliveryProductListAdapter adapter = new DeliveryProductListAdapter(requireActivity());
        recyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireActivity());
        recyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        if (type.equals("D")) {
            deliveryViewModel.getFilterDepositLD().observe(getViewLifecycleOwner(), filter -> {
                filterEditText.removeTextChangedListener(filterTextWatcher);
                filterEditText.setText("");
                filterEditText.append(filter);
                filterEditText.addTextChangedListener(filterTextWatcher);
            });

            deliveryViewModel.getFilteredDepositDeliveryProducts().observe(getViewLifecycleOwner(), deliveryProductDetails -> {
                adapter.setDeliveryProductDetails(deliveryProductDetails);

            });

            // zero quantity filter
            deliveryViewModel.getFilterDepositZeroQuantity().observe(getViewLifecycleOwner(), isFilter -> {
                this.switchSelection.setChecked(isFilter);
            });
            switchSelection.setOnCheckedChangeListener((buttonView, isChecked) -> {
                deliveryViewModel.setFilterDepositZeroQuantity(isChecked);
            });

        }

        if (type.equals("T")) {
            deliveryViewModel.getFilterTakeLD().observe(getViewLifecycleOwner(), filter -> {
                filterEditText.removeTextChangedListener(filterTextWatcher);
                filterEditText.setText("");
                filterEditText.append(filter);
                filterEditText.addTextChangedListener(filterTextWatcher);
            });

            deliveryViewModel.getFilteredTakeDeliveryProducts().observe(getViewLifecycleOwner(), deliveryProductDetails -> {
                System.out.println(deliveryProductDetails);

                adapter.setDeliveryProductDetails(deliveryProductDetails);
            });

            // zero quantity filter
            deliveryViewModel.getFilterTakeZeroQuantity().observe(getViewLifecycleOwner(), isFilter -> {
                this.switchSelection.setChecked(isFilter);
            });
            switchSelection.setOnCheckedChangeListener((buttonView, isChecked) -> {
                deliveryViewModel.setFilterTakeZeroQuantity(isChecked);
            });
        }


        adapter.setQuantityValueChangeListener(deliveryProductDetail -> {
            deliveryViewModel.saveProductDetail(deliveryProductDetail);
        });


    }


}