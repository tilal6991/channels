package co.fusionx.channels.configuration

import org.parceler.Parcel
import org.parceler.ParcelConstructor
import org.parceler.ParcelProperty

@Parcel(Parcel.Serialization.BEAN)
data class ServerConfiguration @ParcelConstructor constructor(
        @param:ParcelProperty("hostname") val hostname: String,
        @param:ParcelProperty("port") val port: Int,
        @param:ParcelProperty("ssl") val ssl: Boolean,
        @param:ParcelProperty("username") val username: String,
        @param:ParcelProperty("password") val password: String?)