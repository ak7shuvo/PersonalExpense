package com.ak7shuvo.personalexpense

import android.app.Application
import com.ak7shuvo.personalexpense.data.AppDatabase
import com.ak7shuvo.personalexpense.data.LedgerRepository

class PersonalExpenseApp : Application() {
    val repository: LedgerRepository by lazy { LedgerRepository(AppDatabase.open(this)) }
}
