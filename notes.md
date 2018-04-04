TODO:

UI
+ breadcrumb nav
+ 2x3 grid of words on spectate page
+ smaller headers
  + spectate header especially is bad
+ "select two words" on spectate
  + other basic instructions!
+ screen to show full standings (with words)
+ Say on spectating page what the currently active role is?
  + and who has that role?

GENERAL
* design UI as a game
+ additional navigation (e.g. leave game / rejoin from welcome)
  * (see also "breadcrumbs, above)
+ restart game from saved state (e.g. local storage) on welcome screen?
  * https://stackoverflow.com/questions/33697444/data-persistence-in-elm
+ stop being buyer without awarding point?
* display fixes
  + discarded words
* change model
* frontend tests
  * https://medium.com/@_rchaves_/testing-in-elm-93ad05ee1832
* help pages

BUILD / OVERHEAD
+ PROD-ready
  + CloudFront
  + static assets in a bucket
  + domain and cert
+ codepipeline / codebuild / codedeploy CI
+ switch to elm-live instead of elm-reactor with hot reloading for frontend dev server (still use something for local API)
  * https://github.com/architectcodes/elm-live


LONG TERM
+ refactor frontend to use navigation / SPA style
  * https://dev.to/rtfeldman/tour-of-an-open-source-elm-spa


DONE

UI
- icon on join game button

GENERAL
- otherPlayers in API should only return *other players*
* display fixes
  - mobile zoom
  - show own name
  - show game name
  - unclickable self
  - display own points
  - display other players' points
* flow fixes
  + back from create/join
  + back from bad game code (failed join)
* flow fixes
- styles (materialize)
* cannot become buyer if someone else is?
* dev server with assets
* varying poll interval (depending on lifecycle)
* better join mechanic
  + prefix support in API
  + find unique prefix in API ( -> model)
  + prefixed game key display
- refactor views
- join game, password? (if required)

* elm SPA frontend
  - create / join
    + create takes password
	+ QR code to join? Or tapphones with Android beam?
	+ join scans QR code, inputs code?
  - joiners put their screenname in
  - creator arranges joined people in order
  - configures game (e.g. time limits)
  - creator 'starts' which deals cards into the game state
  - hits lambda backend for application state reads / updates

* S3 state
  - /prefix/<gameid>/game.json (GameState)
  - /prefix/<gameid>/<playerkey>.json (PlayerState)
  - stores JSON blob of game state, which contains player keys to look up player data
  - there's a race condition on registering :-/
    + Could write players only when the game starts, using ls? <- this sounds sensible
  - players, player cards, player scores (and won cards), current product
  - also player's previous cards so they aren't re-dealt
  - only things that affect other player's rows are awarding win and passing the pitch I think?

* Lambda backend
 - hard-coded list of roles and words as jar resource?
 - lambda randomly deals out of this


Ensure:
* ALL data auto-deleted after a couple of hours?

