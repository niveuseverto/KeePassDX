/*
 * Copyright 2019 Jeremy Jamet / Kunzisoft.
 *
 * This file is part of KeePassDX.
 *
 *  KeePassDX is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  KeePassDX is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with KeePassDX.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.kunzisoft.keepass.activities

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.kunzisoft.keepass.R
import com.kunzisoft.keepass.autofill.AutofillHelper
import com.kunzisoft.keepass.autofill.KeeAutofillService
import com.kunzisoft.keepass.database.element.Database
import com.kunzisoft.keepass.database.search.SearchHelper
import com.kunzisoft.keepass.model.SearchInfo
import com.kunzisoft.keepass.settings.PreferencesUtil

@RequiresApi(api = Build.VERSION_CODES.O)
class AutofillLauncherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        // Build search param
        val searchInfo = SearchInfo().apply {
            applicationId = intent.getStringExtra(KEY_SEARCH_APPLICATION_ID)
            webDomain = intent.getStringExtra(KEY_SEARCH_DOMAIN)
        }

        // Pass extra for Autofill (EXTRA_ASSIST_STRUCTURE)
        val assistStructure = AutofillHelper.retrieveAssistStructure(intent)

        if (assistStructure == null) {
            setResult(Activity.RESULT_CANCELED)
            finish()
        } else if (!KeeAutofillService.searchAllowedFor(searchInfo.applicationId,
                        PreferencesUtil.applicationIdBlocklist(this))
                || !KeeAutofillService.searchAllowedFor(searchInfo.webDomain,
                        PreferencesUtil.webDomainBlocklist(this))) {
            // If item not allowed, show a toast
            Toast.makeText(this.applicationContext, R.string.autofill_block_restart, Toast.LENGTH_LONG).show()
            setResult(Activity.RESULT_CANCELED)
            finish()
        } else {
            // If database is open
            SearchHelper.checkAutoSearchInfo(this,
                    Database.getInstance(),
                    searchInfo,
                    { items ->
                        // Items found
                        AutofillHelper.buildResponse(this, items)
                        finish()
                    },
                    {
                        // Show the database UI to select the entry
                        GroupActivity.launchForAutofillResult(this,
                                assistStructure,
                                false,
                                searchInfo)
                    },
                    {
                        // If database not open
                        FileDatabaseSelectActivity.launchForAutofillResult(this,
                                assistStructure,
                                searchInfo)
                    }
            )
        }

        super.onCreate(savedInstanceState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        AutofillHelper.onActivityResultSetResultAndFinish(this, requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {

        private const val KEY_SEARCH_APPLICATION_ID = "KEY_SEARCH_APPLICATION_ID"
        private const val KEY_SEARCH_DOMAIN = "KEY_SEARCH_DOMAIN"

        fun getAuthIntentSenderForResponse(context: Context,
                                           searchInfo: SearchInfo? = null): IntentSender {
            return PendingIntent.getActivity(context, 0,
                    // Doesn't work with Parcelable (don't know why?)
                    Intent(context, AutofillLauncherActivity::class.java).apply {
                        searchInfo?.let {
                            putExtra(KEY_SEARCH_APPLICATION_ID, it.applicationId)
                            putExtra(KEY_SEARCH_DOMAIN, it.webDomain)
                        }
                    },
                    PendingIntent.FLAG_CANCEL_CURRENT).intentSender
        }
    }
}
