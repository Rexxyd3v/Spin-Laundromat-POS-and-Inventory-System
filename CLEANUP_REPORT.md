# Pre-migration cleanup report

Historical cleanup milestone (before the subsequent feature additions).
Validation counts and source counts below describe that milestone; see README.md
for the current features.

Date: 31 October 2026. Active source root: `JavaProject/src`.

## Result

Removed **28 unused Java source files (4,929 lines)** from the active source
and the unused alternate mascot. The application now has **42 production Java
source files**, down from 70. The approved original mascot and active UI remain.

The obsolete files are recoverable from the local archive
`.cleanup-backups/legacy-code-2026-10-31.zip`. Its contents were SHA-256 checked against the source
before removal. The full manifest is next to it in
`.cleanup-backups/legacy-code-2026-10-31-manifest.json`.
The backup directory is ignored by Git and must be copied separately if moving
this workspace before deciding whether to retain the legacy archive long term.

## How removal was checked

1. Followed source references from Main and every test/preview entry point.
2. Confirmed both VS Code launch configurations use Main. Production code has no
   dynamic Java class loader or service-loader registration for the retired screens;
   resource loading uses the selected v1 mascot directly.
3. Compiled retained sources/tests in an independent preflight output with all 28
   obsolete sources excluded; ran all 169 existing checks there successfully.
4. Created and verified the recovery archive before removing source files.
5. Rebuilt the cleaned active tree and ran the same checks and actual UI previews.
   Final validation status is recorded below.

The first bulk deletion request was rejected by automatic approval review because
reference heuristics alone were insufficient. Cleanup proceeded only after the
independent compile/tests and verified archive made the removal recoverable.

## Removed source inventory

| File | Reason |
| --- | --- |
| `JavaProject/src/model/Staff.java` | Retired user/profile model or UI; replaced by TeamStore, ActiveProfile and the current Team/profile dialogs |
| `JavaProject/src/model/User.java` | Retired user/profile model or UI; replaced by TeamStore, ActiveProfile and the current Team/profile dialogs |
| `JavaProject/src/util/components/ShadowPanel.java` | Used only by retired screens; current surfaces use ModernUI |
| `JavaProject/src/ui/frames/Dashboard.java` | Unused standalone launcher or wrapper; the supported Main/LoginWindow/WorkspacePanel flow handles it |
| `JavaProject/src/ui/frames/StaffLogin.java` | Unused standalone launcher or wrapper; the supported Main/LoginWindow/WorkspacePanel flow handles it |
| `JavaProject/src/ui/panels/dashboardPanel/DashboardPanel.java` | Unused standalone launcher or wrapper; the supported Main/LoginWindow/WorkspacePanel flow handles it |
| `JavaProject/src/ui/panels/inventoryPanel/AddProduct.java` | Retired inventory/dialog path; current CatalogEditor and BusinessPages.Inventory are used instead |
| `JavaProject/src/ui/panels/inventoryPanel/EditProduct.java` | Retired inventory/dialog path; current CatalogEditor and BusinessPages.Inventory are used instead |
| `JavaProject/src/ui/panels/inventoryPanel/EntityManagerDialog.java` | Retired inventory/dialog path; current CatalogEditor and BusinessPages.Inventory are used instead |
| `JavaProject/src/ui/panels/inventoryPanel/InventoryManagementPanel.java` | Retired inventory/dialog path; current CatalogEditor and BusinessPages.Inventory are used instead |
| `JavaProject/src/ui/panels/inventoryPanel/LaundryInventorySystem.java` | Retired inventory/dialog path; current CatalogEditor and BusinessPages.Inventory are used instead |
| `JavaProject/src/ui/panels/inventoryPanel/NotificationDialog.java` | Retired inventory/dialog path; current CatalogEditor and BusinessPages.Inventory are used instead |
| `JavaProject/src/ui/panels/inventoryPanel/StockManagementPanel.java` | Retired inventory/dialog path; current CatalogEditor and BusinessPages.Inventory are used instead |
| `JavaProject/src/ui/panels/inventoryPanel/UpdateStockDialog.java` | Retired inventory/dialog path; current CatalogEditor and BusinessPages.Inventory are used instead |
| `JavaProject/src/ui/panels/reportPanel/LaundryReportsSystem.java` | Retired report/receipt UI; current Reports and saved-order receipt flow are used instead |
| `JavaProject/src/ui/panels/reportPanel/ReceiptPanel.java` | Retired report/receipt UI; current Reports and saved-order receipt flow are used instead |
| `JavaProject/src/ui/panels/settingsPanel/SettingAlertsTab.java` | Obsolete standalone settings tab; replaced by the active SettingsPanel |
| `JavaProject/src/ui/panels/settingsPanel/SettingBackupTab.java` | Obsolete standalone settings tab; replaced by the active SettingsPanel |
| `JavaProject/src/ui/panels/settingsPanel/SettingReceiptTab.java` | Obsolete standalone settings tab; replaced by the active SettingsPanel |
| `JavaProject/src/ui/panels/settingsPanel/SettingSecurityTab.java` | Obsolete standalone settings tab; replaced by the active SettingsPanel |
| `JavaProject/src/ui/panels/settingsPanel/SettingStoreTab.java` | Obsolete standalone settings tab; replaced by the active SettingsPanel |
| `JavaProject/src/ui/panels/userPanel/AddUserDialog.java` | Retired user/profile model or UI; replaced by TeamStore, ActiveProfile and the current Team/profile dialogs |
| `JavaProject/src/ui/panels/userPanel/EditUserDialog.java` | Retired user/profile model or UI; replaced by TeamStore, ActiveProfile and the current Team/profile dialogs |
| `JavaProject/src/ui/panels/userPanel/UserManagementPanel.java` | Retired user/profile model or UI; replaced by TeamStore, ActiveProfile and the current Team/profile dialogs |
| `JavaProject/src/ui/panels/userPanel/UserTableModel.java` | Retired user/profile model or UI; replaced by TeamStore, ActiveProfile and the current Team/profile dialogs |
| `JavaProject/src/ui/panels/userPanel/UserTableRenderers.java` | Retired user/profile model or UI; replaced by TeamStore, ActiveProfile and the current Team/profile dialogs |
| `JavaProject/src/util/components/BrandIcons.java` | Legacy image-name compatibility registry; its remaining logout call now uses AppIcon directly |
| `JavaProject/src/util/config/config.java` | Unused legacy file-path constants; active storage uses AtomicProperties |

