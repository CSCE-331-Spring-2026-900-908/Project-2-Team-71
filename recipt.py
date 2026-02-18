import random
from datetime import datetime, timedelta

receipt_info = []

# Setup for randomization
cashier_ids = [f"{j}" for j in range(0,9)]
member_pool = [f"{i}" for i in range(1, 39)]#

# Start date for random generation (last 30 days)
base_date = datetime.now()

for i in range(1, 301): # Loop 300 times
    # 1. Generate Unique Receipt ID (e.g., REC-1001)
    receipt_id = f"{1000 + i}"
    
    # 2. Random price between 7.00 and 30.00
    price = round(random.uniform(7.00, 30.00), 2)
    
    # 3. 40% chance of being a member
    cust_id = random.choice(member_pool) if random.random() < 0.4 else 0
    
    # 4. Random date within the last month
    random_days = random.randint(0, 365)
    date_str = (base_date - timedelta(days=random_days)).strftime("%Y-%m-%d")
    
    # Add to our library
    receipt_info.append({
        "receipt_id": receipt_id,
        "purchase_date": date_str,
        "total_price": price,
        "customer_id": cust_id,
        "cashier_id": random.choice(cashier_ids)
    })

# Quick check: Print the first 5 receipts
for r in receipt_info[:5]:
    print(r)
# Write to CSV
with open("receipt.csv", "w") as file:
    file.write("receipt_id, purchase_date, total_price, customer_id, cashier_id\n")
    for receipt in receipt_info:
        line = f"{receipt['receipt_id']}, {receipt['purchase_date']}, {receipt['total_price']}, {receipt['customer_id']}, {receipt['cashier_id']}\n"
        file.write(line)
