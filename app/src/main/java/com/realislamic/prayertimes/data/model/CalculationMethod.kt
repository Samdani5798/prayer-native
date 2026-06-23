package com.realislamic.prayertimes.data.model

enum class CalculationMethod(val id: Int, val displayName: String) {
    SHIA_ITHNA_ANSARI(0, "Shia Ithna-Ashari"),
    UNIVERSITY_OF_ISLAMIC_SCIENCES_KARACHI(1, "University of Islamic Sciences, Karachi"),
    ISLAMIC_SOCIETY_OF_NORTH_AMERICA(2, "Islamic Society of North America (ISNA)"),
    MUSLIM_WORLD_LEAGUE(3, "Muslim World League"),
    UMM_AL_QURA_MAKKAH(4, "Umm Al-Qura University, Makkah"),
    EGYPTIAN_GENERAL_AUTHORITY(5, "Egyptian General Authority of Survey"),
    INSTITUTE_OF_GEOPHYSICS_TEHRAN(7, "Institute of Geophysics, University of Tehran"),
    GULF_REGION(8, "Gulf Region"),
    KUWAIT(9, "Kuwait"),
    QATAR(10, "Qatar"),
    MAJLIS_UGAMA_ISLAM_SINGAPORE(11, "Majlis Ugama Islam Singapura"),
    UNION_ORG_ISLAMIC_DE_FRANCE(12, "Union Organization islamic de France"),
    DIYANET_TURKEY(13, "Diyanet İşleri Başkanlığı, Turkey"),
    SPIRITUAL_ADMIN_RUSSIA(14, "Spiritual Administration of Muslims of Russia"),
    MOONSIGHTING_COMMITTEE(15, "Moonsighting Committee Worldwide"),
    DUBAI(16, "Dubai (experimental)"),
    JAKIM_MALAYSIA(17, "Jabatan Kemajuan Islam Malaysia (JAKIM)"),
    TUNISIA(18, "Tunisia"),
    ALGERIA(19, "Algeria"),
    KEMENAG_INDONESIA(20, "Kementerian Agama Republik Indonesia"),
    MOROCCO(21, "Morocco"),
    PORTUGAL(22, "Comunidade Islamica de Lisboa"),
    JORDAN(23, "Jordan");

    companion object {
        fun forCountryCode(countryCode: String?): CalculationMethod {
            return when (countryCode?.uppercase()) {
                "PK" -> UNIVERSITY_OF_ISLAMIC_SCIENCES_KARACHI
                "IN", "BD" -> UNIVERSITY_OF_ISLAMIC_SCIENCES_KARACHI
                "US", "CA" -> ISLAMIC_SOCIETY_OF_NORTH_AMERICA
                "SA" -> UMM_AL_QURA_MAKKAH
                "EG" -> EGYPTIAN_GENERAL_AUTHORITY
                "IR" -> INSTITUTE_OF_GEOPHYSICS_TEHRAN
                "AE", "OM", "BH" -> GULF_REGION
                "KW" -> KUWAIT
                "QA" -> QATAR
                "SG" -> MAJLIS_UGAMA_ISLAM_SINGAPORE
                "FR" -> UNION_ORG_ISLAMIC_DE_FRANCE
                "TR" -> DIYANET_TURKEY
                "RU" -> SPIRITUAL_ADMIN_RUSSIA
                "MY" -> JAKIM_MALAYSIA
                "TN" -> TUNISIA
                "DZ" -> ALGERIA
                "ID" -> KEMENAG_INDONESIA
                "MA" -> MOROCCO
                "PT" -> PORTUGAL
                "JO" -> JORDAN
                "GB", "DE", "IT", "ES", "AU", "NZ" -> MUSLIM_WORLD_LEAGUE
                else -> MUSLIM_WORLD_LEAGUE
            }
        }

        fun fromId(id: Int): CalculationMethod = entries.find { it.id == id } ?: MUSLIM_WORLD_LEAGUE
    }
}
