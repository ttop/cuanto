package cuanto

/**
 * Created by IntelliJ IDEA.
 * User: Todd Wells
 * Date: May 5, 2008
 * Time: 9:10:32 PM
 * 
 */
class WordGenerator {
	Set wordSet
	static Random rand
	def wordList = []

	WordGenerator() {
		rand = new Random()
		words = words.replaceAll("\\p{Punct}", "")
		String [] allwords = words.split("\\s+")
		wordSet = new HashSet()
		for (word in allwords) {
			wordSet << word
		}

		wordList.addAll(wordSet)
	}

	String getWord() {
		wordList[rand.nextInt(wordList.size())]
	}

	String getSentence(maxNumWords) {
		def sentence = new String()
		int numWords = rand.nextInt(maxNumWords)
		for (x in 0..numWords){
			sentence += getWord()
			if (x < numWords) {
				sentence += " "
			}
		}
		return sentence
	}

	static words ='''
Still looking for the perfect dish to serve at that upcoming Mother's Day brunch? How about Blueberry Cream Cheese Braid? I made this last weekend, and it was awesome. I'm not really in the habit of making extravagant breads such as this, but I might just need to start. Although it looks complicated, this bread is actually pretty easy to put together, and much of the work can be done the night before. The dough is much easier to work with than a regular bread dough because it contains butter which keeps it from being too sticky during the kneading process. The end result is a soft, rich, slightly sweet bread. The blueberry and cream cheese filling is wonderful, although nearly any filling would be good here. Poppyseed with rum-soaked raisins or lemon curd with sweetened ricotta cheese sound like excellent alternatives to me. Or, like the recipe suggests, you could even do a savory version with mushrooms and cheese. Yum!

This recipe comes from a really great baking website called The Fresh Loaf. If you are interested in learning to make delicious bread, be sure to check out this site. They have wonderful written lessons for beginners and even a few helpful videos. There are also book reviews, a community forum, baker blogs, and plenty of recipes for scrumptious-looking breads (and some desserts too.) All of the recipes have step-by-step instructions with pictures. So far, I have only had a chance to try the Daily Bread and the Blueberry Cream Cheese Braid. My sister made the Cinnamon Raisin Oatmeal Bread, and she said it was really, really good. It's enough to make me want to buy a huge jar of yeast and spend each day baking a new kind of bread!
'''
}