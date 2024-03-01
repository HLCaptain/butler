package illyan.butler

import org.w3c.dom.Storage

external val localStorage: dynamic

@JsModule("localstorage-slim")
@JsNonModule
external object LocalStorageSlim {
    //const config: StorageConfig = {
    //  ttl: null,
    //  encrypt: false,
    //  encrypter,
    //  decrypter,
    //  secret: 75,
    //  storage: undefined,
    //};
    var config: StorageConfig = definedExternally

//export type Encrypter = (...args: unknown[]) => string;
//export type Decrypter = (...args: unknown[]) => string;
//
//export type Dictionary<T = unknown> = Record<string, T>;
//
//export interface StorageConfig {
//  storage?: Storage;
//  ttl?: number | null;
//  encrypt?: boolean;
//  decrypt?: boolean;
//  encrypter?: Encrypter;
//  decrypter?: Decrypter;
//  secret?: unknown;
//}
}



external interface Encrypter {
    fun invoke(vararg args: dynamic): String
}

external interface Decrypter {
    fun invoke(vararg args: dynamic): String
}

external interface StorageConfig {
    var storage: Storage?
    var ttl: Int?
    var encrypt: Boolean
    var decrypt: Boolean
    var encrypter: Encrypter
    var decrypter: Decrypter
    var secret: dynamic
}
