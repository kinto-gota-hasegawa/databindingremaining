package com.example.ecommerce.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date
import java.math.BigDecimal

@Parcelize
data class Product(
    val id: Long,
    val name: String,
    val description: String,
    val price: BigDecimal,
    val discountPrice: BigDecimal? = null,
    val currency: String = "JPY",
    val category: ProductCategory,
    val subCategory: String? = null,
    val brand: Brand,
    val sku: String,
    val barcode: String? = null,
    val weight: Double? = null,
    val dimensions: ProductDimensions? = null,
    val imageUrls: List<String>,
    val thumbnailUrl: String,
    val videoUrl: String? = null,
    val stock: Int,
    val availableStock: Int,
    val reservedStock: Int = 0,
    val minOrderQuantity: Int = 1,
    val maxOrderQuantity: Int = 10,
    val rating: Float,
    val reviewCount: Int,
    val tags: List<String>,
    val features: List<ProductFeature>,
    val specifications: Map<String, String>,
    val isActive: Boolean = true,
    val isFeatured: Boolean = false,
    val isNewArrival: Boolean = false,
    val isBestSeller: Boolean = false,
    val isOnSale: Boolean = false,
    val salePercentage: Int? = null,
    val createdAt: Date,
    val updatedAt: Date,
    val launchDate: Date? = null,
    val discontinuedDate: Date? = null,
    val warranty: ProductWarranty? = null,
    val shippingInfo: ShippingInfo,
    val returnPolicy: ReturnPolicy,
    val manufacturer: Manufacturer? = null,
    val origin: String? = null,
    val material: List<String>? = null,
    val color: String? = null,
    val size: String? = null,
    val variants: List<ProductVariant>? = null,
    val relatedProductIds: List<Long>? = null,
    val frequentlyBoughtTogetherIds: List<Long>? = null,
    val crossSellProductIds: List<Long>? = null,
    val upSellProductIds: List<Long>? = null,
    val bundleProducts: List<BundleProduct>? = null,
    val customAttributes: Map<String, Any>? = null,
    val seoTitle: String? = null,
    val seoDescription: String? = null,
    val seoKeywords: List<String>? = null,
    val slug: String,
    val views: Long = 0,
    val likes: Long = 0,
    val shares: Long = 0,
    val wishlistCount: Long = 0,
    val cartAdditions: Long = 0,
    val purchases: Long = 0,
    val averageRating: Double = 0.0,
    val totalRevenue: BigDecimal = BigDecimal.ZERO,
    val profitMargin: Double? = null,
    val costPrice: BigDecimal? = null,
    val wholesalePrice: BigDecimal? = null,
    val taxRate: Double = 0.1,
    val taxCategory: String = "standard",
    val hsCode: String? = null,
    val requiresShipping: Boolean = true,
    val requiresAge: Int? = null,
    val digitalProduct: DigitalProductInfo? = null,
    val subscription: SubscriptionInfo? = null,
    val giftOptions: GiftOptions? = null,
    val ecoFriendly: Boolean = false,
    val sustainabilityScore: Int? = null,
    val carbonFootprint: Double? = null,
    val recyclable: Boolean = false,
    val badges: List<ProductBadge>? = null,
    val certifications: List<Certification>? = null,
    val awards: List<Award>? = null,
    val promotions: List<Promotion>? = null,
    val coupons: List<Coupon>? = null,
    val loyaltyPoints: Int? = null,
    val questions: List<ProductQuestion>? = null,
    val faqs: List<FAQ>? = null,
    val userManualUrl: String? = null,
    val assemblyInstructions: String? = null,
    val careInstructions: String? = null,
    val allergens: List<String>? = null,
    val nutritionInfo: NutritionInfo? = null,
    val ingredients: List<String>? = null,
    val expiryDate: Date? = null,
    val storageInstructions: String? = null,
    val usageInstructions: String? = null,
    val warnings: List<String>? = null,
    val ageGroup: String? = null,
    val gender: String? = null,
    val occasion: List<String>? = null,
    val season: String? = null,
    val style: String? = null,
    val pattern: String? = null,
    val customizable: Boolean = false,
    val personalizationOptions: List<PersonalizationOption>? = null,
    val madeToOrder: Boolean = false,
    val leadTime: Int? = null,
    val preOrder: Boolean = false,
    val preOrderDate: Date? = null,
    val backOrder: Boolean = false,
    val backOrderDate: Date? = null,
    val limitedEdition: Boolean = false,
    val editionSize: Int? = null,
    val editionNumber: Int? = null,
    val collectible: Boolean = false,
    val vintage: Boolean = false,
    val year: Int? = null,
    val condition: String? = null,
    val refurbished: Boolean = false,
    val openBox: Boolean = false,
    val damaged: Boolean = false,
    val damageDescription: String? = null,
    val insuranceAvailable: Boolean = false,
    val insuranceOptions: List<InsuranceOption>? = null,
    val financingAvailable: Boolean = false,
    val financingOptions: List<FinancingOption>? = null,
    val tradeInEligible: Boolean = false,
    val tradeInValue: BigDecimal? = null,
    val rentalAvailable: Boolean = false,
    val rentalOptions: List<RentalOption>? = null,
    val bulkDiscount: List<BulkDiscount>? = null,
    val b2bPrice: BigDecimal? = null,
    val minimumB2BQuantity: Int? = null,
    val dealerPrice: BigDecimal? = null,
    val distributorPrice: BigDecimal? = null,
    val msrp: BigDecimal? = null,
    val map: BigDecimal? = null,
    val compareAtPrice: BigDecimal? = null,
    val previousPrice: BigDecimal? = null,
    val futurePrice: BigDecimal? = null,
    val futurePriceDate: Date? = null,
    val pricingTiers: List<PricingTier>? = null,
    val dynamicPricing: Boolean = false,
    val priceHistory: List<PricePoint>? = null,
    val stockHistory: List<StockPoint>? = null,
    val salesHistory: List<SalesPoint>? = null,
    val forecastedDemand: Int? = null,
    val reorderPoint: Int? = null,
    val reorderQuantity: Int? = null,
    val supplier: Supplier? = null,
    val alternativeSuppliers: List<Supplier>? = null,
    val warehouseLocations: List<WarehouseLocation>? = null,
    val pickingLocation: String? = null,
    val binLocation: String? = null,
    val zoneLocation: String? = null,
    val customsValue: BigDecimal? = null,
    val countryOfOrigin: String? = null,
    val harmonizedCode: String? = null,
    val exportRestrictions: List<String>? = null,
    val importRestrictions: List<String>? = null,
    val dangerousGoods: Boolean = false,
    val unNumber: String? = null,
    val hazmatClass: String? = null,
    val packingGroup: String? = null,
    val flashPoint: Double? = null,
    val msdsUrl: String? = null,
    val regulatoryCompliance: List<String>? = null,
    val patents: List<String>? = null,
    val trademarks: List<String>? = null,
    val copyrights: List<String>? = null,
    val licenses: List<String>? = null,
    val modelNumber: String? = null,
    val partNumber: String? = null,
    val serialNumber: String? = null,
    val batchNumber: String? = null,
    val lotNumber: String? = null,
    val version: String? = null,
    val revision: String? = null,
    val compatibility: List<String>? = null,
    val requirements: List<String>? = null,
    val includedItems: List<String>? = null,
    val notIncluded: List<String>? = null,
    val accessories: List<Long>? = null,
    val replacementParts: List<Long>? = null,
    val consumables: List<Long>? = null,
    val maintenanceSchedule: String? = null,
    val serviceInterval: Int? = null,
    val expectedLifespan: Int? = null,
    val disposalInstructions: String? = null,
    val recyclingInstructions: String? = null,
    val productUrl: String? = null,
    val canonicalUrl: String? = null,
    val shortUrl: String? = null,
    val qrCode: String? = null,
    val nfcTag: String? = null,
    val rfidTag: String? = null,
    val bluetoothBeacon: String? = null,
    val ar3DModel: String? = null,
    val vr3DModel: String? = null,
    val interactive360View: String? = null,
    val augmentedRealityEnabled: Boolean = false,
    val virtualTryOn: Boolean = false,
    val sizeChart: String? = null,
    val fitGuide: String? = null,
    val measurementGuide: String? = null,
    val comparisonChart: String? = null,
    val installationGuide: String? = null,
    val troubleshootingGuide: String? = null,
    val warrantyRegistrationUrl: String? = null,
    val productRegistrationUrl: String? = null,
    val supportUrl: String? = null,
    val communityForumUrl: String? = null,
    val socialMediaLinks: Map<String, String>? = null,
    val influencerEndorsements: List<Endorsement>? = null,
    val celebrityEndorsements: List<Endorsement>? = null,
    val mediaFeatures: List<MediaFeature>? = null,
    val pressReleases: List<String>? = null,
    val caseStudies: List<String>? = null,
    val whitepapers: List<String>? = null,
    val testimonials: List<Testimonial>? = null,
    val demonstrations: List<String>? = null,
    val tutorials: List<String>? = null,
    val webinars: List<String>? = null,
    val podcasts: List<String>? = null,
    val ebooks: List<String>? = null,
    val infographics: List<String>? = null,
    val salesPresentations: List<String>? = null,
    val competitorComparison: Map<String, Any>? = null,
    val marketPosition: String? = null,
    val targetMarket: List<String>? = null,
    val targetAudience: List<String>? = null,
    val useCases: List<String>? = null,
    val benefits: List<String>? = null,
    val uniqueSellingPoints: List<String>? = null,
    val valueProposition: String? = null,
    val roi: String? = null,
    val tco: BigDecimal? = null,
    val paybackPeriod: Int? = null,
    val customerSegments: List<String>? = null,
    val personas: List<String>? = null,
    val psychographics: Map<String, Any>? = null,
    val demographics: Map<String, Any>? = null,
    val geographics: Map<String, Any>? = null,
    val behavioristics: Map<String, Any>? = null,
    val salesChannel: List<String>? = null,
    val distributionChannel: List<String>? = null,
    val marketingChannel: List<String>? = null,
    val advertisingChannel: List<String>? = null,
    val affiliateProgram: Boolean = false,
    val affiliateCommission: Double? = null,
    val referralProgram: Boolean = false,
    val referralBonus: BigDecimal? = null,
    val dropshipping: Boolean = false,
    val dropshipSupplier: String? = null,
    val privateLabelAvailable: Boolean = false,
    val whitelabelAvailable: Boolean = false,
    val oemAvailable: Boolean = false,
    val odmAvailable: Boolean = false,
    val moq: Int? = null,
    val sampleAvailable: Boolean = false,
    val samplePrice: BigDecimal? = null,
    val catalogUrl: String? = null,
    val specSheetUrl: String? = null,
    val cadFileUrl: String? = null,
    val bimFileUrl: String? = null,
    val technicalDrawingsUrl: String? = null,
    val installationVideoUrl: String? = null,
    val maintenanceVideoUrl: String? = null,
    val repairVideoUrl: String? = null,
    val unboxingVideoUrl: String? = null,
    val reviewVideoUrls: List<String>? = null,
    val comparisonVideoUrls: List<String>? = null,
    val metadata: Map<String, Any>? = null
) : Parcelable

