package com.breezemobilearndemo.domain

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query


@Entity(tableName = "lms_notification")
data class LMSNotiEntity(
    @PrimaryKey(autoGenerate = true) var sl_no: Int = 0,
    @ColumnInfo var noti_datetime:String = "",
    @ColumnInfo var noti_date:String = "",
    @ColumnInfo var noti_time:String = "",
    @ColumnInfo var noti_header:String = "",
    @ColumnInfo var noti_message:String = "",
    @ColumnInfo var isViwed:Boolean = false
)

@Dao
interface LMSNotiDao {
    @Insert
    fun insert(vararg obj: LMSNotiEntity)

    @Query("SELECT * FROM lms_notification where noti_date =:noti_date order by noti_date DESC , noti_time DESC")
    fun getNotiByDate(noti_date:String): List<LMSNotiEntity>

    @Query("select distinct noti_date from lms_notification")
    fun getDistinctDate(): List<String>

    @Query("update lms_notification set isViwed = :isViwed")
    fun updateISViwed(isViwed:Boolean)

    @Query("select *  from lms_notification where isViwed =:isViwed")
    fun getNotViwed(isViwed:Boolean):List<LMSNotiEntity>
}