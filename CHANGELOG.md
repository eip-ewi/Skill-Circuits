[comment]: <> (Added = New features)
[comment]: <> (Changed = Changes in existing functionality)
[comment]: <> (Deprecated = once-stable features removed in future releases "next release")
[comment]: <> (Removed = Deprecated features removed in this release "this release")
[comment]: <> (Fixed = Bug fixes)
# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## Added

## Changed

## Fixed
- [Everyone] The width of columns is changed to avoid that the circuit extends over the screen width. (@ffiedrich)

## [2627.0.0]

## Added
- [Everyone] A legend was added to circuit pages showing what each symbol on the page means. (@ogangalic)
- [Teacher] A new tab was added with an overview of all tasks in an edition, including their links, which can be edited directly from this page. (@ffiedrich)
- [Teacher] Columns can now be deleted and added anywhere. (@ralani)
- [Editor] When editing a link, the user can press 'escape' and and click outside to stop editing. (@ogangalic)
- [Everyone] Tasks can now have deadlines. 
  This gives editors more fine-grained control to manage their courses. 
  For example, rather than a skill having a deadline because it must be done before a lecture, the lecture can be modeled as a task with a deadline with all dependencies as actual dependencies. (@ralani)
- [Teacher] Tasks can be filtered by links and other properties the edition task list. (@ffiedrich)
- [Editor] Editors can now see the total time needed for the completion of checkpoints.
  This gives editors more insight into how much time they are expecting students to put into their course. (@ogangalic)
- [Everyone] Dark and light high-contrast themes have been added. (@ffiedrich)
- [Student] A setting has been added that adds additional icons to signify completion, in addition to the colour change. (@ffiedrich)
- [Teacher] A research consent form can be configured per course edition. This is useful for teachers and researchers wish to use Skill Circuits usage data for their research. (@rwbackx)

## Changed
- [Student] Optional skills are now marked as optional in the expanded submodule view. (@ffiedrich)

## Fixed
- [Student] When in the submodule view, the skills appeared in a random order. (@ogangalic)
- [Developers] Several errors in the development console. (@ogangalic)
- [Student] Optional skills were required to complete submodules and checkpoints. (@ffiedrich) 
- [Student] Optional skills were included in the completed counts on the edition page. This counter has been separated into a 'required' and an 'optional' count. (@ffiedrich)
- [Everyone] Connections between submodules and skills were not highlighted on hover. (@ffiedrich)

## [2526.2.1]

## Added

## Changed

## Fixed
- [Student] Page redirects from external skills or bookmarked skills work as intended. (@ralani)

- [Student] When in the submodule view, the skills are now sorted. (@ogangalic)
- [Student] Session credentials expiration is tracked correctly. (@ralani)
## [2526.2.0]

## Added 
- [Student] When entering the module view it scrolls to top unfinished skill. (@ogangalic)
- [Student] Add expanded views for submodules that can be accessed from the edition page by clicking "open here". (@ffiedrich)

## Fixed
- [Student] The active path and the task removals/additions are taken into account for skill completion and (un)locking. (@ffiedrich)
- [Editor] Clicking on the "Student/Editor View" button on non-circuit pages now toggles between the modes correctly.
- [Student] The Reset Progress button functions accordingly. @ralani
- [Everyone] Exiting the extended view of a skill using "Escape" works as intended.
- [Teacher] Upcoming courses are displayed in the course overview. @ralani
- [Student] Dragging and dropping the task in the same skill or the Tray does not break drag and drop functionality afterwards. @ralani
- [Editor] When adding a hidden skill, its bookmark appears without refreshing the page. @ogangalic


## [2526.1.0]

## Added
 - [Editor] Can scroll to the skill whose connections they are editing via a button. (@rgiedryte)
 - [Everyone] Add a personal preferences page where users can set their theme and whether unreached blocks should be blurred.

## Changed
- [Editor] Scroll to the skill or submodule which has been newly added. (@ralani)

## Fixed
 - [Editor] Fixed bug where dependency indicator remain. (@ogangalic)
 - [Editor] When switching to student view, block editing closes and makes the edit.(@ogangalic)


## [2526.0.1]

## Added
 - [Teacher] Can download edition statistics at the student and task levels.


## [2526.0.0]

## Added
- [Everyone] A changelog is displayed to users after each new release. (@bbakos)

## Changed
- [Everyone] The UI
- [Editor] On edit, the minTasks value of a choice task is clipped to be inside the correct bounds. (@ffiedrich)
- [Everyone] The action indicator is changed to in the "glass" style. (@rwbackx)
- [Student] The cycle warning is not shown to students anymore. (@ffiedrich)
- [Student] The information about a checkpoint having passed is changed to a dialog. (@ffiedrich)

## Fixed
- [Editor] Fixed moving subtasks outside choice tasks. (@ffiedrich)


## [2.2.8]

## Added
- [Everyone] Functionality to add and complete choice tasks. Choice tasks contain a set of regular tasks, of which at least k need to be completed.

## Changed

## Fixed
- [Everyone] If the new version information cannot be retrieved from GitLab, the what's new dialog is not shown. (@bbakos)
- [Student] Fix hidden skills only appearing after page reload.

## [2.2.7]