@Parcelize
data class ProductCategory(
    val id: Long,
    val name: String,
    val parentId: Long? = null,
    val level: Int,
    val path: String,
    val imageUrl: String? = null,
    val description: String? = null,
    val isActive: Boolean = true,
    val sortOrder: Int = 0,
    val productCount: Int = 0,
    val attributes: Map<String, Any>? = null
) : Parcelable

@Parcelize
data class Brand(
    val id: Long,
    val name: String,
    val logoUrl: String? = null,
    val description: String? = null,
    val website: String? = null,
    val country: String? = null,
    val established: Int? = null,
    val isVerified: Boolean = false,
    val isPremium: Boolean = false,
    val rating: Float? = null
) : Parcelable

@Parcelize
data class ProductDimensions(
    val length: Double,
    val width: Double,
    val height: Double,
    val unit: String = "cm",
    val packageLength: Double? = null,
    val packageWidth: Double? = null,
    val packageHeight: Double? = null,
    val packageWeight: Double? = null
) : Parcelable

@Parcelize
data class ProductFeature(
    val id: Long,
    val title: String,
    val description: String,
    val iconUrl: String? = null,
    val priority: Int = 0
) : Parcelable

@Parcelize
data class ProductWarranty(
    val duration: Int,
    val unit: String = "months",
    val type: String,
    val coverage: String,
    val terms: String? = null,
    val registrationRequired: Boolean = false,
    val internationalCoverage: Boolean = false
) : Parcelable

