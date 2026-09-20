# Checkout Kata

Solution to [Kata09 - Checkout](http://codekata.com/kata/kata09-back-to-the-checkout/).

## How to run

```bash
mvn test
```

## Design

- **`PricingRule`** : a strategy interface: given a quantity of one SKU, return its
  total price. `CheckOut` depends only on this interface, so it knows nothing about
  which items exist or how any item is priced. New pricing schemes can be added by
  implementing this interface, with no change to `CheckOut`.

- **`StandardPricingRule`** : the concrete pricing rule used for all items. Every item
  has a unit price, plus zero, one, or several simultaneous `SpecialOffer`s (e.g. an
  item could have both "3 for 130" and "5 for 200" active at once). Pricing is computed
  with dynamic programming, which always finds the minimum-cost combination of units
  and offers, a naive greedy approach (always apply the largest bundle first) is not
  guaranteed to be optimal once multiple overlapping offers exist.

- **`CheckOut`** : tallies scanned quantities per item and delegates to each item's
  `PricingRule` to compute the total. Items are represented as `char`, matching the
  kata's spec ("individual letters of the alphabet"); this also rules out invalid
  multi-letter input at compile time rather than needing runtime validation.

- **`SpecialOffer`** : a simple value object for one "buy N for Y" tier.

## Tests

`CheckOutTest` covers:
- All totals and incremental-scan examples from the kata spec
- An unknown item being rejected
- An item with no special offer at all (pure unit pricing)
- An item with multiple simultaneous special offers, verifying the cheapest
  combination is chosen even when a larger bundle alone would be suboptimal
