package com.example.ecommerce.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.ecommerce.models.*
import com.example.ecommerce.repository.*
import com.example.ecommerce.utils.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.math.BigDecimal
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class ProductViewModel @Inject constructor(
    application: Application,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository,
    private val analyticsManager: AnalyticsManager,
    private val notificationManager: NotificationManager,
    private val connectivityManager: ConnectivityManager,
    private val performanceMonitor: PerformanceMonitor,
    private val crashlytics: CrashlyticsManager,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {
    
    private val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products
    
    private val _selectedProduct = MutableLiveData<Product?>()
    val selectedProduct: LiveData<Product?> = _selectedProduct
    
    private val _featuredProducts = MutableLiveData<List<Product>>()
    val featuredProducts: LiveData<List<Product>> = _featuredProducts
    
    private val _newArrivals = MutableLiveData<List<Product>>()
    val newArrivals: LiveData<List<Product>> = _newArrivals
    
    private val _bestSellers = MutableLiveData<List<Product>>()
    val bestSellers: LiveData<List<Product>> = _bestSellers
    
    private val _onSaleProducts = MutableLiveData<List<Product>>()
    val onSaleProducts: LiveData<List<Product>> = _onSaleProducts
    
    private val _recommendedProducts = MutableLiveData<List<Product>>()
    val recommendedProducts: LiveData<List<Product>> = _recommendedProducts
    
    private val _relatedProducts = MutableLiveData<List<Product>>()
    val relatedProducts: LiveData<List<Product>> = _relatedProducts
    
    private val _searchResults = MutableLiveData<List<Product>>()
    val searchResults: LiveData<List<Product>> = _searchResults
    
    private val _cartItems = MutableLiveData<List<CartItem>>()
    val cartItems: LiveData<List<CartItem>> = _cartItems
    
    private val _wishlistItems = MutableLiveData<List<Product>>()
    val wishlistItems: LiveData<List<Product>> = _wishlistItems
    
    private val _compareItems = MutableLiveData<List<Product>>()
    val compareItems: LiveData<List<Product>> = _compareItems
    
    private val _recentlyViewedItems = MutableLiveData<List<Product>>()
    val recentlyViewedItems: LiveData<List<Product>> = productRepository.recentlyViewed
    
    private val _categories = MutableLiveData<List<ProductCategory>>()
    val categories: LiveData<List<ProductCategory>> = _categories
    
    private val _brands = MutableLiveData<List<Brand>>()
    val brands: LiveData<List<Brand>> = _brands
    
    private val _filters = MutableLiveData<ProductFilters>()
    val filters: LiveData<ProductFilters> = _filters
    
    private val _selectedCategory = MutableLiveData<ProductCategory?>()
    val selectedCategory: LiveData<ProductCategory?> = _selectedCategory
    
    private val _selectedBrand = MutableLiveData<Brand?>()
    val selectedBrand: LiveData<Brand?> = _selectedBrand
    
    private val _sortOption = MutableLiveData(SortOption.POPULARITY)
    val sortOption: LiveData<SortOption> = _sortOption
    
    private val _viewMode = MutableLiveData(ViewMode.GRID)
    val viewMode: LiveData<ViewMode> = _viewMode
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()
    
    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()
    
    private val _cartCount = MutableStateFlow(0)
    val cartCount: StateFlow<Int> = _cartCount.asStateFlow()
    
    private val _wishlistCount = MutableStateFlow(0)
    val wishlistCount: StateFlow<Int> = _wishlistCount.asStateFlow()
    
    private val _totalPrice = MutableStateFlow(BigDecimal.ZERO)
    val totalPrice: StateFlow<BigDecimal> = _totalPrice.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _priceRange = MutableStateFlow<PriceRange?>(null)
    val priceRange: StateFlow<PriceRange?> = _priceRange.asStateFlow()
    
    private val _selectedFilters = MutableStateFlow(SelectedFilters())
    val selectedFilters: StateFlow<SelectedFilters> = _selectedFilters.asStateFlow()
    
    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()
    
    private val _hasMorePages = MutableStateFlow(true)
    val hasMorePages: StateFlow<Boolean> = _hasMorePages.asStateFlow()
    
    private val _productDetails = MutableStateFlow<ProductDetails?>(null)
    val productDetails: StateFlow<ProductDetails?> = _productDetails.asStateFlow()
    
    private val _productReviews = MutableStateFlow<List<Review>>(emptyList())
    val productReviews: StateFlow<List<Review>> = _productReviews.asStateFlow()
    
    private val _productQuestions = MutableStateFlow<List<ProductQuestion>>(emptyList())
    val productQuestions: StateFlow<List<ProductQuestion>> = _productQuestions.asStateFlow()
    
    private val _productAvailability = MutableStateFlow<AvailabilityStatus?>(null)
    val productAvailability: StateFlow<AvailabilityStatus?> = _productAvailability.asStateFlow()
    
    private val _selectedVariant = MutableStateFlow<ProductVariant?>(null)
    val selectedVariant: StateFlow<ProductVariant?> = _selectedVariant.asStateFlow()
    
    private val _productVariants = MutableStateFlow<List<ProductVariant>>(emptyList())
    val productVariants: StateFlow<List<ProductVariant>> = _productVariants.asStateFlow()
    
    private val _selectedQuantity = MutableStateFlow(1)
    val selectedQuantity: StateFlow<Int> = _selectedQuantity.asStateFlow()
    
    private val _shippingOptions = MutableStateFlow<List<ShippingOption>>(emptyList())
    val shippingOptions: StateFlow<List<ShippingOption>> = _shippingOptions.asStateFlow()
    
    private val _estimatedDelivery = MutableStateFlow<DeliveryEstimate?>(null)
    val estimatedDelivery: StateFlow<DeliveryEstimate?> = _estimatedDelivery.asStateFlow()
    
    private val _promotions = MutableStateFlow<List<Promotion>>(emptyList())
    val promotions: StateFlow<List<Promotion>> = _promotions.asStateFlow()
    
    private val _appliedCoupon = MutableStateFlow<Coupon?>(null)
    val appliedCoupon: StateFlow<Coupon?> = _appliedCoupon.asStateFlow()
    
    private val _discountAmount = MutableStateFlow(BigDecimal.ZERO)
    val discountAmount: StateFlow<BigDecimal> = _discountAmount.asStateFlow()
    
    private val _taxAmount = MutableStateFlow(BigDecimal.ZERO)
    val taxAmount: StateFlow<BigDecimal> = _taxAmount.asStateFlow()
    
    private val _shippingCost = MutableStateFlow(BigDecimal.ZERO)
    val shippingCost: StateFlow<BigDecimal> = _shippingCost.asStateFlow()
    
    private val _finalPrice = MutableStateFlow(BigDecimal.ZERO)
    val finalPrice: StateFlow<BigDecimal> = _finalPrice.asStateFlow()
    
    private val _orderSummary = MutableStateFlow<OrderSummary?>(null)
    val orderSummary: StateFlow<OrderSummary?> = _orderSummary.asStateFlow()
    
    private val _checkoutState = MutableStateFlow(CheckoutState.CART)
    val checkoutState: StateFlow<CheckoutState> = _checkoutState.asStateFlow()
    
    private val _paymentMethods = MutableStateFlow<List<PaymentMethod>>(emptyList())
    val paymentMethods: StateFlow<List<PaymentMethod>> = _paymentMethods.asStateFlow()
    
    private val _selectedPaymentMethod = MutableStateFlow<PaymentMethod?>(null)
    val selectedPaymentMethod: StateFlow<PaymentMethod?> = _selectedPaymentMethod.asStateFlow()
    
    private val _shippingAddress = MutableStateFlow<Address?>(null)
    val shippingAddress: StateFlow<Address?> = _shippingAddress.asStateFlow()
    
    private val _billingAddress = MutableStateFlow<Address?>(null)
    val billingAddress: StateFlow<Address?> = _billingAddress.asStateFlow()
    
    private val _orderConfirmation = MutableStateFlow<OrderConfirmation?>(null)
    val orderConfirmation: StateFlow<OrderConfirmation?> = _orderConfirmation.asStateFlow()
    
    private val searchDebounceJob = AtomicReference<Job?>(null)
    private val loadMoreDebounceJob = AtomicReference<Job?>(null)
    private val isLoadingMore = AtomicBoolean(false)
    
    private val pageSize = 20
    private var currentUserId: Long? = null
    
    init {
        initializeViewModel()
        observeConnectivity()
        loadInitialData()
        setupAutoRefresh()
    }
    
    private fun initializeViewModel() {
        savedStateHandle.get<Long>("productId")?.let { productId ->
            loadProductDetails(productId)
        }
        
        savedStateHandle.get<Long>("categoryId")?.let { categoryId ->
            loadProductsByCategory(categoryId)
        }
        
        savedStateHandle.get<String>("searchQuery")?.let { query ->
            searchProducts(query)
        }
        
        currentUserId = userRepository.getCurrentUserId()
    }
    
    private fun observeConnectivity() {
        connectivityManager.isConnected.onEach { isConnected ->
            if (isConnected && _products.value.isNullOrEmpty()) {
                loadProducts()
            }
        }.launchIn(viewModelScope)
    }
    
    private fun loadInitialData() {
        viewModelScope.launch {
            launch { loadCategories() }
            launch { loadBrands() }
            launch { loadFeaturedProducts() }
            launch { loadNewArrivals() }
            launch { loadBestSellers() }
            launch { loadOnSaleProducts() }
            currentUserId?.let { launch { loadRecommendedProducts(it) } }
            launch { loadCart() }
            currentUserId?.let { launch { loadWishlist(it) } }
        }
    }
    
    private fun setupAutoRefresh() {
        viewModelScope.launch {
            while (isActive) {
                delay(300000)
                if (connectivityManager.isConnected.value) {
                    refreshProducts()
                }
            }
        }
    }
    
    fun loadProducts(page: Int = 1, append: Boolean = false) {
        if (_isLoading.value || (append && !_hasMorePages.value)) return
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                performanceMonitor.startTrace("load_products")
                
                val filters = _selectedFilters.value.toProductFilters()
                val result = productRepository.getAllProducts(
                    page = page,
                    pageSize = pageSize,
                    sortBy = _sortOption.value ?: SortOption.POPULARITY,
                    filters = filters
                )
                
                when (result) {
                    is Result.Success -> {
                        val products = result.data
                        if (append) {
                            _products.value = (_products.value ?: emptyList()) + products
                        } else {
                            _products.value = products
                        }
                        _currentPage.value = page
                        _hasMorePages.value = products.size == pageSize
                        
                        analyticsManager.logEvent("products_loaded", mapOf(
                            "page" to page,
                            "count" to products.size,
                            "sort" to (_sortOption.value?.name ?: "")
                        ))
                    }
                    is Result.Error -> {
                        handleError(result.exception, "Failed to load products")
                    }
                }
            } finally {
                _isLoading.value = false
                performanceMonitor.stopTrace("load_products")
            }
        }
    }
    
    fun loadMoreProducts() {
        if (isLoadingMore.get()) return
        
        loadMoreDebounceJob.get()?.cancel()
        loadMoreDebounceJob.set(
            viewModelScope.launch {
                delay(300)
                if (isLoadingMore.compareAndSet(false, true)) {
                    try {
                        loadProducts(_currentPage.value + 1, append = true)
                    } finally {
                        isLoadingMore.set(false)
                    }
                }
            }
        )
    }
    
    fun refreshProducts() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                productRepository.refreshProducts()
                loadProducts(1, append = false)
                loadInitialData()
            } finally {
                _isRefreshing.value = false
            }
        }
    }
    
    fun searchProducts(query: String) {
        _searchQuery.value = query
        
        searchDebounceJob.get()?.cancel()
        searchDebounceJob.set(
            viewModelScope.launch {
                delay(500)
                performSearch(query)
            }
        )
    }
    
    private suspend fun performSearch(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        
        try {
            _isLoading.value = true
            val result = productRepository.searchProducts(
                query = query,
                category = _selectedCategory.value?.id,
                brand = _selectedBrand.value?.id,
                minPrice = _priceRange.value?.min,
                maxPrice = _priceRange.value?.max,
                inStock = _selectedFilters.value.inStockOnly,
                sortBy = _sortOption.value ?: SortOption.RELEVANCE
            )
            
            when (result) {
                is Result.Success -> {
                    _searchResults.value = result.data
                    analyticsManager.logSearch(query, result.data.size)
                }
                is Result.Error -> {
                    handleError(result.exception, "Search failed")
                }
            }
        } finally {
            _isLoading.value = false
        }
    }
    
    fun loadProductDetails(productId: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                val productResult = productRepository.getProductById(productId)
                when (productResult) {
                    is Result.Success -> {
                        val product = productResult.data
                        _selectedProduct.value = product
                        
                        launch { loadProductReviews(productId) }
                        launch { loadRelatedProducts(productId) }
                        launch { loadProductVariants(productId) }
                        launch { checkProductAvailability(productId) }
                        launch { loadProductQuestions(productId) }
                        launch { calculateShipping(product) }
                        
                        currentUserId?.let {
                            productRepository.trackProductView(productId, it)
                        }
                        
                        analyticsManager.logViewItem(product)
                    }
                    is Result.Error -> {
                        handleError(productResult.exception, "Failed to load product details")
                    }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private suspend fun loadProductReviews(productId: Long) {
        val result = productRepository.getProductReviews(productId)
        if (result is Result.Success) {
            _productReviews.value = result.data
        }
    }
    
    private suspend fun loadRelatedProducts(productId: Long) {
        val result = productRepository.getRelatedProducts(productId)
        if (result is Result.Success) {
            _relatedProducts.value = result.data
        }
    }
    
    private suspend fun loadProductVariants(productId: Long) {
        val result = productRepository.getProductVariants(productId)
        if (result is Result.Success) {
            _productVariants.value = result.data
            if (result.data.isNotEmpty()) {
                _selectedVariant.value = result.data.firstOrNull { it.isDefault } ?: result.data[0]
            }
        }
    }
    
    private suspend fun checkProductAvailability(productId: Long, quantity: Int = 1) {
        val result = productRepository.checkProductAvailability(productId, quantity)
        if (result is Result.Success) {
            _productAvailability.value = result.data
        }
    }
    
    private suspend fun loadProductQuestions(productId: Long) {
        _selectedProduct.value?.questions?.let { questions ->
            _productQuestions.value = questions.filter { it.answer != null }
        }
    }
    
    private suspend fun calculateShipping(product: Product) {
        val options = mutableListOf<ShippingOption>()
        
        options.add(ShippingOption(
            id = 1,
            name = "Standard Shipping",
            cost = if (product.shippingInfo.freeShipping) BigDecimal.ZERO else BigDecimal("5.99"),
            estimatedDays = product.shippingInfo.estimatedDays,
            description = "Delivered in ${product.shippingInfo.estimatedDays.first}-${product.shippingInfo.estimatedDays.last} business days"
        ))
        
        if (product.shippingInfo.expeditedAvailable) {
            options.add(ShippingOption(
                id = 2,
                name = "Express Shipping",
                cost = BigDecimal("14.99"),
                estimatedDays = 1..3,
                description = "Delivered in 1-3 business days"
            ))
            
            options.add(ShippingOption(
                id = 3,
                name = "Next Day Delivery",
                cost = BigDecimal("24.99"),
                estimatedDays = 1..1,
                description = "Delivered next business day"
            ))
        }
        
        _shippingOptions.value = options
    }
    
    fun selectProductVariant(variant: ProductVariant) {
        _selectedVariant.value = variant
        viewModelScope.launch {
            checkProductAvailability(variant.productId, _selectedQuantity.value)
        }
    }
    
    fun updateQuantity(quantity: Int) {
        _selectedQuantity.value = quantity.coerceIn(1, 99)
        _selectedProduct.value?.let { product ->
            viewModelScope.launch {
                checkProductAvailability(product.id, quantity)
            }
        }
    }
    
    fun addToCart(productId: Long? = null, quantity: Int? = null) {
        viewModelScope.launch {
            try {
                val id = productId ?: _selectedProduct.value?.id ?: return@launch
                val qty = quantity ?: _selectedQuantity.value
                
                val result = productRepository.addToCart(id, qty)
                when (result) {
                    is Result.Success -> {
                        _successMessage.value = "Added to cart"
                        loadCart()
                        
                        currentUserId?.let {
                            productRepository.trackAddToCart(id, qty, it)
                        }
                        
                        _selectedProduct.value?.let { product ->
                            analyticsManager.logAddToCart(product, qty)
                        }
                    }
                    is Result.Error -> {
                        _errorMessage.value = result.exception.message ?: "Failed to add to cart"
                    }
                }
            } catch (e: Exception) {
                handleError(e, "Failed to add to cart")
            }
        }
    }
    
    fun removeFromCart(productId: Long) {
        viewModelScope.launch {
            try {
                val result = productRepository.updateCartQuantity(productId, 0)
                when (result) {
                    is Result.Success -> {
                        loadCart()
                        _successMessage.value = "Removed from cart"
                    }
                    is Result.Error -> {
                        handleError(result.exception, "Failed to remove from cart")
                    }
                }
            } catch (e: Exception) {
                handleError(e, "Failed to remove from cart")
            }
        }
    }
    
    fun updateCartQuantity(productId: Long, quantity: Int) {
        viewModelScope.launch {
            try {
                val result = productRepository.updateCartQuantity(productId, quantity)
                when (result) {
                    is Result.Success -> {
                        loadCart()
                    }
                    is Result.Error -> {
                        handleError(result.exception, "Failed to update quantity")
                    }
                }
            } catch (e: Exception) {
                handleError(e, "Failed to update quantity")
            }
        }
    }
    
    private suspend fun loadCart() {
        val result = productRepository.getCartItems()
        if (result is Result.Success) {
            _cartItems.value = result.data
            _cartCount.value = result.data.sumOf { it.quantity }
            calculateCartTotal()
        }
    }
    
    private fun calculateCartTotal() {
        val items = _cartItems.value ?: return
        val subtotal = items.sumOf { item ->
            (item.product.discountPrice ?: item.product.price) * BigDecimal(item.quantity)
        }
        _totalPrice.value = subtotal
        
        val discount = _appliedCoupon.value?.let { coupon ->
            when (coupon.type) {
                "percentage" -> subtotal * BigDecimal(coupon.discount / 100)
                "fixed" -> BigDecimal(coupon.discount)
                else -> BigDecimal.ZERO
            }
        } ?: BigDecimal.ZERO
        _discountAmount.value = discount
        
        val tax = (subtotal - discount) * BigDecimal("0.1")
        _taxAmount.value = tax
        
        _finalPrice.value = subtotal - discount + tax + _shippingCost.value
    }
    
    fun addToWishlist(productId: Long? = null) {
        viewModelScope.launch {
            try {
                val id = productId ?: _selectedProduct.value?.id ?: return@launch
                val userId = currentUserId ?: return@launch
                
                val result = productRepository.addToWishlist(id, userId)
                when (result) {
                    is Result.Success -> {
                        _successMessage.value = "Added to wishlist"
                        loadWishlist(userId)
                        
                        _selectedProduct.value?.let { product ->
                            analyticsManager.logAddToWishlist(product)
                        }
                    }
                    is Result.Error -> {
                        handleError(result.exception, "Failed to add to wishlist")
                    }
                }
            } catch (e: Exception) {
                handleError(e, "Failed to add to wishlist")
            }
        }
    }
    
    fun removeFromWishlist(productId: Long) {
        viewModelScope.launch {
            try {
                val userId = currentUserId ?: return@launch
                
                val result = productRepository.removeFromWishlist(productId, userId)
                when (result) {
                    is Result.Success -> {
                        _successMessage.value = "Removed from wishlist"
                        loadWishlist(userId)
                    }
                    is Result.Error -> {
                        handleError(result.exception, "Failed to remove from wishlist")
                    }
                }
            } catch (e: Exception) {
                handleError(e, "Failed to remove from wishlist")
            }
        }
    }
    
    private suspend fun loadWishlist(userId: Long) {
        val result = productRepository.getWishlist(userId)
        if (result is Result.Success) {
            _wishlistItems.value = result.data
            _wishlistCount.value = result.data.size
        }
    }
    
    fun addToCompare(productId: Long) {
        viewModelScope.launch {
            try {
                val result = productRepository.addToCompare(productId)
                when (result) {
                    is Result.Success -> {
                        loadCompareItems()
                        _successMessage.value = "Added to compare"
                    }
                    is Result.Error -> {
                        _errorMessage.value = result.exception.message ?: "Failed to add to compare"
                    }
                }
            } catch (e: Exception) {
                handleError(e, "Failed to add to compare")
            }
        }
    }
    
    fun removeFromCompare(productId: Long) {
        viewModelScope.launch {
            try {
                val result = productRepository.removeFromCompare(productId)
                when (result) {
                    is Result.Success -> {
                        loadCompareItems()
                        _successMessage.value = "Removed from compare"
                    }
                    is Result.Error -> {
                        handleError(result.exception, "Failed to remove from compare")
                    }
                }
            } catch (e: Exception) {
                handleError(e, "Failed to remove from compare")
            }
        }
    }
    
    private suspend fun loadCompareItems() {
        val result = productRepository.getCompareProducts()
        if (result is Result.Success) {
            _compareItems.value = result.data
        }
    }
    
    fun loadProductsByCategory(categoryId: Long, includeSubcategories: Boolean = true) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = productRepository.getProductsByCategory(
                    categoryId = categoryId,
                    includeSubcategories = includeSubcategories,
                    page = _currentPage.value,
                    pageSize = pageSize
                )
                
                when (result) {
                    is Result.Success -> {
                        _products.value = result.data
                        analyticsManager.logViewCategory(categoryId.toString())
                    }
                    is Result.Error -> {
                        handleError(result.exception, "Failed to load category products")
                    }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadProductsByBrand(brandId: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = productRepository.getProductsByBrand(
                    brandId = brandId,
                    page = _currentPage.value,
                    pageSize = pageSize
                )
                
                when (result) {
                    is Result.Success -> {
                        _products.value = result.data
                    }
                    is Result.Error -> {
                        handleError(result.exception, "Failed to load brand products")
                    }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private suspend fun loadFeaturedProducts() {
        val result = productRepository.getFeaturedProducts()
        if (result is Result.Success) {
            _featuredProducts.value = result.data
        }
    }
    
    private suspend fun loadNewArrivals() {
        val result = productRepository.getNewArrivals()
        if (result is Result.Success) {
            _newArrivals.value = result.data
        }
    }
    
    private suspend fun loadBestSellers() {
        val result = productRepository.getBestSellers()
        if (result is Result.Success) {
            _bestSellers.value = result.data
        }
    }
    
    private suspend fun loadOnSaleProducts() {
        val result = productRepository.getOnSaleProducts()
        if (result is Result.Success) {
            _onSaleProducts.value = result.data
        }
    }
    
    private suspend fun loadRecommendedProducts(userId: Long) {
        val result = productRepository.getRecommendedProducts(userId)
        if (result is Result.Success) {
            _recommendedProducts.value = result.data
        }
    }
    
    private suspend fun loadCategories() {
        val result = productRepository.getCategories()
        if (result is Result.Success) {
            _categories.value = result.data
        }
    }
    
    private suspend fun loadBrands() {
        val result = productRepository.getBrands()
        if (result is Result.Success) {
            _brands.value = result.data
        }
    }
    
    fun selectCategory(category: ProductCategory?) {
        _selectedCategory.value = category
        category?.let {
            loadProductsByCategory(it.id)
        } ?: loadProducts()
    }
    
    fun selectBrand(brand: Brand?) {
        _selectedBrand.value = brand
        brand?.let {
            loadProductsByBrand(it.id)
        } ?: loadProducts()
    }
    
    fun setSortOption(option: SortOption) {
        _sortOption.value = option
        loadProducts()
    }
    
    fun setViewMode(mode: ViewMode) {
        _viewMode.value = mode
    }
    
    fun applyFilters(filters: SelectedFilters) {
        _selectedFilters.value = filters
        loadProducts()
    }
    
    fun setPriceRange(min: BigDecimal, max: BigDecimal) {
        _priceRange.value = PriceRange(min, max)
        _selectedFilters.value = _selectedFilters.value.copy(
            priceRange = PriceRange(min, max)
        )
        loadProducts()
    }
    
    fun clearFilters() {
        _selectedFilters.value = SelectedFilters()
        _priceRange.value = null
        _selectedCategory.value = null
        _selectedBrand.value = null
        loadProducts()
    }
    
    fun applyCoupon(code: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                _appliedCoupon.value = Coupon(
                    code = code,
                    discount = 10.0,
                    type = "percentage",
                    validUntil = Date(System.currentTimeMillis() + 86400000),
                    minimumPurchase = BigDecimal("50")
                )
                calculateCartTotal()
                _successMessage.value = "Coupon applied successfully"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun removeCoupon() {
        _appliedCoupon.value = null
        calculateCartTotal()
    }
    
    fun proceedToCheckout() {
        viewModelScope.launch {
            if (_cartItems.value.isNullOrEmpty()) {
                _errorMessage.value = "Cart is empty"
                return@launch
            }
            
            _checkoutState.value = CheckoutState.ADDRESS
            _navigationEvent.emit(NavigationEvent.GoToCheckout)
        }
    }
    
    fun setShippingAddress(address: Address) {
        _shippingAddress.value = address
        _checkoutState.value = CheckoutState.PAYMENT
    }
    
    fun setBillingAddress(address: Address) {
        _billingAddress.value = address
    }
    
    fun selectPaymentMethod(method: PaymentMethod) {
        _selectedPaymentMethod.value = method
        _checkoutState.value = CheckoutState.REVIEW
    }
    
    fun placeOrder() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                delay(2000)
                
                val confirmation = OrderConfirmation(
                    orderId = "ORD${System.currentTimeMillis()}",
                    orderDate = Date(),
                    estimatedDelivery = Date(System.currentTimeMillis() + 604800000),
                    totalAmount = _finalPrice.value,
                    items = _cartItems.value ?: emptyList(),
                    shippingAddress = _shippingAddress.value!!,
                    billingAddress = _billingAddress.value ?: _shippingAddress.value!!,
                    paymentMethod = _selectedPaymentMethod.value!!,
                    trackingNumber = "TRK${System.currentTimeMillis()}"
                )
                
                _orderConfirmation.value = confirmation
                _checkoutState.value = CheckoutState.CONFIRMATION
                
                currentUserId?.let { userId ->
                    _cartItems.value?.let { items ->
                        productRepository.trackPurchase(items, 1L, userId)
                    }
                }
                
                _cartItems.value?.forEach { item ->
                    analyticsManager.logPurchase(item.product, item.quantity)
                }
                
                productRepository.clearCart()
                loadCart()
                
                _navigationEvent.emit(NavigationEvent.OrderComplete(confirmation.orderId))
                
                notificationManager.showOrderConfirmation(confirmation)
                
            } catch (e: Exception) {
                handleError(e, "Failed to place order")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun submitReview(rating: Int, title: String, content: String, images: List<String>? = null) {
        viewModelScope.launch {
            try {
                val productId = _selectedProduct.value?.id ?: return@launch
                val userId = currentUserId ?: return@launch
                
                val result = productRepository.submitProductReview(
                    productId = productId,
                    userId = userId,
                    rating = rating,
                    title = title,
                    content = content,
                    images = images
                )
                
                when (result) {
                    is Result.Success -> {
                        _successMessage.value = "Review submitted successfully"
                        loadProductReviews(productId)
                    }
                    is Result.Error -> {
                        handleError(result.exception, "Failed to submit review")
                    }
                }
            } catch (e: Exception) {
                handleError(e, "Failed to submit review")
            }
        }
    }
    
    fun navigateToProduct(productId: Long) {
        viewModelScope.launch {
            _navigationEvent.emit(NavigationEvent.GoToProduct(productId))
        }
    }
    
    fun navigateToCategory(categoryId: Long) {
        viewModelScope.launch {
            _navigationEvent.emit(NavigationEvent.GoToCategory(categoryId))
        }
    }
    
    fun navigateToBrand(brandId: Long) {
        viewModelScope.launch {
            _navigationEvent.emit(NavigationEvent.GoToBrand(brandId))
        }
    }
    
    fun navigateToCart() {
        viewModelScope.launch {
            _navigationEvent.emit(NavigationEvent.GoToCart)
        }
    }
    
    fun navigateToWishlist() {
        viewModelScope.launch {
            _navigationEvent.emit(NavigationEvent.GoToWishlist)
        }
    }
    
    fun navigateToCompare() {
        viewModelScope.launch {
            _navigationEvent.emit(NavigationEvent.GoToCompare)
        }
    }
    
    fun shareProduct(product: Product? = null) {
        val productToShare = product ?: _selectedProduct.value ?: return
        viewModelScope.launch {
            _navigationEvent.emit(NavigationEvent.ShareProduct(productToShare))
            analyticsManager.logShare(productToShare)
        }
    }
    
    private fun handleError(exception: Exception, defaultMessage: String) {
        _errorMessage.value = exception.message ?: defaultMessage
        crashlytics.recordException(exception)
        logger.error(defaultMessage, exception)
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
    
    fun clearSuccess() {
        _successMessage.value = null
    }
    
    override fun onCleared() {
        super.onCleared()
        productRepository.onCleared()
        viewModelScope.cancel()
    }
}

enum class ViewMode {
    GRID,
    LIST
}

enum class CheckoutState {
    CART,
    ADDRESS,
    PAYMENT,
    REVIEW,
    CONFIRMATION
}

data class SelectedFilters(
    val categories: List<Long> = emptyList(),
    val brands: List<Long> = emptyList(),
    val priceRange: PriceRange? = null,
    val colors: List<String> = emptyList(),
    val sizes: List<String> = emptyList(),
    val materials: List<String> = emptyList(),
    val features: List<Long> = emptyList(),
    val tags: List<String> = emptyList(),
    val minRating: Float? = null,
    val inStockOnly: Boolean = false,
    val onSaleOnly: Boolean = false,
    val freeShippingOnly: Boolean = false
) {
    fun toProductFilters(): ProductFilters {
        return ProductFilters(
            priceRange = priceRange,
            inStock = if (inStockOnly) true else null,
            onSale = if (onSaleOnly) true else null,
            freeShipping = if (freeShippingOnly) true else null,
            colors = colors.ifEmpty { null },
            sizes = sizes.ifEmpty { null },
            materials = materials.ifEmpty { null },
            tags = tags.ifEmpty { null },
            ratings = minRating?.let { listOf(it) }
        )
    }
}

data class ProductDetails(
    val product: Product,
    val reviews: List<Review>,
    val questions: List<ProductQuestion>,
    val variants: List<ProductVariant>,
    val relatedProducts: List<Product>,
    val frequentlyBoughtTogether: List<Product>
)

data class ShippingOption(
    val id: Long,
    val name: String,
    val cost: BigDecimal,
    val estimatedDays: IntRange,
    val description: String
)

data class DeliveryEstimate(
    val earliestDate: Date,
    val latestDate: Date,
    val expeditedAvailable: Boolean
)

data class OrderSummary(
    val subtotal: BigDecimal,
    val discount: BigDecimal,
    val tax: BigDecimal,
    val shipping: BigDecimal,
    val total: BigDecimal,
    val items: List<CartItem>
)

data class Address(
    val id: Long? = null,
    val name: String,
    val street1: String,
    val street2: String? = null,
    val city: String,
    val state: String,
    val zipCode: String,
    val country: String,
    val phone: String,
    val email: String? = null,
    val isDefault: Boolean = false
)

data class PaymentMethod(
    val id: Long,
    val type: String,
    val name: String,
    val last4: String? = null,
    val expiryMonth: Int? = null,
    val expiryYear: Int? = null,
    val isDefault: Boolean = false
)

data class OrderConfirmation(
    val orderId: String,
    val orderDate: Date,
    val estimatedDelivery: Date,
    val totalAmount: BigDecimal,
    val items: List<CartItem>,
    val shippingAddress: Address,
    val billingAddress: Address,
    val paymentMethod: PaymentMethod,
    val trackingNumber: String
)

sealed class NavigationEvent {
    data class GoToProduct(val productId: Long) : NavigationEvent()
    data class GoToCategory(val categoryId: Long) : NavigationEvent()
    data class GoToBrand(val brandId: Long) : NavigationEvent()
    object GoToCart : NavigationEvent()
    object GoToWishlist : NavigationEvent()
    object GoToCompare : NavigationEvent()
    object GoToCheckout : NavigationEvent()
    data class OrderComplete(val orderId: String) : NavigationEvent()
    data class ShareProduct(val product: Product) : NavigationEvent()
}

data class AtomicReference<T>(private var value: T) {
    fun get(): T = value
    fun set(newValue: T) {
        value = newValue
    }
}