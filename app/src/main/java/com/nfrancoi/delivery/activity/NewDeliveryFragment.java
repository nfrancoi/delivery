package com.nfrancoi.delivery.activity;

import android.app.Activity;
import android.content.DialogInterface;
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
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.snackbar.Snackbar;
import com.nfrancoi.delivery.BuildConfig;
import com.nfrancoi.delivery.DeliveryApplication;
import com.nfrancoi.delivery.R;
import com.nfrancoi.delivery.room.entities.Delivery;
import com.nfrancoi.delivery.room.entities.Employee;
import com.nfrancoi.delivery.room.entities.PointOfDelivery;
import com.nfrancoi.delivery.tools.BitmapTools;
import com.nfrancoi.delivery.tools.FilterWithSpaceAdapter;
import com.nfrancoi.delivery.tools.NoteCreator;
import com.nfrancoi.delivery.tools.NotePDFCreator;
import com.nfrancoi.delivery.viewmodel.DeliveryViewModel;
import com.nfrancoi.delivery.viewmodel.DeliveryViewModelFactory;
import com.nfrancoi.delivery.worker.UploadNoteWorker;

import java.io.File;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Calendar;

public class NewDeliveryFragment extends Fragment implements DialogInterface.OnDismissListener {

    public static final String TAG = NewDeliveryFragment.class.toString();

    public static final String INTENT_DELIVERY_ID = "deliveryId";
    public static final int ACTIVITY_RESULT_DEPOSIT = 1;

    public static final String ACTIVITY_SELECT_PRODUCT_TITLE = "ACTIVITY_SELECT_PRODUCT_TITLE";


    //ViewModels
    private Long deliveryId;
    private DeliveryViewModel deliveryViewModel;

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

    private Switch vatApplicableSwitch;


    //Select Deposit
    private Button depositButton;
    private TextView countDepositTextView;
    private TextView priceDepositTextView;

    //Select take
    private Button takeButton;
    private TextView countTakeTextView;
    private TextView priceTakeTextView;

