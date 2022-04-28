package com.nfrancoi.delivery.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;
import androidx.room.Transaction;

import com.nfrancoi.delivery.DeliveryApplication;
import com.nfrancoi.delivery.googleapi.GoogleApiGateway;
import com.nfrancoi.delivery.room.DeliveryDatabase;
import com.nfrancoi.delivery.room.Synchronizer;
import com.nfrancoi.delivery.room.dao.CompanyDao;
import com.nfrancoi.delivery.room.dao.DeliveryDao;
import com.nfrancoi.delivery.room.dao.DeliveryProductsJoinDao;
import com.nfrancoi.delivery.room.dao.EmployeeDao;
import com.nfrancoi.delivery.room.dao.PointOfDeliveryDao;
import com.nfrancoi.delivery.room.dao.ProductDao;
import com.nfrancoi.delivery.room.entities.Company;
import com.nfrancoi.delivery.room.entities.Delivery;
import com.nfrancoi.delivery.room.entities.DeliveryBillingSummary;
import com.nfrancoi.delivery.room.entities.DeliveryProductsJoin;
import com.nfrancoi.delivery.room.entities.Employee;
import com.nfrancoi.delivery.room.entities.PointOfDelivery;
import com.nfrancoi.delivery.room.entities.Product;
import com.nfrancoi.delivery.tools.CalendarTools;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.Single;

public class Repository {

    private static final String TAG = Repository.class.toString();
    private static Repository INSTANCE;

    private CompanyDao companyDao;
    private LiveData<Company> company;

    private DeliveryDao deliveryDao;


    private PointOfDeliveryDao podDao;
    private LiveData<List<PointOfDelivery>> pods;

    private ProductDao productDao;
    private LiveData<List<Product>> products;

    private DeliveryProductsJoinDao deliveryProductJoinDao;


    private EmployeeDao employeeDao;
    private LiveData<List<Employee>> activeEmployees;
    public LiveData<Employee> employeeByDefault;

