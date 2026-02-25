package com.homeops.grocery.network.models

import com.google.gson.annotations.SerializedName

// ── Dashboard ─────────────────────────────────────────────────────────────────

data class DashboardResponse(
    @SerializedName("month_grocery_spend") val monthGrocerySpend: Double,
    @SerializedName("todays_meal") val todaysMeal: String?,
    @SerializedName("shopping_needed") val shoppingNeeded: Boolean,
)

// ── Inventory ─────────────────────────────────────────────────────────────────

data class InventoryItem(
    val id: Int,
    val location: String,
    val name: String,
    val quantity: Double,
    val unit: String,
    @SerializedName("expiry_date") val expiryDate: String?,
    @SerializedName("updated_at") val updatedAt: String,
)

data class InventoryItemCreate(
    val location: String,
    val name: String,
    val quantity: Double = 1.0,
    val unit: String = "unit",
    @SerializedName("expiry_date") val expiryDate: String? = null,
)

// ── Receipts ──────────────────────────────────────────────────────────────────

data class Receipt(
    val id: Int,
    val store: String?,
    @SerializedName("total_amount") val totalAmount: Double?,
    @SerializedName("purchase_date") val purchaseDate: String?,
    @SerializedName("image_filename") val imageFilename: String,
    @SerializedName("created_at") val createdAt: String,
)

data class ReceiptMetadataUpdate(
    val store: String? = null,
    @SerializedName("total_amount") val totalAmount: Double? = null,
    @SerializedName("purchase_date") val purchaseDate: String? = null,
)

// ── Shopping List ─────────────────────────────────────────────────────────────

data class ShoppingListItem(
    val id: Int,
    val name: String,
    val quantity: Double,
    val unit: String,
    val purchased: Boolean,
)

data class ShoppingListItemCreate(
    val name: String,
    val quantity: Double = 1.0,
    val unit: String = "unit",
)

data class ShoppingListItemPatch(
    val purchased: Boolean? = null,
)

// ── Upload Event ──────────────────────────────────────────────────────────────

data class UploadEventResponse(
    val id: Int,
    val type: String,
    val location: String?,
    val filename: String,
    @SerializedName("created_at") val createdAt: String,
)

// ── Meal Plan ─────────────────────────────────────────────────────────────────

data class Meal(
    val id: Int,
    val date: String,
    @SerializedName("meal_name") val mealName: String,
    val notes: String?,
)

data class MealPlanResponse(
    val id: Int,
    @SerializedName("week_start_date") val weekStartDate: String,
    @SerializedName("created_at") val createdAt: String,
    val meals: List<Meal>,
)

// ── Health ────────────────────────────────────────────────────────────────────

data class HealthResponse(
    val status: String,
)
