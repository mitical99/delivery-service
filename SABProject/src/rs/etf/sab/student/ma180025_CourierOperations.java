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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.operations.CourierOperations;

/**
 *
 * @author pc
 */
public class ma180025_CourierOperations implements CourierOperations {

    private Connection conn = DB.getInstance().getConnection();

    @Override
    public boolean insertCourier(String username, String driverLicenceNumber) {
        ma180025_CourierRequestOperations courierHelper = new ma180025_CourierRequestOperations();
        int idKor = courierHelper.getUserId(username);
        if (idKor < 0) {
            return false;
        }
        return courierHelper.insertCourier(idKor, driverLicenceNumber);
    }

    @Override
    public boolean deleteCourier(String username) {
        try (PreparedStatement ps = conn.prepareStatement("delete from Kurir where IdKor="
                + "(select IdKor from Korisnik where KorisnickoIme=?)")) {
            ps.setString(1, username);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_CourierOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public List<String> getCouriersWithStatus(int statusOfCourier) {
        List<String> usernames = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("select Ko.KorisnickoIme "
                + "from Kurir K join Korisnik Ko on K.IdKor=Ko.IdKor "
                + "where K.Status = ?")) {
            ps.setInt(1, statusOfCourier);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    usernames.add(rs.getString(1));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_CourierOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return usernames;
    }

    @Override
    public List<String> getAllCouriers() {
        List<String> usernames = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("select Ko.KorisnickoIme "
                + "from Kurir K join Korisnik Ko on Ko.IdKor=K.IdKor "
                + "order by K.Profit desc");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                usernames.add(rs.getString(1));
            }
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_CourierOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return usernames;
    }

    @Override
    public BigDecimal getAverageCourierProfit(int numberOfDeliveries) {
        try (CallableStatement cs = conn.prepareCall("{ ? = call getAverageCourierProfit(?) }")) {
            cs.registerOutParameter(1, java.sql.Types.DECIMAL);
            cs.setInt(2, numberOfDeliveries);
            cs.execute();
            return cs.getBigDecimal(1);
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_CourierOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return BigDecimal.ZERO;
    }

}
