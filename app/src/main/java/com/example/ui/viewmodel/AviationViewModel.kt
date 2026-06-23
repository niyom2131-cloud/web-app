package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppScreen {
    DASHBOARD,
    FORMS,
    HISTORY
}

enum class FormType(val value: String, val code: String) {
    WO("1. Work Order (WO)", "WO"),
    WS("2. Work Sheet (WS)", "WS"),
    WB("3. Work Brief (WB)", "WB"),
    SHR("4. Shift Handover Record (SHR)", "SHR"),
    CRS("5. Certificate of Release to Service (CRS)", "CRS")
}

class AviationViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AviationDatabase.getDatabase(application)
    private val repository = AviationRepository(database.dao())

    // UI general navigation states
    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _selectedFormType = MutableStateFlow(FormType.WO)
    val selectedFormType: StateFlow<FormType> = _selectedFormType.asStateFlow()

    // Indev form states (to preserve draft content when switching tabs)
    private val _woDraft = MutableStateFlow(WorkOrderData())
    val woDraft: StateFlow<WorkOrderData> = _woDraft.asStateFlow()

    private val _wsDraft = MutableStateFlow(WorkSheetData())
    val wsDraft: StateFlow<WorkSheetData> = _wsDraft.asStateFlow()

    private val _wbDraft = MutableStateFlow(WorkBriefData())
    val wbDraft: StateFlow<WorkBriefData> = _wbDraft.asStateFlow()

    private val _shrDraft = MutableStateFlow(ShiftHandoverData())
    val shrDraft: StateFlow<ShiftHandoverData> = _shrDraft.asStateFlow()

    private val _crsDraft = MutableStateFlow(CertificateReleaseData())
    val crsDraft: StateFlow<CertificateReleaseData> = _crsDraft.asStateFlow()

    // Log Query filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _categoryFilter = MutableStateFlow<String?>(null) // null = Show All
    val categoryFilter: StateFlow<String?> = _categoryFilter.asStateFlow()

    // Observable database lists
    val allSavedRecords: StateFlow<List<AviationRecord>> = repository.allRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered lists logic
    val filteredRecords: Flow<List<AviationRecord>> = combine(
        allSavedRecords,
        _searchQuery,
        _categoryFilter
    ) { records, query, filter ->
        records.filter { record ->
            // Filter by type
            val matchesFilter = if (filter != null) record.type == filter else true

            // Filter by query (Reference Number, Title, or Date)
            val matchesQuery = if (query.isNotBlank()) {
                record.referenceNo.contains(query, ignoreCase = true) ||
                        record.title.contains(query, ignoreCase = true) ||
                        record.date.contains(query, ignoreCase = true)
            } else {
                true
            }

            matchesFilter && matchesQuery
        }
    }

    // Modal / Detailed View
    private val _selectedViewRecord = MutableStateFlow<AviationRecord?>(null)
    val selectedViewRecord: StateFlow<AviationRecord?> = _selectedViewRecord.asStateFlow()

    // Save Status Feedback Flow
    private val _saveStatusMessage = MutableSharedFlow<String>()
    val saveStatusMessage: SharedFlow<String> = _saveStatusMessage.asSharedFlow()

    // Navigation setters
    fun setScreen(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun setFormType(type: FormType) {
        _selectedFormType.value = type
        _currentScreen.value = AppScreen.FORMS
    }

    // Draft updates
    fun updateWo(updater: (WorkOrderData) -> WorkOrderData) {
        _woDraft.value = updater(_woDraft.value)
    }

    fun updateWs(updater: (WorkSheetData) -> WorkSheetData) {
        _wsDraft.value = updater(_wsDraft.value)
    }

    fun updateWb(updater: (WorkBriefData) -> WorkBriefData) {
        _wbDraft.value = updater(_wbDraft.value)
    }

    fun updateShr(updater: (ShiftHandoverData) -> ShiftHandoverData) {
        _shrDraft.value = updater(_shrDraft.value)
    }

    fun updateCrs(updater: (CertificateReleaseData) -> CertificateReleaseData) {
        _crsDraft.value = updater(_crsDraft.value)
    }

    // Query filters
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(filter: String?) {
        _categoryFilter.value = filter
    }

    // Detail view manager
    fun selectRecordToView(record: AviationRecord?) {
        _selectedViewRecord.value = record
    }

    // Clear draft form inputs after successful submission
    fun clearDraftOf(type: FormType) {
        when (type) {
            FormType.WO -> _woDraft.value = WorkOrderData()
            FormType.WS -> _wsDraft.value = WorkSheetData()
            FormType.WB -> _wbDraft.value = WorkBriefData()
            FormType.SHR -> _shrDraft.value = ShiftHandoverData()
            FormType.CRS -> _crsDraft.value = CertificateReleaseData()
        }
    }

    // Persist active form state
    fun saveActiveForm() {
        viewModelScope.launch {
            val recordToSave = when (_selectedFormType.value) {
                FormType.WO -> {
                    val data = _woDraft.value
                    if (data.woOn.isBlank()) {
                        _saveStatusMessage.emit("⚠️ กรุณาระบุรหัส WO/ON (เลขงาน)")
                        return@launch
                    }
                    AviationRecord(
                        type = "WO",
                        referenceNo = data.woOn,
                        title = data.itemName.ifBlank { "ไม่เจาะจงหลักทรัพย์" },
                        date = data.repairDate.ifBlank { data.docDate },
                        jsonContent = AviationSerializer.fromWo(data)
                    )
                }
                FormType.WS -> {
                    val data = _wsDraft.value
                    if (data.wsOn.isBlank()) {
                        _saveStatusMessage.emit("⚠️ กรุณาระบุรหัส WS/ON")
                        return@launch
                    }
                    AviationRecord(
                        type = "WS",
                        referenceNo = data.wsOn,
                        title = data.aircraftType,
                        date = data.date,
                        jsonContent = AviationSerializer.fromWs(data)
                    )
                }
                FormType.WB -> {
                    val data = _wbDraft.value
                    if (data.wbOn.isBlank()) {
                        _saveStatusMessage.emit("⚠️ กรุณาระบุรหัส WB/ON")
                        return@launch
                    }
                    AviationRecord(
                        type = "WB",
                        referenceNo = data.wbOn,
                        title = "ผู้ปฏิบัติ: " + data.performer.ifBlank { "ไม่ระบุ" },
                        date = data.date,
                        jsonContent = AviationSerializer.fromWb(data)
                    )
                }
                FormType.SHR -> {
                    val data = _shrDraft.value
                    if (data.shrOn.isBlank()) {
                        _saveStatusMessage.emit("⚠️ กรุณาระบุรหัส SHR/ON")
                        return@launch
                    }
                    AviationRecord(
                        type = "SHR",
                        referenceNo = data.shrOn,
                        title = data.aircraftType,
                        date = data.date,
                        jsonContent = AviationSerializer.fromShr(data)
                    )
                }
                FormType.CRS -> {
                    val data = _crsDraft.value
                    if (data.crsOn.isBlank()) {
                        _saveStatusMessage.emit("⚠️ กรุณาระบุรหัส CRS/ON")
                        return@launch
                    }
                    AviationRecord(
                        type = "CRS",
                        referenceNo = data.crsOn,
                        title = data.aircraftType + " (S/N: " + (data.serialNo.ifBlank { "ไม่ระบุ" }) + ")",
                        date = data.crsOn, // Use reference identifier as date string or standard date
                        jsonContent = AviationSerializer.fromCrs(data)
                    )
                }
            }

            val resultId = repository.insertRecord(recordToSave)
            if (resultId > 0) {
                _saveStatusMessage.emit("✅ บันทึกข้อมูล ${_selectedFormType.value.value} สำเร็จ!")
                clearDraftOf(_selectedFormType.value)
            } else {
                _saveStatusMessage.emit("❌ มีข้อผิดพลาดในการบันทึกข้อมูล")
            }
        }
    }

    fun deleteRecord(id: Int) {
        viewModelScope.launch {
            repository.deleteRecordById(id)
            _saveStatusMessage.emit("🗑️ ลบประวัติเรียบร้อยแล้ว")
        }
    }

    fun clearAllRecords() {
        viewModelScope.launch {
            repository.clearAllRecords()
            _saveStatusMessage.emit("🗑️ เคลียร์ประวัติทั้งหมดแล้ว")
        }
    }
}
