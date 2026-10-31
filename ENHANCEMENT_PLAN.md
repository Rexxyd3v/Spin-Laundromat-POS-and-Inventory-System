# Enhancement roadmap before Supabase migration

Updated: 1 November 2026. Status: cleanup, selected local features and local account
authentication implemented; no cloud project, production import or online checkout
has been enabled.
The current application remains a local Java Swing POS. Local authentication now
uses salted password hashes, first-login password changes, persistent attempt limits,
Owner account administration and memory-only sessions; see README.md.

## Applied selections 1 and 5

Implemented repository contracts and file adapters, a checkout use case with safe
same-request retries, central data location, read-only migration audit, normalized
exports, verified backup ZIPs and isolated restore. Added live product alerts at or
below threshold, branch-aware counts and transfer states, machine maintenance and
conflict-checked scheduling, custom-date financial analytics, and 58/80 mm receipt
printing/reprints. See README.md for the complete operating flow and limits.

These are local features. Supabase Auth/RLS, cloud transactions and synchronized branch POS
remain the staging milestone; customer/production/payment-state expansion remains
on this roadmap. Existing data is retained and no live migration ran.

## What already works

The active path is `Main → MasterKey → LoginWindow → WelcomePanel → WorkspacePanel`.
The workspace contains Overview, Point of sale, Catalog, Inventory, Reports, Team,
Settings and Operations. The same window handles the master-key and verified account sign-in screens and logout.

| Area | Verified current behavior | Remaining gap |
| --- | --- | --- |
| POS | Search/filter, photos, decimal kg, mixed service/product cart, customer details, notes, saved checkout | No durable held orders, discounts, split/partial payments, or refunds |
| Catalog | Editable services/products, managed PNG/JPEG photos, responsive add/edit dialogs | Removal deletes the catalog entry; no archive state or branch prices |
| Inventory | Physical count, minimum threshold, stock derived from saved sales | Transfer ledger and branch counts now work; no general receiving/supplier/cost ledger |
| Reports | Saved orders, filters, CSV export, receipt reprint | No shift settlement, refund adjustments, production workload, or branch filters |
| Overview | 7/30/90/custom date ranges, tax/base/cash metrics, payment mix, top sellers, operations summary | Revenue includes recorded tax; no profit calculation or unpaid-order metrics |
| Team | Authenticated Owner/Manager/Cashier accounts, role checks, password resets, profiles and photos | Online identity, branch memberships and production-operator role remain future work |
| Settings | Saved branding, tax, receipt text, view/density and accent | Verified backup/isolated restore now works; no branch configuration or general settings audit |
| Storage | Atomic local writes and complete receipt/price snapshots | Local scope only; no coordination between PCs/branches |

Keep the approved UI direction: navy/teal workspace, yellow accent text on login,
original stationary mascot, animated login background, bordered profile images,
shared vector icons, smooth fades, and scrollable forms with visible actions.

## Recommended delivery order

| Milestone | Work | Completion condition |
| --- | --- | --- |
| 0 — Cleanup | Remove obsolete screens and compatibility code after a reference audit; archive originals; clean build output | Main, all active pages, previews and existing checks pass without old classes |
| 1 — Prepare the local model | Separate persistence from UI; define branch/customer IDs, order/payment states, archive rules and migration mappings; add backup/export verification | Current local behavior still works through the new interfaces; copied data can be audited without changing originals |
| 2 — Build Supabase in staging | Real sessions, memberships, table/storage policies, server checkout transaction, migration dry run and reconciliation | Two test branches are isolated; retries create one order; last-unit concurrency and restore tests pass |
| 3 — Pilot essential operations | Laundry order board, customer lookup, balances, pickup, stock history and cashier settlement | A pilot branch can complete and reconcile the full receive-to-pickup workflow |
| 4 — Expand | Transfers, supplier purchasing, machine scheduling, loyalty, notifications, customer portal | Add only after the pilot metrics and data recovery are reliable |

Do not delay the migration for every optional feature. Design order/payment states,
branch scope, stable IDs, archive behavior and stock movements before importing;
implement advanced screens in later milestones. Authentication, access policies,
idempotent checkout and reconciliation are required before production cutover.

## Prioritized features

