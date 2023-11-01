package com.nfrancoi.delivery.tools;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.print.PrintAttributes;
import android.print.pdf.PrintedPdfDocument;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nfrancoi.delivery.DeliveryApplication;
import com.nfrancoi.delivery.R;
import com.nfrancoi.delivery.activity.NoteProductDetailsListAdapter;
import com.nfrancoi.delivery.room.entities.Delivery;
import com.nfrancoi.delivery.viewmodel.data.NoteData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;

public class NotePDFCreator extends NoteCreator {


    private Activity activity;
    private Fragment fragment;

     public NotePDFCreator(Fragment fragment, Delivery delivery) {
        super(delivery);
        this.fragment = fragment;
        this.activity = fragment.requireActivity();

    }


    private File createClientNotePdf(NoteData noteData) {

        WindowManager wm = (WindowManager) DeliveryApplication.getInstance().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;



        PrintAttributes printAttrs = new PrintAttributes.Builder().
                setColorMode(PrintAttributes.COLOR_MODE_COLOR).
                setMediaSize(PrintAttributes.MediaSize.ISO_A5).
                //ratio to manage muultiple resolution screen
                setResolution(new PrintAttributes.Resolution("zooey", "test", (int)(width*0.3), (int) (width*0.3))).
                setMinMargins(PrintAttributes.Margins.NO_MARGINS).
                build();
        PdfDocument document = new PrintedPdfDocument(activity, printAttrs);


        // create a new document
        //PdfDocument document = new PdfDocument();
        // crate a page description

        //a5 ps dimensions
        int pageHeight = 595;
        int pageWidth = 420;
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        // start a page
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        View view = activity.getLayoutInflater().inflate(R.layout.pdf_note_client, null, false);

        //
        // Binding
        //
        TextView date = view.findViewById(R.id.pdf_note_date);
        date.setText(CalendarTools.DDMMYYYY.format(noteData.delivery.startDate.getTime()));

        TextView id = view.findViewById(R.id.pdf_note_id);
        id.setText(noteData.delivery.noteId);

        TextView pod = view.findViewById(R.id.pdf_note_point_of_delivery);
        pod.setText(noteData.delivery.pointOfDelivery.name + "\n"
                +noteData.delivery.pointOfDelivery.address + "\n"
        +noteData.delivery.pointOfDelivery.email);

        TextView companyTextView = view.findViewById(R.id.pdf_note_company);
        companyTextView.setText(noteData.company.name + "\n"
                + noteData.company.address + "\n"
                + noteData.company.phoneNumber1
                + "\n" + noteData.company.phoneNumber2);

        //list

        RecyclerView recyclerView = view.findViewById(R.id.pdf_note_recyclerview);
        NoteProductDetailsListAdapter adapter = new NoteProductDetailsListAdapter(activity);
        adapter.setNoteDeliveryProductDetails(noteData.deliveryProductNoteDetails);
        recyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);


        TextView deliverName = view.findViewById(R.id.pdf_note_name_delivery);
        deliverName.setText(noteData.delivery.employee.name);
        TextView deliverComment = view.findViewById(R.id.pdf_note_comments_delivery);
        deliverComment.setText(noteData.delivery.commentDelivery);


        TextView receiverName = view.findViewById(R.id.pdf_note_name_receiver);
        receiverName.setText(noteData.delivery.receiverName);
        TextView receiverComment = view.findViewById(R.id.pdf_note_comments_receiver);
        receiverComment.setText(noteData.delivery.commentReceiver);

        //TOTALS
        TextView totalDeposHtText = view.findViewById(R.id.pdf_note_client_total_depos_ht_text);
        totalDeposHtText.setText(NumberFormat.getCurrencyInstance().format(noteData.totalDeposVatExcl.doubleValue()));

        TextView totalTakeHtText = view.findViewById(R.id.pdf_note_client_total_reprise_ht_text);
        totalTakeHtText.setText(NumberFormat.getCurrencyInstance().format(noteData.totalTakeVatExcl.doubleValue()));

        TextView totalHtText = view.findViewById(R.id.pdf_note_client_total_ht_text);
        totalHtText.setText(NumberFormat.getCurrencyInstance().format(noteData.totalVatExcl.doubleValue()));

        TextView totalTaxesText = view.findViewById(R.id.pdf_note_client_total_taxes_text);
        totalTaxesText.setText(NumberFormat.getCurrencyInstance().format(noteData.totalTaxes.doubleValue()));

        TextView totalText = view.findViewById(R.id.pdf_note_client_total_text);
        totalText.setText(NumberFormat.getCurrencyInstance().format(noteData.total.doubleValue()));


        //signature
        ImageView signatureImageView = view.findViewById(R.id.pdf_note_signature);
        if (noteData.delivery.signatureBytes != null) {
            Bitmap signatureBitmap = BitmapTools.byteArrayToBitmap(noteData.delivery.signatureBytes);
            signatureImageView.setImageBitmap(signatureBitmap);
        }

        //scale the caneva to the pdf size
        PrintAttributes.MediaSize pageSizeInches = printAttrs.getMediaSize();
        PrintAttributes.Resolution resolutionPixel = printAttrs.getResolution();
        int hdpi = resolutionPixel.getHorizontalDpi();
        int vdpi = resolutionPixel.getVerticalDpi();
        int availablePageWidthPx = pageSizeInches.getWidthMils() * hdpi / 1000;
        int availablePageHeightPx = pageSizeInches.getHeightMils() * hdpi / 1000;
        view.setLayoutParams(new LinearLayout.LayoutParams(availablePageWidthPx, availablePageHeightPx));

        int measureWidth = View.MeasureSpec.makeMeasureSpec(availablePageWidthPx, View.MeasureSpec.EXACTLY);
        int measureHeight = View.MeasureSpec.makeMeasureSpec(availablePageHeightPx, View.MeasureSpec.EXACTLY);

        view.measure(measureWidth, measureHeight);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        canvas.scale(72f / hdpi, 72f / vdpi);
        view.draw(canvas);


        // finish the page
        document.finishPage(page);


        // write the document content
        File externalFilesDir = DeliveryApplication.getApplicationNotesStorageDirectory();
        File file = new File(externalFilesDir, noteData.delivery.noteId + ".pdf");

        try {
            document.writeTo(new FileOutputStream(file.getPath()));
            Toast.makeText(activity, R.string.pdf_note_creator_file_generated, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e("main", "error " + e.toString());
            e.printStackTrace();
            Toast.makeText(activity, R.string.pdf_note_creator_file_generated + "\n" + e.toString(), Toast.LENGTH_LONG).show();
        }
        // close the document
        document.close();

        //start a PDF reader

        return file;
    }


    @Override
    public LiveData<File> createNote() {
        MutableLiveData<File> fileLiveData = new MutableLiveData<>();
        super.getNoteData().observe(fragment, noteData -> {

            File note = this.createClientNotePdf(noteData);
            fileLiveData.postValue(note);

        });

        return fileLiveData;
    }
}
