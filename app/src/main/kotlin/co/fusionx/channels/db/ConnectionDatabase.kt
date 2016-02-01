package co.fusionx.channels.db

import android.content.Context
import co.fusionx.channels.relay.ClientConfiguration
import co.fusionx.relay.ConnectionConfiguration
import com.squareup.sqlbrite.BriteDatabase
import com.squareup.sqlbrite.SqlBrite
import rx.Observable

public class ConnectionDatabase(private val context: Context) {
    private val briteDb: BriteDatabase

    init {
        briteDb = SqlBrite.create().wrapDatabaseHelper(ConnectionDBHelper.instance(context))
    }

    public fun getConfigurations(): Observable<List<ClientConfiguration>> {
        return briteDb.createQuery(ConnectionDBColumns.TABLE_NAME, "SELECT * from ${ConnectionDBColumns.TABLE_NAME}")
                .mapToList {
                    ClientConfiguration(
                            it.getString(it.getColumnIndex(ConnectionDBColumns.NAME)),
                            ConnectionConfiguration.create {
                                hostname = it.getString(it.getColumnIndex(ConnectionDBColumns.HOSTNAME))
                                port = it.getInt(it.getColumnIndex(ConnectionDBColumns.PORT))
                            }
                    )
                }
    }

    companion object {
        private var instance: ConnectionDatabase? = null

        @Synchronized fun instance(ctx: Context): ConnectionDatabase {
            if (instance == null) {
                instance = ConnectionDatabase(ctx.applicationContext)
            }
            return instance!!
        }
    }
}