import csv

discount_info = [
    {"type" : "Veteran", "amount" : 0.25},
    {"type" : "Student", "amount" : 0.15},
    {"type" : "Employee", "amount" : 0.10}
]

with open("discount.csv", "w", newline="") as file:
    writer = csv.DictWriter(file, fieldnames=["type", "amount"])
    writer.writeheader()
    writer.writerows(discount_info)