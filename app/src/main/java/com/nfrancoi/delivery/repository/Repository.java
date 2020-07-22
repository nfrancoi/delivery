package com.nfrancoi.delivery.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.nfrancoi.delivery.room.DeliveryDatabase;
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

import java.util.Calendar;
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

    public static synchronized Repository getInstance(Application application) {
        if (INSTANCE == null) {
            INSTANCE = new Repository(application);
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
        pods = podDao.getPointOfDeliveries();

        productDao = db.getProductDao();
        products = productDao.getProducts();

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
                int i = deliveryDao.update(delivery);
                System.out.println(i);
            }
        });
    }


    public LiveData<List<Product>> getProducts() {
        return products;
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


    public LiveData<List<DeliveryProductsJoinDao.DeliveryProductDetail>> loadDepositDeliveryProductDetails(Long deliveryId) {
        return deliveryProductJoinDao.loadDeliveryProductDetails(deliveryId, "D");
    }

    public LiveData<List<DeliveryProductsJoinDao.DeliveryProductDetail>> loadTakeDeliveryProductDetails(Long deliveryId) {
        return deliveryProductJoinDao.loadDeliveryProductDetails(deliveryId, "T");
    }

    public LiveData<List<DeliveryProductsJoinDao.DeliveryProductDetail>> loadAllDeliveryProductDetails(Long deliveryId) {
        return deliveryProductJoinDao.loadNoteDeliveryProductDetail(deliveryId);

    }


    //
    // pod
    //
    public LiveData<PointOfDelivery> getPointOfDelivery(Long id) {
        return podDao.getPointOfDelivery(id);

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

    //
    // Company
    //
    public void update(Company company) {

        DeliveryDatabase.databaseWriteExecutor.execute(() -> companyDao.update(company));

    }

}