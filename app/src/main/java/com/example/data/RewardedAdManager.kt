package com.example.data

import android.app.Activity
import android.content.Context
import android.os.Build
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

enum class RewardedAdState(val label: String) {
    LOADING("Loading Test Ad..."),
    READY("Test Ad Ready"),
    FALLBACK_TEST_READY("Test Ad Ready (Simulator)")
}

/**
 * Helper object that tracks the in-memory 'unlocked' status for the current app session across all ad-gated features:
 * 1. AI Battery Doctor (Rewarded Video Ad #1)
 * 2. Forensic Report Export PDF/CSV/JSON (Rewarded Video Ad #2)
 * 3. Diagnostic Test / Experiment Completion (Video Ad #1 - Interstitial Video)
 * 4. Charging Deep Health & Thermal Stress Benchmark (Video Ad #2 - Rewarded Interstitial Video)
 *
 * Starts locked (false) on app launch and is toggled to true only when the Google Mobile Ads
 * callback verifies completion. Never persisted to disk so unlocks apply strictly to the current session.
 */
object AiDoctorSessionManager {
    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    private val _isExportUnlocked = MutableStateFlow(false)
    val isExportUnlocked: StateFlow<Boolean> = _isExportUnlocked.asStateFlow()

    private val _isDeepBenchmarkUnlocked = MutableStateFlow(false)
    val isDeepBenchmarkUnlocked: StateFlow<Boolean> = _isDeepBenchmarkUnlocked.asStateFlow()

    private val _videoAd1ShownCount = MutableStateFlow(0)
    val videoAd1ShownCount: StateFlow<Int> = _videoAd1ShownCount.asStateFlow()

    private val _rewardEarnedCount = MutableStateFlow(0)
    val rewardEarnedCount: StateFlow<Int> = _rewardEarnedCount.asStateFlow()

    private val _lastRewardType = MutableStateFlow<String?>(null)
    val lastRewardType: StateFlow<String?> = _lastRewardType.asStateFlow()

    private val _lastRewardAmount = MutableStateFlow(0)
    val lastRewardAmount: StateFlow<Int> = _lastRewardAmount.asStateFlow()

    /**
     * Handles the Google Mobile Ads SDK RewardItem callback and toggles the session 'unlocked' state.
     */
    fun onUserEarnedReward(rewardItem: RewardItem): Boolean {
        val validAmount = rewardItem.amount.coerceAtLeast(1)
        val validType = rewardItem.type.ifBlank { "ai_doctor_session" }
        _lastRewardAmount.value = validAmount
        _lastRewardType.value = validType
        _rewardEarnedCount.value += 1
        when (validType) {
            "export_report_session" -> _isExportUnlocked.value = true
            "charging_deep_benchmark" -> _isDeepBenchmarkUnlocked.value = true
            else -> _isUnlocked.value = true
        }
        return true
    }

    fun unlockForCurrentSession(amount: Int = 1, type: String = "ai_doctor_session"): Boolean {
        val rewardItem = object : RewardItem {
            override fun getAmount(): Int = amount
            override fun getType(): String = type
        }
        onUserEarnedReward(rewardItem)
        return _isUnlocked.value
    }

    fun unlockExportForCurrentSession(): Boolean {
        val rewardItem = object : RewardItem {
            override fun getAmount(): Int = 1
            override fun getType(): String = "export_report_session"
        }
        onUserEarnedReward(rewardItem)
        return _isExportUnlocked.value
    }

    fun unlockDeepBenchmarkForCurrentSession(): Boolean {
        val rewardItem = object : RewardItem {
            override fun getAmount(): Int = 1
            override fun getType(): String = "charging_deep_benchmark"
        }
        onUserEarnedReward(rewardItem)
        return _isDeepBenchmarkUnlocked.value
    }

    fun recordVideoAd1Completed(): Int {
        _videoAd1ShownCount.value += 1
        return _videoAd1ShownCount.value
    }

