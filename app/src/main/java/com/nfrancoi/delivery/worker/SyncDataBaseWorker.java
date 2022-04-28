package com.nfrancoi.delivery.worker;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class SyncDataBaseWorker extends Worker {

    private static final String TAG = SyncDataBaseWorker.class.toString();


    private static String NOTIFICATION_TITLE = DeliveryApplication.getInstance().getString(R.string.sync_notification_title);


    private int logCounter;

    public SyncDataBaseWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {

        List<String> logs = new ArrayList<>(0);

        if (!this.isOnline()) {
            new HandlerToast(getApplicationContext(), R.string.sync_database_worker_offline_error);
            return Result.failure();
        }

        boolean failure = false;
        try {
            Log.i(TAG, "Start company");
            ValueRange resultsCompany = GoogleApiGateway.getInstance().getCompaniesGoogleSheet();
            List<Company> companies = this.mapToCompany(resultsCompany);
            logs.addAll(Repository.getInstance().syncAllCompanies(companies));

        } catch (Exception e) {
            e.printStackTrace();
            failure = true;
            this.reportSynchronisationError("Synchro de la companie" + "\n" + e.getMessage());

        }

        try {
            Log.i(TAG, "Start Point of deliveries");
            ValueRange resultsPod = GoogleApiGateway.getInstance().getPointOfDeliveriesGoogleSheet();
            List<PointOfDelivery> pods = this.mapToPointOfDeliveries(resultsPod);
            logs.addAll(Repository.getInstance().syncAllPointOfDelivery(pods));
        } catch (Exception e) {
            e.printStackTrace();
            failure = true;
            this.reportSynchronisationError("Synchro des points de livraison" + "\n" + e.getMessage());

        }
        try {
            Log.i(TAG, "Start Produts");
            ValueRange resultsProducts = GoogleApiGateway.getInstance().getProductsGoogleSheet();
            List<Product> products = this.mapToProducts(resultsProducts);
            logs.addAll(Repository.getInstance().syncAllProducts(products));
        } catch (Exception e) {
            e.printStackTrace();
            failure = true;
            this.reportSynchronisationError("Synchro des produits" + "\n" + e.getMessage());

        }

        try {
            Log.i(TAG, "Start Employees");
            ValueRange resultsEmployees = GoogleApiGateway.getInstance().getEmployeeGoogleSheet();
            List<Employee> employees = this.mapToEmployees(resultsEmployees);
            logs.addAll(Repository.getInstance().syncAllEmployee(employees));

        } catch (Exception e) {
            e.printStackTrace();
            failure = true;
            this.reportSynchronisationError("Synchro des employés" + "\n" + e.getMessage());
        }


        if (failure) {
            return Result.failure();
        } else {
            new HandlerToast(getApplicationContext(), R.string.sync_database_worker_success);
            SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(DeliveryApplication.getInstance().getBaseContext()).edit();
            prefs.putString("sync_start_button", getApplicationContext().getString(R.string.sync_database_worker_success));
            prefs.commit();
            return Result.success();
        }


    }

    private void reportSynchronisationError(String message) {
        WorkerUtils.makeStatusNotification(NOTIFICATION_TITLE, message, getApplicationContext());

        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(DeliveryApplication.getInstance().getBaseContext()).edit();
        prefs.putString("sync_start_button", message);
        prefs.commit();
    }


    private List<PointOfDelivery> mapToPointOfDeliveries(ValueRange result) {
        List<List<Object>> resultList = result.getValues();

        this.logCounter = 1;
        List<PointOfDelivery> pods = resultList.stream().skip(1).map(objects -> {
            Long podId = Long.valueOf("" + objects.get(0));
            String name = "" + objects.get(1);
            String address = "" + objects.get(2);
            String mails = "" + objects.get(3);
            BigDecimal discountPercentage = BigDecimal.valueOf(Double.parseDouble("" + objects.get(4)));
            Boolean isActive = ("" + objects.get(5)).equals("Oui") ? true : false;

            PointOfDelivery pod = new PointOfDelivery(podId, name, address, discountPercentage, mails, isActive);

            this.logCounter++;

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
            BigDecimal priceVat = BigDecimal.valueOf(Double.parseDouble("" + objects.get(i++)));
            BigDecimal vat = BigDecimal.valueOf(Double.parseDouble("" + objects.get(i++)));
            Boolean isActive = ("" + objects.get(i++)).equals("Oui") ? true : false;

            Product product = new Product(id, name, type, priceVat, vat, isActive);
            return product;

        }).collect(Collectors.toList());

        return products;
    }

    private List<Company> mapToCompany(ValueRange resultsProducts) {

        try {
            List<List<Object>> resultList = resultsProducts.getValues();
            this.logCounter = 1;
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
                Boolean isActive = true;

                Company company = new Company(id, name, address, phone1, phone2, email, vat, account, isActive);

                this.logCounter++;
                return company;

            }).collect(Collectors.toList());
            return companies;
        } catch (Exception e) {
            throw new RuntimeException("Ligne " + this.logCounter, e);
        }

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

    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

}
