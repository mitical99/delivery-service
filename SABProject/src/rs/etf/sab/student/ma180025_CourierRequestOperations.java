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
import rs.etf.sab.operations.CourierRequestOperation;

/**
 *
 * @author pc
 */
public class ma180025_CourierRequestOperations implements CourierRequestOperation {

    private Connection conn = DB.getInstance().getConnection();

    @Override
    public boolean insertCourierRequest(String username, String driverLicenceNumber) {
        int idKor = this.getUserId(username);
        if (idKor < 0) {
            return false;
        }
        try (PreparedStatement ps = conn.prepareStatement("insert into ZahtevKurir(IdKor, BrVozackaDozvola) values (?, ?)")) {
            ps.setInt(1, idKor);
            ps.setString(2, driverLicenceNumber);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_CourierRequestOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public int getUserId(String username) {
        try (PreparedStatement ps = conn.prepareStatement("select IdKor from Korisnik where KorisnickoIme=?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_CourierRequestOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    @Override
    public boolean deleteCourierRequest(String username) {
        int idKor = this.getUserId(username);
        if (idKor < 0) {
            return false;
        }
        try (PreparedStatement ps = conn.prepareStatement("delete from ZahtevKurir where idKor=?")) {
            ps.setInt(1, idKor);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_CourierRequestOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public boolean changeDriverLicenceNumberInCourierRequest(String username, String driverLicenceNumber) {
        int idKor = this.getUserId(username);
        if (idKor < 0) {
            return false;
        }
        try (PreparedStatement ps = conn.prepareStatement("update ZahtevKurir set BrVozackaDozvola=? where IdKor=?")) {
            ps.setString(1, driverLicenceNumber);
            ps.setInt(2, idKor);
            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_CourierRequestOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public List<String> getAllCourierRequests() {
        List<String> usernames = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("select K.KorisnickoIme"
                + " from ZahtevKurir Z join Korisnik K on K.IdKor=Z.IdKor");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                usernames.add(rs.getString(1));
            }
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_CourierRequestOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return usernames;
    }

    @Override
    public boolean grantRequest(String username) {
        try (PreparedStatement ps = conn.prepareStatement("select Z.IdKor, Z.BrVozackaDozvola "
                + "from Korisnik K join ZahtevKurir Z on K.IdKor=Z.IdKor "
                + "where K.KorisnickoIme=?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return this.insertCourier(rs.getInt(1), rs.getString(2));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_CourierRequestOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public boolean insertCourier(int idKor, String driverLicenceNumber) {
        try (PreparedStatement ps = conn.prepareStatement("insert into Kurir(IdKor, BrVozackaDozvola, Profit, Status, BrojIsporuka)"
                + " values(?, ?, ?, ?, ?)")) {
            ps.setInt(1, idKor);
            ps.setString(2, driverLicenceNumber);
            ps.setBigDecimal(3, BigDecimal.ZERO);
            ps.setInt(4, 0);
            ps.setInt(5, 0);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_CourierRequestOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

}
