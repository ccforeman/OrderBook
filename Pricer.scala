import Fields._
import scala.collection.mutable.HashMap
import scala.io.Source
import scala.math.BigDecimal

object Pricer {
	
	def main(args: Array[String]) = {
		val start = System.nanoTime()

		if(args.length == 0) {
			System.err.println("No target-size set")
			System.exit(1)
		}

		var book = OrderBook(args(0).toLong)
		val log = Source.stdin

		for(line <- log.getLines()) {
			var input = line.split("\\s")
			book.processInput(input)
		}
		log.close()
		getExecTime(start)
	}

	def getExecTime(start: Long) = {
		val time = System.nanoTime() - start
		println("Execution time: " + time / 1000000000.0)
		println(11190024 / (time / 1000000000.0))
	}

}