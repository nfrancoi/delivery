package com.nfrancoi.delivery.tools;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.print.PrintAttributes;
import android.print.pdf.PrintedPdfDocument;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nfrancoi.delivery.BuildConfig;
import com.nfrancoi.delivery.DeliveryApplication;
import com.nfrancoi.delivery.R;
import com.nfrancoi.delivery.activity.NoteProductDetailsListAdapter;
import com.nfrancoi.delivery.room.dao.DeliveryProductsJoinDao;
import com.nfrancoi.delivery.room.entities.Company;
import com.nfrancoi.delivery.room.entities.Delivery;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

public class NotePDFCreator {


    private Activity activity;

    private Delivery delivery;
    private Company company;
    private List<DeliveryProductsJoinDao.NoteDeliveryProductDetail> details;


    public NotePDFCreator(Activity activity, Company company, Delivery delivery, List<DeliveryProductsJoinDao.NoteDeliveryProductDetail> detail) {
        this.activity = activity;
        this.company = company;
        this.delivery = delivery;
        this.details = detail;

    }


    public void createClientNotePdf() {

        PrintAttributes printAttrs = new PrintAttributes.Builder().
                setColorMode(PrintAttributes.COLOR_MODE_COLOR).
                setMediaSize(PrintAttributes.MediaSize.ISO_A5).
                setResolution(new PrintAttributes.Resolution("zooey", "test", 300, 300)).
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
        date.setText(CalendarTools.DDMMYYYY.format(delivery.date.getTime()));

        TextView id = view.findViewById(R.id.pdf_note_id);
        id.setText(delivery.noteId);

        TextView pod = view.findViewById(R.id.pdf_note_point_of_delivery);
        pod.setText(delivery.pointOfDelivery.name + "\n"
                + delivery.pointOfDelivery.address);

        //list

        RecyclerView recyclerView = view.findViewById(R.id.pdf_note_recyclerview);
        NoteProductDetailsListAdapter adapter = new NoteProductDetailsListAdapter(activity);
        adapter.setNoteDeliveryProductDetails(details);
        recyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);


        TextView deliverName = view.findViewById(R.id.pdf_note_name_delivery);
//        deliverName.setText(delivery.employee.name);
        TextView deliverComment = view.findViewById(R.id.pdf_note_comments_delivery);
        deliverComment.setText(delivery.commentDelivery);


        TextView receiverName = view.findViewById(R.id.pdf_note_name_receiver);
        receiverName.setText(delivery.receiverName);
        TextView receiverComment = view.findViewById(R.id.pdf_note_comments_receiver);
        receiverComment.setText(delivery.commentReceiver);

        //signature
        ImageView signatureImageView = view.findViewById(R.id.pdf_note_signature);
        if (delivery.signatureBytes != null) {
            Bitmap signatureBitmap = BitmapTools.byteArrayToBitmap(delivery.signatureBytes);
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
        File externalFilesDir = DeliveryApplication.getApplicationExternalStorageDirectory();
        String noteFilePrefix = CalendarTools.YYYYMMDD.format(Calendar.getInstance().getTime());
        File file = new File(externalFilesDir, noteFilePrefix + "Note.pdf");

        try {
            document.writeTo(new FileOutputStream(file.getPath()));
            Toast.makeText(activity, R.string.pdf_note_creator_file_generated, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e("main", "error " + e.toString());
            Toast.makeText(activity, R.string.pdf_note_creator_file_generated + "\n" + e.toString(), Toast.LENGTH_LONG).show();
        }
        // close the document
        document.close();

        //start a PDF reader
        Uri fileURI = FileProvider.getUriForFile(activity.getApplicationContext(), BuildConfig.APPLICATION_ID, file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(fileURI, "application/pdf");
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        activity.startActivity(intent);

    }


}
