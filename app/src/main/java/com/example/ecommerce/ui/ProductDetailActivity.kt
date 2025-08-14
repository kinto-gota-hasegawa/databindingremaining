package com.example.ecommerce.ui

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.ecommerce.R
import com.example.ecommerce.adapter.*
import com.example.ecommerce.databinding.ActivityProductDetailBinding
import com.example.ecommerce.models.*
import com.example.ecommerce.repository.AvailabilityStatus
import com.example.ecommerce.ui.dialogs.*
import com.example.ecommerce.ui.fragments.*
import com.example.ecommerce.utils.*
import com.example.ecommerce.viewmodel.*
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.abs

@AndroidEntryPoint
class ProductDetailActivity : AppCompatActivity() {

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
    @Inject lateinit var animationHelper: AnimationHelper
    @Inject lateinit var hapticFeedback: HapticFeedback
    @Inject lateinit var soundEffects: SoundEffects
    
    private lateinit var binding: ActivityProductDetailBinding
    private val viewModel: ProductViewModel by viewModels()
    
    private lateinit var imageViewPager: ViewPager2
    private lateinit var imageIndicator: TabLayout
    private lateinit var productNameText: TextView
    private lateinit var brandNameText: TextView
    private lateinit var ratingBar: RatingBar
    private lateinit var ratingText: TextView
    private lateinit var reviewCountText: TextView
    private lateinit var priceText: TextView
    private lateinit var originalPriceText: TextView
    private lateinit var discountBadge: TextView
    private lateinit var descriptionText: TextView
    private lateinit var featuresRecyclerView: RecyclerView
    private lateinit var specificationsRecyclerView: RecyclerView
    private lateinit var variantChipGroup: ChipGroup
    private lateinit var colorChipGroup: ChipGroup
    private lateinit var sizeChipGroup: ChipGroup
    private lateinit var quantityText: TextView
    private lateinit var quantityMinusButton: ImageButton
    private lateinit var quantityPlusButton: ImageButton
    private lateinit var addToCartButton: MaterialButton
    private lateinit var buyNowButton: MaterialButton
    private lateinit var wishlistButton: FloatingActionButton
    private lateinit var shareButton: FloatingActionButton
    private lateinit var compareButton: FloatingActionButton
    private lateinit var availabilityText: TextView
    private lateinit var stockCountText: TextView
    private lateinit var shippingInfoText: TextView
    private lateinit var returnPolicyText: TextView
    private lateinit var reviewsRecyclerView: RecyclerView
    private lateinit var relatedProductsRecyclerView: RecyclerView
    private lateinit var frequentlyBoughtRecyclerView: RecyclerView
    private lateinit var questionsRecyclerView: RecyclerView
    private lateinit var progressBar: CircularProgressIndicator
    private lateinit var errorView: View
    private lateinit var contentScrollView: ScrollView
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var collapsingToolbarLayout: CollapsingToolbarLayout
    private lateinit var toolbar: Toolbar
    private lateinit var bottomActionBar: View
    private lateinit var stickyAddToCartButton: MaterialButton
    private lateinit var expandedImageView: TouchImageView
    private lateinit var imageOverlay: View
    private lateinit var zoomIndicator: View
    
    private lateinit var imageAdapter: ProductImageAdapter
    private lateinit var featureAdapter: ProductFeatureAdapter
    private lateinit var specificationAdapter: SpecificationAdapter
    private lateinit var reviewAdapter: ReviewAdapter
    private lateinit var relatedProductAdapter: ProductAdapter
    private lateinit var frequentlyBoughtAdapter: ProductAdapter
    private lateinit var questionAdapter: ProductQuestionAdapter
    
    private var currentProduct: Product? = null
    private var selectedVariant: ProductVariant? = null
    private var selectedQuantity = 1
    private var isInWishlist = false
    private var isInCompare = false
    private var productId: Long = 0
    
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
    private val percentFormatter = NumberFormat.getPercentInstance(Locale.getDefault())
    
    private var imageTransitionInProgress = false
    private var lastScrollY = 0
    private var isBottomBarVisible = true
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        performanceMonitor.startTrace("product_detail_onCreate")
        
