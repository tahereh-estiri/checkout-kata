package com.easybell.checkout;

/**
 * Strategy interface for pricing a given quantity of a single SKU.
 *
 * The checkout never needs to know whether an item is priced per-unit,
 * has a "buy N for Y" special, or follows some future pricing scheme
 * (e.g. weight-based, tiered discounts). It only knows how to ask a
 * PricingRule "what does this many items cost?".
 */
public interface PricingRule {

    /**
     * @param quantity number of units of the SKU that have been scanned
     * @return total price, in cents, for that quantity
     */
    int priceFor(int quantity);
}


