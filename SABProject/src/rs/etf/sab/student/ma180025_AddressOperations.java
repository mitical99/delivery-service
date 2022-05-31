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
import rs.etf.sab.operations.AddressOperations;

/**
 *
 * @author pc
 */
public class ma180025_AddressOperations implements AddressOperations {

    private Connection conn = DB.getInstance().getConnection();

    @Override
    public int insertAddress(String street, int streetNum, int idG, int x, int y) {
        try (PreparedStatement ps = conn.prepareStatement("insert into Adresa(Ulica, Broj, IdGrad, xKord, yKord) values(?, ?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, street);
            ps.setInt(2, streetNum);
            ps.setInt(3, idG);
            ps.setInt(4, x);
            ps.setInt(5, y);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys();) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_AddressOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    @Override
    public int deleteAddresses(String street, int num) {
        try (PreparedStatement ps = conn.prepareStatement("delete from Adresa where Ulica=? and Broj=?")) {
            ps.setString(1, street);
            ps.setInt(2, num);
            return ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_AddressOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }

    @Override
    public boolean deleteAdress(int idAdr) {
        try (PreparedStatement ps = conn.prepareStatement("delete from Adresa where IdAdr=?")) {
            ps.setInt(1, idAdr);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_AddressOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public int deleteAllAddressesFromCity(int idG) {
        try (PreparedStatement ps = conn.prepareStatement("delete from Adresa where IdGrad=?")) {
            ps.setInt(1, idG);
            return ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_AddressOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }

    @Override
    public List<Integer> getAllAddresses() {
        List<Integer> idAdrList = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("Select IdAdr from Adresa")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    idAdrList.add(rs.getInt(1));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_AddressOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return idAdrList;
    }

    @Override
    public List<Integer> getAllAddressesFromCity(int idG) {
        List<Integer> idAdrList = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("select IdAdr from Adresa where IdGrad=?")) {
            ps.setInt(1, idG);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    idAdrList.add(rs.getInt(1));
                }
                if (idAdrList.isEmpty()) {
                    try (PreparedStatement ps1 = conn.prepareStatement("Select count(*) from Grad where IdGrad=?");) {
                        ps1.setInt(1, idG);
                        try (ResultSet rs1 = ps1.executeQuery()) {
                            if (rs1.next()) {
                                if (rs1.getInt(1) != 1) {
                                    return null;
                                }
                            }
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_AddressOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return idAdrList;
    }

}
