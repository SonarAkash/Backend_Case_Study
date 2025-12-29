@app.route('/api/products', methods=['POST'])
def create_product():
    data = request.get_json()
    
    # Check if all needed data is present to avoid errors later
    required = ['name', 'sku', 'price', 'warehouse_id', 'initial_quantity']
    if not data or not all(k in data for k in required):
        return {"error": "Missing required fields"}, 400
    
    try:
        # Start a transaction
        product = Product(
            name=data['name'],
            sku=data['sku'],
            price=data['price'],
            warehouse_id=data['warehouse_id']
        )
        
        db.session.add(product)
        # flush() generates the ID without permanently saving yet
        db.session.flush() 

        # Add the inventory record using the new product ID
        inventory = Inventory(
            product_id=product.id,
            warehouse_id=data['warehouse_id'],
            quantity=data['initial_quantity']
        )
        
        db.session.add(inventory)
        
        # Commit ONLY if both steps succeed (Atomicity)
        db.session.commit()
        
        return {"message": "Product created", "product_id": product.id}, 201

    except Exception as e:
        # If anything fails, undo changes so we don't have broken data
        db.session.rollback()
        return {"error": str(e)}, 500