@Parcelize
data class ShippingInfo(
    val weight: Double,
    val dimensions: ProductDimensions,
    val shippingClass: String,
    val freeShipping: Boolean = false,
    val freeShippingThreshold: BigDecimal? = null,
    val estimatedDays: IntRange,
    val expeditedAvailable: Boolean = false,
    val internationalShipping: Boolean = false,
    val shippingRestrictions: List<String>? = null,
    val fulfillmentMethod: String = "warehouse",
    val handlingTime: Int = 1
) : Parcelable

@Parcelize
data class ReturnPolicy(
    val returnable: Boolean = true,
    val returnWindow: Int = 30,
    val returnWindowUnit: String = "days",
    val restockingFee: Double? = null,
    val returnShippingPaidBy: String = "customer",
    val refundMethod: String = "original",
    val exchangeAllowed: Boolean = true,
    val conditions: List<String>? = null
) : Parcelable

@Parcelize
data class Manufacturer(
    val id: Long,
    val name: String,
    val country: String,
    val website: String? = null,
    val certifications: List<String>? = null,
    val factoryLocations: List<String>? = null
) : Parcelable

@Parcelize
data class ProductVariant(
    val id: Long,
    val productId: Long,
    val sku: String,
    val name: String,
    val price: BigDecimal,
    val stock: Int,
    val attributes: Map<String, String>,
    val imageUrl: String? = null,
    val weight: Double? = null,
    val dimensions: ProductDimensions? = null,
    val barcode: String? = null,
    val isDefault: Boolean = false
) : Parcelable

