
foodVaraieties = [
    {"item": "BBQ pork", "amount": 100},
    {"item": "Teriyaki chicken", "amount": 100},
    {"item": "French fries", "amount": 100}
]
with open("data/food.csv", "w") as file:
    file.write("id, item, amount\n")
    for id, food in enumerate(foodVaraieties, start=1):
        file.write(f"\n{id},{food['item']},{food['amount']}")
        file.flush()
