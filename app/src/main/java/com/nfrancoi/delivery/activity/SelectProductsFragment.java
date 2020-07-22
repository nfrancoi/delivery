package com.nfrancoi.delivery.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

public class SelectProductsFragment extends Fragment {


    private DeliveryViewModel deliveryViewModel;

    private String type; //D:Deposit, T:Take
    private Long deliveryId;

    private TextInputEditText filterEditText;


    private TextWatcher filterTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            deliveryViewModel.filterDepositDeliveryProductDetails(s.toString());
        }
    };


    public static SelectProductsFragment newInstance(Long deliveryId, String type) {
        SelectProductsFragment fragment = new SelectProductsFragment();
        Bundle args = new Bundle();
        args.putString("type", type);
        args.putLong("deliveryId", deliveryId);
        fragment.setArguments(args);

        return fragment;
    }

    private SelectProductsFragment() {
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
        View view = inflater.inflate(R.layout.fragment_select_products, container, false);
        requireActivity().setTitle(R.string.fragment_select_products_title);
        filterEditText = view.findViewById(R.id.fragment_select_products_filter);

        return view;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        DeliveryViewModelFactory dvmFactory = new DeliveryViewModelFactory(getActivity().getApplication(), this.deliveryId);
        //scope fragment
        String key = this.deliveryId.toString();
        this.deliveryViewModel = ViewModelProviders.of(requireActivity(), dvmFactory).get(key, DeliveryViewModel.class);


        RecyclerView recyclerView = view.findViewById(R.id.activity_select_product_recycler_view);
        final DeliveryProductDetailsListAdapter adapter = new DeliveryProductDetailsListAdapter(requireActivity());
        recyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireActivity());
        recyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        if (type.equals("D")) {
            deliveryViewModel.getFilterDepositDeliveryProductDetails().observe(getViewLifecycleOwner(), filter -> {
                filterEditText.removeTextChangedListener(filterTextWatcher);
                filterEditText.setText("");
                filterEditText.append(filter);
                filterEditText.addTextChangedListener(filterTextWatcher);
            });

            deliveryViewModel.getFilteredDepositDeliveryProductDetails().observe(getViewLifecycleOwner(), deliveryProductDetails -> {
                adapter.setDeliveryProductDetails(deliveryProductDetails);

            });
        }

        if (type.equals("T")) {
            deliveryViewModel.getSelectedTakeDeliveryProductDetails().observe(getViewLifecycleOwner(), deliveryProductDetails -> {
                System.out.println(deliveryProductDetails);

                adapter.setDeliveryProductDetails(deliveryProductDetails);
            });
        }



        adapter.setQuantityValueChangeListener(deliveryProductDetail -> {
            deliveryViewModel.saveProductDetail(deliveryProductDetail);
        });


    }


}