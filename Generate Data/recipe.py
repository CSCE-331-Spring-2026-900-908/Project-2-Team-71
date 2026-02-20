import csv

# 1. Define the Menu Items (Drinks 0-14, Food 15-17)
drinks = {
    0: "Black Milk Tea", 1: "Green Milk Tea", 2: "Matcha Milk Tea",
    3: "Taro Milk Tea", 4: "Jasmine Milk Tea", 5: "Oolong Milk Tea",
    6: "Thai Milk Tea", 7: "Black Tea", 8: "Green Tea",
    9: "Matcha Tea", 10: "Taro Tea", 11: "Jasmine Tea",
    12: "Oolong Tea", 13: "Thai Tea", 14: "Chocolate Milk"
}

food = {
    15: "BBQ pork",
    16: "Teriyaki chicken",
    17: "French fries"
}

# 2. Define the Recipe Map (Food/Ingredient Inventory IDs Only)
# Excludes IDs 29-36 (Supplies like Straws, Cups, Gloves)
recipe_map = {
    # ID 0: Black Milk Tea (Set to 12 items for your example)
    0: [0, 4, 5, 6, 7, 8, 9, 10, 11, 12, 26, 27],
    
    # IDs 1-6: Milk Teas (Tea base + Milk + Ice + Syrup)
    1: [1, 12, 26, 9], 2: [18, 12, 26, 9], 3: [17, 12, 26, 11],
    4: [2, 12, 26, 9], 5: [3, 12, 26, 9], 6: [19, 12, 26, 11],
    
    # IDs 7-13: Plain Teas (Tea base + Ice + Syrup)
    7: [0, 26, 9], 8: [1, 26, 9], 9: [18, 26, 9],
    10: [17, 26, 9], 11: [2, 26, 9], 12: [3, 26, 9], 13: [19, 26, 9],
    
    # ID 14: Chocolate Milk (Powder + Milk + Syrup)
    14: [20, 12, 9],
    
    # IDs 15-17: Food Items (Continuing count from 14)
    15: [37], 16: [38], 17: [39]
}

# 3. Create the CSV File
all_menu_items = {**drinks, **food}

with open('data/recipe.csv', 'w', newline='') as csvfile:
    fieldnames = ['menu_id', 'menu_item_name', 'inventory_id']
    writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
    writer.writeheader()
    
    for m_id, name in all_menu_items.items():
        # Get the list of ingredients for this item
        ingredients = recipe_map.get(m_id, [])
        for inv_id in ingredients:
            writer.writerow({
                'menu_id': m_id,
                'menu_item_name': name,
                'inventory_id': inv_id
            })

print("recipe.csv has been created with menu names and food-only ingredients.")