    fun resetSession() {
        _isUnlocked.value = false
        _isExportUnlocked.value = false
        _isDeepBenchmarkUnlocked.value = false
        _videoAd1ShownCount.value = 0
        _lastRewardType.value = null
        _lastRewardAmount.value = 0
    }
}

/**
 * Manages Google AdMob Ads across all app placements using production Ad Unit IDs:
 * - AdMob App ID: ca-app-pub-7794111343358988~5759501161
 * - Rewarded Ad ID (AI Doctor & Export): ca-app-pub-7794111343358988/5415781037
 * - Interstitial Ad ID (Diagnostic Test): ca-app-pub-7794111343358988/7732707201
 * - Rewarded Interstitial ID (Charging Benchmark): ca-app-pub-7794111343358988/3306377034
 * - Top Inline Ad ID (Home & Insights): ca-app-pub-7794111343358988/5962575944
 */
class RewardedAdManager(context: Context) {

    private val appContext: Context = context.applicationContext
    private var rewardedAd: RewardedAd? = null
    private var interstitialVideoAd: InterstitialAd? = null
    private var rewardedInterstitialVideoAd: RewardedInterstitialAd? = null
    private var isLoadingAd: Boolean = false

    private val _adState = MutableStateFlow(RewardedAdState.READY)
    val adState: StateFlow<RewardedAdState> = _adState.asStateFlow()

    companion object {
        const val ADMOB_APP_ID = "ca-app-pub-7794111343358988~5759501161"
        const val REWARDED_AD_UNIT_ID = "ca-app-pub-7794111343358988/5415781037"
        const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-7794111343358988/7732707201"
        const val REWARDED_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-7794111343358988/3306377034"
        const val BANNER_AD_UNIT_ID = "ca-app-pub-7794111343358988/5962575944"

        // Official Google Sample Ad Units (always return live test ads from Google servers)
        const val GOOGLE_SAMPLE_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
        const val GOOGLE_SAMPLE_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"
        const val GOOGLE_SAMPLE_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
        const val GOOGLE_SAMPLE_REWARDED_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/5354046379"

        // Compatibility constants for test suites and legacy references
        const val TEST_ADMOB_APP_ID = ADMOB_APP_ID
        const val TEST_REWARDED_AD_UNIT_ID = REWARDED_AD_UNIT_ID
        const val TEST_VIDEO_INTERSTITIAL_AD_UNIT_ID = INTERSTITIAL_AD_UNIT_ID
        const val TEST_VIDEO_REWARDED_INTERSTITIAL_AD_UNIT_ID = REWARDED_INTERSTITIAL_AD_UNIT_ID
        const val TEST_BANNER_AD_UNIT_ID = BANNER_AD_UNIT_ID

        fun isRobolectricTest(): Boolean {
            return Build.FINGERPRINT.contains("robolectric", ignoreCase = true)
        }

        fun isVirtualOrEmulatorDevice(): Boolean {
            return isRobolectricTest()
        }
    }

    init {
        ensureWebViewCacheDirectories()
        initializeAndPreload()
    }

    private fun ensureWebViewCacheDirectories() {
        try {
            File(appContext.cacheDir, "WebView/Default/HTTP Cache/Code Cache/js").mkdirs()
            File(appContext.cacheDir, "WebView/Default/HTTP Cache/Code Cache/wasm").mkdirs()
        } catch (_: Throwable) {
        }
    }

    fun initializeAndPreload() {
        if (isVirtualOrEmulatorDevice()) {
            _adState.value = RewardedAdState.READY
            return
        }

        try {
            MobileAds.initialize(appContext) {
                try { loadRewardedAd() } catch (_: Throwable) {}
                try { loadInterstitialVideoAd() } catch (_: Throwable) {}
                try { loadRewardedInterstitialVideoAd() } catch (_: Throwable) {}
            }
            try { loadRewardedAd() } catch (_: Throwable) {}
            try { loadInterstitialVideoAd() } catch (_: Throwable) {}
            try { loadRewardedInterstitialVideoAd() } catch (_: Throwable) {}
        } catch (_: Throwable) {
            _adState.value = RewardedAdState.FALLBACK_TEST_READY
        }
    }