| Priority | Feature | Proposed behavior | Dependencies / acceptance |
| --- | --- | --- | --- |
| P0 | Storage boundaries | Catalog, orders, stock, team, settings and assets accessed through repository interfaces; checkout uses an application service | Retain file implementations first; Swing no longer decides file paths or coordinates writes |
| P0 | One data location | Central configuration for the active data folder and source branch; show it in a support/export screen | Preserve both existing `data` folders and both services files until their provenance is reconciled; never silently merge them |
| P0 | Verified accounts and roles | Owner, manager, cashier and production operator; permission checks on actual actions and server requests | Supabase Auth plus memberships; no plaintext passwords, global master-key bypass or profile-switch impersonation |
| P0 | Branch identity | Branch name in the top bar and on receipts; owner branch filter; explicit switching | Branch-qualified catalog prices, orders and stock; switching cannot silently discard an open draft |
| P0 | Backup and import audit | Export all records plus an image manifest; checksum/count validation and restore into an isolated folder | Recovery succeeds before import; include old CSVs as reviewable legacy records rather than fabricated complete orders |
| P0 | Reliable checkout | Stable request/order ID, price review, busy/unknown/confirmed/failed states, one confirmed receipt | Server transaction creates order, lines, payment, stock movements and audit together; timeout lookup before retry |
| P1 | Laundry order board | Received → Washing → Drying → Ready → Collected, with due date, assigned operator and transition history | Separate production status from payment status; skip stages for product-only orders and configurable service workflows |
| P1 | Customer history and pickup | Customer ID, phone search, previous orders, order-number lookup and pickup slip | Walk-in remains possible; limited access to contact data; authorized release records who collected and when |
| P1 | Held orders and restart recovery | Save/resume named drafts, confirm before discarding, recover after app restart | Drafts are excluded from revenue and stock deduction; draft storage survives outages |
| P1 | Partial and split payments | Deposit, balance due, cash/GCash split and later balance collection | Payment ledger and balance rules; order total counted once, cash collections counted by payment date; GCash remains manually verified until a provider is integrated |
| P1 | Controlled discounts, voids and refunds | Configured discount types/limits; manager approval, reason, original-order link | Reverse payment and stock deliberately; retain original receipt and an audit trail rather than deleting history |
| P1 | Inventory movement history | Opening count, delivery, sale, waste, adjustment and reversal records | Branch/item ledger with actor, time, quantity and reason; counted stock reconciles to movements |
| P1 | Shifts and cash reconciliation | Opening cash, cash in/out, expected cash, counted cash and variance | Track verified cashier and device; include refunds and deposits in the correct shift; closing cannot silently rewrite sales |
| P1 | Catalog archive and price versions | Archive instead of deleting; branch overrides, service turnaround, optional minimum kg/charge | Old receipts retain item/price/tax snapshots; archived items cannot be added to new orders |
| P1 | Useful analytics | Date range, branch, overdue jobs, ready-for-pickup count, receivables, net sales, refunds and cash collections | Define each metric; distinguish tax-inclusive total, sales excluding tax, refunds and receipts of money; profit waits for historical cost snapshots |
| P1 | Receipt and printing options | 58/80 mm layouts, store/branch/operator, pickup date, status, deposit/balance, reprint label | Printable data comes from the confirmed immutable order; printer failure does not duplicate checkout |
| P1 | Consistent UX and performance | Keyboard shortcuts, focus scrolling, empty/loading/retry states, responsive dialogs and long-name handling | Move file/network/report work off the Swing UI thread; use pagination and cancel/ignore stale search results |
| P2 | Purchasing and branch transfers | Supplier deliveries/costs; transfer requested, dispatched, received and reconciled | Linked movement records, in-transit quantities and approval; no unrelated stock edits at each branch |
| P2 | Machine scheduling | Washer/dryer availability, cycle estimate and assignment | Uses production jobs; resolves conflicting assignments and maintenance outages |
| P2 | Loyalty, notifications and portal | Explicit opt-in; status lookup, ready alerts, optional points | Privacy rules and separate integration budget; do not assume free SMS or free unlimited email |

P0 = needed for safe shared-data rollout. P1 = core operating improvements, delivered
incrementally around the pilot. P2 = optional expansion, not a migration blocker.

## Proposed navigation and counter flow

Keep the current seven pages during preparation. As features are implemented, add
**Orders** for production/pickup, **Customers** for history, and **Shifts** for cash
settlement. Group owner configuration under Settings instead of adding duplicate
entry points. Put permissions on actions as well as on navigation.

