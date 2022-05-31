package rs.etf.sab.student;

import rs.etf.sab.operations.*;
import rs.etf.sab.tests.TestHandler;
import rs.etf.sab.tests.TestRunner;

public class StudentMain {

    public static void main(String[] args) {

        AddressOperations addressOperations = new ma180025_AddressOperations(); // Change this to your implementation.
        CityOperations cityOperations = new ma180025_CityOperations(); // Do it for all classes.
        CourierOperations courierOperations = new ma180025_CourierOperations(); // e.g. = new MyDistrictOperations();
        CourierRequestOperation courierRequestOperation = new ma180025_CourierRequestOperations();
        DriveOperation driveOperation = new ma180025_DriveOperations();
        GeneralOperations generalOperations = new ma180025_GeneralOperations();
        PackageOperations packageOperations = new ma180025_PackageOperations();
        StockroomOperations stockroomOperations = new ma180025_StockroomOperations();
        UserOperations userOperations = new ma180025_UserOperations();
        VehicleOperations vehicleOperations = new ma180025_VehicleOperations();

        TestHandler.createInstance(
                addressOperations,
                cityOperations,
                courierOperations,
                courierRequestOperation,
                driveOperation,
                generalOperations,
                packageOperations,
                stockroomOperations,
                userOperations,
                vehicleOperations);

        TestRunner.runTests();
    }
}
