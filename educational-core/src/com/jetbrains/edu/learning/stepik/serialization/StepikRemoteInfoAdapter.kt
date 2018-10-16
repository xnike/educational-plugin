package com.jetbrains.edu.learning.stepik.serialization

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.jetbrains.edu.learning.EduNames
import com.jetbrains.edu.learning.courseFormat.Course
import com.jetbrains.edu.learning.courseFormat.Lesson
import com.jetbrains.edu.learning.courseFormat.Section
import com.jetbrains.edu.learning.courseFormat.tasks.Task
import com.jetbrains.edu.learning.stepik.StepikNames
import com.jetbrains.edu.learning.stepik.courseFormat.StepikCourse
import com.jetbrains.edu.learning.stepik.courseFormat.remoteInfo.StepikCourseRemoteInfo
import com.jetbrains.edu.learning.stepik.courseFormat.remoteInfo.StepikLessonRemoteInfo
import com.jetbrains.edu.learning.stepik.courseFormat.remoteInfo.StepikSectionRemoteInfo
import com.jetbrains.edu.learning.stepik.courseFormat.remoteInfo.StepikTaskRemoteInfo
import org.fest.util.Lists
import java.lang.reflect.Type
import java.util.*

private const val UPDATE_DATE = "update_date"

class StepikCourseRemoteInfoAdapter : JsonDeserializer<StepikCourse>, JsonSerializer<Course> {
  private val IS_PUBLIC = "is_public"
  private val IS_IDEA_COMPATIBLE = "is_idea_compatible"
  private val ID = "id"
  private val SECTIONS = "sections"
  private val INSTRUCTORS = "instructors"
  private val COURSE_FORMAT = "course_format"

  override fun serialize(course: Course?, type: Type?, context: JsonSerializationContext?): JsonElement {
    val gson = getGson()
    val tree = gson.toJsonTree(course)
    val jsonObject = tree.asJsonObject
    val remoteInfo = course?.remoteInfo

    val stepikRemoteInfo = remoteInfo as? StepikCourseRemoteInfo

    jsonObject.add(IS_PUBLIC, JsonPrimitive(stepikRemoteInfo?.isPublic ?: false))
    jsonObject.add(IS_IDEA_COMPATIBLE, JsonPrimitive(stepikRemoteInfo?.isIdeaCompatible ?: false))
    jsonObject.add(ID, JsonPrimitive(stepikRemoteInfo?.id ?: 0))
    jsonObject.add(SECTIONS, gson.toJsonTree(stepikRemoteInfo?.sectionIds ?: Lists.emptyList<Int>()))
    jsonObject.add(INSTRUCTORS, gson.toJsonTree(stepikRemoteInfo?.instructors ?: Lists.emptyList<Int>()))
    jsonObject.add(COURSE_FORMAT, JsonPrimitive(stepikRemoteInfo?.courseFormat ?: ""))

    val updateDate = stepikRemoteInfo?.updateDate
    if (updateDate != null) {
      val date = gson.toJsonTree(updateDate)
      jsonObject.add(UPDATE_DATE, date)
    }
    return jsonObject
  }

  @Throws(JsonParseException::class)
  override fun deserialize(json: JsonElement, type: Type, jsonDeserializationContext: JsonDeserializationContext): StepikCourse {
    val gson = getGson()

    val course = gson.fromJson(json, StepikCourse::class.java)
    deserializeRemoteInfo(json, course, gson)
    course.updateCourseCompatibility()
    return course
  }

