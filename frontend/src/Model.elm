module Model exposing
    ( Model, PlayerState, PlayerInfo, PlayerSummary, Registered, NewGame, SavedGame
    , ApiResponse (..), ApiError, Lifecycle(..), PitchStatus (..))

import Time exposing (Time)


type Lifecycle
    = Welcome               -- initial screen (backend awake)
    | Create                -- create new game (gameName, screenName, loading)
        CreateState
    | CreatorWaiting        -- waiting for game to start (will have game and screen name)
        String
    | Join                  -- join existing game (gameId, screenName)
        JoinState
    | Waiting               -- waiting for game to start (will have game and screen name)
    | Rejoining             -- reconnecting to a game (will have game and screen name)
        SavedGame
    | Starting              -- triggered game start, waiting for API to complete
    | Spectating            -- game has started, player is spectating (and can choose words)
        ( List String )
    | Pitching              -- player is pitching two cards from hand
        String String PitchStatus Bool
    | ChooseRole            -- player is given two roles to choose from
    | BecomingBuyer         -- player would like to be the buyer, asking API
    | Buying                -- player is the buyer, will have a role
        String
    | AwardingPoint         -- telling API to award role to named player
        String String
    | Error                 -- error that isn't yet handled
        ( List String )

type alias PlayerState =
    { gameId : String
    , gameName : String
    , screenName : String
    , hand : List String
    , discardedWords : List String
    , role : Maybe String -- has role if current player
    , points : List String
    }

type alias Model =
    { lifecycle : Lifecycle
    , backendAwake : Bool
    , time : Time
    , savedGames : List SavedGame
    , state : Maybe PlayerState
    , playerKey : Maybe String
    , isCreator : Bool
    , opponents : List PlayerSummary
    , errs : List String
    }

-- Lifecycles

type PitchStatus
    = NoCards
    | OneCard
    | TwoCards

type alias CreateState =
    { gameName : String
    , screenName : String
    , loading : Bool
    , errors : List ApiError
    }

type alias JoinState =
    { gameCode : String
    , screenName : String
    , loading : Bool
    , errors : List ApiError
    }

-- Persistence

type alias SavedGame =
    { gameId : String
    , gameName : String
    , playerKey : String
    , screenName : String
    , startTime : Float
    }

-- API responses

type alias ApiError =
    { message : String
    , context : Maybe String
    }

type ApiResponse a
    = ApiOk a
    | ApiErr ( List ApiError )

type alias PlayerInfo =
    { state : PlayerState
    , started : Bool
    , opponents : List PlayerSummary
    }

type alias PlayerSummary =
    { screenName : String
    , points : List String
    }

type alias NewGame =
    { state : PlayerState
    , playerKey : String
    , gameCode : String
    }

type alias Registered =
    { state : PlayerState
    , playerKey : String
    }
