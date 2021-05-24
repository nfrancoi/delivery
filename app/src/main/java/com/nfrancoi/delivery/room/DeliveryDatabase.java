package com.nfrancoi.delivery.room;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.nfrancoi.delivery.DeliveryApplication;
import com.nfrancoi.delivery.room.converter.RoomBigDecimalConverter;
import com.nfrancoi.delivery.room.converter.RoomCalendarConverter;
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

import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {
        Employee.class,
        PointOfDelivery.class,
        Product.class,
        Delivery.class,
        DeliveryProductsJoin.class,
        Company.class},
        version = 73)

@TypeConverters({RoomCalendarConverter.class, RoomBigDecimalConverter.class})
public abstract class DeliveryDatabase extends RoomDatabase {


    public abstract CompanyDao getCompanyDao();

    public abstract ProductDao getProductDao();

    public abstract DeliveryDao getDeliveryDao();

    public abstract PointOfDeliveryDao getPointOfDeliveryDao();

    public abstract DeliveryProductsJoinDao getDeliveryProductJoinDao();

    public abstract EmployeeDao getEmployeeDao();


    private static volatile DeliveryDatabase INSTANCE;

    private static final String DATABASE_NAME = DeliveryApplication.getApplicationExternalStorageDirectoryDocument().getAbsolutePath() + "Delivery.db";

    private static final int NUMBER_OF_THREADS = 4;


    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static DeliveryDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (RoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            DeliveryDatabase.class, DATABASE_NAME).fallbackToDestructiveMigration().addCallback(onOpenCallBack)
                            .build();

                  /* INSTANCE=  Room.databaseBuilder(context.getApplicationContext(),
                            DeliveryDatabase.class, "Delivery").fallbackToDestructiveMigration().addCallback(onOpenCallBack).build();*/
                }
            }
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback onOpenCallBack = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);

            // If you want to keep data through app restarts,
            // comment out the following block
            databaseWriteExecutor.execute(() -> {
                // Populate the database in the background.
                // If you want to start with more words, just add them.
                DeliveryDatabase.initDB();
            });
        }
    };

    private static void initDB() {
        CompanyDao companyDao = INSTANCE.getCompanyDao();
        if (companyDao.countSync() == 0) {
            populateDB();
        }
    }

    private static void populateDB() {

        CompanyDao companyDao = INSTANCE.getCompanyDao();

        Company c = new Company("Euroflor Group SPRL"
                , "Rue de la Station 47 Bte A\n"
                + "4560 Clavier\n"
                , "085/61.25.90"
                , "0495/99.25.12"
                , "euroflor.gille@skynet.be"
                , "BE0645.876.181"
                , "BNP BE14 0017 7659 2483");
        companyDao.insert(c).blockingGet();


        EmployeeDao employeeDao = INSTANCE.getEmployeeDao();
        employeeDao.deleteAll();

        Employee employee1 = new Employee(1l, "JeanPhi", "JP", true);
        employeeDao.insert(employee1).blockingGet();

        Employee employee2 = new Employee(2l, "William", "WL", true);
        employeeDao.insert(employee2).blockingGet();

        Employee employee3 = new Employee(3l, "Nadine", "ND", true);
        employee3.isDefault = true;
        employeeDao.insert(employee3).blockingGet();


        DeliveryProductsJoinDao deliveryProductJoinDao = INSTANCE.getDeliveryProductJoinDao();
        deliveryProductJoinDao.deleteAll();

        DeliveryDao deliveryDao = INSTANCE.getDeliveryDao();
        deliveryDao.deleteAll();

        ProductDao productDao = INSTANCE.getProductDao();
        productDao.deleteAll();

        PointOfDeliveryDao podDao = INSTANCE.getPointOfDeliveryDao();
        podDao.deleteAll();


        Product product = new Product("MonPremierProduit", "Fleur", new BigDecimal(19.18), new BigDecimal(6));
        product.productId = productDao.insert(product).blockingGet();
        Product product2 = new Product("MonSeconfProduit", "Fleur", new BigDecimal(1.0), new BigDecimal(10));
        product2.productId = productDao.insert(product2).blockingGet();
        Product product3 = new Product("Mon3Produit", "Plante", new BigDecimal(1.9), new BigDecimal(21));
        product3.productId = productDao.insert(product3).blockingGet();


        PointOfDelivery pod1 = new PointOfDelivery("Intermarché", "Rue du centre arlon", new BigDecimal(10), "Intermarche@intermarche.com");
        pod1.pointOfDeliveryId = podDao.insert(pod1).blockingGet();
        PointOfDelivery pod2 = new PointOfDelivery("Super Intermarché", "Rue du puis arlon", new BigDecimal(20), "SuperIntermarche@intermarche.com");
        pod2.pointOfDeliveryId = podDao.insert(pod2).blockingGet();
        PointOfDelivery pod3 = new PointOfDelivery("Super carrouf", "Rue du puis Bruxelles", new BigDecimal(30), "SuperCarrouf@carouf.com");
        pod3.pointOfDeliveryId = podDao.insert(pod3).blockingGet();

        /*

        Delivery delivery1 = new Delivery();
        delivery1.pointOfDelivery = pod1;
        delivery1.deliveryId = deliveryDao.insertReplace(delivery1).blockingGet();

        Delivery delivery2 = new Delivery();
        delivery2.pointOfDelivery = pod1;
        delivery2.deliveryId = deliveryDao.insertReplace(delivery2).blockingGet();

        Delivery delivery3 = new Delivery();
        delivery3.pointOfDelivery = pod2;
        delivery3.deliveryId = deliveryDao.insertReplace(delivery3).blockingGet();




        //
        // Delivery future and past
        //
        Calendar deliveryFut1Date = Calendar.getInstance();
        deliveryFut1Date.add(Calendar.DAY_OF_MONTH, 1);
        Delivery deliveryFut1 = new Delivery();
        deliveryFut1.startDate = deliveryFut1Date;
        deliveryFut1.pointOfDelivery = pod1;
        deliveryDao.insertReplace(deliveryFut1).blockingGet();

        Calendar deliveryPast1Date = Calendar.getInstance();
        deliveryPast1Date.add(Calendar.DAY_OF_MONTH, -1);
        Delivery deliveryPast1 = new Delivery();
        deliveryPast1.startDate = deliveryPast1Date;
        deliveryPast1.pointOfDelivery = pod1;
        deliveryDao.insertReplace(deliveryPast1).blockingGet();

        Calendar deliveryPast2Date = Calendar.getInstance();
        deliveryPast2Date.add(Calendar.DAY_OF_MONTH, -1);
        Delivery deliveryPast2 = new Delivery();
        deliveryPast2.startDate = deliveryPast1Date;
        deliveryPast2.pointOfDelivery = pod2;
        deliveryDao.insertReplace(deliveryPast2).blockingGet();

         */
    }

}