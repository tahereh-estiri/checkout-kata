package com.easybell.checkout;

import java.util.HashMap;
import java.util.Map;

/**
 * Supermarket checkout.
 *
 * By design, CheckOut knows nothing about which SKUs exist or how they
 * are priced - it just tallies scanned quantities per SKU and asks the
 * supplied {@link PricingRule} for each SKU to compute the total. This
 * keeps pricing rules and checkout logic fully decoupled: new pricing
 * schemes can be added by implementing PricingRule, with no change to
 * this class.
 *
 * Items are identified by a single letter (per the spec: "individual
 * letters of the alphabet, A, B, C, and so on"), so {@code char} is
 * used rather than {@code String} - this rules out invalid multi-letter
 * input at compile time instead of needing runtime validation.
 */
public final class CheckOut {

    private final Map<Character, PricingRule> pricingRules;
    private final Map<Character, Integer> scannedItems = new HashMap<>();

    public CheckOut(Map<Character, PricingRule> pricingRules) {
        this.pricingRules = pricingRules;
    }

    /**
     * Scan a single unit of an item.
     *
     * @throws IllegalArgumentException if the item has no pricing rule
     */
    public void scan(char item) {
        if (!pricingRules.containsKey(item)) {
            throw new IllegalArgumentException("No pricing rule for item: " + item);
        }
        scannedItems.merge(item, 1, Integer::sum);
    }

    /**
     * @return the total price, in cents, of everything scanned so far
     */
    public int total() {
        int total = 0;
        for (Map.Entry<Character, Integer> entry : scannedItems.entrySet()) {
            char item = entry.getKey();
            int quantity = entry.getValue();
            total += pricingRules.get(item).priceFor(quantity);
        }
        return total;
    }
}
