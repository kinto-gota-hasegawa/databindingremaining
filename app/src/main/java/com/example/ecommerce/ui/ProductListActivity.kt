package com.example.ecommerce.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.ecommerce.R
import com.example.ecommerce.adapter.ProductAdapter
import com.example.ecommerce.adapter.CategoryAdapter
import com.example.ecommerce.adapter.BrandAdapter
import com.example.ecommerce.adapter.FilterChipAdapter
import com.example.ecommerce.databinding.ActivityProductListBinding
import com.example.ecommerce.models.*
import com.example.ecommerce.repository.SortOption
import com.example.ecommerce.ui.fragments.*
import com.example.ecommerce.utils.*
import com.example.ecommerce.viewmodel.*
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.slider.RangeSlider
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class ProductListActivity : AppCompatActivity(), 
    ProductAdapter.OnProductClickListener,
    CategoryAdapter.OnCategoryClickListener,
    BrandAdapter.OnBrandClickListener,
    NavigationView.OnNavigationItemSelectedListener {

    @Inject lateinit var analyticsManager: AnalyticsManager
    @Inject lateinit var performanceMonitor: PerformanceMonitor
    @Inject lateinit var imageLoader: ImageLoader
    @Inject lateinit var networkMonitor: NetworkMonitor
    @Inject lateinit var preferences: PreferencesManager
    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var shareHelper: ShareHelper
    @Inject lateinit var deepLinkHandler: DeepLinkHandler
    @Inject lateinit var themeManager: ThemeManager
    @Inject lateinit var localizationManager: LocalizationManager
    @Inject lateinit var permissionManager: PermissionManager
    @Inject lateinit var biometricManager: BiometricManager
    @Inject lateinit var crashReporter: CrashReporter
    @Inject lateinit var logger: Logger
    
    private lateinit var binding: ActivityProductListBinding
    private val viewModel: ProductViewModel by viewModels()
    
    private lateinit var productAdapter: ProductAdapter
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var brandAdapter: BrandAdapter
    private lateinit var filterChipAdapter: FilterChipAdapter
    
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var toolbar: Toolbar
    private lateinit var searchView: SearchView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: CircularProgressIndicator
    private lateinit var emptyView: View
    private lateinit var errorView: View
    private lateinit var filterButton: FloatingActionButton
    private lateinit var scrollToTopButton: ExtendedFloatingActionButton
    private lateinit var coordinatorLayout: CoordinatorLayout
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var tabLayout: TabLayout
    private lateinit var filterChipGroup: ChipGroup
    private lateinit var sortChipGroup: ChipGroup
    private lateinit var viewModeToggle: ImageButton
    private lateinit var cartBadge: BadgeDrawable
    private lateinit var wishlistBadge: BadgeDrawable
    
    private var filterBottomSheet: BottomSheetDialog? = null
    private var sortBottomSheet: BottomSheetDialog? = null
    private var currentViewMode = ViewMode.GRID
    private var isLoading = false
    private var selectedFilters = SelectedFilters()
    private var searchQuery = ""
    private var currentCategory: ProductCategory? = null
    private var currentBrand: Brand? = null
    
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        performanceMonitor.startTrace("product_list_onCreate")
        
        themeManager.applyTheme(this)
        localizationManager.applyLocale(this)
        
        binding = ActivityProductListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        initializeViews()
        setupToolbar()
        setupDrawer()
        setupBottomNavigation()
        setupRecyclerView()
        setupSwipeRefresh()
        setupSearch()
        setupFilters()
        setupObservers()
        setupListeners()
        checkPermissions()
        handleDeepLink()
        loadInitialData()
        
        performanceMonitor.stopTrace("product_list_onCreate")
        
        analyticsManager.logScreenView("ProductList", this::class.java.simpleName)
    }
    
    private fun initializeViews() {
        drawerLayout = binding.drawerLayout
        navigationView = binding.navigationView
        bottomNavigationView = binding.bottomNavigation
        toolbar = binding.toolbar
        swipeRefreshLayout = binding.swipeRefreshLayout
        recyclerView = binding.recyclerView
        progressBar = binding.progressBar
        emptyView = binding.emptyView
        errorView = binding.errorView
        filterButton = binding.filterFab
        scrollToTopButton = binding.scrollToTopFab
        coordinatorLayout = binding.coordinatorLayout
        appBarLayout = binding.appBarLayout
        tabLayout = binding.tabLayout
        filterChipGroup = binding.filterChipGroup
        sortChipGroup = binding.sortChipGroup
        viewModeToggle = binding.viewModeToggle
        
        setupBadges()
    }
    
    private fun setupBadges() {
        cartBadge = bottomNavigationView.getOrCreateBadge(R.id.navigation_cart)
        wishlistBadge = bottomNavigationView.getOrCreateBadge(R.id.navigation_wishlist)
        
        cartBadge.isVisible = false
        wishlistBadge.isVisible = false
    }
    
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(true)
            title = getString(R.string.app_name)
        }
        
        toolbar.setNavigationOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }
    }
    
    private fun setupDrawer() {
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        
        navigationView.setNavigationItemSelectedListener(this)
        updateNavigationHeader()
    }
    
    private fun updateNavigationHeader() {
        val headerView = navigationView.getHeaderView(0)
        val userNameText = headerView.findViewById<TextView>(R.id.userNameText)
        val userEmailText = headerView.findViewById<TextView>(R.id.userEmailText)
        val userImageView = headerView.findViewById<ImageView>(R.id.userImageView)
        
        val user = preferences.getCurrentUser()
        userNameText.text = user?.name ?: getString(R.string.guest_user)
        userEmailText.text = user?.email ?: getString(R.string.sign_in_prompt)
        
        user?.profileImage?.let { imageUrl ->
            imageLoader.loadCircleImage(userImageView, imageUrl)
        }
    }
    
    private fun setupBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    loadInitialData()
                    true
                }
                R.id.navigation_categories -> {
                    showCategoriesDialog()
                    true
                }
                R.id.navigation_cart -> {
                    navigateToCart()
                    true
                }
                R.id.navigation_wishlist -> {
                    navigateToWishlist()
                    true
                }
                R.id.navigation_account -> {
                    navigateToAccount()
                    true
                }
                else -> false
            }
        }
    }
    
    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(
            context = this,
            imageLoader = imageLoader,
            currencyFormatter = currencyFormatter,
            listener = this
        )
        
        recyclerView.apply {
            adapter = productAdapter
            layoutManager = if (currentViewMode == ViewMode.GRID) {
                GridLayoutManager(this@ProductListActivity, 2)
            } else {
                LinearLayoutManager(this@ProductListActivity)
            }
            setHasFixedSize(true)
            addItemDecoration(SpacingItemDecoration(16))
            
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    
                    val layoutManager = recyclerView.layoutManager
                    val visibleItemCount = layoutManager?.childCount ?: 0
                    val totalItemCount = layoutManager?.itemCount ?: 0
                    val firstVisibleItemPosition = when (layoutManager) {
                        is LinearLayoutManager -> layoutManager.findFirstVisibleItemPosition()
                        is GridLayoutManager -> layoutManager.findFirstVisibleItemPosition()
                        else -> 0
                    }
                    
                    if (!isLoading && viewModel.hasMorePages.value) {
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0
                            && totalItemCount >= 20) {
                            viewModel.loadMoreProducts()
                        }
                    }
                    
                    scrollToTopButton.apply {
                        if (firstVisibleItemPosition > 5) {
                            if (!isShown) show()
                        } else {
                            if (isShown) hide()
                        }
                    }
                }
            })
        }
    }
    
    private fun setupSwipeRefresh() {
        swipeRefreshLayout.apply {
            setColorSchemeResources(
                R.color.primary,
                R.color.secondary,
                R.color.accent
            )
            setOnRefreshListener {
                viewModel.refreshProducts()
            }
        }
    }
    
    private fun setupSearch() {
        searchView = toolbar.menu?.findItem(R.id.action_search)?.actionView as? SearchView ?: return
        
        searchView.apply {
            queryHint = getString(R.string.search_products)
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let {
                        searchQuery = it
                        viewModel.searchProducts(it)
                        analyticsManager.logSearch(it)
                    }
                    return true
                }
                
                override fun onQueryTextChange(newText: String?): Boolean {
                    newText?.let {
                        searchQuery = it
                        viewModel.searchProducts(it)
                    }
                    return true
                }
            })
            
            setOnCloseListener {
                searchQuery = ""
                viewModel.loadProducts()
                false
            }
        }
    }
    
    private fun setupFilters() {
        setupTabCategories()
        setupFilterChips()
        setupSortChips()
        
        filterButton.setOnClickListener {
            showFilterBottomSheet()
        }
        
        viewModeToggle.setOnClickListener {
            toggleViewMode()
        }
    }
    
    private fun setupTabCategories() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val category = tab?.tag as? ProductCategory
                currentCategory = category
                viewModel.selectCategory(category)
            }
            
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
    
    private fun setupFilterChips() {
        filterChipAdapter = FilterChipAdapter { filter ->
            when (filter) {
                is FilterChip.Category -> {
                    selectedFilters = selectedFilters.copy(
                        categories = if (filter.isSelected) {
                            selectedFilters.categories + filter.id
                        } else {
                            selectedFilters.categories - filter.id
                        }
                    )
                }
                is FilterChip.Brand -> {
                    selectedFilters = selectedFilters.copy(
                        brands = if (filter.isSelected) {
                            selectedFilters.brands + filter.id
                        } else {
                            selectedFilters.brands - filter.id
                        }
                    )
                }
                is FilterChip.Price -> {
                    selectedFilters = selectedFilters.copy(
                        priceRange = if (filter.isSelected) filter.range else null
                    )
                }
                is FilterChip.Other -> {
                    when (filter.type) {
                        "in_stock" -> selectedFilters = selectedFilters.copy(inStockOnly = filter.isSelected)
                        "on_sale" -> selectedFilters = selectedFilters.copy(onSaleOnly = filter.isSelected)
                        "free_shipping" -> selectedFilters = selectedFilters.copy(freeShippingOnly = filter.isSelected)
                    }
                }
            }
            viewModel.applyFilters(selectedFilters)
        }
    }
    
    private fun setupSortChips() {
        sortChipGroup.setOnCheckedChangeListener { _, checkedId ->
            val sortOption = when (checkedId) {
                R.id.chip_popularity -> SortOption.POPULARITY
                R.id.chip_price_low -> SortOption.PRICE_LOW_TO_HIGH
                R.id.chip_price_high -> SortOption.PRICE_HIGH_TO_LOW
                R.id.chip_rating -> SortOption.RATING
                R.id.chip_newest -> SortOption.NEWEST
                else -> SortOption.POPULARITY
            }
            viewModel.setSortOption(sortOption)
        }
    }
    
    private fun setupObservers() {
        viewModel.products.observe(this) { products ->
            productAdapter.submitList(products)
            updateEmptyState(products.isEmpty())
        }
        
        viewModel.categories.observe(this) { categories ->
            updateCategoryTabs(categories)
        }
        
        viewModel.brands.observe(this) { brands ->
            updateBrandChips(brands)
        }
        
        lifecycleScope.launch {
            viewModel.isLoading.collectLatest { loading ->
                isLoading = loading
                progressBar.visibility = if (loading && productAdapter.itemCount == 0) View.VISIBLE else View.GONE
            }
        }
        
        lifecycleScope.launch {
            viewModel.isRefreshing.collectLatest { refreshing ->
                swipeRefreshLayout.isRefreshing = refreshing
            }
        }
        
        lifecycleScope.launch {
            viewModel.errorMessage.collectLatest { error ->
                error?.let {
                    showError(it)
                    viewModel.clearError()
                }
            }
        }
        
        lifecycleScope.launch {
            viewModel.successMessage.collectLatest { message ->
                message?.let {
                    showSuccess(it)
                    viewModel.clearSuccess()
                }
            }
        }
        
        lifecycleScope.launch {
            viewModel.cartCount.collectLatest { count ->
                updateCartBadge(count)
            }
        }
        
        lifecycleScope.launch {
            viewModel.wishlistCount.collectLatest { count ->
                updateWishlistBadge(count)
            }
        }
        
        lifecycleScope.launch {
            viewModel.navigationEvent.collectLatest { event ->
                handleNavigationEvent(event)
            }
        }
        
        networkMonitor.isConnected.observe(this) { isConnected ->
            updateNetworkState(isConnected)
        }
    }
    
    private fun setupListeners() {
        scrollToTopButton.setOnClickListener {
            recyclerView.smoothScrollToPosition(0)
            appBarLayout.setExpanded(true, true)
        }
        
        binding.retryButton.setOnClickListener {
            viewModel.loadProducts()
        }
        
        binding.clearFiltersButton.setOnClickListener {
            clearAllFilters()
        }
    }
    
    private fun checkPermissions() {
        if (permissionManager.shouldRequestNotificationPermission()) {
            permissionManager.requestNotificationPermission(this) { granted ->
                if (granted) {
                    notificationHelper.createNotificationChannels()
                }
            }
        }
    }
    
    private fun handleDeepLink() {
        intent?.let { intent ->
            deepLinkHandler.handleIntent(intent) { action, data ->
                when (action) {
                    DeepLinkAction.PRODUCT -> {
                        data["productId"]?.toLongOrNull()?.let { productId ->
                            navigateToProductDetail(productId)
                        }
                    }
                    DeepLinkAction.CATEGORY -> {
                        data["categoryId"]?.toLongOrNull()?.let { categoryId ->
                            viewModel.loadProductsByCategory(categoryId)
                        }
                    }
                    DeepLinkAction.SEARCH -> {
                        data["query"]?.let { query ->
                            searchView.setQuery(query, true)
                        }
                    }
                    else -> {}
                }
            }
        }
    }
    
    private fun loadInitialData() {
        viewModel.loadProducts()
    }
    
    private fun showFilterBottomSheet() {
        filterBottomSheet = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_filter, null)
        
        setupFilterViews(view)
        
        filterBottomSheet?.apply {
            setContentView(view)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            show()
        }
    }
    
    private fun setupFilterViews(view: View) {
        val priceRangeSlider = view.findViewById<RangeSlider>(R.id.priceRangeSlider)
        val minPriceText = view.findViewById<TextView>(R.id.minPriceText)
        val maxPriceText = view.findViewById<TextView>(R.id.maxPriceText)
        val categoryChipGroup = view.findViewById<ChipGroup>(R.id.categoryChipGroup)
        val brandChipGroup = view.findViewById<ChipGroup>(R.id.brandChipGroup)
        val colorChipGroup = view.findViewById<ChipGroup>(R.id.colorChipGroup)
        val sizeChipGroup = view.findViewById<ChipGroup>(R.id.sizeChipGroup)
        val ratingBar = view.findViewById<RatingBar>(R.id.minRatingBar)
        val inStockSwitch = view.findViewById<Switch>(R.id.inStockSwitch)
        val onSaleSwitch = view.findViewById<Switch>(R.id.onSaleSwitch)
        val freeShippingSwitch = view.findViewById<Switch>(R.id.freeShippingSwitch)
        val applyButton = view.findViewById<MaterialButton>(R.id.applyFilterButton)
        val resetButton = view.findViewById<MaterialButton>(R.id.resetFilterButton)
        
        priceRangeSlider.apply {
            valueFrom = 0f
            valueTo = 10000f
            values = listOf(
                selectedFilters.priceRange?.min?.toFloat() ?: 0f,
                selectedFilters.priceRange?.max?.toFloat() ?: 10000f
            )
            
            addOnChangeListener { slider, _, _ ->
                val values = slider.values
                minPriceText.text = currencyFormatter.format(values[0])
                maxPriceText.text = currencyFormatter.format(values[1])
            }
        }
        
        viewModel.categories.value?.forEach { category ->
            val chip = Chip(this).apply {
                text = category.name
                isCheckable = true
                isChecked = selectedFilters.categories.contains(category.id)
                tag = category.id
            }
            categoryChipGroup.addView(chip)
        }
        
        viewModel.brands.value?.forEach { brand ->
            val chip = Chip(this).apply {
                text = brand.name
                isCheckable = true
                isChecked = selectedFilters.brands.contains(brand.id)
                tag = brand.id
            }
            brandChipGroup.addView(chip)
        }
        
        val colors = listOf("Red", "Blue", "Green", "Black", "White", "Yellow", "Pink", "Purple")
        colors.forEach { color ->
            val chip = Chip(this).apply {
                text = color
                isCheckable = true
                isChecked = selectedFilters.colors.contains(color)
                tag = color
            }
            colorChipGroup.addView(chip)
        }
        
        val sizes = listOf("XS", "S", "M", "L", "XL", "XXL", "XXXL")
        sizes.forEach { size ->
            val chip = Chip(this).apply {
                text = size
                isCheckable = true
                isChecked = selectedFilters.sizes.contains(size)
                tag = size
            }
            sizeChipGroup.addView(chip)
        }
        
        ratingBar.rating = selectedFilters.minRating ?: 0f
        inStockSwitch.isChecked = selectedFilters.inStockOnly
        onSaleSwitch.isChecked = selectedFilters.onSaleOnly
        freeShippingSwitch.isChecked = selectedFilters.freeShippingOnly
        
        applyButton.setOnClickListener {
            val values = priceRangeSlider.values
            selectedFilters = SelectedFilters(
                categories = categoryChipGroup.checkedChipIds.map { 
                    categoryChipGroup.findViewById<Chip>(it).tag as Long 
                },
                brands = brandChipGroup.checkedChipIds.map { 
                    brandChipGroup.findViewById<Chip>(it).tag as Long 
                },
                priceRange = PriceRange(
                    BigDecimal(values[0].toDouble()),
                    BigDecimal(values[1].toDouble())
                ),
                colors = colorChipGroup.checkedChipIds.map { 
                    colorChipGroup.findViewById<Chip>(it).tag as String 
                },
                sizes = sizeChipGroup.checkedChipIds.map { 
                    sizeChipGroup.findViewById<Chip>(it).tag as String 
                },
                minRating = if (ratingBar.rating > 0) ratingBar.rating else null,
                inStockOnly = inStockSwitch.isChecked,
                onSaleOnly = onSaleSwitch.isChecked,
                freeShippingOnly = freeShippingSwitch.isChecked
            )
            
            viewModel.applyFilters(selectedFilters)
            filterBottomSheet?.dismiss()
            updateFilterIndicator()
        }
        
        resetButton.setOnClickListener {
            clearAllFilters()
            filterBottomSheet?.dismiss()
        }
    }
    
    private fun showCategoriesDialog() {
        val dialog = CategoriesBottomSheetFragment()
        dialog.show(supportFragmentManager, "categories")
    }
    
    private fun toggleViewMode() {
        currentViewMode = if (currentViewMode == ViewMode.GRID) {
            ViewMode.LIST
        } else {
            ViewMode.GRID
        }
        
        viewModel.setViewMode(currentViewMode)
        updateRecyclerViewLayout()
        
        viewModeToggle.setImageResource(
            if (currentViewMode == ViewMode.GRID) {
                R.drawable.ic_view_list
            } else {
                R.drawable.ic_view_grid
            }
        )
    }
    
    private fun updateRecyclerViewLayout() {
        recyclerView.layoutManager = if (currentViewMode == ViewMode.GRID) {
            GridLayoutManager(this, 2)
        } else {
            LinearLayoutManager(this)
        }
        productAdapter.setViewMode(currentViewMode)
    }
    
    private fun updateCategoryTabs(categories: List<ProductCategory>) {
        tabLayout.removeAllTabs()
        
        val allTab = tabLayout.newTab().apply {
            text = getString(R.string.all_products)
            tag = null
        }
        tabLayout.addTab(allTab)
        
        categories.filter { it.parentId == null }.forEach { category ->
            val tab = tabLayout.newTab().apply {
                text = category.name
                tag = category
            }
            tabLayout.addTab(tab)
        }
    }
    
    private fun updateBrandChips(brands: List<Brand>) {
        
    }
    
    private fun updateEmptyState(isEmpty: Boolean) {
        emptyView.visibility = if (isEmpty && !isLoading) View.VISIBLE else View.GONE
        recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
    
    private fun updateNetworkState(isConnected: Boolean) {
        if (!isConnected) {
            showError(getString(R.string.no_internet_connection))
        }
    }
    
    private fun updateCartBadge(count: Int) {
        cartBadge.apply {
            isVisible = count > 0
            number = count
        }
    }
    
    private fun updateWishlistBadge(count: Int) {
        wishlistBadge.apply {
            isVisible = count > 0
            number = count
        }
    }
    
    private fun updateFilterIndicator() {
        val hasFilters = selectedFilters != SelectedFilters()
        binding.activeFiltersIndicator.visibility = if (hasFilters) View.VISIBLE else View.GONE
        
        if (hasFilters) {
            val filterCount = countActiveFilters()
            binding.activeFiltersText.text = getString(R.string.active_filters_count, filterCount)
        }
    }
    
    private fun countActiveFilters(): Int {
        var count = 0
        if (selectedFilters.categories.isNotEmpty()) count += selectedFilters.categories.size
        if (selectedFilters.brands.isNotEmpty()) count += selectedFilters.brands.size
        if (selectedFilters.priceRange != null) count++
        if (selectedFilters.colors.isNotEmpty()) count += selectedFilters.colors.size
        if (selectedFilters.sizes.isNotEmpty()) count += selectedFilters.sizes.size
        if (selectedFilters.minRating != null) count++
        if (selectedFilters.inStockOnly) count++
        if (selectedFilters.onSaleOnly) count++
        if (selectedFilters.freeShippingOnly) count++
        return count
    }
    
    private fun clearAllFilters() {
        selectedFilters = SelectedFilters()
        viewModel.clearFilters()
        updateFilterIndicator()
    }
    
    private fun showError(message: String) {
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG)
            .setAction(getString(R.string.retry)) {
                viewModel.loadProducts()
            }
            .show()
    }
    
    private fun showSuccess(message: String) {
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_SHORT).show()
    }
    
    private fun handleNavigationEvent(event: NavigationEvent) {
        when (event) {
            is NavigationEvent.GoToProduct -> navigateToProductDetail(event.productId)
            is NavigationEvent.GoToCategory -> viewModel.loadProductsByCategory(event.categoryId)
            is NavigationEvent.GoToBrand -> viewModel.loadProductsByBrand(event.brandId)
            is NavigationEvent.GoToCart -> navigateToCart()
            is NavigationEvent.GoToWishlist -> navigateToWishlist()
            is NavigationEvent.GoToCompare -> navigateToCompare()
            is NavigationEvent.GoToCheckout -> navigateToCheckout()
            is NavigationEvent.OrderComplete -> showOrderComplete(event.orderId)
            is NavigationEvent.ShareProduct -> shareProduct(event.product)
        }
    }
    
    override fun onProductClick(product: Product) {
        navigateToProductDetail(product.id)
    }
    
    override fun onAddToCartClick(product: Product) {
        viewModel.addToCart(product.id)
    }
    
    override fun onAddToWishlistClick(product: Product) {
        viewModel.addToWishlist(product.id)
    }
    
    override fun onShareClick(product: Product) {
        shareProduct(product)
    }
    
    override fun onCategoryClick(category: ProductCategory) {
        currentCategory = category
        viewModel.selectCategory(category)
    }
    
    override fun onBrandClick(brand: Brand) {
        currentBrand = brand
        viewModel.selectBrand(brand)
    }
    
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> loadInitialData()
            R.id.nav_categories -> showCategoriesDialog()
            R.id.nav_brands -> showBrandsDialog()
            R.id.nav_deals -> loadDeals()
            R.id.nav_new_arrivals -> loadNewArrivals()
            R.id.nav_best_sellers -> loadBestSellers()
            R.id.nav_orders -> navigateToOrders()
            R.id.nav_wishlist -> navigateToWishlist()
            R.id.nav_recently_viewed -> navigateToRecentlyViewed()
            R.id.nav_compare -> navigateToCompare()
            R.id.nav_notifications -> navigateToNotifications()
            R.id.nav_help -> navigateToHelp()
            R.id.nav_settings -> navigateToSettings()
            R.id.nav_about -> navigateToAbout()
            R.id.nav_logout -> performLogout()
        }
        
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
    
    private fun navigateToProductDetail(productId: Long) {
        val intent = Intent(this, ProductDetailActivity::class.java).apply {
            putExtra("productId", productId)
        }
        startActivity(intent)
    }
    
    private fun navigateToCart() {
        val intent = Intent(this, CartActivity::class.java)
        startActivity(intent)
    }
    
    private fun navigateToWishlist() {
        val intent = Intent(this, WishlistActivity::class.java)
        startActivity(intent)
    }
    
    private fun navigateToCompare() {
        val intent = Intent(this, CompareActivity::class.java)
        startActivity(intent)
    }
    
    private fun navigateToCheckout() {
        val intent = Intent(this, CheckoutActivity::class.java)
        startActivity(intent)
    }
    
    private fun navigateToAccount() {
        val intent = Intent(this, AccountActivity::class.java)
        startActivity(intent)
    }
    
    private fun navigateToOrders() {
        val intent = Intent(this, OrdersActivity::class.java)
        startActivity(intent)
    }
    
    private fun navigateToRecentlyViewed() {
        val intent = Intent(this, RecentlyViewedActivity::class.java)
        startActivity(intent)
    }
    
    private fun navigateToNotifications() {
        val intent = Intent(this, NotificationsActivity::class.java)
        startActivity(intent)
    }
    
    private fun navigateToHelp() {
        val intent = Intent(this, HelpActivity::class.java)
        startActivity(intent)
    }
    
    private fun navigateToSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }
    
    private fun navigateToAbout() {
        val intent = Intent(this, AboutActivity::class.java)
        startActivity(intent)
    }
    
    private fun showBrandsDialog() {
        val dialog = BrandsBottomSheetFragment()
        dialog.show(supportFragmentManager, "brands")
    }
    
    private fun loadDeals() {
        viewModel.setSortOption(SortOption.DISCOUNT)
        selectedFilters = selectedFilters.copy(onSaleOnly = true)
        viewModel.applyFilters(selectedFilters)
    }
    
    private fun loadNewArrivals() {
        viewModel.setSortOption(SortOption.NEWEST)
        viewModel.loadProducts()
    }
    
    private fun loadBestSellers() {
        viewModel.setSortOption(SortOption.POPULARITY)
        viewModel.loadProducts()
    }
    
    private fun shareProduct(product: Product) {
        shareHelper.shareProduct(this, product)
        analyticsManager.logShare(product)
    }
    
    private fun showOrderComplete(orderId: String) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.order_complete))
            .setMessage(getString(R.string.order_complete_message, orderId))
            .setPositiveButton(getString(R.string.view_order)) { _, _ ->
                navigateToOrderDetail(orderId)
            }
            .setNegativeButton(getString(R.string.continue_shopping), null)
            .show()
    }
    
    private fun navigateToOrderDetail(orderId: String) {
        val intent = Intent(this, OrderDetailActivity::class.java).apply {
            putExtra("orderId", orderId)
        }
        startActivity(intent)
    }
    
    private fun performLogout() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.logout))
            .setMessage(getString(R.string.logout_confirmation))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                preferences.clearUser()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_product_list, menu)
        
        val searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem.actionView as SearchView
        setupSearch()
        
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> true
            R.id.action_filter -> {
                showFilterBottomSheet()
                true
            }
            R.id.action_sort -> {
                showSortBottomSheet()
                true
            }
            R.id.action_scan -> {
                startBarcodeScanner()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun showSortBottomSheet() {
        val sortOptions = arrayOf(
            getString(R.string.sort_popularity),
            getString(R.string.sort_price_low),
            getString(R.string.sort_price_high),
            getString(R.string.sort_rating),
            getString(R.string.sort_newest),
            getString(R.string.sort_name_az),
            getString(R.string.sort_name_za),
            getString(R.string.sort_discount)
        )
        
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.sort_by))
            .setSingleChoiceItems(sortOptions, viewModel.sortOption.value?.ordinal ?: 0) { dialog, which ->
                val sortOption = SortOption.values()[which]
                viewModel.setSortOption(sortOption)
                dialog.dismiss()
            }
            .show()
    }
    
    private fun startBarcodeScanner() {
        if (permissionManager.hasCameraPermission()) {
            val intent = Intent(this, BarcodeScannerActivity::class.java)
            startActivityForResult(intent, REQUEST_BARCODE_SCAN)
        } else {
            permissionManager.requestCameraPermission(this) { granted ->
                if (granted) {
                    startBarcodeScanner()
                } else {
                    showError(getString(R.string.camera_permission_required))
                }
            }
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == REQUEST_BARCODE_SCAN && resultCode == RESULT_OK) {
            data?.getStringExtra("barcode")?.let { barcode ->
                searchView.setQuery(barcode, true)
            }
        }
    }
    
    override fun onBackPressed() {
        when {
            drawerLayout.isDrawerOpen(GravityCompat.START) -> {
                drawerLayout.closeDrawer(GravityCompat.START)
            }
            searchView.isIconified -> {
                searchView.onActionViewCollapsed()
            }
            else -> {
                super.onBackPressed()
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        analyticsManager.logScreenView("ProductList", this::class.java.simpleName)
        updateNavigationHeader()
    }
    
    override fun onPause() {
        super.onPause()
        preferences.saveLastViewedScreen("ProductList")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        filterBottomSheet?.dismiss()
        sortBottomSheet?.dismiss()
    }
    
    companion object {
        private const val REQUEST_BARCODE_SCAN = 1001
    }
}