@Parcelize
data class BundleProduct(
    val productId: Long,
    val quantity: Int,
    val discount: Double? = null,
    val isMandatory: Boolean = true
) : Parcelable

@Parcelize
data class DigitalProductInfo(
    val downloadUrl: String,
    val fileSize: Long,
    val fileFormat: String,
    val licenseType: String,
    val licenseKey: String? = null,
    val downloadLimit: Int? = null,
    val downloadExpiry: Date? = null,
    val drm: Boolean = false
) : Parcelable

@Parcelize
data class SubscriptionInfo(
    val interval: String,
    val intervalCount: Int,
    val trialDays: Int? = null,
    val price: BigDecimal,
    val setupFee: BigDecimal? = null,
    val cancellationPolicy: String
) : Parcelable

@Parcelize
data class GiftOptions(
    val giftWrap: Boolean = true,
    val giftWrapPrice: BigDecimal? = null,
    val giftMessage: Boolean = true,
    val giftReceipt: Boolean = true
) : Parcelable

@Parcelize
data class ProductBadge(
    val id: Long,
    val type: String,
    val label: String,
    val color: String,
    val iconUrl: String? = null,
    val priority: Int = 0
) : Parcelable

@Parcelize
data class Certification(
    val name: String,
    val issuer: String,
    val number: String,
    val validUntil: Date? = null,
    val documentUrl: String? = null
) : Parcelable

@Parcelize
data class Award(
    val name: String,
    val year: Int,
    val category: String? = null,
    val organization: String
) : Parcelable

@Parcelize
data class Promotion(
    val id: Long,
    val type: String,
    val description: String,
    val discountAmount: BigDecimal? = null,
    val discountPercent: Double? = null,
    val startDate: Date,
    val endDate: Date,
    val conditions: List<String>? = null,
    val code: String? = null
) : Parcelable

