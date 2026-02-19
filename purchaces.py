import random
from datetime import datetime, timedelta

########## code used to generate the array below

# Supplier Data Structure
suppliers = {
    "Jade Leaf Tea Wholesalers": {
        "contact": "orders@jadeleaftea.com",
        "items": {
            "Black tea leaves": 45.00,
            "Green tea leaves": 42.50,
            "Jasmine tea leaves": 48.00,
            "Oolong tea leaves": 50.00
        }
    },
    "BubbleBurst Tapioca Supply": {
        "contact": "info@bubblebursttapioca.com",
        "items": {
            "Tapioca pearls": 38.00
        }
    },
    "Tropical Pop Toppings": {
        "contact": "555-831-6640",
        "items": {
            "Popping boba (mango)": 32.00,
            "Popping boba (strawberry)": 32.00,
            "Popping boba (lychee)": 32.00
        }
    },
    "SweetWave Syrup Co.": {
        "contact": "https://www.sweetwaveco.com",
        "items": {
            "Brown sugar syrup": 25.00,
            "Simple syrup": 18.00,
            "Honey": 22.00,
            "Vanilla syrup": 24.00,
            "Strawberry syrup": 24.00,
            "Mango syrup": 24.00,
            "Lychee syrup": 24.00,
            "Passion fruit syrup": 24.00
        }
    },
    "MilkyWay Dairy Distributors": {
        "contact": "https://www.milkywaydairy.com",
        "items": {
            "Condensed milk": 30.00,
            "Whole milk": 28.00,
            "Oat milk": 35.00,
            "Almond milk": 34.00,
            "Coconut milk": 33.00,
            "Non-dairy creamer": 29.00,
            "Whipped cream": 27.00
        }
    },
    "Golden Pearl Beverage Supply": {
        "contact": "555-210-8745",
        "items": {
            "Taro powder": 40.00,
            "Thai tea mix": 37.00,
            "Chocolate powder": 26.00,
            "Ice": 10.00,
            "Cheese foam powder": 36.00,
            "BBQ pork": 65.00,
            "Teriyaki chicken": 60.00,
            "French fries": 40.00
        }
    },
    "ZenMatcha Imports": {
        "contact": "wholesale@zenmatchaimports.com",
        "items": {
            "Matcha powder": 60.00
        }
    },
    "CrystalCup Packaging": {
        "contact": "555-639-4412",
        "items": {
            "Plastic sealing cups": 55.00,
            "Cup sealing film": 48.00,
            "Plastic lids": 50.00,
            "Wide boba straws": 45.00,
            "Napkins": 20.00,
            "Sugar packets": 18.00,
            "Gloves": 22.00,
            "Cleaning solution": 25.00
        }
    }
}

# Generate inventory list
for supplier_name, supplier_info in suppliers.items():
    for item_name, price in supplier_info["items"].items():
        inventory_with_suppliers.append({
            "item": item_name,
            "supplier": supplier_name,
            "contact": supplier_info["contact"],
            "supplier_price": price
        })

#Print to verify
#for item in inventory_with_suppliers:
#    print(item)


##########


