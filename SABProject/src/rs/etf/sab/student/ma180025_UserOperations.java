/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.sab.student;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import rs.etf.sab.operations.UserOperations;

/**
 *
 * @author pc
 */
public class ma180025_UserOperations implements UserOperations {

    private Connection conn = DB.getInstance().getConnection();

    @Override
    public boolean insertUser(String username, String firstname, String lastname, String password, int idAdr) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.

        try (PreparedStatement ps = conn.prepareStatement("insert into Korisnik(Ime, Prezime, KorisnickoIme, Sifra, IdAdr) values (?, ?, ?, ?, ?)")) {
            Pattern passwordPattern = Pattern.compile("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}$");
            Matcher matcher = passwordPattern.matcher(password);
            if (!matcher.matches() || Character.isLowerCase(firstname.charAt(0)) || Character.isLowerCase(lastname.charAt(0))) {
                System.out.println("Incorrect input data for User!");
                return false;
            }
            ps.setString(1, firstname);
            ps.setString(2, lastname);
            ps.setString(3, username);
            ps.setString(4, password);
            ps.setInt(5, idAdr);
            ps.execute();
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_UserOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public boolean declareAdmin(String username) {
        try (PreparedStatement ps_insert = conn.prepareStatement("Insert into Administrator(IdKor) values(?)");
                PreparedStatement ps_select = conn.prepareStatement("Select IdKor from Korisnik where KorisnickoIme=?");) {
            ps_select.setString(1, username);
            try (ResultSet rs = ps_select.executeQuery()) {
                if (rs.next()) {
                    ps_insert.setInt(1, rs.getInt(1));
                    return ps_insert.executeUpdate() > 0;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_UserOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public int getSentPackages(String... usernames) {
        int sentPackages = 0;
        boolean userFound = false;
        try (CallableStatement cs = conn.prepareCall("{ ? = call getNumberOfSentPackagesForUser (?)}");) {
            for (String username : usernames) {
                if (!userFound) {
                    try (PreparedStatement ps = conn.prepareStatement("select count(*) from Korisnik where KorisnickoIme=?")) {
                        ps.setString(1, username);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                if (rs.getInt(1) == 0) {
                                    continue;
                                }
                                userFound = true;
                            }
                        }
                    }
                }
                cs.setString(2, username);
                cs.registerOutParameter(1, java.sql.Types.INTEGER);
                cs.execute();

                sentPackages += cs.getInt(1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_UserOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (!userFound) {
            return -1;
        } else {
            return sentPackages;
        }
    }

    @Override
    public int deleteUsers(String... strings) {
        int deletedUsers = 0;
        try {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Korisnik where KorisnickoIme=?")) {
                for (String s : strings) {
                    ps.setString(1, s);
                    deletedUsers += ps.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            try {
                conn.rollback();
            } catch (SQLException ex1) {
                Logger.getLogger(ma180025_UserOperations.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(ma180025_UserOperations.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                Logger.getLogger(ma180025_UserOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return deletedUsers;
    }

    @Override
    public List<String> getAllUsers() {
        List<String> userList = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT KorisnickoIme from Korisnik")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    userList.add(rs.getString(1));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_UserOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return userList;
    }

}
