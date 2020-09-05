package com.nfrancoi.delivery.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
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
import com.nfrancoi.delivery.room.entities.DeliveryProductsJoin;
import com.nfrancoi.delivery.room.entities.Employee;
import com.nfrancoi.delivery.room.entities.PointOfDelivery;
import com.nfrancoi.delivery.room.entities.Product;
import com.nfrancoi.delivery.tools.CalendarTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

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
    private LiveData<List<Employee>> employees;
    public LiveData<Employee> employeeByDefault;

    public static synchronized Repository getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Repository(DeliveryApplication.getInstance());
        }
        return INSTANCE;
    }


    // Note that in order to unit test the WordRepository, you have to remove the Application
    // dependency. This adds complexity and much more code, and this sample is not about testing.
    // See the BasicSample in the android-architecture-components repository at
    // https://github.com/googlesamples
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
        employees = employeeDao.getEmployee();
        employeeByDefault = employeeDao.getEmployeeByDefault();

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


    public Single<Long> insert(final Delivery delivery) {
        return deliveryDao.insertReplace(delivery);
    }

    public Single<Long> insertReplace(final Delivery delivery) {
        return deliveryDao.insertReplace(delivery);
    }

    public void update(final Delivery delivery) {
        DeliveryDatabase.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                int i = deliveryDao.updateSync(delivery);
                System.out.println(i);
            }
        });
    }

    public int updateDeliveryNoteSentSync(final Long deliveryId) {
        return deliveryDao.updateDeliveryNoteSent(deliveryId);
    }


    public void saveDeliveryDetailsToGoogleSpreadSheet(long deliveryId) throws IOException {
        Delivery delivery = Repository.getInstance().getDeliverySync(deliveryId);
        List<DeliveryProductsJoin> details = Repository.getInstance().loadNoteDeliveryProductDetailSync(deliveryId);

        //convert data
        List<List<Object>> valuess = new ArrayList<>(details.size());
        String uploadTimeStamp = CalendarTools.YYYYMMDDHHmmss.format(Calendar.getInstance().getTime());
        for (int i = 0; i < details.size(); i++) {
            List<Object> values = new LinkedList<>();
            values.add(uploadTimeStamp);
            values.add(delivery.noteId);
            values.add(CalendarTools.DDMMYYYY.format(delivery.startDate.getTime()));
            values.add(delivery.pointOfDelivery.pointOfDeliveryId);
            values.add(delivery.pointOfDelivery.name);
            values.add(delivery.employee.employeeId);
            values.add(delivery.employee.name);
            values.add(details.get(i).type);
            values.add(details.get(i).productId);
            values.add(details.get(i).productName);
            values.add(details.get(i).quantity);
            values.add(details.get(i).priceUnitVatIncl);
            values.add(details.get(i).vat);
            values.add(details.get(i).priceUnitVatExcl);
            values.add(details.get(i).discount);
            values.add(details.get(i).priceTotVatDiscounted);

            valuess.add(values);
        }


        String notesSheetId = GoogleApiGateway.getInstance().getOrCreateSpreadSheetId("Notes", "DetailDesLivraisons");
        GoogleApiGateway.getInstance().appendToSpreadSheet(notesSheetId, CalendarTools.YYYYMM.format(delivery.startDate.getTime()), valuess);
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

    public void deleteDeliveryProductJoin(Long deliveryId, Long productId, String type) {
        DeliveryDatabase.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                deliveryProductJoinDao.delete(deliveryId, productId, type);
            }
        });

    }


    public LiveData<List<DeliveryProductsJoin>> loadDepositDeliveryProductDetails(Long deliveryId) {
        return deliveryProductJoinDao.loadDeliveryProductDetails(deliveryId, "D");
    }

    public LiveData<List<DeliveryProductsJoin>> loadTakeDeliveryProductDetails(Long deliveryId) {
        return deliveryProductJoinDao.loadDeliveryProductDetails(deliveryId, "T");
    }

    public LiveData<List<DeliveryProductsJoin>> loadAllDeliveryProductDetails(Long deliveryId) {
        return deliveryProductJoinDao.loadNoteDeliveryProductDetail(deliveryId);

    }

    public List<DeliveryProductsJoin> loadNoteDeliveryProductDetailSync(Long deliveryId) {
        return deliveryProductJoinDao.loadNoteDeliveryProductDetailSync(deliveryId);

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
    public LiveData<List<Employee>> getEmployees() {
        return employees;
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
    public void update(Company company) {

        DeliveryDatabase.databaseWriteExecutor.execute(() -> companyDao.updateSync(company));

    }

    @Transaction
    public List<String> syncAllCompanies(List<Company> companies) {
        Synchronizer sync = new Synchronizer<Company>(companyDao);
        sync.syncAll(companies);
        return sync.getLogs();
    }


}