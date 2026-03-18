"""
This is dedicated to the Most Sacred Heart of Jesus, 
the Immaculate Heart of Mary 
and the Chaste Heart of St. Joseph
"""
import random
import csv
from datetime import datetime, timedelta

NUM_RECEIPTS = 500_000  # total receipts
NUM_FOODS = 20          # adjust to your food count
NUM_DRINKS = 15         # adjust to your drink count
START_RECEIPT_ID = 3000

# ------------------ CSV HEADERS ------------------
receipt_header = ["id", "customer_id", "cashier_id", "purchase_date", "tax", "discount", "payment_method", "z_closed"]
drink_header = ["receipt_id", "drink_id", "ice", "sweetness", "milk", "boba", "popping_boba", "quantity"]
food_header = ["receipt_id", "food_id", "modifiers", "quantity"]

# ------------------ Options ------------------
ICE_OPTIONS = ["No Ice", "Less Ice", "Normal", "Extra Ice"]
MILK_OPTIONS = ["None", "Cow", "Oat", "Almond", "Soy"]
SWEETNESS_OPTIONS = [0, 25, 50, 75, 100]

# Weighted payment methods: Card 70%, Cash 25%, Check 3%, Void 2%
PAYMENT_METHODS = ["Card", "Cash", "Check", "Void"]
PAYMENT_WEIGHTS = [0.70, 0.95, 0.98, 1.0]

# ------------------ Helper Functions ------------------
def weighted_payment():
    r = random.random()
    for i, w in enumerate(PAYMENT_WEIGHTS):
        if r < w:
            return PAYMENT_METHODS[i]
    return "Card"

def random_date_within_last_year():
    now = datetime.now()
    delta_days = random.randint(0, 365)
    delta_seconds = random.randint(0, 86400)
    return now - timedelta(days=delta_days, seconds=delta_seconds)

# ------------------ Generate CSVs ------------------
with open("data/receipt.csv", "w", newline="") as receipt_file, \
     open("data/drink_to_receipt.csv", "w", newline="") as drink_file, \
     open("data/food_to_receipt.csv", "w", newline="") as food_file:

    receipt_writer = csv.writer(receipt_file)
    drink_writer = csv.writer(drink_file)
    food_writer = csv.writer(food_file)

    receipt_writer.writerow(receipt_header)
    drink_writer.writerow(drink_header)
    food_writer.writerow(food_header)

    for i in range(NUM_RECEIPTS):
        receipt_id = START_RECEIPT_ID + i

        # --------- Receipts ---------
        customer_id = random.randint(1, 1000)
        cashier_id = random.randint(1, 10)
        purchase_date = random_date_within_last_year().strftime("%Y-%m-%d %H:%M:%S")
        tax = round(random.uniform(0, 5), 2)
        discount = round(random.uniform(0, 3), 2)
        payment_method = weighted_payment()
        z_closed = random.random() < 0.8  # 80% chance

        receipt_writer.writerow([receipt_id, customer_id, cashier_id, purchase_date, tax, discount, payment_method, z_closed])

        # --------- Drinks ---------
        r = random.random()
        if r < 0.5:
            num_drinks = 1
        elif r < 0.75:
            num_drinks = 2
        else:
            num_drinks = 3

        for _ in range(num_drinks):
            drink_id = random.randint(0, NUM_DRINKS-1)
            ice = random.choice(ICE_OPTIONS)
            sweetness = random.choice(SWEETNESS_OPTIONS)
            milk = random.choice(MILK_OPTIONS)
            boba = random.random() < 0.3
            popping_boba = not boba and random.random() < 0.1
            quantity = random.randint(1,3)
            drink_writer.writerow([receipt_id, drink_id, ice, sweetness, milk, boba, popping_boba, quantity])

        # --------- Foods ---------
        r = random.random()
        if r < 0.4:
            num_foods = 0
        elif r < 0.8:
            num_foods = 1
        else:
            num_foods = 2

        for _ in range(num_foods):
            food_id = random.randint(0, NUM_FOODS-1)  # must exist in food table
            modifiers = ""  # optional text
            quantity = random.randint(1,3)
            food_writer.writerow([receipt_id, food_id, modifiers, quantity])