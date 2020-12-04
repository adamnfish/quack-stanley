package com.adamnfish.quackstanley.e2e

import com.adamnfish.quackstanley.QuackStanley._
import com.adamnfish.quackstanley.models.{AwardPoint, BecomeBuyer, Config, CreateGame, FinishPitch, Ping, RegisterHost, RegisterPlayer, Role, SetupGame, StartGame, Word}
import com.adamnfish.quackstanley.{AttemptValues, HaveMatchers, TestPersistence}
import org.scalatest.{OneInstancePerTest, OptionValues}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext.Implicits.global


/**
  * Take the API through realistic games, just like the web frontend would IRL
  */
class EndToEndTests extends AnyFreeSpec with Matchers
  with OneInstancePerTest with AttemptValues with OptionValues with HaveMatchers {

  val persistence = new TestPersistence
  val testConfig = Config("test", persistence)

  "standard game" in {
    val hostRegistered = createGame(
      CreateGame("host", "game name"), testConfig
    ).value()
    val p1Registered = registerPlayer(
      RegisterPlayer(hostRegistered.gameCode, "player one"), testConfig
    ).value()
    val p2Registered = registerPlayer(
      RegisterPlayer(hostRegistered.gameCode, "player two"), testConfig
    ).value()

    // TODO: lobby ping checks here

    val hostInfo = startGame(
      StartGame(hostRegistered.state.gameId, hostRegistered.playerKey), testConfig
    ).value()

    val p1Info = ping(
      Ping(p1Registered.state.gameId, p1Registered.playerKey), testConfig
    ).value()
    val p2Info = ping(
      Ping(p2Registered.state.gameId, p2Registered.playerKey), testConfig
    ).value()

    val hostIsBuyer = becomeBuyer(
      BecomeBuyer(hostRegistered.state.gameId, hostRegistered.playerKey), testConfig
    ).value()
    val role = hostIsBuyer.round.value.role

    val p1Words = (p1Info.state.hand(1), p1Info.state.hand(2))
    val p1Info2 = finishPitch(
      FinishPitch(p1Registered.state.gameId, p1Registered.playerKey, p1Words), testConfig
    ).value()

    val p2Words = (p2Info.state.hand(1), p2Info.state.hand(2))
    val p2Info2 = finishPitch(
      FinishPitch(p2Registered.state.gameId, p2Registered.playerKey, p2Words), testConfig
    ).value()

    // check player hands have been refreshed
    p1Info.state.hand should not equal p1Info2.state.hand
    p2Info.state.hand should not equal p2Info2.state.hand

    val hostInfo2 = ping(
      Ping(hostRegistered.state.gameId, hostRegistered.playerKey), testConfig
    ).value()

    // check pitch submissions are visible to host
    hostInfo2.round.value.products.get(p1Info.state.screenName).value shouldEqual p1Words
    hostInfo2.round.value.products.get(p2Info.state.screenName).value shouldEqual p2Words

    val finalHostInfo = awardPoint(
      AwardPoint(hostRegistered.state.gameId, hostRegistered.playerKey, role, p1Info.state.screenName), testConfig
    ).value()
    val finalP1Info = ping(
      Ping(p1Registered.state.gameId, p1Registered.playerKey), testConfig
    ).value()
    val finalP2Info = ping(
      Ping(p2Registered.state.gameId, p2Registered.playerKey), testConfig
    ).value()

    // ensure host can see awarded point
    finalHostInfo.state.points shouldBe empty
    finalHostInfo.opponents.count(_.points.nonEmpty) shouldEqual 1
    // ensure p1 can see their own point
    finalP1Info.state.points shouldEqual List(role)
    finalP1Info.opponents.count(_.points.nonEmpty) shouldEqual 0
    // ensure p2 can see awarded point
    finalP2Info.state.points shouldBe empty
    finalP2Info.opponents.count(_.points.nonEmpty) shouldEqual 1
  }

  "an externally created game via setupGame" - {
    "can get a game started" in {
      val newEmptyGame = setupGame(
        SetupGame("game name"), testConfig
      ).value()

      val hostRegistered = registerHost(
        RegisterHost(newEmptyGame.gameCode, newEmptyGame.hostCode, "host"), testConfig
      ).value()
      registerPlayer(
        RegisterPlayer(newEmptyGame.gameCode, "player one"), testConfig
      ).value()
      registerPlayer(
        RegisterPlayer(newEmptyGame.gameCode, "player two"), testConfig
      ).value()

      val hostInfo = startGame(
        StartGame(hostRegistered.state.gameId, hostRegistered.playerKey), testConfig
      ).value()

      hostInfo.state.screenName shouldEqual "host"
      hostInfo.opponents.map(_.screenName).toSet shouldEqual Set("player one", "player two")
    }

    "works if the host is not the first to join" in {
      val newEmptyGame = setupGame(
        SetupGame("game name"), testConfig
      ).value()
      registerPlayer(
        RegisterPlayer(newEmptyGame.gameCode, "player one"), testConfig
      ).value()
      registerPlayer(
        RegisterPlayer(newEmptyGame.gameCode, "player two"), testConfig
      ).value()

      // the host registers after other players have registered
      val hostRegistered = registerHost(
        RegisterHost(newEmptyGame.gameCode, newEmptyGame.hostCode, "host"), testConfig
      ).value()

      val hostInfo = startGame(
        StartGame(hostRegistered.state.gameId, hostRegistered.playerKey), testConfig
      ).value()

      hostInfo.state.screenName shouldEqual "host"
      hostInfo.opponents.map(_.screenName).toSet shouldEqual Set("player one", "player two")
    }
  }
}
