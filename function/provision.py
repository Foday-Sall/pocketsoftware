#!/usr/bin/env python3
"""Provision the blueprint schema on the Appwrite VPS.

Creates/updates (idempotent, GET-before-POST):
  - platform.bot_actions (intent -> routing registry) + seeds
  - school.students: adds current_grade / registration_status / attendance_percentage
  - school.bot_templates + seed templates
  - hospital.patients: adds medication_status / appointment_time / billing_ledger
  - hospital.bot_templates + seed templates
  - business.car_rentals + business.farmers (blueprint schema)
  - business.bot_templates + seed templates
  - platform.bot_templates: unknown / set_reminder / reminder templates

Run on the VPS: python3 /tmp/provision.py
"""
import json
import time
import urllib.request

KEY = open("/root/.engine_key").read().strip()
PROJ = "6acb2818001e5f0cc411"
BASE = "http://localhost/v1"


def req(path, method="GET", body=None):
    data = json.dumps(body).encode() if body is not None else None
    r = urllib.request.Request(
        BASE + path, data=data, method=method,
        headers={"X-Appwrite-Project": PROJ, "X-Appwrite-Key": KEY,
                 "Content-Type": "application/json"})
    try:
        return json.loads(urllib.request.urlopen(r, timeout=30).read())
    except urllib.error.HTTPError as e:
        try:
            return {"__error__": e.code, "body": json.loads(e.read())}
        except Exception:
            return {"__error__": e.code}
    except Exception as e:
        return {"__error__": str(e)}


def wait_table(db, tid):
    for _ in range(40):
        d = req(f"/tablesdb/{db}/tables/{tid}")
        if d.get("status") == "available":
            return True
        time.sleep(1)
    return False


def ensure_table(db, tid, name):
    existing = req(f"/tablesdb/{db}/tables/{tid}")
    if "__error__" not in existing:
        print(f"table {db}.{tid}: exists")
        return True
    d = req(f"/tablesdb/{db}/tables", "POST", {"tableId": tid, "name": name})
    if "__error__" in d:
        print(f"table {db}.{tid}: ERROR {d}")
        return False
    print(f"table {db}.{tid}: created")
    return wait_table(db, tid)


def add_column(db, tid, key, size=16384):
    cols = req(f"/tablesdb/{db}/tables/{tid}/columns")
    names = {c.get("key") for c in cols.get("columns", [])} if "__error__" not in cols else set()
    if key in names:
        print(f"  {db}.{tid}.{key}: exists")
        return
    d = req(f"/tablesdb/{db}/tables/{tid}/columns/string", "POST",
            {"key": key, "type": "string", "size": size, "required": False})
    if "__error__" in d:
        print(f"  {db}.{tid}.{key}: ERROR {d}")
    else:
        # wait until available before next call
        for _ in range(40):
            c = req(f"/tablesdb/{db}/tables/{tid}/columns/{key}")
            if c.get("status") == "available":
                break
            time.sleep(1)
        print(f"  {db}.{tid}.{key}: created")


def seed_row(db, tid, row_id, data):
    existing = req(f"/tablesdb/{db}/tables/{tid}/rows/{row_id}")
    if "__error__" not in existing:
        print(f"  row {db}.{tid}/{row_id}: exists")
        return
    d = req(f"/tablesdb/{db}/tables/{tid}/rows", "POST", {"rowId": row_id, "data": data})
    print(f"  row {db}.{tid}/{row_id}:", "seeded" if "__error__" not in d else f"ERROR {d}")


def ensure_templates(db, templates):
    """templates: list of (template_key, body)"""
    if not ensure_table(db, "bot_templates", "Bot Templates"):
        return
    add_column(db, "bot_templates", "tenant_id", 64)
    add_column(db, "bot_templates", "template_key", 128)
    add_column(db, "bot_templates", "body", 16384)
    idx = req(f"/tablesdb/{db}/tables/bot_templates/indexes")
    names = {i.get("key") for i in idx.get("indexes", [])} if "__error__" not in idx else set()
    if "tpl_key_idx" not in names:
        d = req(f"/tablesdb/{db}/tables/bot_templates/indexes", "POST",
                {"key": "tpl_key_idx", "type": "key", "attributes": ["template_key"]})
        print(f"  {db}.bot_templates index:", "created" if "__error__" not in d else d)
    for key, body in templates:
        seed_row(db, "bot_templates", key.replace("_", "-")[:36], {
            "tenant_id": "main", "template_key": key, "body": body})


