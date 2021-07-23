package info.nukoneko.cuc.android.kidspos.ui.main.itemlist

import android.view.View
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import info.nukoneko.cuc.android.kidspos.R
import info.nukoneko.cuc.android.kidspos.di.GlobalConfig
import info.nukoneko.cuc.android.kidspos.domain.entity.Item
import info.nukoneko.cuc.android.kidspos.event.BarcodeEvent
import info.nukoneko.cuc.android.kidspos.event.EventBus
import info.nukoneko.cuc.android.kidspos.event.SystemEvent
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ItemListViewModel(
    private val config: GlobalConfig,
    private val eventBus: EventBus
) : ViewModel() {

    private val currentPrice = MutableLiveData<String>()
    fun getCurrentPriceText(): LiveData<String> = currentPrice

    private val currentStaff = MutableLiveData<String>()
    fun getCurrentStaffText(): LiveData<String> = currentStaff

    private val currentStaffVisibility = MutableLiveData<Int>()
    fun getCurrentStaffVisibility(): LiveData<Int> = currentStaffVisibility

    private val accountButtonEnabled = MutableLiveData<Boolean>()
    fun getAccountButtonEnabled(): LiveData<Boolean> = accountButtonEnabled

    var listener: Listener? = null

    private val data: MutableList<Item> = mutableListOf()

    private fun currentTotal(): Int = data.sumOf { it.price.value }

    init {
        updateViews()
    }

    fun onClickClear(@Suppress("UNUSED_PARAMETER") view: View?) {
        data.clear()
        listener?.onDataClear()
        updateViews()
    }

    fun onClickAccount(@Suppress("UNUSED_PARAMETER") view: View?) {
        listener?.onStartAccount(data)
    }

    fun onResume() {
        updateViews()
    }

    fun onStart() {
        eventBus.register(this)
    }

    fun onStop() {
        eventBus.unregister(this)
    }

    private fun updateViews() {
        currentPrice.postValue("${currentTotal()} リバー")
        accountButtonEnabled.postValue(data.isNotEmpty())
        currentStaffVisibility.value =
            if (config.currentStaff == null) View.INVISIBLE else View.VISIBLE
        currentStaff.postValue("たんとう: ${config.currentStaff?.name ?: ""}")
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReadItemSuccessEvent(event: BarcodeEvent.ReadItemSuccess) {
        val item = event.item
        data.add(item)
        listener?.onDataAdded(item)
        updateViews()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReadStaffSuccessEvent(event: BarcodeEvent.ReadStaffSuccess) {
        val staff = event.staff
        config.currentStaff = staff
        updateViews()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReadItemFailedEvent(@Suppress("UNUSED_PARAMETER") event: BarcodeEvent.ReadItemFailed) {
        listener?.onShouldShowMessage(R.string.request_item_failed)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReadStaffFailedEvent(@Suppress("UNUSED_PARAMETER") event: BarcodeEvent.ReadStaffFailed) {
        listener?.onShouldShowMessage(R.string.request_staff_failed)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReadReceiptFailedEvent(@Suppress("UNUSED_PARAMETER") event: BarcodeEvent.ReadReceiptFailed) {
        listener?.onShouldShowMessage(R.string.read_receipt_failed)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSentSaleSuccessEvent(@Suppress("UNUSED_PARAMETER") event: SystemEvent.SentSaleSuccess) {
        data.clear()
        listener?.onDataClear()
        updateViews()
    }

    interface Listener {
        fun onDataClear()

        fun onDataAdded(data: Item)

        fun onStartAccount(data: List<Item>)

        fun onShouldShowMessage(message: String)

        fun onShouldShowMessage(@StringRes messageId: Int)
    }
}
