import scala.collection.mutable.HashMap
import scala.io.Source
import scala.math.BigDecimal
import Fields._

case class BookData(var time: Long,
					val id: String,
					val side: String,
					val price: BigDecimal,
					var size: Long
					) {
	
	def isBuy = side == "B"

}

object Fields {
	val Time   = 0
	val Action = 1
	val Id     = 2
	val Side   = 3
	val Price  = 4
	val Size   = 5
}

object BookData {
	
	def main(args: Array[String]) = {
		val start = System.nanoTime()
		if(args.length == 0) {
			System.err.println("No target-size set")
			System.exit(1)
		}

		var buyBook = new Book("B", args(0).toLong)
		var sellBook = new Book("S", args(0).toLong)
		val log = Source.stdin

		try {
			for(line <- log.getLines()) {
				var input = line.split(' ')

				input(Action) match {
					case "A" => input(Side) match {
						case "B" => buyBook.add(buyBook.newOrder(input))
						case "S" => sellBook.add(sellBook.newOrder(input))
					}
					case "R" => buyBook.book.get(input(Id)) match {
						case Some(b) => buyBook.remove(input(Id), input(Side).toLong, input(Time).toLong)
						case None => sellBook.remove(input(Id), input(Side).toLong, input(Time).toLong)
					}
				}
			}
		} finally {
			log.close()
		}
		
		getExecTime(start)
	}

	def getExecTime(start: Long) = {
		val time = System.nanoTime() - start
		println("Execution time: " + time / 1000000000.0)
		println(11190024 / (time / 1000000000.0))
	}

}