# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.5.6] - 2021-01-28
### Changed
- Added game state information to GameDTO

## [1.5.5] - 2021-01-10
### Changed
- Change premade wall configurations to truly randomly generated ones

### Fixed
- Deploy script now waits for the server port to be free before restarting the server

## [1.5.4] - 2021-01-08
### Changed
- Level 2 and higher: All wall configurations are now randomly selected
- Level 3 and higher: Reduced initial moves from 30 to 20

### Fixed
- Deploy script allows the running server to quit before launching the new instance

## [1.5.3] - 2021-01-08
### Changed
- Included AI output in logs
- Collapsed level 2 variants into one choice, selecting of the variants randomly

## [1.5.2] - 2021-01-06
### Added
- Added variants of level 2 with different wall configurations

### Fixed
- Fixed AI integration

## [1.5.1] - 2020-12-12
### Changed
- Count SKIP actions as used moves

## [1.5.0] - 2020-12-11
### Added
- Introduced AIController allowing to run AIs 

## [1.4.0] - 2020-12-10
### Added
- The action rate of a game can be limited in order to watch AIs play

### Changed
- Added a move limit for levels 1 and 2
- Merged Player classes, eliminating the need for a separate class with secret goal rules

## [1.2.0] - 2020-11-29
### Added
- Symbol messages

## [1.1.0] - 2020-11-16
### Added
- New endpoint /game/byPlayerName
- New endpoint /gamesss
