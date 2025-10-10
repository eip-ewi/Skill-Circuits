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
- [Everyone] Functionality to add and complete choice tasks. Choice tasks contain a set of regular tasks, of which at least k need to be completed.
- [Teacher] Can download edition statistics at the student and task levels.

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


