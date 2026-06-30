package com.maurofmorato.cafecomnota.data.repository

import com.maurofmorato.cafecomnota.data.supabase.SupabaseCoffeeApi
import com.maurofmorato.cafecomnota.ui.model.CoffeeUiModel
import com.maurofmorato.cafecomnota.ui.model.sampleCoffees

data class CoffeeLoadResult(
    val coffees: List<CoffeeUiModel>,
    val source: CoffeeDataSource,
    val error: Throwable? = null
)

enum class CoffeeDataSource {
    Supabase,
    LocalFallback
}

class CoffeeRepository(
    private val supabaseCoffeeApi: SupabaseCoffeeApi = SupabaseCoffeeApi()
) {
    suspend fun loadCoffees(): CoffeeLoadResult {
        return try {
            val supabaseCoffees = supabaseCoffeeApi.loadCoffeeSummaries()

            if (supabaseCoffees.isNotEmpty()) {
                CoffeeLoadResult(
                    coffees = supabaseCoffees,
                    source = CoffeeDataSource.Supabase
                )
            } else {
                CoffeeLoadResult(
                    coffees = sampleCoffees(),
                    source = CoffeeDataSource.LocalFallback,
                    error = IllegalStateException("Supabase não retornou cafés")
                )
            }
        } catch (throwable: Throwable) {
            CoffeeLoadResult(
                coffees = sampleCoffees(),
                source = CoffeeDataSource.LocalFallback,
                error = throwable
            )
        }
    }
}
