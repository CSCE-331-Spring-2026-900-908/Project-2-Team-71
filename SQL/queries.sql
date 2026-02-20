
/* Special Query 1: Total orders by week */
SELECT COUNT(*) AS total_orders FROM receipt group by EXTRACT(WEEK FROM purchase_date);

/* Special Query 2: Top 10 sales */
SELECT 
    r.purchase_date,
    SUM(d.price) + COALESCE(SUM(f.price), 0) AS total_sales
FROM receipt r
LEFT JOIN drink_to_receipt dtr ON r.id = dtr.receipt_id
LEFT JOIN drink d ON dtr.drink_id = d.id
LEFT JOIN food_to_receipt ftr ON r.id = ftr.receipt_id
LEFT JOIN food f ON ftr.food_id = f.id
GROUP BY r.purchase_date
ORDER BY total_sales DESC
LIMIT 10;

/* Special Query 3: sales by hour */
SELECT 
    EXTRACT(HOUR FROM r.purchase_date) AS hour_of_day,
    COUNT(DISTINCT r.id) AS total_orders,
    SUM(d.price) + COALESCE(SUM(f.price), 0) AS total_sales
FROM receipt r
LEFT JOIN drink_to_receipt dtr ON r.id = dtr.receipt_id
LEFT JOIN drink d ON dtr.drink_id = d.id
LEFT JOIN food_to_receipt ftr ON r.id = ftr.receipt_id
LEFT JOIN food f ON ftr.food_id = f.id
GROUP BY hour_of_day
ORDER BY hour_of_day;

/* Special Query 4 */
SELECT * FROM menu_usage_summary;

-- Query 1
select * from receipt;
-- Query 2
select * from drink_to_receipt;
-- Query 3
select * from food_to_receipt;
-- Query 4
select * from drink;
-- Query 5
select * from food;
-- Query 6
select * from cashier;
-- Query 7
select * from customer;
-- Query 8
select * from purchaces;
-- Query 9
select * from inventory;
-- Query 10
select * from receipt as r left join food_to_receipt as ftr on r.id = ftr.receipt_id
-- Query 11
select * from receipt as r left join drink_to_receipt as dtr on r.id = dtr.receipt_id