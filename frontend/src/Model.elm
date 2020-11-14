module Model exposing
    ( ApiError
    , ApiResponse(..)
    , Lifecycle(..)
    , Model
    , NewGame
    , PlayerInfo
    , PlayerState
    , PlayerSummary
    , Registered
    , Round
    , SavedGame
    )

import Dict exposing (Dict)


type Lifecycle
    = Welcome -- initial screen (backend awake)
      -- create new game (gameName, screenName, loading)
    | Create CreateState
      -- waiting for game to start (will have game and screen name)
    | CreatorWaiting String (List ApiError)
      -- join existing game (gameId, screenName)
    | Join JoinState
      -- waiting for game to start (will have game and screen name)
    | Waiting
      -- reconnecting to a game (will have game and screen name)
    | Rejoining SavedGame
      -- triggered game start, waiting for API to complete
    | Starting
      -- game has started, player is spectating (and can choose words)
    | Spectating (List String) (List ApiError)
      -- player is pitching two cards from hand
    | Pitching String String Bool
      -- player is given two roles to choose from
    | ChooseRole
      -- player would like to be the buyer, asking API
    | BecomingBuyer
      -- player does not want to be the buyer, asking API
    | RelinquishingBuyer
      -- player is the buyer, will have a role
    | Buying String
      -- telling API to award role to named player
    | AwardingPoint String String
      -- error that isn't yet handled
    | Error (List String)


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
    , time : Int
    , savedGames : List SavedGame
    , state : Maybe PlayerState
    , playerKey : Maybe String
    , isCreator : Bool
    , opponents : List PlayerSummary
    , round : Maybe Round
    , apiRoot : String
    }



-- Lifecycles


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
    , startTime : Int
    }



-- API responses


type alias ApiError =
    { message : String
    , context : Maybe String
    }


type ApiResponse a
    = ApiOk a
    | ApiErr (List ApiError)


type alias Round =
    { buyer : String
    , role : String
    , products : Dict String (List String)
    }


type alias PlayerInfo =
    { state : PlayerState
    , started : Bool
    , opponents : List PlayerSummary
    , round : Maybe Round
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
