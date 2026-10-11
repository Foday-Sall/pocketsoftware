## Problem Statement

The bot works for its first features, but the live database has tables with **no features using them** (Attendance, Prescriptions, Bills, Inventory), there is **no email capability** at all, WhatsApp users get one-question-at-a-time registration instead of native forms, and cron only covers two jobs. Library and security gaps remain before this can run unattended for real schools/clinics/shops.

## Solution

Close every gap between the database and the product: full feature coverage for all 20 tables across the three systems, native WhatsApp Flows forms (via the Cloud API running alongside the existing GOWA bridge), email notifications through Jakarta Mail, three new cron triggers, and a security hardening pass. All features go through the existing classifier (ModelClassifier with keyword fallback) so every new command is available via free-typed chat AND menus.

## User Stories

### School
1. As a teacher, I want to mark a student present/absent/late for today, so that attendance is recorded without paper.
2. As a teacher, I want to ask "who is absent today", so that I get the list instantly.
3. As a teacher, I want to record a fee payment for a student, so that the school knows who has paid.
4. As a teacher, I want to ask "fees for Fatou", so that I see what was paid and what is owed.
5. As a school admin, I want a daily attendance summary at a set time, so that I see the school's attendance health without asking.
6. As a school admin, I want fee overdue reminders sent automatically, so that parents are nudged without staff effort.
7. As a teacher, I want an email sent to the guardian automatically when I enter marks, so that parents learn results the moment they exist.
8. As a school admin, I want guardian emails stored on the student record, so that notifications have a destination.

### Hospital
9. As a doctor/staff, I want to add a prescription to a visit (medicine, dosage, instructions), so that the visit record is complete.
10. As a staff member, I want to create a bill for a patient, so that amounts owed are tracked.
11. As a staff member, I want to record a payment against a patient's bill, so that balances stay accurate.
12. As a staff member, I want to ask "bill for [patient]", so that I see total billed, total paid, and balance.
13. As a staff member, I want an end-of-day summary of visits, bills, and payments, so that I can reconcile the day.

### Business
14. As a shop owner, I want to add inventory items with price/quantity/low-stock threshold, so that stock is tracked.
15. As a shop owner, I want to record stock in/out (sales should decrement automatically), so that quantities stay true.
16. As a shop owner, I want a low-stock alert each morning, so that I reorder before running out.
17. As a shop owner, I want to ask "stock of [item]", so that I know quantity on hand instantly.

### WhatsApp Flows (native forms via Cloud API)
18. As a parent, I want a single WhatsApp form to register my child (name, class, guardian contact), so that I don't answer questions one message at a time.
19. As a patient, I want a single form for registration (name, gender, age, address), so that registration takes one screen.
20. As a shop owner, I want a form for recording a sale (customer, item, amount, method), so that sales take seconds.
21. As a staff member, I want a form for booking an appointment (date, time, doctor), so that booking is tap-driven.
22. As an admin, I want flows that fall back gracefully: if the Cloud API is not configured, the chat Q&A flow still works exactly as today.

### Email
23. As an admin, I want to configure SMTP in config, so that email works with any provider (Gmail app-password, school SMTP).
24. As a teacher, I want emails to guardians to include the student's full report card summary, so that parents get real information.
25. As an admin, I want email failures logged in the audit log, so that delivery problems are visible.

### Security & reliability
26. As an admin, I want per-chat rate limiting, so that one user cannot flood the classifier or database.
27. As an admin, I want all user input length-validated before it reaches the database, so that oversized/abusive values are rejected.
28. As an admin, I want the webhook endpoint to require a shared secret, so that only our channels can post.
29. As an admin, I want secrets only in .env/config (never in code or repo), so that the public repo stays safe.

## Implementation Decisions

1. **Email: simple-java-mail over Jakarta Mail** (org.simplejavamail:simple-java-mail + com.sun.mail:jakarta.mail). Fluent API, ~10 lines to send, TLS/SSL config built in. No Spring.
2. **Students table gains guardianEmail** (string, optional), **Staff table gains email** — added at startup by the existing ColumnEnsurer, zero manual migration.
3. **WhatsApp Flows via Meta Cloud API running ALONGSIDE GOWA** (Baileys cannot do Flows). New channels/cloudapi/ package: send flow, receive flow-completion webhook, verify X-Hub-Signature-256. Requires Meta business verification + a Cloud API phone number; chat Q&A flows remain the fallback and the default until Cloud API is configured.
4. **Flow definitions as JSON resources** (Flow JSON format per Meta docs), one per form; flow submission maps 1:1 onto the same stores the chat flows use (no duplicate write logic).
5. **New features follow the existing one-job-per-file pattern**: MarkAttendance, Fees (extended), AddPrescription, Billing, Inventory, plus stores where needed — each under 100 lines, records for data.
6. **New Actions added to the classifier**: mark_attendance, attendance_report, fee payment/queries, prescription, bill, pay bill, inventory add/stock/low. Keywords added to KeywordClassifier so commands work even if the classifier is down.
7. **New cron triggers** (all via existing DailyJob): morning low-stock alert (business), fee overdue reminder (school), end-of-day hospital summary. Config keys like cron.lowstock=08:00.
8. **Rate limiting**: simple token bucket per chat in the Router (no library needed), configurable.
9. **Input validation**: length caps at store level matching Appwrite column sizes.
10. **PDF upgrade**: keep OpenPDF; add class-average and per-subject breakdown to report cards.
11. **JSON**: keep minimal-json (tiny, zero-dep). No Jackson/Gson — not worth the weight.

## Testing Decisions

- Tests cover external behavior only: store CRUD against test doubles of the HTTP layer, classifier keyword coverage for every new action, rate limiter behavior, and flow-JSON well-formedness.
- Follow existing patterns: KeywordClassifierTest for keyword coverage, QueryTest for query building, ModulesTest for wiring.
- CI (GitHub Actions) remains the compiler and test runner; no local JDK.

## Out of Scope

- Full migration off GOWA/Baileys (parallel run instead).
- Online payments / mobile money integration.
- Multi-tenant school/clinic support (one deployment = one institution).
- A web dashboard.

## Further Notes

- Live database audit done 2026-10-11: school 10 tables, hospital 6, business 4. Gaps confirmed by code search: Attendance, Prescriptions, Bills, Inventory have zero feature code; Fees has partial coverage.
- Cloud API Flows need: Meta business verification, a dedicated phone number, a public webhook (WebServer + tunnel infrastructure exists).
- Everything ships as small PRs: (1) DB columns + email lib + email-after-marks, (2) school attendance + fees complete, (3) hospital prescriptions + billing, (4) business inventory + low-stock cron, (5) hardening, (6) Cloud API Flows.