        themeManager.applyTheme(this)
        localizationManager.applyLocale(this)
        
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        productId = intent.getLongExtra("productId", 0)
        if (productId == 0L) {
            handleDeepLink()
        }
        
        if (productId == 0L) {
            showError("Invalid product")
            finish()
            return
        }
        
        initializeViews()
        setupToolbar()
        setupAdapters()
        setupListeners()
        setupObservers()
        loadProductDetails()
        
        performanceMonitor.stopTrace("product_detail_onCreate")
        
        analyticsManager.logScreenView("ProductDetail", this::class.java.simpleName)
    }
    
    private fun initializeViews() {
        imageViewPager = binding.imageViewPager
        imageIndicator = binding.imageIndicator
        productNameText = binding.productNameText
        brandNameText = binding.brandNameText
        ratingBar = binding.ratingBar
        ratingText = binding.ratingText
        reviewCountText = binding.reviewCountText
        priceText = binding.priceText
        originalPriceText = binding.originalPriceText
        discountBadge = binding.discountBadge
        descriptionText = binding.descriptionText
        featuresRecyclerView = binding.featuresRecyclerView
        specificationsRecyclerView = binding.specificationsRecyclerView
        variantChipGroup = binding.variantChipGroup
        colorChipGroup = binding.colorChipGroup
        sizeChipGroup = binding.sizeChipGroup
        quantityText = binding.quantityText
        quantityMinusButton = binding.quantityMinusButton
        quantityPlusButton = binding.quantityPlusButton
        addToCartButton = binding.addToCartButton
        buyNowButton = binding.buyNowButton
        wishlistButton = binding.wishlistButton
        shareButton = binding.shareButton
        compareButton = binding.compareButton
        availabilityText = binding.availabilityText
        stockCountText = binding.stockCountText
        shippingInfoText = binding.shippingInfoText
        returnPolicyText = binding.returnPolicyText
        reviewsRecyclerView = binding.reviewsRecyclerView
        relatedProductsRecyclerView = binding.relatedProductsRecyclerView
        frequentlyBoughtRecyclerView = binding.frequentlyBoughtRecyclerView
        questionsRecyclerView = binding.questionsRecyclerView
        progressBar = binding.progressBar
        errorView = binding.errorView
        contentScrollView = binding.contentScrollView
        appBarLayout = binding.appBarLayout
        collapsingToolbarLayout = binding.collapsingToolbarLayout
        toolbar = binding.toolbar
        bottomActionBar = binding.bottomActionBar
        stickyAddToCartButton = binding.stickyAddToCartButton
        expandedImageView = binding.expandedImageView
        imageOverlay = binding.imageOverlay
        zoomIndicator = binding.zoomIndicator
        
        setupWindowInsets()
    }
    
    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, 0)
            toolbar.setPadding(0, systemBars.top, 0, 0)
            bottomActionBar.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }
    }
    
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
        }
        
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        
        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBar, verticalOffset ->
            val totalScrollRange = appBar.totalScrollRange
            val percentage = abs(verticalOffset).toFloat() / totalScrollRange.toFloat()
            
            collapsingToolbarLayout.title = if (percentage > 0.7f) {
                currentProduct?.name ?: ""
            } else {
                ""
            }
            
            val alpha = 1f - percentage
            imageViewPager.alpha = alpha
            imageIndicator.alpha = alpha
        })
    }
    
    private fun setupAdapters() {
        imageAdapter = ProductImageAdapter(imageLoader) { position ->
            showFullScreenImage(position)
        }
        imageViewPager.adapter = imageAdapter
        
        TabLayoutMediator(imageIndicator, imageViewPager) { _, _ -> }.attach()
        
        featureAdapter = ProductFeatureAdapter()
        featuresRecyclerView.apply {
            adapter = featureAdapter
            layoutManager = LinearLayoutManager(this@ProductDetailActivity)
            setHasFixedSize(true)
        }
        
        specificationAdapter = SpecificationAdapter()
        specificationsRecyclerView.apply {
            adapter = specificationAdapter
            layoutManager = LinearLayoutManager(this@ProductDetailActivity)
            setHasFixedSize(true)
        }
        
        reviewAdapter = ReviewAdapter(imageLoader) { review ->
            showReviewDetail(review)
        }
        reviewsRecyclerView.apply {
            adapter = reviewAdapter
            layoutManager = LinearLayoutManager(this@ProductDetailActivity)
            setHasFixedSize(false)
            isNestedScrollingEnabled = false
        }
        
        relatedProductAdapter = ProductAdapter(
            context = this,
            imageLoader = imageLoader,
            currencyFormatter = currencyFormatter,
            listener = object : ProductAdapter.OnProductClickListener {
                override fun onProductClick(product: Product) {
                    navigateToProduct(product.id)
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
            }
        )
        
        relatedProductsRecyclerView.apply {
            adapter = relatedProductAdapter
            layoutManager = LinearLayoutManager(this@ProductDetailActivity, LinearLayoutManager.HORIZONTAL, false)
            setHasFixedSize(true)
            val snapHelper = PagerSnapHelper()
            snapHelper.attachToRecyclerView(this)
        }
        
        frequentlyBoughtAdapter = ProductAdapter(
            context = this,
            imageLoader = imageLoader,
            currencyFormatter = currencyFormatter,
            listener = object : ProductAdapter.OnProductClickListener {
                override fun onProductClick(product: Product) {
                    navigateToProduct(product.id)
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
            }
        )
        
        frequentlyBoughtRecyclerView.apply {
            adapter = frequentlyBoughtAdapter
            layoutManager = LinearLayoutManager(this@ProductDetailActivity, LinearLayoutManager.HORIZONTAL, false)
            setHasFixedSize(true)
        }
        
        questionAdapter = ProductQuestionAdapter { question ->
            showQuestionDetail(question)
        }
        questionsRecyclerView.apply {
            adapter = questionAdapter
            layoutManager = LinearLayoutManager(this@ProductDetailActivity)
            setHasFixedSize(false)
            isNestedScrollingEnabled = false
        }
    }
    
    private fun setupListeners() {
        quantityMinusButton.setOnClickListener {
            hapticFeedback.tick()
            updateQuantity(selectedQuantity - 1)
        }
        
        quantityPlusButton.setOnClickListener {
            hapticFeedback.tick()
            updateQuantity(selectedQuantity + 1)
        }
        
        addToCartButton.setOnClickListener {
            hapticFeedback.impact()
            soundEffects.playAddToCart()
            addToCart()
        }
        
        stickyAddToCartButton.setOnClickListener {
            hapticFeedback.impact()
            soundEffects.playAddToCart()
            addToCart()
        }
        
        buyNowButton.setOnClickListener {
            hapticFeedback.impact()
            buyNow()
        }
        
        wishlistButton.setOnClickListener {
            hapticFeedback.impact()
            toggleWishlist()
        }
        
        shareButton.setOnClickListener {
            hapticFeedback.tick()
            shareProduct()
        }
        
        compareButton.setOnClickListener {
            hapticFeedback.tick()
            toggleCompare()
        }
        
        binding.retryButton.setOnClickListener {
            loadProductDetails()
        }
        
        binding.writeReviewButton.setOnClickListener {
            showWriteReviewDialog()
        }
        
        binding.askQuestionButton.setOnClickListener {
            showAskQuestionDialog()
        }
        
        binding.viewAllReviewsButton.setOnClickListener {
            navigateToAllReviews()
        }
        
        binding.viewAllQuestionsButton.setOnClickListener {
            navigateToAllQuestions()
        }
        
        binding.sizeGuideButton.setOnClickListener {
            showSizeGuide()
        }
        
        binding.deliveryInfoButton.setOnClickListener {
            showDeliveryInfo()
        }
        
        binding.returnPolicyButton.setOnClickListener {
            showReturnPolicy()
        }
        
        contentScrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            handleScroll(scrollY)
        }
        
        imageOverlay.setOnClickListener {
            hideFullScreenImage()
        }
        
        setupVariantListeners()
        setupColorListeners()
        setupSizeListeners()
    }
    
    private fun setupVariantListeners() {
        variantChipGroup.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId != View.NO_ID) {
                val chip = group.findViewById<Chip>(checkedId)
                val variant = chip?.tag as? ProductVariant
                variant?.let {
                    selectVariant(it)
                }
            }
        }
    }
    
    private fun setupColorListeners() {
        colorChipGroup.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId != View.NO_ID) {
                val chip = group.findViewById<Chip>(checkedId)
                val color = chip?.text?.toString()
                currentProduct?.variants?.find { it.attributes["color"] == color }?.let {
                    selectVariant(it)
                }
            }
        }
    }
    
    private fun setupSizeListeners() {
        sizeChipGroup.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId != View.NO_ID) {
                val chip = group.findViewById<Chip>(checkedId)
                val size = chip?.text?.toString()
                currentProduct?.variants?.find { it.attributes["size"] == size }?.let {
                    selectVariant(it)
                }
            }
        }
    }
    
    private fun setupObservers() {
        viewModel.selectedProduct.observe(this) { product ->
            product?.let {
                currentProduct = it
                updateUI(it)
            }
        }
        
        viewModel.relatedProducts.observe(this) { products ->
            relatedProductAdapter.submitList(products)
            binding.relatedProductsSection.visibility = if (products.isEmpty()) View.GONE else View.VISIBLE
        }
        
        lifecycleScope.launch {
            viewModel.productReviews.collectLatest { reviews ->
                reviewAdapter.submitList(reviews.take(3))
                binding.reviewsSection.visibility = if (reviews.isEmpty()) View.GONE else View.VISIBLE
                updateReviewsSummary(reviews)
            }
        }
        
        lifecycleScope.launch {
            viewModel.productQuestions.collectLatest { questions ->
                questionAdapter.submitList(questions.take(3))
                binding.questionsSection.visibility = if (questions.isEmpty()) View.GONE else View.VISIBLE
            }
        }
        
        lifecycleScope.launch {
            viewModel.productVariants.collectLatest { variants ->
                updateVariants(variants)
            }
        }
        
        lifecycleScope.launch {
            viewModel.selectedVariant.collectLatest { variant ->
                selectedVariant = variant
                variant?.let {
                    updateVariantSelection(it)
                }
            }
        }
        
        lifecycleScope.launch {
            viewModel.productAvailability.collectLatest { status ->
                updateAvailability(status)
            }
        }
        
        lifecycleScope.launch {
            viewModel.isLoading.collectLatest { loading ->
                progressBar.visibility = if (loading) View.VISIBLE else View.GONE
                contentScrollView.visibility = if (loading) View.GONE else View.VISIBLE
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
            viewModel.navigationEvent.collectLatest { event ->
                handleNavigationEvent(event)
            }
        }
    }
    
    private fun loadProductDetails() {
        viewModel.loadProductDetails(productId)
    }
    
    private fun updateUI(product: Product) {
        productNameText.text = product.name
        brandNameText.text = product.brand.name
        ratingBar.rating = product.rating
        ratingText.text = String.format("%.1f", product.rating)
        reviewCountText.text = getString(R.string.review_count, product.reviewCount)
        
        updatePrice(product)
        updateDescription(product)
        updateFeatures(product)
        updateSpecifications(product)
        updateImages(product)
        updateShippingInfo(product)
        updateReturnPolicy(product)
        updateStock(product)
        updateBadges(product)
        
        brandNameText.setOnClickListener {
            navigateToBrand(product.brand.id)
        }
        
        invalidateOptionsMenu()
    }
    
    private fun updatePrice(product: Product) {
        val currentPrice = selectedVariant?.price ?: product.discountPrice ?: product.price
        val originalPrice = selectedVariant?.price ?: product.price
        
        priceText.text = currencyFormatter.format(currentPrice)
        
        if (product.discountPrice != null && product.discountPrice < product.price) {
            originalPriceText.apply {
                text = currencyFormatter.format(originalPrice)
                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                visibility = View.VISIBLE
            }
            
            val discountPercent = ((product.price.toDouble() - product.discountPrice.toDouble()) / product.price.toDouble() * 100).toInt()
            discountBadge.apply {
                text = getString(R.string.discount_percent, discountPercent)
                visibility = View.VISIBLE
            }
        } else {
            originalPriceText.visibility = View.GONE
            discountBadge.visibility = View.GONE
        }
    }
    
    private fun updateDescription(product: Product) {
        descriptionText.text = product.description
        
        binding.expandDescriptionButton.setOnClickListener {
            if (descriptionText.maxLines == 3) {
                descriptionText.maxLines = Integer.MAX_VALUE
                binding.expandDescriptionButton.text = getString(R.string.show_less)
            } else {
                descriptionText.maxLines = 3
                binding.expandDescriptionButton.text = getString(R.string.show_more)
            }
        }
    }
    
    private fun updateFeatures(product: Product) {
        featureAdapter.submitList(product.features)
        binding.featuresSection.visibility = if (product.features.isEmpty()) View.GONE else View.VISIBLE
    }
    
    private fun updateSpecifications(product: Product) {
        val specs = product.specifications.map { (key, value) ->
            Specification(key, value)
        }
        specificationAdapter.submitList(specs)
        binding.specificationsSection.visibility = if (specs.isEmpty()) View.GONE else View.VISIBLE
    }
    
    private fun updateImages(product: Product) {
        val images = listOf(product.thumbnailUrl) + product.imageUrls
        imageAdapter.submitList(images)
        
        imageViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                analyticsManager.logEvent("product_image_viewed", mapOf(
                    "product_id" to product.id,
                    "image_position" to position
                ))
            }
        })
    }
    
    private fun updateShippingInfo(product: Product) {
        val shippingInfo = buildString {
            if (product.shippingInfo.freeShipping) {
                append(getString(R.string.free_shipping))
            } else {
                append(getString(R.string.shipping_cost, currencyFormatter.format(5.99)))
            }
            append("\n")
            append(getString(R.string.estimated_delivery, 
                product.shippingInfo.estimatedDays.first,
                product.shippingInfo.estimatedDays.last))
        }
        shippingInfoText.text = shippingInfo
    }
    
    private fun updateReturnPolicy(product: Product) {
        val returnPolicy = if (product.returnPolicy.returnable) {
            getString(R.string.return_window, product.returnPolicy.returnWindow)
        } else {
            getString(R.string.no_returns)
        }
        returnPolicyText.text = returnPolicy
    }
    
    private fun updateStock(product: Product) {
        val stock = selectedVariant?.stock ?: product.stock
        
        when {
            stock == 0 -> {
                stockCountText.text = getString(R.string.out_of_stock)
                stockCountText.setTextColor(ContextCompat.getColor(this, R.color.error))
                addToCartButton.isEnabled = false
                buyNowButton.isEnabled = false
            }
            stock < 10 -> {
                stockCountText.text = getString(R.string.low_stock, stock)
                stockCountText.setTextColor(ContextCompat.getColor(this, R.color.warning))
                addToCartButton.isEnabled = true
                buyNowButton.isEnabled = true
            }
            else -> {
                stockCountText.text = getString(R.string.in_stock)
                stockCountText.setTextColor(ContextCompat.getColor(this, R.color.success))
                addToCartButton.isEnabled = true
                buyNowButton.isEnabled = true
            }
        }
    }
    
    private fun updateBadges(product: Product) {
        binding.badgeContainer.removeAllViews()
        
        if (product.isFeatured) {
            addBadge(getString(R.string.featured), R.color.primary)
        }
        if (product.isNewArrival) {
            addBadge(getString(R.string.new_arrival), R.color.accent)
        }
        if (product.isBestSeller) {
            addBadge(getString(R.string.best_seller), R.color.secondary)
        }
        if (product.isOnSale) {
            addBadge(getString(R.string.on_sale), R.color.error)
        }
    }
    
    private fun addBadge(text: String, colorRes: Int) {
        val badge = layoutInflater.inflate(R.layout.item_badge, binding.badgeContainer, false) as TextView
        badge.text = text
        badge.setBackgroundColor(ContextCompat.getColor(this, colorRes))
        binding.badgeContainer.addView(badge)
    }
    
    private fun updateVariants(variants: List<ProductVariant>) {
        variantChipGroup.removeAllViews()
        colorChipGroup.removeAllViews()
        sizeChipGroup.removeAllViews()
        
        val colors = variants.mapNotNull { it.attributes["color"] }.distinct()
        val sizes = variants.mapNotNull { it.attributes["size"] }.distinct()
        
        colors.forEach { color ->
            val chip = Chip(this).apply {
                text = color
                isCheckable = true
                tag = color
            }
            colorChipGroup.addView(chip)
        }
        
        sizes.forEach { size ->
            val chip = Chip(this).apply {
                text = size
                isCheckable = true
                tag = size
            }
            sizeChipGroup.addView(chip)
        }
        
        binding.variantsSection.visibility = if (variants.isEmpty()) View.GONE else View.VISIBLE
        binding.colorSection.visibility = if (colors.isEmpty()) View.GONE else View.VISIBLE
        binding.sizeSection.visibility = if (sizes.isEmpty()) View.GONE else View.VISIBLE
    }
    
    private fun updateVariantSelection(variant: ProductVariant) {
        currentProduct?.let { product ->
            val variantPrice = variant.price
            if (variantPrice != product.price) {
                priceText.text = currencyFormatter.format(variantPrice)
            }
            
            if (variant.stock != product.stock) {
                updateStock(product.copy(stock = variant.stock))
            }
            
            variant.imageUrl?.let { imageUrl ->
                imageViewPager.setCurrentItem(0, true)
            }
        }
    }
    
    private fun updateAvailability(status: AvailabilityStatus?) {
        status?.let {
            val (text, color) = when (it) {
                AvailabilityStatus.IN_STOCK -> getString(R.string.in_stock) to R.color.success
                AvailabilityStatus.LIMITED_STOCK -> getString(R.string.limited_stock) to R.color.warning
                AvailabilityStatus.OUT_OF_STOCK -> getString(R.string.out_of_stock) to R.color.error
                AvailabilityStatus.BACK_ORDER -> getString(R.string.back_order) to R.color.info
                AvailabilityStatus.PRE_ORDER -> getString(R.string.pre_order) to R.color.info
                AvailabilityStatus.DISCONTINUED -> getString(R.string.discontinued) to R.color.disabled
            }
            
            availabilityText.text = text
            availabilityText.setTextColor(ContextCompat.getColor(this, color))
        }
    }
    
    private fun updateReviewsSummary(reviews: List<Review>) {
        if (reviews.isEmpty()) return
        
        val averageRating = reviews.map { it.rating }.average()
        val ratingCounts = reviews.groupBy { it.rating }.mapValues { it.value.size }
        
        binding.averageRatingText.text = String.format("%.1f", averageRating)
        binding.averageRatingBar.rating = averageRating.toFloat()
        binding.totalReviewsText.text = getString(R.string.total_reviews, reviews.size)
        
        for (i in 5 downTo 1) {
            val count = ratingCounts[i] ?: 0
            val percentage = if (reviews.isNotEmpty()) count * 100 / reviews.size else 0
            
            when (i) {
                5 -> {
                    binding.rating5Bar.progress = percentage
                    binding.rating5Count.text = count.toString()
                }
                4 -> {
                    binding.rating4Bar.progress = percentage
                    binding.rating4Count.text = count.toString()
                }
                3 -> {
                    binding.rating3Bar.progress = percentage
                    binding.rating3Count.text = count.toString()
                }
                2 -> {
                    binding.rating2Bar.progress = percentage
                    binding.rating2Count.text = count.toString()
                }
                1 -> {
                    binding.rating1Bar.progress = percentage
                    binding.rating1Count.text = count.toString()
                }
            }
        }
    }
    
    private fun selectVariant(variant: ProductVariant) {
        viewModel.selectProductVariant(variant)
        hapticFeedback.selection()
    }
    
    private fun updateQuantity(quantity: Int) {
        val maxQuantity = currentProduct?.maxOrderQuantity ?: 10
        val minQuantity = currentProduct?.minOrderQuantity ?: 1
        
        selectedQuantity = quantity.coerceIn(minQuantity, maxQuantity)
        quantityText.text = selectedQuantity.toString()
        
        quantityMinusButton.isEnabled = selectedQuantity > minQuantity
        quantityPlusButton.isEnabled = selectedQuantity < maxQuantity
        
        viewModel.updateQuantity(selectedQuantity)
    }
    
    private fun addToCart() {
        currentProduct?.let { product ->
            viewModel.addToCart(product.id, selectedQuantity)
            
            animationHelper.animateAddToCart(
                fromView = addToCartButton,
                toView = toolbar.findViewById(R.id.action_cart)
            )
        }
    }
    
    private fun buyNow() {
        currentProduct?.let { product ->
            viewModel.addToCart(product.id, selectedQuantity)
            viewModel.proceedToCheckout()
        }
    }
    
    private fun toggleWishlist() {
        currentProduct?.let { product ->
            if (isInWishlist) {
                viewModel.removeFromWishlist(product.id)
                wishlistButton.setImageResource(R.drawable.ic_wishlist_outline)
                isInWishlist = false
            } else {
                viewModel.addToWishlist(product.id)
                wishlistButton.setImageResource(R.drawable.ic_wishlist_filled)
                isInWishlist = true
                
                animationHelper.animateHeartBeat(wishlistButton)
            }
        }
    }
    
    private fun toggleCompare() {
        currentProduct?.let { product ->
            if (isInCompare) {
                viewModel.removeFromCompare(product.id)
                compareButton.setImageResource(R.drawable.ic_compare_outline)
                isInCompare = false
            } else {
                viewModel.addToCompare(product.id)
                compareButton.setImageResource(R.drawable.ic_compare_filled)
                isInCompare = true
            }
        }
    }
    
    private fun shareProduct(product: Product? = null) {
        val productToShare = product ?: currentProduct ?: return
        shareHelper.shareProduct(this, productToShare)
        analyticsManager.logShare(productToShare)
    }
    
    private fun showFullScreenImage(position: Int) {
        imageTransitionInProgress = true
        
        expandedImageView.visibility = View.VISIBLE
        imageOverlay.visibility = View.VISIBLE
        
        currentProduct?.let { product ->
            val images = listOf(product.thumbnailUrl) + product.imageUrls
            if (position < images.size) {
                imageLoader.loadImage(expandedImageView, images[position])
            }
        }
        
        ObjectAnimator.ofFloat(imageOverlay, "alpha", 0f, 1f).apply {
            duration = 300
            start()
        }
        
        ObjectAnimator.ofFloat(expandedImageView, "alpha", 0f, 1f).apply {
            duration = 300
            interpolator = DecelerateInterpolator()
            start()
        }
        
        lifecycleScope.launch {
            delay(300)
            imageTransitionInProgress = false
        }
    }
    
    private fun hideFullScreenImage() {
        if (imageTransitionInProgress) return
        
        ObjectAnimator.ofFloat(imageOverlay, "alpha", 1f, 0f).apply {
            duration = 300
            start()
        }
        
        ObjectAnimator.ofFloat(expandedImageView, "alpha", 1f, 0f).apply {
            duration = 300
            interpolator = DecelerateInterpolator()
            start()
        }
        
        lifecycleScope.launch {
            delay(300)
            expandedImageView.visibility = View.GONE
            imageOverlay.visibility = View.GONE
        }
    }
    
    private fun handleScroll(scrollY: Int) {
        val threshold = resources.getDimensionPixelSize(R.dimen.bottom_bar_threshold)
        
        if (scrollY > threshold && isBottomBarVisible) {
            animationHelper.slideDown(bottomActionBar)
            isBottomBarVisible = false
        } else if (scrollY <= threshold && !isBottomBarVisible) {
            animationHelper.slideUp(bottomActionBar)
            isBottomBarVisible = true
        }
        
        lastScrollY = scrollY
    }
    
    private fun showWriteReviewDialog() {
        if (!preferences.isLoggedIn()) {
            showLoginPrompt()
            return
        }
        
        val dialog = WriteReviewDialogFragment.newInstance(productId)
        dialog.show(supportFragmentManager, "write_review")
    }
    
    private fun showAskQuestionDialog() {
        if (!preferences.isLoggedIn()) {
            showLoginPrompt()
            return
        }
        
        val dialog = AskQuestionDialogFragment.newInstance(productId)
        dialog.show(supportFragmentManager, "ask_question")
    }
    
    private fun showReviewDetail(review: Review) {
        val dialog = ReviewDetailDialogFragment.newInstance(review)
        dialog.show(supportFragmentManager, "review_detail")
    }
    
    private fun showQuestionDetail(question: ProductQuestion) {
        val dialog = QuestionDetailDialogFragment.newInstance(question)
        dialog.show(supportFragmentManager, "question_detail")
    }
    
    private fun showSizeGuide() {
        currentProduct?.sizeChart?.let { sizeChartUrl ->
            val dialog = SizeGuideDialogFragment.newInstance(sizeChartUrl)
            dialog.show(supportFragmentManager, "size_guide")
        }
    }
    
    private fun showDeliveryInfo() {
        val dialog = DeliveryInfoDialogFragment.newInstance(productId)
        dialog.show(supportFragmentManager, "delivery_info")
    }
    
    private fun showReturnPolicy() {
        currentProduct?.returnPolicy?.let { policy ->
            val dialog = ReturnPolicyDialogFragment.newInstance(policy)
            dialog.show(supportFragmentManager, "return_policy")
        }
    }
    
    private fun showLoginPrompt() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.login_required))
            .setMessage(getString(R.string.login_required_message))
            .setPositiveButton(getString(R.string.login)) { _, _ ->
                navigateToLogin()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
    
    private fun navigateToProduct(productId: Long) {
        val intent = Intent(this, ProductDetailActivity::class.java).apply {
            putExtra("productId", productId)
        }
        startActivity(intent)
    }
    
    private fun navigateToBrand(brandId: Long) {
        val intent = Intent(this, BrandActivity::class.java).apply {
            putExtra("brandId", brandId)
        }
        startActivity(intent)
    }
    
    private fun navigateToAllReviews() {
        val intent = Intent(this, AllReviewsActivity::class.java).apply {
            putExtra("productId", productId)
        }
        startActivity(intent)
    }
    
    private fun navigateToAllQuestions() {
        val intent = Intent(this, AllQuestionsActivity::class.java).apply {
            putExtra("productId", productId)
        }
        startActivity(intent)
    }
    
    private fun navigateToCart() {
        val intent = Intent(this, CartActivity::class.java)
        startActivity(intent)
    }
    
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivityForResult(intent, REQUEST_LOGIN)
    }
    
    private fun handleNavigationEvent(event: NavigationEvent) {
        when (event) {
            is NavigationEvent.GoToCheckout -> {
                val intent = Intent(this, CheckoutActivity::class.java)
                startActivity(intent)
            }
            else -> {}
        }
    }
    
    private fun handleDeepLink() {
        intent?.let { intent ->
            deepLinkHandler.handleIntent(intent) { action, data ->
                if (action == DeepLinkAction.PRODUCT) {
                    data["productId"]?.toLongOrNull()?.let { id ->
                        productId = id
                    }
                }
            }
        }
    }
    
    private fun showError(message: String) {
        errorView.visibility = View.VISIBLE
        contentScrollView.visibility = View.GONE
        binding.errorText.text = message
    }
    
    private fun showSuccess(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_product_detail, menu)
        
        val cartItem = menu.findItem(R.id.action_cart)
        val cartActionView = cartItem.actionView
        val cartBadge = cartActionView?.findViewById<TextView>(R.id.cart_badge)
        val cartIcon = cartActionView?.findViewById<ImageView>(R.id.cart_icon)
        
        cartActionView?.setOnClickListener {
            navigateToCart()
        }
        
        lifecycleScope.launch {
            viewModel.cartCount.collectLatest { count ->
                cartBadge?.apply {
                    visibility = if (count > 0) View.VISIBLE else View.GONE
                    text = count.toString()
                }
            }
        }
        
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_cart -> {
                navigateToCart()
                true
            }
            R.id.action_search -> {
                val intent = Intent(this, SearchActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == REQUEST_LOGIN && resultCode == RESULT_OK) {
            
        }
    }
    
    override fun onResume() {
        super.onResume()
        currentProduct?.let { product ->
            analyticsManager.logViewItem(product)
        }
    }
    
    override fun onPause() {
        super.onPause()
        preferences.addRecentlyViewedProduct(productId)
    }
    
    companion object {
        private const val REQUEST_LOGIN = 1001
    }
    
    data class Specification(
        val name: String,
        val value: String
    )
}