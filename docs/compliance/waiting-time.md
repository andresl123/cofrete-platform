# Loading And Unloading Waiting Time

Waiting time at loading and unloading points affects real profitability and customer quality. Cofrete should track it as a financial and customer-risk signal.

## MVP Behavior

- Store arrival time, release/departure time, location, customer, cargo tons, and trip ID.
- Calculate advisory excess waiting time when configured thresholds are exceeded.
- Lower customer score when repeated delays damage trip economics.
- Keep official wording advisory until legal review confirms production copy.

## Planning Formula

The master blueprint records a planning rule for loading/unloading compensation:

```text
extra_waiting_charge = cargo_tons * excess_hours_or_fraction * configured_rate_per_ton_hour
```

The threshold, rate, effective year, and source URL must be configurable data, not hardcoded constants.

```text
waiting_time_rule
threshold_hours
rate_per_ton_hour
effective_start_date
effective_end_date
source_url
```

## Product Wording

- "Waiting time can reduce the real profit of this freight."
- "This is an estimate. Confirm official rules and contract terms before charging or disputing a customer."
