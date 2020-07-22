package com.nfrancoi.delivery.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.snackbar.Snackbar;
import com.nfrancoi.delivery.R;
import com.nfrancoi.delivery.room.dao.DeliveryProductsJoinDao;
import com.nfrancoi.delivery.room.entities.Company;
import com.nfrancoi.delivery.room.entities.Delivery;
import com.nfrancoi.delivery.room.entities.Employee;
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
    private NoteViewModel noteViewModel;

    //Delivery
    private TextView receiverNameTextView;
    private TextView receiverCommentTextView;
    private TextView deliverCommentTextView;

    //Select employee
    private Spinner employeeNameSpinner;
    private ArrayAdapter<Employee> employeeAdapter;

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
        receiverCommentTextView = view.findViewById(R.id.fragment_new_delivery_text_receiver_comment);
        deliverCommentTextView = view.findViewById(R.id.fragment_new_delivery_text_deliver_comment);

        //
        // Select employee
        //
        employeeNameSpinner = view.findViewById(R.id.fragment_new_delivery_text_select_deliver_name);

        //
        //Select PointOfDelivery
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

        //
        //ViewModels
        //
        DeliveryViewModelFactory dvmFactory = new DeliveryViewModelFactory(getActivity().getApplication(), this.deliveryId);
        //scope fragment
        String key = this.deliveryId.toString();
        deliveryViewModel = ViewModelProviders.of(requireActivity(), dvmFactory).get(key, DeliveryViewModel.class);
        noteViewModel = ViewModelProviders.of(this, new NoteViewModelFactory(getActivity().getApplication(), this.deliveryId)).get(NoteViewModel.class);


        //
        // Delivery
        //
        deliveryViewModel.getSelectedDelivery().observe(getViewLifecycleOwner(), selectedDelivery -> {
            if (selectedDelivery.receiverName != null)
                receiverNameTextView.setText(selectedDelivery.receiverName);
            if (selectedDelivery.commentReceiver != null)
                receiverCommentTextView.setText(selectedDelivery.commentReceiver);
            if (selectedDelivery.commentDelivery != null)
                deliverCommentTextView.setText(selectedDelivery.commentDelivery);

            //
            // Product selection
            //
            if(deliveryViewModel.isDeliveryReadyToSelectProducts(selectedDelivery)) {
                depositButton.setEnabled(true);
                takeButton.setEnabled(true);
            }else{
                depositButton.setEnabled(false);
                takeButton.setEnabled(false);
            }

            //
            //Employee selection
            //
            employeeAdapter = new ArrayAdapter(requireActivity(), R.layout.widget_spinner_selected_employee, R.id.widget_spinner_employee_text);
            employeeAdapter.setDropDownViewResource(R.layout.widget_spinner_employee);

            employeeNameSpinner.setAdapter(employeeAdapter);
            deliveryViewModel.getEmployees().observe(getViewLifecycleOwner(), employees -> {
                //fill drop down list
                employeeAdapter.addAll(employees);


                //chose default if not selected
                if (selectedDelivery.employee == null) {
                    LiveData<Employee> defaultEmployeeLD = deliveryViewModel.getEmployeeByDefault();
                    defaultEmployeeLD.observe(getViewLifecycleOwner(), defaultEmployee -> {
                        int selectedEmployeePosition = employeeAdapter.getPosition(defaultEmployee);
                        deliveryViewModel.onSelectedEmployee(defaultEmployee);
                    });
                }
                //previously selected
                else {
                    int selectedEmployeePosition = employeeAdapter.getPosition(selectedDelivery.employee);
                    employeeNameSpinner.setOnItemSelectedListener(null);
                    employeeNameSpinner.setSelection(selectedEmployeePosition);
                    employeeNameSpinner.setOnItemSelectedListener(new EmployeeSpinnerOnItemSelectedListener());
                }



            });


            if (selectedDelivery.pointOfDelivery != null) {
                podTextView.setText(selectedDelivery.pointOfDelivery.name);
                podTextView.dismissDropDown();
            }
            if (selectedDelivery.signatureBytes != null) {
                Bitmap signatureBitmap = BitmapTools.byteArrayToBitmap(selectedDelivery.signatureBytes);
                signatureImageView.setImageBitmap(signatureBitmap);
            }


            receiverNameTextView.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    selectedDelivery.receiverName = receiverNameTextView.getText().toString();
                    deliveryViewModel.updateSelectedDelivery();
                }
            });

            deliverCommentTextView.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    selectedDelivery.commentDelivery = deliverCommentTextView.getText().toString();
                    deliveryViewModel.updateSelectedDelivery();
                }
            });
            receiverCommentTextView.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    selectedDelivery.commentReceiver = receiverCommentTextView.getText().toString();
                    deliveryViewModel.updateSelectedDelivery();
                }
            });

            //note image
            if (deliveryViewModel.isDeliverySigned(selectedDelivery)) {
                noteImageButton.setVisibility(View.VISIBLE);
                noteImageButton.setColorFilter(ContextCompat.getColor(requireContext(), R.color.greenStatus));
            } else if (deliveryViewModel.isDeliveryReadyToSign(selectedDelivery)) {
                noteImageButton.setVisibility(View.VISIBLE);
                noteImageButton.setColorFilter(ContextCompat.getColor(requireContext(), R.color.orangeStatus));
            } else if (deliveryViewModel.isDeliveryReadyToGenerateDocuments(selectedDelivery)) {
                noteImageButton.setVisibility(View.VISIBLE);
                noteImageButton.setColorFilter(null);
            } else {
                noteImageButton.setVisibility(View.INVISIBLE);
                noteImageButton.setColorFilter(null);
            }


            //Signature
            signatureImageView.setOnClickListener(imageView -> {
                //reset signature
                selectedDelivery.signatureBytes = null;
                deliveryViewModel.updateDelivery(selectedDelivery);
                signatureImageView.setImageBitmap(null);


                //check all data is set
                if (deliveryViewModel.isDeliveryReadyToSign(selectedDelivery)) {
                    FragmentManager fm = requireActivity().getSupportFragmentManager();
                    SignatureDialogFragment signatureDialog = SignatureDialogFragment.newInstance(selectedDelivery.deliveryId);
                    signatureDialog.show(fm, "fragment_signature");
                } else {
                    Snackbar.make(requireActivity().findViewById(android.R.id.content), R.string.fragment_new_delivery_snackbar_notreadytosign, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }


            });

            //finished button
            if (deliveryViewModel.isDeliverySigned(selectedDelivery)) {
                finishButton.setEnabled(true);
                finishButton.setAlpha(1f);
            } else {
                finishButton.setEnabled(false);
                finishButton.setAlpha(.5f);
            }
        });


        //
        // Point of delivery selection
        //

        podAdapter = new FilterWithSpaceAdapter<PointOfDelivery>(

                requireActivity(), android.R.layout.simple_dropdown_item_1line, android.R.id.text1);
        podTextView.setAdapter(podAdapter);
        deliveryViewModel.getPointOfDeliveries().observe(getViewLifecycleOwner(), pods -> {
            // Update the cached copy of the words in the adapter.
            podAdapter.addAll(pods);
        });

        podTextView.setOnItemClickListener((parent, arg1, position, arg3) -> {
            //remove keyboard
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(podTextView.getContext().INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(podTextView.getApplicationWindowToken(), 0);

            Object item = parent.getItemAtPosition(position);
            if (item instanceof PointOfDelivery) {
                PointOfDelivery pod = (PointOfDelivery) item;
                deliveryViewModel.onSelectedPointOfDelivery(pod);
            }
        });


        //
        //Deposit
        //
        deliveryViewModel.getSelectedDepositDeliveryProductDetails().

                observe(getViewLifecycleOwner(), deliveryProductDetails ->

                {
                    int countDeposit = deliveryProductDetails.stream().mapToInt(value -> value.quantity).sum();
                    countDepositTextView.setText("" + countDeposit);
                    BigDecimal sumPriceDeposit = deliveryProductDetails.stream().map(value -> value.priceHtUnit.multiply(BigDecimal.valueOf(value.quantity))).reduce(BigDecimal.ZERO, BigDecimal::add);

                    priceDepositTextView.setText(NumberFormat.getCurrencyInstance().format(sumPriceDeposit));

                });

        //
        //Take
        //
        deliveryViewModel.getSelectedTakeDeliveryProductDetails().

                observe(getViewLifecycleOwner(), deliveryProductDetails ->

                {
                    int sumQuantity = deliveryProductDetails.stream().mapToInt(value -> value.quantity).sum();
                    countTakeTextView.setText("" + sumQuantity);
                    BigDecimal sumPriceTake = deliveryProductDetails.stream().map(
                            value -> value.priceHtUnit.multiply(BigDecimal.valueOf(value.quantity))).reduce(BigDecimal.ZERO, BigDecimal::add);

                    priceTakeTextView.setText(NumberFormat.getCurrencyInstance().format(sumPriceTake));
                });

        //
        // Total
        //
        deliveryViewModel.getSelectedDeliveryTotalAmount().

                observe(getViewLifecycleOwner(), totalBigDecimal ->

                {
                    if (totalBigDecimal != null)
                        this.priceTotalTextView.setText(NumberFormat.getCurrencyInstance().format(totalBigDecimal));
                });

        //
        // Documents
        //
        noteImageButton.setOnClickListener(view1 ->

        {
            if (deliveryViewModel.isDeliveryReadyToGenerateDocuments(deliveryViewModel.getSelectedDelivery().getValue()))
                NewDeliveryFragment.this.generateAndOpenNotePDF();
            else
                Snackbar.make(requireActivity().findViewById(android.R.id.content), R.string.fragment_new_delivery_snackbar_notreadytosign, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

        });

        finishButton.setOnClickListener(view1 ->

        {
            Delivery currentDelivery = deliveryViewModel.getSelectedDelivery().getValue();
            currentDelivery.sentDate = Calendar.getInstance();
            deliveryViewModel.updateDelivery(currentDelivery);
            NewDeliveryFragment.this.sendNotePDFByMail();
        });


    }

    //
    // Employee
    //
    private class EmployeeSpinnerOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

        boolean isFirstTimeCalled = true;
        @Override
        public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
            if (isFirstTimeCalled) {
                isFirstTimeCalled = false;
                return;
            }
            Object item = parent.getSelectedItem();
            if (item instanceof Employee) {
                Employee employee = (Employee) item;
                deliveryViewModel.onSelectedEmployee(employee);
            }
        }

        @Override
        public void onNothingSelected(android.widget.AdapterView<?> parent) {
        }
    }


    private void showSelectProductsFragment(String type) {

        SelectProductsFragment fragment = SelectProductsFragment.newInstance(this.deliveryId, type);
        fragment.setRetainInstance(true);
        getFragmentManager().beginTransaction()
                .replace(R.id.activity_main_layout, fragment, "SelectProductFragment")
                .addToBackStack(null)
                .commit();
    }


    private void generateAndOpenNotePDF() {
        Delivery selectedDelivery = deliveryViewModel.getSelectedDelivery().getValue();
        //Calculate noteId
        selectedDelivery.noteId = CalendarTools.YYYYMM.format(Calendar.getInstance().getTime()) + "_" + selectedDelivery.deliveryId;
        deliveryViewModel.updateDelivery(selectedDelivery);

        noteViewModel.getNoteLiveData().observe(getViewLifecycleOwner(), pair -> {
            Company company = pair.first;
            List<DeliveryProductsJoinDao.DeliveryProductDetail> deliveryProductNoteDetails = pair.second;
            NotePDFCreator notePdfCreator = new NotePDFCreator(getActivity(),
                    company, selectedDelivery, deliveryProductNoteDetails,
                    noteViewModel.getTotalHt(), noteViewModel.getTotalTaxes(), noteViewModel.getTotal());

            Uri noteUri = notePdfCreator.createClientNotePdf();
            selectedDelivery.noteURI = noteUri.toString();
            deliveryViewModel.updateDelivery(selectedDelivery);

            //start a PDF reader
            Activity mainActivity = requireActivity();
            Uri fileURI = Uri.parse(selectedDelivery.noteURI);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(fileURI, "application/pdf");
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            mainActivity.startActivity(intent);

        });


    }


    private void sendNotePDFByMail() {


        Delivery selectedDelivery = deliveryViewModel.getSelectedDelivery().getValue();
        String emailPointOfDelivery = selectedDelivery.pointOfDelivery.email;
        String noteURI = selectedDelivery.noteURI;


        noteViewModel.getCompany().observe(getViewLifecycleOwner(), company -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.pdf_note_mail_subject_prefix));
            intent.putExtra(Intent.EXTRA_EMAIL,
                    new String[]{company.email, emailPointOfDelivery});
            intent.putExtra(Intent.EXTRA_TEXT, "");
            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(noteURI));
            intent.setType("application/pdf");
            startActivity(Intent.createChooser(intent, "Send mail"));

        });

    }
}