inventory_with_suppliers = [
    {"item": "Black tea leaves", "supplier": "Jade Leaf Tea Wholesalers", "contact": "orders@jadeleaftea.com", "supplier_price": 45.00},
    {"item": "Green tea leaves", "supplier": "Jade Leaf Tea Wholesalers", "contact": "orders@jadeleaftea.com", "supplier_price": 42.50},
    {"item": "Jasmine tea leaves", "supplier": "Jade Leaf Tea Wholesalers", "contact": "orders@jadeleaftea.com", "supplier_price": 48.00},
    {"item": "Oolong tea leaves", "supplier": "Jade Leaf Tea Wholesalers", "contact": "orders@jadeleaftea.com", "supplier_price": 50.00},
    {"item": "Tapioca pearls", "supplier": "BubbleBurst Tapioca Supply", "contact": "info@bubblebursttapioca.com", "supplier_price": 38.00},
    {"item": "Popping boba (mango)", "supplier": "Tropical Pop Toppings", "contact": "555-831-6640", "supplier_price": 32.00},
    {"item": "Popping boba (strawberry)", "supplier": "Tropical Pop Toppings", "contact": "555-831-6640", "supplier_price": 32.00},
    {"item": "Popping boba (lychee)", "supplier": "Tropical Pop Toppings", "contact": "555-831-6640", "supplier_price": 32.00},
    {"item": "Brown sugar syrup", "supplier": "SweetWave Syrup Co.", "contact": "https://www.sweetwaveco.com", "supplier_price": 25.00},
    {"item": "Simple syrup", "supplier": "SweetWave Syrup Co.", "contact": "https://www.sweetwaveco.com", "supplier_price": 18.00},
    {"item": "Honey", "supplier": "SweetWave Syrup Co.", "contact": "https://www.sweetwaveco.com", "supplier_price": 22.00},
    {"item": "Condensed milk", "supplier": "MilkyWay Dairy Distributors", "contact": "https://www.milkywaydairy.com", "supplier_price": 30.00},
    {"item": "Whole milk", "supplier": "MilkyWay Dairy Distributors", "contact": "https://www.milkywaydairy.com", "supplier_price": 28.00},
    {"item": "Oat milk", "supplier": "MilkyWay Dairy Distributors", "contact": "https://www.milkywaydairy.com", "supplier_price": 35.00},
    {"item": "Almond milk", "supplier": "MilkyWay Dairy Distributors", "contact": "https://www.milkywaydairy.com", "supplier_price": 34.00},
    {"item": "Coconut milk", "supplier": "MilkyWay Dairy Distributors", "contact": "https://www.milkywaydairy.com", "supplier_price": 33.00},
    {"item": "Non-dairy creamer", "supplier": "MilkyWay Dairy Distributors", "contact": "https://www.milkywaydairy.com", "supplier_price": 29.00},
    {"item": "Taro powder", "supplier": "Golden Pearl Beverage Supply", "contact": "555-210-8745", "supplier_price": 40.00},
    {"item": "Matcha powder", "supplier": "ZenMatcha Imports", "contact": "wholesale@zenmatchaimports.com", "supplier_price": 60.00},
    {"item": "Thai tea mix", "supplier": "Golden Pearl Beverage Supply", "contact": "555-210-8745", "supplier_price": 37.00},
    {"item": "Chocolate powder", "supplier": "Golden Pearl Beverage Supply", "contact": "555-210-8745", "supplier_price": 26.00},
    {"item": "Vanilla syrup", "supplier": "SweetWave Syrup Co.", "contact": "https://www.sweetwaveco.com", "supplier_price": 24.00},
    {"item": "Strawberry syrup", "supplier": "SweetWave Syrup Co.", "contact": "https://www.sweetwaveco.com", "supplier_price": 24.00},
    {"item": "Mango syrup", "supplier": "SweetWave Syrup Co.", "contact": "https://www.sweetwaveco.com", "supplier_price": 24.00},
    {"item": "Lychee syrup", "supplier": "SweetWave Syrup Co.", "contact": "https://www.sweetwaveco.com", "supplier_price": 24.00},
    {"item": "Passion fruit syrup", "supplier": "SweetWave Syrup Co.", "contact": "https://www.sweetwaveco.com", "supplier_price": 24.00},
    {"item": "Ice", "supplier": "Golden Pearl Beverage Supply", "contact": "555-210-8745", "supplier_price": 10.00},
    {"item": "Whipped cream", "supplier": "MilkyWay Dairy Distributors", "contact": "https://www.milkywaydairy.com", "supplier_price": 27.00},
    {"item": "Cheese foam powder", "supplier": "Golden Pearl Beverage Supply", "contact": "555-210-8745", "supplier_price": 36.00},
    {"item": "Plastic sealing cups", "supplier": "CrystalCup Packaging", "contact": "555-639-4412", "supplier_price": 55.00},
    {"item": "Cup sealing film", "supplier": "CrystalCup Packaging", "contact": "555-639-4412", "supplier_price": 48.00},
    {"item": "Plastic lids", "supplier": "CrystalCup Packaging", "contact": "555-639-4412", "supplier_price": 50.00},
    {"item": "Wide boba straws", "supplier": "CrystalCup Packaging", "contact": "555-639-4412", "supplier_price": 45.00},
    {"item": "Napkins", "supplier": "CrystalCup Packaging", "contact": "555-639-4412", "supplier_price": 20.00},
    {"item": "Sugar packets", "supplier": "CrystalCup Packaging", "contact": "555-639-4412", "supplier_price": 18.00},
    {"item": "Gloves", "supplier": "CrystalCup Packaging", "contact": "555-639-4412", "supplier_price": 22.00},
    {"item": "Cleaning solution", "supplier": "CrystalCup Packaging", "contact": "555-639-4412", "supplier_price": 25.00},
    {"item": "BBQ pork", "supplier": "Golden Pearl Beverage Supply", "contact": "555-210-8745", "supplier_price": 65.00},
    {"item": "Teriyaki chicken", "supplier": "Golden Pearl Beverage Supply", "contact": "555-210-8745", "supplier_price": 60.00},
    {"item": "French fries", "supplier": "Golden Pearl Beverage Supply", "contact": "555-210-8745", "supplier_price": 40.00}
]

with open("purchaces.csv", "w") as file:  # writing out to csv for data base
    itemLen = len(inventory_with_suppliers) - 1
    file.write("id, item_id, buy_date, supplier_name, supplier_contact, amount, supplier_price\n") # write out attributes
    for (key, x) in enumerate(range(random.randint(100,300))):
        random_days = random.randint(0, 365*5) # min a year record of purchases
        date_str = (datetime.now() - timedelta(days=random_days)).strftime("%Y-%m-%d")
        itemID = random.randint(0,itemLen)
        item = f"{key}, {itemID}, {date_str}, {inventory_with_suppliers[itemID]["supplier"]}, {inventory_with_suppliers[itemID]["contact"]}, {random.randint(10,100)}, {inventory_with_suppliers[itemID]["supplier_price"]}\n"
        file.write(item)


