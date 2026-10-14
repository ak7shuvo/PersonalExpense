# PersonalExpense

Offline personal finance tracker for Android. Currency: Bangladeshi Taka (৳). No account, no network, **no permissions**.

- Dashboard: current balance, income, expenses, money lent, repayments received, outstanding loans
- Add income (source), expenses (category), money lent (person), full or partial repayments
- History with edit and delete; light and dark themes follow the system
- Kotlin · Jetpack Compose · Material 3 · Room · MVVM · Gradle Kotlin DSL · JDK 17 · minSdk 26

## Money rules

| Action | Balance | Income | Expenses | Outstanding |
|---|---|---|---|---|
| Income | + | + | | |
| Expense | − | | + | |
| Lend | − | | | + |
| Repayment | + | | | − |

Lending is not an expense; a repayment is not income. `balance + outstanding = income − expenses`, so lending and
getting repaid never change total wealth. Amounts are stored as `Long` poisha (1 ৳ = 100) and parsed with `BigDecimal`,
never `Double`. A repayment can't exceed what is still owed, and a loan can't be edited below what has been repaid.
Deleting a loan also deletes its repayments. Transfers between accounts are not supported (only one pot of money exists).

Pure logic lives in `app/src/main/java/.../domain` (no Android types) so it is unit-tested directly.

## Develop

Requirements: JDK 17, Android SDK (platform 35, build-tools 34.0.0). Android Studio is optional.

```bash
./gradlew testDebugUnitTest      # unit tests (domain rules + Room persistence via Robolectric)
./gradlew lintDebug              # Android lint
./gradlew assembleDebug          # app/build/outputs/apk/debug/app-debug.apk
```

## Get an APK from GitHub

**Every push to `main`** (or **Actions → Android CI & Release → Run workflow**) builds a debug APK:

1. Open the repo → **Actions** → latest *Android CI & Release* run.
2. Under **Artifacts**, download **PersonalExpense-debug** (a zip containing `PersonalExpense-debug.apk`).
3. Unzip, copy to the phone, open it and allow "install unknown apps" for your file manager/browser.

Debug APKs are signed with an auto-generated *debug* key. Fine for personal testing, not a release.

## Release

```bash
git checkout main && git pull
git tag v1.0.0
git push origin v1.0.0
```

The tag must look like `v1.2.3` (or `v1.2.3-rc.1`, published as a pre-release) and must point at a commit on the default
branch. The workflow then runs tests and lint, builds the release APK, and creates the GitHub Release `v1.0.0` with
generated notes and the APK attached. `versionName` comes from the tag; `versionCode = major·1,000,000 + minor·1,000 + patch`.
Re-running a tag's workflow refreshes that release's assets instead of creating a duplicate; runs for one tag are serialised.

Download from **Releases** → the tag → *Assets*.

### Signing (important)

| Secrets configured | Release asset | Installable? |
|---|---|---|
| none | `PersonalExpense-v1.0.0-unsigned.apk` | **No.** Android refuses unsigned APKs. Release notes say so. |
| all four | `PersonalExpense-v1.0.0.apk`, verified with `apksigner` | Yes |
| some but not all | build **fails** | |

Unsigned is the default so the pipeline works without private keys. It is never silently signed with the debug key.
To get installable releases, create a keystore once and keep it safe (losing it means you can't ship updates):

```bash
keytool -genkeypair -v -keystore release.jks -alias personalexpense \
  -keyalg RSA -keysize 4096 -validity 10000
base64 -w0 release.jks     # macOS: base64 -i release.jks
```

Add these under **Settings → Secrets and variables → Actions → New repository secret**:

| Secret | Value |
|---|---|
| `ANDROID_KEYSTORE_BASE64` | output of `base64` above |
| `ANDROID_KEYSTORE_PASSWORD` | keystore password |
| `ANDROID_KEY_ALIAS` | `personalexpense` (the alias you chose) |
| `ANDROID_KEY_PASSWORD` | key password |

Never commit `*.jks`/`*.keystore` (they are git-ignored). Locally, signing uses the env vars `PE_KEYSTORE_FILE`,
`PE_KEYSTORE_PASSWORD`, `PE_KEY_ALIAS`, `PE_KEY_PASSWORD`; the same all-or-nothing rule applies.

## Limitations

- Release builds are not minified/obfuscated.
- No export/backup: data lives only in the app's private storage (auto-backup is disabled for privacy), so uninstalling erases it.
- One currency (৳), one pot of money, no multi-account transfers.
