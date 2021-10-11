package ru.skillbranch.skillarticles

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

import org.junit.Assert
import org.junit.Test
import ru.skillbranch.skillarticles.ui.custom.markdown.Element
import ru.skillbranch.skillarticles.ui.custom.markdown.MarkdownParser

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

class UnitTest {
    @Test
    fun parse_list_item() {
        val result = MarkdownParser.parse(unorderedListString)
        val actual = prepare<Element.UnorderedListItem>(result.elements)
        Assert.assertEquals(expectedUnorderedList, actual)

        printResults(actual)
        println("")
        printElements(result.elements)
    }

    @Test
    fun parse_ordered_list_item() {
        val result = MarkdownParser.parse(orderedListString)
        val actualOrderedList = prepare<Element.OrderedListItem>(result.elements)
        val actualLevels = result.elements.spread()
            .filterIsInstance<Element.OrderedListItem>()
            .map { it.order }
        Assert.assertEquals(listOf("1.", "2."), actualLevels)
        Assert.assertEquals(expectedOrderedList, actualOrderedList)
    }

    @Test
    fun parse_header() {
        val result = MarkdownParser.parse(headerString)
        val actual = prepare<Element.Header>(result.elements)
        val actualLevels = result.elements.spread()
            .filterIsInstance<Element.Header>()
            .map{it.level}
        Assert.assertEquals(expectedHeader, actual)
        Assert.assertEquals(listOf(1, 2, 3, 4, 5, 6), actualLevels)
    }

    @Test
    fun parse_quote() {
        val result = MarkdownParser.parse(quoteString)
        val actual = prepare<Element.Quote>(result.elements)
        Assert.assertEquals(expectedQuote, actual)
    }

    @Test
    fun parse_italic() {
        val result = MarkdownParser.parse(italicString)
        val actual = prepare<Element.Italic>(result.elements)
        Assert.assertEquals(expectedItalic, actual)
    }

    @Test
    fun parse_bold() {
        val result = MarkdownParser.parse(boldString)
        val actual = prepare<Element.Bold>(result.elements)
        Assert.assertEquals(expectedBold, actual)
    }

    @Test
    fun parse_strike() {
        val result = MarkdownParser.parse(strikeString)
        val actual = prepare<Element.Strike>(result.elements)
        Assert.assertEquals(expectedStrike, actual)
    }

    @Test
    fun parse_combine() {
        val result = MarkdownParser.parse(combineEmphasisString)
        val actualItalic= prepare<Element.Italic>(result.elements)
        val actualBold= prepare<Element.Bold>(result.elements)
        val actualStrike= prepare<Element.Strike>(result.elements)
        Assert.assertEquals(expectedCombine["italic"], actualItalic)
        Assert.assertEquals(expectedCombine["bold"], actualBold)
        Assert.assertEquals(expectedCombine["strike"], actualStrike)
    }

    @Test
    fun parse_rule() {
        val result = MarkdownParser.parse(ruleString)
        val actual = prepare<Element.Rule>(result.elements)
        Assert.assertEquals(3, actual.size)
    }

    @Test
    fun parse_inline_code() {
        val result = MarkdownParser.parse(inlineString)
        val actual = prepare<Element.InlineCode>(result.elements)
        Assert.assertEquals(expectedInline, actual)
    }

    @Test
    fun parse_multiline_code() {
        val result = MarkdownParser.parse(multilineCode)
        val actual = prepare<Element.BlockCode>(result.elements) //optionally
        Assert.assertEquals(expectedMultilineCode, actual) //optionally
    }

    @Test
    fun parse_link() {
        val result = MarkdownParser.parse(linkString)
        val actual = prepare<Element.Link>(result.elements)
        val actualLink = result.elements.spread()
            .filterIsInstance<Element.Link>()
            .map{it.link}

        Assert.assertEquals(expectedLink["titles"], actual)
        Assert.assertEquals(expectedLink["links"], actualLink)
    }

    @Test
    fun parse_images() {
        val result = MarkdownParser.parse(imagesString)
        val actual = prepare<Element.Image>(result.elements)
        val actualLink = result.elements.spread()
            .filterIsInstance<Element.Image>()
            .map{it.url}

        val actualAlts = result.elements.spread()
            .filterIsInstance<Element.Image>()
            .map{it.alt}

        Assert.assertEquals(expectedImages["titles"], actual)
        Assert.assertEquals(expectedImages["alts"], actualAlts)
        Assert.assertEquals(expectedImages["links"], actualLink)
    }

