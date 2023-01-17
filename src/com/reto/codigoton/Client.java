package com.reto.codigoton;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author felixrojas
 */
public class Client {
    private String code;
    private boolean male;
    private int type;
    private String location;
    private String company;    
    private boolean encrypt;
    private double balance;

    public Client(String code, boolean male, int type, String location, String company, boolean encrypt, double balance) {
        this.code = code;
        this.male = male;
        this.type = type;
        this.location = location;
        this.company = company;
        this.encrypt = encrypt;
        this.balance = balance;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isMale() {
        return male;
    }

    public void setMale(boolean male) {
        this.male = male;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public boolean isEncrypt() {
        return encrypt;
    }

    public void setEncrypt(boolean encrypt) {
        this.encrypt = encrypt;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
           
}
