package com.my.timecheckerforegroundservice

import android.app.usage.UsageStatsManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.my.timecheckerforegroundservice.databinding.ActivityMainBinding
import com.my.timecheckerforegroundservice.service.TimeCheckerService
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val REQUEST_PACKAGE_USAGE_STATS = 1
    private val REQUEST_CODE_SYSTEM_ALERT_WINDOW = 154


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.plant(Timber.DebugTree())
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        checkOnDrawPermission()
    }

    private fun startServiceProcess() {
        val intent = Intent(this, TimeCheckerService::class.java)
        startService(intent)
    }

    private fun observeForegroundApplications() {
        val usageStatsManager = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_PACKAGE_USAGE_STATS) {
            Timber.tag("XYZ").e("izin verildi")
            // Kullanıcı izinleri verdiğinde yapılacak işlemleri burada gerçekleştirebilirsiniz
        }

        if (requestCode == REQUEST_CODE_SYSTEM_ALERT_WINDOW) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
                Timber.tag("XYZ").e("izin başarıyla alındı")
                startServiceProcess()
                // İzin başarıyla alındı, üstte gösterme özelliğini kullanabilirsiniz.
                // Örneğin, pencereyi ekleme işlemini burada gerçekleştirebilirsiniz.
            } else {
                Timber.tag("XYZ").e("izin verilmedi")
                // İzin hala alınmadı, gerekli işlemleri gerçekleştiremezsiniz.
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun checkOnDrawPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Timber.tag("XYZ").e("izin YOK")
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + packageName)
            )
            startActivityForResult(intent, REQUEST_CODE_SYSTEM_ALERT_WINDOW)
        } else {
            Timber.tag("XYZ").e("izin zaten verilmiş")
            startServiceProcess()
            // İzin zaten alınmışsa veya cihazın API seviyesi < 23 ise doğrudan üstte gösterme özelliğini kullanabilirsiniz.
            // Örneğin, pencereyi ekleme işlemini burada gerçekleştirebilirsiniz.
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.tag("XYZ").e("onDestroy foregroundAppTestActivity")
        val intent = Intent(this, TimeCheckerService::class.java)
        stopService(intent)
    }


}