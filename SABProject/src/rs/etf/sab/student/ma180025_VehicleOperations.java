/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.sab.student;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.operations.VehicleOperations;

/**
 *
 * @author pc
 */
public class ma180025_VehicleOperations implements VehicleOperations {

    private Connection conn = DB.getInstance().getConnection();

    @Override
    public boolean insertVehicle(String plateNum, int fuelType, BigDecimal consumption, BigDecimal capacity) {
        try (PreparedStatement ps = conn.prepareStatement("insert into Vozilo(RegBr, TipGoriva, Potrosnja, Nosivost) values (?, ?, ?, ?)")) {
            ps.setString(1, plateNum);
            ps.setInt(2, fuelType);
            ps.setBigDecimal(3, consumption);
            ps.setBigDecimal(4, capacity);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            //Logger.getLogger(ma180025_VehicleOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public int deleteVehicles(String... licencePlates) {
        int deletedVehicles = 0;
        try (PreparedStatement ps = conn.prepareStatement("delete from Vozilo where RegBr=?")) {
            for (String licencePlate : licencePlates) {
                ps.setString(1, licencePlate);
                deletedVehicles += ps.executeUpdate();
            }
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_VehicleOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return deletedVehicles;
    }

    @Override
    public List<String> getAllVehichles() {
        List<String> licencePlates = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("Select RegBr from Vozilo");
                ResultSet rs = ps.executeQuery();) {
            while (rs.next()) {
                licencePlates.add(rs.getString(1));
            }
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_VehicleOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return licencePlates;
    }

    @Override
    public boolean changeFuelType(String licencePlate, int newFuelType) {
        int idV = this.getParkedVehicleId(licencePlate);
        if (idV < 0) {
            return false;
        }
        try (PreparedStatement ps = conn.prepareStatement("Update Vozilo set TipGoriva=? where IdVoz=?")) {
            ps.setInt(1, newFuelType);
            ps.setInt(2, idV);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_VehicleOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public boolean changeConsumption(String licencePlate, BigDecimal newConsumption) {
        int idV = this.getParkedVehicleId(licencePlate);
        if (idV < 0) {
            return false;
        }
        try (PreparedStatement ps = conn.prepareStatement("Update Vozilo set Potrosnja=? where IdVoz=?")) {
            ps.setBigDecimal(1, newConsumption);
            ps.setInt(2, idV);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_VehicleOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public boolean changeCapacity(String licencePlate, BigDecimal newCapacity) {
        int idV = this.getParkedVehicleId(licencePlate);
        if (idV < 0) {
            return false;
        }
        try (PreparedStatement ps = conn.prepareStatement("Update Vozilo set Nosivost=? where IdVoz=?")) {
            ps.setBigDecimal(1, newCapacity);
            ps.setInt(2, idV);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_VehicleOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public boolean parkVehicle(String licencePlate, int idStock) {
        int idV = this.getFreeVehicleId(licencePlate);
        if (idV < 0) {
            return false;
        }
        try (PreparedStatement ps = conn.prepareStatement("insert into Parkirano(IdVoz, IdLok) values (?, ?)")) {
            ps.setInt(1, idV);
            ps.setInt(2, idStock);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_VehicleOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    private int getParkedVehicleId(String licencePlate) {
        try (PreparedStatement ps = conn.prepareStatement("select V.IdVoz "
                + "from Parkirano P join Vozilo V on P.IdVoz=V.IdVoz"
                + " where V.RegBr=?")) {
            ps.setString(1, licencePlate);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_VehicleOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    private int getFreeVehicleId(String licencePlate) {
        try (PreparedStatement ps = conn.prepareStatement("select IdVoz from Vozilo where RegBr=? "
                + "and RegBr not in(select V.RegBr from Vozilo V join Vozi Vo on V.IdVoz=Vo.IdVoz )")) {
            ps.setString(1, licencePlate);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_VehicleOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

}
