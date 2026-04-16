INSERT INTO customers (customer_id, customer_name) VALUES
('C1001', 'Kavin'),
('C1002', 'Prabhu'),
('C1003', 'KP');

INSERT INTO reward_transactions (transaction_id, customer_id, transaction_date, amount, description) VALUES
('T10001', 'C1001', '2026-01-05', 120.00, 'Grocery order'),
('T10002', 'C1001', '2026-01-21', 75.00, 'Home supplies'),
('T10003', 'C1001', '2026-02-04', 45.00, 'Pharmacy purchase'),
('T10004', 'C1001', '2026-02-16', 130.00, 'Electronics accessories'),
('T10005', 'C1001', '2026-03-12', 210.00, 'Appliance order'),
('T10006', 'C1001', '2026-03-22', 51.25, 'Pet supplies'),
('T20001', 'C1002', '2026-01-08', 49.99, 'Books'),
('T20002', 'C1002', '2026-01-28', 101.00, 'Weekly shopping'),
('T20003', 'C1002', '2026-02-10', 99.00, 'Department store'),
('T20004', 'C1002', '2026-02-23', 140.00, 'Furniture deposit'),
('T20005', 'C1002', '2026-03-02', 55.00, 'Garden supplies'),
('T20006', 'C1002', '2026-03-19', 320.75, 'Renovation materials'),
('T30001', 'C1003', '2025-12-30', 88.00, 'Year-end shopping'),
('T30002', 'C1003', '2026-01-14', 150.00, 'Winter apparel'),
('T30003', 'C1003', '2026-02-08', 20.00, 'Coffee shop'),
('T30004', 'C1003', '2026-02-28', 60.00, 'Office supplies'),
('T30005', 'C1003', '2026-03-06', 110.00, 'Grocery refill'),
('T30006', 'C1003', '2026-03-18', 500.00, 'Television purchase');
