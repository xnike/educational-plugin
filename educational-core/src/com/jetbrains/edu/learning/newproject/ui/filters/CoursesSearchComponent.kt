package com.jetbrains.edu.learning.newproject.ui.filters

import com.intellij.ide.plugins.newui.HorizontalLayout
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.jetbrains.edu.learning.courseFormat.Course
import com.jetbrains.edu.learning.courseFormat.ext.supportedTechnologies
import com.jetbrains.edu.learning.newproject.ui.coursePanel.MAIN_BG_COLOR
import com.jetbrains.edu.learning.newproject.ui.coursePanel.groups.CoursesGroup
import java.awt.BorderLayout
import javax.swing.JPanel

class CoursesSearchComponent(
  val getCoursesGroups: () -> List<CoursesGroup>,
  val updateModel: (List<CoursesGroup>) -> Unit
) : JPanel(BorderLayout()) {
  private val coursesSearchComponent: CoursesFilterComponent = CoursesFilterComponent({ getCoursesGroups() },
                                                                                      { groups -> updateModel(groups) })
  private var programmingLanguagesFilterDropdown: ProgrammingLanguageFilterDropdown = ProgrammingLanguageFilterDropdown(
    programmingLanguages(emptyList())) {
    updateModel(getCoursesGroups())
  }
  private var humanLanguagesFilterDropdown: HumanLanguageFilterDropdown = HumanLanguageFilterDropdown(humanLanguages(emptyList())) {
    updateModel(getCoursesGroups())
  }

  init {
    border = JBUI.Borders.empty(11, 0)

    add(coursesSearchComponent, BorderLayout.CENTER)

    val filtersPanel = JPanel(HorizontalLayout(0))
    filtersPanel.add(programmingLanguagesFilterDropdown)
    filtersPanel.add(humanLanguagesFilterDropdown)

    add(filtersPanel, BorderLayout.LINE_END)

    UIUtil.setBackgroundRecursively(this, MAIN_BG_COLOR)
  }

  fun resetSearchField() = coursesSearchComponent.resetSearchField()

  fun filterCourses(courses: List<Course>): List<Course> {
    var filteredCourses = programmingLanguagesFilterDropdown.filter(courses)
    filteredCourses = humanLanguagesFilterDropdown.filter(filteredCourses)
    return filteredCourses
  }

  fun updateFilters(coursesGroups: List<CoursesGroup>) {
    val courses = coursesGroups.flatMap { it.courses }
    humanLanguagesFilterDropdown.updateItems(humanLanguages(courses))
    programmingLanguagesFilterDropdown.updateItems(programmingLanguages(courses))
  }

  fun resetSelection() {
    humanLanguagesFilterDropdown.resetSelection()
    programmingLanguagesFilterDropdown.resetSelection()
  }

  fun hideFilters() {
    humanLanguagesFilterDropdown.isVisible = false
    programmingLanguagesFilterDropdown.isVisible = false
  }

  fun selectAllHumanLanguageItems() {
    humanLanguagesFilterDropdown.selectedItems = humanLanguagesFilterDropdown.allItems
  }

  private fun humanLanguages(courses: List<Course>): Set<String> = courses.map { it.humanLanguage }.toSet()

  private fun programmingLanguages(courses: List<Course>): Set<String> = courses.map { it.supportedTechnologies }.flatten().toSet()
}