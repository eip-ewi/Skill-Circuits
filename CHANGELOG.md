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
- Fixed changing checkpoint name in setup for teachers (@bbakos)
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


