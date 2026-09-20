package com.easybell.checkout;

/**
 * A single "buy {@code quantity} for {@code price}" special offer tier.
 * An item can have zero, one, or many of these active at once.
 */
public final class SpecialOffer {

    private final int quantity;
    private final int price;

    public SpecialOffer(int quantity, int price) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        this.quantity = quantity;
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getPrice() {
        return price;
    }
}
