import './main.css';
import { Elm } from './Main.elm';

var app = Elm.Main.init({
    node: document.getElementById('root'),
    flags: {
        apiRoot: deriveApiRoot(document.location)
    }
});


// saved games, should be in own module!
var savedGamesKey = "QS_SAVED_GAMES";

app.ports.fetchSavedGames.subscribe(function() {
    console.log("sending game to Elm");
    var existingGames = JSON.parse(localStorage.getItem(savedGamesKey)) || [];
    // clear out expired games
    var recentGames = filterExpired(existingGames);
    localStorage.setItem(savedGamesKey, JSON.stringify(recentGames));

    app.ports.savedGames.send(recentGames);
});

app.ports.sendGameToJS.subscribe(function(game) {
    var now = +new Date;
    game.startTime = now;
    var games = JSON.parse(localStorage.getItem(savedGamesKey)) || [];
    // remove matching games before saving to deduplicate
    var filteredGames = removeGame(games, game);
    filteredGames.unshift(game);
    localStorage.setItem(savedGamesKey, JSON.stringify(filteredGames));
});

app.ports.removeSavedGame.subscribe(function(game) {
    var existingGames = JSON.parse(localStorage.getItem(savedGamesKey)) || [];
    // clear out expired games
    var filteredGames = removeGame(existingGames, game);
    localStorage.setItem(savedGamesKey, JSON.stringify(filteredGames));

    app.ports.savedGames.send(filteredGames);
});

function filterExpired(games) {
    var now = +new Date;
    var maxAge = 1000 * 60 * 60 * 24 * 4; // 4 days
    return games.filter(function(game) {
        return game.startTime > (now - maxAge);
    });
}

function removeGame(games, game) {
    return games.filter(function(savedGame) {
        var match = savedGame.gameId == game.gameId && savedGame.playerKey == game.playerKey
        return !match;
    });
}

function deriveApiRoot(location) {
    var devHost = /[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}/;
    if (location.hostname === "localhost" || devHost.test(location.hostname)) {
        return location.protocol + "//" + location.hostname + ":9001/api";
    } else {
        // hard-coded for now
        // TODO: move API route to /api in CloudFormation
        return "https://api.quackstanley.net/";
    }
}
