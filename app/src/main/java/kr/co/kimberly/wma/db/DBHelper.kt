package kr.co.kimberly.wma.db

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import kr.co.kimberly.wma.model.OrderTempList

class DBHelper(
    context: Context?,
    factory: CursorFactory?,
):SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "ORDER_TEMP_LIST"
        const val DATABASE_VERSION = 1

        // Table
        const val TABLE_NAME = "ORDER_LIST"
        const val UID = "UID"
        const val COL_IDX= "IDX"
        const val COL_ITEMNAME = "ITEM_NAME"
        const val COL_ITEMCODE = "ITEM_CODE"
        const val COL_QTYBOX = "QTY_BOX"
        const val COL_QTYEA = "QTY_EA"
        const val COL_TOTALPRICE = "TOTAL_PRICE"
        const val COL_DATE = "TOTAL_PRICE"
        const val COL_UPDATEDDATE = "UPDATED_DATE"
        const val COL_DELETEDDATE = "DELETED_DATE"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // UID 는 SQLite 를 사용하기 위해서 필수적으로 필요한 column
        val sql : String = "CREATE TABLE IF NOT EXISTS " +
                "$TABLE_NAME ($UID integer primary key autoincrement, " +
                "$COL_IDX Int, $COL_ITEMNAME text, $COL_ITEMCODE text, $COL_QTYBOX int, $COL_QTYEA int, $COL_TOTALPRICE int, $COL_DATE text, $COL_UPDATEDDATE Int, $COL_DELETEDDATE Int);"

        db.execSQL(sql)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        val sql : String = "DROP TABLE IF EXISTS $TABLE_NAME"

        db.execSQL(sql)
        onCreate(db)
    }

    val savedOrderList : List<OrderTempList>
        @SuppressLint("Range", "Recycle")
        get() {
            val list = ArrayList<OrderTempList>()
            val selectedQueryHandler = "SELECT * FROM $TABLE_NAME"
            val db = this.writableDatabase
            val cursor = db.rawQuery(selectedQueryHandler, null)
            if (cursor.moveToFirst()){
                do {
                    val data = OrderTempList()
                    data.idx = cursor.getInt(cursor.getColumnIndex(COL_IDX))
                    data.itemName = cursor.getString(cursor.getColumnIndex(COL_ITEMNAME))
                    data.itemCode = cursor.getString(cursor.getColumnIndex(COL_ITEMCODE))
                    data.qtyBox = cursor.getInt(cursor.getColumnIndex(COL_QTYBOX))
                    data.qtyEA = cursor.getInt(cursor.getColumnIndex(COL_QTYEA))
                    data.totalPrice = cursor.getInt(cursor.getColumnIndex(COL_TOTALPRICE))
                    data.date = cursor.getString(cursor.getColumnIndex(COL_DATE))
                    data.updatedDate = cursor.getString(cursor.getColumnIndex(COL_UPDATEDDATE))
                    data.deletedDate = cursor.getString(cursor.getColumnIndex(COL_DELETEDDATE))

                    list.add(data)
                }while (cursor.moveToNext())
                Log.d("test log", "저장된 데이터 : $list")
            }
            db.close()
            return list
        }

    fun addData(data: OrderTempList) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COL_IDX, data.idx)
        values.put(COL_ITEMNAME, data.itemName)
        values.put(COL_ITEMCODE, data.itemCode)
        values.put(COL_QTYBOX, data.qtyBox)
        values.put(COL_QTYEA, data.qtyEA)
        values.put(COL_TOTALPRICE, data.totalPrice)
        values.put(COL_DATE, data.date)
        values.put(COL_UPDATEDDATE, data.updatedDate)
        values.put(COL_DELETEDDATE, data.deletedDate)

        db.insert(TABLE_NAME, null, values)
        db.close()
        Log.d("test log", "데이터 저장 성공")
    }

    fun updateData(data: OrderTempList): Int{
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COL_IDX, data.idx)
        values.put(COL_ITEMNAME, data.itemName)
        values.put(COL_ITEMCODE, data.itemCode)
        values.put(COL_QTYBOX, data.qtyBox)
        values.put(COL_QTYEA, data.qtyEA)
        values.put(COL_TOTALPRICE, data.totalPrice)
        values.put(COL_DATE, data.date)
        values.put(COL_UPDATEDDATE, data.updatedDate)
        values.put(COL_DELETEDDATE, data.deletedDate)

        return db.update(TABLE_NAME, values, "$COL_IDX=?", arrayOf(arrayOf(data.idx).toString()))
    }

    fun deleteData(data: OrderTempList){
        val db = this.writableDatabase

        db.delete(TABLE_NAME, "$COL_ITEMCODE=?", arrayOf(arrayOf(data.idx).toString()))
        db.close()
    }
}