    public static synchronized Repository getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Repository(DeliveryApplication.getInstance());
        }
        return INSTANCE;
    }


    private Repository(Application application) {
        DeliveryDatabase db = DeliveryDatabase.getDatabase(application);

        companyDao = db.getCompanyDao();
        company = companyDao.getFirstCompany();

        deliveryDao = db.getDeliveryDao();

        podDao = db.getPointOfDeliveryDao();
        pods = podDao.getPointOfDeliveriesActive();

        productDao = db.getProductDao();
        products = productDao.getActiveProducts();

        deliveryProductJoinDao = db.getDeliveryProductJoinDao();

        podDao = db.getPointOfDeliveryDao();

        employeeDao = db.getEmployeeDao();
        activeEmployees = employeeDao.getActiveEmployee();

        String defaultEmployeeName = PreferenceManager.getDefaultSharedPreferences(DeliveryApplication.getInstance().getApplicationContext()).getString("employee_default", null);


        employeeByDefault = employeeDao.getEmployeeByName(defaultEmployeeName);

    }

    //
    // Company
    //

    public LiveData<Company> getFirstCompany() {
        return company;

    }


    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    public LiveData<List<PointOfDelivery>> getPointOfDeliveries() {
        return pods;
    }


    //
    // Delivery
    //
    public LiveData<List<Delivery>> getDeliveriesByDay(Calendar calendarDay) {
        Calendar begin = CalendarTools.roundByDay(calendarDay);
        Calendar end = (Calendar) begin.clone();
        end.add(Calendar.DAY_OF_MONTH, 1);
        Log.d(TAG, "getDeliveriesByDay begin=" + CalendarTools.YYYYMMDD.format(begin.getTime()) +
                "end=" + CalendarTools.YYYYMMDD.format(end.getTime()));

        return deliveryDao.getDeliveriesByDates(begin, end);
    }

    public LiveData<Delivery> getDelivery(Long deliveryId) {
        return deliveryDao.getDelivery(deliveryId);
    }

    public Delivery getDeliverySync(Long deliveryId) {
        return deliveryDao.getDeliverySync(deliveryId);
    }

    public Delivery getDeliveryFromNoteFileNameSync(String noteName) {
        return deliveryDao.getDeliveryFromNoteFileNameSync(noteName);
    }

    public Single<Long> insertReplace(final Delivery delivery) {
        return deliveryDao.insertReplace(delivery);
    }


    public void updateSync(final Delivery delivery) {
        int i = deliveryDao.updateSync(delivery);
    }

    public LiveData<Delivery> update(final Delivery delivery) {
        MutableLiveData<Delivery> deliveryLiveData = new MutableLiveData<>();
        DeliveryDatabase.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                int i = deliveryDao.updateSync(delivery);
                System.out.println(i);
                deliveryLiveData.postValue(delivery);
            }
        });
        return deliveryLiveData;
    }

    public int updateDeliveryNoteSentSync(final Long deliveryId) {
        return deliveryDao.updateDeliveryNoteSent(deliveryId);
    }


    //
    // Sync
    //

    public boolean saveDeliveryNoteToGoogleDrive(Delivery delivery) {


        File file = new File(delivery.noteURI);
        if (!file.exists()) {
            delivery.isNoteSaved = false;
            delivery.syncErrorMessage = delivery.noteURI + " n'existe pas";
            Repository.getInstance().updateSync(delivery);
            return false;
        }

        //save pdf file
        try {//create the directory Delivery
            String rootDirectoryId = GoogleApiGateway.getInstance().createDirectory("DeliveryWorkspace", null);
            String pointOfDeliveryDirectoryId = GoogleApiGateway.getInstance().createDirectory(delivery.pointOfDelivery.name, rootDirectoryId);
            String pointOfDeliveryDateDirectoryId = GoogleApiGateway.getInstance().createDirectory(CalendarTools.YYYYMM.format(delivery.startDate.getTime()), pointOfDeliveryDirectoryId);


            // save PDF file
            String existingNoteFileNameId = GoogleApiGateway.getInstance().getFileIdByNameOnGoogleDrive(file.getName());
            if (existingNoteFileNameId != null) {
                GoogleApiGateway.getInstance().deleteFileByIdOnGoogleDrive(existingNoteFileNameId);
            }
            GoogleApiGateway.getInstance().savePdfFileOnGoogleDrive(file, pointOfDeliveryDateDirectoryId);
        } catch (Exception e) {
            delivery.isNoteSaved = false;
            delivery.syncErrorMessage = e.getMessage();
            Repository.getInstance().updateSync(delivery);
            return false;
        }

        delivery.isNoteSaved = true;
        Repository.getInstance().updateSync(delivery);
        return true;
    }


    public boolean isDeliveryDetailsToGoogleSpreadSheet(Delivery delivery) throws IOException {


        String rootDirectoryId = GoogleApiGateway.getInstance().createDirectory("DeliveryWorkspace", null);
        String billingDirectoryId = GoogleApiGateway.getInstance().createDirectory("_Facturation", rootDirectoryId);

        String spreadSheetId = GoogleApiGateway.getInstance().getOrCreateSpreadSheetIdInFolder(billingDirectoryId, "FacturationNotes");
        String sheetName = CalendarTools.YYYYMM.format(delivery.startDate.getTime());
        //
        // Check if data exists
        //
        List<Integer> rowsIds;
        try {
            rowsIds = GoogleApiGateway.getInstance().findLinesInSpreadsheet(spreadSheetId, sheetName, delivery.noteId, "E");
        }catch  (IOException e){
            e.printStackTrace();
            return false;
        }
        if (rowsIds != null && rowsIds.size() > 0) return true;

        return false;


    }

    public boolean saveDeliveryDetailsToGoogleSpreadSheet(Delivery delivery) {
        List<DeliveryBillingSummary> details = Repository.getInstance().loadDeliveryBillingSummarySync(delivery.deliveryId);

        //
        //convert data
        //
        List<List<Object>> valuess = new ArrayList<>(details.size());
        String uploadTimeStamp = CalendarTools.YYYYMMDDHHmmss.format(Calendar.getInstance().getTime());
        for (int i = 0; i < details.size(); i++) {
            DeliveryBillingSummary billingSummary = details.get(i);

            List<Object> values = new LinkedList<>();
            values.add(uploadTimeStamp);
            values.add(billingSummary.pointOfDeliveryId);
            values.add(billingSummary.pointOfDeliveryName);
            values.add(CalendarTools.YYYYMMDD.format(billingSummary.deliveryDate.getTime()));
            values.add(billingSummary.noteId);

            if (billingSummary.vat != null) {
                values.add(billingSummary.vat.setScale(0, RoundingMode.HALF_UP));
            } else {
                values.add(0);
            }

            if (billingSummary.priceTotVatExclDiscounted != null) {
                values.add(billingSummary.priceTotVatExclDiscounted.setScale(2, RoundingMode.HALF_UP));
            } else {
                values.add(0);
            }

            if (billingSummary.priceTotVatInclDiscounted != null) {
                values.add(billingSummary.priceTotVatInclDiscounted.setScale(2, RoundingMode.HALF_UP));
            } else {
                values.add(0);
            }

            valuess.add(values);
        }

        try {
            String rootDirectoryId = GoogleApiGateway.getInstance().createDirectory("DeliveryWorkspace", null);
            String billingDirectoryId = GoogleApiGateway.getInstance().createDirectory("_Facturation", rootDirectoryId);

            String spreadSheetId = GoogleApiGateway.getInstance().getOrCreateSpreadSheetIdInFolder(billingDirectoryId, "FacturationNotes");
            String sheetName = CalendarTools.YYYYMM.format(delivery.startDate.getTime());

            //
            // Check if data exists
            //

            //check the spreadSheet and sheet
            if (!GoogleApiGateway.getInstance().isSheetExistInSpreadSheet(spreadSheetId, sheetName)) {
                GoogleApiGateway.getInstance().addSheetToSpreadSheet(spreadSheetId, sheetName);

            }

            List<Integer> rowsIds = GoogleApiGateway.getInstance().findLinesInSpreadsheet(spreadSheetId, sheetName, delivery.noteId, "E");
            if (rowsIds.size() > 0)
                GoogleApiGateway.getInstance().deleteLinesInSpreadsheet(spreadSheetId, sheetName, rowsIds);


            int linesUpdated = GoogleApiGateway.getInstance().appendToSpreadSheet(spreadSheetId, CalendarTools.YYYYMM.format(delivery.startDate.getTime()), valuess);

            if (linesUpdated != details.size()) {
                delivery.isAccountingDataSent = false;
                delivery.syncErrorMessage = "Nombre de lignes de facturations uploaded = " + linesUpdated + "sur un total de " + details.size();
                Repository.getInstance().updateSync(delivery);
                return false;
            } else {
                delivery.isAccountingDataSent = true;
                Repository.getInstance().updateSync(delivery);
                return true;
            }
        } catch (Exception e) {
            delivery.isAccountingDataSent = false;
            delivery.syncErrorMessage = e.getMessage();
            Repository.getInstance().updateSync(delivery);
            return false;
        }
    }


    //
    // Products
    //


    public LiveData<List<Product>> getProducts() {
        return products;
    }

    @Transaction
    public List<String> syncAllProducts(List<Product> products) {
        Synchronizer sync = new Synchronizer<Product>(productDao);
        sync.syncAll(products);
        return sync.getLogs();
    }

    //
    // deliveryProductsJoin
    //
    public void insertReplace(final DeliveryProductsJoin deliveryProductsJoin) {
        DeliveryDatabase.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                deliveryProductJoinDao.insertReplace(deliveryProductsJoin);
            }
        });
    }

    public void deleteDeliveryProductJoin(DeliveryProductsJoin deliveryProductsJoin) {
        DeliveryDatabase.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                deliveryProductJoinDao.delete(deliveryProductsJoin);
            }
        });

    }


    public LiveData<DeliveryProductsJoin> getDeliveryProductsJoin(Long deliveryProductsJoin) {
        return deliveryProductJoinDao.getDeliveryProductsJoin(deliveryProductsJoin);
    }

    public LiveData<List<DeliveryProductsJoin>> loadDepositDeliveryProducts(Long deliveryId) {
        return deliveryProductJoinDao.loadDeliveryProducts(deliveryId, "D");
    }

    public LiveData<List<DeliveryProductsJoin>> loadTakeDeliveryProducts(Long deliveryId) {
        return deliveryProductJoinDao.loadDeliveryProducts(deliveryId, "T");
    }

    public LiveData<List<DeliveryProductsJoin>> loadSellDeliveryProducts(Long deliveryId) {
        return deliveryProductJoinDao.loadSellDeliveryProducts(deliveryId);
    }


    public List<DeliveryProductsJoin> loadNoteDeliveryProductDetailSync(Long deliveryId) {
        return deliveryProductJoinDao.loadNoteDeliveryProductDetailSync(deliveryId);

    }

    public List<DeliveryBillingSummary> loadDeliveryBillingSummarySync(Long deliveryId) {
        return deliveryDao.loadDeliveryBillingSummarySync(deliveryId);

    }


    //
    // pod
    //
    public LiveData<PointOfDelivery> getPointOfDelivery(Long id) {
        return podDao.getPointOfDelivery(id);

    }

    @Transaction
    public List<String> syncAllPointOfDelivery(List<PointOfDelivery> pointOfDeliveries) {
        Synchronizer sync = new Synchronizer<PointOfDelivery>(podDao);
        sync.syncAll(pointOfDeliveries);
        return sync.getLogs();
    }


    //
    // Employees
    //
    public LiveData<List<Employee>> getActiveEmployees() {
        return activeEmployees;
    }

    public LiveData<Employee> getEmployeeByDefault() {
        return employeeByDefault;
    }

    public List<String> syncAllEmployee(List<Employee> employees) {
        Synchronizer sync = new Synchronizer<Employee>(employeeDao);
        sync.syncAll(employees);
        return sync.getLogs();

    }

    //
    // Company
    //
    public void updateSync(Company company) {

        DeliveryDatabase.databaseWriteExecutor.execute(() -> companyDao.updateSync(company));

    }

    @Transaction
    public List<String> syncAllCompanies(List<Company> companies) {
        Synchronizer sync = new Synchronizer<Company>(companyDao);
        sync.syncAll(companies);
        return sync.getLogs();
    }


    @Transaction
    public LiveData<List<DeliveryProductsJoin>> calculateDeliveryProductsJoinsPrices(Delivery delivery) {
        MutableLiveData<List<DeliveryProductsJoin>> updateReturn = new MutableLiveData<>();

        DeliveryDatabase.databaseWriteExecutor.execute(() -> {


            List<DeliveryProductsJoin> deliveryProductsJoinsUpdated =
                    loadNoteDeliveryProductDetailSync(delivery.deliveryId).stream().map(deliveryProductDetailToUpdate -> {

                        BigDecimal priceUnitVatIncl = deliveryProductDetailToUpdate.priceUnitVatIncl.setScale(3, RoundingMode.HALF_UP);

                        BigDecimal vatDivider = deliveryProductDetailToUpdate.vat.equals(BigDecimal.ZERO) ? BigDecimal.ONE : BigDecimal.ONE.add(deliveryProductDetailToUpdate.vat.divide(BigDecimal.valueOf(100l)));
                        BigDecimal priceUnitVatExcl = priceUnitVatIncl.divide(vatDivider, 3, RoundingMode.HALF_UP);
                        deliveryProductDetailToUpdate.priceUnitVatExcl = priceUnitVatExcl.setScale(2, RoundingMode.HALF_UP);

                        BigDecimal discountMultiplicator = BigDecimal.ONE.add(deliveryProductDetailToUpdate.discount.negate().divide(BigDecimal.valueOf(100l), 3, RoundingMode.HALF_UP));
                        BigDecimal priceUnitVatExclDiscounted = priceUnitVatExcl.multiply(discountMultiplicator);

                        BigDecimal priceTotVatExclDiscounted = priceUnitVatExclDiscounted.multiply(new BigDecimal(deliveryProductDetailToUpdate.quantity));
                        deliveryProductDetailToUpdate.priceTotVatExclDiscounted = priceTotVatExclDiscounted.setScale(2, RoundingMode.HALF_UP);
                        ;


                        if (delivery.isVatApplicable) {
                            deliveryProductDetailToUpdate.vatApplicable = deliveryProductDetailToUpdate.vat;
                        } else {
                            deliveryProductDetailToUpdate.vatApplicable = BigDecimal.ZERO;
                        }

                        BigDecimal priceTotVatInclDiscounted = priceTotVatExclDiscounted.multiply(BigDecimal.ONE.add(deliveryProductDetailToUpdate.vatApplicable.divide(new BigDecimal(100l)))).setScale(3, RoundingMode.HALF_UP);
                        ;
                        deliveryProductDetailToUpdate.priceTotVatInclDiscounted = priceTotVatInclDiscounted.setScale(2, RoundingMode.HALF_UP);
                        ;


                        deliveryProductDetailToUpdate.priceTotVatExclDiscounted = priceTotVatExclDiscounted.setScale(2, RoundingMode.HALF_UP);

                        return deliveryProductDetailToUpdate;

                    }).collect(Collectors.toList());

            int nbrUpdated = deliveryProductJoinDao.update(deliveryProductsJoinsUpdated);
            updateReturn.postValue(deliveryProductsJoinsUpdated);
        });


        return updateReturn;


    }

    //
    // Reset database
    //
    public void resetDatabaseSync() {
        deliveryProductJoinDao.deleteAll();
        deliveryDao.deleteAll();
        employeeDao.deleteAll();
        podDao.deleteAll();
        productDao.deleteAll();
        companyDao.deleteAll();
    }

}