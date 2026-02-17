employee_info = [
     { "name": "Marcus Chen", "phone": "832-555-0101", "hourly_pay": 12.50},
     { "name": "Sarah Jenkins", "phone": "832-555-0102", "hourly_pay": 12.50},
     { "name": "Amina Yusuf", "phone": "832-555-0103", "hourly_pay": 12.50},
     { "name": "David Miller", "phone": "832-555-0104", "hourly_pay": 12.50},
     { "name": "Elena Rodriguez", "phone": "832-555-0105", "hourly_pay": 12.50},
     { "name": "Kevin Park", "phone": "832-555-0106", "hourly_pay": 12.50},
     { "name": "Riley Thompson", "phone": "832-555-0107", "hourly_pay": 12.50},
     { "name": "Sloane Baxter", "phone": "832-555-0108", "hourly_pay": 12.50},
     { "name": "Isaac Newton", "phone": "832-555-0109", "hourly_pay": 12.50},
     { "name": "Maya Williams", "phone": "832-555-0110", "hourly_pay": 12.50}
]

with open("cashier.csv", "w") as file:
    file.write("id, name, phone, hourly_pay")
    for (id, employee) in enumerate(employee_info, start=1):
        file.write(f"\n{id}, {employee['name']}, {employee['phone']}, {employee['hourly_pay']}")
        file.flush()