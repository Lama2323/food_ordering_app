package com.example.foodorderingapp.classes;

public class Product {
    private String objectId;
    private String string_id;
    private String description;
    private String image_source;
    private String name;
    private int price;
    private int quantity;

    // Constructor
    public Product() {}

    // Getters and Setters
    public String getObjectId() { return objectId; }
    public void setObjectId(String objectId) { this.objectId = objectId; }

    public String getString_id() { return string_id; }
    public void setString_id(String string_id) { this.string_id = string_id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImage_source() { return image_source; }
    public void setImage_source(String image_source) { this.image_source = image_source; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
