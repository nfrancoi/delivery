package com.nfrancoi.delivery.activity;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.nfrancoi.delivery.R;
import com.nfrancoi.delivery.tools.CalendarTools;

import java.util.Calendar;

public class DeliverySwipeFragment extends Fragment {
    //factory
    public static DeliverySwipeFragment newInstance(Calendar calendar) {
        DeliverySwipeFragment fragment = new DeliverySwipeFragment(calendar);

        return fragment;
    }

    public final int ITEM_COUNT = 31;

    private ViewPager2 viewPager;
    private Calendar calendar;

    private DeliverySwipeFragment(Calendar calendar) {
        this.calendar = CalendarTools.roundByDay(calendar);
    }

    class ViewPagerAdapter extends FragmentStateAdapter {
        public ViewPagerAdapter(Fragment fragment) {
            super(fragment);


        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            // Return a NEW fragment instance in createFragment(int)
            Calendar calendarPosition = (Calendar) calendar.clone();
            calendarPosition.add(Calendar.DAY_OF_MONTH, getDaysToAdd(position));
            DeliveryListFragment deliveryListFragment =  DeliveryListFragment.newInstance(calendarPosition);
            return deliveryListFragment;
        }

        // 0   1   2   3   4 (position)
        //-4  -3  -2  -1   0 (days to add)
        private int getDaysToAdd(int position){
            return position-getItemCount()+1;


        }

        @Override
        public int getItemCount() {
            return ITEM_COUNT;
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_delivery_swipe, container, false);
        view.findViewById(R.id.fragment_delivery_swipe_viewpager2);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewPager = view.findViewById(R.id.fragment_delivery_swipe_viewpager2);
        viewPager.setAdapter(new ViewPagerAdapter(this));
        this.goToLastPage();


    }

    private void goToLastPage() {
        //workaroud to set the initial position
        new Handler().postDelayed(() -> viewPager.setCurrentItem(ITEM_COUNT, false), 100);
    }

}


