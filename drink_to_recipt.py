"""
This is dedicated to the Most Sacred Heart of Jesus, 
the Immaculate Heart of Mary 
and the Chaste Heart of St. Joseph
"""
import random

header = "drink_id, receipt_id\n"

#50% chance one drink ordered per receipt
#25% chance two drinks
#25% chance three drinks

with open("drink_to_recipt.csv", "w") as file:
    file.write(header)

    for i in range(0,100):
        rowString = f"{i % 15}, {i}\n"
        file.write(rowString)

        if random.randint(0,1) == 0:
            rowString = f"{(i + 10) % 15}, {i}\n"
            file.write(rowString)

            if random.randint(0,1) == 0:
                rowString = f"{(i + 20) % 15}, {i}\n"
                file.write(rowString)

        

        
