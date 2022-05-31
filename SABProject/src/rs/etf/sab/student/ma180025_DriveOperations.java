/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.sab.student;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.operations.DriveOperation;

/**
 *
 * @author pc
 */
public class ma180025_DriveOperations implements DriveOperation {

    private Connection conn = DB.getInstance().getConnection();

    @Override
    public boolean planingDrive(String username) {

        DriveInfo drive = first_phase_delivery(username);
        if (drive == null) {
            return false;
        }
        return this.second_phase_delivery(drive) != null;
    }

    private DriveInfo first_phase_delivery(String username) {

        DriveInfo drive = new DriveInfo();
        int courierID = this.getCourierId(username);
        if (courierID < 0) {
            return null;
        }
        int cityID = this.getCourierCityId(courierID);
        if (cityID < 0) {
            return null;
        }
        drive.setCourierID(courierID);
        drive.setCityID(cityID);

        drive = this.getFreeVehicleId(drive);
        if (drive == null) {
            return null;
        }

        drive = this.startDelivery(drive);

        if (drive == null) {
            return null;
        }

        return drive;
    }

    private int getCourierId(String username) {
        try (PreparedStatement ps = conn.prepareStatement("select Ku.IdKor "
                + "from Kurir Ku join Korisnik K on K.IdKor=Ku.IdKor "
                + "where K.KorisnickoIme=?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_DriveOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    private int getCourierCityId(int idKor) {
        try (PreparedStatement ps = conn.prepareStatement("select A.IdGrad"
                + " from Adresa A join Korisnik K on A.IdAdr=K.IdAdr "
                + "where K.IdKor=?")) {
            ps.setInt(1, idKor);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_DriveOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    private DriveInfo getFreeVehicleId(DriveInfo drive) {
        try (PreparedStatement ps = conn.prepareStatement("select top 1 P.IdVoz, P.IdLok, L.IdAdr "
                + "from Parkirano P join Lokacija_magacin L on L.IdLok=P.IdLok "
                + "join Adresa A on A.IdAdr=L.IdAdr where A.IdGrad=?")) {

            ps.setInt(1, drive.getCityID());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    drive.setStockroomStartID(rs.getInt(2));
                    drive.setStockroomStartAddressID(rs.getInt(3));
                    drive.setVehicleID(rs.getInt(1));
                    return drive;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_DriveOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private DriveInfo startDelivery(DriveInfo drive) {
        try (PreparedStatement ps = conn.prepareStatement("insert into Vozi(IdKor, IdVoz, IdAdr, DuzinaPuta, SlobodanProstor, IdLok, Zarada, BrojIsporuka) "
                + "values(?, ?, ?, 0.000, ?, ?, 0.000, 0)")) {
            ps.setInt(1, drive.getCourierID());
            ps.setInt(2, drive.getVehicleID());
            ps.setInt(3, drive.getStockroomStartAddressID());
            BigDecimal capacity = getCapacity(drive.getVehicleID());
            ps.setBigDecimal(4, capacity);
            ps.setInt(5, drive.getStockroomStartID());
            drive.setCurrentCapacity(capacity);

            if (ps.executeUpdate() > 0) {
                return this.planDrive(drive);
            }
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_DriveOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private BigDecimal getCapacity(int idVeh) {
        try (PreparedStatement ps = conn.prepareStatement("select Nosivost from Vozilo where IdVoz=?")) {
            ps.setInt(1, idVeh);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_DriveOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return BigDecimal.ZERO;
    }

    private DriveInfo planDrive(DriveInfo drive) {
        List<Integer> packages = new ArrayList<>();
        int deliveryNumber = 1;
        try (PreparedStatement ps = conn.prepareStatement("select P.IdPak, P.Tezina, P.IdAdrOd "
                + "from Paket P join Adresa A on P.IdAdrOd=A.IdAdr "
                + "where P.StatusIsporuke=1 and A.IdGrad=? "
                + "order by P.VremePrihvatanjaPonude asc")) {
            ps.setInt(1, drive.getCityID());
            try (ResultSet rs = ps.executeQuery()) {
                BigDecimal cap = drive.getCurrentCapacity();
                boolean vehicleFull = false;
                int idAdrFrom = drive.getStockroomStartAddressID();
                int idAdrTo;
                while (rs.next()) {
                    int idPak = rs.getInt(1);
                    BigDecimal packageWeight = rs.getBigDecimal(2);
                    BigDecimal valueAfterLoad = cap.subtract(packageWeight);
                    if (valueAfterLoad.signum() >= 0) {
                        idAdrTo = rs.getInt(3);
                        cap = valueAfterLoad;
                        if (!insertDriveStage(idPak, deliveryNumber++, 0, drive.getCourierID(), drive.getVehicleID(), 1)) {
                            return null;
                        }
                        packages.add(idPak);
                        idAdrFrom = idAdrTo;
                    } else {
                        vehicleFull = true;
                        break;
                    }
                }
                drive.setCurrentCapacity(cap);
                drive.setRoute(packages);
                drive.setStartAddressID(idAdrFrom);
                drive.setMaxDeliveryNumber(deliveryNumber);

                if (!vehicleFull) {
                    drive = this.loadPackagesFromStockroom(drive, deliveryNumber);
                    if (drive == null) {
                        return null;
                    }
                }

                return drive;
            }
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_DriveOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private DriveInfo loadPackagesFromStockroom(DriveInfo drive, int deliveryNumber) {
        try (PreparedStatement ps = conn.prepareStatement("Select P.IdPak, P.Tezina "
                + "from Paket P join Lokacija_magacin L on P.IdMag=L.IdLok "
                + "join Adresa A on L.IdAdr=A.IdAdr "
                + "where A.IdGrad=? and P.StatusIsporuke=2 "
                + "order by P.VremePrihvatanjaPonude asc")) {
            ps.setInt(1, drive.getCityID());
            try (ResultSet rs = ps.executeQuery()) {
                BigDecimal remainingCapacity = drive.getCurrentCapacity();
                List<Integer> packages = drive.getRoute();
                boolean packageInStockroom = false;
                while (rs.next()) {
                    BigDecimal packageWeight = rs.getBigDecimal(2);
                    BigDecimal valueAfterLoad = remainingCapacity.subtract(packageWeight);
                    if (valueAfterLoad.signum() >= 0) {
                        remainingCapacity = valueAfterLoad;
                        int idPak = rs.getInt(1);

                        if (!insertDriveStage(idPak, deliveryNumber, 0, drive.getCourierID(), drive.getVehicleID(), 1)) {
                            return null;
                        }
                        packageInStockroom = true;
                        packages.add(idPak);

                    } else {
                        break;
                    }
                }
                if (packageInStockroom) {
                    drive.setMaxDeliveryNumber(++deliveryNumber);
                    drive.setRoute(packages);
                    drive.setCurrentCapacity(remainingCapacity);
                    drive.setStartAddressID(drive.getStockroomStartAddressID());
                }
                return drive;
            }
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_DriveOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private boolean insertDriveStage(int idPak, int deliveryNumber, int forStockroom, int courierID, int vehicleID, int forPickup) {
        try (PreparedStatement ps = conn.prepareStatement("insert into Etapa(IdPak, RedosledIsporuke, IdKor, IdVoz, Za_magacin, Za_pokupiti) "
                + "values(?, ?, ?, ?, ?, ?)")) {
            ps.setInt(1, idPak);
            ps.setInt(2, deliveryNumber);
            ps.setInt(3, courierID);
            ps.setInt(4, vehicleID);
            ps.setInt(5, forStockroom);
            ps.setInt(6, forPickup);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_DriveOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    private DriveInfo second_phase_delivery(DriveInfo drive) {
        List<Integer> packagesForDelivery = getPackagesForDelivery(drive.getCourierID());
        if (packagesForDelivery.isEmpty()) {
            return null;
        }
        List<Integer> deliveryRoute = new ArrayList<>();
        int packageCount = packagesForDelivery.size(); //number of packages to be delivered
        Map<Integer, Integer> packageDestinationID = getPackagesAddressDestinationID(); //mapped package id to destination address id
        Map<Integer, BigDecimal> packagesWeight = getPackagesWeight(); //mapped package id to package weight
        BigDecimal vehicleCapacity = drive.getCurrentCapacity(); //current vehicle capacity 

        if (vehicleCapacity == null) {
            return null;
        }

        drive.setPackageIDAddressDestIDPair(packageDestinationID);

        int startAddress = drive.getStartAddressID(); //start address
        int deliveryNumber = drive.getMaxDeliveryNumber();
        for (int i = 0; i <= packageCount; i++) {
            int packageID = findNextNearestPackageDestination(packagesForDelivery, startAddress, packageDestinationID); //next package for delivery
            if (packageID == -1) { // no more packages for delivery
                int leavingCity = isCourierLeavingCity(startAddress, drive.getStockroomStartAddressID());
                List<Integer> packagesForPickup = loadPackagesForStockroom(leavingCity, vehicleCapacity);
                if (!packagesForPickup.isEmpty()) {
                    int lastPackagePickedUpId = -1;
                    for (int packageForPickupID : packagesForPickup) {
                        BigDecimal valueAfterLoad = vehicleCapacity.subtract(packagesWeight.get(packageForPickupID));
                        if (valueAfterLoad.signum() >= 0) {
                            if (!insertDriveStage(packageForPickupID, deliveryNumber++, 1, drive.getCourierID(), drive.getVehicleID(), 1)) {
                                return null;
                            }
                            lastPackagePickedUpId++;
                            vehicleCapacity = valueAfterLoad;
                            deliveryRoute.add(packageForPickupID);
                        }
                    }
                    if (lastPackagePickedUpId >= 0) {
                        startAddress = getAddressFromForPackage(packagesForPickup.get(lastPackagePickedUpId));
                    }
                }
                break;
            }
            int nextStop = packageDestinationID.get(packageID); //destination of selected package
            int leavingCity = isCourierLeavingCity(startAddress, nextStop); //city where courier has delivered last package
            if (leavingCity != -1) { //courier is leaving current city
                List<Integer> packagesForPickup = loadPackagesForStockroom(leavingCity, vehicleCapacity);
                if (!packagesForPickup.isEmpty()) { //wrap in function?
                    int lastPackagePickedUpId = -1;
                    for (int packageForPickupID : packagesForPickup) {
                        BigDecimal valueAfterLoad = vehicleCapacity.subtract(packagesWeight.get(packageForPickupID));
                        if (valueAfterLoad.signum() >= 0) {
                            if (!insertDriveStage(packageForPickupID, deliveryNumber++, 1, drive.getCourierID(), drive.getVehicleID(), 1)) {
                                return null;
                            }
                            lastPackagePickedUpId++;
                            vehicleCapacity = valueAfterLoad;
                            deliveryRoute.add(packageForPickupID);
                        }
                    }
                    if (lastPackagePickedUpId >= 0) {
                        startAddress = getAddressFromForPackage(packagesForPickup.get(lastPackagePickedUpId));
                        i--;
                        continue;
                    }
                }
            }
            if (!insertDriveStage(packageID, deliveryNumber++, 0, drive.getCourierID(), drive.getVehicleID(), 0)) {
                return null;
            }
            deliveryRoute.add(packageID);
            vehicleCapacity = vehicleCapacity.add(packagesWeight.get(packageID));
            startAddress = nextStop;
            packagesForDelivery.remove(Integer.valueOf(packageID));
        }

        drive.setLastVisitedAddress(startAddress);
        List<Integer> route = drive.getRoute();
        route.addAll(deliveryRoute);
        drive.setRoute(route);
        drive.setMaxDeliveryNumber(deliveryNumber);
        drive.setCurrentCapacity(vehicleCapacity);
        return drive;
    }

    private List<Integer> getPackagesForDelivery(int courierID) {
        List<Integer> packages = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("select IdPak "
                + "from Etapa where IdKor=?")) {
            ps.setInt(1, courierID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    packages.add(rs.getInt(1));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_DriveOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return packages;
    }

    private int findNextNearestPackageDestination(List<Integer> packages, int startAddress, Map<Integer, Integer> packageIDAddressDestIDPair) {
        BigDecimal minDistance = null, currDistance;
        int minDistancePackageID = -1;

        for (int packageID : packages) {
            int currDestAdrID = packageIDAddressDestIDPair.get(packageID);
            currDistance = calculateDistance(startAddress, currDestAdrID);
            if (minDistancePackageID == -1) {
                minDistance = currDistance;
                minDistancePackageID = packageID;
                continue;
            }
            if (currDistance.compareTo(minDistance) < 0) {
                minDistance = currDistance;
                minDistancePackageID = packageID;
            }
        }
        return minDistancePackageID;
    }

    private int isCourierLeavingCity(int idAddressFrom, int idAddressTo) {
        try (PreparedStatement ps = conn.prepareStatement("select A1.IdGrad, A2.IdGrad "
                + "from Adresa A1, Adresa A2 where A1.IdAdr=? and A2.IdAdr=?")) {
            ps.setInt(1, idAddressFrom);
            ps.setInt(2, idAddressTo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int cityFromID = rs.getInt(1);
                    int cityToID = rs.getInt(2);
                    if (cityFromID != cityToID) {
                        return cityFromID; //city where is courier leaving from
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_DriveOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1; //if courier isn't leaving
    }

    private List<Integer> loadPackagesForStockroom(int cityID, BigDecimal currVehicleCapacity) {
        List<Integer> packagesID = new ArrayList<>();
        boolean vehicleFull = false;
        try (PreparedStatement ps = conn.prepareStatement("select P.IdPak, P.Tezina "
                + "from Paket P join Adresa AOd on P.IdAdrOd=AOd.IdAdr "
                + "where P.StatusIsporuke=1 and AOd.IdGrad=? "
                + " and P.IdPak not in("
                + "select E.IdPak from Etapa E where E.Za_pokupiti=1)"
                + "order by P.VremePrihvatanjaPonude asc")) {
            ps.setInt(1, cityID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BigDecimal packageWeight = rs.getBigDecimal(2);
                    if (currVehicleCapacity.subtract(packageWeight).signum() >= 0) {
                        packagesID.add(rs.getInt(1));
                        currVehicleCapacity = currVehicleCapacity.subtract(packageWeight);

                    } else {
                        vehicleFull = true;
                        break;
                    }
                }
            }
            if (!vehicleFull) {
                try (PreparedStatement ps1 = conn.prepareStatement("select P.IdPak, P.Tezina "
                        + "from Paket P join Lokacija_magacin M on P.IdMag=M.IdLok "
                        + "join Adresa AMag on AMag.IdAdr=M.IdAdr "
                        + "where AMag.IdGrad=? and P.IdPak not in ("
                        + "select E.IdPak from Etapa E where E.Za_pokupiti=1) "
                        + "order by P.VremePrihvatanjaPonude asc")) {
                    ps1.setInt(1, cityID);
                    try (ResultSet rs = ps1.executeQuery()) {
                        while (rs.next()) {
                            BigDecimal packageWeight = rs.getBigDecimal(2);
                            if (currVehicleCapacity.subtract(packageWeight).signum() >= 0) {
                                packagesID.add(rs.getInt(1));
                                currVehicleCapacity = currVehicleCapacity.subtract(packageWeight);
                            } else {
                                break;
                            }
                        }
                    }
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(ma180025_DriveOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return packagesID;
    }

    private int getAddressFromForPackage(int packageID) {
        try (PreparedStatement ps = conn.prepareStatement("Select IdAdrOd from Paket where IdPak=?")) {
            ps.setInt(1, packageID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_DriveOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    private Map<Integer, Integer> getPackagesAddressDestinationID() {
        Map<Integer, Integer> addresses = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement("select IdPak, IdAdrDo "
                + "from Paket where StatusIsporuke in(1, 2, 3)")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    addresses.put(rs.getInt(1), rs.getInt(2));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_DriveOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return addresses;
    }

    private Map<Integer, BigDecimal> getPackagesWeight() {
        Map<Integer, BigDecimal> packagesWeight = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement("select IdPak, Tezina "
                + "from Paket where StatusIsporuke in (1, 2, 3)")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    packagesWeight.put(rs.getInt(1), rs.getBigDecimal(2));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_DriveOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return packagesWeight;
    }

    private BigDecimal calculateDistance(int IdAdrFrom, int IdAdrTo) {
        try (CallableStatement cs = conn.prepareCall(" {? = call calculateDistance(?, ?) } ")) {
            cs.setInt(2, IdAdrFrom);
            cs.setInt(3, IdAdrTo);
            cs.registerOutParameter(1, java.sql.Types.DECIMAL);

            cs.execute();

            return cs.getBigDecimal(1);

        } catch (SQLException ex) {
            Logger.getLogger(ma180025_DriveOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public int nextStop(String courierUsername) {
        int courierID = getCourierId(courierUsername);
        int vehicleID = getDrivenVehicle(courierID);
        int currentAddress = getCurrentLocation(courierID);
        try (PreparedStatement ps = conn.prepareStatement("select E1.IdPak, E1.Za_pokupiti, E1.Za_magacin "
                + "from Etapa E1 where E1.IdKor=? and E1.RedosledIsporuke=("
                + "select MIN(E2.RedosledIsporuke) from Etapa E2 where E2.IdKor=E1.IdKor)")) {
            ps.setInt(1, courierID);
            boolean updated = false;
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int packageID = rs.getInt(1);
                    boolean forPickup = rs.getInt(2) == 1;
                    int forStockroom = rs.getInt(3);
                    if (!updated) {
                        int nextAddress = getNextAddressInRoute(forPickup, packageID);
                        BigDecimal distance = calculateDistance(currentAddress, nextAddress);
                        if (!updateCurrentAddressAndDistance(nextAddress, distance, courierID)) {
                            return 0;
                        }
                        updated = true;
                    }

                    if (forPickup) {//package for pickup
                        if (!loadPackage(packageID, courierID, vehicleID, forStockroom)) {
                            return 0;
                        }

                    } else {
                        if (!unloadPackage(packageID)) {
                            return 0;
                        }
                        if (!finishDriveStage(courierID)) {
                            return 0;
                        }
                        return packageID;
                    }
                }

                if (!updated) {//last stop

                    int nextAddress = getStartStockroomAddress(courierID);
                    BigDecimal distance = calculateDistance(currentAddress, nextAddress);
                    if (!updateCurrentAddressAndDistance(nextAddress, distance, courierID)) {
                        return 0;
                    }
                    if (!unloadPackagesInStockroom(courierID)) {
                        return 0;
                    }
                    //update all
                    if (!finishDrive(courierID)) {
                        return 0;
                    }
                    return -1;
                }

                if (!finishDriveStage(courierID)) {
                    return 0;
                }
                return -2;
            }
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_DriveOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return 0;
    }

    private int getNextAddressInRoute(boolean forPickup, int packageID) {
        if (!forPickup) {
            try (PreparedStatement ps = conn.prepareStatement("Select IdAdrDo from Paket where IdPak=?")) {
                ps.setInt(1, packageID);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(ma180025_DriveOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try (PreparedStatement ps = conn.prepareStatement("select case "
                    + "when StatusIsporuke=2 then (Select IdAdr from Lokacija_magacin where IdLok=IdMag) "
                    + "when StatusIsporuke=1 then IdAdrOd end "
                    + "from Paket where IdPak=?")) {
                ps.setInt(1, packageID);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }

            } catch (SQLException ex) {
                Logger.getLogger(ma180025_DriveOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return -1;
    }

    private int getCurrentLocation(int courierID) {
        try (PreparedStatement ps = conn.prepareStatement("Select IdAdr from Vozi where IdKor=?")) {
            ps.setInt(1, courierID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_DriveOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    private boolean unloadPackagesInStockroom(int courierID) {
        int stockroomID = -1;
        try (PreparedStatement ps = conn.prepareStatement("Select IdLok from Vozi where IdKor=?")) {
            ps.setInt(1, courierID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stockroomID = rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_DriveOperations.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        try (PreparedStatement ps = conn.prepareStatement("select IdPak from Prevozi_se"
                + " where IdKor=? and Za_magacin=1")) {
            ps.setInt(1, courierID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int packageID = rs.getInt(1);
                    try (PreparedStatement ps1 = conn.prepareStatement("update Paket set IdMag=? where IdPak=?")) {
                        ps1.setInt(2, packageID);
                        ps1.setInt(1, stockroomID);
                        ps1.executeUpdate();
                    }
                    if (!unloadPackage(packageID)) {
                        return false;
                    }
                }
                return true;
            }

        } catch (SQLException ex) {
            Logger.getLogger(ma180025_DriveOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    private boolean finishDriveStage(int courierID) {
        try (PreparedStatement ps = conn.prepareStatement("delete from Etapa "
                + "where IdKor=? and "
                + " RedosledIsporuke=(select MIN(E2.RedosledIsporuke) from Etapa E2 where E2.IdKor=?)")) {
            ps.setInt(1, courierID);
            ps.setInt(2, courierID);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_DriveOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    private boolean updateCurrentAddressAndDistance(int nextAddressID, BigDecimal distance, int courierID) {
        try (PreparedStatement ps = conn.prepareStatement("Update Vozi "
                + "set IdAdr=?, DuzinaPuta=DuzinaPuta+? where IdKor=?")) {
            ps.setInt(1, nextAddressID);
            ps.setBigDecimal(2, distance);
            ps.setInt(3, courierID);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_DriveOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    private int getDrivenVehicle(int courierID) {
        try (PreparedStatement ps = conn.prepareStatement("Select IdVoz from Vozi where IdKor=?")) {
            ps.setInt(1, courierID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_DriveOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    private boolean loadPackage(int packageID, int courierID, int vehicleID, int forStockroom) {
        try (PreparedStatement ps = conn.prepareStatement("insert into Prevozi_se(IdKor, IdVoz, IdPak, Za_magacin) "
                + "values (?, ?, ?, ?)")) {
            ps.setInt(1, courierID);
            ps.setInt(2, vehicleID);
            ps.setInt(3, packageID);
            ps.setInt(4, forStockroom);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_DriveOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    private boolean unloadPackage(int packageID) {
        try (PreparedStatement ps = conn.prepareStatement("delete from Prevozi_se "
                + "where IdPak=?")) {
            ps.setInt(1, packageID);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_DriveOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    private int getStartStockroomAddress(int courierID) {
        try (PreparedStatement ps = conn.prepareStatement("select M.IdAdr "
                + "from Vozi V join Lokacija_magacin M on V.IdLok=M.IdLok "
                + "where V.IdKor=?")) {
            ps.setInt(1, courierID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(ma180025_DriveOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    private boolean finishDrive(int courierID) {
        BigDecimal travelDistance, consumption, income;
        int deliveryCount, fuelType;
        try (PreparedStatement ps = conn.prepareStatement("Select V.DuzinaPuta, Vo.TipGoriva, Vo.Potrosnja, V.BrojIsporuka, V.Zarada"
                + " from Vozi V join Vozilo Vo on V.IdVoz=Vo.IdVoz"
                + " where V.IdKor=?")) {
            ps.setInt(1, courierID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    travelDistance = rs.getBigDecimal(1);
                    fuelType = rs.getInt(2);
                    consumption = rs.getBigDecimal(3);
                    deliveryCount = rs.getInt(4);
                    income = rs.getBigDecimal(5);
                    BigDecimal fuelConsumed = travelDistance.multiply(consumption);
                    double cost;
                    switch (fuelType) {
                        case 0:
                            cost = fuelConsumed.doubleValue() * 15.0;
                            break;
                        case 1:
                            cost = fuelConsumed.doubleValue() * 32.0;
                            break;
                        default:
                            cost = fuelConsumed.doubleValue() * 36.0;
                    }

                    BigDecimal profit = income.subtract(new BigDecimal(cost));

                    try (PreparedStatement ps1 = conn.prepareStatement("Update Kurir "
                            + "set BrojIsporuka=BrojIsporuka+?, Profit=Profit+?, Status=0 "
                            + "where IdKor=?")) {
                        ps1.setInt(1, deliveryCount);
                        ps1.setBigDecimal(2, profit);
                        ps1.setInt(3, courierID);
                        if (ps1.executeUpdate() == 0) {
                            return false;
                        }
                    }
                    try (PreparedStatement ps1 = conn.prepareStatement("Delete from Vozi "
                            + "where IdKor=?")) {
                        ps1.setInt(1, courierID);
                        if (ps1.executeUpdate() == 0) {
                            return false;
                        }
                    }
                    return true;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_DriveOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public List<Integer> getPackagesInVehicle(String username) {
        List<Integer> packagesID = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement("select P.IdPak "
                + "from Prevozi_se P join Korisnik K on P.IdKor=K.IdKor "
                + "where K.KorisnickoIme=?")) {
            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    packagesID.add(rs.getInt(1));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_DriveOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return packagesID;
    }

}