1. Sign in with a real staff account, select an authorized branch and open a shift.
2. Find/select a customer or choose walk-in; add services/products and weights.
3. Set due date, instructions and any permitted discount; review the complete total.
4. Record full payment or deposit; wait for a confirmed server response.
5. Print/reprint the receipt and pickup slip from the saved order.
6. Update laundry stages in Orders; show ready/overdue work on Overview.
7. Look up pickup, collect any balance and record release to the customer.
8. Close and reconcile the shift; manager reviews exceptions and audit history.

During a connection loss, allow cached lookup and durable drafts. Mark checkout as
requiring an online confirmation for the first shared release. Full offline selling
needs a separate outbox, reconciliation and stock policy; it is not an automatic
property of moving data to Supabase.

## Code structure to prepare

Keep `JavaProject/src` as the source root and `Main` as the single supported launch.
Do not introduce empty placeholder classes just to create folders.

- `model`: pure records, money/quantity rules and state transitions.
- `application`: use cases such as checkout, refund, receive stock and pickup.
- `persistence`: repository contracts and the current file implementations.
- `integration/supabase`: future Auth, REST/RPC and Storage adapters with timeouts,
  cancellation and typed failures; no Swing controls inside them.
- `ui`: current screens and shared components; split the growing `BusinessPages`
  into focused Reports, Inventory and Team pages when those areas are next changed.
- `test`: contract tests reused for file and future cloud adapters, plus UI checks.

Keep `OrderPricing` as the client's preview calculator. The server must independently
validate prices, tax and totals; the desktop cannot be the authority for stock or
permissions. Preserve the current complete order snapshot during refactoring.

## Supabase preparation decisions

Use one shared organization project with organizations, branches, memberships,
customers, catalog items/branch prices, orders/lines, payments, stock movements,
assets, shifts and audit events. Use precise decimal money/weights, UUIDs for stable
identity and server timestamps; report dates use each branch's timezone.

Use the publishable key and signed-in user session in the distributed desktop.
Privileged provisioning/import credentials stay in an administrator-controlled
process. Server functions must validate membership and restrict execution/write
permissions; a user must not bypass checkout by inserting payment/order rows
directly. Table access needs row-level policies, and private photos need Storage
policies too. These requirements follow Supabase's
[API key guidance](https://supabase.com/docs/guides/getting-started/api-keys),
[row-level security](https://supabase.com/docs/guides/database/postgres/row-level-security),
and [Storage access controls](https://supabase.com/docs/guides/storage/security/access-control).

For images, prepare asset IDs, checksums, dimensions, storage keys and cached
thumbnails. Proposed upload target: longest side 1,024 px and roughly 250 KB where
quality permits, plus small thumbnails generated by the application. Keep staff
photos private; do not copy a local absolute path or an expiring signed URL into
permanent shared records. Failed uploads remain retryable; delete unreferenced
objects only after a grace period and reference check.

The free-only preference remains in force. Checked on 31 October 2026, the Free
plan lists 500 MB database space, 1 GB file storage, 5 GB uncached plus 5 GB cached
egress and 50,000 monthly active users; inactive projects can pause. Track usage,
cache images, paginate reports and avoid continuous full-table refreshes.
[Supabase pricing](https://supabase.com/pricing).

Plan regular database exports and a separate copy of stored photos. Free projects
need their own export/backup process, and a database backup alone does not restore
Storage object contents. Verify a complete restore before the pilot.
[Supabase backup documentation](https://supabase.com/docs/guides/platform/backups).

## Migration readiness checklist

- [ ] Inventory both local data locations, source branch, legacy IDs and image references.
- [ ] Back up originals; prove restore into an isolated folder without touching live data.
- [ ] Introduce and test repository contracts while keeping local behavior unchanged.
- [ ] Freeze ID/state/archive/payment rules and define missing-data handling.
- [ ] Create versioned schema, constraints, grants and policies in staging.
- [ ] Test real sign-in, disabled users and cross-branch denial for every operation.
- [ ] Test concurrent last-unit checkout, double clicks, timeouts and idempotent retries.
- [ ] Dry-run a copied import; compare counts, amounts, tax, receipts, stock and image checksums.
- [ ] Re-run the import without duplicates; generate a review file for incomplete rows.
- [ ] Confirm cash/printer recovery, connection-loss UX and database-plus-assets restore.
- [ ] Define a branch cutover time, opening stock and rollback procedure; avoid dual writes.

Recommended next implementation: repository boundaries plus a read-only migration
audit/export tool. Then build the staging schema/Auth/checkout path before importing
production data. This roadmap does not authorize or perform a live migration.
