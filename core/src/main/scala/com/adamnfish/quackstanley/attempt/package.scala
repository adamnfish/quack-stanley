package com.adamnfish.quackstanley

import cats.data.EitherT
import cats.effect.IO

package object attempt {
  type Attempt[A] = EitherT[IO, FailedAttempt, A]
}
