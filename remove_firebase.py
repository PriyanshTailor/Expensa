import os
import re

base_dir = r"android_java\app\src\main\java\com\smartexpense\mobile"

replacements = {
    "AddBillBottomSheet.java": [
        ("import com.google.firebase.firestore.FirebaseFirestore;", "import com.smartexpense.mobile.network.*;\nimport retrofit2.*;\nimport java.util.Map;"),
        ("FirebaseFirestore db = FirebaseFirestore.getInstance();", ""),
        ("db.collection(\"users\").document(uid).collection(\"bills\").document(docId).set(data)", "/* TODO Backend Call */ RetrofitClient.getClient().create(ApiService.class).createTransaction(data)"),
    ],
    "AddTripBottomSheet.java": [
        ("import com.google.firebase.firestore.FirebaseFirestore;", ""),
        ("private FirebaseFirestore db;", ""),
        ("db = FirebaseFirestore.getInstance();", ""),
        ("db.collection(\"Trips\").add(tripMap)", "RetrofitClient.getClient().create(ApiService.class).createTrip(tripMap)"),
    ],
    "AddTripExpenseBottomSheet.java": [
        ("import com.google.firebase.firestore.FirebaseFirestore;", ""),
        ("import com.google.firebase.firestore.FieldValue;", ""),
        ("private FirebaseFirestore db;", ""),
        ("db = FirebaseFirestore.getInstance();", ""),
        ("db.collection(\"Trips\").document(tripId)", "/* TODO Replace */"),
    ],
    "AnalyticsFragment.java": [
        ("import com.google.firebase.auth.FirebaseAuth;", ""),
        ("import com.google.firebase.firestore.FirebaseFirestore;", ""),
        ("import com.google.firebase.firestore.QueryDocumentSnapshot;", ""),
        ("private FirebaseFirestore db;", ""),
        ("db = FirebaseFirestore.getInstance();", ""),
        ("db.collection(\"users\").document(uid).collection(\"expenses\")", "/* Firebase Removed */"),
        ("for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {", "for (Object doc : new java.util.ArrayList<>()) {"),
    ],
    "BillsFragment.java": [
        ("import com.google.firebase.firestore.DocumentSnapshot;", ""),
        ("import com.google.firebase.firestore.FirebaseFirestore;", ""),
        ("FirebaseFirestore.getInstance().collection(\"users\").document(uid).collection(\"bills\")", "/* Firebase Removed */"),
        ("for (DocumentSnapshot doc : queryDocumentSnapshots)", "for (Object doc : new java.util.ArrayList<>())"),
        ("FirebaseFirestore db = FirebaseFirestore.getInstance();", ""),
        ("new com.google.firebase.Timestamp(new java.util.Date())", "new java.util.Date().getTime()"),
    ],
    "TripDetailFragment.java": [
        ("import com.google.firebase.firestore.FirebaseFirestore;", ""),
        ("private FirebaseFirestore db;", ""),
        ("db = FirebaseFirestore.getInstance();", ""),
    ],
    "sms\\SmsReceiver.java": [
        ("import com.google.firebase.auth.FirebaseAuth;", ""),
        ("import com.google.firebase.auth.FirebaseUser;", ""),
        ("import com.google.firebase.firestore.FirebaseFirestore;", ""),
        ("FirebaseFirestore db = FirebaseFirestore.getInstance();", ""),
        ("new com.google.firebase.Timestamp(new java.util.Date(timestamp))", "timestamp"),
        ("com.google.firebase.firestore.DocumentSnapshot doc = task.getResult();", ""),
    ]
}

for filename, rules in replacements.items():
    filepath = os.path.join(base_dir, filename)
    if os.path.exists(filepath):
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
        for old, new in rules:
            content = content.replace(old, new)
        # generic firebase imports
        content = re.sub(r'import com\.google\.firebase\..*;\n', '', content)
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Updated {filename}")
