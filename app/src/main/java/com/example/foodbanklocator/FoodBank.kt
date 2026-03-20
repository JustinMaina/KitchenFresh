package com.example.foodbanklocator


/**
 * Data model representing a Food Bank.
 *
 * [id]          â€” Firestore document ID (empty string until saved to Firestore)
 * [name]        â€” Display name of the food bank
 * [latitude]    â€” GPS latitude
 * [longitude]   â€” GPS longitude
 * [address]     â€” Human-readable address (optional but useful for users)
 * [phone]       â€” Contact phone number (optional)
 * [openingHours]â€” e.g. "Monâ€“Fri 08:00â€“17:00" (optional)
 * [items]       â€” List of available items e.g. ["flour", "sugar", "rice"]
 */
data class FoodBank(
    val id: String = "",
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = "",
    val phone: String = "",
    val openingHours: String = "",
    val items: List<String> = emptyList()
) {
    /**
     * Firestore requires a no-argument constructor for deserialization.
     * The default parameter values above satisfy this automatically in Kotlin.
     *
     * toMap() is used when writing to Firestore (add / set operations).
     */
    fun toMap(): Map<String, Any> = mapOf(
        "name"         to name,
        "latitude"     to latitude,
        "longitude"    to longitude,
        "address"      to address,
        "phone"        to phone,
        "openingHours" to openingHours,
        "items"        to items
    )

    companion object {
        /**
         * Converts a Firestore document snapshot into a FoodBank object.
         * Safe: uses get() with defaults so missing fields don't crash the app.
         */
        fun fromMap(id: String, data: Map<String, Any?>): FoodBank = FoodBank(
            id           = id,
            name         = data["name"]         as? String ?: "",
            latitude     = (data["latitude"]    as? Number)?.toDouble() ?: 0.0,
            longitude    = (data["longitude"]   as? Number)?.toDouble() ?: 0.0,
            address      = data["address"]      as? String ?: "",
            phone        = data["phone"]        as? String ?: "",
            openingHours = data["openingHours"] as? String ?: "",
            items        = (data["items"]       as? List<*>)?.filterIsInstance<String>() ?: emptyList()
        )
    }
}