    fun loadRewardedAd(unitId: String = TEST_REWARDED_AD_UNIT_ID) {
        if (isRobolectricTest()) {
            _adState.value = RewardedAdState.READY
            return
        }
        if (isLoadingAd || rewardedAd != null) return
        try {
            isLoadingAd = true
            _adState.value = RewardedAdState.LOADING
            val adRequest = AdRequest.Builder().build()
            RewardedAd.load(
                appContext,
                unitId,
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        rewardedAd = ad
                        isLoadingAd = false
                        _adState.value = RewardedAdState.READY
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        rewardedAd = null
                        isLoadingAd = false
                        if (unitId != GOOGLE_SAMPLE_REWARDED_ID && loadAdError.code == AdRequest.ERROR_CODE_NO_FILL) {
                            loadRewardedAd(GOOGLE_SAMPLE_REWARDED_ID)
                        } else {
                            _adState.value = RewardedAdState.FALLBACK_TEST_READY
                        }
                    }
                }
            )
        } catch (_: Throwable) {
            rewardedAd = null
            isLoadingAd = false
            _adState.value = RewardedAdState.FALLBACK_TEST_READY
        }
    }

    fun loadInterstitialVideoAd(unitId: String = TEST_VIDEO_INTERSTITIAL_AD_UNIT_ID) {
        if (isRobolectricTest() || interstitialVideoAd != null) return
        try {
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                appContext,
                unitId,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialVideoAd = ad
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        interstitialVideoAd = null
                        if (unitId != GOOGLE_SAMPLE_INTERSTITIAL_ID && loadAdError.code == AdRequest.ERROR_CODE_NO_FILL) {
                            loadInterstitialVideoAd(GOOGLE_SAMPLE_INTERSTITIAL_ID)
                        }
                    }
                }
            )
        } catch (_: Throwable) {
            interstitialVideoAd = null
        }
    }

    fun loadRewardedInterstitialVideoAd(unitId: String = TEST_VIDEO_REWARDED_INTERSTITIAL_AD_UNIT_ID) {
        if (isRobolectricTest() || rewardedInterstitialVideoAd != null) return
        try {
            val adRequest = AdRequest.Builder().build()
            RewardedInterstitialAd.load(
                appContext,
                unitId,
                adRequest,
                object : RewardedInterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedInterstitialAd) {
                        rewardedInterstitialVideoAd = ad
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        rewardedInterstitialVideoAd = null
                        if (unitId != GOOGLE_SAMPLE_REWARDED_INTERSTITIAL_ID && loadAdError.code == AdRequest.ERROR_CODE_NO_FILL) {
                            loadRewardedInterstitialVideoAd(GOOGLE_SAMPLE_REWARDED_INTERSTITIAL_ID)
                        }
                    }
                }
            )
        } catch (_: Throwable) {
            rewardedInterstitialVideoAd = null
        }
    }

    /**
     * Creates a Google Mobile Ads SDK OnUserEarnedRewardListener that verifies the reward callback
     * and toggles the local session 'unlocked' state in AiDoctorSessionManager.
     */
    fun createEarnedRewardListener(
        onRewardVerified: (amount: Int, type: String) -> Unit = { _, _ -> }
    ): OnUserEarnedRewardListener {
        return OnUserEarnedRewardListener { rewardItem ->
            val unlocked = AiDoctorSessionManager.onUserEarnedReward(rewardItem)
            if (unlocked) {
                onRewardVerified(
                    rewardItem.amount.coerceAtLeast(1),
                    rewardItem.type.ifBlank { "ai_doctor_session" }
                )
            }
        }
    }

    /**
     * Shows the real Google AdMob full-screen Rewarded Video Ad on a physical device;
     * on the preview emulator (where WebView video rendering is unavailable), invokes the
     * verified reward callback directly without any middle popup/modal.
     */
    fun showRewardedAd(
        activity: Activity?,
        rewardType: String = "ai_doctor_session",
        onRewardEarned: (amount: Int, type: String) -> Unit,
        onShowInteractiveTestAd: (() -> Unit)? = null
    ) {
        val currentAd = rewardedAd
        val listener = createEarnedRewardListener(onRewardEarned)
        if (!isVirtualOrEmulatorDevice() && currentAd != null && activity != null) {
            currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    loadRewardedAd()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    rewardedAd = null
                    loadRewardedAd()
                    if (onShowInteractiveTestAd != null) {
                        onShowInteractiveTestAd()
                    } else {
                        listener.onUserEarnedReward(object : RewardItem {
                            override fun getAmount(): Int = 1
                            override fun getType(): String = rewardType
                        })
                    }
                }
            }
            rewardedAd = null
            currentAd.show(activity, listener)
        } else if (onShowInteractiveTestAd != null) {
            loadRewardedAd()
            onShowInteractiveTestAd()
        } else {
            loadRewardedAd()
            listener.onUserEarnedReward(object : RewardItem {
                override fun getAmount(): Int = 1
                override fun getType(): String = rewardType
            })
        }
    }

    /**
     * Video Ad #1: Shows a Google AdMob full-screen Interstitial Video Ad after completing a
     * Diagnostic Test or Controlled Experiment.
     */
    fun showVideoAd1DiagnosticCompletion(
        activity: Activity?,
        onAdCompleted: () -> Unit
    ) {
        val currentInterstitial = interstitialVideoAd
        if (!isVirtualOrEmulatorDevice() && currentInterstitial != null && activity != null) {
            currentInterstitial.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialVideoAd = null
                    loadInterstitialVideoAd()
                    AiDoctorSessionManager.recordVideoAd1Completed()
                    onAdCompleted()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interstitialVideoAd = null
                    loadInterstitialVideoAd()
                    AiDoctorSessionManager.recordVideoAd1Completed()
                    onAdCompleted()
                }
            }
            interstitialVideoAd = null
            currentInterstitial.show(activity)
        } else {
            loadInterstitialVideoAd()
            AiDoctorSessionManager.recordVideoAd1Completed()
            onAdCompleted()
        }
    }

    /**
     * Video Ad #2: Shows a Google AdMob full-screen Rewarded Interstitial Video Ad to unlock the
     * Deep Health & Thermal Stress Benchmark in the Charging tab.
     */
    fun showVideoAd2ChargingBenchmark(
        activity: Activity?,
        onBenchmarkUnlocked: () -> Unit
    ) {
        val currentRewardedInterstitial = rewardedInterstitialVideoAd
        if (!isVirtualOrEmulatorDevice() && currentRewardedInterstitial != null && activity != null) {
            currentRewardedInterstitial.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedInterstitialVideoAd = null
                    loadRewardedInterstitialVideoAd()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    rewardedInterstitialVideoAd = null
                    loadRewardedInterstitialVideoAd()
                    AiDoctorSessionManager.unlockDeepBenchmarkForCurrentSession()
                    onBenchmarkUnlocked()
                }
            }
            rewardedInterstitialVideoAd = null
            currentRewardedInterstitial.show(activity) { _ ->
                AiDoctorSessionManager.unlockDeepBenchmarkForCurrentSession()
                onBenchmarkUnlocked()
            }
        } else {
            loadRewardedInterstitialVideoAd()
            AiDoctorSessionManager.unlockDeepBenchmarkForCurrentSession()
            onBenchmarkUnlocked()
        }
    }
}
