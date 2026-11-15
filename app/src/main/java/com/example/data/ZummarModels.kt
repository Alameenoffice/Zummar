package com.example.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class ZummarRole(
    val titleAr: String,
    val badgeAr: String,
    val descriptionAr: String
) {
    CITIZEN(
        titleAr = "المواطن / الزائر",
        badgeAr = "تصفح شامل وحجز ومراسلة",
        descriptionAr = "تصفح الخرائط الحية، معرفة الصيدليات الخافرة، حجز مواعيد الأطباء، التسوق ومراسلة التجار والصيادلة أونلاين."
    ),
    ELECTRICITY_DEPT(
        titleAr = "حساب دائرة الكهرباء",
        badgeAr = "إدارة المغذيات والأحياء",
        descriptionAr = "رسم الأحياء والمغذيات على خريطة زمار، تشغيل وإطفاء المناطق، وإعلان الأعطال وأوقات التشغيل."
    ),
    WATER_DEPT(
        titleAr = "حساب دائرة الماء",
        badgeAr = "إدارة قواطع إسالة المياه",
        descriptionAr = "رسم قواطع ضخ المياه على خريطة زمار، تشغيل وإيقاف الضخ، وإعلان الصيانة وأوقات التوزيع."
    ),
    MEDICAL_SECTOR(
        titleAr = "حساب القطاع الطبي",
        badgeAr = "صيدلية / عيادة طبيب / مختبر",
        descriptionAr = "تحديد الموقع الدقيق على خريطة زمار، تفعيل شارة صيدلية خافرة، إدارة حجوزات المرضى، والرد على رسائل المواطنين."
    ),
    MERCHANT(
        titleAr = "حساب التاجر وصاحب المحل",
        badgeAr = "إدارة المتجر والبضائع",
        descriptionAr = "تحديد موقع المحل على الخريطة، رفع المنتجات والأسعار والعروض في متجره الخاص، والرد على طلبات الزبائن."
    ),
    SUPER_ADMIN(
        titleAr = "الإدارة العليا لبوابة زمار",
        badgeAr = "المدير العام والمصادقة",
        descriptionAr = "الموافقة على طلبات تسجيل الصيدليات والعيادات والمحلات قبل ظهورها للعامة، تفعيل حسابات الدوائر الخدمية، والإشراف الكامل."
    )
}

enum class PortalDestination(val titleAr: String, val subtitleAr: String) {
    WELCOME("بوابة الترحيب", "اختيار الوجهة الخدمية"),
    ELECTRICITY("شبكة الكهرباء الوطنية والمولدات", "خريطة المغذيات الحية، حالات التشغيل والأعطال"),
    WATER("شبكة إسالة وضخ المياه", "خريطة قواطع الضخ، الجداول الزمنية والصيانة"),
    MEDICAL("الصيدليات الخافرة والعيادات والمختبرات", "رادار الصيدليات الخافرة، حجز الأطباء والمراسلة"),
    MARKET("سوق زمار والمحلات التجارية", "المتاجر التفاعلية، البضائع والأسعار بالدينار"),
    MUNICIPALITY("خدمات البلدية والمجتمع (قريباً)", "المشاريع الخدمية، النظافة والمبادرات المجتمعية"),
    ADMIN_PANEL("لوحة الإدارة العليا والمصادقات", "اعتماد الحسابات الطبية والتجارية وإدارة البوابة")
}

data class NormalizedPoint(
    val x: Float = 0.5f,
    val y: Float = 0.5f
)

@IgnoreExtraProperties
data class ZoneDocument(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val powerStatus: String = "NATIONAL", // NATIONAL, GENERATOR, OFF, FAULT
    val powerStartTime: String = "08:00 صباحاً",
    val powerOffDuration: String = "3 ساعات",
    val powerFaultReason: String = "لا يوجد خلل",
    val powerExpectedFix: String = "التيار مستقر",
    val waterStatus: String = "PUMPING", // PUMPING, STOPPED, MAINTENANCE
    val waterOpenTime: String = "06:00 صباحاً",
    val waterDuration: String = "6 ساعات",
    val waterFaultNote: String = "الضخ منتظم",
    val waterExpectedReturn: String = "مستمر",
    val coordsText: String = "0.25,0.25;0.45,0.25;0.45,0.45;0.25,0.45",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun parsePoints(): List<NormalizedPoint> {
        return coordsText.split(";")
            .mapNotNull { token ->
                val parts = token.split(",")
                if (parts.size == 2) {
                    val px = parts[0].trim().toFloatOrNull()
                    val py = parts[1].trim().toFloatOrNull()
                    if (px != null && py != null) {
                        NormalizedPoint(px.coerceIn(0f, 1f), py.coerceIn(0f, 1f))
                    } else null
                } else null
            }
            .ifEmpty {
                listOf(
                    NormalizedPoint(0.25f, 0.25f),
                    NormalizedPoint(0.45f, 0.25f),
                    NormalizedPoint(0.45f, 0.45f),
                    NormalizedPoint(0.25f, 0.45f)
                )
            }
    }

    fun centerPoint(): NormalizedPoint {
        val pts = parsePoints()
        val avgX = pts.map { it.x }.average().toFloat()
        val avgY = pts.map { it.y }.average().toFloat()
        return NormalizedPoint(avgX, avgY)
    }
}

@IgnoreExtraProperties
data class FacilityDocument(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val facilityType: String = "PHARMACY", // PHARMACY, CLINIC, LAB
    val specialty: String = "",
    val address: String = "",
    val workHours: String = "",
    val isOnDuty: Boolean = false,
    val approvalStatus: String = "APPROVED", // APPROVED, PENDING
    val mapX: Double = 0.5,
    val mapY: Double = 0.5,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class AppointmentDocument(
    @DocumentId val id: String = "",
    val userId: String = "",
    val facilityId: String = "",
    val facilityName: String = "",
    val doctorSpecialty: String = "",
    val patientName: String = "",
    val patientPhone: String = "",
    val appointmentDate: String = "",
    val serialNumber: Long = 1L,
    val status: String = "CONFIRMED", // CONFIRMED, COMPLETED, CANCELLED
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class StoreDocument(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val category: String = "",
    val description: String = "",
    val address: String = "",
    val approvalStatus: String = "APPROVED", // APPROVED, PENDING
    val mapX: Double = 0.5,
    val mapY: Double = 0.5,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ProductDocument(
    @DocumentId val id: String = "",
    val userId: String = "",
    val storeId: String = "",
    val storeName: String = "",
    val name: String = "",
    val category: String = "",
    val priceIqd: Long = 1000L,
    val description: String = "",
    val inStock: Boolean = true,
    val visualStyle: String = "GOLD_BOX",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class MessageDocument(
    @DocumentId val id: String = "",
    val userId: String = "",
    val targetId: String = "",
    val targetName: String = "",
    val targetType: String = "PHARMACY", // PHARMACY, CLINIC, LAB, STORE
    val senderRole: String = "",
    val senderName: String = "",
    val content: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
