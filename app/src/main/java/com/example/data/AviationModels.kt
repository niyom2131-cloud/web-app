package com.example.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

// Main entity for the SQLite Room Database
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "aviation_records")
data class AviationRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String,          // "WO", "WS", "WB", "SHR", "CRS"
    val referenceNo: String,   // Reference / Document number, e.g., WO/ON number, WS/O number, etc.
    val title: String,         // Secondary identifier (e.g., Aircraft model or item name)
    val date: String,          // Primary log date
    val savedAt: Long = System.currentTimeMillis(),
    val jsonContent: String    // Full form fields stored securely as JSON
)

// Inner model representations for the 5 forms
data class WorkOrderData(
    val repairNo: String = "",       // เลขที่ใบส่งซ่อม
    val woOn: String = "",           // WO/ON (เลขงาน)
    val partNo: String = "",         // P/N (Part Number)
    val repairDate: String = "",     // วันที่ส่งซ่อม
    val docDate: String = "",        // ลงวันที่
    val serialNo: String = "",       // S/N (Serial Number)
    val equipmentLine: String = "",  // สายยุทโธปกรณ์ (สพ., สส., ขส., ช., พบ.)
    val itemName: String = "",       // ITEM NAME
    val tsoTsn: String = "",         // TSO / TSN
    val owner: String = "",          // OWNER / หน่วยส่งซ่อม
    val receivedDate: String = "",   // วันที่รับงานซ่อม
    val section: String = "",        // ตอนรับงานซ่อม (ตอนไฟฟ้า, ตอนซ่อมโครงสร้าง, ตอนถ่ายทอดกำลัง, ตอนเย็บหนัง, ตอนบริภัณฑ์ภาคพื้น, อื่นๆ)
    val maintenanceLocation: String = "", // MAINTENANCE LOCATION
    val receiver: String = "",       // ผู้รับงานซ่อม
    val priority: String = "",        // ลำดับความเร่งด่วน (ปกติ, ด่วน, ด่วนมาก, ด่วนที่สุด)
    val workDetails: String = "",     // รายละเอียดการซ่อมบำรุง
    val refDocs: String = ""          // เอกสารอ้างอิง
)

data class WorkSheetData(
    val wsOn: String = "",           // WS/ON
    val woOnRef: String = "",        // WO/ON Ref
    val aircraftType: String = "Bell 212", // AIRCRAFT TYPE
    val aircraftMsn: String = "",    // AIRCRAFT MSN
    val date: String = "",           // DATE
    val technicalManual: String = "" // OPERATION MAINTENANCE TECHNICAL MANUAL
)

data class WorkBriefData(
    val wbOn: String = "",           // WB/ON
    val wsOnRef: String = "",        // WS/ON Ref
    val date: String = "",           // DATE
    val workTasks: String = "",      // WORK TASKS OPERATION
    val supervisor: String = "",     // SUPERVISER / CONTROLLER
    val performer: String = ""       // PERFORMER
)

data class ShiftHandoverData(
    val shrOn: String = "",          // SHR/ON
    val wbOnRef: String = "",        // WB/ON Ref
    val date: String = "",           // DATE
    val aircraftType: String = "Bell 212", // AIRCRAFT TYPE
    val aircraftMsn: String = "",    // AIRCRAFT MSN
    val maintenanceLocation: String = "", // MAINTENANCE LOCATION
    val workPackageRef: String = "", // WORK PACKAGE REF: WORK SHEET
    val startDate: String = "",      // START DATE
    val shiftEnd: String = "",       // SHIFT END
    val taskWoNo: String = "",       // TASK WO/NO
    val lastStepCompleted: String = "", // LAST STEP COMPLETED
    val remainingCaution: String = "" // REMAINING / CAUTION
)

data class CertificateReleaseData(
    val crsOn: String = "",          // CRS/ON
    val woOnRef: String = "",        // WO/ON
    val aircraftType: String = "Bell 212", // A/C TYPE
    val owner: String = "",          // OWNER
    val maintenanceLocation: String = "", // MAINTENANCE LOCATION
    val aircraftMsn: String = "",    // A/C MSN
    val itemName: String = "",       // ITEM NAME
    val partNo: String = "",         // P/N
    val serialNo: String = "",       // S/N
    val detailedDescription: String = "", // DETAILED DESCRIPTION MAINTENANCE...
    val independentInspection: String = "", // INDEPENDENT INSPECTION...
    val deferredDefects: String = "" // DEFERRED DEFECTS / ITEMS
)

// Helper object for simple JSON conversions using Moshi
object AviationSerializer {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    fun toWo(json: String): WorkOrderData {
        return try {
            moshi.adapter(WorkOrderData::class.java).fromJson(json) ?: WorkOrderData()
        } catch (e: Exception) {
            WorkOrderData()
        }
    }

    fun fromWo(data: WorkOrderData): String {
        return moshi.adapter(WorkOrderData::class.java).toJson(data)
    }

    fun toWs(json: String): WorkSheetData {
        return try {
            moshi.adapter(WorkSheetData::class.java).fromJson(json) ?: WorkSheetData()
        } catch (e: Exception) {
            WorkSheetData()
        }
    }

    fun fromWs(data: WorkSheetData): String {
        return moshi.adapter(WorkSheetData::class.java).toJson(data)
    }

    fun toWb(json: String): WorkBriefData {
        return try {
            moshi.adapter(WorkBriefData::class.java).fromJson(json) ?: WorkBriefData()
        } catch (e: Exception) {
            WorkBriefData()
        }
    }

    fun fromWb(data: WorkBriefData): String {
        return moshi.adapter(WorkBriefData::class.java).toJson(data)
    }

    fun toShr(json: String): ShiftHandoverData {
        return try {
            moshi.adapter(ShiftHandoverData::class.java).fromJson(json) ?: ShiftHandoverData()
        } catch (e: Exception) {
            ShiftHandoverData()
        }
    }

    fun fromShr(data: ShiftHandoverData): String {
        return moshi.adapter(ShiftHandoverData::class.java).toJson(data)
    }

    fun toCrs(json: String): CertificateReleaseData {
        return try {
            moshi.adapter(CertificateReleaseData::class.java).fromJson(json) ?: CertificateReleaseData()
        } catch (e: Exception) {
            CertificateReleaseData()
        }
    }

    fun fromCrs(data: CertificateReleaseData): String {
        return moshi.adapter(CertificateReleaseData::class.java).toJson(data)
    }
}