    //Select Sell
    private Button sellButton;
    private TextView countSellTextView;
    private TextView priceSellTextView;

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


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_new_delivery, container, false);

        requireActivity().setTitle(R.string.fragment_new_delivery_title);

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
        vatApplicableSwitch = view.findViewById(R.id.fragment_new_delivery_swith_vat);


        //
        // Deposit products
        //
        depositButton = view.findViewById(R.id.fragment_new_delivery_button_deposit);
        countDepositTextView = view.findViewById(R.id.fragment_new_delivery_text_deposit_quantity);
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
        countTakeTextView = view.findViewById(R.id.fragment_new_delivery_text_take_quantity);
        priceTakeTextView = view.findViewById(R.id.fragment_new_delivery_text_take_price);


        takeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewDeliveryFragment.this.showSelectProductsFragment("T");
            }
        });

        //
        // sell products
        //
        sellButton = view.findViewById(R.id.fragment_new_delivery_button_sell);
        countSellTextView = view.findViewById(R.id.fragment_new_delivery_text_sell_quantity);
        priceSellTextView = view.findViewById(R.id.fragment_new_delivery_text_sell_price);


        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewDeliveryFragment.this.showSelectProductsCustomFragment();
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

    private void activateCaptureFields(boolean isActive) {
        vatApplicableSwitch.setEnabled(isActive);
        depositButton.setEnabled(isActive);
        takeButton.setEnabled(isActive);
        sellButton.setEnabled(isActive);
        receiverNameTextView.setEnabled(isActive);
        receiverCommentTextView.setEnabled(isActive);
        deliverCommentTextView.setEnabled(isActive);
        employeeNameSpinner.setEnabled(isActive);
        podTextView.setEnabled(isActive);


    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //
        //ViewModels
        //
        if (getArguments() != null) {
            this.deliveryId = Long.valueOf(getArguments().getLong("deliveryId", 0));
        } else {
            new IllegalStateException("Fragment must be created with deliveryId in bundle");
        }

        DeliveryViewModelFactory dvmFactory = new DeliveryViewModelFactory(getActivity().getApplication(), this.deliveryId);
        //scope fragment
        String key = this.deliveryId.toString();
        deliveryViewModel = new ViewModelProvider(requireActivity(), dvmFactory).get(key, DeliveryViewModel.class);


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
            // Products selection take deposit sell
            //
            if (deliveryViewModel.isDeliveryReadyToSelectProducts(selectedDelivery)) {
                depositButton.setEnabled(true);
                takeButton.setEnabled(true);
                sellButton.setEnabled(true);
            } else {
                depositButton.setEnabled(false);
                takeButton.setEnabled(false);
                sellButton.setEnabled(false);
            }

            vatApplicableSwitch.setChecked(selectedDelivery.isVatApplicable);
            vatApplicableSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                selectedDelivery.isVatApplicable = isChecked;
                deliveryViewModel.updateDelivery(selectedDelivery);
            });

            //
            //Employee selection
            //
            employeeAdapter = new ArrayAdapter(requireActivity(), R.layout.widget_spinner_selected_employee, R.id.widget_spinner_employee_text);
            employeeAdapter.setDropDownViewResource(R.layout.widget_spinner_employee);

            employeeNameSpinner.setAdapter(employeeAdapter);
            deliveryViewModel.getActiveEmployees().observe(getViewLifecycleOwner(), employees -> {
                //fill drop down list
                employeeAdapter.addAll(employees);


                //chose default if not selected
                if (selectedDelivery.employee == null) {
                    LiveData<Employee> defaultEmployeeLD = deliveryViewModel.getEmployeeByDefault();
                    defaultEmployeeLD.observe(getViewLifecycleOwner(), defaultEmployee -> {
                        int selectedEmployeePosition = employeeAdapter.getPosition(defaultEmployee);
                        if (selectedEmployeePosition >= 0) {
                            deliveryViewModel.onSelectedEmployee(defaultEmployee);
                        }else{
                            deliveryViewModel.onSelectedEmployee(employeeAdapter.getItem(0));
                            Toast.makeText(requireActivity().getApplicationContext(), "Le livreur par défaut paramétré n'existe pas",
                                    Toast.LENGTH_LONG).show();
                        }

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
                    signatureDialog.setTargetFragment(NewDeliveryFragment.this, 1337);
                    signatureDialog.show(fm, "fragment_signature");
                    //NFR

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

            //disabel pannel when signed
            this.activateCaptureFields(!deliveryViewModel.isDeliverySigned(selectedDelivery));
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
        deliveryViewModel.getSelectedDepositDeliveryProducts().

                observe(getViewLifecycleOwner(), deliveryProductDetails ->

                {
                    int countDeposit = deliveryProductDetails.stream().mapToInt(value -> value.quantity).sum();
                    countDepositTextView.setText("" + countDeposit);
                    BigDecimal sumPriceDeposit = deliveryProductDetails.stream().map(value -> value.priceUnitVatIncl.multiply(BigDecimal.valueOf(value.quantity))).reduce(BigDecimal.ZERO, BigDecimal::add);

                    priceDepositTextView.setText(NumberFormat.getCurrencyInstance().format(sumPriceDeposit));

                });

        //
        //Take
        //
        deliveryViewModel.getSelectedTakeDeliveryProducts().

                observe(getViewLifecycleOwner(), deliveryProductDetails ->

                {
                    int sumQuantity = deliveryProductDetails.stream().mapToInt(value -> value.quantity).sum();
                    countTakeTextView.setText("" + sumQuantity);
                    BigDecimal sumPriceTake = deliveryProductDetails.stream().map(
                            value -> value.priceUnitVatIncl.multiply(BigDecimal.valueOf(value.quantity))).reduce(BigDecimal.ZERO, BigDecimal::add);

                    priceTakeTextView.setText(NumberFormat.getCurrencyInstance().format(sumPriceTake));
                });

        //
        //Sell
        //
        deliveryViewModel.getSelectedSellDeliveryProducts().

                observe(getViewLifecycleOwner(), deliveryProductDetails ->

                {
                    int sumQuantity = deliveryProductDetails.stream().mapToInt(value -> value.quantity).sum();
                    countSellTextView.setText("" + sumQuantity);
                    BigDecimal sumPriceTake = deliveryProductDetails.stream().map(
                            value -> value.priceUnitVatIncl.multiply(BigDecimal.valueOf(value.quantity))).reduce(BigDecimal.ZERO, BigDecimal::add);

                    priceSellTextView.setText(NumberFormat.getCurrencyInstance().format(sumPriceTake));
                });

        //
        // Total
        //
        deliveryViewModel.getSelectedDeliveryTotalAmount().observe(getViewLifecycleOwner(), totalBigDecimal ->
        {
            if (totalBigDecimal != null)
                this.priceTotalTextView.setText(NumberFormat.getCurrencyInstance().format(totalBigDecimal));
        });

        //
        // Documents
        //
        noteImageButton.setOnClickListener(view1 -> {
            Delivery delivery = deliveryViewModel.getSelectedDelivery().getValue();
            if (deliveryViewModel.isDeliverySigned(delivery)) {
                NewDeliveryFragment.this.openPDF(delivery.noteURI);
                return;
            }

            if (deliveryViewModel.isDeliveryReadyToGenerateDocuments(delivery)) {
                NewDeliveryFragment.this.generateNotePDF(true);

            } else {
                Snackbar.make(requireActivity().findViewById(android.R.id.content), R.string.fragment_new_delivery_snackbar_notreadytosign, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }

        });

        finishButton.setOnClickListener(view1 ->

        {
            Delivery currentDelivery = deliveryViewModel.getSelectedDelivery().getValue();
            currentDelivery.sentDate = Calendar.getInstance();
            currentDelivery.isMailSent = true;
            currentDelivery.isNoteSaved = false;
            currentDelivery.isAccountingDataSent = false;
            currentDelivery.syncErrorMessage = null;

            deliveryViewModel.updateDelivery(currentDelivery);


            String emailPointOfDelivery = currentDelivery.pointOfDelivery.email;
            String fileUriString = currentDelivery.noteURI;

            //TODO String emailCompany = noteViewModel.getNoteDataLiveData().getValue().company.email;
            String emailCompany = "";
            NewDeliveryFragment.this.sendNotePDFByMail(fileUriString, new String[]{emailCompany, emailPointOfDelivery});


            //save note file
            Constraints networkConstraint = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();
            Data inputData = new Data.Builder()
                    .putLong(UploadNoteWorker.PARAM_DELIVERY_ID, currentDelivery.deliveryId)
                    .build();

            OneTimeWorkRequest saveNoteFile = new OneTimeWorkRequest.Builder(UploadNoteWorker.class)
                    .setConstraints(networkConstraint).addTag(fileUriString).setInputData(inputData).build();

            WorkManager.getInstance(requireActivity().getApplicationContext()).enqueue(saveNoteFile);

            getActivity().onBackPressed();
        });


    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        //triggered when leave SignatureDialogFragment
        this.generateNotePDF(false);
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

        DeliveryProductsSelectFragment fragment = DeliveryProductsSelectFragment.newInstance(this.deliveryId, type);
        fragment.setRetainInstance(true);
        getFragmentManager().beginTransaction()
                .replace(R.id.activity_main_layout, fragment, "SelectProductFragment")
                .addToBackStack(null)
                .commit();
    }

    private void showSelectProductsCustomFragment() {

        DeliveryProductsCustomSelectFragment fragment = DeliveryProductsCustomSelectFragment.newInstance(this.deliveryId);
        fragment.setRetainInstance(true);
        getFragmentManager().beginTransaction()
                .replace(R.id.activity_main_layout, fragment, "SelectProductFragment")
                .addToBackStack(null)
                .commit();
    }


    private void generateNotePDF(boolean isAndOpen) {
        Delivery selectedDelivery = deliveryViewModel.getSelectedDelivery().getValue();


        // sort by type
        NoteCreator notePdfCreator = new NotePDFCreator(this, selectedDelivery);
        notePdfCreator.createNote().observe(getViewLifecycleOwner(), file -> {
            selectedDelivery.noteURI = file.toString();

            deliveryViewModel.updateDelivery(selectedDelivery);

            if (isAndOpen) {
                this.openPDF(selectedDelivery.noteURI);
            }
        });
    }


    private void openPDF(String pdfFile) {
        //start a PDF reader
        Activity mainActivity = requireActivity();
        Uri fileURI = FileProvider.getUriForFile(DeliveryApplication.getInstance().getApplicationContext(), BuildConfig.APPLICATION_ID, new File(pdfFile));
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(fileURI, "application/pdf");
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        mainActivity.startActivity(intent);

    }


    private void sendNotePDFByMail(String noteURI, String[] emails) {


        Uri fileURI = FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID, new File(noteURI));

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.pdf_note_mail_subject_prefix));
        intent.putExtra(Intent.EXTRA_EMAIL, emails);
        intent.putExtra(Intent.EXTRA_TEXT, "");
        intent.putExtra(Intent.EXTRA_STREAM, fileURI);
        intent.setType("application/pdf");
        startActivity(Intent.createChooser(intent, "Send mail"));

    }
}