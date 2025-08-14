package com.example.ecommerce.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.ecommerce.models.*
import com.example.ecommerce.network.ApiService
import com.example.ecommerce.network.NetworkManager
import com.example.ecommerce.database.ProductDao
import com.example.ecommerce.database.AppDatabase
import com.example.ecommerce.utils.CacheManager
import com.example.ecommerce.utils.Logger
import com.example.ecommerce.utils.PreferencesManager
import com.example.ecommerce.utils.Result
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val context: Context,
    private val apiService: ApiService,
    private val productDao: ProductDao,
    private val database: AppDatabase,
    private val networkManager: NetworkManager,
    private val cacheManager: CacheManager,
    private val preferencesManager: PreferencesManager,
    private val logger: Logger
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val productCache = ConcurrentHashMap<Long, Product>()
    private val categoryCache = ConcurrentHashMap<Long, ProductCategory>()
    private val brandCache = ConcurrentHashMap<Long, Brand>()
    
    private val _featuredProducts = MutableLiveData<List<Product>>()
    val featuredProducts: LiveData<List<Product>> = _featuredProducts
    
    private val _newArrivals = MutableLiveData<List<Product>>()
    val newArrivals: LiveData<List<Product>> = _newArrivals
    
    private val _bestSellers = MutableLiveData<List<Product>>()
    val bestSellers: LiveData<List<Product>> = _bestSellers
    
    private val _onSaleProducts = MutableLiveData<List<Product>>()
    val onSaleProducts: LiveData<List<Product>> = _onSaleProducts
    
    private val _recentlyViewed = MutableLiveData<List<Product>>()
    val recentlyViewed: LiveData<List<Product>> = _recentlyViewed
    
    private val _searchResults = MutableLiveData<List<Product>>()
    val searchResults: LiveData<List<Product>> = _searchResults
    
    private val _categories = MutableLiveData<List<ProductCategory>>()
    val categories: LiveData<List<ProductCategory>> = _categories
    
    private val _brands = MutableLiveData<List<Brand>>()
    val brands: LiveData<List<Brand>> = _brands
    
    private val _loadingState = MutableStateFlow(false)
    val loadingState: StateFlow<Boolean> = _loadingState.asStateFlow()
    
    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState.asStateFlow()
    
    private val _refreshState = MutableStateFlow(false)
    val refreshState: StateFlow<Boolean> = _refreshState.asStateFlow()
    
    private val _networkState = MutableStateFlow(NetworkState.CONNECTED)
    val networkState: StateFlow<NetworkState> = _networkState.asStateFlow()
    
    private val _syncState = MutableStateFlow(SyncState.IDLE)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()
    
    private val recentlyViewedIds = mutableListOf<Long>()
    private val wishlistIds = mutableSetOf<Long>()
    private val cartIds = mutableMapOf<Long, Int>()
    private val compareIds = mutableSetOf<Long>()
    
    private var lastSyncTime: Long = 0
    private val syncInterval = TimeUnit.HOURS.toMillis(1)
    private val cacheExpiry = TimeUnit.MINUTES.toMillis(30)
    
    init {
        initializeRepository()
        startPeriodicSync()
        observeNetworkChanges()
        loadCachedData()
    }
    
    private fun initializeRepository() {
        coroutineScope.launch {
            try {
                loadInitialData()
                loadUserPreferences()
                setupCachePolicy()
            } catch (e: Exception) {
                logger.error("Failed to initialize repository", e)
                _errorState.value = "Initialization failed: ${e.message}"
            }
        }
    }
    
    private fun startPeriodicSync() {
        coroutineScope.launch {
            while (isActive) {
                delay(syncInterval)
                syncWithServer()
            }
        }
    }
    
    private fun observeNetworkChanges() {
        networkManager.networkState.onEach { state ->
            _networkState.value = state
            if (state == NetworkState.CONNECTED && shouldSync()) {
                syncWithServer()
            }
        }.launchIn(coroutineScope)
    }
    
    private fun loadCachedData() {
        coroutineScope.launch {
            try {
                val cachedProducts = productDao.getAllProducts()
                updateCaches(cachedProducts)
                categorizeProducts(cachedProducts)
            } catch (e: Exception) {
                logger.error("Failed to load cached data", e)
            }
        }
    }
    
    suspend fun getAllProducts(
        page: Int = 1,
        pageSize: Int = 20,
        sortBy: SortOption = SortOption.POPULARITY,
        filters: ProductFilters? = null
    ): Result<List<Product>> {
        return withContext(Dispatchers.IO) {
            try {
                _loadingState.value = true
                
                val cacheKey = generateCacheKey(page, pageSize, sortBy, filters)
                val cachedResult = cacheManager.get<List<Product>>(cacheKey)
                
                if (cachedResult != null && !isCacheExpired(cacheKey)) {
                    _loadingState.value = false
                    return@withContext Result.Success(cachedResult)
                }
                
                val response = if (networkManager.isConnected()) {
                    apiService.getProducts(page, pageSize, sortBy.value, filters?.toQueryMap())
                } else {
                    productDao.getProductsPage(page * pageSize, pageSize)
                }
                
                val products = response.map { it.toProduct() }
                
                cacheManager.put(cacheKey, products, cacheExpiry)
                updateCaches(products)
                
                if (networkManager.isConnected()) {
                    saveProductsToDatabase(products)
                }
                
                _loadingState.value = false
                Result.Success(products)
            } catch (e: Exception) {
                _loadingState.value = false
                _errorState.value = "Failed to load products: ${e.message}"
                logger.error("Error fetching products", e)
                Result.Error(e)
            }
        }
    }
    
    suspend fun getProductById(productId: Long): Result<Product> {
        return withContext(Dispatchers.IO) {
            try {
                productCache[productId]?.let {
                    addToRecentlyViewed(productId)
                    return@withContext Result.Success(it)
                }
                
                val cachedProduct = productDao.getProductById(productId)
                if (cachedProduct != null) {
                    val product = cachedProduct.toProduct()
                    productCache[productId] = product
                    addToRecentlyViewed(productId)
                    return@withContext Result.Success(product)
                }
                
                if (!networkManager.isConnected()) {
                    return@withContext Result.Error(Exception("No network connection"))
                }
                
                val response = apiService.getProductById(productId)
                val product = response.toProduct()
                
                productCache[productId] = product
                saveProductToDatabase(product)
                addToRecentlyViewed(productId)
                incrementProductView(productId)
                
                Result.Success(product)
            } catch (e: Exception) {
                logger.error("Error fetching product $productId", e)
                Result.Error(e)
            }
        }
    }
    
    suspend fun searchProducts(
        query: String,
        category: Long? = null,
        brand: Long? = null,
        minPrice: BigDecimal? = null,
        maxPrice: BigDecimal? = null,
        inStock: Boolean = false,
        sortBy: SortOption = SortOption.RELEVANCE
    ): Result<List<Product>> {
        return withContext(Dispatchers.IO) {
            try {
                _loadingState.value = true
                
                val searchParams = SearchParams(
                    query = query,
                    categoryId = category,
                    brandId = brand,
                    minPrice = minPrice,
                    maxPrice = maxPrice,
                    inStock = inStock,
                    sortBy = sortBy
                )
                
                val results = if (networkManager.isConnected()) {
                    apiService.searchProducts(searchParams.toQueryMap()).map { it.toProduct() }
                } else {
                    searchLocalProducts(searchParams)
                }
                
                _searchResults.postValue(results)
                _loadingState.value = false
                
                saveSearchHistory(query, results.size)
                Result.Success(results)
            } catch (e: Exception) {
                _loadingState.value = false
                logger.error("Search failed for query: $query", e)
                Result.Error(e)
            }
        }
    }
    
    suspend fun getProductsByCategory(
        categoryId: Long,
        includeSubcategories: Boolean = true,
        page: Int = 1,
        pageSize: Int = 20
    ): Result<List<Product>> {
        return withContext(Dispatchers.IO) {
            try {
                val categoryIds = if (includeSubcategories) {
                    getAllSubcategoryIds(categoryId)
                } else {
                    listOf(categoryId)
                }
                
                val products = if (networkManager.isConnected()) {
                    apiService.getProductsByCategories(categoryIds, page, pageSize)
                        .map { it.toProduct() }
                } else {
                    productDao.getProductsByCategories(categoryIds, page * pageSize, pageSize)
                        .map { it.toProduct() }
                }
                
                Result.Success(products)
            } catch (e: Exception) {
                logger.error("Error fetching products for category $categoryId", e)
                Result.Error(e)
            }
        }
    }
    
    suspend fun getProductsByBrand(brandId: Long, page: Int = 1, pageSize: Int = 20): Result<List<Product>> {
        return withContext(Dispatchers.IO) {
            try {
                val products = if (networkManager.isConnected()) {
                    apiService.getProductsByBrand(brandId, page, pageSize).map { it.toProduct() }
                } else {
                    productDao.getProductsByBrand(brandId, page * pageSize, pageSize).map { it.toProduct() }
                }
                Result.Success(products)
            } catch (e: Exception) {
                logger.error("Error fetching products for brand $brandId", e)
                Result.Error(e)
            }
        }
    }
    
    suspend fun getFeaturedProducts(limit: Int = 10): Result<List<Product>> {
        return withContext(Dispatchers.IO) {
            try {
                val products = if (networkManager.isConnected()) {
                    apiService.getFeaturedProducts(limit).map { it.toProduct() }
                } else {
                    productDao.getFeaturedProducts(limit).map { it.toProduct() }
                }
                _featuredProducts.postValue(products)
                Result.Success(products)
            } catch (e: Exception) {
                logger.error("Error fetching featured products", e)
                Result.Error(e)
            }
        }
    }
    
    suspend fun getNewArrivals(days: Int = 30, limit: Int = 20): Result<List<Product>> {
        return withContext(Dispatchers.IO) {
            try {
                val cutoffDate = Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(days.toLong()))
                val products = if (networkManager.isConnected()) {
                    apiService.getNewArrivals(cutoffDate.time, limit).map { it.toProduct() }
                } else {
                    productDao.getNewArrivals(cutoffDate, limit).map { it.toProduct() }
                }
                _newArrivals.postValue(products)
                Result.Success(products)
            } catch (e: Exception) {
                logger.error("Error fetching new arrivals", e)
                Result.Error(e)
            }
        }
    }
    
    suspend fun getBestSellers(period: String = "month", limit: Int = 20): Result<List<Product>> {
        return withContext(Dispatchers.IO) {
            try {
                val products = if (networkManager.isConnected()) {
                    apiService.getBestSellers(period, limit).map { it.toProduct() }
                } else {
                    productDao.getBestSellers(limit).map { it.toProduct() }
                }
                _bestSellers.postValue(products)
                Result.Success(products)
            } catch (e: Exception) {
                logger.error("Error fetching best sellers", e)
                Result.Error(e)
            }
        }
    }
    
    suspend fun getOnSaleProducts(limit: Int = 20): Result<List<Product>> {
        return withContext(Dispatchers.IO) {
            try {
                val products = if (networkManager.isConnected()) {
                    apiService.getOnSaleProducts(limit).map { it.toProduct() }
                } else {
                    productDao.getOnSaleProducts(limit).map { it.toProduct() }
                }
                _onSaleProducts.postValue(products)
                Result.Success(products)
            } catch (e: Exception) {
                logger.error("Error fetching on sale products", e)
                Result.Error(e)
            }
        }
    }
    
    suspend fun getRelatedProducts(productId: Long, limit: Int = 10): Result<List<Product>> {
        return withContext(Dispatchers.IO) {
            try {
                val product = getProductById(productId)
                if (product is Result.Success) {
                    val relatedIds = product.data.relatedProductIds ?: emptyList()
                    val relatedProducts = if (relatedIds.isNotEmpty()) {
                        getProductsByIds(relatedIds)
                    } else {
                        getSimilarProducts(product.data, limit)
                    }
                    Result.Success(relatedProducts)
                } else {
                    Result.Error(Exception("Product not found"))
                }
            } catch (e: Exception) {
                logger.error("Error fetching related products for $productId", e)
                Result.Error(e)
            }
        }
    }
    
    suspend fun getFrequentlyBoughtTogether(productId: Long, limit: Int = 5): Result<List<Product>> {
        return withContext(Dispatchers.IO) {
            try {
                val products = if (networkManager.isConnected()) {
                    apiService.getFrequentlyBoughtTogether(productId, limit).map { it.toProduct() }
                } else {
                    val product = productDao.getProductById(productId)?.toProduct()
                    product?.frequentlyBoughtTogetherIds?.let { ids ->
                        getProductsByIds(ids.take(limit))
                    } ?: emptyList()
                }
                Result.Success(products)
            } catch (e: Exception) {
                logger.error("Error fetching frequently bought together for $productId", e)
                Result.Error(e)
            }
        }
    }
    
    suspend fun getRecommendedProducts(userId: Long, limit: Int = 20): Result<List<Product>> {
        return withContext(Dispatchers.IO) {
            try {
                val products = if (networkManager.isConnected()) {
                    apiService.getRecommendedProducts(userId, limit).map { it.toProduct() }
                } else {
                    generateLocalRecommendations(userId, limit)
                }
                Result.Success(products)
            } catch (e: Exception) {
                logger.error("Error fetching recommendations for user $userId", e)
                Result.Error(e)
            }
        }
    }
    
    suspend fun getProductReviews(productId: Long, page: Int = 1, pageSize: Int = 20): Result<List<Review>> {
        return withContext(Dispatchers.IO) {
            try {
                val reviews = if (networkManager.isConnected()) {
                    apiService.getProductReviews(productId, page, pageSize).map { it.toReview() }
                } else {
                    database.reviewDao().getProductReviews(productId, page * pageSize, pageSize)
                        .map { it.toReview() }
                }
                Result.Success(reviews)
            } catch (e: Exception) {
                logger.error("Error fetching reviews for product $productId", e)
                Result.Error(e)
            }
        }
    }
    
    suspend fun submitProductReview(
        productId: Long,
        userId: Long,
        rating: Int,
        title: String,
        content: String,
        images: List<String>? = null
    ): Result<Review> {
        return withContext(Dispatchers.IO) {
            try {
                if (!networkManager.isConnected()) {
                    return@withContext Result.Error(Exception("Network connection required"))
                }
                
                val reviewRequest = ReviewRequest(
                    productId = productId,
                    userId = userId,
                    rating = rating,
                    title = title,
                    content = content,
                    images = images
                )
                
                val review = apiService.submitReview(reviewRequest).toReview()
                database.reviewDao().insertReview(review.toEntity())
                updateProductRating(productId)
                
                Result.Success(review)
            } catch (e: Exception) {
                logger.error("Error submitting review for product $productId", e)
                Result.Error(e)
            }
        }
    }
    
    suspend fun addToWishlist(productId: Long, userId: Long): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                wishlistIds.add(productId)
                if (networkManager.isConnected()) {
                    apiService.addToWishlist(userId, productId)
                }
                database.wishlistDao().addItem(WishlistItem(userId, productId, Date()))
                Result.Success(true)
            } catch (e: Exception) {
                logger.error("Error adding product $productId to wishlist", e)
                Result.Error(e)
            }
        }
    }
    
    suspend fun removeFromWishlist(productId: Long, userId: Long): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                wishlistIds.remove(productId)
                if (networkManager.isConnected()) {
                    apiService.removeFromWishlist(userId, productId)
                }
                database.wishlistDao().removeItem(userId, productId)
                Result.Success(true)
            } catch (e: Exception) {
                logger.error("Error removing product $productId from wishlist", e)
                Result.Error(e)
            }
        }
    }
    
    suspend fun getWishlist(userId: Long): Result<List<Product>> {
        return withContext(Dispatchers.IO) {
            try {
                val productIds = if (networkManager.isConnected()) {
                    apiService.getWishlist(userId).map { it.productId }
                } else {
                    database.wishlistDao().getUserWishlist(userId).map { it.productId }
                }
                val products = getProductsByIds(productIds)
                Result.Success(products)
            } catch (e: Exception) {
                logger.error("Error fetching wishlist for user $userId", e)
                Result.Error(e)
            }
        }
    }
    
    suspend fun addToCart(productId: Long, quantity: Int = 1): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val currentQuantity = cartIds[productId] ?: 0
                cartIds[productId] = currentQuantity + quantity
                
                val product = getProductById(productId)
                if (product is Result.Success && cartIds[productId]!! > product.data.stock) {
                    cartIds[productId] = product.data.stock
                    return@withContext Result.Error(Exception("Insufficient stock"))
                }
                
                Result.Success(true)
            } catch (e: Exception) {
                logger.error("Error adding product $productId to cart", e)
                Result.Error(e)
            }
        }
    }
    
    suspend fun updateCartQuantity(productId: Long, quantity: Int): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                if (quantity <= 0) {
                    cartIds.remove(productId)
                } else {
                    val product = getProductById(productId)
                    if (product is Result.Success) {
                        cartIds[productId] = minOf(quantity, product.data.stock)
                    }
                }
                Result.Success(true)
            } catch (e: Exception) {
                logger.error("Error updating cart quantity for product $productId", e)
                Result.Error(e)
            }
        }
    }
    
    suspend fun getCartItems(): Result<List<CartItem>> {
        return withContext(Dispatchers.IO) {
            try {
                val items = cartIds.map { (productId, quantity) ->
                    val product = getProductById(productId)
                    if (product is Result.Success) {
                        CartItem(product.data, quantity)
                    } else null
                }.filterNotNull()
                Result.Success(items)
            } catch (e: Exception) {
                logger.error("Error fetching cart items", e)
                Result.Error(e)
            }
        }
    }
    
    suspend fun clearCart(): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                cartIds.clear()
                Result.Success(true)
            } catch (e: Exception) {
                logger.error("Error clearing cart", e)
                Result.Error(e)
            }
        }
    }
    
    suspend fun addToCompare(productId: Long): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                if (compareIds.size >= 4) {
                    return@withContext Result.Error(Exception("Maximum 4 products can be compared"))
                }
                compareIds.add(productId)
                Result.Success(true)
            } catch (e: Exception) {
                logger.error("Error adding product $productId to compare", e)
                Result.Error(e)
            }
        }
    }
    
    suspend fun removeFromCompare(productId: Long): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                compareIds.remove(productId)
                Result.Success(true)
            } catch (e: Exception) {
                logger.error("Error removing product $productId from compare", e)
                Result.Error(e)
            }
        }
    }
    
    suspend fun getCompareProducts(): Result<List<Product>> {
        return withContext(Dispatchers.IO) {
            try {
                val products = getProductsByIds(compareIds.toList())
                Result.Success(products)
            } catch (e: Exception) {
                logger.error("Error fetching compare products", e)
                Result.Error(e)
            }
        }
    }
    
    suspend fun checkProductAvailability(productId: Long, quantity: Int = 1): Result<AvailabilityStatus> {
        return withContext(Dispatchers.IO) {
            try {
                val product = getProductById(productId)
                if (product is Result.Success) {
                    val status = when {
                        product.data.stock >= quantity -> AvailabilityStatus.IN_STOCK
                        product.data.stock > 0 -> AvailabilityStatus.LIMITED_STOCK
                        product.data.backOrder -> AvailabilityStatus.BACK_ORDER
                        product.data.preOrder -> AvailabilityStatus.PRE_ORDER
                        else -> AvailabilityStatus.OUT_OF_STOCK
                    }
                    Result.Success(status)
                } else {
                    Result.Error(Exception("Product not found"))
                }
            } catch (e: Exception) {
                logger.error("Error checking availability for product $productId", e)
                Result.Error(e)
            }
        }
    }
    
    suspend fun getProductVariants(productId: Long): Result<List<ProductVariant>> {
        return withContext(Dispatchers.IO) {
            try {
                val variants = if (networkManager.isConnected()) {
                    apiService.getProductVariants(productId).map { it.toProductVariant() }
                } else {
                    database.variantDao().getProductVariants(productId).map { it.toProductVariant() }
                }
                Result.Success(variants)
            } catch (e: Exception) {
                logger.error("Error fetching variants for product $productId", e)
                Result.Error(e)
            }
        }
    }
    
    suspend fun getCategories(parentId: Long? = null): Result<List<ProductCategory>> {
        return withContext(Dispatchers.IO) {
            try {
                val categories = if (networkManager.isConnected()) {
                    apiService.getCategories(parentId).map { it.toProductCategory() }
                } else {
                    database.categoryDao().getCategories(parentId).map { it.toProductCategory() }
                }
                _categories.postValue(categories)
                categories.forEach { categoryCache[it.id] = it }
                Result.Success(categories)
            } catch (e: Exception) {
                logger.error("Error fetching categories", e)
                Result.Error(e)
            }
        }
    }
    
    suspend fun getBrands(categoryId: Long? = null): Result<List<Brand>> {
        return withContext(Dispatchers.IO) {
            try {
                val brands = if (networkManager.isConnected()) {
                    apiService.getBrands(categoryId).map { it.toBrand() }
                } else {
                    database.brandDao().getBrands(categoryId).map { it.toBrand() }
                }
                _brands.postValue(brands)
                brands.forEach { brandCache[it.id] = it }
                Result.Success(brands)
            } catch (e: Exception) {
                logger.error("Error fetching brands", e)
                Result.Error(e)
            }
        }
    }
    
    suspend fun getFilters(categoryId: Long? = null): Result<ProductFilters> {
        return withContext(Dispatchers.IO) {
            try {
                val filters = if (networkManager.isConnected()) {
                    apiService.getFilters(categoryId).toProductFilters()
                } else {
                    generateLocalFilters(categoryId)
                }
                Result.Success(filters)
            } catch (e: Exception) {
                logger.error("Error fetching filters", e)
                Result.Error(e)
            }
        }
    }
    
    suspend fun trackProductView(productId: Long, userId: Long? = null) {
        coroutineScope.launch {
            try {
                if (networkManager.isConnected()) {
                    apiService.trackProductView(productId, userId)
                }
                database.analyticsDao().trackView(productId, userId, Date())
            } catch (e: Exception) {
                logger.error("Error tracking product view", e)
            }
        }
    }
    
    suspend fun trackProductClick(productId: Long, source: String, userId: Long? = null) {
        coroutineScope.launch {
            try {
                if (networkManager.isConnected()) {
                    apiService.trackProductClick(productId, source, userId)
                }
                database.analyticsDao().trackClick(productId, source, userId, Date())
            } catch (e: Exception) {
                logger.error("Error tracking product click", e)
            }
        }
    }
    
    suspend fun trackAddToCart(productId: Long, quantity: Int, userId: Long? = null) {
        coroutineScope.launch {
            try {
                if (networkManager.isConnected()) {
                    apiService.trackAddToCart(productId, quantity, userId)
                }
                database.analyticsDao().trackAddToCart(productId, quantity, userId, Date())
            } catch (e: Exception) {
                logger.error("Error tracking add to cart", e)
            }
        }
    }
    
    suspend fun trackPurchase(products: List<CartItem>, orderId: Long, userId: Long) {
        coroutineScope.launch {
            try {
                if (networkManager.isConnected()) {
                    apiService.trackPurchase(products.map { it.toTrackingItem() }, orderId, userId)
                }
                products.forEach { item ->
                    database.analyticsDao().trackPurchase(
                        item.product.id,
                        item.quantity,
                        item.product.price,
                        orderId,
                        userId,
                        Date()
                    )
                }
            } catch (e: Exception) {
                logger.error("Error tracking purchase", e)
            }
        }
    }
    
    suspend fun refreshProducts() {
        withContext(Dispatchers.IO) {
            try {
                _refreshState.value = true
                syncWithServer()
                _refreshState.value = false
            } catch (e: Exception) {
                _refreshState.value = false
                logger.error("Error refreshing products", e)
            }
        }
    }
    
    private suspend fun syncWithServer() {
        if (!networkManager.isConnected() || _syncState.value == SyncState.SYNCING) return
        
        try {
            _syncState.value = SyncState.SYNCING
            
            val lastSync = preferencesManager.getLastSyncTime()
            val updates = apiService.getProductUpdates(lastSync)
            
            updates.newProducts?.let { products ->
                saveProductsToDatabase(products.map { it.toProduct() })
            }
            
            updates.updatedProducts?.let { products ->
                updateProductsInDatabase(products.map { it.toProduct() })
            }
            
            updates.deletedProductIds?.let { ids ->
                deleteProductsFromDatabase(ids)
            }
            
            preferencesManager.setLastSyncTime(System.currentTimeMillis())
            lastSyncTime = System.currentTimeMillis()
            
            _syncState.value = SyncState.SUCCESS
        } catch (e: Exception) {
            _syncState.value = SyncState.ERROR
            logger.error("Sync failed", e)
        }
    }
    
    private suspend fun getProductsByIds(ids: List<Long>): List<Product> {
        return ids.mapNotNull { id ->
            when (val result = getProductById(id)) {
                is Result.Success -> result.data
                else -> null
            }
        }
    }
    
    private suspend fun getSimilarProducts(product: Product, limit: Int): List<Product> {
        return productDao.getSimilarProducts(
            product.category.id,
            product.price.toDouble(),
            product.id,
            limit
        ).map { it.toProduct() }
    }
    
    private suspend fun generateLocalRecommendations(userId: Long, limit: Int): List<Product> {
        val viewedProducts = database.analyticsDao().getUserViewedProducts(userId, 20)
        val purchasedProducts = database.analyticsDao().getUserPurchasedProducts(userId, 10)
        
        val categoryIds = (viewedProducts + purchasedProducts)
            .map { it.categoryId }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(3)
            .map { it.key }
        
        return productDao.getProductsByCategories(categoryIds, 0, limit)
            .map { it.toProduct() }
    }
    
    private suspend fun searchLocalProducts(params: SearchParams): List<Product> {
        return productDao.searchProducts(
            params.query,
            params.categoryId,
            params.brandId,
            params.minPrice?.toDouble(),
            params.maxPrice?.toDouble(),
            params.inStock
        ).map { it.toProduct() }
    }
    
    private suspend fun generateLocalFilters(categoryId: Long?): ProductFilters {
        val products = categoryId?.let {
            productDao.getProductsByCategory(it).map { product -> product.toProduct() }
        } ?: productDao.getAllProducts().map { it.toProduct() }
        
        return ProductFilters(
            priceRange = PriceRange(
                min = products.minByOrNull { it.price }?.price ?: BigDecimal.ZERO,
                max = products.maxByOrNull { it.price }?.price ?: BigDecimal.ZERO
            ),
            brands = products.map { it.brand }.distinct(),
            categories = products.map { it.category }.distinct(),
            colors = products.mapNotNull { it.color }.distinct(),
            sizes = products.mapNotNull { it.size }.distinct(),
            materials = products.flatMap { it.material ?: emptyList() }.distinct(),
            features = products.flatMap { it.features }.distinct(),
            tags = products.flatMap { it.tags }.distinct(),
            ratings = listOf(4.0f, 3.0f, 2.0f, 1.0f)
        )
    }
    
    private suspend fun getAllSubcategoryIds(categoryId: Long): List<Long> {
        val subcategories = mutableListOf(categoryId)
        val queue = mutableListOf(categoryId)
        
        while (queue.isNotEmpty()) {
            val current = queue.removeAt(0)
            val children = database.categoryDao().getChildCategories(current)
            children.forEach { child ->
                subcategories.add(child.id)
                queue.add(child.id)
            }
        }
        
        return subcategories
    }
    
    private fun updateCaches(products: List<ProductEntity>) {
        products.forEach { entity ->
            val product = entity.toProduct()
            productCache[product.id] = product
        }
    }
    
    private fun updateCaches(products: List<Product>) {
        products.forEach { product ->
            productCache[product.id] = product
        }
    }
    
    private fun categorizeProducts(products: List<ProductEntity>) {
        val productList = products.map { it.toProduct() }
        
        _featuredProducts.postValue(productList.filter { it.isFeatured }.take(10))
        _newArrivals.postValue(productList.filter { it.isNewArrival }.take(20))
        _bestSellers.postValue(productList.filter { it.isBestSeller }.take(20))
        _onSaleProducts.postValue(productList.filter { it.isOnSale }.take(20))
    }
    
    private suspend fun saveProductsToDatabase(products: List<Product>) {
        products.forEach { product ->
            productDao.insertProduct(product.toEntity())
        }
    }
    
    private suspend fun saveProductToDatabase(product: Product) {
        productDao.insertProduct(product.toEntity())
    }
    
    private suspend fun updateProductsInDatabase(products: List<Product>) {
        products.forEach { product ->
            productDao.updateProduct(product.toEntity())
        }
    }
    
    private suspend fun deleteProductsFromDatabase(ids: List<Long>) {
        productDao.deleteProducts(ids)
    }
    
    private suspend fun updateProductRating(productId: Long) {
        val reviews = database.reviewDao().getProductReviews(productId, 0, Int.MAX_VALUE)
        val averageRating = reviews.map { it.rating }.average()
        productDao.updateProductRating(productId, averageRating.toFloat(), reviews.size)
    }
    
    private fun addToRecentlyViewed(productId: Long) {
        recentlyViewedIds.remove(productId)
        recentlyViewedIds.add(0, productId)
        if (recentlyViewedIds.size > 20) {
            recentlyViewedIds.removeAt(recentlyViewedIds.size - 1)
        }
        updateRecentlyViewedProducts()
    }
    
    private fun updateRecentlyViewedProducts() {
        coroutineScope.launch {
            val products = recentlyViewedIds.mapNotNull { productCache[it] }
            _recentlyViewed.postValue(products)
        }
    }
    
    private suspend fun incrementProductView(productId: Long) {
        productDao.incrementProductView(productId)
    }
    
    private suspend fun saveSearchHistory(query: String, resultCount: Int) {
        database.searchHistoryDao().insertSearch(
            SearchHistoryEntity(
                query = query,
                timestamp = Date(),
                resultCount = resultCount
            )
        )
    }
    
    private fun generateCacheKey(
        page: Int,
        pageSize: Int,
        sortBy: SortOption,
        filters: ProductFilters?
    ): String {
        return "products_${page}_${pageSize}_${sortBy.value}_${filters?.hashCode() ?: 0}"
    }
    
    private fun isCacheExpired(key: String): Boolean {
        val cacheTime = cacheManager.getCacheTime(key)
        return System.currentTimeMillis() - cacheTime > cacheExpiry
    }
    
    private fun shouldSync(): Boolean {
        return System.currentTimeMillis() - lastSyncTime > syncInterval
    }
    
    private suspend fun loadInitialData() {
        val hasData = productDao.getProductCount() > 0
        if (!hasData && networkManager.isConnected()) {
            val products = apiService.getInitialProducts().map { it.toProduct() }
            saveProductsToDatabase(products)
        }
    }
    
    private fun loadUserPreferences() {
        wishlistIds.addAll(preferencesManager.getWishlistIds())
        cartIds.putAll(preferencesManager.getCartItems())
        compareIds.addAll(preferencesManager.getCompareIds())
    }
    
    private fun setupCachePolicy() {
        cacheManager.setMaxCacheSize(100 * 1024 * 1024)
        cacheManager.setDefaultExpiry(cacheExpiry)
    }
    
    fun clearCache() {
        productCache.clear()
        categoryCache.clear()
        brandCache.clear()
        cacheManager.clearAll()
    }
    
    fun onCleared() {
        coroutineScope.cancel()
        preferencesManager.saveWishlistIds(wishlistIds.toList())
        preferencesManager.saveCartItems(cartIds)
        preferencesManager.saveCompareIds(compareIds.toList())
    }
}

