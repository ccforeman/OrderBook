import Fields._
import scala.io.Source

object Pricer {
	
	def main(args: Array[String]) = {
		// Target-size must be included
		if(args.length ==  0) {
			System.err.println("Invalid number of arguments.")
			System.exit(-1)
		}

		var book = OrderBook(args(0).toLong)
		val log = Source.stdin

		try {
			for(line <- log.getLines()) {
				var input = line.split("\\s")
				book.processInput(input)
			}
		} finally {
			log.close()
		}
	}
}
