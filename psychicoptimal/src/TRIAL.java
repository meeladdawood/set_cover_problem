/**	PA1 - psychicoptimal.java
 * 	@author	Chris Jung, Meelad Dawood
 *  @date	October 9, 2017
 *  Dr. Glick, COMP 480
 */

import java.io.*;
import java.util.*;

/**	Class "Ticket"
 * 	Holds information about a ticket generated by the "k" parameter:
 * 	ID for the number generated, HashSet "numbers" for the ticket numbers.
 *  Can be initialized with a specific ID (used for the starting "null" ticket).  
 **/
class Ticket
{
	private static int IDCounter = 0;
	private int ID;  
	private HashSet<Integer> numbers = new HashSet<Integer>();
	public Ticket ()
	{
		this.ID = IDCounter++;
	}
	public Ticket (int id)
	{
		this.ID = id;
	}
	public void addNumber(int a)
	{
		numbers.add(a);
	}
	public int getID() { return this.ID; }
	public HashSet<Integer> getNumbers()
	{
		return numbers;
	}
	public boolean contains (Combination b, int requiredToContain)
	{
		if (this.numbers.isEmpty()) return false;
		int contains = 0;
		for (Integer number: b.getNumbers())
		{
			if (this.numbers.contains(number))
				contains++;
			if (contains >= requiredToContain) 
				return true;
		}
		return false;
	}
}

/**	Class "Combination" 
 * 	Holds information about a combination of numbers, generated by the "j" parameter:
 * 	ID for the number generated, HashSet "numbers" for the combination number.
 */
class Combination 
{ 
	private static int IDCounter = 0; 
	private int ID; 
	private HashSet<Integer> numbers = new HashSet<Integer>();
	public Combination ()
	{
		this.ID = IDCounter++;
	}
	public void addNumber (int n) 
	{
		numbers.add(n);
	}
	public int getID () { return this.ID; }
	public HashSet<Integer> getNumbers()
	{
		return numbers;
	}
}

/**	Class "TicketComparator"
 * 	Compares the Integer values of Strings, returns the comparison result.
 * 	For use when sorting the final results in lex order.
 */
class TicketComparator<E> implements Comparator<E>
{
	public int compare(Object a, Object b)
	{
		String aa = (String)a;
		String bb = (String)b;
		String[] aarr = aa.split(" ");
		String[] barr = bb.split(" ");
		for (int i = 0; i < aarr.length; i++)
		{
			int check1 = Integer.parseInt(aarr[i]);
			int check2 = Integer.parseInt(barr[i]);
			//System.out.println("");
			int toReturn = check1 - check2;
			if (toReturn < 0)
				return -1; 
			else if (toReturn > 0)
				return 1;
		}
		return 0;
	}
}

public class TRIAL 
{
	static int smallest = Integer.MAX_VALUE;
	static LinkedList<Ticket> bestPath = null;
	static ArrayList<Ticket> everything = new ArrayList<Ticket>();
	static ArrayList<Combination> possibilities = new ArrayList<Combination>(); 
	public static void main (String [] args)
	{
		// Get input from file
		int n = 0, k = 0, j = 0, l = 0; 
		String inputName = args[0], outputName = args[1];
		try 
		{
			File inFile = new File(inputName);
			Scanner in = new Scanner(inFile);
			n = in.nextInt(); 
			j = in.nextInt();
			k = in.nextInt(); 
			l = in.nextInt();
			in.close();
		}
		catch (FileNotFoundException d)
		{
			System.out.println("File not found!");
		}
		
		// Sanitize inputs
		if (n < k || k < j || j < l || n == 0 || k == 0 || j == 0 || l == 0) 
		{
			System.out.println("Bad values for n, j, k, l!");
			System.exit(1); 
		}
		
		// Generate Tickets and Combinations based on k, j
		int[] elements = new int[n];
		for (int index = 0; index < n; index++)
			elements[index] = index + 1; 
		
		int [] data = new int [n];
		generateRecursive(elements, data, 0, n-1, 0, k, true);

		int[] givenData = new int[n];
		generateRecursive(elements, givenData, 0, n-1, 0, j, false);
		
		
		// Brute force search all the Combinations and Tickets for working sets. 
		// Store information in "bestPath"
		runningSumBruteForce(new Ticket(-1), null, elements, l);
		
		
		// Collect the results and convert to strings for printing
		ArrayList<String> resultStrings = new ArrayList<String>(); 
		Collections.sort(resultStrings);
		for (Ticket d: bestPath)
		{
			String dd = d.getNumbers().toString();
			dd = dd.replaceAll("([\\[,\\]])", "");
			resultStrings.add(dd);
		}
		resultStrings.sort(new TicketComparator<String>());

		// Print results to output file
		try
		{
			File out = new File (outputName);
			out.createNewFile();
			PrintWriter print = new PrintWriter (out);
			print.println(smallest);
			for (String str : resultStrings)
			{
				print.println(str);
			}
			print.close();
		}
		catch (Exception exception) 
		{
			System.out.println("Unable to print to file");
		}
	}
	
