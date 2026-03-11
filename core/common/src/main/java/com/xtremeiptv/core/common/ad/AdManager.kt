package com.xtremeiptv.core.common.ad

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val _isInitialized = MutableStateFlow(false)
    private val _bannerAdView = MutableStateFlow<AdView?>(null)
    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var rewardedInterstitialAd: RewardedInterstitialAd? = null
    
    private val adLoadScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private val adLoadCallbacks = mutableMapOf<String, MutableList<(Boolean) -> Unit>>()
    private val adDisplayCallbacks = mutableMapOf<String, MutableList<() -> Unit>>()
    
    init {
        initializeMobileAds()
    }
    
    private fun initializeMobileAds() {
        MobileAds.initialize(context) { initializationStatus ->
            _isInitialized.value = true
            
            // Pre-load ads
            loadInterstitialAd()
            loadRewardedAd()
            loadRewardedInterstitialAd()
        }
    }
    
    fun createBannerAd(
        activity: Activity,
        adUnitId: String,
        adSize: AdSize = AdSize.BANNER
    ): AdView {
        val adView = AdView(activity).apply {
            setAdSize(adSize)
            this.adUnitId = adUnitId
            
            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    _bannerAdView.value = this@apply
                    notifyAdLoaded(adUnitId)
                }
                
                override onAdFailedToLoad(error: LoadAdError) {
                    notifyAdFailed(adUnitId, error)
                }
                
                override onAdClicked() {
                    notifyAdClicked(adUnitId)
                }
                
                override onAdImpression() {
                    notifyAdImpression(adUnitId)
                }
            }
        }
        
        return adView
    }
    
    fun loadBannerAd(adView: AdView) {
        if (!_isInitialized.value) return
        
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }
    
    private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        
        InterstitialAd.load(
            context,
            INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    setupInterstitialCallbacks(ad)
                    notifyAdLoaded(INTERSTITIAL_AD_UNIT_ID)
                }
                
                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    notifyAdFailed(INTERSTITIAL_AD_UNIT_ID, error)
                    
                    // Retry after delay
                    adLoadScope.launch {
                        delay(60000) // Retry after 1 minute
                        loadInterstitialAd()
                    }
                }
            }
        )
    }
    
    private fun setupInterstitialCallbacks(ad: InterstitialAd) {
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                // Load next interstitial
                loadInterstitialAd()
            }
            
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                // Load next interstitial
                loadInterstitialAd()
            }
            
            override fun onAdShowedFullScreenContent() {
                interstitialAd = null
                notifyAdShowed(INTERSTITIAL_AD_UNIT_ID)
            }
        }
    }
    
    fun showInterstitialAd(
        activity: Activity,
        onAdShown: () -> Unit = {},
        onAdDismissed: () -> Unit = {},
        onAdFailed: (AdError?) -> Unit = {}
    ) {
        interstitialAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    onAdDismissed()
                    loadInterstitialAd()
                }
                
                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    onAdFailed(error)
                    loadInterstitialAd()
                }
                
                override fun onAdShowedFullScreenContent() {
                    onAdShown()
                }
            }
            
            ad.show(activity)
        } ?: run {
            onAdFailed(null)
        }
    }
    
    private fun loadRewardedAd() {
        val adRequest = AdRequest.Builder().build()
        
        RewardedAd.load(
            context,
            REWARDED_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    setupRewardedCallbacks(ad)
                    notifyAdLoaded(REWARDED_AD_UNIT_ID)
                }
                
                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    notifyAdFailed(REWARDED_AD_UNIT_ID, error)
                    
                    adLoadScope.launch {
                        delay(60000)
                        loadRewardedAd()
                    }
                }
            }
        )
    }
    
    private fun setupRewardedCallbacks(ad: RewardedAd) {
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                loadRewardedAd()
            }
            
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                loadRewardedAd()
            }
            
            override fun onAdShowedFullScreenContent() {
                rewardedAd = null
                notifyAdShowed(REWARDED_AD_UNIT_ID)
            }
        }
    }
    
    fun showRewardedAd(
        activity: Activity,
        onUserEarnedReward: (RewardItem) -> Unit,
        onAdFailed: (AdError?) -> Unit = {}
    ) {
        rewardedAd?.let { ad ->
            ad.show(activity) { rewardItem ->
                onUserEarnedReward(rewardItem)
                loadRewardedAd()
            }
        } ?: run {
            onAdFailed(null)
        }
    }
    
    private fun loadRewardedInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        
        RewardedInterstitialAd.load(
            context,
            REWARDED_INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    rewardedInterstitialAd = ad
                    setupRewardedInterstitialCallbacks(ad)
                    notifyAdLoaded(REWARDED_INTERSTITIAL_AD_UNIT_ID)
                }
                
                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedInterstitialAd = null
                    notifyAdFailed(REWARDED_INTERSTITIAL_AD_UNIT_ID, error)
                    
                    adLoadScope.launch {
                        delay(60000)
                        loadRewardedInterstitialAd()
                    }
                }
            }
        )
    }
    
    private fun setupRewardedInterstitialCallbacks(ad: RewardedInterstitialAd) {
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                loadRewardedInterstitialAd()
            }
            
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                loadRewardedInterstitialAd()
            }
            
            override fun onAdShowedFullScreenContent() {
                rewardedInterstitialAd = null
                notifyAdShowed(REWARDED_INTERSTITIAL_AD_UNIT_ID)
            }
        }
    }
    
    fun showRewardedInterstitialAd(
        activity: Activity,
        onUserEarnedReward: (RewardItem) -> Unit,
        onAdFailed: (AdError?) -> Unit = {}
    ) {
        rewardedInterstitialAd?.let { ad ->
            ad.show(activity) { rewardItem ->
                onUserEarnedReward(rewardItem)
                loadRewardedInterstitialAd()
            }
        } ?: run {
            onAdFailed(null)
        }
    }
    
    fun canShowInterstitial(): Boolean = interstitialAd != null
    
    fun canShowRewarded(): Boolean = rewardedAd != null
    
    fun canShowRewardedInterstitial(): Boolean = rewardedInterstitialAd != null
    
    fun addAdLoadListener(adUnitId: String, callback: (Boolean) -> Unit) {
        adLoadCallbacks.getOrPut(adUnitId) { mutableListOf() }.add(callback)
    }
    
    fun addAdDisplayListener(adUnitId: String, callback: () -> Unit) {
        adDisplayCallbacks.getOrPut(adUnitId) { mutableListOf() }.add(callback)
    }
    
    private fun notifyAdLoaded(adUnitId: String) {
        adLoadCallbacks[adUnitId]?.forEach { it(true) }
    }
    
    private fun notifyAdFailed(adUnitId: String, error: LoadAdError) {
        adLoadCallbacks[adUnitId]?.forEach { it(false) }
    }
    
    private fun notifyAdShowed(adUnitId: String) {
        adDisplayCallbacks[adUnitId]?.forEach { it() }
    }
    
    private fun notifyAdClicked(adUnitId: String) {
        // Handle ad click
    }
    
    private fun notifyAdImpression(adUnitId: String) {
        // Handle ad impression
    }
    
    fun destroyBannerAd(adView: AdView) {
        adView.destroy()
        if (_bannerAdView.value == adView) {
            _bannerAdView.value = null
        }
    }
    
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()
    val bannerAdView: StateFlow<AdView?> = _bannerAdView.asStateFlow()
    
    companion object {
        // Replace with your actual AdMob unit IDs
        private const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
        private const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
        private const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
        private const val REWARDED_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/5354046374"
    }
}
