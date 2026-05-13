# Cofrete Platform Context

Cofrete is a financial health and compliance assistant for Brazilian truck drivers and owner-operators. This context records the canonical language used across product, architecture, compliance, and agent documentation.

## Language

**Canonical Markdown Doc**:
A markdown document that owns the durable source-of-truth wording for product scope, architecture, compliance, contracts, operations, or agent workflow.
_Avoid_: generated page, visual explainer

**Explainer Page**:
A manually maintained, derived HTML summary of canonical markdown docs.
_Avoid_: source of truth, canonical doc

**Derived Entry Point**:
A reader-friendly orientation page that helps people choose which canonical docs to read next.
_Avoid_: canonical reference

**Agent Harness Check**:
A lightweight repository validation script that confirms required foundation files and documentation guardrails are present.
_Avoid_: full test suite, content drift detector

## Relationships

- An **Explainer Page** summarizes one or more **Canonical Markdown Docs**.
- An **Explainer Page** can be used as a **Derived Entry Point** for faster orientation.
- A **Canonical Markdown Doc** should be updated before its manually maintained **Explainer Page** is realigned.
- The **Agent Harness Check** verifies that required **Explainer Pages** exist and identify themselves as manually maintained derived summaries.

## Example dialogue

> **Dev:** "Should I edit the architecture explainer to change service ownership?"
> **Domain expert:** "No. Update the **Canonical Markdown Doc** first, then manually realign the derived **Explainer Page** so it stays accurate."

> **Dev:** "Will the harness prove the explainer content is current?"
> **Domain expert:** "No. The **Agent Harness Check** only verifies the **Explainer Page** guardrail text exists; humans still realign the content after canonical markdown changes."

## Flagged ambiguities

- "docs" was used to describe both canonical markdown files and generated HTML summaries. Resolved: markdown docs are canonical; HTML explainers are manually maintained derived summaries until a generator exists.
