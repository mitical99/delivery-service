/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.sab.student;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author pc
 */
public class DriveInfo {

    private List<Integer> route = new ArrayList<>();
    private int vehicleID;
    private int courierID;
    private int stockroomStartID;
    private int stockroomStartAddressID;
    private int startAddressID;
    private int cityID;
    private BigDecimal currentCapacity;
    private Map<Integer, Integer> packageIDAddressDestIDPair;
    private int maxDeliveryNumber = 1;
    private int lastVisitedAddress;

    public int getLastVisitedAddress() {
        return lastVisitedAddress;
    }

    public void setLastVisitedAddress(int lastVisitedAddress) {
        this.lastVisitedAddress = lastVisitedAddress;
    }

    public int getMaxDeliveryNumber() {
        return maxDeliveryNumber;
    }

    public void setMaxDeliveryNumber(int maxDeliveryNumber) {
        this.maxDeliveryNumber = maxDeliveryNumber;
    }

    public BigDecimal getCurrentCapacity() {
        return currentCapacity;
    }

    public void setCurrentCapacity(BigDecimal currentCapacity) {
        this.currentCapacity = currentCapacity;
    }

    public List<Integer> getRoute() {
        return route;
    }

    public int getStartAddressID() {
        return startAddressID;
    }

    public void setStartAddressID(int startAddressID) {
        this.startAddressID = startAddressID;
    }

    public void setRoute(List<Integer> route) {
        this.route = route;
    }

    public int getVehicleID() {
        return vehicleID;
    }

    public void setVehicleID(int vehicleID) {
        this.vehicleID = vehicleID;
    }

    public int getCourierID() {
        return courierID;
    }

    public void setCourierID(int courierID) {
        this.courierID = courierID;
    }

    public int getStockroomStartID() {
        return stockroomStartID;
    }

    public void setStockroomStartID(int stockroomStartID) {
        this.stockroomStartID = stockroomStartID;
    }

    public int getStockroomStartAddressID() {
        return stockroomStartAddressID;
    }

    public void setStockroomStartAddressID(int stockroomStartAddressID) {
        this.stockroomStartAddressID = stockroomStartAddressID;
    }

    public int getCityID() {
        return cityID;
    }

    public void setCityID(int cityID) {
        this.cityID = cityID;
    }

    public Map<Integer, Integer> getPackageIDAddressDestIDPair() {
        return packageIDAddressDestIDPair;
    }

    public void setPackageIDAddressDestIDPair(Map<Integer, Integer> packageIDAddressDestIDPair) {
        this.packageIDAddressDestIDPair = packageIDAddressDestIDPair;
    }

}
