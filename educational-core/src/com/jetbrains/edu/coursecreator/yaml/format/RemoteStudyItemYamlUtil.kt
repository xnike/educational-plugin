package com.jetbrains.edu.coursecreator.yaml.format

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import java.util.*

const val ID = "id"
const val UPDATE_DATE = "update_date"

/**
 * Mixin class is used to deserialize remote information of [com.jetbrains.edu.learning.courseFormat.StudyItem] item stored on Stepik.
 */
@Suppress("unused", "UNUSED_PARAMETER") // used for json serialization
@JsonPropertyOrder(ID, UPDATE_DATE)
abstract class RemoteStudyItemYamlMixin {
  @JsonProperty(ID)
  private var myId: Int = 0

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "EEE, dd MMM yyyy HH:mm:ss zzz")
  @JsonProperty(UPDATE_DATE)
  private lateinit var myUpdateDate: Date
}