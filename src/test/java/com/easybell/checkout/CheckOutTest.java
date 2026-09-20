package com.easybell.checkout;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CheckOutTest {

    // Item   Unit    Special       Price
    // A      50      3 for 130
    // B      30      2 for 45
    // C      20      (no special)
    // D      15      (no special)
    private Map<Character, PricingRule> rules;

    @BeforeEach
    void setUp() {
        rules = Map.of(
                'A', StandardPricingRule.withOffer(50, 3, 130),
                'B', StandardPricingRule.withOffer(30, 2, 45),
                'C', StandardPricingRule.unitOnly(20),
                'D', StandardPricingRule.unitOnly(15)
        );
    }

    private int price(String goods) {
        CheckOut co = new CheckOut(rules);
        for (char item : goods.toCharArray()) {
            co.scan(item);
        }
        return co.total();
    }

    @Test
    void totals() {
        assertEquals(0, price(""));
        assertEquals(50, price("A"));
        assertEquals(80, price("AB"));
        assertEquals(115, price("CDBA"));

        assertEquals(100, price("AA"));
        assertEquals(130, price("AAA"));
        assertEquals(180, price("AAAA"));
        assertEquals(230, price("AAAAA"));
        assertEquals(260, price("AAAAAA"));

        assertEquals(160, price("AAAB"));
        assertEquals(175, price("AAABB"));
        assertEquals(190, price("AAABBD"));
        assertEquals(190, price("DABABA"));
    }

    @Test
    void incrementalScanning() {
        CheckOut co = new CheckOut(rules);
        assertEquals(0, co.total());
        co.scan('A');
        assertEquals(50, co.total());
        co.scan('B');
        assertEquals(80, co.total());
        co.scan('A');
        assertEquals(130, co.total());
        co.scan('A');
        assertEquals(160, co.total());
        co.scan('B');
        assertEquals(175, co.total());
    }

    @Test
    void unknownItemIsRejected() {
        CheckOut co = new CheckOut(rules);
        assertThrows(IllegalArgumentException.class, () -> co.scan('Z'));
    }

    // --- Corner cases: not every item has an offer, and some items
    // may have more than one simultaneous offer. ---

    @Test
    void itemWithNoOfferAlwaysChargesUnitPrice() {
        // C has no special offer at all - any quantity is just unitPrice * quantity.
        PricingRule cRule = rules.get('C');
        assertEquals(0, cRule.priceFor(0));
        assertEquals(20, cRule.priceFor(1));
        assertEquals(200, cRule.priceFor(10));
    }

    @Test
    void itemWithMultipleOffersPicksCheapestCombination() {
        // Item E: unit price 100, with two simultaneous offers:
        //   3 for 250
        //   5 for 500
        // These interact: at quantity 5, buying one unit's worth
        // fewer via two "3 for 250" style combos can beat the flat
        // "5 for 500" bundle. The rule must always find the true
        // minimum-cost combination, not just greedily prefer the
        // largest bundle.
        PricingRule eRule = StandardPricingRule.withOffers(
                100,
                new SpecialOffer(3, 250),
                new SpecialOffer(5, 500)
        );

        assertEquals(0, eRule.priceFor(0));
        assertEquals(100, eRule.priceFor(1));
        assertEquals(200, eRule.priceFor(2));
        assertEquals(250, eRule.priceFor(3));  // one 3-for-250 bundle
        assertEquals(350, eRule.priceFor(4));  // 3-for-250 + 1 unit
        assertEquals(450, eRule.priceFor(5));  // 3-for-250 + 2 units beats 5-for-500
        assertEquals(500, eRule.priceFor(6));  // two 3-for-250 bundles
        assertEquals(600, eRule.priceFor(7));  // two 3-for-250 + 1 unit
        assertEquals(750, eRule.priceFor(9));  // three 3-for-250 bundles
    }

    @Test
    void explicitEmptyOfferListBehavesLikeUnitOnly() {
        PricingRule rule = new StandardPricingRule(40, List.of());
        assertEquals(0, rule.priceFor(0));
        assertEquals(40, rule.priceFor(1));
        assertEquals(120, rule.priceFor(3));
    }
}
