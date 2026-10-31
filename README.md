# Laundry workspace

A Java Swing desktop POS with a shared layout across Point of Sale, Overview,
Catalog, Inventory, Reports, Team, Settings, checkout, and the welcome screens.

For the proposed multi-branch online architecture and database migration, see
[STORAGE_PLAN.md](STORAGE_PLAN.md). The current application still uses local files.
The prioritized feature roadmap and migration readiness criteria are in
[ENHANCEMENT_PLAN.md](ENHANCEMENT_PLAN.md). The code cleanup inventory and recovery
instructions are in [CLEANUP_REPORT.md](CLEANUP_REPORT.md).

## Run

Install or select a JDK version 21 or later. From this repository, run:

```powershell
.\Run.ps1
```

Alternatively, open this folder or `JavaProject` in VS Code, select **Run Laundry
System**, and press F5. Restart an already running application to see the new UI.
If old Java errors remain, run **Java: Clean Java Language Server Workspace**.

The source root is `JavaProject/src`; the entry point is `Main`. Packages match
their folders within `src`, such as `model`, `ui.frames`, and `util.components`.

## Dashboard and counter flow

The workspace opens on **Overview**. Select the last 7 or 30 days to update the
revenue and order metrics, previous-period comparison, revenue trend, payment mix,
best sellers, and recent orders together. Hover over the trend for daily amounts.
Revenue includes the saved order total; best-seller rankings use item sales before
tax. Repeat customers match recorded phone numbers, otherwise names; anonymous
walk-in customers are excluded from that count.

Use **New order** to go to the POS. The order summary supports a customer name,
item photos, editable quantities with separate minus/plus buttons, and order notes.
**Review & pay** carries the customer and instructions into checkout. Page changes
preserve the unfinished cart. The amount due and payment action stay visible while
the order details scroll.

Long pages have a main scrolling viewport. When an inner table reaches its scroll
boundary, the mouse wheel continues scrolling the page. The dashboard adapts its
metric cards and chart columns to narrower windows.

## Sidebar name and profile photo

Click the profile card at the bottom of the sidebar, then **Edit my profile**.
Choose a PNG/JPEG photo, update the name, and save. The selected profile's name,
role label, and circular photo appear in the sidebar and top bar. Initials are
shown until a photo is provided. **Team** also supports adding and editing photos
for other staff profiles. Use the profile picker to select an active team member.

Profiles are saved locally. The name processing each new checkout is recorded in
the order, so renaming or switching profiles does not change old receipt history.
Profile selection identifies the current operator; it does not authenticate them.

## Add images to services and products

1. Open **Catalog** in the navigation, or click **Catalog** on the POS screen.
2. Choose **Add item**, or select an existing item and click **Edit item**.
3. Click **Choose image**, select a PNG or JPEG, then click **Save item**.
4. The image appears in the catalog and POS cards. Use **Remove image** to return
   to the default illustration, then save the item.

Images are copied into the application's data folder, so moving the original file
does not break them. Uploads support PNG/JPEG files up to 12 MB and 20 megapixels.
Cards preserve the image's proportions and crop to fill their image area.

## Features

- Consistent page layouts, shared buttons and controls, drawn icons without emoji,
  a collapsible sidebar, and a cart drawer for narrower windows.
- Editable services and products, uploaded photos, categories, live search, grid
  or list views, decimal kilogram quantities, and correct per-item units.
- Saved branding, receipt message, accent color, catalog density, default view,
  tax configuration, and manually recorded GCash payments with references.
- Checkout with decimal money calculations, cash received/change, unique order
  references, saved pricing snapshots, receipts, and retry after a failed save.
  The cart clears only after the order has been saved.
- Overview and reports based on saved POS orders, date/search filters, receipt
  reprinting, and CSV export.
- Physical stock counts and minimum levels. Tracked stock decreases from saved
  sales; checkout rejects quantities above the available tracked balance. Items
  without a recorded stock count remain untracked.
- Saved staff profiles with contact details, role labels, and active status.

## Data tools, stock alerts and operations

**Settings → Data & backup** offers a read-only migration audit, backup/export,
checksum verification and restore into a new, separate folder. The ZIP includes
raw records and photos, a checksum manifest, normalized catalog/order/line/team/
inventory/transfer/machine/booking CSVs and an audit report. Older source files
remain separate rather than silently merged. Backups contain private business data;
keep a trusted copy. Restore refuses live/existing destinations and unsafe ZIP paths.
It does not switch the application to the restored copy or import anything to Supabase.

The top-bar **bell** lists tracked retail products with available stock **less than
or equal to** their minimum, including the branch, quantity and threshold. Zero or
negative balances show as out of stock. Alerts refresh after checkout, counts,
catalog changes and transfers; replenishing above the threshold resolves them.
Untracked items and services do not create product warnings. Unreadable order data
shows an unavailable warning instead of a misleading zero-alert count.

