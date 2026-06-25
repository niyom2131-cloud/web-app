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
    HISTORY,
    SETTINGS
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

    // Persistence using SharedPreferences
    private val prefs = application.getSharedPreferences("aviation_settings", android.content.Context.MODE_PRIVATE)

    // Persistent Setting Flow variables
    private val _defaultInspectorName = MutableStateFlow(prefs.getString("default_inspector", "") ?: "")
    val defaultInspectorName: StateFlow<String> = _defaultInspectorName.asStateFlow()

    private val _defaultRank = MutableStateFlow(prefs.getString("default_rank", "จ.ส.อ.") ?: "จ.ส.อ.")
    val defaultRank: StateFlow<String> = _defaultRank.asStateFlow()

    private val _defaultUnit = MutableStateFlow(prefs.getString("default_unit", "ร้อย.ซบร.บ.ทบ.สท.") ?: "ร้อย.ซบร.บ.ทบ.สท.")
    val defaultUnit: StateFlow<String> = _defaultUnit.asStateFlow()

    private val _defaultSection = MutableStateFlow(prefs.getString("default_section", "ตอนซ่อมโครงสร้าง") ?: "ตอนซ่อมโครงสร้าง")
    val defaultSection: StateFlow<String> = _defaultSection.asStateFlow()

    private val _autoSaveDraft = MutableStateFlow(prefs.getBoolean("auto_save_draft", true))
    val autoSaveDraft: StateFlow<Boolean> = _autoSaveDraft.asStateFlow()

    private val _enableSoundEffects = MutableStateFlow(prefs.getBoolean("enable_sound", true))
    val enableSoundEffects: StateFlow<Boolean> = _enableSoundEffects.asStateFlow()

    private val _defaultAircraftType = MutableStateFlow(prefs.getString("default_aircraft_type", "Bell 212") ?: "Bell 212")
    val defaultAircraftType: StateFlow<String> = _defaultAircraftType.asStateFlow()

    private val _defaultLocation = MutableStateFlow(prefs.getString("default_location", "โรงเก็บที่ 1") ?: "โรงเก็บที่ 1")
    val defaultLocation: StateFlow<String> = _defaultLocation.asStateFlow()

    private val _appThemeColor = MutableStateFlow(prefs.getString("app_theme_color", "Default Slate") ?: "Default Slate")
    val appThemeColor: StateFlow<String> = _appThemeColor.asStateFlow()

    // Setting update functions
    fun updateDefaultInspectorName(name: String) {
        _defaultInspectorName.value = name
        prefs.edit().putString("default_inspector", name).apply()
    }

    fun updateDefaultRank(rank: String) {
        _defaultRank.value = rank
        prefs.edit().putString("default_rank", rank).apply()
    }

    fun updateDefaultUnit(unit: String) {
        _defaultUnit.value = unit
        prefs.edit().putString("default_unit", unit).apply()
    }

    fun updateDefaultSection(section: String) {
        _defaultSection.value = section
        prefs.edit().putString("default_section", section).apply()
    }

    fun updateAutoSaveDraft(enabled: Boolean) {
        _autoSaveDraft.value = enabled
        prefs.edit().putBoolean("auto_save_draft", enabled).apply()
    }

    fun updateEnableSoundEffects(enabled: Boolean) {
        _enableSoundEffects.value = enabled
        prefs.edit().putBoolean("enable_sound", enabled).apply()
    }

    fun updateDefaultAircraftType(type: String) {
        _defaultAircraftType.value = type
        prefs.edit().putString("default_aircraft_type", type).apply()
    }

    fun updateDefaultLocation(location: String) {
        _defaultLocation.value = location
        prefs.edit().putString("default_location", location).apply()
    }

    fun updateAppThemeColor(color: String) {
        _appThemeColor.value = color
        prefs.edit().putString("app_theme_color", color).apply()
    }

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

    fun insertRecords(records: List<AviationRecord>) {
        viewModelScope.launch {
            records.forEach { repository.insertRecord(it) }
        }
    }
}