def main():
    # ---------------- 1. platform.bot_actions registry
    print("== platform.bot_actions ==")
    ensure_table("platform", "bot_actions", "Bot Actions")
    add_column("platform", "bot_actions", "template_key", 128)
    add_column("platform", "bot_actions", "target_database_id", 64)
    add_column("platform", "bot_actions", "target_collection", 128)
    add_column("platform", "bot_actions", "data_field_to_read", 256)
    idx = req("/tablesdb/platform/tables/bot_actions/indexes")
    names = {i.get("key") for i in idx.get("indexes", [])} if "__error__" not in idx else set()
    if "template_key_idx" not in names:
        d = req("/tablesdb/platform/tables/bot_actions/indexes", "POST",
                {"key": "template_key_idx", "type": "key", "attributes": ["template_key"]})
        print("  index:", "created" if "__error__" not in d else d)

    actions = [
        # key, target_db, target_table, data_field_to_read
        ("check_grades",        "school",   "students",     "current_grade"),
        ("check_attendance",    "school",   "students",     "attendance_percentage"),
        ("check_registration",  "school",   "students",     "registration_status"),
        ("register_student",    "school",   "students",     "fullName,current_grade"),
        ("check_prescription",  "hospital", "patients",     "medication_status"),
        ("check_appointment",   "hospital", "patients",     "appointment_time"),
        ("check_billing",       "hospital", "patients",     "billing_ledger"),
        ("register_patient",    "hospital", "patients",     "fullName,medication_status"),
        ("rent_car",            "business", "car_rentals",  "vehicle_model,rental_status,return_timer"),
        ("check_return",        "business", "car_rentals",  "return_timer"),
        ("register_farmer",     "business", "farmers",      "farmer_name,crop_type,field_id"),
        ("check_yield",         "business", "farmers",      "yield_weight"),
    ]
    for key, db, table, field in actions:
        seed_row("platform", "bot_actions", key, {
            "template_key": key, "target_database_id": db,
            "target_collection": table, "data_field_to_read": field})

    # ---------------- 2. school schema
    print("== school ==")
    add_column("school", "students", "current_grade", 64)
    add_column("school", "students", "registration_status", 32)
    add_column("school", "students", "attendance_percentage", 32)
    ensure_templates("school", [
        ("check_grades", "Hi {fullName}, your current grade is {value}."),
        ("check_attendance", "Hi {fullName}, your attendance is {value}%."),
        ("check_registration", "Hi {fullName}, your registration status: {value}."),
        ("register_student_ask_fullName", "Let's register a student. What is their full name?"),
        ("register_student_ask_current_grade", "What is their current grade?"),
        ("register_student_confirm", "Confirm this student?\n{summary}\nYES / NO"),
        ("register_student_saved", "Student saved:\n{summary}"),
        ("register_student_cancelled", "Registration cancelled."),
        ("check_grades_missing", "No student record found for this number."),
        ("check_attendance_missing", "No student record found for this number."),
        ("check_registration_missing", "No student record found for this number."),
    ])

    # ---------------- 3. hospital schema
    print("== hospital ==")
    add_column("hospital", "patients", "medication_status", 128)
    add_column("hospital", "patients", "appointment_time", 64)
    add_column("hospital", "patients", "billing_ledger", 256)
    ensure_templates("hospital", [
        ("check_prescription", "Hi {fullName}, your medication status: {value}."),
        ("check_appointment", "Hi {fullName}, your next appointment: {value}."),
        ("check_billing", "Hi {fullName}, your billing ledger: {value}."),
        ("register_patient_ask_fullName", "Let's register a patient. What is their full name?"),
        ("register_patient_ask_medication_status", "What is their current medication?"),
        ("register_patient_confirm", "Confirm this patient?\n{summary}\nYES / NO"),
        ("register_patient_saved", "Patient saved:\n{summary}"),
        ("register_patient_cancelled", "Registration cancelled."),
        ("check_prescription_missing", "No patient record found for this number."),
        ("check_appointment_missing", "No patient record found for this number."),
        ("check_billing_missing", "No patient record found for this number."),
    ])

    # ---------------- 4. business schema (blueprint tables)
    print("== business ==")
    if ensure_table("business", "car_rentals", "Car Rentals"):
        add_column("business", "car_rentals", "phone", 32)
        add_column("business", "car_rentals", "vehicle_model", 128)
        add_column("business", "car_rentals", "rental_status", 64)
        add_column("business", "car_rentals", "return_timer", 64)
    if ensure_table("business", "farmers", "Farmers"):
        add_column("business", "farmers", "phone", 32)
        add_column("business", "farmers", "farmer_name", 128)
        add_column("business", "farmers", "crop_type", 128)
        add_column("business", "farmers", "yield_weight", 32)
        add_column("business", "farmers", "field_id", 64)
    ensure_templates("business", [
        ("rent_car_ask_vehicle_model", "Let's rent a car. Which vehicle model?"),
        ("rent_car_ask_rental_status", "Rental status (e.g. rented, returned)?"),
        ("rent_car_ask_return_timer", "Return timer (e.g. 3 days)?"),
        ("rent_car_confirm", "Confirm this rental?\n{summary}\nYES / NO"),
        ("rent_car_saved", "Rental saved:\n{summary}"),
        ("rent_car_cancelled", "Rental cancelled."),
        ("check_return", "Return timer for {vehicle_model}: {value}."),
        ("check_return_missing", "No rental found for this number."),
        ("register_farmer_ask_farmer_name", "Let's register a farmer. What is their name?"),
        ("register_farmer_ask_crop_type", "What crop do they grow?"),
        ("register_farmer_ask_field_id", "Which field (ID)?"),
        ("register_farmer_confirm", "Confirm this farmer?\n{summary}\nYES / NO"),
        ("register_farmer_saved", "Farmer saved:\n{summary}"),
        ("register_farmer_cancelled", "Registration cancelled."),
        ("check_yield", "Hi {farmer_name}, your yield: {value} kg."),
        ("check_yield_missing", "No farmer record found for this number."),
    ])

    # ---------------- 5. platform templates
    print("== platform templates ==")
    ensure_templates("platform", [
        ("unknown", "Sorry, I didn't understand that. Try: check grades, attendance, "
                    "appointment, billing, rent a car, or say register."),
        ("set_reminder", "Reminder set for {time}."),
        ("reminder", "Reminder: {text}"),
    ])

    print("\nDone. Schema and seeds are in place.")


if __name__ == "__main__":
    main()
