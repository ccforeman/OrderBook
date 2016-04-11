# Overview
OrderBook is a simple Limit Order Book simulation.  The OrderBook represents all of the Asks and Bids for a particular company's stock at a given time.  Where an Ask is somebody trying to sell stock at a certain price (an asking price) and a Bid is somebody looking to buy stock at a certain price.  For our purposes, imagine all of the Asks and Bids over a period of time are recorded in a logfile.

Pricer is given an amount of shares of the stock and prints to STDOUT how much it would cost to buy the stock from the available Asks (if the amount of available shares permits), or how much it would money it would make to sell to the available Bids (if the amount of requested shares permits).  It is important to note that Pricer should do this in an optimal way.  For example, buy the cheapest available shares of the stock until it has met the target amount of shares, or sell at greatest price for Bids.  This entire process is done by Pricer reading the aformentioned logfile from STDIN line by line.

# Input
The lines that Pricer reads will be either an Add or a Remove. An Add represents a new Ask or Bid in the Order Book, and a Remove represents an Ask or a Bid either having its quantity reduced or being completely taken out of the Order Book.

# Add
The format of an Add is:

    time A id side price size

Where 'time' represents the time since another arbitrary time in milliseconds at which this order occurs, 'A' shows that this is an Add, 'id' is the unique identifier of this particular order, 'side' determines whether this is a Ask or a Bid, 'price' is the buy or sell price per share depending on the value of 'side', and 'size' represents the number of shares for this order. All fields are delimited by a single space.

# Remove
The format of a Remove is:

    time R id size

Where 'time' represents the time since another arbitrary time in milliseconds at which this order occurs, 'R' shows that this is a Remove, 'id' is the unique identifier of this particular order, and 'size' represents the number of shares being removed from the order.  It is important to note that if this number is smaller than the what is currently available in the Order Book then the order isn't completely removed, just has the number of shares reduced by 'size'. All fields are delimited by a single space.

# Output Format
The format for Pricer's output is:

    time action total

Where 'time' represents the 'time' value of the order that caused this output to occur, 'action' can either be 'B' or 'S' representing a Buy or a Sell, 'total' represents either the total amount of money made if 'action' is 'B', or the total cost incurred if 'action' is 'S'. All fields are delimited by a single space.

# Notes on Implementation
There are a few comments I'd like to make on my implementation of Pricer and OrderBook. I should mention that this project was developed with performance in mind. However, that wasn't the only consideration because I also wanted to highlight my understanding of certain aspects not only of Scala, but general programming principles i.e. recursion.

In the getExpense method, I intentionally used recursion knowing that there can be a considerable cost because of allocation of a new frame on the stack. However, Scala Streams use lazy evalutation of the tail and head and tail are constant time operations.

I chose to use a mutable TreeSet and HashMap to represent the Order Book. The HashMap is essentially used as an index to the Book.  This is because Scala HashMaps have effectively constant time lookup, which is better than the logarathmic lookup provided by the TreeSet. Since every Remove requires a lookup, I considered it a worthy sacrifice. The main reason I used a TreeSet was because it is kept in sorted order, which allowed the getExpense method to work quite well since there was no need to sort the Book or look for a min or max for each iteration.
