package com.nfrancoi.delivery.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nfrancoi.delivery.R;
import com.nfrancoi.delivery.room.entities.DeliveryProductsJoin;
import com.nfrancoi.delivery.viewmodel.DeliveryViewModel;
import com.nfrancoi.delivery.viewmodel.DeliveryViewModelFactory;

public class DeliveryProductsCustomSelectFragment extends Fragment {


    private Long deliveryId;

    public static DeliveryProductsCustomSelectFragment newInstance(Long deliveryId) {
        DeliveryProductsCustomSelectFragment fragment = new DeliveryProductsCustomSelectFragment();
        Bundle args = new Bundle();
        args.putLong("deliveryId", deliveryId);
        fragment.setArguments(args);

        return fragment;
    }

    private DeliveryProductsCustomSelectFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        //TODO do not pass deliveryId as class member. Fetch in arguments in method  onViewCreated
        this.deliveryId = args.getLong("deliveryId");
        if (this.deliveryId == null)
            throw new IllegalStateException("DeliveryProductsCustomSelectFragment must be called with deliveryId param");

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requireActivity().setTitle(R.string.fragment_delivery_products_custom_new_title);
        View view = inflater.inflate(R.layout.fragment_delivery_products_custom_select, container, false);


        return view;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        DeliveryViewModelFactory dvmFactory = new DeliveryViewModelFactory(getActivity().getApplication(), this.deliveryId);
        //scope fragment
        String key = this.deliveryId.toString();
        DeliveryViewModel deliveryViewModel = new ViewModelProvider(requireActivity(), dvmFactory).get(key, DeliveryViewModel.class);

        FloatingActionButton fab = view.findViewById(R.id.fragment_delivery_product_custom_select_add_button);
        fab.setOnClickListener(button -> {
            deliveryViewModel.setEditDeliveryProductsJoin(null);
            this.openDeliveryProductCustomNewDialogFragment(deliveryId);
        });


        RecyclerView recyclerView = view.findViewById(R.id.fragment_delivery_product_custom_select_rv);
        final DeliveryProductCustomListAdapter adapter = new DeliveryProductCustomListAdapter(requireActivity());
        recyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireActivity());
        recyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        deliveryViewModel.getSelectedSellDeliveryProducts().observe(getViewLifecycleOwner(), deliveryProductsJoins -> {
            adapter.setDeliveryProducts(deliveryProductsJoins);


        });

        adapter.setOnItemClickListener(new DeliveryProductCustomListAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(DeliveryProductsJoin item) {

                deliveryViewModel.setEditDeliveryProductsJoin(item);
                openDeliveryProductCustomNewDialogFragment(item.deliveryId);
            }

            @Override
            public void onDeleteClick(DeliveryProductsJoin item) {
                deliveryViewModel.deleteDeliveryProductsJoin(item);
            }
        });
    }


    private void openDeliveryProductCustomNewDialogFragment(Long deliveryid) {
        FragmentManager fm = requireActivity().getSupportFragmentManager();
        DeliveryProductCustomNewDialogFragment dialog = DeliveryProductCustomNewDialogFragment.newInstance(this.deliveryId);
        dialog.show(fm, "fragment_add_custom");

    }
}

