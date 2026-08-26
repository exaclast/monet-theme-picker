package com.exaclast.renoir.util

import android.content.Context
import android.os.Build
import com.materialkolor.PaletteStyle
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

enum class CompatibilityStatus {
    FULLY_SUPPORTED,
    PARTIAL_SUPPORT,
    INCOMPATIBLE,
    UNKNOWN
}

data class DeviceRule(
    val matchOsNames: List<String>?,
    val matchModel: String?,
    val minSdkVersion: Int?,
    val maxSdkVersion: Int?,
    val status: CompatibilityStatus,
    val unsupportedStyles: List<PaletteStyle>,
    val description: String
)

object DeviceCompatibility {
    private var rules = mutableListOf<DeviceRule>()
    private var isLoaded = false

    private fun loadRules(context: Context) {
        if (isLoaded) return

        try {
            val inputStream = context.assets.open("compatibility_manifest.json")
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonString = reader.use { it.readText() }
            val json = JSONObject(jsonString)

            if (json.has("rules")) {
                val rulesArray = json.getJSONArray("rules")
                for (i in 0 until rulesArray.length()) {
                    val obj = rulesArray.getJSONObject(i)
                    
                    val matchObj = if (obj.has("match")) obj.getJSONObject("match") else JSONObject()
                    val statusStr = if (obj.has("status")) obj.getString("status") else "UNKNOWN"
                    
                    val parsedStatus = try {
                        CompatibilityStatus.valueOf(statusStr)
                    } catch (e: Exception) {
                        CompatibilityStatus.UNKNOWN
                    }

                    val stylesArray = if (obj.has("unsupported_styles")) obj.getJSONArray("unsupported_styles") else null
                    val styles = mutableListOf<PaletteStyle>()
                    if (stylesArray != null) {
                        for (j in 0 until stylesArray.length()) {
                            val styleStr = stylesArray.getString(j)
                            val style = PaletteStyle.values().find { it.name.equals(styleStr, ignoreCase = true) }
                            if (style != null) styles.add(style)
                        }
                    }

                    rules.add(
                        DeviceRule(
                            matchOsNames = if (matchObj.has("os_name")) {
                                val osNameVal = matchObj.get("os_name")
                                if (osNameVal is org.json.JSONArray) {
                                    (0 until osNameVal.length()).map { osNameVal.getString(it) }
                                } else {
                                    listOf(osNameVal.toString())
                                }
                            } else null,
                            matchModel = if (matchObj.has("model")) matchObj.getString("model") else null,
                            minSdkVersion = if (matchObj.has("min_sdk_version")) matchObj.getInt("min_sdk_version") else null,
                            maxSdkVersion = if (matchObj.has("max_sdk_version")) matchObj.getInt("max_sdk_version") else null,
                            status = parsedStatus,
                            unsupportedStyles = styles,
                            description = if (obj.has("description")) obj.getString("description") else ""
                        )
                    )
                }
            }
            isLoaded = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun matchRule(
        osNamesRule: List<String>?,
        modelRule: String?,
        minSdkRule: Int?,
        maxSdkRule: Int?,
        currentOsName: String,
        currentModel: String,
        currentSdk: Int
    ): Boolean {
        if (osNamesRule != null && osNamesRule.none { currentOsName.contains(it, ignoreCase = true) }) return false
        if (modelRule != null && !currentModel.contains(modelRule, ignoreCase = true)) return false
        if (minSdkRule != null && currentSdk < minSdkRule) return false
        if (maxSdkRule != null && currentSdk > maxSdkRule) return false
        return true
    }

    private fun getSystemProperty(key: String): String {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("getprop", key))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readLine()
            reader.close()
            process.destroy()
            output ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    fun getOsName(context: Context): String {
        val pm = context.packageManager
        val graphenePackages = listOf("app.grapheneos.apps", "app.grapheneos.setupwizard")
        for (pkg in graphenePackages) {
            try {
                pm.getPackageInfo(pkg, 0)
                return "GrapheneOS"
            } catch (e: Exception) {}
        }
        
        val lineageProp = getSystemProperty("ro.lineage.build.version")
        if (lineageProp.isNotEmpty()) return "LineageOS"
        
        val calyxProp = getSystemProperty("ro.calyx.version")
        if (calyxProp.isNotEmpty()) return "CalyxOS"
        
        val manufacturer = Build.MANUFACTURER.lowercase()
        return when {
            manufacturer.contains("samsung") -> "OneUI"
            manufacturer.contains("google") -> "Pixel UI"
            manufacturer.contains("motorola") -> "MyUX"
            manufacturer.contains("xiaomi") -> "HyperOS"
            manufacturer.contains("oneplus") -> "OxygenOS"
            else -> Build.MANUFACTURER
        }
    }

    fun getDeviceRule(context: Context): DeviceRule? {
        loadRules(context)
        val currentOsName = getOsName(context)
        val currentModel = Build.MODEL ?: ""
        val currentSdk = Build.VERSION.SDK_INT

        return rules.find { rule ->
            matchRule(rule.matchOsNames, rule.matchModel, rule.minSdkVersion, rule.maxSdkVersion, currentOsName, currentModel, currentSdk)
        }
    }
    
    fun getStatus(context: Context): CompatibilityStatus {
        return getDeviceRule(context)?.status ?: CompatibilityStatus.UNKNOWN
    }

    fun getUnsupportedStyles(context: Context): List<PaletteStyle> {
        return getDeviceRule(context)?.unsupportedStyles ?: emptyList()
    }
    
    fun getCompatibilityWarningText(context: Context): String {
        val status = getStatus(context)
        return if (status == CompatibilityStatus.FULLY_SUPPORTED) {
            "Your device is fully supported!"
        } else if (status == CompatibilityStatus.UNKNOWN) {
            "Your device's compatibility with Monet overlays is currently unknown. Results are not guaranteed.\n\nIf you find any issues, please report them to help improve compatibility!"
        } else {
            val rule = getDeviceRule(context)
            var text = "Known Issues on your device:\n${rule?.description ?: ""}"
            if (rule?.unsupportedStyles?.isNotEmpty() == true) {
                text += "\n\nUnsupported Styles: ${rule.unsupportedStyles.joinToString(", ") { it.name }}"
            }
            text
        }
    }
    
    fun generateCompatibilityReportTemplate(context: Context): String {
        val rule = getDeviceRule(context)
        val stylesStr = rule?.unsupportedStyles?.joinToString(", ") { it.name }?.takeIf { it.isNotBlank() } ?: "[List any styles that did not apply correctly]"
        return """
            Please test both applying a supported theme and an unsupported style (like Expressive) to see if your OS silently falls back to Tonal Spot.
            
            OS Name: ${getOsName(context)}
            Model: ${Build.MODEL}
            SDK Version: ${Build.VERSION.SDK_INT}
            
            Compatibility Status: [Fully Supported / Partial Support / Incompatible]
            Unsupported Styles (if any): $stylesStr
            Description: [Describe any issues or confirm full working status]
        """.trimIndent()
    }
}
