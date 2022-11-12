package main

import akka.actor.typed.Behavior
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Promise}
import org.mockito.IdiomaticMockito

import scala.util.{Failure, Success, Try}

object Main extends IdiomaticMockito {
  def main(args: Array[String]): Unit = {
    withObjectMocked[FooService.type] {
      // mock `FooService.doit()`: The real method throws a `RuntimeException` and should never be called during tests
      FooService.doit() returns {
        "mock result"
      }

      /* The mocked `FooService.doit()` method is invoked by the `FooActor.handleDoit()` method only.
        I now define two test setups that both trigger the `FooActor.handleDoit()` method:
        (1) runSimple: call `FooActor.handleDoit()` directly
        (2) runActor: setup an actor system and send the `Doit` message, which calls `FooActor.handleDoit()`

        In both cases `FooActor.handleDoit()` will publish it's result via the `FooActor.outcome` promise.
        Finally - within the `withObjectMocked` context - I wait for the result of the promise and print it.

        Since both are executed within the `withObjectMocked` context, we should expect that both setups will call the
        mocked `FooService.doit()`
       */

      // test setup 1: call `FooActor.handleDoit()` directly
      def runSimple(): Try[String] = {
        FooActor.handleDoit()
        val result: Try[String] = Await.result(FooActor.outcome.future, 1.seconds)
        result
      }

      // test setup 2: setup an actor system and send the `Doit` message, which calls `FooActor.handleDoit()`
      def runActor(): Try[String] = {
        val system: ActorSystem[FooActor.Doit.type] = ActorSystem(FooActor(), "FooSystem")
        // trigger actor  to call `handleDoit`
        system ! FooActor.Doit
        // wait for `outcome` future. The 'real' `FooService.doit` impl results in a `Failure`
        val result: Try[String] = Await.result(FooActor.outcome.future, 1.seconds)
        system.terminate()
        result
      }

      // val result: Try[String] = runSimple()
      val result: Try[String] = runActor()

      result match {
        case Success(res) => println(f"finished with result: $res")
        case Failure(ex) => println(f"failed with exception: ${ex.getMessage}")
      }

      // runSimple prints: finished with result: mock result
      // runActor prints: failed with exception: executed real impl!!!
    }
  }
}

object FooService {
  def doit(): String = {
    // I don't want this to be executed in my tests
    throw new RuntimeException(f"executed real impl!!!")
  }
}

object FooActor {
  val outcome: Promise[Try[String]] = Promise[Try[String]]()

  case object Doit

  def apply(): Behavior[Doit.type] = Behaviors.receiveMessage { _ =>
    handleDoit()
    Behaviors.same
  }

  // moved out actual doit behavior so I can compare calling it directly with calling it from the actor
  def handleDoit(): Unit = {
    try {
      // invoke `FooService.doit()` if mock works correctly it should return the "mock result"
      // otherwise the `RuntimeException` from the real exception will be thrown
      val res = FooService.doit()
      outcome.success(Success(res))
    } catch {
      case ex: RuntimeException =>
        outcome.success(Failure(ex))
    }
  }
}
