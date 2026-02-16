import random

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

with open("purchaces.csv", "w") as file:
    itemLen = len(inventory_with_suppliers) - 1
    file.write("item_id, item_name, buy_date, supplier_name, supplier_contact, amount, supplier_price,\n")
    for x in range(random.randint(100,300)):
        itemID = random.randint(0,itemLen)
        item = f"{itemID}, {inventory_with_suppliers[itemID]["item"]}, {str(random.randint(1,12)) + "-" + str(random.randint(1,31)) + "-" + str(random.randint(2012, 2026))}, {inventory_with_suppliers[itemID]["supplier"]}, {inventory_with_suppliers[itemID]["contact"]}, {random.randint(10,100)}, {inventory_with_suppliers[itemID]["supplier_price"]},\n"
        file.write(item)