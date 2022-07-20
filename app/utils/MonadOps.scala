package utils

import cats.implicits._
import cats.Monad

import scala.language.higherKinds

object MonadOps {

  implicit class BooleanMonadSyntax[F[_]](fa: F[Boolean])(implicit m: Monad[F]) {

    def &&(that: F[Boolean]): F[Boolean] = fa.flatMap {
      case false => m.pure(false)
      case true  => that
    }
  }
}
