"""
This is dedicated to the Most Sacred Heart of Jesus, 
the Immaculate Heart of Mary 
and the Chaste Heart of St. Joseph
"""

header = "id, name, amount, ice_level, traditional_boba, popping_boba, sweetness, milk\n"
drinkVarieties = {
    "0": {"name": "Black Milk Tea", "amount": "6.00", "ice_level": "Normal", "traditional_boba": "False", "popping_boba": "False", "sweetness": "100", "milk": "Cow"},
    "1": {"name": "Green Milk Tea", "amount": "6.00", "ice_level": "Normal", "traditional_boba": "False", "popping_boba": "False", "sweetness": "100", "milk": "Cow"},
    "2": {"name": "Matcha Milk Tea", "amount": "6.00", "ice_level": "Normal", "traditional_boba": "False", "popping_boba": "False", "sweetness": "100", "milk": "Cow"},
    "3": {"name": "Taro Milk Tea", "amount": "6.00", "ice_level": "Normal", "traditional_boba": "False", "popping_boba": "False", "sweetness": "100", "milk": "Cow"},
    "4": {"name": "Jasmine Milk Tea", "amount": "6.00", "ice_level": "Normal", "traditional_boba": "False", "popping_boba": "False", "sweetness": "100", "milk": "Cow"},
    "5": {"name": "Oolong Milk Tea", "amount": "6.00", "ice_level": "Normal", "traditional_boba": "False", "popping_boba": "False", "sweetness": "100", "milk": "Cow"},
    "6": {"name": "Thai Milk Tea", "amount": "6.00", "ice_level": "Normal", "traditional_boba": "False", "popping_boba": "False", "sweetness": "100", "milk": "Cow"},
    "7": {"name": "Black Tea", "amount": "4.00", "ice_level": "Normal", "traditional_boba": "False", "popping_boba": "False", "sweetness": "100", "milk": "None"},
    "8": {"name": "Green Tea", "amount": "4.00", "ice_level": "Normal", "traditional_boba": "False", "popping_boba": "False", "sweetness": "100", "milk": "None"},
    "9": {"name": "Matcha Tea", "amount": "4.00", "ice_level": "Normal", "traditional_boba": "False", "popping_boba": "False", "sweetness": "100", "milk": "None"},
    "10": {"name": "Taro Tea", "amount": "4.00", "ice_level": "Normal", "traditional_boba": "False", "popping_boba": "False", "sweetness": "100", "milk": "None"},
    "11": {"name": "Jasmine Tea", "amount": "4.00", "ice_level": "Normal", "traditional_boba": "False", "popping_boba": "False", "sweetness": "100", "milk": "None"},
    "12": {"name": "Oolong Tea", "amount": "4.00", "ice_level": "Normal", "traditional_boba": "False", "popping_boba": "False", "sweetness": "100", "milk": "None"},
    "13": {"name": "Thai Tea", "amount": "4.00", "ice_level": "Normal", "traditional_boba": "False", "popping_boba": "False", "sweetness": "100", "milk": "None"},
    "14": {"name": "Chocolate Milk", "amount": "2.00", "ice_level": "None", "traditional_boba": "False", "popping_boba": "False", "sweetness": "100", "milk": "Cow"}
}

with open("drink.csv", "w") as file:
    file.write(header)

    for (id,drink) in drinkVarieties.items():
        idString = f"{id}, "
        file.write(idString)

        for attribute in drink:
            drinkString = f"{drink[attribute]}"


            #if last attribute of drink, add newline character
            if attribute == "milk":
                drinkString += "\n"
            else:
                drinkString += ", "

            file.write(drinkString)

#DONE :)