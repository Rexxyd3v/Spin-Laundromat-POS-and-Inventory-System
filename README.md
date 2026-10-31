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

Owner and Manager workspaces open on **Overview**; Cashier workspaces open on the POS. Select the last 7 or 30 days to update the
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

## Local sign-in and accounts

Sign-in is now **Master Key → username/password → workspace**. A new installation
shows an Owner setup form; there is no built-in login bypass or hard-coded default
password. For explicit first-time sample setup, run `./Run.ps1 -SampleAccounts`.
It creates `owner`, `manager`, and `cashier` with unique randomly generated temporary
passwords and a random master key, displayed once in the terminal. It refuses to
replace existing accounts. Sample passwords must be changed before entering the
workspace for the first time. Credentials are case-sensitive; usernames ignore case.

The Owner can open **Settings → Accounts & access** to create accounts, change roles,
disable access, reset a temporary password, or change the master key. At least one
Owner must remain active. Resets, role/status changes and master-key changes revoke
existing sessions; the window checks for revocation every two seconds and writes
check authorization immediately. The profile menu lets each signed-in user change
their own password. **Sign out** clears the session and master-key unlock.

| Role | Access |
| --- | --- |
| Owner | All pages, accounts, store/tax settings, backups and operational actions |
| Manager | Analytics, POS, catalog/stock editing, operations, reports, team viewing and printer settings |
| Cashier | POS, catalog/stock viewing, local printer settings and own profile/password |

Profiles and authentication are linked by ID. Changing a profile label cannot grant
account permissions; switching to another user requires a new sign-in. **Team**
manages display names, contact details and photos; **Accounts & access** manages the
actual sign-in permissions. Profile-only legacy members do not automatically gain
login access. Existing Owner profile photos and business records are retained.

The local `JavaProject/data/authentication.properties` file contains independently
salted PBKDF2-HMAC-SHA256 hashes (600,000 iterations) for passwords and the master key,
plus account metadata and persistent failed-attempt limits. It contains no plaintext
passwords. Five failed attempts temporarily lock that account or master-key entry
for one minute. Unknown and unavailable accounts share the same error message.
Corrupt account files fail closed and are never silently reset. The old
`masterkey.key` is not used or imported into authentication.

Sessions exist only in memory and expire when the app closes or the user signs out.
The account file is excluded from Git and included in full local backups; keep backups
private and retain the current master key separately. This is local application access
control for one workstation, not encrypted business data or multi-device online auth.
People who can modify the application/data files through Windows can bypass local
application controls. Supabase authentication will be a separate migration.

Password storage follows the [OWASP password storage guidance](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html).

## Sidebar name and profile photo

Click the sidebar profile card, then **Edit profile**. Choose a PNG/JPEG photo, update
the name and save. The authenticated profile's name, role and circular photo appear
in the sidebar and top bar. Initials appear until a photo is provided. Owners can
also edit team display profiles. Each saved order records the verified operator ID,
username and name, so changing a profile later does not change old receipts.

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
- Authenticated local accounts, role permissions, password changes and resets, plus
  saved staff profiles, contact details and photos.

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
Custom ranges support 1–366 days; date inputs appear only for Custom range.
The dashboard uses adaptive rows with a compact secondary totals strip. Navigation
automatically collapses on smaller windows, and its scrollbar uses a dark track. Revenue remains the tax-inclusive confirmed sale
total; cash collections here are fully paid Cash checkouts, not shift settlement.
No cost or profit estimate is invented. Receipt preview/reprint shares **58/80 mm**
printing, wraps long text and paginates. Configure the device in **Settings → Receipt printer**. New receipts snapshot date, branch and operator when confirmed;
historical reprints retain that text. Printer failures do not create another sale.

## Current boundaries

Local credential verification and role checks are active. Account storage is local
to this workstation; it does not yet provide online identity or branch membership.
GCash is manually verified by the cashier.

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
lifecycle, and duplicate input during screen transitions. Authentication checks cover
wrong/empty credentials, mandatory password replacement, role restrictions in the UI
and data layer, profile impersonation, account revocation, persistent lockout, fresh
process sign-in, sample provisioning and preservation of existing records.
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

Native Swing validation checks also cover populated dashboard cards, non-overlapping
sections, small-window navigation and the actual painted sidebar gutter at
1366×768, 1024×650 and 940×680.

## Thermal printer setup

1. Connect the thermal printer and install its Windows driver (USB or network queue).
2. Open **Settings → Receipt printer** and click **Find printers**. Type part of the
   printer name to filter the installed queues, then select the intended device.
3. Configure **58/80 mm paper**, all four margins, text size and extra paper feed.
   Review the live preview. Short receipts use content-based length; long receipts
   paginate, subject to the installed driver's supported paper settings.
4. Use **Test print** to check alignment and the peso symbol. This is a sample, not
   a sale. A test print always offers the system print dialog and only sends a job
   after the user confirms it. A submitted job still needs checking on the paper.
5. Click **Save printer**. Checkout and historical receipt printing use the saved
   queue and layout. The optional print dialog can remain enabled for each receipt.

This discovery lists installed 2D print services; it does not install drivers or scan
for unconfigured network devices. An explicitly saved missing printer does not
silently redirect receipts to another queue. Status reflects what the driver reports;
not all drivers report physical readiness. Paper cutting and cash-drawer commands
are model-specific and are not configured by this graphics-based driver path.
[Java printer selection and page validation](https://docs.oracle.com/en/java/javase/21/docs/api/java.desktop/java/awt/print/PrinterJob.html).

Device preferences live in `printer.properties`, separate from store appearance and
tax settings. Old installations use their previous receipt width until a device
profile is saved. Backups include this file; review the saved queue after restoring
onto another PC. Invalid printer settings are reported without overwriting them.

The notification flyout now has Low stock/Out of stock filters, a branch selector,
refresh, quantity/threshold cards, last-update time and per-item inventory actions.
Selecting an item's arrow opens its branch and selects that product in Inventory.
An open flyout updates when stock changes and keeps its selected branch when an
alert resolves. Alerts are actionable balances rather than dismissible messages;
restocking above the threshold clears them.

## Sample catalog photos

Eight generic sample photos are bundled under `JavaProject/src/resources/catalog-samples`.
The installer fills only missing photos for the standard Wash/Dry/Full/Fold services
and four starter products. Existing uploads and item details are preserved. Product
photos are unbranded samples, not official brand images. Replace them using
Catalog → Edit item → Choose image.

Run `.\Run.ps1 -SampleImages` to explicitly apply missing samples again. The catalog
is backed up in `.cleanup-backups` before an installation that changes images.
Normal launches never automatically refill photos you have removed. Generation
prompts, asset names and tool details are in the sample folder's README.
