/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.sab.student;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.operations.GeneralOperations;

/**
 *
 * @author pc
 */
public class ma180025_GeneralOperations implements GeneralOperations {

    private Connection conn = DB.getInstance().getConnection();

    @Override
    public void eraseAll() {
        try (CallableStatement cs = conn.prepareCall("{ call eraseAll }");) {
            cs.execute();
        } catch (SQLException ex) {
            Logger.getLogger(ma180025_GeneralOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
