package com.nfrancoi.delivery.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nfrancoi.delivery.R;
import com.nfrancoi.delivery.room.entities.Delivery;
import com.nfrancoi.delivery.tools.CalendarTools;
import com.nfrancoi.delivery.viewmodel.DeliveryByDateViewModel;
import com.nfrancoi.delivery.viewmodel.DeliveryByDateViewModelFactory;
import com.nfrancoi.delivery.viewmodel.DeliveryViewModel;
import com.nfrancoi.delivery.viewmodel.DeliveryViewModelFactory;

import java.util.Calendar;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;

public class DeliveryListFragment extends Fragment implements DeliveryListAdapter.OnItemClickListener {

    private static final String TAG = DeliveryListFragment.class.toString();

    private static final int ACTIVITY_RESULT_REQUEST_PERMISSIONS = 100;
    public static final int ACTIVITY_RESULT_NEW_DELIVERY = 101;

    private Calendar calendarDay;

    private DeliveryByDateViewModel deliveryByDateViewModel;
    private DeliveryListAdapter deliveryListAdapter;


    public static DeliveryListFragment newInstance(Calendar calendarDay) {
        DeliveryListFragment fragment = new DeliveryListFragment();
        Bundle bundle = new Bundle();
        bundle.putLong("calendarDay", calendarDay.getTimeInMillis());
        fragment.setArguments(bundle);

        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            long time = Long.valueOf(getArguments().getLong("calendarDay", 0));
            calendarDay = Calendar.getInstance();
            calendarDay.setTimeInMillis(time);
        }else{
            new IllegalStateException("Fragment must be created with deliveryId in bundle");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_delivery_list, container, false);
        TextView date = (TextView) view.findViewById(R.id.fragment_delivery_list_date);
        date.setText(CalendarTools.DDMMYYYY.format(calendarDay.getTime()));

        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
        deliveryListAdapter = new DeliveryListAdapter(requireActivity());
        deliveryListAdapter.setOnItemClickListener(this);
        recyclerView.setAdapter(deliveryListAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireActivity());
        recyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        //this used here to have several viewmodel 1 per day
        DeliveryByDateViewModelFactory dvmFactory = new DeliveryByDateViewModelFactory(getActivity().getApplication(), calendarDay);
        String dvmKey = CalendarTools.YYYYMMDD.format(calendarDay.getTime());
        Log.d(TAG, "DeliveryByDateViewModelFactory KEY="+dvmKey);
        deliveryByDateViewModel = new ViewModelProvider(requireActivity(), dvmFactory).get(dvmKey,DeliveryByDateViewModel.class);

        deliveryByDateViewModel.getDeliveries().observe(getViewLifecycleOwner(), new Observer<List<Delivery>>() {
            @Override
            public void onChanged(@Nullable final List<Delivery> deliveries) {
                // Update the cached copy of the words in the adapter.
                deliveryListAdapter.setDeliveries(deliveries);
            }
        });

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(button -> {
            Delivery newDelivery = new Delivery();
            Single<Long> insertSingle = deliveryByDateViewModel.insert(newDelivery);
            insertSingle.subscribe(new SingleObserver<Long>() {
                @Override
                public void onSubscribe(Disposable d) {
                    System.out.println("on Subscribe");
                }

                @Override
                public void onSuccess(Long deliveryId) {
                    newDelivery.deliveryId = deliveryId;
                    showNewDeliveryFragment(newDelivery);
                }

                @Override
                public void onError(Throwable e) {
                    throw new IllegalStateException(e);
                }
            });


        });
        return view;

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onItemClick(Delivery selectedDelivery) {
        showNewDeliveryFragment(selectedDelivery);

    }

    private void showNewDeliveryFragment(Delivery selectedDelivery) {
        //prepare viewmodel for next Fragment
        DeliveryViewModelFactory dvmFactory = new DeliveryViewModelFactory(getActivity().getApplication(), selectedDelivery.deliveryId);
        DeliveryViewModel deliveryViewModel = ViewModelProviders.of(requireActivity(), dvmFactory).get(DeliveryViewModel.class);


        NewDeliveryFragment newDeliveryFragment = NewDeliveryFragment.newInstance(selectedDelivery.deliveryId);
        newDeliveryFragment.setRetainInstance(true);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_main_layout, newDeliveryFragment, "findThisFragment")
                .addToBackStack(null)
                .commit();
    }
}
