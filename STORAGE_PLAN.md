# Storage plan for multiple laundry branches

Status: architecture proposal. The UI fixes are implemented separately. No database,
cloud project, billing account, or production migration has been created.

Confirmed requirement: multiple branches with online access. Offline selling has
not been requested; the first shared release should confirm sales online and keep
unfinished drafts locally during an outage.

## Updated budget: free online services only

The owner requires zero paid cloud subscriptions. The preferred initial provider
is **Supabase Free**, which supplies hosted PostgreSQL, authentication, a Data API,
and image storage. Use one project for the organization and distinguish branches
with branch IDs and access policies; do not create a separate project per branch.

Checked on 31 October 2026, the published Free allowance includes 500 MB of database space, 1 GB of
file storage, 5 GB uncached plus 5 GB cached egress, and 50,000 monthly active users. Projects may pause after
a week of inactivity. These are provider limits, not a promise of unlimited usage
or permanent availability. [Supabase pricing](https://supabase.com/pricing).

For this version, avoid a separately hosted Java API server. The Java desktop can
call Supabase's HTTPS Data API and authenticated database functions. Keep checkout
rules in controlled server-side database functions, not only in the desktop UI.
Supabase exposes database APIs and function calls, which makes this architecture
possible without a separate application-hosting subscription.
[Data API](https://supabase.com/docs/guides/api),
[database functions](https://supabase.com/docs/guides/database/functions).

Implementation rules for the free setup:

- Use real user sessions and row-level branch permissions on every exposed table.
  Ordinary cashier sessions must not have direct write access to confirmed sales,
  payment totals, or stock balances; expose constrained transaction functions.
  Review function execution grants and privileges. Never put service-role keys or
  database administrator credentials in the distributed desktop app.
  [Row-level security](https://supabase.com/docs/guides/database/postgres/row-level-security).
- Keep idempotent checkout, stock locking, price validation, audit history, and
  reconciliation from the plan below. A managed API does not implement these
  business rules automatically.
- Compress and resize shared photos before uploading; keep original files locally
  where needed. Cache thumbnails to reduce repeated downloads.
- Check storage and traffic in the provider dashboard. Avoid constantly reloading
  entire order histories or attaching unrestricted live listeners per terminal.
- Use periodic local exports of database data plus photo manifests and test a
  restore. Free projects do not offer downloadable platform database backups.
  [Free-plan availability and backups](https://supabase.com/docs/guides/deployment/going-into-prod).
- Plan staff provisioning and password recovery explicitly. The default Supabase
  email sender is restricted to project-team addresses and limited test traffic;
  it is not a production staff email service. Use manually provisioned, verified
  staff accounts for a controlled pilot, or configure a separately verified free
  email-delivery allowance for confirmations/recovery. Avoid paid SMS features.
  [Email delivery limitations](https://supabase.com/docs/guides/auth/auth-smtp).
- Remain on the Free plan. Do not enable paid upgrades, paid add-ons, or a billing
  project as part of this implementation. If the free allowance becomes too small,
  review retention, image sizes, usage, or another architecture with the owner.

This is a bounded free-tier pilot, not a guarantee of always-on production service.
The application's current local file storage has not yet been migrated or
connected to Supabase.

## Recommendation

Use centrally hosted **PostgreSQL through Supabase Free's authenticated APIs and
controlled database functions**, with its storage service for product/staff photos.
Keep the Java Swing POS as the desktop client. Add local SQLite for cached data
and durable drafts when needed. Full offline checkout and synchronization should
be a separate phase. A separate backend server remains a future option if later
requirements justify it and the budget changes.

This is a recommendation for this application's linked orders, line items,
payments, branch inventory, transfers, and reporting. PostgreSQL provides
transactions for committing related changes together and constraints for enforcing
valid relationships and unique identifiers. The business rules still need to be
implemented correctly in the server. [Transactions](https://www.postgresql.org/docs/current/tutorial-transactions.html),
[constraints](https://www.postgresql.org/docs/current/ddl-constraints.html).

Proposed flow:

1. Each branch POS signs in and receives access to its authorized branch.
2. The desktop communicates over HTTPS with authenticated Supabase APIs and
   database transaction functions.
3. The API validates prices, permissions, payment records, and stock, then commits
   the sale in PostgreSQL.
4. Photos are uploaded to object storage; the database holds their asset metadata.
5. The owner dashboard queries branch-specific or consolidated server reports.
6. A local cache/draft store can keep the counter usable while connectivity is
   interrupted; it must clearly show which actions require an online connection.

## Options compared

| Option | Appropriate role here | Main tradeoff |
| --- | --- | --- |
| Current `.properties` files | Existing local data, import/export, simple device preferences | No shared branch database or cross-device transaction coordination |
| SQLite | Local cache, draft orders, or a standalone single-PC installation | A local database does not itself provide branch synchronization |
| PostgreSQL + backend API | Recommended central source of truth | Requires backend hosting, maintenance, monitoring, and backups |
| Firebase / Cloud Firestore + backend | Viable managed document-database alternative | Needs a deliberate document/reporting model and a desktop offline strategy |

SQLite is useful for application-local data and enterprise caches. Avoid sharing
one live SQLite file through a network folder as a way to connect branches; SQLite's
own guidance recommends a client/server approach for that situation.
[SQLite deployment guidance](https://www.sqlite.org/whentouse.html).

## Where Firebase fits

Firebase is viable if managed cloud services and document-based data access are
preferred. It would still need real authentication, branch authorization, server
validation of sales, object storage, backups, and a migration path for current data.
For this Java desktop application, use an authenticated backend between the POS
and Firestore for privileged operations.

Two distinctions matter for this codebase:

- Firebase's **Java server libraries** run in a privileged environment and use
  IAM rather than client Security Rules. Keep their administrative credentials on
  the server; do not bundle service-account keys in the distributed POS.
  [Firebase library guidance](https://firebase.google.com/docs/firestore/client/libraries).
- Firestore's built-in offline persistence is documented for Android, Apple, and
  web applications. The Java examples on that offline page are Android examples;
  they do not provide automatic offline persistence for a Swing desktop client.
  [Offline support](https://firebase.google.com/docs/firestore/manage-data/enable-offline).

Firestore supports atomic transactions, so it can implement a consistent sale
and stock update when designed correctly. Transactions fail offline; queued local
sales therefore still need explicit reconciliation policies.
[Firestore transactions](https://firebase.google.com/docs/firestore/manage-data/transactions).

For this relational POS, PostgreSQL is my first choice. Firestore remains an
alternative if the team prefers its managed model and accepts the extra work for
cross-branch reporting, derived summaries, and desktop synchronization. Firebase
does not automatically solve overselling or offline conflict resolution.

## Central data model

| Data | Important fields and rules |
| --- | --- |
| Organizations / branches | Organization ID, branch ID, name, timezone, active status |
| Users / memberships | Authenticated user ID, assigned branches, role, active status |
| Devices / shifts | Device ID, branch, cashier, opening/closing records |
| Catalog / branch prices | Stable item ID, service/product type, unit, category, branch price, archive state |
| Customers | Stable customer ID, contact details, scoped access policy |
| Orders / order lines | UUID, branch, device, cashier, customer, price/tax snapshots, status, timestamps |
| Payments | Order reference, method, amount, payment reference, reversal history |
| Stock balances / movements | Branch + item, quantities, sale/restock/count/transfer references |
| Assets | Storage key, checksum, media type, dimensions, organization/branch scope |
| Audit records | Actor, branch, action, target, timestamp, reason |
| Client requests / sync records | Idempotency key, device, result, retry status |

Use precise decimal values for money and weights. Keep original receipt values
immutable; future price or tax changes must not rewrite historical sales. Use
server timestamps for recorded events and branch timezones for daily reports.
Archive catalog entries so their historical order references remain usable.

## Checkout and inventory rules

The current implementation uses local files and an in-process synchronized save.
It checks stock by scanning saved orders. That is appropriate only within its
current local scope; it cannot coordinate simultaneous checkout across PCs.

The proposed server checkout should:

1. Verify the authenticated user, device, organization, and branch permissions.
2. Require a stable client order UUID / idempotency key. Retrying after a timeout
   must return the existing sale rather than create another one.
3. Validate the selected catalog version and branch prices. Present changed prices
   for review instead of silently charging a different total.
4. Start a database transaction; lock/update the relevant branch stock rows in a
   consistent order and enforce the stock policy.
5. Write the order, lines, payment, stock movements, and audit entry together.
6. Commit, return the confirmed order/receipt identity, and update analytics.
7. If the request times out, look up its idempotency key before offering a retry.

Stock is tracked per branch. Branch transfers need linked transfer-out and
transfer-in records with a defined in-transit state, rather than editing two
unrelated stock counts. Consolidated reporting must distinguish confirmed sales
from drafts, pending synchronization, voids, and refunds.

## Connectivity and offline behavior

For the first online release:

- Keep cached catalog data and draft orders locally. Show connection status and
  the last successful refresh time.
- Confirm payments/sales only when the server acknowledges the transaction.
- After a timeout, show a checking state and query the original request; do not
  ask the cashier to charge again while the result is uncertain.
- If offline selling becomes necessary, add a durable SQLite outbox, device IDs,
  versioned messages, retry deduplication, and a manager reconciliation screen.
- Decide an explicit offline stock policy before allowing offline sales. Options
  include per-terminal stock allocations or restricted product sales. Independent
  offline terminals cannot guarantee a shared real-time stock balance by themselves.

Do not build general synchronization by overwriting complete database files or
using last-write-wins replacement for payments and stock counts.

## Photo and file handling

Keep files for what they do well: imported images, cached thumbnails, exports,
printable receipts, and backups. Keep transaction metadata and relationships in
the central database.

The upload flow should validate the image, generate a storage key, upload the
object, and register its metadata. Branch PCs download/cache the photo by asset
ID or checksum. Use access-controlled downloads or time-limited URLs where needed.
Do not store one PC's absolute image path as a shared branch photo reference.
Failed uploads must be retryable; unreferenced assets need a delayed cleanup policy.

## Migration and delivery sequence

1. **Separate persistence from Swing screens.** Introduce repository interfaces
   for catalog, orders, inventory, profiles, and assets. Keep current behavior
   working while replacing its storage implementation.
2. **Build the central schema and API.** Add real login, branch membership checks,
   server pricing, transaction-safe stock updates, idempotent checkout, and audit
   records. Current profile switching is display identity, not authentication.
3. **Import a copied dataset into staging.** Back up the complete current data
   folder. Map local catalog IDs per source branch to avoid collisions such as
   repeated `legacy-service-0` identifiers. Preserve order UUIDs, receipt snapshots,
   cashier names, and image references. Flag incomplete CSV rows for review rather
   than inventing missing order details.
4. **Reconcile imports.** Compare row counts, order totals, tax totals, stock
   balances, photo checksums, and representative receipts. Make rerunning an import
   safe and deduplicated.
5. **Pilot one branch.** Use a defined cutover point and reconcile opening stock.
   Keep a recoverable pre-migration snapshot. Avoid uncontrolled double entry into
   both the old files and the new database.
6. **Roll out additional branches.** Verify branch isolation and consolidated
   reports before enabling owner-wide access. Add offline checkout only after its
   conflict policies have been tested.

## Backup, operations, and acceptance checks

For the required Free plan, schedule regular database exports and keep off-site
copies; automatic daily platform backups are a paid-plan feature. Copy Storage
objects separately because database backups contain their metadata, not the photo
contents. Define retention and a restore target for both database and assets.
[Supabase backup documentation](https://supabase.com/docs/guides/platform/backups).
Test restoring into an isolated environment. A synchronized folder alone is not a tested backup system. For a
live local SQLite cache, use a supported snapshot/backup method instead of copying
an actively changing database blindly.
[SQLite backup guidance](https://www.sqlite.org/backup.html).

Before rollout, verify:

- Two terminals trying to sell the last unit cannot both obtain a confirmed sale.
- Repeated clicks, retries, and reconnects produce one payment/order result.
- A cashier cannot retrieve or modify another branch's data by changing a request.
- Cashier identity is verified and stored with every transaction.
- Daily branch reports and owner totals reconcile to confirmed orders and payments.
- Connection loss, uncertain responses, stale prices, and failed uploads have
  clear user-visible recovery paths.
- A database-and-assets restore reproduces receipts, stock, users, and photos.

## Cost decision

Estimate using the number of branches, concurrent terminals, daily orders, lines
per order, report frequency, image storage, retention, and expected outages.
Compare managed PostgreSQL hosting/storage/backups and the API with Firestore's
operation, index-read, storage, and network charges plus its backend and assets.
Avoid treating either architecture as automatically free or automatically cheaper.
[Firestore billing model](https://firebase.google.com/docs/firestore/pricing).

Recommended next implementation milestone: repository interfaces and a local
Supabase-compatible schema/API prototype with two simulated branches, verified
sign-in, and an idempotent transactional checkout. Then connect a single Free
project for the controlled pilot. Production rollout comes after reconciliation
and concurrency tests and an explicit acceptance of free-tier availability limits.
