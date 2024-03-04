import java.util.HashMap;
import java.util.Random;

public class LanguageModel {

    // The map of this model.
    // Maps windows to lists of charachter data objects.
    HashMap<String, List> CharDataMap;
    
    // The window length used in this model.
    int windowLength;
    
    // The random number generator used by this model. 
	private Random randomGenerator;

    /** Constructs a language model with the given window length and a given
     *  seed value. Generating texts from this model multiple times with the 
     *  same seed value will produce the same random texts. Good for debugging. */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    /** Constructs a language model with the given window length.
     * Generating texts from this model multiple times will produce
     * different random texts. Good for production. */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /** Builds a language model from the text in the given file (the corpus). */
	public void train(String fileName) {
		In in = new In(fileName);
        String window = "";
        char chr;
        for(int i = 0;i < windowLength ;i++) {
            window = window+in.readChar();
        }
        while(in.isEmpty() == false) {
            chr = in.readChar();
            List probs = CharDataMap.get(window);
            if(probs == null) {
                probs = new List();
                CharDataMap.put(window, probs);
            }
            probs.update(chr);
            window = window.substring(1) + chr; 
        }
        for(List probs:CharDataMap.values()){
            calculateProbabilities(probs);
        }
	}

    // Computes and sets the probabilities (p and cp fields) of all the
	// characters in the given list. */
	public void calculateProbabilities(List probs) {				
		Node current = probs.getFirstNode();
        int countTotal = probs.countTotalLetters();
        double thisCP = 0;
        while (current != null) {
            current.cp.p = (double)current.cp.count/countTotal;
            thisCP += (double)current.cp.count/countTotal;
            current.cp.cp = thisCP;
            current = current.next;
        }
	}

    // Returns a random character from the given probabilities list.
	public char getRandomChar(List probs) {
        calculateProbabilities(probs);
        Node current = probs.getFirstNode();
		double r = randomGenerator.nextDouble();
        int index = 0;
        char chr = 'a';
        while(current.cp.cp < r) {
            index++;
            current = current.next;
        }
        chr = current.cp.chr;
        return chr;
	}

    /**
	 * Generates a random text, based on the probabilities that were learned during training. 
	 * @param initialText - text to start with. If initialText's last substring of size numberOfLetters
	 * doesn't appear as a key in Map, we generate no text and return only the initial text. 
	 * @param numberOfLetters - the size of text to generate
	 * @return the generated text
	 */
	public String generate(String initialText, int textLength) {
		String window = initialText.substring(initialText.length() - windowLength);
        String generated = window;
        if(initialText.length() < windowLength) {
            return initialText;
        }
        while(generated.length() < (windowLength + textLength)) {
            List probs = CharDataMap.get(window);
            if (probs == null) {
               return window;
            }
            char chr = getRandomChar(probs);
            generated += chr;
            window = generated.substring(generated.length() - windowLength);
        }
        return generated;
	}

    /** Returns a string representing the map of this language model. */
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (String key : CharDataMap.keySet()) {
			List keyProbs = CharDataMap.get(key);
			str.append(key + " : " + keyProbs + "\n");
		}
		return str.toString();
	}

    public static void main(String[] args) {
		// Your code goes here
    }
}
