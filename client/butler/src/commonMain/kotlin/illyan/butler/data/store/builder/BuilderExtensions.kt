package illyan.butler.data.store.builder

import org.mobilenativefoundation.store.store5.Converter
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.MutableStoreBuilder
import org.mobilenativefoundation.store.store5.SourceOfTruth

fun<Key : Any, Output : Any> from(
    fetcher: Fetcher<Key, Output>,
    sourceOfTruth: SourceOfTruth<Key, Output, Output>
) = MutableStoreBuilder.from(fetcher, sourceOfTruth, converter = NoopConverter())

fun<Output : Any> NoopConverter() = Converter.Builder<Output, Output, Output>()
    .fromOutputToLocal { it }
    .fromNetworkToLocal { it }
    .build()