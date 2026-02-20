import csv
import random
from datetime import datetime, timedelta

# =====================================
# CONFIG
# =====================================

NUM_RECEIPTS = 999

MAX_DRINK_ID = 14   # change to match your drink table
MAX_FOOD_ID = 8     # change to match your food table

RECEIPT_FILE = "data/receipt.csv"
DRINK_FILE = "data/drink_to_receipt.csv"
FOOD_FILE = "data/food_to_receipt.csv"

# =====================================
# HELPERS
# =====================================

def weighted_customer_id():
    # 60% anonymous
    if random.random() < 0.6:
        return 0
    return random.randint(1, 39)

def random_cashier_id():
    return random.randint(0, 9)

def random_timestamp():
    now = datetime.now()
    past = now - timedelta(days=600)

    random_seconds = random.randint(0, int((now - past).total_seconds()))
    return past + timedelta(seconds=random_seconds)

def random_ice():
    return random.choice(["No Ice", "Less Ice", "Regular", "Extra Ice"])

def random_sweetness():
    return random.choice([0, 25, 50, 75, 100])

def random_milk():
    return random.choice(["Whole", "2%", "Oat", "Almond", "None"])

def random_boba():
    return random.choice([None, "Tapioca", "Crystal"])

def random_popping():
    return random.choice([None, "Mango", "Strawberry", "Lychee"])

def random_food_modifier():
    return random.choice([None, "No onions", "Extra sauce", "Spicy", "No mayo"])

# =====================================
# GENERATE FILES
# =====================================

with open(RECEIPT_FILE, "w", newline="") as r_file, \
     open(DRINK_FILE, "w", newline="") as d_file, \
     open(FOOD_FILE, "w", newline="") as f_file:

    receipt_writer = csv.writer(r_file)
    drink_writer = csv.writer(d_file)
    food_writer = csv.writer(f_file)

    # Headers
    receipt_writer.writerow(["id", "customer_id", "cashier_id", "purchase_date"])
    drink_writer.writerow(["receipt_id", "drink_id", "ice", "sweetness", "milk", "boba", "popping_boba"])
    food_writer.writerow(["food_id", "receipt_id", "modifiers"])

    for receipt_id in range(1601, NUM_RECEIPTS + 1601):

        # ===== Receipt =====
        customer_id = weighted_customer_id()
        cashier_id = random_cashier_id()
        purchase_date = random_timestamp()

        receipt_writer.writerow([
            receipt_id,
            customer_id,
            cashier_id,
            purchase_date.strftime("%Y-%m-%d %H:%M:%S")
        ])

        # ===== Drinks =====
        num_drinks = random.randint(1, 4)

        for _ in range(num_drinks):
            drink_writer.writerow([
                receipt_id,
                random.randint(1, MAX_DRINK_ID),
                random_ice(),
                random_sweetness(),
                random_milk(),
                random_boba(),
                random_popping()
            ])

        # ===== Food (40% chance receipt has food) =====
        if random.random() < 0.4:
            num_food = random.randint(1, 2)
            for _ in range(num_food):
                food_writer.writerow([
                    random.randint(1, MAX_FOOD_ID),
                    receipt_id,
                    random_food_modifier()
                ])

print("Generated receipt.csv, drink_to_receipt.csv, and food_to_receipt.csv")
