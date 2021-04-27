package com.nfrancoi.delivery.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.github.gcacace.signaturepad.views.SignaturePad;
import com.nfrancoi.delivery.R;
import com.nfrancoi.delivery.room.entities.Delivery;
import com.nfrancoi.delivery.tools.BitmapTools;
import com.nfrancoi.delivery.viewmodel.DeliveryViewModel;
import com.nfrancoi.delivery.viewmodel.DeliveryViewModelFactory;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SignatureDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SignatureDialogFragment extends DialogFragment {


    private Long deliveryId;

    private SignaturePad signaturePad;
    private Button saveButton;

    public SignatureDialogFragment() {
        // Required empty public constructor
    }


    public static SignatureDialogFragment newInstance(Long deliveryId) {
        SignatureDialogFragment fragment = new SignatureDialogFragment();
        Bundle args = new Bundle();
        args.putLong("deliveryId", deliveryId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            deliveryId = getArguments().getLong("deliveryId");

        }
    }

    @Override
    public void onResume() {
        super.onResume();

        Window window = getDialog().getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 1000);
        window.setGravity(Gravity.CENTER);

    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getDialog().setTitle(R.string.fragment_signature_dialog_title);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_signature_dialog, container, false);
        signaturePad = view.findViewById(R.id.fragment_signature_pad);
        saveButton = view.findViewById(R.id.fragment_signature_save_button);
        saveButton.setEnabled(false);

        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        DeliveryViewModelFactory dvmFactory = new DeliveryViewModelFactory(getActivity().getApplication(), this.deliveryId);
        //scope fragment
        String key = this.deliveryId.toString();
        DeliveryViewModel deliveryViewModel = ViewModelProviders.of(requireActivity(), dvmFactory).get(key, DeliveryViewModel.class);
        saveButton.setOnClickListener(button -> {

            byte[] signatureBytes = BitmapTools.bitmapToByteArray(signaturePad.getSignatureBitmap());
            Delivery selectedDelivery = deliveryViewModel.getSelectedDelivery().getValue();
            selectedDelivery.signatureBytes = signatureBytes;
            deliveryViewModel.updateDeliverySync(selectedDelivery);

            dismiss();
            // getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();

        });


        deliveryViewModel.getSelectedDelivery().observe(getViewLifecycleOwner(), selectedDelivery -> {
            saveButton.setEnabled(true);

            //
            // Signature
            //
            signaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {
                @Override
                public void onStartSigning() {

                }

                @Override
                public void onSigned() {


                }

                @Override
                public void onClear() {
                    deliveryViewModel.getSelectedDelivery().getValue();
                    selectedDelivery.signatureBytes = null;
                    deliveryViewModel.updateDeliverySync(selectedDelivery);
                }
            });

        });


    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);
        Fragment parentFragment = getTargetFragment();
        if (parentFragment instanceof DialogInterface.OnDismissListener) {
            ((DialogInterface.OnDismissListener) parentFragment).onDismiss(dialog);
        }
    }
}