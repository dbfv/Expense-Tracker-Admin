package com.example.expensetrackeradmin.models;

public class ExpenseImage {
    private String imageId;
    private String expenseId;
    private String imageUrl;

    public ExpenseImage() {
    }

    public ExpenseImage(String imageId, String expenseId, String imageUrl) {
        this.imageId = imageId;
        this.expenseId = expenseId;
        this.imageUrl = imageUrl;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getExpenseId() {
        return expenseId;
    }

    public void setExpenseId(String expenseId) {
        this.expenseId = expenseId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
