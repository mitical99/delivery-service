/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.sab.student;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.operations.PackageOperations;

/**
 *
 * @author pc
 */
public class ma180025_PackageOperations implements PackageOperations {

    private Connection conn = DB.getInstance().getConnection();

    @Override
    public int insertPackage(int idAdrFrom, int idAdrTo, String username, int packageType, BigDecimal weight) {
        ma180025_CourierRequestOperations courierHelper = new ma180025_CourierRequestOperations();
        int idKor = courierHelper.getUserId(username);
        if (idKor < 0) {
            return -1;
        }
        try (PreparedStatement ps1 = conn.prepareStatement("insert into Paket(StatusIsporuke, VremeKreiranjaZahtev, TipPaket, Tezina, IdKor, IdAdrOd, IdAdrDo) "
                + "values (0, getdate(), ?, ?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps1.setInt(1, packageType);
            ps1.setBigDecimal(2, weight);
            ps1.setInt(3, idKor);
            ps1.setInt(4, idAdrFrom);
            ps1.setInt(5, idAdrTo);
            ps1.executeUpdate();
            try (ResultSet rs = ps1.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(ma180025_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    @Override
    public boolean acceptAnOffer(int idPak) {
        BigDecimal price = this.getOfferPrice(idPak);
        if (price.equals(BigDecimal.ZERO)) {
            return false;
        }
        try (PreparedStatement ps = conn.prepareStatement("update Paket "
                + "set StatusIsporuke=1, Cena=?, VremePrihvatanjaPonude=getdate()"
                + " where IdPak=? and StatusIsporuke=0")) {
            ps.setBigDecimal(1, price);
            ps.setInt(2, idPak);
            if (ps.executeUpdate() == 1) {
                return this.deleteOffer(idPak);
            }
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    private BigDecimal getOfferPrice(int idPak) {
        try (PreparedStatement ps = conn.prepareStatement("select Cena from Ponuda"
                + " where IdPak=?")) {
            ps.setInt(1, idPak);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return BigDecimal.ZERO;
    }

    private boolean deleteOffer(int idPak) {
        try (PreparedStatement ps = conn.prepareStatement("delete from Ponuda "
                + "where IdPak=?")) {
            ps.setInt(1, idPak);
            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public boolean rejectAnOffer(int idPak) {
        try (PreparedStatement ps = conn.prepareStatement("update Paket "
                + "set StatusIsporuke=4 where IdPak=? and StatusIsporuke=0")) {
            ps.setInt(1, idPak);
            if (ps.executeUpdate() == 1) {
                return this.deleteOffer(idPak);
            }
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public List<Integer> getAllPackages() {
        List<Integer> packageIds = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("select IdPak from Paket");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                packageIds.add(rs.getInt(1));
            }
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return packageIds;
    }

    @Override
    public List<Integer> getAllPackagesWithSpecificType(int type) {
        List<Integer> packageIds = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("select IdPak from Paket "
                + "where TipPaket=?")) {
            ps.setInt(1, type);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    packageIds.add(rs.getInt(1));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return packageIds;
    }

    @Override
    public List<Integer> getAllUndeliveredPackages() {
        List<Integer> packageIds = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("select IdPak from Paket "
                + "where StatusIsporuke in (1, 2)");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                packageIds.add(rs.getInt(1));
            }
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return packageIds;
    }

    @Override
    public List<Integer> getAllUndeliveredPackagesFromCity(int idCity) {
        List<Integer> packageIds = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("select P.IdPak "
                + "from Paket P join Adresa A on P.IdAdrOd=A.IdAdr "
                + "where P.StatusIsporuke in (1, 2) and A.IdGrad=? ")) {
            ps.setInt(1, idCity);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    packageIds.add(rs.getInt(1));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return packageIds;
    }

    @Override
    public List<Integer> getAllPackagesCurrentlyAtCity(int idCity) {
        List<Integer> packageIds = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("select P.IdPak "
                + "from Paket P left join Adresa AOd on P.IdAdrOd=AOd.IdAdr "
                + "left join Lokacija_magacin L on L.IdLok=P.IdMag left join Adresa AMag on AMag.IdAdr=L.IdAdr "
                + "left join Adresa ADo on Ado.IdAdr=P.IdAdrDo "
                + "where (P.StatusIsporuke=1 and AOd.IdGrad=?) or "
                + "(P.StatusIsporuke=2 and AMag.IdGrad=?) or "
                + "(P.StatusIsporuke=3 and ADo.IdGrad=?)")) {
            ps.setInt(1, idCity);
            ps.setInt(2, idCity);
            ps.setInt(3, idCity);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    packageIds.add(rs.getInt(1));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return packageIds;
    }

    @Override
    public boolean deletePackage(int idPak) {
        this.deleteOffer(idPak);
        try (PreparedStatement ps = conn.prepareStatement("delete from Paket "
                + "where IdPak=? and StatusIsporuke in (0, 4)")) {
            ps.setInt(1, idPak);
            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public boolean changeWeight(int idPak, BigDecimal newWeight) {
        try (PreparedStatement ps = conn.prepareStatement("update Paket set Tezina=? where IdPak=? and StatusIsporuke=0")) {
            ps.setBigDecimal(1, newWeight);
            ps.setInt(2, idPak);
            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public boolean changeType(int idPak, int newType) {
        try (PreparedStatement ps = conn.prepareStatement("update Paket set TipPaket=? where IdPak=? and StatusIsporuke=0")) {
            ps.setInt(1, newType);
            ps.setInt(2, idPak);
            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public int getDeliveryStatus(int idPak) {
        try (PreparedStatement ps = conn.prepareStatement("select StatusIsporuke "
                + "from Paket where IdPak=?")) {
            ps.setInt(1, idPak);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    @Override
    public BigDecimal getPriceOfDelivery(int idPak) {
        try (PreparedStatement ps = conn.prepareStatement("select Cena from Paket where IdPak=?")) {
            ps.setInt(1, idPak);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public int getCurrentLocationOfPackage(int idPak) {
        try (PreparedStatement ps = conn.prepareStatement("select case\n"
                + "when StatusIsporuke=1 then P.IdAdrOd\n "
                + "when StatusIsporuke=2 and P.IdMag is not null then (select M.IdAdr from Lokacija_magacin M where M.IdLok=P.IdMag)\n "
                + "when StatusIsporuke=3 then P.IdAdrDo\n "
                + "else -1\n "
                + "end\n "
                + "from Paket P \n"
                + "where P.IdPak=?")) {
            ps.setInt(1, idPak);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int idAdr = rs.getInt(1);
                    if (idAdr > 0) {
                        try (PreparedStatement ps1 = conn.prepareStatement("select IdGrad "
                                + " from Adresa where IdAdr=?")) {
                            ps1.setInt(1, idAdr);
                            try (ResultSet rs1 = ps1.executeQuery()) {
                                if (rs1.next()) {
                                    return rs1.getInt(1);
                                }
                            }
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    @Override
    public Date getAcceptanceTime(int idPak) {
        try (PreparedStatement ps = conn.prepareStatement("Select VremePrihvatanjaPonude from Paket where IdPak=?")) {
            ps.setInt(1, idPak);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDate(1);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_PackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

}
