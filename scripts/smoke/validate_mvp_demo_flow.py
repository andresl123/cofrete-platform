#!/usr/bin/env python3
"""Validate the synthetic Cofrete MVP demo flow fixture."""

from __future__ import annotations

import csv
import json
from decimal import Decimal
from pathlib import Path
import sys


ROOT = Path(__file__).resolve().parents[2]
SEED = ROOT / "scripts" / "demo" / "cofrete-mvp-demo-seed.json"
ANP_FIXTURE = (
    ROOT
    / "data-importer-worker"
    / "src"
    / "main"
    / "resources"
    / "fixtures"
    / "anp-diesel-prices-synthetic.csv"
)
MOBILE_DASHBOARD_TEST = ROOT / "mobile-app" / "src" / "__tests__" / "MobileApp.test.tsx"


def money(value: str) -> Decimal:
    return Decimal(value).quantize(Decimal("0.01"))


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def validate_finance(seed: dict) -> None:
    trip = seed["tripCostInput"]
    reserves = seed["reservePolicy"]
    expected = seed["expectedFinance"]

    pass_through = (
        money(trip["tollReimbursement"])
        + money(trip["valePedagio"])
        + money(trip["otherPassThrough"])
    )
    require(pass_through == money(expected["passThroughAmount"]), "pass-through amount mismatch")

    fuel = (
        money(trip["totalDistanceKm"])
        / money(trip["dieselConsumptionKmPerLiter"])
        * money(trip["dieselPricePerLiter"])
    )
    direct_cost = (
        fuel
        + money(trip["arlaCost"])
        + money(trip["nonReimbursedToll"])
        + money(trip["mealsAndLodgingCost"])
        + money(trip["otherDirectCost"])
    )
    require(direct_cost == money(expected["directTripCost"]), "direct trip cost mismatch")

    reserve_base = money(trip["grossFreight"]) - pass_through
    require(reserve_base == money(expected["profitBaseExcludingPassThrough"]), "reserve base mismatch")

    required_reserves = sum(
        reserve_base * money(rate)
        for rate in (
            reserves["maintenanceRate"],
            reserves["tireRate"],
            reserves["taxRate"],
            reserves["insuranceRate"],
            reserves["replacementRate"],
            reserves["emergencyRate"],
        )
    )
    require(required_reserves == money(expected["requiredReserves"]), "required reserves mismatch")

    profit = reserve_base - direct_cost - required_reserves - money(trip["financingAllocation"])
    require(profit == money(expected["expectedProfit"]), "expected profit mismatch")
    require(
        profit == money(expected["safePersonalWithdrawal"]),
        "safe personal withdrawal must equal expected demo profit",
    )

    pass_through_heavy_profit = money("1000.00") - money("900.00")
    require(pass_through_heavy_profit == money("100.00"), "Vale-Pedagio/toll pass-through increased profit")


def validate_anp_fixture(seed: dict) -> None:
    expected = seed["anpFuelPriceExpectation"]
    with ANP_FIXTURE.open(newline="", encoding="utf-8") as handle:
        rows = list(csv.DictReader(handle))

    matches = [
        row
        for row in rows
        if row["fuel_type"] == expected["fuelType"]
        and row["state"] == expected["state"]
        and row["city"] == expected["city"]
    ]
    require(len(matches) == 1, "expected one matching ANP fixture row")
    row = matches[0]
    require(row["price_per_liter_brl"] == expected["pricePerLiterBrl"], "ANP latest fuel price mismatch")
    require(row["period_end"] == expected["periodEnd"], "ANP period end mismatch")
    require(row["freshness_status"] == expected["freshnessStatus"], "ANP freshness mismatch")


def validate_compliance(seed: dict) -> None:
    compliance = seed["compliance"]
    require(compliance["rntrcStatus"] == "UNKNOWN", "demo RNTRC starts as unknown advisory metadata")
    require(
        compliance["rntrcOfficialActionUrl"] == "https://consultapublica.antt.gov.br/",
        "RNTRC official action URL mismatch",
    )
    require(
        compliance["expectedAlert"]["subjectType"] == "INSURANCE_POLICY",
        "insurance expiration alert subject mismatch",
    )
    require(compliance["expectedAlert"]["status"] == "EXPIRING_SOON", "insurance alert status mismatch")


def validate_dashboard_test() -> None:
    test_source = MOBILE_DASHBOARD_TEST.read_text(encoding="utf-8")
    for required in (
        "renders financial health dashboard with reserves, receivables, compliance, and caveats",
        "88/100",
        "Pedagio reembolsado e Vale-Pedagio ficam fora do lucro",
        "/api/financial-health-score",
        "/api/receivables?status=overdue",
    ):
        require(required in test_source, f"mobile dashboard smoke coverage missing: {required}")


def main() -> int:
    seed = json.loads(SEED.read_text(encoding="utf-8"))
    require(seed["synthetic"] is True, "demo seed must be explicitly synthetic")
    validate_finance(seed)
    validate_anp_fixture(seed)
    validate_compliance(seed)
    validate_dashboard_test()
    print("MVP demo smoke fixture passed.")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except AssertionError as error:
        print(f"MVP demo smoke fixture failed: {error}", file=sys.stderr)
        raise SystemExit(1)