  private fun deserializeRemoteInfo(json: JsonElement, course: StepikCourse, gson: Gson) {
    val jsonObject = json.asJsonObject
    val remoteInfo = StepikCourseRemoteInfo()
    val isPublic = jsonObject.get(IS_PUBLIC).asBoolean
    val isCompatible = jsonObject.get(IS_IDEA_COMPATIBLE).asBoolean
    val id = jsonObject.get(ID).asInt
    val courseFormat = jsonObject.get(COURSE_FORMAT).asString

    val sections = gson.fromJson<MutableList<Int>>(jsonObject.get(SECTIONS), object: TypeToken<MutableList<Int>>(){}.type)
    val instructors = gson.fromJson<MutableList<Int>>(jsonObject.get(INSTRUCTORS), object: TypeToken<MutableList<Int>>(){}.type)
    val updateDate = gson.fromJson(jsonObject.get(UPDATE_DATE), Date::class.java)

    remoteInfo.isPublic = isPublic
    remoteInfo.isIdeaCompatible = isCompatible
    remoteInfo.id = id
    remoteInfo.sectionIds = sections
    remoteInfo.instructors = instructors
    remoteInfo.updateDate = updateDate
    remoteInfo.courseFormat = courseFormat

    course.remoteInfo = remoteInfo
  }

  private fun getGson(): Gson {
    return GsonBuilder()
      .setPrettyPrinting()
      .excludeFieldsWithoutExposeAnnotation()
      .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
      .registerTypeAdapter(Lesson::class.java, StepikLessonRemoteInfoAdapter())
      .registerTypeAdapter(Section::class.java, StepikSectionRemoteInfoAdapter())
      .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
      .create()
  }
}

class StepikSectionRemoteInfoAdapter : JsonDeserializer<Section>, JsonSerializer<Section> {
  private val ID = "id"
  private val COURSE_ID = "course"
  private val POSITION = "position"
  private val UNITS = "units"

  override fun serialize(section: Section?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
    val gson = getGson()
    val tree = gson.toJsonTree(section)
    val jsonObject = tree.asJsonObject
    val remoteInfo = section?.remoteInfo

    val stepikRemoteInfo = remoteInfo as? StepikSectionRemoteInfo

    jsonObject.add(ID, JsonPrimitive(stepikRemoteInfo?.id ?: 0))
    jsonObject.add(COURSE_ID, JsonPrimitive(stepikRemoteInfo?.courseId ?: 0))
    jsonObject.add(POSITION, JsonPrimitive(stepikRemoteInfo?.position ?: 0))
    jsonObject.add(UNITS, gson.toJsonTree(stepikRemoteInfo?.units ?: Lists.emptyList<Int>()))

    val updateDate = stepikRemoteInfo?.updateDate
    if (updateDate != null) {
      val date = gson.toJsonTree(updateDate)
      jsonObject.add(UPDATE_DATE, date)
    }
    return jsonObject
  }

  override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): Section {
    val gson = getGson()

    val section = gson.fromJson(json, Section::class.java)
    deserializeRemoteInfo(gson, section, json)
    return section
  }

  private fun deserializeRemoteInfo(gson: Gson, section: Section, json: JsonElement) {
    val jsonObject = json.asJsonObject

    val remoteInfo = StepikSectionRemoteInfo()
    val id = jsonObject.get(ID).asInt
    val courseId = jsonObject.get(COURSE_ID).asInt
    val position = jsonObject.get(POSITION).asInt
    val updateDate = gson.fromJson(jsonObject.get(UPDATE_DATE), Date::class.java)
    val units = gson.fromJson<MutableList<Int>>(jsonObject.get(UNITS), object: TypeToken<MutableList<Int>>(){}.type)

    remoteInfo.id = id
    remoteInfo.courseId = courseId
    remoteInfo.position = position
    remoteInfo.updateDate = updateDate
    remoteInfo.units = units

    section.remoteInfo = remoteInfo
  }

  private fun getGson(): Gson {
    return GsonBuilder()
      .setPrettyPrinting()
      .excludeFieldsWithoutExposeAnnotation()
      .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
      .registerTypeAdapter(Lesson::class.java, StepikLessonRemoteInfoAdapter())
      .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
      .create()
  }
}

class StepikLessonRemoteInfoAdapter : JsonDeserializer<Lesson>, JsonSerializer<Lesson> {
  private val ID = "id"
  private val IS_PUBLIC = "is_public"
  private val STEPS = "steps"

