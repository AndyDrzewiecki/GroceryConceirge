package com.homeops.grocery.network

import com.homeops.grocery.network.models.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ── Health ────────────────────────────────────────────────────────────────

    @GET("health")
    suspend fun health(): Response<HealthResponse>

    // ── Dashboard ─────────────────────────────────────────────────────────────

    @GET("dashboard")
    suspend fun getDashboard(): Response<DashboardResponse>

    // ── Inventory ─────────────────────────────────────────────────────────────

    @GET("inventory")
    suspend fun getInventory(): Response<List<InventoryItem>>

    @POST("inventory")
    suspend fun createInventoryItem(@Body item: InventoryItemCreate): Response<InventoryItem>

    @PUT("inventory/{id}")
    suspend fun updateInventoryItem(
        @Path("id") id: Int,
        @Body item: InventoryItemCreate,
    ): Response<InventoryItem>

    @DELETE("inventory/{id}")
    suspend fun deleteInventoryItem(@Path("id") id: Int): Response<Unit>

    // ── Uploads ───────────────────────────────────────────────────────────────

    @Multipart
    @POST("uploads/inventory-photo")
    suspend fun uploadInventoryPhoto(
        @Query("location") location: String,
        @Part file: MultipartBody.Part,
    ): Response<UploadEventResponse>

    @Multipart
    @POST("uploads/receipt-photo")
    suspend fun uploadReceiptPhoto(
        @Part file: MultipartBody.Part,
    ): Response<UploadEventResponse>

    // ── Receipts ──────────────────────────────────────────────────────────────

    @GET("receipts")
    suspend fun getReceipts(): Response<List<Receipt>>

    @POST("receipts/{id}/metadata")
    suspend fun updateReceiptMetadata(
        @Path("id") id: Int,
        @Body metadata: ReceiptMetadataUpdate,
    ): Response<Receipt>

    // ── Shopping List ─────────────────────────────────────────────────────────

    @GET("shopping-list")
    suspend fun getShoppingList(): Response<List<ShoppingListItem>>

    @POST("shopping-list")
    suspend fun addShoppingItem(@Body item: ShoppingListItemCreate): Response<ShoppingListItem>

    @PATCH("shopping-list/{id}")
    suspend fun patchShoppingItem(
        @Path("id") id: Int,
        @Body patch: ShoppingListItemPatch,
    ): Response<ShoppingListItem>

    // ── Meal Plan ─────────────────────────────────────────────────────────────

    @POST("mealplan/generate")
    suspend fun generateMealPlan(): Response<MealPlanResponse>

    @GET("mealplan/current")
    suspend fun getCurrentMealPlan(): Response<MealPlanResponse>
}