@Parcelize
data class Coupon(
    val code: String,
    val discount: Double,
    val type: String,
    val validUntil: Date,
    val minimumPurchase: BigDecimal? = null,
    val usageLimit: Int? = null
) : Parcelable

@Parcelize
data class ProductQuestion(
    val id: Long,
    val question: String,
    val answer: String? = null,
    val askedBy: String,
    val askedAt: Date,
    val answeredBy: String? = null,
    val answeredAt: Date? = null,
    val helpful: Int = 0
) : Parcelable

@Parcelize
data class FAQ(
    val question: String,
    val answer: String,
    val category: String? = null,
    val order: Int = 0
) : Parcelable

@Parcelize
data class NutritionInfo(
    val servingSize: String,
    val servingsPerContainer: Int,
    val calories: Int,
    val totalFat: Double,
    val saturatedFat: Double,
    val transFat: Double,
    val cholesterol: Double,
    val sodium: Double,
    val totalCarbohydrate: Double,
    val dietaryFiber: Double,
    val sugars: Double,
    val protein: Double,
    val vitamins: Map<String, Double>? = null,
    val minerals: Map<String, Double>? = null
) : Parcelable

@Parcelize
data class PersonalizationOption(
    val type: String,
    val label: String,
    val required: Boolean = false,
    val maxLength: Int? = null,
    val options: List<String>? = null,
    val price: BigDecimal? = null
) : Parcelable

@Parcelize
data class InsuranceOption(
    val name: String,
    val coverage: String,
    val price: BigDecimal,
    val duration: Int,
    val provider: String
) : Parcelable

@Parcelize
data class FinancingOption(
    val provider: String,
    val apr: Double,
    val term: Int,
    val minimumAmount: BigDecimal,
    val monthlyPayment: BigDecimal
) : Parcelable

@Parcelize
data class RentalOption(
    val period: String,
    val price: BigDecimal,
    val deposit: BigDecimal,
    val terms: String
) : Parcelable

@Parcelize
data class BulkDiscount(
    val minQuantity: Int,
    val maxQuantity: Int? = null,
    val discountPercent: Double,
    val pricePerUnit: BigDecimal? = null
) : Parcelable

@Parcelize
data class PricingTier(
    val name: String,
    val minQuantity: Int,
    val maxQuantity: Int? = null,
    val price: BigDecimal
) : Parcelable

@Parcelize
data class PricePoint(
    val date: Date,
    val price: BigDecimal,
    val event: String? = null
) : Parcelable

@Parcelize
data class StockPoint(
    val date: Date,
    val quantity: Int,
    val event: String? = null
) : Parcelable

@Parcelize
data class SalesPoint(
    val date: Date,
    val quantity: Int,
    val revenue: BigDecimal
) : Parcelable

@Parcelize
data class Supplier(
    val id: Long,
    val name: String,
    val contact: String,
    val email: String,
    val phone: String,
    val address: String,
    val leadTime: Int,
    val minimumOrder: Int,
    val paymentTerms: String
) : Parcelable

@Parcelize
data class WarehouseLocation(
    val warehouseId: Long,
    val warehouseName: String,
    val quantity: Int,
    val aisle: String? = null,
    val shelf: String? = null,
    val bin: String? = null
) : Parcelable

@Parcelize
data class Endorsement(
    val name: String,
    val title: String? = null,
    val quote: String,
    val imageUrl: String? = null,
    val verifiedAt: Date
) : Parcelable

@Parcelize
data class MediaFeature(
    val publication: String,
    val title: String,
    val url: String,
    val date: Date,
    val quote: String? = null
) : Parcelable

@Parcelize
data class Testimonial(
    val id: Long,
    val customerName: String,
    val rating: Int,
    val title: String,
    val content: String,
    val date: Date,
    val verified: Boolean = false,
    val helpful: Int = 0,
    val imageUrls: List<String>? = null
) : Parcelable