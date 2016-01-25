import Fields._
import scala.io.Source

/*
	Pricer reads from stdin line by line that is either an add:

	   timestamp A order-id side price size

	Or a remove:

	   timestamp R order-id size

	Output should be:

	   timestamp action total

*/

object Pricer {
	
	def main(args: Array[String]) = {
		// Target-size must be included
		if(args.length ==  0) {
			System.err.println("Invalid number of arguments.")
			System.exit(-1)
		}

		// Book instantiation requires target-size
		var book = OrderBook(args(0).toLong)
		val log = Source.stdin

		try {
			for(line <- log.getLines()) {
				var input = line.split("\\s")
				book.processInput(input)
			}
		} catch {
			case e: Exception => System.err.println("Warning: Encountered error in input.")
		}finally {
			log.close()
		}
	}
}
