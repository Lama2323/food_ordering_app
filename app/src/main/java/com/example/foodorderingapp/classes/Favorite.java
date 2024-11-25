package com.example.foodorderingapp.classes;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;

public class Favorite {
    private String objectId;
    private String customer_id;
    private String product_id;
    private String created;
    private String updated;


    public Favorite(){

    }
    public Favorite(String customerId, String productId) {
        this.customer_id = customerId;
        this.product_id = productId;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(String customer_id) {
        this.customer_id = customer_id;
    }

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }


    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }


}