package id.fajarproject.animusic.data.realm

import id.fajarproject.animusic.data.network.model.MusicItem
import io.realm.Realm
import java.util.*


/**
 * Created by Fajar Adi Prasetyo on 21/08/2020.
 */

class RealmHelper(private val realm: Realm) {

    fun save(musicItem: MusicItem) {
        musicItem.createDate = Calendar.getInstance().timeInMillis
        realm.executeTransaction { transaction: Realm ->
            transaction.copyToRealmOrUpdate(musicItem)
        }
    }

    fun loadMusic(): MutableList<MusicItem?> {
        val result = realm.where(MusicItem::class.java).findAll()
        return realm.copyFromRealm(result)
    }

    fun deleteMusic(id: Int) {
        val item = realm.where(MusicItem::class.java).equalTo("id", id).findFirst()
        if (item != null) {
            realm.executeTransaction { item.deleteFromRealm() }
        }
    }

    fun checkData(id: Int): Boolean {
        val item = realm.where(MusicItem::class.java).equalTo("id", id).findFirst()
        return item != null
    }
}