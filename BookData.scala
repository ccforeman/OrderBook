import scala.collection.mutable.HashMap
import scala.io.Source
import scala.math.BigDecimal

class BookData(a: Long, b: String, c: String, d: Long, side: String) {
	var time   = a
	var id     = b
	var price  = BigDecimal(c)
	var shares = d
	var isBuy = side match { case "B" => true; case _ => false }
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
			val input = Source.stdin

			try {
				for(line <- input.getLines()) {
					var a = line.split(' ')

					a(1) match {
						case "A" => a(3) match {
							case "B" => buyBook.add(buyBook.newOrder(a))
							case "S" => sellBook.add(sellBook.newOrder(a))
						}
						case "R" => buyBook.book.get(a(2)) match {
							case Some(b) => buyBook.remove(a(2), a(3).toLong, a(0).toLong)
							case None => sellBook.remove(a(2), a(3).toLong, a(0).toLong)
						}
					}
				}
			} finally {
				input.close()
			}
			
			getExecTime(start)
		}

		def getExecTime(start: Long) = {
			val time = System.nanoTime() - start
			println("Execution time: " + time / 1000000000.0)
			println(11190024 / (time / 1000000000.0))
		}

	}