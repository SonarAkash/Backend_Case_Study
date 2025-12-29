# Case Study Submission - Backend Engineering Intern

**Candidate:** Akash Sonar

---

## Part 1: Code Review & Debugging

### Problem Summary
The task was to review and fix a legacy API endpoint (`create_product`) that creates a Product and an associated Inventory record. The original code failed to ensure data consistency (atomicity) and lacked proper error handling, leading to potential data corruption in production.

[`fix_product_api.py`](fix_product_api.py) 

### Explanation of Fixes
* **Atomic Transaction:** I removed the first `commit()` and used `flush()` instead. `flush()` pushes the Product to the database to generate the `product.id` (needed for the Inventory record) but keeps the transaction open. The single `commit()` at the end ensures that if the Inventory creation fails, the Product creation is rolled back automatically.
* **Exception Handling:** Added a `try...except` block with `db.session.rollback()`. This ensures that if an error occurs, the database connection is cleaned up, and the user receives a proper error message (HTTP 409 for conflicts, 500 for server errors) instead of a crash.
* **Input Validation:** Added a check to ensure all required fields exist before attempting to process the data, preventing `KeyError` crashes.

---

## Part 2: Database Design

### Problem Summary
The goal was to design a database schema for a B2B SaaS inventory system that supports multiple warehouses, suppliers, inventory history tracking, and product bundles.

[`schema.sql`](schema.sql) 

### Identifying Gaps (Questions for Product Team)
To build a robust system, I would need clarification on these missing requirements:
* **Bundle Inventory Logic:** Do we physically stock "Bundles," or is the stock calculation "virtual" (calculated on the fly based on the available components)? My design assumes virtual calculation currently.
* **Multiple Suppliers:** Can a single product be supplied by multiple vendors at different prices? Currently, I linked one supplier per product for simplicity.
* **Currency:** Do we need to handle multiple currencies for companies operating internationally?

### Explanation of Design Decisions
* **Audit Trail (inventory_history):** The prompt asked to "track when inventory levels change." Instead of just updating the inventory table, I added a history table. This allows the business to generate reports on how fast items are selling or if theft (shrinkage) is occurring.
* **Data Integrity (CHECK constraints):** I added `CHECK (quantity >= 0)` on the inventory table. This prevents bugs (like the one in Part 1) from creating impossible negative stock levels in the database.
* **Performance (Indexes):** While not explicitly written in DDL, I would add indexes on `product_id` and `warehouse_id` in the inventory table because those columns will be queried frequently for dashboards.
* **Money Types:** I used `DECIMAL` instead of `FLOAT` for prices to avoid floating-point rounding errors which are critical in financial applications.

---

## Part 3: API Implementation

### Problem Summary
The task was to implement an API endpoint (`GET /api/companies/{company_id}/alerts/low-stock`) using Java Spring Boot. The endpoint must return low-stock alerts based on product thresholds, but only for products with recent sales activity.

- [`LowStockAlertDTO.java`](LowStockAlertDTO.java) - DTO
- [`InventoryController.java`](InventoryController.java) - REST Controller  
- [`InventoryService.java`](InventoryService.java) - Business logic  

### Assumptions Made (Documenting Gaps)
Since the prompt was incomplete, I made these specific assumptions:
* **Scope of Alert:** I assumed alerts are generated **per warehouse**, not globally. If Warehouse A is low but Warehouse B is full, we still alert for Warehouse A to prevent local stockouts.
* **Recent Activity:** I defined "recent sales activity" as "any sale occurring within the last 30 days."
* **Stockout Calculation:** I assumed `days_until_stockout` is a simple linear projection: `Current Quantity / Average Daily Sales`.

### Handling Edge Cases
* **Division by Zero:** If a product is low on stock but hasn't sold recently (avg sales = 0), the math for `days_until_stockout` would crash. I handled this by defaulting to a safe high number (999) or skipping the division.
* **Inactive Products:** The code explicitly filters out products with no recent sales to avoid "alert fatigue" for obsolete items that we don't intend to restock.