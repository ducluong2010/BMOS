/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DAO;

import Context.DBContext;
import Entity.Bill;
import Entity.BillDetail;
import Entity.Cart;
import Entity.Category;
import Entity.Product;
import Entity.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ngodi
 */
public class billDAO {

    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;

      public void addOrder(User u, float cartTotal, String payment, String address, String date, String phone) throws Exception {
        try {
            String sql = "INSERT INTO bill (user_id, total_money, payment, address, date, phone, delivery_status) VALUES (?, ?, ?, ?, ?, ?, ?)";
            conn = new DBContext().getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, u.getUser_id());
            ps.setFloat(2, cartTotal);
            ps.setString(3, payment);
            ps.setString(4, address);
            ps.setString(5, date);
            ps.setString(6, phone);
            ps.setBoolean(7, false);
            ps.executeUpdate();

            String sql1 = "SELECT TOP 1 bill_id FROM bill ORDER BY bill_id DESC";
            ps = conn.prepareStatement(sql1);
            rs = ps.executeQuery();

            int billId = 0;
            if (rs.next()) {
                billId = rs.getInt("bill_id");

                Cart cart = u.getCart();
                Map<Product, Integer> products = cart.getItems();
                System.out.println(products.entrySet().size());
                for (Map.Entry<Product, Integer> entry : products.entrySet()) {
                    Product product = entry.getKey();
                    int quantity = entry.getValue();
                    String sql2 = "INSERT INTO bill_detail (bill_id, product_id, quantity, product_total, delivery_status) VALUES (?, ?, ?, ?, ?)";
                    ps = conn.prepareStatement(sql2);
                    double productTotal = product.getProduct_price() * quantity;
                    ps.setInt(1, billId);
                    ps.setString(2, product.getProduct_id());
                    ps.setInt(3, quantity);
                    ps.setDouble(4, productTotal);
                    ps.setBoolean(5, false);
                    ps.executeUpdate();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }             
        
        public List<BillDetail> getDetail(int bill_id){
        List<BillDetail> list = new ArrayList<>();
        String sql = "select d.detail_id, p.product_id, p.product_name, p.img, d.quantity, d.price from bill_detail d "
                + "inner join product p on d.product_id = p.product_id where d.bill_id = ?";
        try {
            conn = new DBContext().getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, bill_id);
            rs = ps.executeQuery();
            while (rs.next()) {
                Product p = new Product(rs.getString(2), rs.getString(3), rs.getString(4));
                list.add(new BillDetail(rs.getInt(1), p,rs.getInt(5),rs.getFloat(6)));
            }
        } catch (Exception e) {
        }
        return list;
    }
        
       public List<Bill> getBillsByUserId(int user_id) throws Exception {
        List<Bill> bills = new ArrayList<>();
        try {
            String sql = "SELECT bill_id, date, payment, address, total_money FROM bill WHERE user_id = ?";
            conn = new DBContext().getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, user_id);
            rs = ps.executeQuery();
            while (rs.next()) {          
                int bill_id = rs.getInt("bill_id");
                java.sql.Date date = rs.getDate("date");
                String payment = rs.getString("payment");
                String address = rs.getString("address");
                float totalMoney = rs.getFloat("total_money");  
                Bill bill = new Bill(bill_id, date, payment, address, totalMoney);
                bills.add(bill);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bills;
    }            
}