enum class SortOption(val value: String) {
    POPULARITY("popularity"),
    PRICE_LOW_TO_HIGH("price_asc"),
    PRICE_HIGH_TO_LOW("price_desc"),
    RATING("rating"),
    NEWEST("newest"),
    RELEVANCE("relevance"),
    NAME_A_TO_Z("name_asc"),
    NAME_Z_TO_A("name_desc"),
    DISCOUNT("discount"),
    TRENDING("trending")
}

enum class NetworkState {
    CONNECTED,
    DISCONNECTED,
    CONNECTING
}

enum class SyncState {
    IDLE,
    SYNCING,
    SUCCESS,
    ERROR
}

enum class AvailabilityStatus {
    IN_STOCK,
    LIMITED_STOCK,
    OUT_OF_STOCK,
    BACK_ORDER,
    PRE_ORDER,
    DISCONTINUED
}

data class ProductFilters(
    val priceRange: PriceRange? = null,
    val brands: List<Brand>? = null,
    val categories: List<ProductCategory>? = null,
    val colors: List<String>? = null,
    val sizes: List<String>? = null,
    val materials: List<String>? = null,
    val features: List<ProductFeature>? = null,
    val tags: List<String>? = null,
    val ratings: List<Float>? = null,
    val inStock: Boolean? = null,
    val onSale: Boolean? = null,
    val freeShipping: Boolean? = null
) {
    fun toQueryMap(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        priceRange?.let {
            map["min_price"] = it.min.toString()
            map["max_price"] = it.max.toString()
        }
        brands?.let { map["brands"] = it.joinToString(",") { brand -> brand.id.toString() } }
        categories?.let { map["categories"] = it.joinToString(",") { cat -> cat.id.toString() } }
        colors?.let { map["colors"] = it.joinToString(",") }
        sizes?.let { map["sizes"] = it.joinToString(",") }
        materials?.let { map["materials"] = it.joinToString(",") }
        tags?.let { map["tags"] = it.joinToString(",") }
        ratings?.let { map["min_rating"] = it.minOrNull().toString() }
        inStock?.let { map["in_stock"] = it.toString() }
        onSale?.let { map["on_sale"] = it.toString() }
        freeShipping?.let { map["free_shipping"] = it.toString() }
        return map
    }
}

data class PriceRange(
    val min: BigDecimal,
    val max: BigDecimal
)

data class SearchParams(
    val query: String,
    val categoryId: Long? = null,
    val brandId: Long? = null,
    val minPrice: BigDecimal? = null,
    val maxPrice: BigDecimal? = null,
    val inStock: Boolean = false,
    val sortBy: SortOption = SortOption.RELEVANCE
) {
    fun toQueryMap(): Map<String, String> {
        val map = mutableMapOf("q" to query, "sort" to sortBy.value)
        categoryId?.let { map["category"] = it.toString() }
        brandId?.let { map["brand"] = it.toString() }
        minPrice?.let { map["min_price"] = it.toString() }
        maxPrice?.let { map["max_price"] = it.toString() }
        if (inStock) map["in_stock"] = "true"
        return map
    }
}

data class CartItem(
    val product: Product,
    val quantity: Int,
    val selected: Boolean = true
)

data class Review(
    val id: Long,
    val productId: Long,
    val userId: Long,
    val userName: String,
    val rating: Int,
    val title: String,
    val content: String,
    val images: List<String>? = null,
    val verified: Boolean,
    val helpful: Int,
    val createdAt: Date,
    val updatedAt: Date
)