    @Test
    fun parse_all() {
        val result = MarkdownParser.parse(markdownString)
        val actualUnorderedList = prepare<Element.UnorderedListItem>(result.elements)
        val actualHeaders = prepare<Element.Header>(result.elements)
        val actualQuotes = prepare<Element.Quote>(result.elements)
        val actualItalic = prepare<Element.Italic>(result.elements)
        val actualBold = prepare<Element.Bold>(result.elements)
        val actualStrike = prepare<Element.Strike>(result.elements)
        val actualRule = prepare<Element.Rule>(result.elements)
        val actualInline = prepare<Element.InlineCode>(result.elements)
        val actualLinkTitles = prepare<Element.Link>(result.elements)
        val actualLinks = result.elements.spread()
            .filterIsInstance<Element.Link>()
            .map { it.link }

        Assert.assertEquals(expectedMarkdown["unorderedList"], actualUnorderedList)
        Assert.assertEquals(expectedMarkdown["header"], actualHeaders)
        Assert.assertEquals(expectedMarkdown["quote"], actualQuotes)
        Assert.assertEquals(expectedMarkdown["italic"], actualItalic)
        Assert.assertEquals(expectedMarkdown["bold"], actualBold)
        Assert.assertEquals(expectedMarkdown["strike"], actualStrike)
        Assert.assertEquals(3, actualRule.size)
        Assert.assertEquals(expectedMarkdown["inline"], actualInline)
        Assert.assertEquals(expectedMarkdown["linkTitles"], actualLinkTitles)
        Assert.assertEquals(expectedMarkdown["links"], actualLinks)
    }

    @Test
    fun clear_all() {
        val result = MarkdownParser.clear(markdownString)
        Assert.assertEquals(markdownClearString, result)
    }


    @Test
    fun clear_all_with_optionally() {
        val result = MarkdownParser.clear(markdownString)
        Assert.assertEquals(markdownOptionallyClearString, result)
    }

    //optionally (delete @Ignore fo run)
    @Test
    fun parse_all_with_optionally() {
        val result = MarkdownParser.parse(markdownString)
        val actualUnorderedList = prepare<Element.UnorderedListItem>(result.elements)
        val actualHeaders = prepare<Element.Header>(result.elements)
        val actualQuotes = prepare<Element.Quote>(result.elements)
        val actualItalic = prepare<Element.Italic>(result.elements)
        val actualBold = prepare<Element.Bold>(result.elements)
        val actualStrike = prepare<Element.Strike>(result.elements)
        val actualRule = prepare<Element.Rule>(result.elements)
        val actualInline = prepare<Element.InlineCode>(result.elements)
        val actualLinkTitles = prepare<Element.Link>(result.elements)
        val actualLinks = result.elements.spread()
            .filterIsInstance<Element.Link>()
            .map { it.link }
        val actualBlockCode = prepare<Element.BlockCode>(result.elements) //optionally
        val actualOrderedList = prepare<Element.OrderedListItem>(result.elements) //optionally

        Assert.assertEquals(expectedMarkdown["unorderedList"], actualUnorderedList)
        Assert.assertEquals(expectedMarkdown["header"], actualHeaders)
        Assert.assertEquals(expectedMarkdown["quote"], actualQuotes)
        Assert.assertEquals(expectedMarkdown["italic"], actualItalic)
        Assert.assertEquals(expectedMarkdown["bold"], actualBold)
        Assert.assertEquals(expectedMarkdown["strike"], actualStrike)
        Assert.assertEquals(3, actualRule.size)
        Assert.assertEquals(expectedMarkdown["inline"], actualInline)
        Assert.assertEquals(expectedMarkdown["linkTitles"], actualLinkTitles)
        Assert.assertEquals(expectedMarkdown["links"], actualLinks)
        Assert.assertEquals(expectedMarkdown["multiline"], actualBlockCode)
        Assert.assertEquals(expectedMarkdown["orderedList"], actualOrderedList)
    }



    private fun printResults(list:List<String>){
        val iterator = list.iterator()
        while (iterator.hasNext()){
            println("find >> ${iterator.next()}")
        }
    }

    private fun printElements(list:List<Element>){
        val iterator = list.iterator()
        while (iterator.hasNext()){
            println("element >> ${iterator.next()}")
        }
    }

    private fun Element.spread():List<Element>{
        val elements = mutableListOf<Element>()
        elements.add(this)
        elements.addAll(this.elements.spread())
        return elements
    }

    private fun List<Element>.spread():List<Element>{
        val elements = mutableListOf<Element>()
        if(this.isNotEmpty()) elements.addAll(
            this.fold(mutableListOf()){acc, el -> acc.also { it.addAll(el.spread()) }}
        )
        return elements
    }

    private inline fun <reified T:Element> prepare(list:List<Element>) : List<String>{
        return list
            .fold(mutableListOf<Element>()){ acc, el -> //spread inner elements
                acc.also { it.addAll(el.spread()) }
            }
            .filterIsInstance<T>() //filter only expected instance
            .map { it.text.toString() } //transform to element text
    }
}