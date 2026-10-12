package com.timeskip.smartsubscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timeskip.smartsubscription.data.BillingPeriod
import com.timeskip.smartsubscription.data.Cadence
import com.timeskip.smartsubscription.data.Category
import com.timeskip.smartsubscription.data.DemoData
import com.timeskip.smartsubscription.data.ExchangeRates
import com.timeskip.smartsubscription.data.Ledger
import com.timeskip.smartsubscription.data.PurchaseState
import com.timeskip.smartsubscription.data.Status
import com.timeskip.smartsubscription.data.Subscription
import com.timeskip.smartsubscription.data.ThemeMode
import com.timeskip.smartsubscription.data.money
import com.timeskip.smartsubscription.scan.ScanResult
import com.timeskip.smartsubscription.i18n.messagesText
import com.timeskip.smartsubscription.i18n.settingsText
import com.timeskip.smartsubscription.data.shortText
import com.timeskip.smartsubscription.data.step
import com.timeskip.smartsubscription.data.fetchExchangeRates
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

const val UNLOCK_PRICE = 2.99

data class ArchiveUndo(val message: String, val before: Subscription)

data class AppState(
    val today: LocalDate = LocalDate.now(),
    val subscriptions: List<Subscription> = emptyList(),
    val categories: List<Category> = DemoData.defaultCategories,
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val currency: String = "USD",
    val remindersOn: Boolean = true,
    val leadDays: Int = 3,
    val reminderTime: LocalTime = LocalTime.of(9, 0),
    val purchase: PurchaseState = PurchaseState.OFFER,
    val archiveUndo: ArchiveUndo? = null,
    /** The last scan, waiting for the review form. */
    val scanDraft: ScanResult? = null,
    /** Null until fetched, and while offline; the totals then show one sum per currency. */
    val rates: ExchangeRates? = null,
) {
    val ledger: Ledger get() = Ledger(subscriptions, today, currency, rates)
    val unlocked: Boolean get() = purchase == PurchaseState.OWNED

    fun subscription(id: String?) = subscriptions.firstOrNull { it.id == id }
    fun category(id: String?) = categories.firstOrNull { it.id == id }
}

/** What the Add/Edit form hands back. [editingId] is null for a new subscription. */
data class Draft(
    val editingId: String?,
    val name: String,
    val provider: String,
    val categoryId: String?,
    val amount: Double,
    val currency: String,
    val cadence: Cadence,
    val nextBilling: LocalDate,
    val notes: String,
)

class AppViewModel : ViewModel() {
    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages = _messages.receiveAsFlow()

    init {
        _state.update { it.copy(subscriptions = DemoData.subscriptions(it.today)) }
        refreshRates()
    }

    // ponytail: rates live in memory and are fetched on launch and on a currency change; cache them on disk if offline launches matter.
    private fun refreshRates() {
        val base = _state.value.currency
        viewModelScope.launch {
            val rates = fetchExchangeRates(base) ?: return@launch
            _state.update { if (it.currency == base) it.copy(rates = rates) else it }
        }
    }

    private fun say(text: String) {
        _messages.trySend(text)
    }

    private fun updateSub(id: String, transform: (Subscription) -> Subscription) = _state.update { s ->
        s.copy(subscriptions = s.subscriptions.map { if (it.id == id) transform(it) else it })
    }

    // ---------- this cycle ----------

    fun markRenewed(id: String) {
        val today = _state.value.today
        updateSub(id) { sub ->
            sub.copy(periods = sub.periods.dropLast(1) + sub.periods.last().copy(paidOn = today))
        }
        _state.value.subscription(id)?.let { say(messagesText.renewed(it.name, it.nextBilling.shortText())) }
    }

    fun undoRenewed(id: String) {
        updateSub(id) { sub -> sub.copy(periods = sub.periods.dropLast(1) + sub.periods.last().copy(paidOn = null)) }
        say(messagesText.cycleExpectedAgain)
    }

    fun setReminder(id: String, on: Boolean) {
        updateSub(id) { it.copy(reminderOn = on) }
        _state.value.subscription(id)?.let { say(if (on) messagesText.reminderOn(it.name) else messagesText.reminderOff(it.name)) }
    }

    // ---------- lifecycle ----------

    /** Pause, cancel or archive. A cancelled subscription keeps access until the date it was paid up to. */
    fun changeStatus(id: String, status: Status) {
        val before = _state.value.subscription(id) ?: return
        updateSub(id) {
            it.copy(status = status, accessUntil = if (status == Status.CANCELLED) it.nextBilling else it.accessUntil)
        }
        _state.update { it.copy(archiveUndo = ArchiveUndo(messagesText.statusChanged(before.name, status), before)) }
    }

    /** Resume or reactivate. Billing starts again from the next cycle; nothing is back-billed for the pause. */
    fun restore(id: String) {
        val before = _state.value.subscription(id) ?: return
        val today = _state.value.today
        val next = before.cadence.step(today, 1)
        updateSub(id) {
            it.copy(
                status = Status.ACTIVE,
                nextBilling = next,
                accessUntil = null,
                periods = it.periods + BillingPeriod(today, next, it.amount, null),
            )
        }
        _state.update { it.copy(archiveUndo = ArchiveUndo(messagesText.restored(before.name, before.status == Status.PAUSED), before)) }
    }