In **Inventory**, select a branch and product, then **Update stock** to set the
physical count and low-stock threshold. **Operations → Stock transfers** lets you
add a branch and request stock. Dispatch deducts the source balance; Receive credits
the destination. Requested transfers may be cancelled; repeated/invalid transitions
and dispatches above tracked source stock are rejected. History records actor and
state timestamps. Recounts account for transfer deltas without counting receipts twice.

**Operations → Machines** adds washers/dryers and toggles maintenance. **Schedule**
reserves time, optionally linked to a saved receipt, then supports Start, Complete
and Cancel. Times use Manila time. Overlapping active bookings on the same machine,
unknown linked orders and maintenance-machine reservations are rejected. Start is
allowed within the reserved window; reschedule an expired slot by cancelling and
reserving again. Finish/cancel active bookings before marking a machine unavailable.

Overview now includes **7/30/90-day or custom inclusive date ranges**, sales excluding
tax, recorded tax, cash collections, in-transit transfers and machine activity.
Custom ranges support 1–366 days. Revenue remains the tax-inclusive confirmed sale
total; cash collections here are fully paid Cash checkouts, not shift settlement.
No cost or profit estimate is invented. Receipt preview/reprint shares **58/80 mm**
printing, wraps long text and paginates. Set the preferred width in **Settings →
Store & receipt**. New receipts snapshot date, branch and operator when confirmed;
historical reprints retain that text. Printer failures do not create another sale.

## Current boundaries

The welcome screens remain a clearly labeled **preview**: credential verification
and role enforcement are not implemented. Profile switching is display identity,
not authentication. GCash is manually verified by the cashier.

All branch stock, transfers and machine bookings operate in **one local workspace**.
The POS currently sells from Main branch. This is not online branch synchronization
or a multi-PC transaction guarantee. Repository contracts now separate screens and
checkout from file adapters so Supabase can replace storage later.

Production-stage tracking, partial payments, refunds, shifts, supplier purchasing,
loyalty and customer notifications remain future work. Backup restore is intentionally
an isolated verified copy rather than an automatic live cutover. Actual receipt
paper layout/pagination is tested, but printer-driver/hardware compatibility still
requires checking with the installed printer. No cloud migration ran.

## Data

Normal launches use `JavaProject` as the working directory. Runtime files live in
`JavaProject/data`:

- `catalog.properties`: services/products and photo references
- `images/`: imported catalog photos
- `pos-settings.properties`: store and appearance preferences
- `orders/`: complete order and receipt snapshots
- `stock.properties`: physical stock baselines and minimums
- `team.properties`: staff profiles and photo references
- `profile.properties`: the selected workspace profile
- `operations.properties`: branch names, transfer history, machines and reservations

Existing `services.properties` is read when no new catalog has been saved. Import
does not edit or delete the original. An intentionally empty saved catalog stays
empty. Invalid saved catalogs are reported instead of overwritten with defaults.
Keep the entire data folder together when making a manual backup.

## Verification and previews

```powershell
.\Run.ps1 -Test
.\Run.ps1 -Preview
```

The login uses the original cartoon mascot with yellow accents confined to the
login screens. The mascot and text stay fixed while bubbles and water waves move
in the background. Screen changes crossfade in the same window. Motion respects
the system animation preference and `-Dlaundry.reduceMotion=true`; there is no
on-screen pause control. Artwork and generation prompts are recorded in
`JavaProject/src/resources/brand/README.md`.

The tests use isolated temporary data and cover pricing, failed-save recovery,
catalog/settings persistence, image import/rendering and restart loading, stock,
reports, staff photos, operator history, analytics windows and comparisons, actual
mouse-wheel scrolling, order navigation, workspace layouts, logo
packaging, stationary login artwork, login-only yellow accents, animation timer
lifecycle, and duplicate input during screen transitions.
Add/edit catalog dialogs also support scrolling and narrow windows: images and
fields stack, paired fields become single rows, and Save/Cancel stay visible.
Dialog checks cover actual mouse-wheel movement, reaching the last field, keeping
draft values while resizing, and validation messages in a small window.
Preview renders of the actual Swing components are saved under `preview/` and use
isolated demonstration orders and profiles; they do not add data to your store.
Build files, previews, and runtime store data are excluded from Git.

The added checks cover repository replacement, checkout retry recovery, exact
threshold alerts, branch transfer reconciliation/concurrency, machine conflicts,
backup tampering/path checks, byte-identical isolated restore, restored-state restart,
custom-date UI behavior and 58/80 mm receipt wrapping/pagination. Preview data remains
isolated from live records.
