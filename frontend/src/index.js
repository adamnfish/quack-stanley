import './main.css';
import { Main } from './Main.elm';

var app = Main.embed(document.getElementById('root'));


// saved games, should be in own module!
var savedGamesKey = "QS_SAVED_GAMES";

app.ports.fetchSavedGames.subscribe(function() {
    console.log("sending game to Elm");
    var existingGames = JSON.parse(localStorage.getItem(savedGamesKey)) || [];
    // clear out expired games
    var recentGames = filterExpired(existingGames);
    localStorage.setItem(savedGamesKey, JSON.stringify(recentGames));

    console.log("games", recentGames);
    app.ports.savedGames.send(recentGames);
});

app.ports.sendGameToJS.subscribe(function(game) {
    var now = +new Date;
    game.startTime = now;
    // TODO only save if it is a new game ID/player key (prevent duplicates)
    console.log("game sent to JS", game);
    var games = JSON.parse(localStorage.getItem(savedGamesKey)) || [];
    console.log("games:", games);
    games.unshift(game);
    console.log("games after update", games);
    localStorage.setItem(savedGamesKey, JSON.stringify(games));
});

function filterExpired(games) {
    var now = +new Date;
    var maxAge = 1000 * 60 * 60 * 24 * 4; // 4 days
    return games.filter(function(game) {
        return game.startTime > (now - maxAge);
    });
}
