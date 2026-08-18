package com.footballmanager.serialization

import com.footballmanager.model.Attribute
import com.footballmanager.model.PlayerAttributes
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Serializes [PlayerAttributes] as a flat `Map<Attribute, Int>`.
 *
 * [PlayerAttributes] is a regular class whose only state is a private,
 * normalized attribute map (not a constructor property), so the plugin cannot
 * derive a serializer for it automatically. We delegate to the map serializer,
 * which keeps the JSON compact and reconstructs through the primary constructor
 * (so clamping/defaulting still applies on load).
 */
object PlayerAttributesSerializer : KSerializer<PlayerAttributes> {

    private val delegate: KSerializer<Map<Attribute, Int>> =
        MapSerializer(Attribute.serializer(), Int.serializer())

    override val descriptor: SerialDescriptor = delegate.descriptor

    override fun serialize(encoder: Encoder, value: PlayerAttributes) {
        encoder.encodeSerializableValue(delegate, value.toMap())
    }

    override fun deserialize(decoder: Decoder): PlayerAttributes =
        PlayerAttributes(decoder.decodeSerializableValue(delegate))
}
