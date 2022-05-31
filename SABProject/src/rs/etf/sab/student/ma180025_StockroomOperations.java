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
import rs.etf.sab.operations.StockroomOperations;

/**
 *
 * @author pc
 */
public class ma180025_StockroomOperations implements StockroomOperations {

    private Connection conn = DB.getInstance().getConnection();

    @Override
    public int insertStockroom(int idAdr) {
        try (PreparedStatement ps = conn.prepareStatement("Insert into Lokacija_magacin(IdAdr) values(?)", PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idAdr);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(ma180025_StockroomOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    @Override
    public boolean deleteStockroom(int idStock) {
        if (!isStockroomEmpty(idStock)) {
            return false;
        }
        try (PreparedStatement ps = conn.prepareStatement("delete from Lokacija_magacin where IdLok=?")) {
            ps.setInt(1, idStock);
            return ps.executeUpdate() > 0;

        } catch (SQLException ex) {
            Logger.getLogger(ma180025_StockroomOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    private boolean isStockroomEmpty(int idStock) {
        return this.isStockroomPackagesEmpty(idStock) && this.isStockroomVehiclesEmpty(idStock);
    }

    private boolean isStockroomVehiclesEmpty(int idStock) {
        try (PreparedStatement ps1 = conn.prepareStatement("select count(*) from Parkirano where idLok=?")) {
            ps1.setInt(1, idStock);
            try (ResultSet rs1 = ps1.executeQuery()) {
                if (rs1.next()) {
                    if (rs1.getInt(1) > 0) {
                        return false;
                    }
                    return true;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_StockroomOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    private boolean isStockroomPackagesEmpty(int idStock) {
        try (PreparedStatement ps2 = conn.prepareStatement("select count(*) "
                + "from Paket "
                + "where IdMag=?")) {
            ps2.setInt(1, idStock);
            try (ResultSet rs2 = ps2.executeQuery()) {
                if (rs2.next()) {
                    if (rs2.getInt(1) > 0) {
                        return false;
                    }
                    return true;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_StockroomOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public int deleteStockroomFromCity(int idG) {
        try (PreparedStatement ps = conn.prepareStatement("select L.IdLok "
                + "from Lokacija_magacin L join Adresa A on L.IdAdr=A.IdAdr "
                + "where A.IdGrad=?")) {
            ps.setInt(1, idG);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int idLok = rs.getInt(1);
                    if (this.deleteStockroom(idLok)) {
                        return idLok;
                    }
                }
                return -1;
            }

        } catch (SQLException ex) {
            Logger.getLogger(ma180025_StockroomOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    @Override
    public List<Integer> getAllStockrooms() {
        List<Integer> idLokList = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("select IdLok from Lokacija_magacin");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                idLokList.add(rs.getInt(1));
            }
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_StockroomOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return idLokList;
    }
}
