import random

header = "food_id, receipt_id\n"


with open("data/food_to_receipt.csv", "w") as file:
    file.write(header)
    for i in range(0, 100):
        rowString = f"{i % 10}, {i}\n"
        file.write(rowString)
        if random.randint(0, 1) == 0:
            rowString = f"{(i + 5) % 10}, {i}\n"
            file.write(rowString)
            if random.randint(0, 1) == 0:
                rowString = f"{(i + 8) % 10}, {i}\n"
                file.write(rowString)

