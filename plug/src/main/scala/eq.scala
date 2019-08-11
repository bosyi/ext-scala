package zd.gs.plug

import scala.tools.nsc.{Global, Phase}
import scala.tools.nsc.plugins.{Plugin, PluginComponent}

class EqPlugin(override val global: Global) extends Plugin {
  override val name = "eq"
  override val description = "equals only for same type"
  override val components = List[PluginComponent](new Component(global))

  class Component(val global: Global) extends PluginComponent {
    import global._
    import definitions.ArrayClass
    import nme.EQ
    import nme.NE
    val phaseName = "eq"
    val runsAfter = List[String]("typer")
    override val runsBefore = List[String]("patmat")
    def newPhase(prev: Phase): Phase = new StdPhase(prev) {
      override def apply(unit: CompilationUnit): Unit = {
        global.reporter.echo("Checking == and !=")
        unit.body.foreach {
          case Apply(Select(Literal(Constant(null)), op), List(r)) => // ok
          case Apply(Select(l, op), List(Literal(Constant(null)))) => // ok
          case tree@Apply(Select(l, EQ), List(r)) if l.tpe.dealiasWiden.typeConstructor =:= ArrayClass.selfType.typeConstructor && r.tpe.dealiasWiden.typeConstructor =:= ArrayClass.selfType.typeConstructor =>
            global.reporter.error(tree.pos, s"comparing ${l.tpe.dealiasWiden} with ${r.tpe.dealiasWiden}; use java.util.Arrays.equals(${if (l.symbol.asTerm.isStable) l.symbol.asTerm.name.toString else "_"}, ${if (r.symbol.asTerm.isStable) r.symbol.asTerm.name.toString else "_"}) instead")
          case Apply(Select(l, EQ), List(r)) if l.tpe.dealiasWiden =:= r.tpe.dealiasWiden => // ok
          case tree@Apply(Select(l, EQ), List(r)) =>
            global.reporter.error(tree.pos, s"comparing ${l.tpe.dealiasWiden} with ${r.tpe.dealiasWiden}")
          case Apply(Select(l, NE), List(r)) if l.tpe.dealiasWiden =:= r.tpe.dealiasWiden => // ok
          case tree@Apply(Select(l, NE), List(r)) =>
            global.reporter.error(tree.pos, s"comparing ${l.tpe.dealiasWiden} with ${r.tpe.dealiasWiden}")
          case _ => // skip
        }
      }
    }
  }
}
