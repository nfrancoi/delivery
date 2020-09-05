package com.nfrancoi.delivery.worker;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.api.services.sheets.v4.model.ValueRange;
import com.nfrancoi.delivery.DeliveryApplication;
import com.nfrancoi.delivery.R;
import com.nfrancoi.delivery.googleapi.GoogleApiGateway;
import com.nfrancoi.delivery.repository.Repository;
import com.nfrancoi.delivery.room.entities.Company;
import com.nfrancoi.delivery.room.entities.Employee;
import com.nfrancoi.delivery.room.entities.PointOfDelivery;
import com.nfrancoi.delivery.room.entities.Product;
import com.nfrancoi.delivery.tools.CalendarTools;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;


public class SyncDataBaseWorker extends Worker {

    private static final String TAG = SyncDataBaseWorker.class.toString();


    private static String NOTIFICATION_TITLE = DeliveryApplication.getInstance().getString(R.string.sync_notification_title);


    public SyncDataBaseWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        List<String> logs = new ArrayList<>(0);

        try {
            logs.add("Start Companies");
            ValueRange resultsCompany = GoogleApiGateway.getInstance().getCompaniesGoogleSheet();
            List<Company> companies = this.mapToCompany(resultsCompany);
            logs.addAll(Repository.getInstance().syncAllCompanies(companies));


            logs.add("Start PointOfDeliveries");
            ValueRange resultsPod = GoogleApiGateway.getInstance().getPointOfDeliveriesGoogleSheet();
            List<PointOfDelivery> pods = this.mapToPointOfDeliveries(resultsPod);
            logs.addAll(Repository.getInstance().syncAllPointOfDelivery(pods));

            logs.add("Start Products");
            ValueRange resultsProducts = GoogleApiGateway.getInstance().getProductsGoogleSheet();
            List<Product> products = this.mapToProducts(resultsProducts);
            logs.addAll(Repository.getInstance().syncAllProducts(products));


            logs.add("Start Employees");
            ValueRange resultsEmployees = GoogleApiGateway.getInstance().getEmployeeGoogleSheet();
            List<Employee> employees = this.mapToEmployees(resultsEmployees);
            logs.addAll(Repository.getInstance().syncAllEmployee(employees));



        } catch (Exception e) {
            e.printStackTrace();
            WorkerUtils.makeStatusNotification(NOTIFICATION_TITLE, "Erreur:" + e.getMessage(), getApplicationContext());

            SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(DeliveryApplication.getInstance().getBaseContext()).edit();
            prefs.putString("sync_start_button", "ERREUR: "+ e.getMessage());
            prefs.commit();

            return Result.failure();

        }

        WorkerUtils.makeStatusNotification(NOTIFICATION_TITLE, "done", getApplicationContext());

        logs.stream().forEach(s -> {
            Log.i(TAG, s);
        });

        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(DeliveryApplication.getInstance().getBaseContext()).edit();
        prefs.putString("sync_start_button", CalendarTools.DDMMYYYY.format(Calendar.getInstance().getTime()) + " " + CalendarTools.HHmm.format(Calendar.getInstance().getTime()));
        prefs.apply();


        return Result.success();
    }



    private List<PointOfDelivery> mapToPointOfDeliveries(ValueRange result) {
        List<List<Object>> resultList = result.getValues();
        List<PointOfDelivery> pods = resultList.stream().skip(1).map(objects -> {
            int i = 0;
            Long podId = Long.valueOf("" + objects.get(i++));
            String name = "" + objects.get(i++);
            String address = "" + objects.get(i++);
            String mails = "" + objects.get(i++);
            BigDecimal discountPercentage = BigDecimal.valueOf(Double.parseDouble("" + objects.get(i++)));
            Boolean isActive = ("" + objects.get(i++)).equals("Oui") ? true : false;

            PointOfDelivery pod = new PointOfDelivery(podId, name, address, discountPercentage, mails, isActive);
            return pod;

        }).collect(Collectors.toList());

        return pods;

    }

    private List<Product> mapToProducts(ValueRange resultsProducts) {

        List<List<Object>> resultList = resultsProducts.getValues();
        List<Product> products = resultList.stream().skip(1).map(objects -> {
            int i = 0;
            Long id = Long.valueOf("" + objects.get(i++));
            String name = "" + objects.get(i++);
            String type = "" + objects.get(i++);
            BigDecimal priceVat =  BigDecimal.valueOf(Double.parseDouble("" +objects.get(i++)));
            BigDecimal vat = BigDecimal.valueOf(Double.parseDouble("" +objects.get(i++)));
            Boolean isActive = ("" + objects.get(i++)).equals("Oui") ? true : false;

            Product product = new Product(id, name, type, priceVat, vat, isActive);
            return product;

        }).collect(Collectors.toList());

        return products;
    }

    private List<Company> mapToCompany(ValueRange resultsProducts) {

        List<List<Object>> resultList = resultsProducts.getValues();
        List<Company> companies = resultList.stream().skip(1).map(objects -> {
            int i = 0;
            Long id = Long.valueOf("" + objects.get(i++));
            String name = "" + objects.get(i++);
            String address = "" + objects.get(i++);
            String phone1 = "" + objects.get(i++);
            String phone2 = "" + objects.get(i++);
            String email = "" + objects.get(i++);
            String vat = "" + objects.get(i++);
            String account = "" + objects.get(i++);
            Boolean isActive = ("" + objects.get(i++)).equals("Oui") ? true : false;

            Company company = new Company(id, name, address, phone1, phone2, email, vat, account,isActive);
            return company;

        }).collect(Collectors.toList());

        return companies;
    }


    private List<Employee> mapToEmployees(ValueRange resultsEmployees) {


        List<List<Object>> resultList = resultsEmployees.getValues();
        List<Employee> employees = resultList.stream().skip(1).map(objects -> {
            int i = 0;
            Long id = Long.valueOf("" + objects.get(i++));
            String name = "" + objects.get(i++);
            String notePrefix = "" + objects.get(i++);
            Boolean isActive = ("" + objects.get(i++)).equals("Oui") ? true : false;

            Employee employee = new Employee(id, name, notePrefix, isActive);
            return employee;

        }).collect(Collectors.toList());

        return employees;
    }


}
