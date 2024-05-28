package com.mood.screen.premium

import android.util.Log
import android.view.LayoutInflater
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.mood.R
import com.mood.base.BaseActivity
import com.mood.databinding.ActivityPremiumBinding
import com.mood.screen.home.MainActivity
import com.mood.screen.premium.PurchaseHelper.Companion.PRODUCT_ID
import com.mood.utils.Constant
import com.mood.utils.Define
import com.mood.utils.SharePrefUtils
import com.mood.utils.gone
import com.mood.utils.openActivity
import com.mood.utils.setOnSafeClick
import com.mood.utils.showToast
import com.mood.utils.trackingEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PremiumActivity : BaseActivity<ActivityPremiumBinding>() {
    companion object {
        const val TAG = Constant.TAG

        enum class PackPremium {
            Monthly, Yearly
        }
    }

    private var productDetails: MutableList<ProductDetails> = mutableListOf()

    private val purchaseHelper by lazy {
        PurchaseHelper(this)
    }
    private val isFromStartApp by lazy {
        intent?.extras?.getBoolean(Constant.IS_FROM_START_APP, false) ?: false
    }
    private var packSelected = PackPremium.Yearly

    override fun initView() {
    }

    override fun initData() {
    }

    override fun initListener() {
        binding.layoutBtnBack.setOnSafeClick {
            Define.CLICK_CLOSE_PREMIUM.trackingEvent()
            onBack()
        }
        binding.btnBack.setOnSafeClick {
            Define.CLICK_CLOSE_PREMIUM.trackingEvent()
            onBack()
        }
        binding.layoutPackYearly.setOnClickListener {
            packSelected = PackPremium.Yearly
            setupPackSelect()
        }
        binding.layoutPackMonthly.setOnClickListener {
            packSelected = PackPremium.Monthly
            setupPackSelect()
        }
        binding.btnContinue.setOnSafeClick {
            Define.CLICK_CONTINUE_PREMIUM.trackingEvent()
            showProductAvailable()
        }
    }

    private fun setupPackSelect() {
        when (packSelected) {
            PackPremium.Monthly -> {
                binding.layoutPackMonthly.setBackgroundResource(R.drawable.bg_pack_premium_select)
                binding.layoutPackYearly.setBackgroundResource(R.drawable.bg_pack_premium_un_select)
            }

            else -> {
                binding.layoutPackMonthly.setBackgroundResource(R.drawable.bg_pack_premium_un_select)
                binding.layoutPackYearly.setBackgroundResource(R.drawable.bg_pack_premium_select)
            }
        }
    }

    private fun getProductIdPack(): String = when (packSelected) {
        PackPremium.Monthly -> PurchaseHelper.PRODUCT_PREMIUM_MONTHLY
        PackPremium.Yearly -> PurchaseHelper.PRODUCT_PREMIUM_YEARLY
    }

    private fun showProductAvailable() {
        Log.d(TAG, "showProduct: $productDetails")
        val product = productDetails.find { it.productId == PRODUCT_ID }
        product?.let {
            val listProductSub = product.subscriptionOfferDetails
            val productRequest = listProductSub?.firstOrNull { item ->
                item?.basePlanId == getProductIdPack()
            }
            purchaseHelper.launchBillingFlow(this, product, productRequest?.offerToken ?: "")
        } ?: kotlin.run {
            runOnUiThread {
                showToast(getString(R.string.product_not_available))
            }
        }
    }

    private fun getPricePack() {
        purchaseHelper.getProductDetails(PurchaseHelper.SUBS_TYPE, PRODUCT_ID) { listProductDetail ->
            productDetails.clear()
            productDetails.addAll(listProductDetail)
            Log.d(TAG, "showProduct: $listProductDetail")
            val product = listProductDetail.find { it.productId == PRODUCT_ID }
            product?.let {
                val listProductSub = product.subscriptionOfferDetails
                val productRequestMonth = listProductSub?.firstOrNull { item ->
                    item?.basePlanId == PurchaseHelper.PRODUCT_PREMIUM_MONTHLY
                }
                val productRequest12Month = listProductSub?.firstOrNull { item ->
                    item?.basePlanId == PurchaseHelper.PRODUCT_PREMIUM_YEARLY
                }
                runOnUiThread {
                    productRequestMonth?.pricingPhases?.pricingPhaseList?.get(0)?.formattedPrice?.let {
                        binding.tvPriceMonth.text = it
                        binding.loadingViewPriceMonth.gone()
                    }
                    productRequest12Month?.pricingPhases?.pricingPhaseList?.get(0)?.formattedPrice?.let {
                        binding.tvPriceYear.text = it
                        binding.loadingViewPriceYear.gone()
                    }
                }
            } ?: kotlin.run {
                runOnUiThread {
                    showToast(getString(R.string.product_not_available))
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        purchaseHelper.onServiceReady { isReady ->
            if (isReady) {
                getPricePack()
                Log.d(TAG, "service connected")
                purchaseHelper.queryPurchases(
                    PurchaseHelper.SUBS_TYPE
                ) { billingResult, listPurchase ->
                    if (billingResult.responseCode == PurchaseHelper.RESPONSE_CODE_OK) {
                        runOnUiThread {
                            if (listPurchase.isNotEmpty()) {//user is bought premium
                                Log.d(TAG, "onResume: $listPurchase")
                                Log.d(TAG, "user owner bought product $listPurchase")
                                SharePrefUtils.setBought(true)
                            } else {
                                SharePrefUtils.setBought(false)
                                Log.d(TAG, "onResume: user not bought")
                            }
                        }
                    }
                }
            } else {
                Log.d(TAG, "service not connected")
            }
            purchaseHelper.onPurchaseStatus { status, responseCode ->
                runOnUiThread {
                    if (status) {//change layout
                        SharePrefUtils.setBought(true)
                        CoroutineScope(Dispatchers.Main).launch {
                            showToast("payment_success, auto back after two seconds")
                            Constant.isPremium.value = true
                            delay(2000)
                            onBack()
                        }
                    } else {
                        val msg = when (responseCode) {
                            BillingClient.BillingResponseCode.SERVICE_TIMEOUT -> "SERVICE_TIMEOUT"
                            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> "FEATURE_NOT_SUPPORTED"
                            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> "SERVICE_DISCONNECTED"
                            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> "SERVICE_UNAVAILABLE"
                            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> "BILLING_UNAVAILABLE"
                            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> "ITEM_UNAVAILABLE"
                            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> "DEVELOPER_ERROR"
                            BillingClient.BillingResponseCode.ERROR -> "ERROR"
                            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> "ITEM_ALREADY_OWNED"
                            BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> "ITEM_NOT_OWNED"
                            else -> null
                        }
                        if (msg != null) {
                            showToast(msg)
                        }
                        SharePrefUtils.setBought(false)
                    }
                }
            }

        }
    }

    override fun onDestroy() {
        purchaseHelper.endConnect()
        super.onDestroy()
    }

    fun onBack() {
        if (isFromStartApp) {
            openActivity(MainActivity::class.java, isFinish = true)
        } else {
            finish()
        }
    }

    override fun onBackPressed() {
        onBack()
    }

    override fun inflateViewBinding(inflater: LayoutInflater): ActivityPremiumBinding {
        return ActivityPremiumBinding.inflate(inflater)
    }

}