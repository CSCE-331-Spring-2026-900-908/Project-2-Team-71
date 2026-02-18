import random

# Header based on the food_to_receipt table in the DB diagram
header = "food_id, receipt_id\n"

# Probability Distribution:
# 50% chance one food item ordered per receipt
# 25% chance two food items
# 25% chance three food items

with open("food_to_receipt.csv", "w") as file:
    file.write(header)

    # Assuming 100 receipts (matching the drink_to_receipt range)
    for i in range(0, 100):
        # First food item (Replace 10 with total number of food items you have)
        rowString = f"{i % 10}, {i}\n"
        file.write(rowString)

        # 50% chance for a second item
        if random.randint(0, 1) == 0:
            rowString = f"{(i + 5) % 10}, {i}\n"
            file.write(rowString)

            # 50% chance for a third item (25% total chance)
            if random.randint(0, 1) == 0:
                rowString = f"{(i + 8) % 10}, {i}\n"
                file.write(rowString)