    fun undoArchive() {
        val undo = _state.value.archiveUndo ?: return
        updateSub(undo.before.id) { undo.before }
        _state.update { it.copy(archiveUndo = null) }
        say(messagesText.undone)
    }

    fun dismissUndo() = _state.update { it.copy(archiveUndo = null) }

    fun delete(id: String) {
        val sub = _state.value.subscription(id) ?: return
        _state.update { s -> s.copy(subscriptions = s.subscriptions.filter { it.id != id }, archiveUndo = null) }
        say(messagesText.deleted(sub.name))
    }

    // ---------- add and edit ----------

    fun setScanDraft(result: ScanResult?) = _state.update { it.copy(scanDraft = result) }

    fun save(draft: Draft) {
        _state.update { it.copy(scanDraft = null) }
        val today = _state.value.today
        if (draft.editingId == null) {
            val sub = Subscription(
                id = UUID.randomUUID().toString(),
                name = draft.name,
                provider = draft.provider.ifBlank { draft.name },
                amount = draft.amount,
                currency = draft.currency,
                cadence = draft.cadence,
                categoryId = draft.categoryId,
                startDate = today,
                nextBilling = draft.nextBilling,
                notes = draft.notes,
                periods = listOf(BillingPeriod(draft.cadence.step(draft.nextBilling, -1), draft.nextBilling, draft.amount, null)),
            )
            _state.update { it.copy(subscriptions = it.subscriptions + sub) }
            val home = _state.value.currency
            val converted = _state.value.rates?.takeIf { it.base == home }?.perBase?.containsKey(draft.currency) == true
            say(messagesText.saved(draft.name, draft.currency, home, converted.takeIf { draft.currency != home }))
        } else {
            updateSub(draft.editingId) { sub ->
                val current = sub.periods.lastOrNull()
                sub.copy(
                    name = draft.name,
                    provider = draft.provider.ifBlank { draft.name },
                    amount = draft.amount,
                    currency = draft.currency,
                    cadence = draft.cadence,
                    categoryId = draft.categoryId,
                    nextBilling = draft.nextBilling,
                    notes = draft.notes,
                    periods = if (current == null || current.paidOn != null) sub.periods
                    else sub.periods.dropLast(1) + current.copy(end = draft.nextBilling, amount = draft.amount),
                )
            }
            say(messagesText.updated(draft.name))
        }
    }

    // ---------- categories ----------

    fun saveCategory(originalId: String?, name: String, icon: String) {
        if (originalId == null) {
            _state.update { it.copy(categories = it.categories + Category(UUID.randomUUID().toString(), name, icon)) }
            say(messagesText.categoryAdded(name))
        } else {
            val old = _state.value.category(originalId) ?: return
            _state.update { s ->
                s.copy(categories = s.categories.map { if (it.id == originalId) it.copy(name = name, icon = icon) else it })
            }
            say(if (old.name == name) messagesText.updated(name) else messagesText.categoryRenamed(old.name, name))
        }
    }

    fun deleteCategory(id: String) {
        val cat = _state.value.category(id) ?: return
        val inUse = _state.value.subscriptions.count { it.categoryId == id && it.status == Status.ACTIVE }
        _state.update { s ->
            s.copy(
                categories = s.categories.filter { it.id != id },
                subscriptions = s.subscriptions.map { if (it.categoryId == id) it.copy(categoryId = null) else it },
            )
        }
        say(messagesText.categoryDeleted(cat.name, inUse))
    }

    fun moveCategory(id: String, by: Int) = _state.update { s ->
        val list = s.categories.toMutableList()
        val i = list.indexOfFirst { it.id == id }
        val j = i + by
        if (i < 0 || j !in list.indices) return@update s
        list[i] = list[j].also { list[j] = list[i] }
        s.copy(categories = list)
    }

    // ---------- settings ----------

    fun setTheme(mode: ThemeMode) = _state.update { it.copy(theme = mode) }

    fun setCurrency(code: String) {
        _state.update { it.copy(currency = code) }
        refreshRates()
        say(messagesText.totalsIn(code))
    }

    fun setReminders(on: Boolean) = _state.update { it.copy(remindersOn = on) }

    fun setLeadDays(days: Int) {
        _state.update { it.copy(leadDays = days) }
        say(messagesText.leadChanged(days))
    }

    fun setReminderTime(time: LocalTime) = _state.update { it.copy(reminderTime = time) }

    fun toast(text: String) = say(text)

    // ---------- unlock ----------

    /** Stand-in for Google Play Billing: the flow and its states are real, the purchase is not. */
    fun purchase() {
        _state.update { it.copy(purchase = PurchaseState.BUYING) }
        viewModelScope.launch {
            delay(1600)
            if (_state.value.purchase == PurchaseState.BUYING) {
                _state.update { it.copy(purchase = PurchaseState.OWNED) }
                say(messagesText.unlocked)
            }
        }
    }

    fun restorePurchase() = say(messagesText.noPastPurchases)

    /** Debug builds only: set the purchase state directly, without going through the store. */
    fun devSetPurchase(purchase: PurchaseState) = _state.update { it.copy(purchase = purchase) }

    companion object {
        val leadChoices = listOf(1, 2, 3, 5, 7)
        fun leadLabel(days: Int) = settingsText.leadLabel(days)
        fun lifetimeLine(sub: Subscription): String {
            val n = sub.paidPeriods.size
            return if (n == 0) messagesText.noPayments else messagesText.lifetime(n, money(sub.amount, sub.currency))
        }
    }
}
