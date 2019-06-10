package com.santper.biIder2

import android.util.Log

import java.io.File
import java.io.IOException


import edu.stanford.nlp.io.IOUtils
import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.ling.CoreLabel
import edu.stanford.nlp.pipeline.Annotation
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import edu.stanford.nlp.stats.ClassicCounter
import edu.stanford.nlp.stats.Counter
import edu.stanford.nlp.util.CoreMap

import java.io.FileInputStream
import java.io.ObjectInputStream
import java.util.Collections
import java.util.Comparator
import java.util.Properties


class Summarizer(private val dfCounter: Counter<String>) {
    private val numDocuments: Int

    init {
        this.numDocuments = dfCounter.getCount("__all__").toInt()
    }

    private inner class SentenceComparator(private val termFrequencies: Counter<String>) : Comparator<CoreMap> {

        override fun compare(o1: CoreMap, o2: CoreMap): Int {
            return Math.round(score(o2) - score(o1)).toInt()
        }


        private fun score(sentence: CoreMap): Double {
            val tfidf = tfIDFWeights(sentence)

            // Weight by position of sentence in document
            val index = sentence.get(CoreAnnotations.SentenceIndexAnnotation::class.java)
            val indexWeight = 5.0 / index

            return indexWeight * tfidf * 100.0
        }

        private fun tfIDFWeights(sentence: CoreMap): Double {
            var total = 0.0
            for (cl in sentence.get(CoreAnnotations.TokensAnnotation::class.java))
                if (cl.get(CoreAnnotations.PartOfSpeechAnnotation::class.java).startsWith("n"))
                    total += tfIDFWeight(cl.get(CoreAnnotations.TextAnnotation::class.java))

            return total
        }

        private fun tfIDFWeight(word: String): Double {
            if (dfCounter.getCount(word) == 0.0)
                return 0.0

            val tf = 1 + Math.log(termFrequencies.getCount(word))
            val idf = Math.log(numDocuments / (1 + dfCounter.getCount(word)))
            return tf * idf
        }
    }

    private fun rankSentences(sentences: List<CoreMap>, tfs: Counter<String>): List<CoreMap> {
        Collections.sort(sentences, SentenceComparator(tfs))
        return sentences
    }

    fun summarize(document: String, num: Float): String {
        val annotation = pipeline.process(document)
        var sentences = annotation.get(CoreAnnotations.SentencesAnnotation::class.java)

        val tfs = getTermFrequencies(sentences)
        sentences = rankSentences(sentences, tfs)

        val ret = StringBuilder()
        var i = 0
        while (i < sentences.size * num) {
            ret.append(sentences[i])
            ret.append(" ")
            i++
        }
        Log.e("num", sentences.size.toString())
        Log.e("num", (sentences.size * num).toString())
        return ret.toString()

    }

    companion object {

        private val pipeline: StanfordCoreNLP

        init {
            val props = Properties()
            props.setProperty("annotators", "tokenize,ssplit,pos")
            props.setProperty("tokenize.language", "es")
            props.setProperty("pos.model", "edu/stanford/nlp/models/pos-tagger/spanish/spanish-distsim.tagger")

            pipeline = StanfordCoreNLP(props)
        }

        private fun getTermFrequencies(sentences: List<CoreMap>): Counter<String> {
            val ret = ClassicCounter<String>()

            for (sentence in sentences)
                for (cl in sentence.get(CoreAnnotations.TokensAnnotation::class.java))
                    ret.incrementCount(cl.get(CoreAnnotations.TextAnnotation::class.java))

            return ret
        }

        private val DF_COUNTER_PATH = "df-counts.ser"

        @Throws(IOException::class, ClassNotFoundException::class)
        private fun loadDfCounter(path: String): Counter<String> {
            val ois = ObjectInputStream(FileInputStream(path))
            return ois.readObject() as Counter<String>
        }

        @Throws(IOException::class, ClassNotFoundException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val filename = args[0]
            val content = IOUtils.slurpFile(filename)

            val dfCounter = loadDfCounter(DF_COUNTER_PATH)

            val summarizer = Summarizer(dfCounter)
            val result = summarizer.summarize(content, 2f)

            println(result)
        }
    }

}