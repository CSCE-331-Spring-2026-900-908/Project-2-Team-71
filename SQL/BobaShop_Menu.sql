-- Create the Bridge Table
CREATE TABLE menu_recipe (
    menu_id INTEGER,
    menu_item_name TEXT,
    inventory_id INTEGER
);

\copy menu_recipe FROM 'recipe.csv' WITH (FORMAT csv, HEADER true);

CREATE VIEW menu_usage_summary AS
SELECT
    menu_item_name || ' uses ' || COUNT(inventory_id) || ' inventory items' AS "menu itemusage"
FROM menu_recipe
GROUP BY menu_item_name;