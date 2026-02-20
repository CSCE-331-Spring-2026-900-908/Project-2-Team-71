import random

customers = [
    {'name': 'Anonymous User', 'phone': '000-000-0000', 'points': 0},
    {'name': 'Chloe Gonzalez', 'phone': '210-653-4071', 'points': 143}, 
    {'name': 'Chloe Peterson', 'phone': '210-395-8472', 'points': 136}, 
    {'name': 'Scarlett Scott', 'phone': '210-462-5937', 'points': 73}, 
    {'name': 'Penelope Wright', 'phone': '210-351-6751', 'points': 117}, 
    {'name': 'Violet Rogers', 'phone': '210-947-4776', 'points': 20}, 
    {'name': 'Samuel Davis', 'phone': '210-369-9306', 'points': 143}, 
    {'name': 'Paige Anderson', 'phone': '210-250-5268', 'points': 15}, 
    {'name': 'Brianna Hill', 'phone': '210-464-5755', 'points': 56}, 
    {'name': 'Katherine Miller', 'phone': '210-660-1574', 'points': 65}, 
    {'name': 'Hunter Walker', 'phone': '210-181-1823', 'points': 74}, 
    {'name': 'Julia Mitchell', 'phone': '210-628-8935', 'points': 31}, 
    {'name': 'Isaac Wright', 'phone': '210-187-5682', 'points': 53}, 
    {'name': 'Grace Ortiz', 'phone': '210-327-4538', 'points': 87}, 
    {'name': 'Tristan Chavez', 'phone': '210-630-8315', 'points': 137}, 
    {'name': 'Madison Jackson', 'phone': '210-534-2438', 'points': 58}, 
    {'name': 'Hazel Ramos', 'phone': '210-190-1841', 'points': 142}, 
    {'name': 'Finn Hernandez', 'phone': '210-683-4830', 'points': 104}, 
    {'name': 'Dylan Sanders', 'phone': '210-184-5205', 'points': 62}, 
    {'name': 'Alice Kelly', 'phone': '210-943-5555', 'points': 148}, 
    {'name': 'Thomas Nguyen', 'phone': '210-854-1725', 'points': 38}, 
    {'name': 'Abigail Thompson', 'phone': '210-522-6747', 'points': 23}, 
    {'name': 'Amelia Myers', 'phone': '210-658-8749', 'points': 32}, 
    {'name': 'Joshua King', 'phone': '210-421-3212', 'points': 100}, 
    {'name': 'Kevin Wood', 'phone': '210-946-9655', 'points': 106}, 
    {'name': 'Gavin Smith', 'phone': '210-176-3918', 'points': 108}, 
    {'name': 'Charlotte Bennett', 'phone': '210-550-9093', 'points': 69}, 
    {'name': 'Violet Thomas', 'phone': '210-181-4505', 'points': 21}, 
    {'name': 'Paige Martinez', 'phone': '210-673-8799', 'points': 15}, 
    {'name': 'Emily Gray', 'phone': '210-749-2579', 'points': 63}, 
    {'name': 'Aaron Adams', 'phone': '210-274-6643', 'points': 34}, 
    {'name': 'Noah Bailey', 'phone': '210-611-2414', 'points': 47}, 
    {'name': 'Benjamin Cruz', 'phone': '210-501-2166', 'points': 1}, 
    {'name': 'Michael Rodriguez', 'phone': '210-431-5016', 'points': 134}, 
    {'name': 'Joshua Howard', 'phone': '210-705-1968', 'points': 141}, 
    {'name': 'Violet Gonzalez', 'phone': '210-285-3638', 'points': 118}, 
    {'name': 'Ella Ortiz', 'phone': '210-207-7420', 'points': 80}, 
    {'name': 'Jacob Smith', 'phone': '210-304-4968', 'points': 62}, 
    {'name': 'Carter Jones', 'phone': '210-370-7862', 'points': 66}, 
    {'name': 'Amelia Bailey', 'phone': '210-780-4529', 'points': 8}
    ]


with open("data/customers.csv", "w") as file:
    file.write("id, name, phone, points\n")
    for (id, item) in enumerate(customers):
        itemString = f"{id}, {item['name']}, {item['phone']}, {item['points']}\n"
        file.write(itemString)