## Added
- [Everyone] The skill circuits email address is now displayed on the home page. (@bbakos)
- [Everyone] When a your session expires, a popup will now be shown asking you to log in again. (@bbakos)
- [Teacher] Task order can now be changed using drag and drop. (@ffiedrich)

## Changed
- [Everyone] Courses are now displayed under 'Your courses' if you have a role in them.
  This will not significantly impact the courses you see, unless you often work with other Labrador products. (@ffiedrich)
- [Teacher] Modules, submodules, and checkpoints are now sorted alphabetically in dropdowns. (@bbakos)
- [TA] TAs now get redirected to the latest edition of a course they have a role in, similar to students. (@ffiedrich)

## Fixed
- The correct checkpoint is now considered as the last checkpoint. (@ffiedrich)
- Checkpoint deletion now makes the correct next checkpoint be used instead. (@ffiedrich)
- Moving the last skill from a checkpoint now makes the checkpoint disappear. (@ffiedrich)
- Redundant ghost checkpoint at bottom of page has been removed. (@ffiedrich)
- After dragging a checkpoint, the menu now disappears unless it is hovered. (@ffiedrich)
- Path toggle listeners are now correctly added to hidden skills on first reveal. (@ffiedrich)
- Completion status of revealed hidden skills is now updated without page reload. (@ffiedrich)
- Revealed skills now remain visible even when required tasks are marked as 'not completed'. (@rglans)
- Skills no longer duplicate after the cancellation of the editing. (@bbakos)
- The 'Optional' box is now displayed correctly. (@bbakos)
- Only authorized people can now access tasks. (@bbakos)
- Submodules can now be edited after editing modules in the setup page. (@bbakos)
- Back button now navigates to previously expanded skills instead of navigating to the same page. (@ffiedrich)
- Renaming a path does no longer remove all tasks in path. (@ffiedrich)
- When starting to edit a new checkpoint whilst editing another checkpoint, the previous checkpoint will now be restored. (@bbakos)
- Adding and removing a task on the custom path now results in the teacher determined order. (@bbakos)

## [2.2.6]

## Added
## Changed
- Empty path toggles are greyed out, and padding is added between expanded toggles (@ffiedrich)
- Split courses on homepage into categories and show finished/active courses (@ffiedrich)
- Assigning student role after task completion if user had no role yet and is student by default (@ffiedrich)

## Fixed
- Fixed adding/removing task frontend bugs and updating skill completion status (@ffiedrich)
- Teacher view: Reordering of tasks now works on first click (@rglans)
- Teacher view: Can now reorder new tasks on creation (@rglans)

## [2.2.5]

## Added
- Show a changelog to users on new releases (@bbakos)
## Changed
- Head TAs have the same permissions in a course edition as the teacher (@rglans)
## Fixed

## [2.2.4]

## Added
## Changed
## Fixed
- Fixed skill completion tracker (@bbakos)
- Fixed changing checkpoint name in setup for teachers (@bbakos)
- Fixed task completion to send request and to show hidden skill (@ffiedrich)
- Fixed being able to remove dependency lines (@ffiedrich)
- Fixed deleting hidden skills with at least one dependency (@ffiedrich)

## [2.2.3]

### Added
- Add a table overview for editing links in an edition (@ffiedrich)
- Add a table for storing information about clicked links (@bbakos) 
- Paths can now be customised per user. (@mcoman)

### Changed

### Fixed
- Copy edition confirmation pop up shows the correct edition. (@ffiedrich)
- Fixed student view for teachers (@bbakos)
- Fix connections between skills to be hoverable and removable (@ffiedrich)

## [2.2.2]

### Added
- Floating box which displays information about the most recently completed task (@ffiedrich)
- Skill can remember the skill from the edition it was copied from (@ffiedrich)
- Ability to insert new rows and columns when editing editions or modules (@bbakos)
- Added option to change/switch checkpoint to another one which is not used in the current module. (@ffiedrich)
- Add the ability to copy the contents of one edition to another (empty) edition. (@ffiedrich)

### Changed

### Fixed
 - Fixed isNotTransitivelyConnected for cyclic graphs and for external skills
 - Fixed to not count optional skills in highlighting of skills that can be worked on. (@ffiedrich)
 - Fixed external skill block link (@ffiedrich)
 - External skills are accessible if they are visible to the user. (@ffiedrich)
 - Checking access rights for skills and modules, and enforcing login for view methods. (@ffiedrich)

## [2.2.1]

### Changed
 - When creating a skill outside of checkpoint, you can now create a checkpoint in there. (@okaaij)
### Fixed
 - Fixed ghosts after moving a checkpoints (@okaaij)
 - Fixed moving a skill outside of the checkpoints (@okaaij)
 - Fixed filtering transitive connections with hidden skills (@stefanhugtenbu)

## [2.2.0]
### Added
 - Paths: tasks can be added to a path, only those skills are then displayed when that path is selected (@mcoman)
 - Teachers can set a default path (@mcoman)
 - Students can select a preferred path (@mcoman)

### Changed
 - When creating a skill on an empty module page, a checkpoint can be created or selected for that skill (@okaaij)
 - Transitive connections no longer display (@wjbreedveld)

### Fixed
 - Grid does not function properly if the last row or column is only external skills (@okaaij)


