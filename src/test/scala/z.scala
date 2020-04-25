package zd.gs.z

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.Matchers

class zSpec extends AnyFreeSpec with Matchers {
  "maybe" in {
    val _1: Maybe[Int] = Some(0)
    val _2: Maybe[Int] = Maybe(0)
    val _3: Just[Int] = Some(0)
    val _4: Just[Int] = Just(0)
    val _5: Just[Int] = 0.just
    val _6: Nothing = None
    val _7: Nothing = Nothing
  }
  "boolean" in {
    assert(true.fold("true", "ignored") === "true")
    assert(false.fold("ignored", "false") === "false")
  }
  "either" in {
    val _8: Left[String,Int] = "left".left[Int]
    val _9: Right[String,Int] = 0.right[String]
    val _d: Either[String,String] = "left".left[Int].coerceRight[String]
    val _e: Either[Int,Int] = 0.right[String].coerceLeft[Int]
    assert(Right("").ensure("is empty")(_.nonEmpty) === Left("is empty"))
    assert(Left[String,String]("").ensure("is empty")(_.nonEmpty) === Left(""))
    assert(Left(0).leftMap(_ + 1) === Left(1))
    assert(Right[Int,Int](0).leftMap(_ + 1) === Right(0))
    Left(0).orElse(Right(1)) shouldBe (Right(1))
  }
  "sequence" in {
    val _a: Either[String,Vector[Int]] = Stream(1.right,"2".left,3.right).sequenceU
    val _b: Either[String,Unit] = Stream(1.right,"2".left,3.right).sequence_
    val _c: Option[List[Int]] = List(1.just,Nothing,3.just).sequenceU
  }
  "apply" in {
    def f: Int => Int => Int = x => y => x + y
    assert(f `<$>` 2.just <*> 3.just === Some(5))
    assert(f `<$>` 2.just <*> Nothing === Nothing)
  }
  "apply is lazy" in {
    var i = 0
    def f: Int => Int => Int = x => y => x + y
    def y: Maybe[Int] = { i = i + 1; 3.just }
    assert(f `<$>` Nothing <*> y === Nothing)
    assert(i === 0)
  }
}

