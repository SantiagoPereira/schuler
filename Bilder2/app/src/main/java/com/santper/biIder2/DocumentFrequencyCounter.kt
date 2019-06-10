package com.santper.biIder2

import edu.stanford.nlp.io.IOUtils
import edu.stanford.nlp.io.ReaderInputStream
import edu.stanford.nlp.ling.HasWord
import edu.stanford.nlp.ling.TaggedWord
import edu.stanford.nlp.process.DocumentPreprocessor
import edu.stanford.nlp.process.TokenizerFactory
import edu.stanford.nlp.stats.ClassicCounter
import edu.stanford.nlp.stats.Counter
import edu.stanford.nlp.tagger.maxent.MaxentTagger
import edu.stanford.nlp.trees.international.spanish.SpanishTreebankLanguagePack
import edu.stanford.nlp.util.Factory
import edu.stanford.nlp.util.XMLUtils
import edu.stanford.nlp.util.logging.Redwood

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import org.xml.sax.SAXException

import javax.xml.parsers.DocumentBuilder
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectOutputStream
import java.io.Reader
import java.io.StringReader
import java.io.StringWriter
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.Map
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.regex.Pattern

class DocumentFrequencyCounter : Counter<String> {

    override fun getFactory(): Factory<Counter<String>>? {
        return null
    }

    override fun setDefaultReturnValue(rv: Double) {

    }

    override fun defaultReturnValue(): Double {
        return 0.0
    }

    override fun getCount(key: Any): Double {
        return 0.0
    }

    override fun setCount(key: String, value: Double) {

    }

    override fun incrementCount(key: String, value: Double): Double {
        return 0.0
    }

    override fun incrementCount(key: String): Double {
        return 0.0
    }

    override fun decrementCount(key: String, value: Double): Double {
        return 0.0
    }

    override fun decrementCount(key: String): Double {
        return 0.0
    }

    override fun logIncrementCount(key: String, value: Double): Double {
        return 0.0
    }

    override fun addAll(counter: Counter<String>) {

    }

    override fun remove(key: String): Double {
        return 0.0
    }

    override fun containsKey(key: String): Boolean {
        return false
    }

    override fun keySet(): Set<String>? {
        return null
    }

    override fun values(): Collection<Double>? {
        return null
    }

    override fun entrySet(): MutableSet<MutableMap.MutableEntry<String, Double>>? {
        return null
    }

    override fun clear() {

    }

    override fun size(): Int {
        return 0
    }

    override fun totalCount(): Double {
        return 0.0
    }

    override fun prettyLog(channels: Redwood.RedwoodChannels, description: String) {

    }

    private class FileIDFBuilder(private val file: File) : Callable<Counter<String>> {


        @Throws(Exception::class)
        override fun call(): Counter<String> {
            // We need to hallucinate some overarching document tag.. because the Gigaword files don't
            // have them :/
            var fileContents = IOUtils.slurpFile(file)
            fileContents = "<docs>$fileContents</docs>"

            return getIDFMapForFile(StringReader(fileContents))
        }
    }

    companion object {

        private val tagger = MaxentTagger("edu/stanford/nlp/models/pos-tagger/spanish/spanish-distsim.tagger")

        private val MAX_SENTENCE_LENGTH = 100

        private val headingSeparator = Pattern.compile("[-=]{3,}")
        private val paragraphMarker = Pattern.compile("</?(?:TEXT|P)>(\n|$)")

        private val tlp = SpanishTreebankLanguagePack()
        private val tokenizerFactory = tlp.tokenizerFactory

        /**
         * Get an IDF map for the given document string.
         *
         * @param document
         * @return
         */

        private fun getIDFMapForDocument(document: String): Counter<String> {
            var document = document
            // Clean up -- remove some Gigaword patterns that slow things down
            // / don't help anything
            document = headingSeparator.matcher(document).replaceAll("")

            val preprocessor = DocumentPreprocessor(StringReader(document))
            preprocessor.setTokenizerFactory(tokenizerFactory)

            val idfMap = ClassicCounter<String>()
            for (sentence in preprocessor) {
                if (sentence.size > MAX_SENTENCE_LENGTH)
                    continue

                val tagged = tagger.tagSentence(sentence)

                for (w in tagged) {
                    if (w.tag().startsWith("n"))
                        idfMap.incrementCount(w.word())
                }
            }

            return idfMap
        }

        private val TAG_DOCUMENT = "DOC"
        private val TAG_TEXT = "TEXT"

        @Throws(TransformerException::class)
        private fun getFullTextContent(e: Element): String {
            val transFactory = TransformerFactory.newInstance()
            val transformer = transFactory.newTransformer()
            val buffer = StringWriter()
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
            transformer.transform(DOMSource(e),
                    StreamResult(buffer))
            var str = buffer.toString()

            // Remove paragraph markers
            str = paragraphMarker.matcher(str).replaceAll("")

            return str
        }

        @Throws(SAXException::class, IOException::class, TransformerException::class)
        private fun getIDFMapForFile(file: Reader): Counter<String> {

            val parser = XMLUtils.getXmlParser()
            val xml = parser.parse(ReaderInputStream(file))
            val docNodes = xml.documentElement.getElementsByTagName(TAG_DOCUMENT)

            var doc: Element
            val idfMap = ClassicCounter<String>()
            for (i in 0 until docNodes.length) {
                doc = docNodes.item(i) as Element
                val texts = doc.getElementsByTagName(TAG_TEXT)
                assert(texts.length == 1)

                val text = texts.item(0) as Element
                val textContent = getFullTextContent(text)

                idfMap.addAll(getIDFMapForDocument(textContent))

                // Increment magic counter
                idfMap.incrementCount("__all__")
            }

            return idfMap
        }

        private val OUT_FILE = "df-counts.ser"
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")

        @Throws(InterruptedException::class, ExecutionException::class, IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
            val futures = ArrayList<Future<Counter<String>>>()

            for (filePath in args)
                futures.add(pool.submit(FileIDFBuilder(File(filePath))))

            var finished = 0
            val overall = ClassicCounter<String>()

            for (future in futures) {
                System.err.printf("%s: Polling future #%d / %d%n",
                        dateFormat.format(Date()), finished + 1, args.size)
                val result = future.get()
                finished++
                System.err.printf("%s: Finished future #%d / %d%n",
                        dateFormat.format(Date()), finished, args.size)

                System.err.printf("\tMerging counter.. ")
                overall.addAll(result)
                System.err.printf("done.%n")
            }
            pool.shutdown()

            System.err.printf("\n%s: Saving to '%s'.. ", dateFormat.format(Date()),
                    OUT_FILE)
            var oos: ObjectOutputStream? = null
            try {
                oos = ObjectOutputStream(FileOutputStream(OUT_FILE))
            } catch (e: IOException) {
                e.printStackTrace()
            }

            oos!!.writeObject(overall)
            System.err.printf("done.%n")
        }
    }

}