  override fun serialize(lesson: Lesson?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
    val gson = getGson()

    val tree = gson.toJsonTree(lesson)
    val jsonObject = tree.asJsonObject
    val remoteInfo = lesson?.remoteInfo

    val stepikRemoteInfo = remoteInfo as? StepikLessonRemoteInfo

    jsonObject.add(ID, JsonPrimitive(stepikRemoteInfo?.id ?: 0))
    jsonObject.add(IS_PUBLIC, JsonPrimitive(stepikRemoteInfo?.isPublic ?: false))
    jsonObject.add(STEPS, gson.toJsonTree(stepikRemoteInfo?.steps ?: Lists.emptyList<Int>()))

    val updateDate = stepikRemoteInfo?.updateDate
    if (updateDate != null) {
      val date = gson.toJsonTree(updateDate)
      jsonObject.add(UPDATE_DATE, date)
    }

    return jsonObject
  }

  override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): Lesson {
    val gson = getGson()

    val lesson = gson.fromJson(json, Lesson::class.java)
    deserializeRemoteInfo(gson, lesson, json)
    renameAdditionalInfo(lesson)
    return lesson
  }

  private fun deserializeRemoteInfo(gson: Gson, lesson: Lesson, json: JsonElement) {
    val jsonObject = json.asJsonObject

    val remoteInfo = StepikLessonRemoteInfo()

    val id = jsonObject.get(ID).asInt
    val isPublic = jsonObject.get(IS_PUBLIC).asBoolean
    val updateDate = gson.fromJson(jsonObject.get(UPDATE_DATE), Date::class.java)
    val steps = gson.fromJson<MutableList<Int>>(jsonObject.get(STEPS), object: TypeToken<MutableList<Int>>(){}.type)

    remoteInfo.id = id
    remoteInfo.isPublic = isPublic
    remoteInfo.updateDate = updateDate
    remoteInfo.steps = steps

    lesson.remoteInfo = remoteInfo
  }

  private fun renameAdditionalInfo(lesson: Lesson) {
    val name = lesson.name
    if (StepikNames.PYCHARM_ADDITIONAL == name) {
      lesson.name = EduNames.ADDITIONAL_MATERIALS
    }
  }

  private fun getGson(): Gson {
    return GsonBuilder()
      .setPrettyPrinting()
      .excludeFieldsWithoutExposeAnnotation()
      .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
      .registerTypeAdapter(Task::class.java, StepikTaskRemoteInfoAdapter())
      .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
      .create()
  }
}

class StepikTaskRemoteInfoAdapter : JsonDeserializer<Task>, JsonSerializer<Task> {
  private val ID = "stepic_id"

  override fun serialize(task: Task?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
    val gson = getGson()
    val tree = gson.toJsonTree(task)
    val jsonObject = tree.asJsonObject
    val remoteInfo = task?.remoteInfo

    val stepikRemoteInfo = remoteInfo as? StepikTaskRemoteInfo

    jsonObject.add(ID, JsonPrimitive(stepikRemoteInfo?.id ?: 0))

    val updateDate = stepikRemoteInfo?.updateDate
    if (updateDate != null) {
      val date = gson.toJsonTree(updateDate)
      jsonObject.add(UPDATE_DATE, date)
    }

    return jsonObject
  }

  override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): Task {
    val gson = getGson()

    val task = gson.fromJson(json, Task::class.java)
    deserializeRemoteInfo(gson, task, json)
    return task
  }

  private fun deserializeRemoteInfo(gson: Gson, task: Task, json: JsonElement) {
    val jsonObject = json.asJsonObject

    val remoteInfo = StepikTaskRemoteInfo()

    val id = jsonObject.get(ID).asInt
    val updateDate = gson.fromJson(jsonObject.get(UPDATE_DATE), Date::class.java)

    remoteInfo.id = id
    remoteInfo.updateDate = updateDate

    task.remoteInfo = remoteInfo
  }

  private fun getGson(): Gson {
    return GsonBuilder()
      .setPrettyPrinting()
      .excludeFieldsWithoutExposeAnnotation()
      .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
      .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
      .create()
  }
}