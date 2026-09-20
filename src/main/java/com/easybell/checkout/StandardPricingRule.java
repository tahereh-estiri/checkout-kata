package com.easybell.checkout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * General-purpose pricing rule for a single item.
 *
 * Every item has a unit price, and optionally zero, one, or several
 * {@link SpecialOffer} tiers active at the same time (e.g. "3 for 130"
 * AND "5 for 200" both available for the same SKU). Some earlier,
 * simpler designs assumed every item had at most one special - that
 * does not hold in general, so this rule handles the fully general
 * case instead.
 *
 * Pricing is computed by dynamic programming: for a given quantity we
 * find the minimum-cost way to cover it using any combination of unit
 * purchases and special-offer bundles. A naive greedy approach (always
 * apply the largest special first) is NOT always optimal once multiple
 * overlapping specials exist - e.g. with specials "5 for 500" and
 * "3 for 250" and unit price 100, greedily taking one "5 for 500" for
 * a quantity of 6 gives 500 + 100 = 600, but two "3 for 250" bundles
 * give 500, which is cheaper. The DP always finds the true minimum.
 */
public final class StandardPricingRule implements PricingRule {

    private final int unitPrice;
    private final List<SpecialOffer> specialOffers;

    public StandardPricingRule(int unitPrice) {
        this(unitPrice, List.of());
    }

    public StandardPricingRule(int unitPrice, List<SpecialOffer> specialOffers) {
        this.unitPrice = unitPrice;
        this.specialOffers = List.copyOf(specialOffers);
    }

    @Override
    public int priceFor(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("quantity must not be negative");
        }

        // minCost[q] = cheapest way to price exactly q units of this item
        int[] minCost = new int[quantity + 1];
        Arrays.fill(minCost, Integer.MAX_VALUE);
        minCost[0] = 0;

        for (int q = 1; q <= quantity; q++) {
            // Option 1: price one unit at the base unit price, plus the
            // best price already found for the rest.
            int best = minCost[q - 1] + unitPrice;

            // Option 2: apply any special offer that fits within q, plus
            // the best price already found for the remainder.
            for (SpecialOffer offer : specialOffers) {
                if (offer.getQuantity() <= q) {
                    int candidate = minCost[q - offer.getQuantity()] + offer.getPrice();
                    if (candidate < best) {
                        best = candidate;
                    }
                }
            }

            minCost[q] = best;
        }

        return minCost[quantity];
    }

    /** Convenience factory for an item with no special offers at all. */
    public static StandardPricingRule unitOnly(int unitPrice) {
        return new StandardPricingRule(unitPrice);
    }

    /** Convenience factory for an item with exactly one special offer. */
    public static StandardPricingRule withOffer(int unitPrice, int specialQuantity, int specialPrice) {
        return new StandardPricingRule(unitPrice, List.of(new SpecialOffer(specialQuantity, specialPrice)));
    }

    /** Convenience factory for an item with several simultaneous special offers. */
    public static StandardPricingRule withOffers(int unitPrice, SpecialOffer... offers) {
        return new StandardPricingRule(unitPrice, new ArrayList<>(Arrays.asList(offers)));
    }
}