## Other cleanup

- Removed the unused `laundry-mascot-v2.png` from packaged resources; retained the
  original image and both generation prompts/documentation.
- Removed unused UIStyle color aliases and the abandoned login-badge colors.
- Removed generated `JavaProject/bin` and the separate preflight output; the IDE
  can regenerate its bin directory. Added bin and cleanup backups to .gitignore.
- Run.ps1 now verifies the build output stays within the workspace and cleans its
  class output before compiling, preventing removed classes from surviving there.
- Added ignore patterns for local environment files and legacy credential paths.
- Replaced the outdated enhancement roadmap with current-state findings, feature
  priorities, delivery dependencies and Supabase readiness checks.
- Corrected the storage plan's free-tier egress distinction and backup strategy.

## Preserved data and intentional files

Both `data` folders, existing transaction CSV, both `services.properties` files,
local catalog/settings/team/profile records, legacy key file and uploaded images
were preserved. All **10 existing data/import files** match their pre-cleanup
SHA-256 values. The selected v1 mascot also matches its original checksum.

The old data copies are intentionally retained until a migration audit identifies
source branch, duplicate IDs and import provenance. They are not redundant build
artifacts. Current file stores and legacy-service import support are retained until
staging import and rollback are verified. No Supabase connection or migration ran.

The legacy key file was already tracked by Git. Adding an ignore pattern does not
remove an already tracked file or its history. Credential handling and removal from
future source-control tracking must be addressed when real Auth is introduced;
the current preview screens do not verify this key.

Tests and preview generators remain because they verify pricing, persistence,
images, scrolling, profiles, transitions and responsive add/edit dialogs. Current
previews and normal build output remain generated/ignored rather than committed.

## Recovery

Close any running application before restoring legacy files. Extract the archive
into an isolated folder first and compare the manifest/checksums. Each zip entry
retains its original repository-relative path. Copy only the file(s) required for
review back to the matching path; do not restore an entire legacy UI over the active
screens or runtime data. These sources are historical reference, not an alternate
supported application entry point.

## Final validation

Cleaned-tree `Run.ps1 -Test -Preview` completed successfully:

- 27 POS/pricing/persistence checks.
- 29 catalog/settings/image/restart checks.
- 38 analytics/scrolling/profile/cart-flow checks.
- 25 login/artwork/transition checks.
- 50 add/edit dialog resizing/scrolling/draft checks.
- Actual Swing previews generated for all workspace pages, checkout, profiles,
  login and responsive add/edit dialogs.

Total: **169 passing checks**. Compilation targets Java 21. The selected mascot
and all 10 existing data/import files passed checksum verification after the run.
Old generated test data and one-off formatting/preview helpers were removed from
`.build`; its normal compiled application/test output remains ignored.
