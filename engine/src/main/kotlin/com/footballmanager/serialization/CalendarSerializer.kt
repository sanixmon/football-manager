package com.footballmanager.serialization

import com.footballmanager.model.Calendar
import com.footballmanager.model.Fixture
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Serializes [Calendar] as the sorted list of its [Fixture]s.
 *
 * [Calendar] hides its fixtures behind a private field (it only exposes a
 * read-only accessor), so the plugin cannot derive a serializer from its
 * constructor. We treat the calendar as a plain fixture list, which is its
 * complete observable state; [Calendar] re-sorts on construction.
 */
object CalendarSerializer : KSerializer<Calendar> {

    private val delegate: KSerializer<List<Fixture>> = ListSerializer(Fixture.serializer())

    override val descriptor: SerialDescriptor = delegate.descriptor

    override fun serialize(encoder: Encoder, value: Calendar) {
        encoder.encodeSerializableValue(delegate, value.fixtures())
    }

    override fun deserialize(decoder: Decoder): Calendar =
        Calendar(decoder.decodeSerializableValue(delegate))
}
