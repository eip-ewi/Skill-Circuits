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
- Mention email address on the home page (@bbakos)
## Changed
- Teacher view: Sorting modules, submodules and checkpoints at selection dropdowns (@bbakos)
- Teacher view: Task order can be changed via drag and drop (@ffiedrich)
- A course is added to "Your courses" if the user has a role, instead of if they completed a task (@ffiedrich)
- TAs are handled similarly to students in the selection of a default edition for the homepage (@ffiedrich)
## Fixed
- The correct checkpoint is considered as the last checkpoint (@ffiedrich)
- Checkpoint deletion makes the correct next checkpoint be used instead (@ffiedrich)
- Moving the last skill from a checkpoint makes the checkpoint disappear (@ffiedrich)
- Redundant ghost checkpoint at bottom of page is removed (@ffiedrich)
- After dragging a checkpoint, the menu disappears unless it is hovered (@ffiedrich)
- Path toggle listeners are added to hidden skills on first reveal (@ffiedrich)
- Completion status of revealed hidden skills is updated without page reload (@ffiedrich)
- Revealed skills are not hidden again when required tasks are marked as uncomplete (@rglans)
- Skills do not duplicate after the cancellation of the editing (@bbakos)
- Moving "Optional" box inside the expanded skill (@bbakos)
- Only authorized people can access tasks (@bbakos)
- Submodules are editable after editing modules in the setup page (@bbakos)
- Back button navigates to previously expanded skills instead of navigating to the same page (@ffiedrich)
- Renaming a path does not remove all tasks in path (@ffiedrich)
- Restore previously edited checkpoint when editing a new checkpoint (@bbakos)
- Adding and removing a task on the custom path results in the teacher determined order (@bbakos)
- Task "drag and drop areas" are removed from default skill block after creation or cancellation (@ffiedrich)

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


