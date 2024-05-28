package com.mood.screen.premium

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.AcknowledgePurchaseResponseListener
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.SkuDetailsParams
import com.android.billingclient.api.SkuDetailsResponseListener
import com.mood.utils.Constant
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.collect.ImmutableList
import org.json.JSONException


class PurchaseHelper(val context: Context) {

    private var readyListener: ((Boolean) -> Unit)? = null
    private var showDialogPaymentFail: ((Boolean) -> Unit)? = null
    private var productDetail: ProductDetails? = null
    private var purchaseStatus: ((Boolean, Int) -> Unit)? = null

    init {
        startConnect()
    }

    companion object {
        const val IN_APP_TYPE = BillingClient.ProductType.INAPP
        const val SUBS_TYPE = BillingClient.ProductType.SUBS
        const val PRODUCT_PREMIUM_MONTHLY = "monthly"
        const val PRODUCT_PREMIUM_YEARLY = "yearly"
        const val RESPONSE_CODE_OK = BillingClient.BillingResponseCode.OK
        const val PRODUCT_ID = "premium_1"
    }

    private lateinit var purchasesUpdatedListener: PurchasesUpdatedListener
    private lateinit var billingClientStateListener: BillingClientStateListener
    private lateinit var billingClient: BillingClient

    private lateinit var acknowledgePurchaseResponseListener: AcknowledgePurchaseResponseListener

    fun endConnect() {
        billingClient.endConnection()
    }

    private fun initVariable() {
        billingClientStateListener = object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    readyListener?.invoke(true)
                } else {
                    readyListener?.invoke(false)
                }
            }

            override fun onBillingServiceDisconnected() {
                readyListener?.invoke(false)
            }
        }
        acknowledgePurchaseResponseListener = AcknowledgePurchaseResponseListener {}
        purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                //success
                purchases.forEach { item ->
                    handlePurchase(item)
                }
            } else {
                var rs = when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.SERVICE_TIMEOUT -> "SERVICE_TIMEOUT"
                    BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> "FEATURE_NOT_SUPPORTED"
                    BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> "SERVICE_DISCONNECTED"
                    BillingClient.BillingResponseCode.OK -> "OK"
                    BillingClient.BillingResponseCode.USER_CANCELED -> "USER_CANCELED"
                    BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> "SERVICE_UNAVAILABLE"
                    BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> "BILLING_UNAVAILABLE"
                    BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> "ITEM_UNAVAILABLE"
                    BillingClient.BillingResponseCode.DEVELOPER_ERROR -> "DEVELOPER_ERROR"
                    BillingClient.BillingResponseCode.ERROR -> "ERROR"
                    BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> "ITEM_ALREADY_OWNED"
                    BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> "ITEM_NOT_OWNED"
                    else -> ""
                }
                Log.d(Constant.TAG, "responseCode - $rs")
                //failed
                purchaseStatus?.invoke(false, billingResult.responseCode)
            }
        }
        billingClient = BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()
    }

    private fun startConnect() {
        initVariable()
        billingClient.startConnection(billingClientStateListener)
    }

    fun onServiceReady(listener: (Boolean) -> Unit) {
        readyListener = listener
    }

    fun onShowDialogBuyFail(listener: (Boolean) -> Unit) {
        showDialogPaymentFail = listener
    }

    fun onPurchaseStatus(listener: (Boolean, Int) -> Unit) {
        purchaseStatus = listener
    }

    fun queryPurchases(productType: String, onQueryPurchasesResponseListener: (BillingResult, List<Purchase>) -> Unit) {
        val params = QueryPurchasesParams.newBuilder().setProductType(productType).build()
        billingClient.queryPurchasesAsync(params) { billingResult, listPurchase ->
            Log.d(
                Constant.TAG, "queryProductUserBought: " +
                        "responseCode:${billingResult.responseCode}  - listPurchase: $listPurchase"
            )
            onQueryPurchasesResponseListener.invoke(billingResult, listPurchase)
        }
    }

    /**
     * @param productType : #BillingClient.ProductType
     */
    fun getProductDetails(
        productType: String,
        productId: String,
        resultListener: (List<ProductDetails>) -> Unit
    ) {
        if (!billingClient.isReady) {
            billingClient.startConnection(billingClientStateListener)
        }
        val queryProductDetailsParams =
            QueryProductDetailsParams.newBuilder()
                .setProductList(
                    ImmutableList.of(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(productId)
                            .setProductType(productType)
                            .build()
                    )
                )
                .build()
        billingClient.queryProductDetailsAsync(queryProductDetailsParams) { billingResult,
                                                                            productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                resultListener.invoke(productDetailsList)
            } else {
                Log.e(Constant.TAG, "${billingResult.responseCode}")
            }
        }
    }

    fun launchBillingFlow(activity: Activity, productDetails: ProductDetails, offerToken: String) {
        Log.d(Constant.TAG, "------launchBillingFlow---------")
        productDetail = productDetails
        val productDetailsParamsList: List<BillingFlowParams.ProductDetailsParams> = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(offerToken)
                .build()
        )
        val billingFlowParams =
            BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
        billingClient.launchBillingFlow(activity, billingFlowParams.build())
    }

    private fun acknowledge(purchase: Purchase) {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        //For non-consumables:
        billingClient.acknowledgePurchase(
            acknowledgePurchaseParams,
            acknowledgePurchaseResponseListener
        )
    }

    @Throws(JSONException::class)
    fun handlePurchase(purchase: Purchase) {
        //TODO: Không xóa hoặc comment hàm này
        acknowledge(purchase)
        purchaseStatus?.invoke(true, BillingClient.BillingResponseCode.OK)
    }


    fun initiatePurchase(activity: Activity) {
        val skuList: MutableList<String> = ArrayList()
        skuList.add(PRODUCT_ID)
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS)
        billingClient.querySkuDetailsAsync(params.build(),
            SkuDetailsResponseListener { billingResult, skuDetailsList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    if (skuDetailsList != null && skuDetailsList.size > 0) {
                        val flowParams = BillingFlowParams.newBuilder()
                            .setSkuDetails(skuDetailsList[0])
                            .build()
                        billingClient.launchBillingFlow(activity, flowParams)
                    } else {
                        //try to add item/product id "consumable" inside managed product in google play console
                        Log.d(Constant.TAG, "Purchase Item not Found")
                    }
                } else {
                    Log.d(Constant.TAG, " Error " + billingResult.debugMessage)
                }
            })
    }
}