	/** runningSumBruteForce
	 * 	Recursively compares combinations of Ticket objects to a set of Combination objects.
	 * 	Stores the best solutions (fewest number of tickets) in "bestPath" if a new best is encountered.
	 * 	Runs in (O(2^n)).  
	 * 
	 * 	@param root					Ticket object from which to start
	 * 	@param decisions			LinkedList of the included Tickets in this iteration
	 * 	@param data					Data to check
	 * 	@param requiredToContain	Number of Integers required to match to be covered by a Ticket
	 * 	@return						LinkedList of the decisions (if all Combinations covered) or null
	 */
	static LinkedList<Ticket> runningSumBruteForce (Ticket root, LinkedList<Ticket> decisions, int[] data, int requiredToContain)
	{
		if (decisions == null) decisions = new LinkedList<Ticket>();
		
		// Check the combinations on the current list of tickets
		boolean allFound = true;
		for (Combination each:possibilities)
		{
			if (!root.contains(each, requiredToContain))
			{
				boolean foundElsewhere = false;
				for (Ticket checkDecisions:decisions)
				{
					if (checkDecisions.contains(each, requiredToContain))
					{
						foundElsewhere = true;
						break;
					}
				}
				if (!foundElsewhere) allFound = false;
			}
		}
		
		// Not all combinations are covered, call recursively on larger sets
		if (!allFound) 
		{
			for (int index = root.getID() + 1; index < everything.size(); index++)
			{
				LinkedList<Ticket> newDecisions = new LinkedList<Ticket>(decisions);
				if (root.getID() != -1) 
					newDecisions.add(root);
				LinkedList<Ticket> found = runningSumBruteForce(everything.get(index), newDecisions, data, requiredToContain);
				
				// Recursive call has returned, if !null and better than current
				// best, set bestPath to new path and smallest to bestPath.size()
				if (found != null) 
				{
					if (found.size() < smallest)
					{
						bestPath = new LinkedList<Ticket>(found);
						smallest = found.size();
					}
				}
			}
			return null;
		}
		else 
		{
			// All combinations are covered, return the decisions list and root
			decisions.add(root);
			return decisions;
		}
	}
	
	/**	generateRecursive
	 * 	Recursively generates a set of Ticket objects or Combination objects based on values for input.
	 * 	Stores values in "possibilities" or "everything"
	 * 	@param arr		Placeholder array for integers
	 * 	@param data		Data to cover
	 * 	@param start	Starting point for generation
	 * 	@param end		Ending point for generation
	 * 	@param index	Current index
	 * 	@param r		Number until the end (calculated later)
	 * 	@param ticket	true if generating Ticket objects, false if Combination objects
	 */
	static void generateRecursive(int arr[], int data[], int start, int end, int index, int r, boolean ticket)
	{
		if (index == r)
		{
			if (ticket) 
			{
				Ticket toAdd = new Ticket();
				for (int j=0; j<r; j++)
				{
					toAdd.addNumber(data[j]);
				}
				 everything.add(toAdd);	
			}
			else 
			{
				Combination toAdd = new Combination();
				for (int j=0; j<r; j++)
				{
					toAdd.addNumber(data[j]);
				}
				possibilities.add(toAdd);
			}
		}
		for (int i=start; i<=end && end-i+1 >= r-index; i++)
		{
			data[index] = arr[i];
			generateRecursive(arr, data, i+1, end, index+1, r, ticket);
		}
	}
}
