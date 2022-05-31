/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.sab.student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.operations.CityOperations;

/**
 *
 * @author pc
 */
public class ma180025_CityOperations implements CityOperations {

    private Connection conn = DB.getInstance().getConnection();

    @Override
    public int insertCity(String name, String postalCode) {
        try (PreparedStatement ps = conn.prepareStatement("insert into Grad(Naziv,PostanskiBr) values(?, ?)")) {
            ps.setString(1, name);
            ps.setString(2, postalCode);
            if (ps.executeUpdate() > 0) {
                try (PreparedStatement ps1 = conn.prepareStatement("Select IdGrad from Grad where Naziv=? and PostanskiBr=?");) {
                    ps1.setString(1, name);
                    ps1.setString(2, postalCode);
                    try (ResultSet rs = ps1.executeQuery()) {
                        if (rs.next()) {
                            return rs.getInt(1);
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    @Override
    public int deleteCity(String... cities) {
        int deletedCities = 0;
        for (String city : cities) {
            try (PreparedStatement ps = conn.prepareStatement("Delete from Grad where Naziv=?")) {
                ps.setString(1, city);
                deletedCities += ps.executeUpdate();
            } catch (SQLException ex) {
                Logger.getLogger(ma180025_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return deletedCities;
    }

    @Override
    public boolean deleteCity(int i) {
        try (PreparedStatement ps = conn.prepareStatement("delete from Grad where IdGrad=?")) {
            ps.setInt(1, i);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public List<Integer> getAllCities() {
        List<Integer> idCityList = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("Select IdGrad from Grad")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    idCityList.add(rs.getInt(1));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return idCityList;
    }

}
