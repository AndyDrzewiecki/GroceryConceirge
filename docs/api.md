# HomeOps Grocery – API Reference

Base URL: `http://<minipc-ip>:8000`

All endpoints return JSON unless noted. All request bodies are JSON unless the endpoint accepts multipart/form-data.

---

## Health

### `GET /health`

Returns a simple health status.

**Response 200**
```json
{ "status": "ok" }
```

---

## Uploads

### `POST /uploads/inventory-photo?location=FRIDGE|FREEZER|PANTRY`

Upload a photo of fridge, freezer, or pantry contents.

**Query parameters**

| Name | Required | Values |
|---|---|---|
| `location` | yes | `FRIDGE`, `FREEZER`, `PANTRY` |

**Body** `multipart/form-data`

| Field | Type |
|---|---|
| `file` | image file (JPEG/PNG) |

**Response 201**
```json
{
  "id": 1,
  "type": "inventory_photo",
  "location": "FRIDGE",
  "filename": "abc123.jpg",
  "created_at": "2024-01-15T10:30:00"
}
```

---

### `POST /uploads/receipt-photo`

Upload a photo of a receipt. Automatically creates a `receipt` record.

**Body** `multipart/form-data`

| Field | Type |
|---|---|
| `file` | image file (JPEG/PNG) |

**Response 201** – same shape as inventory-photo upload event.

---

## Inventory

### `GET /inventory`

List all inventory items.

**Response 200**
```json
[
  {
    "id": 1,
    "location": "FRIDGE",
    "name": "Milk",
    "quantity": 2.0,
    "unit": "litre",
    "expiry_date": "2024-01-20",
    "updated_at": "2024-01-15T10:00:00"
  }
]
```

---

### `POST /inventory`

Add a new inventory item.

**Body**
```json
{
  "location": "PANTRY",
  "name": "Rice",
  "quantity": 1.5,
  "unit": "kg",
  "expiry_date": null
}
```

**Response 201** – created item object.

---

### `PUT /inventory/{id}`

Update an existing inventory item (full or partial fields).

**Body** – same shape as POST, all fields optional.

**Response 200** – updated item object.

---

### `DELETE /inventory/{id}`

Remove an inventory item.

**Response 204** – no content.

---

## Receipts

### `GET /receipts`

List all receipts, newest first.

**Response 200**
```json
[
  {
    "id": 1,
    "store": "Lidl",
    "total_amount": 42.50,
    "purchase_date": "2024-01-14",
    "image_filename": "abc123.jpg",
    "created_at": "2024-01-14T18:00:00"
  }
]
```

---

### `POST /receipts/{id}/metadata`

Attach store, total, and date to a receipt that was previously uploaded via photo.

**Body**
```json
{
  "store": "Lidl",
  "total_amount": 42.50,
  "purchase_date": "2024-01-14"
}
```

All fields are optional. **Response 200** – updated receipt object.

---

## Dashboard

### `GET /dashboard`

Aggregated summary for the home screen.

**Response 200**
```json
{
  "month_grocery_spend": 127.80,
  "todays_meal": "Grilled Chicken & Rice",
  "shopping_needed": true
}
```

| Field | Description |
|---|---|
| `month_grocery_spend` | Sum of `receipt.total_amount` for the current calendar month. |
| `todays_meal` | Meal name from the current meal plan for today, or `null`. |
| `shopping_needed` | `true` if there are unpurchased items on the shopping list. |

---

## Shopping List

### `GET /shopping-list`

Retrieve all shopping list items.

**Response 200**
```json
[
  { "id": 1, "name": "Eggs", "quantity": 12.0, "unit": "unit", "purchased": false }
]
```

---

### `POST /shopping-list`

Add a manual item to the shopping list.

**Body**
```json
{ "name": "Butter", "quantity": 1.0, "unit": "pack" }
```

**Response 201** – created item.

---

### `PATCH /shopping-list/{id}`

Toggle purchased state (or update any field).

**Body**
```json
{ "purchased": true }
```

**Response 200** – updated item.

---

## Meal Plan

### `POST /mealplan/generate`

Generate a new 7-day meal plan starting from the current Monday. If a plan for this week already exists it is replaced. Also populates the shopping list with any missing ingredients.

**Response 201**
```json
{
  "id": 1,
  "week_start_date": "2024-01-15",
  "created_at": "2024-01-15T09:00:00",
  "meals": [
    { "id": 1, "date": "2024-01-15", "meal_name": "Spaghetti Bolognese", "notes": "spaghetti, ground beef, tomato sauce, onion, garlic" },
    ...
  ]
}
```

---

### `GET /mealplan/current`

Return the most recently generated meal plan.

**Response 200** – same shape as POST response.
**Response 404** – if no plan exists yet.

---

## Error Responses

All errors follow this shape:

```json
{ "detail": "Human-readable error message" }
```

Common HTTP codes:

| Code | Meaning |
|---|---|
| 400 | Bad request / validation error |
| 404 | Resource not found |
| 422 | Pydantic validation error (body schema mismatch) |
| 500 | Internal server error |
